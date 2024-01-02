// GENERATED CODE - DO NOT MODIFY BY HAND

// **************************************************************************
// StackedFormGenerator
// **************************************************************************

// ignore_for_file: public_member_api_docs, constant_identifier_names, non_constant_identifier_names,unnecessary_this

import 'package:flutter/material.dart';
import 'package:flutter_smart_display/ui/views/sensors_update_item/sensors_update_item_viewmodel.dart';
import 'package:stacked/stacked.dart';

const bool _autoTextFieldValidation = true;

const String TitleInputValueKey = 'titleInput';
const String Topic1InputValueKey = 'topic1Input';
const String Unit1InputValueKey = 'unit1Input';
const String Topic2InputValueKey = 'topic2Input';
const String Unit2InputValueKey = 'unit2Input';
const String Topic3InputValueKey = 'topic3Input';
const String Unit3InputValueKey = 'unit3Input';
const String Topic4InputValueKey = 'topic4Input';
const String Unit4InputValueKey = 'unit4Input';

final Map<String, TextEditingController>
    _SensorsUpdateItemViewTextEditingControllers = {};

final Map<String, FocusNode> _SensorsUpdateItemViewFocusNodes = {};

final Map<String, String? Function(String?)?>
    _SensorsUpdateItemViewTextValidations = {
  TitleInputValueKey: InputValidators.validateRequiredText,
  Topic1InputValueKey: InputValidators.validateRequiredText,
  Unit1InputValueKey: null,
  Topic2InputValueKey: null,
  Unit2InputValueKey: null,
  Topic3InputValueKey: null,
  Unit3InputValueKey: null,
  Topic4InputValueKey: null,
  Unit4InputValueKey: null,
};

mixin $SensorsUpdateItemView {
  TextEditingController get titleInputController =>
      _getFormTextEditingController(TitleInputValueKey);
  TextEditingController get topic1InputController =>
      _getFormTextEditingController(Topic1InputValueKey);
  TextEditingController get unit1InputController =>
      _getFormTextEditingController(Unit1InputValueKey);
  TextEditingController get topic2InputController =>
      _getFormTextEditingController(Topic2InputValueKey);
  TextEditingController get unit2InputController =>
      _getFormTextEditingController(Unit2InputValueKey);
  TextEditingController get topic3InputController =>
      _getFormTextEditingController(Topic3InputValueKey);
  TextEditingController get unit3InputController =>
      _getFormTextEditingController(Unit3InputValueKey);
  TextEditingController get topic4InputController =>
      _getFormTextEditingController(Topic4InputValueKey);
  TextEditingController get unit4InputController =>
      _getFormTextEditingController(Unit4InputValueKey);

  FocusNode get titleInputFocusNode => _getFormFocusNode(TitleInputValueKey);
  FocusNode get topic1InputFocusNode => _getFormFocusNode(Topic1InputValueKey);
  FocusNode get unit1InputFocusNode => _getFormFocusNode(Unit1InputValueKey);
  FocusNode get topic2InputFocusNode => _getFormFocusNode(Topic2InputValueKey);
  FocusNode get unit2InputFocusNode => _getFormFocusNode(Unit2InputValueKey);
  FocusNode get topic3InputFocusNode => _getFormFocusNode(Topic3InputValueKey);
  FocusNode get unit3InputFocusNode => _getFormFocusNode(Unit3InputValueKey);
  FocusNode get topic4InputFocusNode => _getFormFocusNode(Topic4InputValueKey);
  FocusNode get unit4InputFocusNode => _getFormFocusNode(Unit4InputValueKey);

  TextEditingController _getFormTextEditingController(
    String key, {
    String? initialValue,
  }) {
    if (_SensorsUpdateItemViewTextEditingControllers.containsKey(key)) {
      return _SensorsUpdateItemViewTextEditingControllers[key]!;
    }

    _SensorsUpdateItemViewTextEditingControllers[key] =
        TextEditingController(text: initialValue);
    return _SensorsUpdateItemViewTextEditingControllers[key]!;
  }

  FocusNode _getFormFocusNode(String key) {
    if (_SensorsUpdateItemViewFocusNodes.containsKey(key)) {
      return _SensorsUpdateItemViewFocusNodes[key]!;
    }
    _SensorsUpdateItemViewFocusNodes[key] = FocusNode();
    return _SensorsUpdateItemViewFocusNodes[key]!;
  }

  /// Registers a listener on every generated controller that calls [model.setData()]
  /// with the latest textController values
  void syncFormWithViewModel(FormStateHelper model) {
    titleInputController.addListener(() => _updateFormData(model));
    topic1InputController.addListener(() => _updateFormData(model));
    unit1InputController.addListener(() => _updateFormData(model));
    topic2InputController.addListener(() => _updateFormData(model));
    unit2InputController.addListener(() => _updateFormData(model));
    topic3InputController.addListener(() => _updateFormData(model));
    unit3InputController.addListener(() => _updateFormData(model));
    topic4InputController.addListener(() => _updateFormData(model));
    unit4InputController.addListener(() => _updateFormData(model));

    _updateFormData(model, forceValidate: _autoTextFieldValidation);
  }

  /// Registers a listener on every generated controller that calls [model.setData()]
  /// with the latest textController values
  @Deprecated(
    'Use syncFormWithViewModel instead.'
    'This feature was deprecated after 3.1.0.',
  )
  void listenToFormUpdated(FormViewModel model) {
    titleInputController.addListener(() => _updateFormData(model));
    topic1InputController.addListener(() => _updateFormData(model));
    unit1InputController.addListener(() => _updateFormData(model));
    topic2InputController.addListener(() => _updateFormData(model));
    unit2InputController.addListener(() => _updateFormData(model));
    topic3InputController.addListener(() => _updateFormData(model));
    unit3InputController.addListener(() => _updateFormData(model));
    topic4InputController.addListener(() => _updateFormData(model));
    unit4InputController.addListener(() => _updateFormData(model));

    _updateFormData(model, forceValidate: _autoTextFieldValidation);
  }

  /// Updates the formData on the FormViewModel
  void _updateFormData(FormStateHelper model, {bool forceValidate = false}) {
    model.setData(
      model.formValueMap
        ..addAll({
          TitleInputValueKey: titleInputController.text,
          Topic1InputValueKey: topic1InputController.text,
          Unit1InputValueKey: unit1InputController.text,
          Topic2InputValueKey: topic2InputController.text,
          Unit2InputValueKey: unit2InputController.text,
          Topic3InputValueKey: topic3InputController.text,
          Unit3InputValueKey: unit3InputController.text,
          Topic4InputValueKey: topic4InputController.text,
          Unit4InputValueKey: unit4InputController.text,
        }),
    );

    if (_autoTextFieldValidation || forceValidate) {
      updateValidationData(model);
    }
  }

  bool validateFormFields(FormViewModel model) {
    _updateFormData(model, forceValidate: true);
    return model.isFormValid;
  }

  /// Calls dispose on all the generated controllers and focus nodes
  void disposeForm() {
    // The dispose function for a TextEditingController sets all listeners to null

    for (var controller
        in _SensorsUpdateItemViewTextEditingControllers.values) {
      controller.dispose();
    }
    for (var focusNode in _SensorsUpdateItemViewFocusNodes.values) {
      focusNode.dispose();
    }

    _SensorsUpdateItemViewTextEditingControllers.clear();
    _SensorsUpdateItemViewFocusNodes.clear();
  }
}

extension ValueProperties on FormStateHelper {
  bool get hasAnyValidationMessage => this
      .fieldsValidationMessages
      .values
      .any((validation) => validation != null);

  bool get isFormValid {
    if (!_autoTextFieldValidation) this.validateForm();

    return !hasAnyValidationMessage;
  }

  String? get titleInputValue =>
      this.formValueMap[TitleInputValueKey] as String?;
  String? get topic1InputValue =>
      this.formValueMap[Topic1InputValueKey] as String?;
  String? get unit1InputValue =>
      this.formValueMap[Unit1InputValueKey] as String?;
  String? get topic2InputValue =>
      this.formValueMap[Topic2InputValueKey] as String?;
  String? get unit2InputValue =>
      this.formValueMap[Unit2InputValueKey] as String?;
  String? get topic3InputValue =>
      this.formValueMap[Topic3InputValueKey] as String?;
  String? get unit3InputValue =>
      this.formValueMap[Unit3InputValueKey] as String?;
  String? get topic4InputValue =>
      this.formValueMap[Topic4InputValueKey] as String?;
  String? get unit4InputValue =>
      this.formValueMap[Unit4InputValueKey] as String?;

  set titleInputValue(String? value) {
    this.setData(
      this.formValueMap..addAll({TitleInputValueKey: value}),
    );

    if (_SensorsUpdateItemViewTextEditingControllers.containsKey(
        TitleInputValueKey)) {
      _SensorsUpdateItemViewTextEditingControllers[TitleInputValueKey]?.text =
          value ?? '';
    }
  }

  set topic1InputValue(String? value) {
    this.setData(
      this.formValueMap..addAll({Topic1InputValueKey: value}),
    );

    if (_SensorsUpdateItemViewTextEditingControllers.containsKey(
        Topic1InputValueKey)) {
      _SensorsUpdateItemViewTextEditingControllers[Topic1InputValueKey]?.text =
          value ?? '';
    }
  }

  set unit1InputValue(String? value) {
    this.setData(
      this.formValueMap..addAll({Unit1InputValueKey: value}),
    );

    if (_SensorsUpdateItemViewTextEditingControllers.containsKey(
        Unit1InputValueKey)) {
      _SensorsUpdateItemViewTextEditingControllers[Unit1InputValueKey]?.text =
          value ?? '';
    }
  }

  set topic2InputValue(String? value) {
    this.setData(
      this.formValueMap..addAll({Topic2InputValueKey: value}),
    );

    if (_SensorsUpdateItemViewTextEditingControllers.containsKey(
        Topic2InputValueKey)) {
      _SensorsUpdateItemViewTextEditingControllers[Topic2InputValueKey]?.text =
          value ?? '';
    }
  }

  set unit2InputValue(String? value) {
    this.setData(
      this.formValueMap..addAll({Unit2InputValueKey: value}),
    );

    if (_SensorsUpdateItemViewTextEditingControllers.containsKey(
        Unit2InputValueKey)) {
      _SensorsUpdateItemViewTextEditingControllers[Unit2InputValueKey]?.text =
          value ?? '';
    }
  }

  set topic3InputValue(String? value) {
    this.setData(
      this.formValueMap..addAll({Topic3InputValueKey: value}),
    );

    if (_SensorsUpdateItemViewTextEditingControllers.containsKey(
        Topic3InputValueKey)) {
      _SensorsUpdateItemViewTextEditingControllers[Topic3InputValueKey]?.text =
          value ?? '';
    }
  }

  set unit3InputValue(String? value) {
    this.setData(
      this.formValueMap..addAll({Unit3InputValueKey: value}),
    );

    if (_SensorsUpdateItemViewTextEditingControllers.containsKey(
        Unit3InputValueKey)) {
      _SensorsUpdateItemViewTextEditingControllers[Unit3InputValueKey]?.text =
          value ?? '';
    }
  }

  set topic4InputValue(String? value) {
    this.setData(
      this.formValueMap..addAll({Topic4InputValueKey: value}),
    );

    if (_SensorsUpdateItemViewTextEditingControllers.containsKey(
        Topic4InputValueKey)) {
      _SensorsUpdateItemViewTextEditingControllers[Topic4InputValueKey]?.text =
          value ?? '';
    }
  }

  set unit4InputValue(String? value) {
    this.setData(
      this.formValueMap..addAll({Unit4InputValueKey: value}),
    );

    if (_SensorsUpdateItemViewTextEditingControllers.containsKey(
        Unit4InputValueKey)) {
      _SensorsUpdateItemViewTextEditingControllers[Unit4InputValueKey]?.text =
          value ?? '';
    }
  }

  bool get hasTitleInput =>
      this.formValueMap.containsKey(TitleInputValueKey) &&
      (titleInputValue?.isNotEmpty ?? false);
  bool get hasTopic1Input =>
      this.formValueMap.containsKey(Topic1InputValueKey) &&
      (topic1InputValue?.isNotEmpty ?? false);
  bool get hasUnit1Input =>
      this.formValueMap.containsKey(Unit1InputValueKey) &&
      (unit1InputValue?.isNotEmpty ?? false);
  bool get hasTopic2Input =>
      this.formValueMap.containsKey(Topic2InputValueKey) &&
      (topic2InputValue?.isNotEmpty ?? false);
  bool get hasUnit2Input =>
      this.formValueMap.containsKey(Unit2InputValueKey) &&
      (unit2InputValue?.isNotEmpty ?? false);
  bool get hasTopic3Input =>
      this.formValueMap.containsKey(Topic3InputValueKey) &&
      (topic3InputValue?.isNotEmpty ?? false);
  bool get hasUnit3Input =>
      this.formValueMap.containsKey(Unit3InputValueKey) &&
      (unit3InputValue?.isNotEmpty ?? false);
  bool get hasTopic4Input =>
      this.formValueMap.containsKey(Topic4InputValueKey) &&
      (topic4InputValue?.isNotEmpty ?? false);
  bool get hasUnit4Input =>
      this.formValueMap.containsKey(Unit4InputValueKey) &&
      (unit4InputValue?.isNotEmpty ?? false);

  bool get hasTitleInputValidationMessage =>
      this.fieldsValidationMessages[TitleInputValueKey]?.isNotEmpty ?? false;
  bool get hasTopic1InputValidationMessage =>
      this.fieldsValidationMessages[Topic1InputValueKey]?.isNotEmpty ?? false;
  bool get hasUnit1InputValidationMessage =>
      this.fieldsValidationMessages[Unit1InputValueKey]?.isNotEmpty ?? false;
  bool get hasTopic2InputValidationMessage =>
      this.fieldsValidationMessages[Topic2InputValueKey]?.isNotEmpty ?? false;
  bool get hasUnit2InputValidationMessage =>
      this.fieldsValidationMessages[Unit2InputValueKey]?.isNotEmpty ?? false;
  bool get hasTopic3InputValidationMessage =>
      this.fieldsValidationMessages[Topic3InputValueKey]?.isNotEmpty ?? false;
  bool get hasUnit3InputValidationMessage =>
      this.fieldsValidationMessages[Unit3InputValueKey]?.isNotEmpty ?? false;
  bool get hasTopic4InputValidationMessage =>
      this.fieldsValidationMessages[Topic4InputValueKey]?.isNotEmpty ?? false;
  bool get hasUnit4InputValidationMessage =>
      this.fieldsValidationMessages[Unit4InputValueKey]?.isNotEmpty ?? false;

  String? get titleInputValidationMessage =>
      this.fieldsValidationMessages[TitleInputValueKey];
  String? get topic1InputValidationMessage =>
      this.fieldsValidationMessages[Topic1InputValueKey];
  String? get unit1InputValidationMessage =>
      this.fieldsValidationMessages[Unit1InputValueKey];
  String? get topic2InputValidationMessage =>
      this.fieldsValidationMessages[Topic2InputValueKey];
  String? get unit2InputValidationMessage =>
      this.fieldsValidationMessages[Unit2InputValueKey];
  String? get topic3InputValidationMessage =>
      this.fieldsValidationMessages[Topic3InputValueKey];
  String? get unit3InputValidationMessage =>
      this.fieldsValidationMessages[Unit3InputValueKey];
  String? get topic4InputValidationMessage =>
      this.fieldsValidationMessages[Topic4InputValueKey];
  String? get unit4InputValidationMessage =>
      this.fieldsValidationMessages[Unit4InputValueKey];
}

extension Methods on FormStateHelper {
  setTitleInputValidationMessage(String? validationMessage) =>
      this.fieldsValidationMessages[TitleInputValueKey] = validationMessage;
  setTopic1InputValidationMessage(String? validationMessage) =>
      this.fieldsValidationMessages[Topic1InputValueKey] = validationMessage;
  setUnit1InputValidationMessage(String? validationMessage) =>
      this.fieldsValidationMessages[Unit1InputValueKey] = validationMessage;
  setTopic2InputValidationMessage(String? validationMessage) =>
      this.fieldsValidationMessages[Topic2InputValueKey] = validationMessage;
  setUnit2InputValidationMessage(String? validationMessage) =>
      this.fieldsValidationMessages[Unit2InputValueKey] = validationMessage;
  setTopic3InputValidationMessage(String? validationMessage) =>
      this.fieldsValidationMessages[Topic3InputValueKey] = validationMessage;
  setUnit3InputValidationMessage(String? validationMessage) =>
      this.fieldsValidationMessages[Unit3InputValueKey] = validationMessage;
  setTopic4InputValidationMessage(String? validationMessage) =>
      this.fieldsValidationMessages[Topic4InputValueKey] = validationMessage;
  setUnit4InputValidationMessage(String? validationMessage) =>
      this.fieldsValidationMessages[Unit4InputValueKey] = validationMessage;

  /// Clears text input fields on the Form
  void clearForm() {
    titleInputValue = '';
    topic1InputValue = '';
    unit1InputValue = '';
    topic2InputValue = '';
    unit2InputValue = '';
    topic3InputValue = '';
    unit3InputValue = '';
    topic4InputValue = '';
    unit4InputValue = '';
  }

  /// Validates text input fields on the Form
  void validateForm() {
    this.setValidationMessages({
      TitleInputValueKey: getValidationMessage(TitleInputValueKey),
      Topic1InputValueKey: getValidationMessage(Topic1InputValueKey),
      Unit1InputValueKey: getValidationMessage(Unit1InputValueKey),
      Topic2InputValueKey: getValidationMessage(Topic2InputValueKey),
      Unit2InputValueKey: getValidationMessage(Unit2InputValueKey),
      Topic3InputValueKey: getValidationMessage(Topic3InputValueKey),
      Unit3InputValueKey: getValidationMessage(Unit3InputValueKey),
      Topic4InputValueKey: getValidationMessage(Topic4InputValueKey),
      Unit4InputValueKey: getValidationMessage(Unit4InputValueKey),
    });
  }
}

/// Returns the validation message for the given key
String? getValidationMessage(String key) {
  final validatorForKey = _SensorsUpdateItemViewTextValidations[key];
  if (validatorForKey == null) return null;

  String? validationMessageForKey = validatorForKey(
    _SensorsUpdateItemViewTextEditingControllers[key]!.text,
  );

  return validationMessageForKey;
}

/// Updates the fieldsValidationMessages on the FormViewModel
void updateValidationData(FormStateHelper model) =>
    model.setValidationMessages({
      TitleInputValueKey: getValidationMessage(TitleInputValueKey),
      Topic1InputValueKey: getValidationMessage(Topic1InputValueKey),
      Unit1InputValueKey: getValidationMessage(Unit1InputValueKey),
      Topic2InputValueKey: getValidationMessage(Topic2InputValueKey),
      Unit2InputValueKey: getValidationMessage(Unit2InputValueKey),
      Topic3InputValueKey: getValidationMessage(Topic3InputValueKey),
      Unit3InputValueKey: getValidationMessage(Unit3InputValueKey),
      Topic4InputValueKey: getValidationMessage(Topic4InputValueKey),
      Unit4InputValueKey: getValidationMessage(Unit4InputValueKey),
    });
