import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class SensorsView extends ConsumerWidget {
  const SensorsView({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return const Scaffold(
        body: Center(
          child: Text("SensorsView")
        )
    );
  }
}