import 'package:flutter/material.dart';
import 'package:flutter_smart_display/models/sensor.dart';
import 'package:flutter_smart_display/ui/views/sensors/text_with_icon.dart';
import 'package:flutter_smart_display/ui/views/sensors/text_with_unit_icon.dart';

import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import '../../common/app_colors.dart';
import '../../common/ui_helpers.dart';

class Item extends StatelessWidget {
  const Item(
      {super.key,
      required this.item,
      required this.itemsData,
      required this.editMode,
      required this.onDelete,
      required this.onOptionsClick});

  final Sensor item;
  final Map<String, String> itemsData;
  final bool editMode;
  final Function() onDelete;
  final Function() onOptionsClick;

  @override
  Widget build(BuildContext context) {
    Widget cardContent() {
      return Column(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          TextWithIcon(text: item.title, icon: item.titleIcon),
          verticalSpaceSmall,
          Row(
            children: [
              Expanded(
                  child: TextWithUnitIcon(
                      text: itemsData[item.topic1] ?? "n/a",
                      unit: (itemsData[item.topic1] != null) ? item.topic1Unit : "",
                      icon: item.topic1Icon,
                      editMode: editMode)),
              if (item.topic2.isNotEmpty) ...[
                horizontalSpaceMicro,
                Expanded(
                  child: TextWithUnitIcon(
                      text: itemsData[item.topic2] ?? "n/a",
                      unit: (itemsData[item.topic2] != null) ? item.topic2Unit : "",
                      icon: item.topic2Icon,
                      editMode: editMode),
                ),
              ]
            ],
          ),
          Row(
            children: [
              if (item.topic3.isNotEmpty) ...[
                Expanded(
                    child: TextWithUnitIcon(
                        text: itemsData[item.topic3] ?? "n/a",
                        unit: (itemsData[item.topic3] != null) ? item.topic3Unit : "",
                        icon: item.topic3Icon,
                        editMode: editMode)),
              ],
              if (item.topic4.isNotEmpty) ...[
                horizontalSpaceMicro,
                Expanded(
                  child: TextWithUnitIcon(
                      text: itemsData[item.topic4] ?? "n/a",
                      unit: (itemsData[item.topic4] != null) ? item.topic4Unit : "",
                      icon: item.topic4Icon,
                      editMode: editMode),
                ),
              ]
            ],
          ),
        ],
      );
    }

    Widget cardEdit() {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.end,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        mainAxisSize: MainAxisSize.max,
        children: [
          IconButton(
            icon: Icon(
              Icons.delete,
              semanticLabel: AppLocalizations.of(context)!.delete,
            ),
            padding: const EdgeInsets.all(6),
            constraints: const BoxConstraints(),
            style: const ButtonStyle(
              tapTargetSize: MaterialTapTargetSize.shrinkWrap,
            ),
            onPressed: onDelete,
          ),
          IconButton(
            icon: Icon(
              Icons.edit,
              semanticLabel: AppLocalizations.of(context)!.edit,
            ),
            padding: const EdgeInsets.all(6),
            constraints: const BoxConstraints(),
            style: const ButtonStyle(
              tapTargetSize: MaterialTapTargetSize.shrinkWrap,
            ),
            onPressed: onOptionsClick,
          )
        ],
      );
    }

    return Card(
        color: kcDarkGreyColor,
        elevation: 8,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8.0),
        ),
        child: Padding(
          padding: const EdgeInsets.all(6.0),
          child: Row(
            children: [
              Expanded(flex: 5, child: cardContent()),
              if (editMode) ...[
                Expanded(
                  child: cardEdit(),
                ),
              ]
            ],
          ),
        ));
  }
}
