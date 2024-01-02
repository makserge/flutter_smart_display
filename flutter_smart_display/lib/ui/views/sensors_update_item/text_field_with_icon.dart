import 'package:flutter/material.dart';
import 'package:flutter_smart_display/ui/views/sensors_update_item/item_type_selector.dart';
import 'package:flutter_smart_display/ui/views/sensors_update_item/sensors_update_item_viewmodel.dart';

import '../../common/ui_helpers.dart';
import 'icon_picker.dart';
import 'custom_text_field.dart';

class TextFieldWithIcon extends StatelessWidget {
  const TextFieldWithIcon(
      {super.key,
      required this.viewModel,
      required this.titleController,
      this.focusNode,
      this.unitController,
      this.showUnit = false,
      this.showType = false,
      required this.title,
      this.unit = "",
      this.unitTitle = "",
      this.icon = "",
      this.type = "",
      this.onIconChange,
      this.onTypeChange});

  final SensorsUpdateItemViewModel viewModel;
  final TextEditingController titleController;
  final TextEditingController? unitController;
  final FocusNode? focusNode;
  final bool showUnit;
  final bool showType;
  final String title;
  final String unitTitle;
  final String unit;
  final String icon;
  final String type;
  final Function(String value)? onIconChange;
  final Function(String value)? onTypeChange;

  @override
  Widget build(BuildContext context) {
    return Row(children: [
      Expanded(
        flex: 4,
        child: CustomTextField(
          viewModel: viewModel,
          controller: titleController,
          focusNode: focusNode,
          title: title,
        ),
      ),
      if (showUnit) ...[
        horizontalSpaceTiny,
        Expanded(
          flex: 1,
          child: CustomTextField(
            viewModel: viewModel,
            controller: unitController!,
            title: unitTitle,
          ),
        ),
      ],
      horizontalSpaceTiny,
      Expanded(
          flex: 2,
          child: IconPicker(
            value: icon,
            onValueChange: onIconChange ?? (v) => v,
          )),
      if (showType) ...[
        horizontalSpaceTiny,
        Expanded(
          flex: 2,
          child: ItemTypeSelector(
            value: type,
            onValueChange: onTypeChange ?? (v) => v,
          ),
        ),
      ],
    ]);
  }
}
