import 'package:flutter/material.dart';
import 'package:flutter_smart_display/app/app.dialogs.dart';
import 'package:flutter_smart_display/app/app.locator.dart';
import 'package:flutter_smart_display/app/app.router.dart';
import 'package:flutter_smart_display/ui/common/app_colors.dart';
import 'package:stacked_services/stacked_services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

import 'app/app.bottomsheets.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await setupLocator();
  setupDialogUi();
  setupBottomSheetUi();
  runApp(const MainApp());
}

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData.dark().copyWith(
        dividerColor: Colors.transparent,
        colorScheme: const ColorScheme.dark().copyWith(
            primary: kcPrimaryColor,
            secondary: kcSecondaryColor,
            background: kcBackgroundColor),
      ),
      initialRoute: Routes.sensorsView,
      onGenerateRoute: StackedRouter().onGenerateRoute,
      navigatorKey: StackedService.navigatorKey,
      navigatorObservers: [
        StackedService.routeObserver,
      ],
      localizationsDelegates: const [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      supportedLocales: const [
        Locale('en'), // English
        Locale('ru'), // Russian
      ],
    );
  }
}
