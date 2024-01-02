import 'package:flutter_smart_display/models/sensor_type.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'sensor.freezed.dart';
part 'sensor.g.dart';

@freezed
class Sensor with _$Sensor {
  const factory Sensor({
    @JsonKey(includeIfNull: false) int? id,
    required String title,
    @Default("") String titleIcon,
    required String topic1,
    @Default("") String topic1Unit,
    @Default("") String topic1Icon,
    @Default("") String topic2,
    @Default("") String topic2Unit,
    @Default("") String topic2Icon,
    @Default("") String topic3,
    @Default("") String topic3Unit,
    @Default("") String topic3Icon,
    @Default("") String topic4,
    @Default("") String topic4Unit,
    @Default("") String topic4Icon,
    @Default("MQTT") String type,
  }) = _Sensor;

  factory Sensor.fromJson(Map<String, Object?> json) => _$SensorFromJson(json);

  static Sensor emptySensor = Sensor(
      id: null,
      title: "",
      titleIcon: "",
      topic1: "",
      topic1Unit: "",
      topic1Icon: "",
      topic2: "",
      topic2Unit: "",
      topic2Icon: "",
      topic3: "",
      topic3Unit: "",
      topic3Icon: "",
      topic4: "",
      topic4Unit: "",
      topic4Icon: "",
      type: SensorType.defaultType.name);
}
