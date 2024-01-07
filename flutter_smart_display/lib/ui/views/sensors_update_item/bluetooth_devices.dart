import 'dart:async';
import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

import '../../../models/bluetooth_device_type.dart';
import '../../common/ui_helpers.dart';

class BluetoothDevices extends StatefulWidget {
  const BluetoothDevices(
      {super.key,
        required this.value,
        required this.onValueChange,
        required this.onRescan,
        required this.onBluetoothPermissionsDenied,
  });

  final String value;
  final Function(String value) onValueChange;
  final Function() onRescan;
  final Function() onBluetoothPermissionsDenied;

  @override
  State<BluetoothDevices> createState() => _BluetoothDevicesState();
}

class _BluetoothDevicesState extends State<BluetoothDevices> {
  List<ScanResult> _scanResults = [];
  StreamSubscription<List<ScanResult>>? _scanResultsSubscription;
  StreamSubscription<bool>? _isScanningSubscription;
  bool _isScanPreparingState = true;

  @override
  void initState() {
    super.initState();
    _initScan();
  }

  @override
  void dispose() {
    _scanResultsSubscription?.cancel();
    _isScanningSubscription?.cancel();
    super.dispose();
  }

  Widget _buildProgressIndicator() {
    return const Align(
      alignment: Alignment.center,
      child: Padding(
        padding: EdgeInsets.symmetric(vertical: 8.0),
        child: SizedBox(
            height: 30.0,
            width: 30.0,
            child: CircularProgressIndicator()
        ),
      ),
    );
  }

  Widget _buildNoResults() {
    return GestureDetector(
      onTap: widget.onRescan,
      child: Text(
        AppLocalizations.of(context)!.noBluetoothDevices,
        style: textStyle,
      ),
    );
  }

  Widget _buildList() {
    return _scanResults.isEmpty
      ? _buildNoResults()
      : ListView(
          shrinkWrap: true,
          children: _scanResults.map((item) =>
              ScanResultTile(
                result: item,
                    //onTap: () => widget.onValueChange(item),
              )
          ).toList()
    );
    /*
    Column(
        modifier = Modifier
            .fillMaxSize(fraction = 0.6f),
    ) {
        when (state.value) {
            BluetoothScanState.Initial -> CircularProgressIndicator()
            is BluetoothScanState.Result -> {
                val items = (state.value as BluetoothScanState.Result).devices

                if (items.isNotEmpty()) {
                    DevicesList(
                        modifier = Modifier,
                        items = items,
                        selectedItem = selectedItem,
                        onClick = onClick
                    )
                } else {
                    NoDevices(
                        modifier = Modifier,
                        onClick = onRescan
                    )
                }
            }
            else -> {}
        }
    }
     */
  }

  @override
  Widget build(BuildContext context) {
    return _isScanPreparingState
        ? _buildProgressIndicator()
        : _buildList();
  }

  void _initScan() async {
    if (FlutterBluePlus.adapterStateNow != BluetoothAdapterState.on) {
      _turnOnBluetooth().then((result) {
        if (result) {
          _startScan();
        } else {
          widget.onBluetoothPermissionsDenied();
        }
      });
    } else {
      _startScan();
    }
  }

  void _startScan() async {
    _scanResultsSubscription = FlutterBluePlus.scanResults.listen((results) {
      if (mounted) {
        setState(() {
          _scanResults = _filterResults(results);
        });
      }
    });

    _isScanningSubscription = FlutterBluePlus.isScanning.listen((state) {
      if (mounted) {
        setState(() {
          _isScanPreparingState = false;
        });
      }
    });

    try {
      await FlutterBluePlus.startScan(
          continuousUpdates: true, continuousDivisor: 1);
    } catch (e) {
      log(e.toString());
      widget.onBluetoothPermissionsDenied();
    }

    /*
    try {
      FlutterBluePlus.stopScan();
    } catch (e) {
      Snackbar.show(ABC.b, prettyException("Stop Scan Error:", e), success: false);
    }
     */
  }

  List<ScanResult> _filterResults(List<ScanResult> results) {
    List<ScanResult> items = [];
    List<String> types = BluetoothDeviceType.toList();
    for (var item in results) {
      for (var type in types) {
        if (item.device.platformName.startsWith(type)) {
          items.add(item);
        }
      }
    }
    return items;
  }

  Future<bool> _turnOnBluetooth() async {
    try {
      await FlutterBluePlus.turnOn();
      return true;
    } catch (e) {
      return false;
    }
  }
}

class ScanResultTile extends StatelessWidget {
  const ScanResultTile({super.key, required this.result, this.onTap});

  final ScanResult result;
  final VoidCallback? onTap;

  String getNiceHexArray(List<int> bytes) {
    return '[${bytes.map((i) => i.toRadixString(16).padLeft(2, '0')).join(', ')}]';
  }

  String getNiceManufacturerData(Map<int, List<int>> data) {
    return data.entries
        .map((entry) =>
            '${entry.key.toRadixString(16)}: ${getNiceHexArray(entry.value)}')
        .join(', ')
        .toUpperCase();
  }

  String getNiceServiceData(Map<Guid, List<int>> data) {
    return data.entries
        .map((v) => '${v.key}: ${getNiceHexArray(v.value)}')
        .join(', ')
        .toUpperCase();
  }

  String getNiceServiceUuids(List<Guid> serviceUuids) {
    return serviceUuids.join(', ').toUpperCase();
  }

  Widget _buildTitle(BuildContext context) {
    if (result.device.platformName.isNotEmpty) {
      return Column(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            result.device.platformName,
            overflow: TextOverflow.ellipsis,
          ),
          Text(
            result.device.remoteId.toString(),
            style: Theme.of(context).textTheme.bodySmall,
          )
        ],
      );
    } else {
      return Text(result.device.remoteId.toString());
    }
  }

  Widget _buildAdvRow(BuildContext context, String title, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 4.0),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(title, style: Theme.of(context).textTheme.bodySmall),
          const SizedBox(
            width: 12.0,
          ),
          Expanded(
            child: Text(
              value,
              style: Theme.of(context)
                  .textTheme
                  .bodySmall
                  ?.apply(color: Colors.black),
              softWrap: true,
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return _buildTitle(context);
    /*
    var adv = result.advertisementData;
    return ExpansionTile(
      title: _buildTitle(context),
      leading: Text(result.rssi.toString()),
      //trailing: _buildConnectButton(context),
      children: <Widget>[
        //if (adv.advName.isNotEmpty) _buildAdvRow(context, 'Name', adv.advName),
        //if (adv.txPowerLevel != null) _buildAdvRow(context, 'Tx Power Level', '${adv.txPowerLevel}'),
        //if (adv.manufacturerData.isNotEmpty)
        //  _buildAdvRow(context, 'Manufacturer Data', getNiceManufacturerData(adv.manufacturerData)),
        //if (adv.serviceUuids.isNotEmpty) _buildAdvRow(context, 'Service UUIDs', getNiceServiceUuids(adv.serviceUuids)),
        //if (adv.serviceData.isNotEmpty) _buildAdvRow(context, 'Service Data', getNiceServiceData(adv.serviceData)),
      ],
    );

     */
  }
}
