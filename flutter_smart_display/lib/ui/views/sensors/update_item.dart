import 'package:flutter/material.dart';
import 'package:stacked_services/stacked_services.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

import '../../common/ui_helpers.dart';

class UpdateItem extends StatelessWidget {
  const UpdateItem({
    super.key,
    required this.request,
    required this.onClick,
  });

  final DialogRequest request;
  final Function() onClick;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Expanded(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    AppLocalizations.of(context)!.addSensor,
                    style: const TextStyle(
                        fontSize: 16, fontWeight: FontWeight.w900),
                  ),
                  verticalSpaceTiny,
                  /*
                  Text(
                    request.description,
                    style:
                    const TextStyle(fontSize: 14, color: kcMediumGrey),
                    maxLines: 3,
                    softWrap: true,
                  ),

                   */
                ],
              ),
            ),
          ],
        ),
        verticalSpaceMedium,
        Row(
          mainAxisAlignment: MainAxisAlignment.end,
          children: [
            FlatButton(
                title: AppLocalizations.of(context)!.addSensorCancelButton,
                onClick: onClick
            ),
            FlatButton(
                title: AppLocalizations.of(context)!.addSensorAddButton,
                onClick: onClick
            ),
          ],
        )
      ],
    );
  }
}

class FlatButton extends StatelessWidget {
  const FlatButton({
    super.key,
    required this.title,
    required this.onClick,
  });

  final String title;
  final Function() onClick;

  @override
  Widget build(BuildContext context) {
    return TextButton(
      onPressed: onClick,
      child: Text(
        title,
        style: const TextStyle(
          fontWeight: FontWeight.bold,
          fontSize: 16,
        ),
      ),
    );
  }
}