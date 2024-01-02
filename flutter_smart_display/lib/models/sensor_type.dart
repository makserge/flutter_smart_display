enum SensorType {
  mqtt,
  bluetooth;

  static SensorType get defaultType => mqtt;

  static bool isBluetooth(String type) => type == SensorType.bluetooth.name;
}
