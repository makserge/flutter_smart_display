enum ClockType {
  analogNightDream("analogNightDream"),
  analogClock("analogClock"),
  analogClock2("analogClock2"),
  analogRectangularClock("analogRectangularClock"),
  analogFsClock("analogFsClock"),
  analogJetAlarmClock("analogJetAlarmClock"),
  digitalFlipClock("digitalFlipClock"),
  digitalMatrixClock("digitalMatrixClock"),
  digitalClock("digitalClock"),
  digitalClock2("digitalClock2");

  const ClockType(this.id);
  final String id;
}