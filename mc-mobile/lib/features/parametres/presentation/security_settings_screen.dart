import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/validators.dart';
import 'package:mlc_mobile/core/widgets/app_text_field.dart';
import 'package:mlc_mobile/core/widgets/form_scaffold.dart';
import 'package:mlc_mobile/features/parametres/presentation/settings_providers.dart';

class SecuritySettingsScreen extends ConsumerStatefulWidget {
  const SecuritySettingsScreen({super.key});
  @override
  ConsumerState<SecuritySettingsScreen> createState() =>
      _SecuritySettingsScreenState();
}

class _SecuritySettingsScreenState
    extends ConsumerState<SecuritySettingsScreen> {
  final _key = GlobalKey<FormState>();
  final _current = TextEditingController(),
      _next = TextEditingController(),
      _confirm = TextEditingController();
  bool _saving = false;
  Object? _error;
  @override
  void dispose() {
    _current.dispose();
    _next.dispose();
    _confirm.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => FormScaffold(
    title: 'Sécurité',
    formKey: _key,
    submitLabel: 'Changer le mot de passe',
    isLoading: _saving,
    error: _error,
    onSubmit: _save,
    children: [
      AppTextField(
        label: 'Mot de passe actuel',
        controller: _current,
        obscure: true,
        validator: (v) =>
            Validators.required(v, field: 'Le mot de passe actuel'),
      ),
      AppTextField(
        label: 'Nouveau mot de passe',
        controller: _next,
        obscure: true,
        validator: Validators.password,
      ),
      AppTextField(
        label: 'Confirmer le nouveau mot de passe',
        controller: _confirm,
        obscure: true,
        validator: (v) => Validators.confirmPassword(v, _next.text),
      ),
    ],
  );
  Future<void> _save() async {
    if (!_key.currentState!.validate()) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await ref
          .read(settingsRepositoryProvider)
          .changePassword(
            currentPassword: _current.text,
            newPassword: _next.text,
          );
      _current.clear();
      _next.clear();
      _confirm.clear();
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('Mot de passe modifié.')));
      }
    } catch (e) {
      if (mounted) {
        setState(() => _error = e is AppFailure ? e : const UnknownFailure());
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }
}
