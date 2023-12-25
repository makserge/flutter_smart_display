import 'package:flutter_smart_display/ui/views/home/home_view.dart';
import 'package:flutter_smart_display/ui/views/startup/startup_view.dart';
import 'package:stacked/stacked_annotations.dart';
import 'package:stacked_services/stacked_services.dart';
import 'package:flutter_smart_display/ui/views/sensors/sensors_view.dart';

import '../ui/dialogs/update_sensor_item_alert/update_sensor_item_alert_dialog.dart';
// @stacked-import

@StackedApp(
  routes: [
    MaterialRoute(page: HomeView),
    MaterialRoute(page: StartupView),
    MaterialRoute(page: SensorsView),
// @stacked-route
  ],
  dependencies: [
    LazySingleton(classType: BottomSheetService),
    LazySingleton(classType: DialogService),
    LazySingleton(classType: NavigationService),
    // @stacked-service
  ],
  bottomsheets: [
    // @stacked-bottom-sheet
  ],
  dialogs: [
    StackedDialog(classType: UpdateSensorItemAlertDialog),
    // @stacked-dialog
  ],
)
class App {}
