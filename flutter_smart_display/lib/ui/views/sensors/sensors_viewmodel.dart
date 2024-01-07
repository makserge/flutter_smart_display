import 'package:stacked/stacked.dart';
import 'package:stacked_services/stacked_services.dart';
import 'package:mqtt_client/mqtt_client.dart';
import 'package:mqtt_client/mqtt_server_client.dart' as mqtt;
import 'package:uuid/uuid.dart';

import '../../../app/app.dialogs.dart';
import '../../../app/app.locator.dart';
import '../../../models/sensor_type.dart';
import '../../../services/database_service.dart';
import '../../../models/sensor.dart';

const mqttHost = "192.168.8.100";
const mqttUsername = "*";
const mqttPassword = "*";

class SensorsViewModel extends BaseViewModel {
  final _dialogService = locator<DialogService>();
  final _databaseService = locator<DatabaseService>();

  bool _isOpenEditItemDialog = false;
  bool get isOpenEditItemDialog => _isOpenEditItemDialog;

  bool _editMode = false;
  bool get editMode => _editMode;

  List<Sensor> _items = [];
  List<Sensor> get data => _items;

  late final mqtt.MqttServerClient _mqttClient;
  final String _mqttClientId = const Uuid().v1();
  final Map<String, String> _mqttTopicData = {};
  Map<String, String> get mqttTopicData => _mqttTopicData;

  set editMode(bool value) {
    _editMode = value;
    rebuildUi();
  }

  runStartupLogic() {
    _getItems();
    _initMqtt();
  }

  void openAddItemDialog() {
    _isOpenEditItemDialog = true;
    _showUpdateItemDialog(Sensor.emptySensor);
  }

  void openEditItemDialog(Sensor item) {
    _isOpenEditItemDialog = true;
    _showUpdateItemDialog(item);
  }

  void deleteItem(Sensor item) async {
    _databaseService.deleteSensor(item: item);
    _unSubscribeMqttTopic(item);
    _getItems();
  }

  void _showUpdateItemDialog(Sensor item) async {
    var response = await _dialogService.showCustomDialog(
      variant: DialogType.updateSensorItemAlert,
      data: item,
    );
    if (response?.data != null) {
      _databaseService.updateSensor(item: response!.data);
      _subscribeToMqttTopic(item);
    }
    _isOpenEditItemDialog = false;
    _getItems();
  }

  void _getItems() {
    _databaseService.getSensors().then((value) {
      _items = value;
      rebuildUi();
    });
  }

  void _initMqtt() async {
    _mqttClient = mqtt.MqttServerClient(mqttHost, _mqttClientId);
    _mqttClient.setProtocolV311();
    _mqttClient.autoReconnect = true;
    try {
      await _mqttClient.connect(mqttUsername, mqttPassword);
    } catch (e) {
      _mqttClient.disconnect();
    }

    _initMqttSubscription();
  }

  void _initMqttSubscription() {
    _mqttClient.updates!.listen((List<MqttReceivedMessage<MqttMessage>> c) {
      final MqttPublishMessage recMess = c[0].payload as MqttPublishMessage;
      final topic = c[0].topic;
      final String message =
          MqttPublishPayload.bytesToStringAsString(recMess.payload.message);

      _mqttTopicData[topic] = message;

      rebuildUi();
    });

    for (var item in _items) {
      if (!SensorType.isBluetooth(item.type)) {
        _subscribeToMqttTopic(item);
      }
    }
  }

  void _subscribeToMqttTopic(Sensor item) {
    if (SensorType.isBluetooth(item.type)) {
      return;
    }
    if (item.topic1.isNotEmpty) {
      _mqttClient.subscribe(item.topic1, MqttQos.atMostOnce);
    }
    if (item.topic2.isNotEmpty) {
      _mqttClient.subscribe(item.topic2, MqttQos.atMostOnce);
    }
    if (item.topic3.isNotEmpty) {
      _mqttClient.subscribe(item.topic3, MqttQos.atMostOnce);
    }
    if (item.topic4.isNotEmpty) {
      _mqttClient.subscribe(item.topic4, MqttQos.atMostOnce);
    }
  }

  void _unSubscribeMqttTopic(Sensor item) {
    if (SensorType.isBluetooth(item.type)) {
      return;
    }
    if (item.topic1.isNotEmpty) {
      _mqttClient.unsubscribe(item.topic1);
    }
    if (item.topic2.isNotEmpty) {
      _mqttClient.unsubscribe(item.topic2);
    }
    if (item.topic3.isNotEmpty) {
      _mqttClient.unsubscribe(item.topic3);
    }
    if (item.topic4.isNotEmpty) {
      _mqttClient.unsubscribe(item.topic4);
    }
  }
}
