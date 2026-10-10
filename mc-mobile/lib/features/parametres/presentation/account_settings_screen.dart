import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/validators.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_text_field.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/parametres/domain/settings_models.dart';
import 'package:mlc_mobile/features/parametres/presentation/settings_providers.dart';

class AccountSettingsScreen extends ConsumerWidget {
  const AccountSettingsScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final account = ref.watch(accountProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Mon compte')),
      body: AsyncValueView<UserAccount>(
        value: account,
        onRetry: () => ref.invalidate(accountProvider),
        data: (value) => _AccountForm(account: value),
      ),
    );
  }
}

class _AccountForm extends ConsumerStatefulWidget {
  const _AccountForm({required this.account});
  final UserAccount account;
  @override
  ConsumerState<_AccountForm> createState() => _AccountFormState();
}

class _AccountFormState extends ConsumerState<_AccountForm> {
  final _key = GlobalKey<FormState>();
  late final TextEditingController _phone, _email;
  bool _saving = false;
  Object? _error;
  @override
  void initState() {
    super.initState();
    _phone = TextEditingController(text: widget.account.telephone);
    _email = TextEditingController(text: widget.account.email ?? '');
  }

  @override
  void dispose() {
    _phone.dispose();
    _email.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => SafeArea(
    child: SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Form(
        key: _key,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            AppTextField(
              label: 'Téléphone',
              controller: _phone,
              keyboardType: TextInputType.phone,
              validator: Validators.phone,
            ),
            const SizedBox(height: 16),
            AppTextField(
              label: 'Email (facultatif)',
              controller: _email,
              keyboardType: TextInputType.emailAddress,
              validator: Validators.emailOptional,
            ),
            const SizedBox(height: 16),
            const Text(
              'Une modification du téléphone peut demander une nouvelle vérification.',
              style: TextStyle(color: Colors.black54),
            ),
            if (_error != null) ...[
              const SizedBox(height: 12),
              Text(
                failureMessage(_error!),
                style: const TextStyle(color: Colors.red),
              ),
            ],
            const SizedBox(height: 24),
            AppButton(
              label: 'Enregistrer',
              onPressed: _save,
              isLoading: _saving,
            ),
          ],
        ),
      ),
    ),
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
          .updateAccount(
            AccountChanges(telephone: _phone.text, email: _email.text),
          );
      ref.read(accountRevisionProvider.notifier).state++;
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Coordonnées mises à jour.')),
        );
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
