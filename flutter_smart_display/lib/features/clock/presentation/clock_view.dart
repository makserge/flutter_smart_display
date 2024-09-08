import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../domain/clock_type.dart';
import 'analog_clock.dart';
import 'analog_clock2.dart';
import 'analog_fs_clock.dart';
import 'analog_jet_alarm_clock.dart';
import 'analog_rectangular_clock.dart';
import 'digital_clock.dart';
import 'digital_clock2.dart';
import 'digital_flip_clock.dart';
import 'digital_matrix_clock.dart';
import 'night_dream_analog_clock.dart';

class ClockView extends ConsumerWidget {
  final int _hour = 0;
  final int _minute = 0;
  final int _second = 0;

  const ClockView({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    ClockType clockType = ClockType.analogNightDream;
    return Scaffold(
        body: _drawClock(clockType)
    );
  }

  Widget _drawClock(ClockType clockType) {
    switch(clockType) {
      case ClockType.analogNightDream:
        return NightdreamAnalogClock(
          hour: _hour,
          minute: _minute,
          second: _second,
        );
      case ClockType.analogClock:
        return const AnalogClock();
      case ClockType.analogClock2:
        return const AnalogClock2();
      case ClockType.analogRectangularClock:
        return const AnalogRectangularClock();
      case ClockType.analogFsClock:
        return const AnalogFsClock();
      case ClockType.analogJetAlarmClock:
        return const AnalogJetAlarmClock();
      case ClockType.digitalFlipClock:
        return const DigitalFlipClock();
      case ClockType.digitalMatrixClock:
        return const DigitalMatrixClock();
      case ClockType.digitalClock:
        return const DigitalClock();
      case ClockType.digitalClock2:
        return const DigitalClock2();
    }
  }
}