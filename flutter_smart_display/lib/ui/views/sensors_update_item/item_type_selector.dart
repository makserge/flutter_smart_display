import 'package:drop_down_search_field/drop_down_search_field.dart';
import 'package:flutter/material.dart';

import 'package:flutter_gen/gen_l10n/app_localizations.dart';

class ItemTypeSelector extends StatefulWidget {
  const ItemTypeSelector(
      {super.key, required this.value, required this.onValueChange});

  final String value;
  final Function(String value) onValueChange;

  @override
  State<ItemTypeSelector> createState() => _ItemTypeSelectorState();
}

class _ItemTypeSelectorState extends State<ItemTypeSelector> {
  final TextEditingController _dropdownSearchFieldController =
      TextEditingController();

  late String _value;

  SuggestionsBoxController suggestionBoxController = SuggestionsBoxController();

  static final List<String> items = [
    "mqtt",
    "bluetooth",
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
      case "mqtt":
        return AppLocalizations.of(context)!.sensorTypeMqtt;
      case "bluetooth":
        return AppLocalizations.of(context)!.sensorTypeBluetooth;
    }
    return null;
  }

  @override
  Widget build(BuildContext context) {
    return DropDownSearchFormField(
      hideKeyboard: true,
      textFieldConfiguration: TextFieldConfiguration(
        decoration: InputDecoration(
          contentPadding: const EdgeInsets.all(10),
          border: const OutlineInputBorder(),
          hintText: getTitleByKey(context, _value),
        ),
        controller: _dropdownSearchFieldController,
      ),
      suggestionsCallback: (pattern) {
        return getSuggestions(pattern);
      },
      itemBuilder: (context, String suggestion) {
        return ListTile(
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
