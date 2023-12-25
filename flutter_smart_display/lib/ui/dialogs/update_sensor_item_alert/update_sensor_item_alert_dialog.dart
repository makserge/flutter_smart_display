import 'package:flutter/material.dart';
import 'package:flutter_smart_display/ui/dialogs/update_sensor_item_alert/update_sensor_item_alert_dialog_model.dart';
import 'package:stacked/stacked.dart';
import 'package:stacked_services/stacked_services.dart';

import '../../views/sensors/update_item.dart';

class UpdateSensorItemAlertDialog extends StackedView<UpdateSensorItemAlertDialogModel> {
  final DialogRequest request;
  final Function(DialogResponse) completer;

  const UpdateSensorItemAlertDialog({
    Key? key,
    required this.request,
    required this.completer,
  }) : super(key: key);

  @override
  Widget builder(
    BuildContext context,
    UpdateSensorItemAlertDialogModel viewModel,
    Widget? child,
  ) {
    return Dialog.fullscreen(
      backgroundColor: Colors.red,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
        child: UpdateItem(
          request: request,
          onClick: () => completer(DialogResponse(
            confirmed: true,
          ))
        ),
      ),
    );
  }

  @override
  UpdateSensorItemAlertDialogModel viewModelBuilder(BuildContext context) =>
      UpdateSensorItemAlertDialogModel();
}


