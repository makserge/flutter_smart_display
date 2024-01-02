import 'package:drop_down_search_field/drop_down_search_field.dart';
import 'package:flutter/material.dart';

import 'package:flutter_gen/gen_l10n/app_localizations.dart';

import '../../common/ui_helpers.dart';

class IconPicker extends StatefulWidget {
  const IconPicker(
      {super.key, required this.value, required this.onValueChange});

  final String value;
  final Function(String value) onValueChange;

  @override
  // ignore: library_private_types_in_public_api
  _IconPickerState createState() => _IconPickerState();
}

class _IconPickerState extends State<IconPicker> {
  final TextEditingController _dropdownSearchFieldController =
      TextEditingController();

  late String _value;

  SuggestionsBoxController suggestionBoxController = SuggestionsBoxController();

  static final List<String> items = [
    "co2",
    "lightbulb",
    "toggle_on",
    "thermostat",
    "humidity_low",
  ];

  @override
  void initState() {
    _value = widget.value;
    super.initState();
  }

  void setValue(String value) {
    setState(() {
      _value = value;
      widget.onValueChange(value);
    });
  }

  static List<String> getSuggestions(String query) {
    List<String> matches = <String>[];
    matches.addAll(items);

    matches.retainWhere((s) => s.toLowerCase().contains(query.toLowerCase()));
    return matches;
  }

  static String? getTitleByKey(BuildContext context, String suggestion) {
    switch (suggestion) {
      case "co2":
        return AppLocalizations.of(context)!.addSensorTitleIconCO2;
      case "lightbulb":
        return AppLocalizations.of(context)!.addSensorTitleIconLight;
      case "toggle_on":
        return AppLocalizations.of(context)!.addSensorTitleIconSwitch;
      case "thermostat":
        return AppLocalizations.of(context)!.addSensorTitleIconTemp;
      case "humidity_low":
        return AppLocalizations.of(context)!.addSensorTitleIconHumidity;
    }
    return null;
  }

  @override
  Widget build(BuildContext context) {
    return DropDownSearchFormField(
      textFieldConfiguration: TextFieldConfiguration(
        decoration: InputDecoration(
          contentPadding: const EdgeInsets.all(10),
          border: const OutlineInputBorder(),
          prefixIcon: Icon(getIconByKey(_value)),
          hintMaxLines: 1,
          hintText: getTitleByKey(context, _value) ??
              AppLocalizations.of(context)!.addSensorIcon,
          hintStyle: const TextStyle(overflow: TextOverflow.ellipsis),
        ),
        controller: _dropdownSearchFieldController,
        maxLines: 1,
      ),
      suggestionsCallback: (pattern) {
        return getSuggestions(pattern);
      },
      itemBuilder: (context, String suggestion) {
        return ListTile(
            leading: Icon(getIconByKey(suggestion)),
            title: Text(
              getTitleByKey(context, suggestion)!,
              overflow: TextOverflow.ellipsis,
            ));
      },
      transitionBuilder: (context, suggestionsBox, controller) {
        return suggestionsBox;
      },
      onSuggestionSelected: (String suggestion) {
        _dropdownSearchFieldController.text =
            getTitleByKey(context, suggestion)!;
        setValue(suggestion);
      },
      suggestionsBoxController: suggestionBoxController,
      displayAllSuggestionWhenTap: true,
    );
  }
}
