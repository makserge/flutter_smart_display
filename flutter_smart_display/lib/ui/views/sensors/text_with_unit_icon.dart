import 'package:flutter/material.dart';

import '../../common/app_colors.dart';
import '../../common/ui_helpers.dart';

class TextWithUnitIcon extends StatelessWidget {
  const TextWithUnitIcon(
      {super.key,
      required this.text,
      this.unit = "",
      this.icon = "",
      required this.editMode});

  final String text;
  final String unit;
  final String icon;
  final bool editMode;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        if (icon.isNotEmpty) ...[Icon(getIconByKey(icon)), horizontalSpaceTiny],
        SizedBox(
          width: editMode ? 27 : 35,
          child: Text(text,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style:
                  Font.apply(FontSize.h7, FontStyle.regular, kcPrimaryColor)),
        ),
        SizedBox(
          width: 30,
          child: Text(unit,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style:
                  Font.apply(FontSize.h7, FontStyle.regular, kcPrimaryColor)),
        ),
      ],
    );
  }
}
