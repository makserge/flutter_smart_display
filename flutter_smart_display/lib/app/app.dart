import 'package:flutter_smart_display/ui/views/home/home_view.dart';
import 'package:sqflite_migration_service/sqflite_migration_service.dart';
import 'package:stacked/stacked_annotations.dart';
import 'package:stacked_services/stacked_services.dart';
import 'package:flutter_smart_display/ui/views/sensors/sensors_view.dart';

import '../ui/dialogs/update_sensor_item_alert/update_sensor_item_alert_dialog.dart';
import 'package:flutter_smart_display/services/database_service.dart';
import 'package:flutter_smart_display/ui/bottom_sheets/alert/alert_sheet.dart';
// @stacked-import

@StackedApp(
  routes: [
    MaterialRoute(page: HomeView),
    MaterialRoute(page: SensorsView),
// @stacked-route
  ],
  dependencies: [
    LazySingleton(classType: BottomSheetService),
    LazySingleton(classType: DialogService),
    LazySingleton(classType: NavigationService),
    LazySingleton(classType: DatabaseService),
    LazySingleton(classType: DatabaseMigrationService),
// @stacked-service
  ],
  bottomsheets: [
    StackedBottomsheet(classType: AlertSheet),
// @stacked-bottom-sheet
  ],
  dialogs: [
    StackedDialog(classType: UpdateSensorItemAlertDialog),
    // @stacked-dialog
  ],
)
class App {}
