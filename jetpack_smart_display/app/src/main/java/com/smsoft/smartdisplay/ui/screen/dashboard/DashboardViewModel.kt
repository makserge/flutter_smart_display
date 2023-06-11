package com.smsoft.smartdisplay.ui.screen.dashboard

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsoft.smartdisplay.data.PreferenceKey
import dagger.hilt.android.lifecycle.HiltViewModel
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject


@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val mqttClient: MqttAndroidClient
) : ViewModel() {

    fun sendPressButtonEvent() {
        if (!mqttClient.isConnected) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val message = MqttMessage().apply {
                payload = MQTT_PRESS_BUTTON_MESSAGE.toByteArray()
            }
            mqttClient.publish(
                topic = getTopic(dataStore),
                message = message,
                userContext = null,
                callback = object: IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d("DashboardViewModel", "sendPressButtonEvent Ok")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d("DashboardViewModel", "sendPressButtonEvent Error")
                    }
                }
            )
        }
    }

    private suspend fun getTopic(
        dataStore: DataStore<Preferences>
    ): String {
        val data = dataStore.data.first()
        var topic = PUSH_BUTTON_DEFAULT_TOPIC
        data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_TOPIC.key)]?.let {
            topic = it.trim()
        }
        return topic
    }

    private val MQTT_PRESS_BUTTON_MESSAGE = "1"
}

const val PUSH_BUTTON_DEFAULT_TOPIC = "pushbutton"