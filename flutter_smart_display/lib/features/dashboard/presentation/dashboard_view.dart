import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dots_indicator/dots_indicator.dart';

import '../../../constants/app_colors.dart';
import '../../../constants/constants.dart';
import '../../alarms/presentation/alarms_view.dart';
import '../../clock/presentation/clock_view.dart';
import '../../doorbell/presentation/doorbell_view.dart';
import '../../internet_radio/presentation/internet_radio_view.dart';
import '../../sensors/presentation/sensors_view.dart';
import '../../timers/presentation/timers_view.dart';
import '../../weather/presentation/weather_view.dart';
import '../application/providers.dart';

class DashboardView extends ConsumerWidget {
  const DashboardView({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersive);
    ValueNotifier<int> pageIndex = ref.watch(currentPageProvider);
    return Scaffold(
        body: Stack(
          children: [
            PageView.builder(
                itemCount: dashboardItems.length,
                itemBuilder: (BuildContext context, int index) {
                  return Center(
                    child: _renderPage(index),
                  );
                },
                onPageChanged: (int index) {
                  ref.read(currentPageProvider.notifier).value = index;
                }),
            Positioned(
                left: 0.0,
                right: 0.0,
                bottom: 0.0,
                child: Padding(
                  padding: const EdgeInsets.all(20.0),
                  child: DotsIndicator(
                      mainAxisAlignment: MainAxisAlignment.center,
                      reversed: false,
                      dotsCount: dashboardItems.length,
                      position: pageIndex.value,
                      decorator: DotsDecorator(
                        activeColor: AppColors.accentColor,
                        activeShape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(5.0)),
                      )
                  ),
                )
            )
          ],
        )
    );
  }

  _renderPage(int index) {
    switch(index) {
      case 0:
        return const ClockView();
      case 1:
        return const WeatherView();
      case 2:
        return const SensorsView();
      case 3:
        return const TimersView();
      case 4:
        return const AlarmsView();
      case 5:
        return const InternetRadioView();
      case 6:
        return const DoorbellView();
    }
  }
}