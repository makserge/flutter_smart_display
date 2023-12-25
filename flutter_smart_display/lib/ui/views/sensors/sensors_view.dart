import 'package:flutter/material.dart';
import 'package:stacked/stacked.dart';

import 'no_items.dart';
import 'sensors_viewmodel.dart';

class SensorsView extends StackedView<SensorsViewModel> {
  const SensorsView({Key? key}) : super(key: key);

  @override
  Widget builder(
    BuildContext context,
    SensorsViewModel viewModel,
    Widget? child,
  ) {
    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.background,
      body: Container(
        padding: const EdgeInsets.only(left: 10.0, right: 10.0),
        child: SafeArea(
            child:
                Builder(
                  builder: (context) {
                    if (viewModel.isOpenEditItemDialog) {
                      return Container();
                    } else {
                      return NoItems(onClick: () {
                        viewModel.isOpenEditItemDialog = true;
                      });
                    }
                  }
                ),
        )
      ),
      /*
      floatingActionButton: Builder(
        builder: (context) {
          return FloatingActionButton(
            onPressed: (){},
            child: const Icon(Icons.add),
          );
        }
      ),*/
    );
  }

  @override
  SensorsViewModel viewModelBuilder(
    BuildContext context,
  ) =>
      SensorsViewModel();
}