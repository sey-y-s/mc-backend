import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/utils/validators.dart';
import 'package:mlc_mobile/core/widgets/app_text_field.dart';
import 'package:mlc_mobile/core/widgets/form_scaffold.dart';
import 'package:mlc_mobile/features/auth/presentation/password_reset_providers.dart';

/// Route : /forgot-password. L'utilisateur donne son téléphone ou son email ; un code lui est envoyé.
/// Le message de succès est neutre : on ne révèle jamais si un compte existe.
class ForgotPasswordScreen extends ConsumerStatefulWidget {
  const ForgotPasswordScreen({super.key});

  @override
  ConsumerState<ForgotPasswordScreen> createState() => _ForgotPasswordScreenState();
}

class _ForgotPasswordScreenState extends ConsumerState<ForgotPasswordScreen> {
  final _formKey = GlobalKey<FormState>();
  final _id = TextEditingController();

  @override
  void dispose() {
    _id.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    final ok = await ref.read(passwordResetControllerProvider.notifier).requestCode(_id.text.trim());
    if (!ok || !mounted) return;
    showSuccess(context, "Si ce compte existe, un code vient d'être envoyé.");
    context.push('/reset-password', extra: _id.text.trim());
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(passwordResetControllerProvider);
    final busy = state.isLoading;
    return FormScaffold(
      title: 'Mot de passe oublié',
      formKey: _formKey,
      submitLabel: 'Recevoir le code',
      onSubmit: _submit,
      isLoading: busy,
      error: state.hasError ? state.error : null,
      children: [
        Text(
          'Entrez votre téléphone ou votre email. Nous vous enverrons un code pour choisir un nouveau mot de passe.',
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: AppColors.muted),
        ),
        AppTextField(
          label: 'Téléphone ou email',
          controller: _id,
          enabled: !busy,
          keyboardType: TextInputType.emailAddress,
          textInputAction: TextInputAction.done,
          onSubmitted: (_) => _submit(),
          validator: (v) => Validators.required(v, field: "L'identifiant"),
        ),
        TextButton(
          onPressed: busy ? null : () => context.push('/reset-password', extra: _id.text.trim()),
          child: const Text("J'ai déjà un code"),
        ),
      ],
    );
  }
}