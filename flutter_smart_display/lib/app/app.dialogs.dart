// GENERATED CODE - DO NOT MODIFY BY HAND

// **************************************************************************
// StackedDialogGenerator
// **************************************************************************

import 'package:stacked_services/stacked_services.dart';

import 'app.locator.dart';
import '../ui/dialogs/update_sensor_item_alert/update_sensor_item_alert_dialog.dart';

enum DialogType {
  updateSensorItemAlert,
}

void setupDialogUi() {
  final dialogService = locator<DialogService>();

  final Map<DialogType, DialogBuilder> builders = {
    DialogType.updateSensorItemAlert: (context, request, completer) =>
        UpdateSensorItemAlertDialog(request: request, completer: completer),
  };

  dialogService.registerCustomDialogBuilders(builders);
}
