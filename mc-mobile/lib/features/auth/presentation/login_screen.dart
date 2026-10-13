import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/validators.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_logo.dart';
import 'package:mlc_mobile/core/widgets/bogolan_pattern.dart';
import 'package:mlc_mobile/core/widgets/app_text_field.dart';
import 'package:mlc_mobile/features/auth/presentation/auth_providers.dart';
import 'package:mlc_mobile/l10n/strings.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});
  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _id = TextEditingController();
  final _pwd = TextEditingController();

  @override
  void dispose() {
    _id.dispose();
    _pwd.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    // La navigation est faite par le routeur dès que la session passe à "authenticated".
    await ref.read(authControllerProvider.notifier).login(_id.text.trim(), _pwd.text);
  }

  @override
  Widget build(BuildContext context) {
    final auth = ref.watch(authControllerProvider);
    final text = Theme.of(context).textTheme;
    return AnnotatedRegion<SystemUiOverlayStyle>(
      value: SystemUiOverlayStyle.light,
      child: Scaffold(
        body: SingleChildScrollView(
          child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
            // Seul moment « décoratif » de l'écran : le motif bogolan.
            Container(
              height: 230,
              decoration: const BoxDecoration(
                color: AppColors.green,
                borderRadius: BorderRadius.vertical(bottom: Radius.circular(32)),
              ),
              child: Stack(children: [
                const Positioned.fill(child: BogolanPattern(color: Color(0x2EC09427))),
                SafeArea(
                  child: Center(
                    child: Column(mainAxisSize: MainAxisSize.min, children: [
                      const AppLogo(size: 72),
                      const SizedBox(height: 12),
                      Text(S.appName, style: text.headlineSmall?.copyWith(color: Colors.white)),
                    ]),
                  ),
                ),
              ]),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 28, 24, 24),
              child: Form(
                key: _formKey,
                child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                  Text('Bienvenue', style: text.headlineMedium),
                  const SizedBox(height: 4),
                  Text('Connectez-vous à votre passeport de compétences.',
                      style: text.bodyMedium?.copyWith(color: AppColors.muted)),
                  const SizedBox(height: 24),
                  AppTextField(
                    label: 'Téléphone ou email',
                    controller: _id,
                    keyboardType: TextInputType.emailAddress,
                    textInputAction: TextInputAction.next,
                    enabled: !auth.isLoading,
                    validator: (v) => Validators.required(v, field: "L'identifiant"),
                  ),
                  const SizedBox(height: 16),
                  AppTextField(
                    label: 'Mot de passe',
                    controller: _pwd,
                    obscure: true,
                    textInputAction: TextInputAction.done,
                    enabled: !auth.isLoading,
                    onSubmitted: (_) => _submit(),
                    validator: (v) => Validators.required(v, field: 'Le mot de passe'),
                  ),
                  if (auth.hasError) ...[
                    const SizedBox(height: 12),
                    Text(failureMessage(auth.error!), style: const TextStyle(color: AppColors.error)),
                  ],
                  const SizedBox(height: 24),
                  AppButton(label: S.login, onPressed: _submit, isLoading: auth.isLoading),
                  const SizedBox(height: 8),
                  TextButton(
                    onPressed: auth.isLoading ? null : () => context.push('/forgot-password'),
                    child: const Text('Mot de passe oublié ?'),
                  ),
                  const SizedBox(height: 4),
                  AppButton(
                    label: S.register,
                    variant: AppButtonVariant.secondary,
                    onPressed: auth.isLoading ? null : () => context.push('/register'),
                  ),
                ]),
              ),
            ),
          ]),
        ),
      ),
    );
  }
}
