import 'dart:math';

import 'package:flutter/material.dart';

const double _microSize = 3.0;
const double _tinySize = 5.0;
const double _smallSize = 10.0;
const double _mediumSize = 25.0;
const double _largeSize = 50.0;
const double _massiveSize = 120.0;

const Widget horizontalSpaceMicro = SizedBox(width: _microSize);
const Widget horizontalSpaceTiny = SizedBox(width: _tinySize);
const Widget horizontalSpaceSmall = SizedBox(width: _smallSize);
const Widget horizontalSpaceMedium = SizedBox(width: _mediumSize);
const Widget horizontalSpaceLarge = SizedBox(width: _largeSize);

const Widget verticalSpaceTiny = SizedBox(height: _tinySize);
const Widget verticalSpaceSmall = SizedBox(height: _smallSize);
const Widget verticalSpaceMedium = SizedBox(height: _mediumSize);
const Widget verticalSpaceLarge = SizedBox(height: _largeSize);
const Widget verticalSpaceMassive = SizedBox(height: _massiveSize);

Widget spacedDivider = const Column(
  children: <Widget>[
    verticalSpaceMedium,
    Divider(color: Colors.blueGrey, height: 5.0),
    verticalSpaceMedium,
  ],
);

Widget verticalSpace(double height) => SizedBox(height: height);

double screenWidth(BuildContext context) => MediaQuery.of(context).size.width;
double screenHeight(BuildContext context) => MediaQuery.of(context).size.height;

double screenHeightFraction(BuildContext context,
        {int dividedBy = 1, double offsetBy = 0, double max = 3000}) =>
    min((screenHeight(context) - offsetBy) / dividedBy, max);

double screenWidthFraction(BuildContext context,
        {int dividedBy = 1, double offsetBy = 0, double max = 3000}) =>
    min((screenWidth(context) - offsetBy) / dividedBy, max);

double halfScreenWidth(BuildContext context) =>
    screenWidthFraction(context, dividedBy: 2);

double thirdScreenWidth(BuildContext context) =>
    screenWidthFraction(context, dividedBy: 3);

double quarterScreenWidth(BuildContext context) =>
    screenWidthFraction(context, dividedBy: 4);

double getResponsiveHorizontalSpaceMedium(BuildContext context) =>
    screenWidthFraction(context, dividedBy: 10);
double getResponsiveSmallFontSize(BuildContext context) =>
    getResponsiveFontSize(context, fontSize: 14, max: 15);

double getResponsiveMediumFontSize(BuildContext context) =>
    getResponsiveFontSize(context, fontSize: 16, max: 17);

double getResponsiveLargeFontSize(BuildContext context) =>
    getResponsiveFontSize(context, fontSize: 21, max: 31);

double getResponsiveExtraLargeFontSize(BuildContext context) =>
    getResponsiveFontSize(context, fontSize: 25);

double getResponsiveMassiveFontSize(BuildContext context) =>
    getResponsiveFontSize(context, fontSize: 30);

double getResponsiveFontSize(BuildContext context,
    {double? fontSize, double? max}) {
  max ??= 100;

  var responsiveSize = min(
      screenWidthFraction(context, dividedBy: 10) * ((fontSize ?? 100) / 100),
      max);

  return responsiveSize;
}

TextStyle textStyle = Font.apply(FontSize.h6, FontStyle.extraBold);

IconData? getIconByKey(String suggestion) {
  switch (suggestion) {
    case "co2":
      return Icons.co2_outlined;
    case "lightbulb":
      return Icons.lightbulb_outlined;
    case "toggle_on":
      return Icons.toggle_on_outlined;
    case "thermostat":
      return Icons.thermostat_outlined;
    case "humidity_low":
      return Icons.water_drop_outlined;
  }
  return null;
}

class Font {
  static apply(FontSize size, FontStyle style,
      [Color? color, TextDecoration? decoration]) {
    return TextStyle(
        color: color,
        fontWeight: style.value,
        fontSize: size.value,
        fontFamily: 'baloo2',
        decoration: decoration);
  }
}

//This enum is defined for text size in the form of headings
enum FontSize {
  h1,
  h2,
  h3,
  h4,
  h5,
  h6,
  h7,
  h8,
  h9,
  h10;
}

//This enum is defined for font-weight
enum FontStyle { semiBold, bold, extraBold, medium, regular }

//This will return value of text size
extension FontSizes on FontSize {
  double get value {
    switch (this) {
      case FontSize.h1:
        return 28;
      case FontSize.h2:
        return 26;
      case FontSize.h3:
        return 22;
      case FontSize.h4:
        return 20;
      case FontSize.h5:
        return 18;
      case FontSize.h6:
        return 16;
      case FontSize.h7:
        return 14;
      case FontSize.h8:
        return 12;
      case FontSize.h9:
        return 10;
      case FontSize.h10:
        return 8;
    }
  }
}

//This will return style for text
extension FontStyles on FontStyle {
  FontWeight get value {
    switch (this) {
      case FontStyle.regular:
        return FontWeight.normal;
      case FontStyle.bold:
        return FontWeight.bold;
      case FontStyle.semiBold:
        return FontWeight.w700;
      case FontStyle.medium:
        return FontWeight.w600;
      case FontStyle.extraBold:
        return FontWeight.w900;
    }
  }
}
