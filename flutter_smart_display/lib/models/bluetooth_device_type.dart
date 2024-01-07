enum BluetoothDeviceType {
  thermobeacon("ThermoBeacon"),
  atc("ATC_");

  const BluetoothDeviceType(this.title);
  final String title;

  static List<String> toList() {
    List<String> result = [];
    for (var element in BluetoothDeviceType.values) {
      result.add(element.title);
    }
    return result;
  }
}