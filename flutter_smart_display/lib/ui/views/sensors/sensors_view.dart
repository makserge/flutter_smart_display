import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_smart_display/models/sensor.dart';
import 'package:stacked/stacked.dart';

import 'item.dart';
import 'no_items.dart';
import 'sensors_viewmodel.dart';

class SensorsView extends StackedView<SensorsViewModel> {
  const SensorsView({super.key});

  @override
  Widget builder(
    BuildContext context,
    SensorsViewModel viewModel,
    Widget? child,
  ) {
    Widget itemsList(List<Sensor> data) {
      return GridView.count(
        crossAxisCount: 2,
        crossAxisSpacing: 0,
        mainAxisSpacing: 0,
        childAspectRatio: 2.1,
        children: List.generate(data.length, (index) {
          Sensor item = data[index];
          return Item(
            item: item,
            editMode: viewModel.editMode,
            onDelete: () {
              viewModel.deleteItem(item);
            },
            onOptionsClick: () {
              viewModel.openEditItemDialog(item);
            },
          );
        }),
      );
    }

    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.background,
      body: GestureDetector(
        onLongPress: () {
          viewModel.editMode = !viewModel.editMode;
        },
        child: Container(
            padding: const EdgeInsets.only(left: 10.0, right: 10.0),
            child: viewModel.data.isEmpty
                ? NoItems(onClick: viewModel.openAddItemDialog)
                : itemsList(viewModel.data)),
      ),
      floatingActionButton: Builder(builder: (context) {
        if (viewModel.data.isNotEmpty && viewModel.editMode) {
          return FloatingActionButton(
            backgroundColor: Theme.of(context).colorScheme.primary,
            shape: const CircleBorder(),
            onPressed: viewModel.openAddItemDialog,
            child: const Icon(Icons.add),
          );
        } else {
          return Container();
        }
      }),
    );
  }

  @override
  SensorsViewModel viewModelBuilder(BuildContext context) => SensorsViewModel();

  @override
  void onViewModelReady(SensorsViewModel viewModel) => SchedulerBinding.instance
      .addPostFrameCallback((timeStamp) => viewModel.runStartupLogic());
}
