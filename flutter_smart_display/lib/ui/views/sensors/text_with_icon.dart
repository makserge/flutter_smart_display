import 'package:flutter/material.dart';

import '../../common/app_colors.dart';
import '../../common/ui_helpers.dart';

class TextWithIcon extends StatelessWidget {
  const TextWithIcon({
    super.key,
    required this.text,
    this.icon = "",
  });

  final String text;
  final String icon;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        if (icon.isNotEmpty) ...[Icon(getIconByKey(icon)), horizontalSpaceTiny],
        Text(text,
            style: Font.apply(FontSize.h5, FontStyle.regular, kcSecondaryColor))
      ],
    );
  }
}
