import 'package:flutter/material.dart';

import '../../common/ui_helpers.dart';

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
      child: Text(title, style: textStyle),
    );
  }
}
