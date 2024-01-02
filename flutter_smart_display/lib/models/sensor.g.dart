// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sensor.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$SensorImpl _$$SensorImplFromJson(Map<String, dynamic> json) => _$SensorImpl(
      id: json['id'] as int?,
      title: json['title'] as String,
      titleIcon: json['titleIcon'] as String? ?? "",
      topic1: json['topic1'] as String,
      topic1Unit: json['topic1Unit'] as String? ?? "",
      topic1Icon: json['topic1Icon'] as String? ?? "",
      topic2: json['topic2'] as String? ?? "",
      topic2Unit: json['topic2Unit'] as String? ?? "",
      topic2Icon: json['topic2Icon'] as String? ?? "",
      topic3: json['topic3'] as String? ?? "",
      topic3Unit: json['topic3Unit'] as String? ?? "",
      topic3Icon: json['topic3Icon'] as String? ?? "",
      topic4: json['topic4'] as String? ?? "",
      topic4Unit: json['topic4Unit'] as String? ?? "",
      topic4Icon: json['topic4Icon'] as String? ?? "",
      type: json['type'] as String? ?? "MQTT",
    );

Map<String, dynamic> _$$SensorImplToJson(_$SensorImpl instance) {
  final val = <String, dynamic>{};

  void writeNotNull(String key, dynamic value) {
    if (value != null) {
      val[key] = value;
    }
  }

  writeNotNull('id', instance.id);
  val['title'] = instance.title;
  val['titleIcon'] = instance.titleIcon;
  val['topic1'] = instance.topic1;
  val['topic1Unit'] = instance.topic1Unit;
  val['topic1Icon'] = instance.topic1Icon;
  val['topic2'] = instance.topic2;
  val['topic2Unit'] = instance.topic2Unit;
  val['topic2Icon'] = instance.topic2Icon;
  val['topic3'] = instance.topic3;
  val['topic3Unit'] = instance.topic3Unit;
  val['topic3Icon'] = instance.topic3Icon;
  val['topic4'] = instance.topic4;
  val['topic4Unit'] = instance.topic4Unit;
  val['topic4Icon'] = instance.topic4Icon;
  val['type'] = instance.type;
  return val;
}
