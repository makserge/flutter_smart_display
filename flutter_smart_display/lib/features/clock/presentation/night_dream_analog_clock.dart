import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_smart_display/features/clock/domain/analog_clock_config.dart';
import 'package:flutter_smart_display/features/clock/domain/tick_style.dart';

import '../domain/digit_style.dart';
import '../domain/hand_style.dart';

class NightdreamAnalogClock extends StatelessWidget {
  final int hour;
  final int minute;
  final int second;
  final ColorFilter secondaryColorFilter = const ColorFilter.mode(Colors.white, BlendMode.color);

  const NightdreamAnalogClock({required this.hour, required this.minute, required this.second, super.key});

  @override
  Widget build(BuildContext context) {
    Size size = MediaQuery
        .of(context)
        .size;
    return CustomPaint(
      painter: CurvePainter(size, hour, minute, second, AnalogClockConfig(), secondaryColorFilter),
      child: SizedBox(
        width: size.width,
        height: size.height,
      )
    );
  }
}

class CurvePainter extends CustomPainter{
  final Size size;
  final int hour;
  final int minute;
  final int second;
  final AnalogClockConfig config;
  final ColorFilter secondaryColorFilter;
  final ColorFilter customColorFilter = const ColorFilter.mode(Colors.white, BlendMode.color);

  final List<String> _romanDigits = ["I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"];
  final double _cosOf30Degree = cos(pi / 6.0).toDouble();

  late final List<double> _minuteAnglesSin;
  late final List<double> _minuteAnglesCos;
  late final List<double> _hourAnglesSin;
  late final List<double> _hourAnglesCos;

  CurvePainter(this.size, this.hour, this.minute, this.second, this.config, this.secondaryColorFilter) {
    _initCounter();
  }

  @override
  void paint(Canvas canvas, Size size) {
    Paint paint = Paint();

    paint.isAntiAlias = true;
    paint.color = Colors.white;

    double centerX = size.width / 2;
    double centerY = size.height / 2;
    double radius = size.height / 2 - 20;

    double hourAngle = (hour.toDouble() / 6.0 * pi - pi / 2.0 +
        minute.toDouble() / 60.0 * pi / 6.0);
    double minAngle = minute.toDouble() / 30.0 * pi - pi / 2.0;
    double secAngle = second.toDouble() / 30.0 * pi - pi / 2.0;
    _drawTicks(canvas, paint, centerX, centerY, radius, config);
    _drawHourDigits(canvas, centerX, centerY, radius);
    _drawHands(canvas, paint, centerX, centerY, radius, hourAngle, minAngle, secAngle);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) {
    return oldDelegate != this;
  }

  _initCounter() {
    _minuteAnglesSin =
    List<double>.generate(59, (value) => sin(_getMinAngle(value)));
    _minuteAnglesCos =
    List<double>.generate(59, (value) => cos(_getMinAngle(value)));
    _hourAnglesSin =
    List<double>.generate(11, (value) => sin(_getHourAngle(value)));
    _hourAnglesCos =
    List<double>.generate(11, (value) => cos(_getHourAngle(value)));
  }

  double _getMinAngle(int value) {
    return value.toDouble() * (pi / 30.0);
  }

  double _getHourAngle(int value) {
    return value.toDouble() * (pi / 6.0);
  }

  _drawTicks(Canvas canvas, Paint paint, double centerX, double centerY, double radius, AnalogClockConfig config) {
    paint.colorFilter = secondaryColorFilter;
    paint.style = PaintingStyle.fill;

    for (var minuteCounter = 0; minuteCounter < 59; minuteCounter++) {
      bool isHourTick = minuteCounter % 5 == 0;
      TickStyle tickStyle = (isHourTick) ? config.tickStyleHours : config.tickStyleMinutes;
      double tickStart = (isHourTick) ? config.tickStartHours : config.tickStartMinutes;
      double tickLength = (isHourTick) ? config.tickLengthHours : config.tickLengthMinutes;
      int width = ((isHourTick) ? config.tickWidthHours * radius : config.tickWidthMinutes * radius).toInt();
      paint.strokeWidth = width.toDouble();
      double tickStartX = (centerX + tickStart * radius * _minuteAnglesCos[minuteCounter]).toDouble();
      double tickStartY = (centerY + tickStart * radius * _minuteAnglesSin[minuteCounter]).toDouble();
      double tickEndX = (centerX + (tickStart + tickLength) * radius * _minuteAnglesCos[minuteCounter]).toDouble();
      double tickEndY = (centerY + (tickStart + tickLength) * radius * _minuteAnglesSin[minuteCounter]).toDouble();
      switch (tickStyle) {
        case TickStyle.none: {}
        case TickStyle.circle: {
          if (isHourTick && config.emphasizeHour12 && minuteCounter == 45) {
            double triangleHeight = tickLength * radius * 1.2;
            double triangleWidth = triangleHeight * 1.2;
            _drawTriangle(canvas, paint, tickEndX, tickEndY - triangleHeight * 0.1, triangleWidth, triangleHeight);
          } else {
            double roundTickRadius = tickLength * 0.5 * radius;
            double roundTickCenterX = centerX + (tickStart + tickLength * 0.5) * radius.toDouble() *
                      _minuteAnglesCos[minuteCounter].toDouble();
            double roundTickCenterY = centerY + (tickStart + tickLength * 0.5) * radius.toDouble() *
                      _minuteAnglesSin[minuteCounter].toDouble();
            canvas.drawCircle(Offset(roundTickCenterX, roundTickCenterY), roundTickRadius, paint);
          }
        }
        case TickStyle.dash:
          canvas.drawLine(Offset(tickStartX, tickStartY), Offset(tickEndX, tickEndY), paint);
      }
    }
  }

  _drawTriangle(Canvas canvas, Paint paint, double baseX, double baseY, double width, double height) {
    double halfWidth = width / 2;
    Path path = Path();
    path.moveTo(baseX - halfWidth, baseY);
    path.lineTo(baseX + halfWidth, baseY);
    path.lineTo(baseX, baseY + height);
    path.close();

    canvas.drawPath(
      path,
      paint
    );
  }

  _drawHourDigits(Canvas canvas, double centerX, double centerY, double radius) {
    if (config.digitStyle == DigitStyle.none) return;
/*
    const textStyle = TextStyle(
      color: Colors.black,
      fontSize: 30,
    );
    const textSpan = TextSpan(
      text: 'Hello, world.',
      style: textStyle,
    );
    final textPainter = TextPainter(
      text: textSpan,
      textDirection: TextDirection.ltr,
    );
    textPainter.typeface = typeface


    double fontSizeBig = config.fontSize * radius;
    double fontSizeSmall = 0.75 * config.fontSize * radius;
    //val textSizeBig = _fontSizeForWidth("5", fontSizeBig, paint);
    //val textSizeSmall = _fontSizeForWidth("5", fontSizeSmall, paint);
    double minTickStart = config.tickStartHours - config.tickLengthHours * 0.5;
    double maxTickStart = config.tickStartHours + config.tickLengthHours * 1.5;
    double defaultDigitPosition = config.digitPosition * radius;
    double maxDigitPosition = minTickStart * radius;
    double minDigitPosition = maxTickStart * radius;
    for (var digitCounter = 0; digitCounter <= 11; digitCounter++) {
      int currentHour = (digitCounter + 2) % 12 + 1;
      if (config.highlightQuarterOfHour && currentHour % 3 == 0) {
        paint.colorFilter = customColorFilter;
        //paint.textSize = textSizeBig;
        //paint.typeface = boldTypeface;
      } else {
        paint.colorFilter = secondaryColorFilter;
        //paint.textSize = textSizeSmall;
        //paint.typeface = typeface;
      }

      String currentHourText = _getHourTextOfDigitStyle(currentHour, config.digitStyle);

//      val bounds = Rect();
//      paint.getTextBounds(
  //        currentHourText,
    //      0,
      //    currentHourText.length,
        //  bounds
      //);
      //val textWidth = paint.measureText(
//          currentHourText,
  //        0,
    //      currentHourText.length
      //);
      //val textHeight = bounds.height().toFloat();

      //double distanceDigitCenterToBorder = _distanceHourTextBoundsCenterToBorder(currentHour, textWidth, textHeight);
      var correctedAbsoluteDigitPosition = defaultDigitPosition;
     // if (config.digitPosition < config.tickStartHours) {
    //    if (defaultDigitPosition + distanceDigitCenterToBorder > maxDigitPosition) {
    //      correctedAbsoluteDigitPosition = maxDigitPosition - distanceDigitCenterToBorder;
    //    }
    //  } else if (config.digitPosition >= config.tickStartHours) {
    //    if (defaultDigitPosition - distanceDigitCenterToBorder < minDigitPosition) {
    //      correctedAbsoluteDigitPosition = minDigitPosition + distanceDigitCenterToBorder;
    //    }
    //  }
     // var x = (centerX + correctedAbsoluteDigitPosition * _hourAnglesCos[digitCounter]).toDouble();
    //  var y = (centerY + correctedAbsoluteDigitPosition * _hourAnglesSin[digitCounter]).toDouble();

      //x -= (textWidth / 2.0).toDouble();
      //y -= textHeight / 2 + 1;
      //canvas.drawText(
      //    currentHourText,
      //    x,
      //    y + textHeight,
      //    paint
      //);
    }

 */
  }

  _fontSizeForWidth(String dummyText, double fontSizeBig, Paint paint) {
    //paint.
    //val bounds = Rect()
    //paint.getTextBounds(
    //    dummyText,
    //    0,
    //    dummyText.length,
    //    bounds
    //)
    //return dummyFontSize * destWidth / bounds.width();
  }

  String _getHourTextOfDigitStyle(int currentHour, DigitStyle digitStyle) {
    if (digitStyle == DigitStyle.arabic) {
      return currentHour.toString();
    } else {
      return _romanDigits[currentHour - 1];
    }
  }

  double _distanceHourTextBoundsCenterToBorder(int currentHour, double textWidth, double textHeight) {
    switch (currentHour) {
      case 6:
      case 12:
        return textHeight / 2;
      case 3:
      case 9:
        return textWidth / 2;
    }
    return (textWidth / 2 / _cosOf30Degree).abs();
  }

  _drawHands(Canvas canvas, Paint paint, double centerX, double centerY, double radius, double hourAngle, double minAngle, double secAngle) {
    paint.style = PaintingStyle.fill;
    paint.shader = null;
    // minute hand
    canvas.save();
    canvas.rotate(_radiansToDegrees(minAngle));//, centerX, centerY);
    _drawHand(canvas, paint, centerX, centerY, (config.handLengthMinutes * radius).toInt(), (config.handWidthMinutes * radius).toInt(), config.handStyle);
    canvas.restore();

    // second hand
    if (config.showSecondHand) {
      canvas.save();
      canvas.rotate(_radiansToDegrees(secAngle));
          //centerX,
          //centerY
      _drawHand(canvas, paint, centerX, centerY, (config.handLengthMinutes * radius).toInt(), (config.handWidthMinutes / 3 * radius).toInt(), config.handStyle);
      canvas.restore();
    }
    // hour hand
    canvas.save();
    canvas.rotate(_radiansToDegrees(hourAngle));//,
        //centerX,
        //centerY
    //);
    _drawHand(canvas, paint, centerX, centerY, (config.handLengthHours * radius).toInt(), (config.handWidthHours * radius).toInt(), config.handStyle);
    canvas.restore();
    _drawInnerCircle(canvas, paint, centerX, centerY, config.innerCircleRadius);
  }

  double _radiansToDegrees(double rad) {
    return (rad * 180.0 / pi).toDouble();
  }

  _drawHand(Canvas canvas, Paint paint, double centerX, double centerY, int height, int width, HandStyle handStyle) {
    switch (handStyle) {
      case HandStyle.bar: _drawHandBar(canvas, paint, centerX, centerY, height, width);
      case HandStyle.triangle: _drawHandTriangle(canvas, paint, centerX, centerY, height, width);
    }
  }

  _drawInnerCircle(Canvas canvas, Paint paint, double centerX, double centerY, innerCircleRadius) {
    paint.colorFilter = secondaryColorFilter;
    //paint.alpha = 255;
    canvas.drawCircle(Offset(centerX, centerY), innerCircleRadius, paint);
    paint.colorFilter = null;
    paint.color = Colors.black;
    paint.strokeWidth = 2;
    //canvas.drawPoint(Offset(centerX, centerY), paint);
    paint.style = PaintingStyle.fill;
    paint.color = Colors.white;

    canvas.drawCircle(Offset(centerX, centerY), innerCircleRadius, paint);
  }

  _drawHandBar(Canvas canvas, Paint paint, double centerX, double centerY, int length, int width) {
    paint.strokeWidth = width.toDouble();
    canvas.drawLine(Offset(centerX, centerY), Offset(centerX + length, centerY), paint);
  }

  _drawHandTriangle(Canvas canvas, Paint paint, double centerX, double centerY, int length, int width) {
    double halfWidth = width / 2;
    Path path = Path();
    path.moveTo(centerX, centerY - halfWidth);
    path.lineTo(centerX + length, centerY);
    path.lineTo(centerX, centerY + halfWidth);
    path.lineTo(centerX, centerY - halfWidth);
    path.close();
    canvas.drawPath(path, paint);
  }
}