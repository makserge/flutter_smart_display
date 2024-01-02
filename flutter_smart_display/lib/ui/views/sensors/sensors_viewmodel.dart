import 'package:stacked/stacked.dart';
import 'package:stacked_services/stacked_services.dart';

import '../../../app/app.dialogs.dart';
import '../../../app/app.locator.dart';
import '../../../services/database_service.dart';
import '../../../models/sensor.dart';

class SensorsViewModel extends BaseViewModel {
  final _dialogService = locator<DialogService>();
  final _databaseService = locator<DatabaseService>();

  bool _isOpenEditItemDialog = false;
  bool get isOpenEditItemDialog => _isOpenEditItemDialog;

  bool _editMode = false;
  bool get editMode => _editMode;

  List<Sensor> _items = [];

  List<Sensor> get data => _items;

  set editMode(bool value) {
    _editMode = value;
    rebuildUi();
  }

  void showUpdateItemDialog(Sensor item) async {
    var response = await _dialogService.showCustomDialog(
      variant: DialogType.updateSensorItemAlert,
      data: item,
    );
    if (response?.data != null) {
      _databaseService.updateSensor(item: response!.data);
    }
    _isOpenEditItemDialog = false;
    _getItems();
  }

  void openAddItemDialog() {
    _isOpenEditItemDialog = true;
    showUpdateItemDialog(Sensor.emptySensor);
  }

  void openEditItemDialog(Sensor item) {
    _isOpenEditItemDialog = true;
    showUpdateItemDialog(item);
  }

  void deleteItem(Sensor item) async {
    _databaseService.deleteSensor(item: item);
    _getItems();
  }

  void _getItems() {
    _databaseService.getSensors().then((value) {
      _items = value;
      rebuildUi();
    });
  }

  runStartupLogic() {
    _getItems();
  }
}
