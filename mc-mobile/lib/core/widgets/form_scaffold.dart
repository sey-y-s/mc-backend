import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';

/// Écran de formulaire standard : AppBar, défilement, espacement, erreur globale, bouton d'envoi.
/// Tous les formulaires (expérience, preuve, profil...) l'utilisent : un seul comportement.
class FormScaffold extends StatelessWidget {
  const FormScaffold({
    super.key,
    required this.title,
    required this.formKey,
    required this.children,
    required this.submitLabel,
    required this.onSubmit,
    this.isLoading = false,
    this.error,
  });

  final String title;
  final GlobalKey<FormState> formKey;
  final List<Widget> children;
  final String submitLabel;
  final VoidCallback onSubmit;
  final bool isLoading;

  /// Erreur de la dernière soumission (affichée via [failureMessage], jamais brute).
  final Object? error;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Form(
            key: formKey,
            child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
              for (final c in children) ...[c, const SizedBox(height: 16)],
              if (error != null) ...[
                Text(failureMessage(error!), style: const TextStyle(color: AppColors.error)),
                const SizedBox(height: 12),
              ],
              const SizedBox(height: 8),
              AppButton(label: submitLabel, onPressed: onSubmit, isLoading: isLoading),
            ]),
          ),
        ),
      ),
    );
  }
}
