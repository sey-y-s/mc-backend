import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/utils/validators.dart';
import 'package:mlc_mobile/core/widgets/app_text_field.dart';
import 'package:mlc_mobile/core/widgets/form_scaffold.dart';
import 'package:mlc_mobile/features/auth/presentation/password_reset_providers.dart';

/// Route : /reset-password. Code reçu + nouveau mot de passe. Succès : retour à la connexion.
class ResetPasswordScreen extends ConsumerStatefulWidget {
  const ResetPasswordScreen({super.key, this.identifiant});

  /// Identifiant saisi à l'écran précédent (peut être vide si l'utilisateur a déjà un code).
  final String? identifiant;

  @override
  ConsumerState<ResetPasswordScreen> createState() => _ResetPasswordScreenState();
}

class _ResetPasswordScreenState extends ConsumerState<ResetPasswordScreen> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _id =
      TextEditingController(text: widget.identifiant ?? '');
  final _code = TextEditingController();
  final _pwd = TextEditingController();
  final _pwd2 = TextEditingController();

  @override
  void dispose() {
    _id.dispose();
    _code.dispose();
    _pwd.dispose();
    _pwd2.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    final ok = await ref.read(passwordResetControllerProvider.notifier).reset(
          identifiant: _id.text.trim(),
          code: _code.text.trim(),
          newPassword: _pwd.text,
        );
    if (!ok || !mounted) return;
    showSuccess(context, 'Mot de passe modifié. Connectez-vous.');
    context.go('/login');
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(passwordResetControllerProvider);
    final busy = state.isLoading;
    return FormScaffold(
      title: 'Réinitialisation',
      formKey: _formKey,
      submitLabel: 'Changer le mot de passe',
      onSubmit: _submit,
      isLoading: busy,
      error: state.hasError ? state.error : null,
      children: [
        Text(
          'Saisissez le code reçu, puis choisissez un nouveau mot de passe.',
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: AppColors.muted),
        ),
        AppTextField(
          label: 'Téléphone ou email',
          controller: _id,
          enabled: !busy,
          keyboardType: TextInputType.emailAddress,
          textInputAction: TextInputAction.next,
          validator: (v) => Validators.required(v, field: "L'identifiant"),
        ),
        AppTextField(
          label: 'Code reçu',
          controller: _code,
          enabled: !busy,
          keyboardType: TextInputType.number,
          textInputAction: TextInputAction.next,
          validator: Validators.resetCode,
        ),
        AppTextField(
          label: 'Nouveau mot de passe',
          controller: _pwd,
          obscure: true,
          enabled: !busy,
          textInputAction: TextInputAction.next,
          validator: Validators.password,
        ),
        AppTextField(
          label: 'Confirmer le mot de passe',
          controller: _pwd2,
          obscure: true,
          enabled: !busy,
          textInputAction: TextInputAction.done,
          validator: (v) => Validators.confirmPassword(v, _pwd.text),
        ),
        TextButton(
          onPressed: busy ? null : () => Navigator.of(context).maybePop(),
          child: const Text("Je n'ai pas reçu de code"),
        ),
      ],
    );
  }
}