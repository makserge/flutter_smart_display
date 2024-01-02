import 'package:flutter/material.dart';
import 'package:flutter_smart_display/ui/dialogs/update_sensor_item_alert/update_sensor_item_alert_dialog_model.dart';
import 'package:flutter_smart_display/ui/views/sensors_update_item/sensors_update_item_view.dart';
import 'package:stacked/stacked.dart';
import 'package:stacked_services/stacked_services.dart';

import '../../../models/sensor.dart';

class UpdateSensorItemAlertDialog
    extends StackedView<UpdateSensorItemAlertDialogModel> {
  final DialogRequest request;
  final Function(DialogResponse) completer;

  const UpdateSensorItemAlertDialog({
    super.key,
    required this.request,
    required this.completer,
  });

  @override
  Widget builder(
    BuildContext context,
    UpdateSensorItemAlertDialogModel viewModel,
    Widget? child,
  ) {
    return Dialog.fullscreen(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
        child: SensorsUpdateItemView(
          item: request.data as Sensor,
          onCloseDialog: () => completer(DialogResponse(confirmed: true)),
          onUpdateItem: (item) =>
              completer(DialogResponse(confirmed: true, data: item)),
        ),
      ),
    );
  }

  @override
  UpdateSensorItemAlertDialogModel viewModelBuilder(BuildContext context) =>
      UpdateSensorItemAlertDialogModel();
}
