import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class WeatherView extends ConsumerWidget {
  const WeatherView({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return const Scaffold(
        body: Center(
          child: Text("WeatherView")
        )
    );
  }
}