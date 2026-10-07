package com.smsoft.smartdisplay.ui.screen.dashboard.mqtt

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.service.mqtt.MqttCallbackDispatcher
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import com.smsoft.smartdisplay.ui.screen.settings.MQTT_SERVER_DEFAULT_HOST
import com.smsoft.smartdisplay.ui.screen.settings.MQTT_SERVER_DEFAULT_PORT
import com.smsoft.smartdisplay.data.SensorType
import com.smsoft.smartdisplay.data.database.repository.SensorRepository
import com.smsoft.smartdisplay.ui.composable.settings.MESSAGE_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_STATUS_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_ALARM_DEFAULT_TOPIC
import info.mqtt.android.service.QoS
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

/** [isRetained]: a stored message the broker replays on every subscribe, not a new event. */
data class DashboardMqttMessage(val topic: String, val payload: String, val isRetained: Boolean = false)

class DashboardMqttManager(
    private val dataStore: DataStore<Preferences>,
    private val mqttClient: MqttAndroidClient,
    private val mqttCallbackDispatcher: MqttCallbackDispatcher,
    private val sensorRepository: SensorRepository,
) {
    private var scope: CoroutineScope? = null
    private var retryDelayMs = CONNECT_RETRY_INITIAL_MS
    // Once a connection was established, paho's automatic reconnect takes over.
    @Volatile
    private var hasConnectedOnce = false
    private var retryJob: Job? = null

    // The topics this manager subscribed on the broker. Only syncSubscriptions changes them; it
    // is the only owner of the dashboard's subscriptions (Settings used to subscribe on its own,
    // only while it was open, and nothing updated the controllers).
    private val subscriptionMutex = Mutex()
    private var subscribedTopics: Set<String> = emptySet()

    private val incomingMessagesInt = MutableSharedFlow<DashboardMqttMessage>(extraBufferCapacity = 16)
    val incomingMessages = incomingMessagesInt.asSharedFlow()

    private val mqttClientCallback = object : MqttCallbackExtended {
        override fun connectComplete(
            reconnect: Boolean,
            serverURI: String
        ) {
            if (reconnect) {
                Log.d(TAG, "Reconnected: $serverURI")
            } else {
                Log.d(TAG, "Connected: $serverURI")
            }
            hasConnectedOnce = true
            retryDelayMs = CONNECT_RETRY_INITIAL_MS
            // A manual retry must not run next to paho's own reconnect cycle from now on.
            retryJob?.cancel()
            // The broker may have lost the session (restart without persistence, expiry, new
            // client id), so the topics are subscribed again on every connect. paho calls this
            // for the first connect as well.
            scope?.launch(Dispatchers.IO) {
                syncSubscriptions(resubscribeAll = true)
            }
        }

        override fun connectionLost(
            cause: Throwable?
        ) {
            Log.d(TAG, "The Connection was lost.")
        }

        override fun messageArrived(
            topic: String,
            message: MqttMessage
        ) {
            Log.d(TAG, "messageArrived: $topic: $message")
            incomingMessagesInt.tryEmit(
                DashboardMqttMessage(
                    topic = topic,
                    payload = message.payload.toString(Charsets.UTF_8),
                    isRetained = message.isRetained
                )
            )
        }

        override fun deliveryComplete(
            token: IMqttDeliveryToken
        ) {
        }
    }

    fun start(scope: CoroutineScope) {
        this.scope = scope
        mqttCallbackDispatcher.addListener(mqttClientCallback)
        scope.launch(Dispatchers.IO) {
            connect()
        }
        // Topic changes in Settings and sensor edits apply at once. They used to apply only on
        // the next connect, in practice after a restart.
        scope.launch(Dispatchers.IO) {
            combine(
                dataStore.data.map(::readDashboardTopics),
                sensorRepository.getByType(SensorType.MQTT.id).map(::sensorTopics)
            ) { dashboardTopics, sensorTopics -> dashboardTopics + sensorTopics }
                .distinctUntilChanged()
                .collect {
                    syncSubscriptions(resubscribeAll = false)
                }
        }
        // A changed broker address or login needs a new connection. The client's address was
        // fixed when the app started, so setting up the broker for the first time needed a
        // restart.
        scope.launch(Dispatchers.IO) {
            dataStore.data.map(::readBrokerSettings).distinctUntilChanged().drop(1).collect {
                reconnect()
            }
        }
    }

    fun stop() {
        mqttCallbackDispatcher.removeListener(mqttClientCallback)
        retryJob?.cancel()
        scope = null
    }

    fun publish(scope: CoroutineScope, topic: String, messagePayload: String) {
        if (!isClientConnected()) {
            return
        }
        scope.launch(Dispatchers.IO) {
            try {
                mqttClient.publish(
                    topic = topic,
                    message = MqttMessage().apply {
                        payload = messagePayload.toByteArray()
                    },
                    userContext = null,
                    callback = object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.d(TAG, "publishMQTT Error")
                        }
                    }
                )
            } catch (e: Exception) {
                // E.g. the connection is being replaced after a broker change.
                Log.d(TAG, "Publish to $topic failed", e)
            }
        }
    }

    /**
     * isConnected of MqttAndroidClient throws ("Invalid ClientHandle") while a disconnect is
     * completing: the service drops the connection before the client hears about it.
     */
    private fun isClientConnected(): Boolean = try {
        mqttClient.isConnected
    } catch (e: Exception) {
        false
    }

    private suspend fun connect() {
        if (isClientConnected()) {
            // E.g. the dashboard was recreated while the singleton client stayed connected.
            syncSubscriptions(resubscribeAll = true)
            return
        }
        val broker = readBrokerSettings(dataStore.data.first())
        val mqttConnectOptions = try {
            MqttConnectOptions().apply {
                userName = broker.login.trim()
                password = broker.password.trim().toCharArray()
                isAutomaticReconnect = true
                isCleanSession = false
                // The current address from Settings; the client was created with the address
                // that was set when the app started. paho validates it here.
                serverURIs = arrayOf(broker.uri)
            }
        } catch (e: IllegalArgumentException) {
            // A host or port paho cannot use (e.g. "tcp://host" or "host/" typed into Settings).
            // No retry: it fails the same way until the setting changes, and the settings
            // observer then calls reconnect(). This used to crash the app at every start.
            Log.w(TAG, "Invalid MQTT broker address ${broker.uri}", e)
            return
        }
        // MqttAndroidClient can report a stale failure on an old, already finished connect token
        // (e.g. after a network change); only the first result of this attempt counts.
        val isHandled = AtomicBoolean(false)
        val mqttClientListener = object : IMqttActionListener {
            override fun onSuccess(
                asyncActionToken: IMqttToken
            ) {
                if (!isHandled.compareAndSet(false, true)) {
                    return
                }
                val disconnectedBufferOptions = DisconnectedBufferOptions().apply {
                    isBufferEnabled = true
                    bufferSize = 100
                    isPersistBuffer = false
                    isDeleteOldestMessages = false
                }
                mqttClient.setBufferOpts(disconnectedBufferOptions)
                retryDelayMs = CONNECT_RETRY_INITIAL_MS
            }

            override fun onFailure(
                asyncActionToken: IMqttToken?,
                exception: Throwable?
            ) {
                if (!isHandled.compareAndSet(false, true)) {
                    return
                }
                Log.d(TAG, "Failed to connect", exception)
                scheduleConnectRetry()
            }
        }
        try {
            mqttClient.connect(
                options = mqttConnectOptions,
                userContext = null,
                callback = mqttClientListener
            )
        } catch (e: Exception) {
            Log.d(TAG, "Failed to connect", e)
            scheduleConnectRetry()
        }
    }

    /**
     * Automatic reconnect only covers a connection that was established once. When the broker is
     * unreachable at start (e.g. the panel boots faster than the broker), keep trying.
     */
    private fun scheduleConnectRetry() {
        if (hasConnectedOnce || isClientConnected()) {
            return
        }
        val retryScope = scope ?: return
        val delayMs = retryDelayMs
        retryDelayMs = (retryDelayMs * 2).coerceAtMost(CONNECT_RETRY_MAX_MS)
        retryJob = retryScope.launch(Dispatchers.IO) {
            delay(delayMs)
            // paho may have connected by itself meanwhile (e.g. on a network change).
            if (hasConnectedOnce) {
                return@launch
            }
            connect()
        }
    }

    /**
     * Makes the broker subscriptions match every topic the dashboard and the MQTT sensors listen
     * to: subscribes new topics and unsubscribes topics nobody uses any more. A topic that is
     * still used elsewhere (another dashboard topic or a sensor) is never unsubscribed.
     * [resubscribeAll] after a connect: the broker may have lost the session.
     */
    private suspend fun syncSubscriptions(resubscribeAll: Boolean) = subscriptionMutex.withLock {
        if (!isClientConnected()) {
            // The next connectComplete syncs.
            return@withLock
        }
        val wanted = readDashboardTopics(dataStore.data.first()) +
            sensorTopics(sensorRepository.getByType(SensorType.MQTT.id).first())
        (subscribedTopics - wanted).forEach { topic ->
            try {
                mqttClient.unsubscribe(topic)
            } catch (e: Exception) {
                Log.d(TAG, "Unsubscribe from $topic failed", e)
            }
        }
        val toSubscribe = if (resubscribeAll) wanted else (wanted - subscribedTopics)
        toSubscribe.forEach { topic ->
            try {
                mqttClient.subscribe(
                    topic = topic,
                    qos = QoS.AtMostOnce.value
                )
            } catch (e: Exception) {
                Log.d(TAG, "Subscribe to $topic failed", e)
            }
        }
        subscribedTopics = wanted
    }

    /** Connects again with the broker settings from Settings. */
    private suspend fun reconnect() {
        Log.d(TAG, "Broker settings changed, reconnecting")
        retryJob?.cancel()
        hasConnectedOnce = false
        retryDelayMs = CONNECT_RETRY_INITIAL_MS
        subscriptionMutex.withLock {
            subscribedTopics = emptySet()
        }
        if (isClientConnected()) {
            // Leaves the old broker. Not done otherwise: while paho is in its own reconnect cycle
            // a disconnect only drops the service's record of the connection and the old client
            // keeps reconnecting to the old broker. connect() reuses that client instead, and
            // the new options also become the ones its reconnect cycle uses.
            awaitDisconnect()
        }
        connect()
    }

    private suspend fun awaitDisconnect() {
        val isDone = CompletableDeferred<Unit>()
        try {
            // No quiesce time: with an unanswered ping or message pending, paho would otherwise
            // wait up to 30 s before it reports the disconnect.
            mqttClient.disconnect(
                quiesceTimeout = 0L,
                userContext = null,
                callback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        isDone.complete(Unit)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        isDone.complete(Unit)
                    }
                }
            )
        } catch (e: Exception) {
            // Not connected: nothing to wait for.
            isDone.complete(Unit)
        }
        withTimeoutOrNull(DISCONNECT_TIMEOUT_MS) {
            isDone.await()
        }
    }

    companion object {
        private const val DISCONNECT_TIMEOUT_MS = 5_000L
        private const val TAG = "DashboardMqttManager"
        private const val CONNECT_RETRY_INITIAL_MS = 10_000L
        private const val CONNECT_RETRY_MAX_MS = 5 * 60_000L
    }
}

private data class BrokerSettings(
    val uri: String,
    val login: String,
    val password: String
)

private fun readBrokerSettings(data: Preferences): BrokerSettings {
    val host = data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_HOST.key)] ?: MQTT_SERVER_DEFAULT_HOST
    val port = data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_PORT.key)] ?: MQTT_SERVER_DEFAULT_PORT
    return BrokerSettings(
        uri = "tcp://" + host.trim() + ":" + port.trim(),
        login = data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_USERNAME.key)] ?: "",
        password = data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_PASSWORD.key)] ?: ""
    )
}

/** The doorbell, push-button status and message topics, with the same defaults as before. */
private fun readDashboardTopics(data: Preferences): Set<String> = normalizeTopics(
    listOf(
        data[stringPreferencesKey(PreferenceKey.DOORBELL_ALARM_TOPIC.key)] ?: DOORBELL_ALARM_DEFAULT_TOPIC,
        data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_STATUS_TOPIC.key)] ?: PUSH_BUTTON_STATUS_DEFAULT_TOPIC,
        data[stringPreferencesKey(PreferenceKey.MESSAGE_TOPIC.key)] ?: MESSAGE_DEFAULT_TOPIC
    )
)

private fun sensorTopics(sensors: List<com.smsoft.smartdisplay.data.database.entity.Sensor>): Set<String> =
    normalizeTopics(sensors.flatMap { listOf(it.topic1, it.topic2, it.topic3, it.topic4) })

private fun normalizeTopics(topics: List<String>): Set<String> =
    topics.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
