import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/experiences/presentation/experience_form_screen.dart';
import 'package:mlc_mobile/features/experiences/presentation/experience_providers.dart';

/// Route : /experiences/:id/modifier. Charge l'expérience puis affiche le formulaire partagé, pré-rempli.
class ExperienceEditScreen extends ConsumerWidget {
  const ExperienceEditScreen({super.key, this.id});
  final String? id;

  Widget _shell(Widget body) => Scaffold(appBar: AppBar(title: const Text("Modifier l'expérience")), body: body);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final id = this.id;
    if (id == null) return _shell(const ErrorView(error: NotFoundFailure()));
    // skipLoadingOnReload : l'enregistrement invalide le détail ; le formulaire ne doit pas être détruit en plein envoi.
    return ref.watch(experienceDetailProvider(id)).when(
          skipLoadingOnReload: true,
          loading: () => _shell(const LoadingView()),
          error: (e, _) => _shell(ErrorView(error: e, onRetry: () => ref.invalidate(experienceDetailProvider(id)))),
          data: (e) => ExperienceFormScreen(initial: e),
        );
  }
}