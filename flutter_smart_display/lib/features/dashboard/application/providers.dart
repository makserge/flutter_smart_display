import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final currentPageProvider = ChangeNotifierProvider<ValueNotifier<int>>((ref) {
  return ValueNotifier(0);
});