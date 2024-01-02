import 'package:flutter/cupertino.dart';
import 'package:flutter_smart_display/models/sensor.dart';
import 'package:flutter_smart_display/ui/views/sensors_update_item/sensors_update_item_view.form.dart';
import 'package:stacked/stacked.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

import '../../../models/sensor_type.dart';

class SensorsUpdateItemViewModel extends FormViewModel
    with $SensorsUpdateItemView {
  late int? id;
  String type = SensorType.defaultType.name;
  late String titleIcon;
  late String topic1Icon;
  late String topic2Icon;
  late String topic3Icon;
  late String topic4Icon;

  @override
  SensorsUpdateItemViewModel({required Sensor item}) {
    id = item.id;
    type = item.type;

    titleIcon = item.titleIcon;
    topic1Icon = item.topic1Icon;
    topic2Icon = item.topic2Icon;
    topic3Icon = item.topic3Icon;
    topic4Icon = item.topic4Icon;

    titleInputController.text = item.title;
    topic1InputController.text = item.topic1;
    unit1InputController.text = item.topic1Unit;
    topic2InputController.text = item.topic2;
    unit2InputController.text = item.topic2Unit;
    topic3InputController.text = item.topic3;
    unit3InputController.text = item.topic3Unit;
    topic4InputController.text = item.topic4;
    unit4InputController.text = item.topic4Unit;
  }

  String localisedMessage(BuildContext context, String key) {
    switch (key) {
      case 'empty':
        return AppLocalizations.of(context)!.emptyValue;
      default:
        return '';
    }
  }

  bool checkRequiredFields() {
    if (hasTitleInputValidationMessage) {
      titleInputFocusNode.requestFocus();
      return false;
    }
    if (SensorType.isBluetooth(type)) {
      //    if (selectedBleDevice.address.isEmpty()) {
      //     return false;
      // }
    } else {
      if (hasTopic1InputValidationMessage) {
        topic1InputFocusNode.requestFocus();
        return false;
      }
    }
    return true;
  }

  void stopBleScan() {}

  Sensor updateItem(Sensor item) {
    if (SensorType.isBluetooth(type)) {
      return item;
      /*
      return getSensorByBluetoothType(
          id = item.id,
          title = title,
          type = selectedBleDevice.deviceName,
          address = selectedBleDevice.address
      )

     */
    } else {
      return item.copyWith(
          title: titleInputController.text,
          titleIcon: titleIcon,
          topic1: topic1InputController.text,
          topic1Unit: unit1InputController.text,
          topic1Icon: topic1Icon,
          topic2: topic2InputController.text,
          topic2Unit: unit2InputController.text,
          topic2Icon: topic2Icon,
          topic3: topic3InputController.text,
          topic3Unit: unit3InputController.text,
          topic3Icon: topic3Icon,
          topic4: topic4InputController.text,
          topic4Unit: unit4InputController.text,
          topic4Icon: topic4Icon,
          type: type);
    }
  }
}

class InputValidators {
  static String? validateRequiredText(String? value) {
    if (value == null) {
      return null;
    }
    if (value.isEmpty) {
      return 'empty';
    }
    return null;
  }
}
