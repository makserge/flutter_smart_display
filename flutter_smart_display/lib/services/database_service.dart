import 'dart:developer';

import 'package:sqflite/sqflite.dart';
import 'package:sqflite_migration_service/sqflite_migration_service.dart';

import '../app/app.locator.dart';
import '../models/sensor.dart';

const String dbName = 'smart_display_database.sqlite';
const String sensorsTableName = 'sensors';

class DatabaseService {
  final _migrationService = locator<DatabaseMigrationService>();
  Database? _database;

  Future initialise() async {
    if (_database == null) {
      _database = await openDatabase(dbName, version: 1);

      await _migrationService.runMigration(
        _database,
        migrationFiles: [
          '1_create_schema.sql',
        ],
        verbose: true,
      );
    }
  }

  Future<List<Sensor>> getSensors() async {
    await initialise();
    List<Map> results = await _database!.query(sensorsTableName);
    return results
        .map((item) => Sensor.fromJson(item as Map<String, Object?>))
        .toList();
  }

  Future updateSensor({required Sensor item}) async {
    try {
      await initialise();
      if (item.id != null) {
        await _database!.update(sensorsTableName, item.toJson(),
            where: 'id = ?', whereArgs: [item.id]);
      } else {
        await _database!.insert(sensorsTableName, item.toJson());
      }
    } catch (e) {
      log('Could not update the sensor: $e');
    }
  }

  Future deleteSensor({required Sensor item}) async {
    try {
      await initialise();
      await _database!
          .delete(sensorsTableName, where: 'id = ?', whereArgs: [item.id]);
    } catch (e) {
      log('Could not delete the sensor: $e');
    }
  }
}
