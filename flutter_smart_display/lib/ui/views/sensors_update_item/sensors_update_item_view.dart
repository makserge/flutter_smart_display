import 'package:flutter/material.dart';
import 'package:flutter_smart_display/ui/views/sensors_update_item/text_field_with_icon.dart';
import 'package:stacked/stacked.dart';
import 'package:stacked/stacked_annotations.dart';

import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import '../../../models/sensor_type.dart';
import '../../../models/sensor.dart';
import '../../common/ui_helpers.dart';
import 'bluetooth_devices.dart';
import 'flat_button.dart';
import 'sensors_update_item_viewmodel.dart';
import 'sensors_update_item_view.form.dart';

@FormView(fields: [
  FormTextField(
      name: 'titleInput', validator: InputValidators.validateRequiredText),
  FormTextField(
      name: 'topic1Input', validator: InputValidators.validateRequiredText),
  FormTextField(name: 'unit1Input'),
  FormTextField(name: 'topic2Input'),
  FormTextField(name: 'unit2Input'),
  FormTextField(name: 'topic3Input'),
  FormTextField(name: 'unit3Input'),
  FormTextField(name: 'topic4Input'),
  FormTextField(name: 'unit4Input'),
])
class SensorsUpdateItemView extends StackedView<SensorsUpdateItemViewModel>
    with $SensorsUpdateItemView {
  const SensorsUpdateItemView(
      {super.key,
      required this.item,
      required this.onCloseDialog,
      required this.onUpdateItem});

  final Sensor item;
  final Function() onCloseDialog;
  final Function(Sensor item) onUpdateItem;

  @override
  SensorsUpdateItemViewModel viewModelBuilder(BuildContext context) =>
      SensorsUpdateItemViewModel(item: item);

  @override
  void onViewModelReady(SensorsUpdateItemViewModel viewModel) {
    super.onViewModelReady(viewModel);
    syncFormWithViewModel(viewModel);

    titleInputFocusNode.requestFocus();
  }

  @override
  void onDispose(SensorsUpdateItemViewModel viewModel) {
    super.onDispose(viewModel);
    disposeForm();
  }

  @override
  Widget builder(BuildContext context, SensorsUpdateItemViewModel viewModel,
      Widget? child) {

    Widget updateMqttTopics() {
      return Column(
        children: [
          TextFieldWithIcon(
            viewModel: viewModel,
            titleController: topic1InputController,
            focusNode: topic1InputFocusNode,
            unitController: unit1InputController,
            showUnit: true,
            title: AppLocalizations.of(context)!.addSensorTopic1,
            unitTitle: AppLocalizations.of(context)!.addSensorUnit,
            icon: viewModel.topic1Icon,
            onIconChange: (String value) => viewModel.topic1Icon = value,
          ),
          verticalSpaceTiny,
          TextFieldWithIcon(
            viewModel: viewModel,
            titleController: topic2InputController,
            unitController: unit2InputController,
            showUnit: true,
            title: AppLocalizations.of(context)!.addSensorTopic2,
            unitTitle: AppLocalizations.of(context)!.addSensorUnit,
            icon: viewModel.topic2Icon,
            onIconChange: (String value) => viewModel.topic2Icon = value,
          ),
          verticalSpaceTiny,
          TextFieldWithIcon(
            viewModel: viewModel,
            titleController: topic3InputController,
            unitController: unit3InputController,
            showUnit: true,
            title: AppLocalizations.of(context)!.addSensorTopic3,
            unitTitle: AppLocalizations.of(context)!.addSensorUnit,
            icon: viewModel.topic3Icon,
            onIconChange: (String value) => viewModel.topic3Icon = value,
          ),
          verticalSpaceTiny,
          TextFieldWithIcon(
            viewModel: viewModel,
            titleController: topic4InputController,
            unitController: unit4InputController,
            showUnit: true,
            title: AppLocalizations.of(context)!.addSensorTopic4,
            unitTitle: AppLocalizations.of(context)!.addSensorUnit,
            icon: viewModel.topic4Icon,
            onIconChange: (String value) => viewModel.topic4Icon = value,
          ),
        ],
      );
    }

    void onClose() {
      if (SensorType.isBluetooth(viewModel.type)) {
        viewModel.stopBleScan();
      }
      onCloseDialog();
    }

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Row(
          children: [
            Expanded(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    viewModel.id != null
                        ? AppLocalizations.of(context)!.editSensor
                        : AppLocalizations.of(context)!.addSensor,
                    style: textStyle,
                  ),
                  verticalSpaceMedium,
                  TextFieldWithIcon(
                      viewModel: viewModel,
                      titleController: titleInputController,
                      focusNode: titleInputFocusNode,
                      title: AppLocalizations.of(context)!.addSensorTitle,
                      icon: viewModel.titleIcon,
                      type: viewModel.type,
                      showType: true,
                      onIconChange: (String value) =>
                          viewModel.titleIcon = value,
                      onTypeChange: (String value) {
                        viewModel.type = value;
                        viewModel.rebuildUi();
                      }),
                  verticalSpaceTiny,
                  if (SensorType.isBluetooth(viewModel.type)) ...[
                    verticalSpaceTiny,
                    BluetoothDevices(
                        value: '',
                        onValueChange: (String value) {

                        },
                        onBluetoothPermissionsDenied: () {
                          viewModel.showBleErrorBottomSheet(context);
                        },
                        onRescan: () {
                          viewModel.startBleScan();
                        }
                      //state = bleScanState,
                      //selectedItem = selectedBleDevice,
                      //onClick = {
                      //  selectedBleDevice = it
                      //},
                      //onRescan = {
                      //  viewModel.startBleScan()
                      //}
                    )
                  ] else ...[
                    updateMqttTopics()
                  ]
                ],
              ),
            ),
          ],
        ),
        verticalSpaceTiny,
        Row(
          mainAxisAlignment: MainAxisAlignment.end,
          children: [
            FlatButton(
              title: AppLocalizations.of(context)!.addSensorCancelButton,
              onClick: onClose,
            ),
            FlatButton(
              title: viewModel.id != null
                  ? AppLocalizations.of(context)!.addSensorUpdateButton
                  : AppLocalizations.of(context)!.addSensorAddButton,
              onClick: () {
                if (!viewModel.checkRequiredFields()) {
                  return;
                }
                onUpdateItem(viewModel.updateItem(item));
                onClose();
              },
            ),
          ],
        )
      ],
    );
  }
}
