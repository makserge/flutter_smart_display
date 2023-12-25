import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

class NoItems extends StatelessWidget {
  final Function() onClick;

  const NoItems({
    super.key,
    required this.onClick,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: GestureDetector(
          onTap: onClick,
          child: Text(
              AppLocalizations.of(context)!.noSensorsItems,
              style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w900
              )
          )
      ),
    );
  }
}