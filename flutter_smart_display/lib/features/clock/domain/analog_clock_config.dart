import 'digit_style.dart';
import 'hand_style.dart';
import 'tick_style.dart';

class AnalogClockConfig {
  DigitStyle digitStyle = DigitStyle.getDefault();
  double digitPosition = defaultDigitPositionNd;
  HandStyle handStyle = HandStyle.getDefault();
  TickStyle tickStyleHours = TickStyle.circle;
  TickStyle tickStyleMinutes = TickStyle.getDefault();
  double tickStartHours = defaultTickStartHoursNd;
  double tickStartMinutes = defaultTickStartMinutesNd;
  double tickLengthHours = defaultTickLenHoursNd / 100;
  double tickLengthMinutes = defaultTickLenMinutesNd / 100;
  double tickWidthHours = 0.01;
  double tickWidthMinutes = 0.01;
  bool emphasizeHour12 = true;
  double fontSize = defaultDigitFontSizeNd / 100;
  bool highlightQuarterOfHour = defaultDigitEmpQuartersNd;
  double handLengthMinutes = defaultHandLenMinNd;
  double handLengthHours = defaultHandLenHoursNd;
  double handWidthMinutes = defaultHandWidthMinNd / 100;
  double handWidthHours = defaultHandWidthHoursNd / 100;
  bool showSecondHand = defaultShowSecondHandNd;
  double innerCircleRadius = defaultInnerCircleRadiusNd / 10;
}

const double defaultTickStartHoursNd = 0.9;
const double defaultTickStartMinutesNd = 0.9;
const double defaultTickLenHoursNd = 4;
const double defaultTickLenMinutesNd = 4;
const double defaultDigitFontSizeNd = 8;
const double defaultDigitPositionNd = 0.85;
const bool defaultDigitEmpQuartersNd = true;
const double defaultHandLenMinNd = 0.9;
const double defaultHandLenHoursNd = 0.7;
const double defaultHandWidthMinNd = 4;
const double defaultHandWidthHoursNd = 4;
const bool defaultShowSecondHandNd = true;
const double defaultInnerCircleRadiusNd = 0.45;
