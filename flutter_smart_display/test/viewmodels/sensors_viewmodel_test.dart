import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_smart_display/app/app.locator.dart';

import '../helpers/test_helpers.dart';

void main() {
  group('SensorsViewModel Tests -', () {
    setUp(() => registerServices());
    tearDown(() => locator.reset());
  });
}
