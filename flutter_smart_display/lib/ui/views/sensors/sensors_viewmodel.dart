import 'package:stacked/stacked.dart';
import 'package:stacked_services/stacked_services.dart';

import '../../../app/app.dialogs.dart';
import '../../../app/app.locator.dart';

class SensorsViewModel extends BaseViewModel {
  final _dialogService = locator<DialogService>();

  bool _isOpenEditItemDialog = false;
  bool get isOpenEditItemDialog => _isOpenEditItemDialog;

  set isOpenEditItemDialog(bool value) {
    _isOpenEditItemDialog = value;
    showUpdateItemDialog();
    rebuildUi();
  }

  void showUpdateItemDialog() async {
    var response = await _dialogService.showCustomDialog(
      variant: DialogType.updateSensorItemAlert,
    );
    _isOpenEditItemDialog = false;
    rebuildUi();
  }
}
