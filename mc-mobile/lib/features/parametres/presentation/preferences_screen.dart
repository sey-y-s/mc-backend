import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/parametres/domain/settings_models.dart';
import 'package:mlc_mobile/features/parametres/presentation/settings_providers.dart';

class PreferencesScreen extends ConsumerWidget {
  const PreferencesScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(preferencesProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Préférences')),
      body: AsyncValueView<AppPreferences>(
        value: value,
        onRetry: () => ref.invalidate(preferencesProvider),
        data: (settings) => ListView(
          padding: const EdgeInsets.all(16),
          children: [
            AppCard(
              child: Column(
                children: [
                  SwitchListTile(
                    secondary: const Icon(AppIcons.dataSaver),
                    title: const Text('Économie de données'),
                    subtitle: const Text(
                      'Réduire le chargement des images et des médias.',
                    ),
                    value: settings.lowDataMode,
                    onChanged: (v) =>
                        _save(context, ref, settings.copyWith(lowDataMode: v)),
                  ),
                  const Divider(),
                  SwitchListTile(
                    secondary: const Icon(AppIcons.notifications),
                    title: const Text('Notifications'),
                    subtitle: const Text(
                      'Préférence enregistrée sur cet appareil.',
                    ),
                    value: settings.notificationsEnabled,
                    onChanged: (v) => _save(
                      context,
                      ref,
                      settings.copyWith(notificationsEnabled: v),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _save(
    BuildContext context,
    WidgetRef ref,
    AppPreferences value,
  ) async {
    try {
      await ref.read(settingsRepositoryProvider).savePreferences(value);
      ref.invalidate(preferencesProvider);
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(failureMessage(e))));
      }
    }
  }
}
