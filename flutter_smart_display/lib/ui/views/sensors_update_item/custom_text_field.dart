import 'package:flutter/material.dart';
import 'package:flutter_smart_display/ui/views/sensors_update_item/sensors_update_item_viewmodel.dart';

class CustomTextField extends StatelessWidget {
  const CustomTextField(
      {super.key,
      required this.viewModel,
      required this.controller,
      this.focusNode,
      required this.title});

  final SensorsUpdateItemViewModel viewModel;
  final TextEditingController controller;
  final FocusNode? focusNode;
  final String title;

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      maxLines: 1,
      decoration: InputDecoration(
        contentPadding: const EdgeInsets.all(10),
        border: const OutlineInputBorder(),
        hintText: title,
      ),
      controller: controller,
      focusNode: focusNode,
    );
  }
}
