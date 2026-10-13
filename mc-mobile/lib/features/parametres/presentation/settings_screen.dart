import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/router/routes.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/widgets/app_dialogs.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/features/admin/presentation/admin_providers.dart';
import 'package:mlc_mobile/features/auth/presentation/auth_providers.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final role = ref.watch(currentRoleProvider).value;
    final isAdmin = role == 'ADMIN' || role == 'SUPER_ADMIN';

    return Scaffold(
      appBar: AppBar(title: const Text('Paramètres')),
      body: LayoutBuilder(
        builder: (context, _) => SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 640),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const _SectionHeading(
                    title: 'Votre espace',
                    subtitle: 'Gérez votre profil et vos informations de contact.',
                  ),
                  const SizedBox(height: 12),
                  _SettingsGroup(
                    items: [
                      _SettingsItemData(
                        icon: AppIcons.person,
                        title: 'Mon compte',
                        subtitle: 'Téléphone, adresse e-mail et coordonnées',
                        onTap: () => context.push('/parametres/compte'),
                      ),
                      _SettingsItemData(
                        icon: AppIcons.lock,
                        title: 'Sécurité',
                        subtitle: 'Mot de passe et protection du compte',
                        onTap: () => context.push('/parametres/securite'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 24),
                  const _SectionHeading(
                    title: 'Application',
                    subtitle: 'Personnalisez votre expérience sur cet appareil.',
                  ),
                  const SizedBox(height: 12),
                  _SettingsGroup(
                    items: [
                      _SettingsItemData(
                        icon: AppIcons.tune,
                        title: 'Préférences',
                        subtitle: 'Notifications et économie de données',
                        onTap: () => context.push('/parametres/preferences'),
                      ),
                      _SettingsItemData(
                        icon: AppIcons.document,
                        title: 'Conditions d’utilisation',
                        subtitle: 'Consulter les règles d’utilisation du service',
                        onTap: () => context.push('/parametres/conditions'),
                      ),
                    ],
                  ),
                  if (isAdmin) ...[
                    const SizedBox(height: 24),
                    const _SectionHeading(
                      title: 'Administration',
                      subtitle: 'Outils réservés à l’équipe de gestion.',
                    ),
                    const SizedBox(height: 12),
                    _SettingsGroup(
                      items: [
                        _SettingsItemData(
                          icon: Icons.admin_panel_settings_outlined,
                          title: 'Ouvrir l’administration',
                          subtitle: 'Gérer les utilisateurs et les ressources',
                          onTap: () => context.push('/admin'),
                          iconColor: AppColors.goldDeep,
                          iconBackground: AppColors.goldSoft,
                        ),
                      ],
                    ),
                  ],
                  const SizedBox(height: 28),
                  OutlinedButton.icon(
                    onPressed: () => _logout(context, ref),
                    icon: const Icon(AppIcons.logout),
                    label: const Text('Se déconnecter'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.error,
                      side: const BorderSide(color: AppColors.error),
                      minimumSize: const Size.fromHeight(54),
                    ),
                  ),
                  const SizedBox(height: 18),
                  Text(
                    'Mali Compétences · Votre parcours, vos talents.',
                    textAlign: TextAlign.center,
                    style: Theme.of(context)
                        .textTheme
                        .bodySmall
                        ?.copyWith(color: AppColors.muted),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Future<void> _logout(BuildContext context, WidgetRef ref) async {
    final yes = await confirmDialog(
      context,
      title: 'Se déconnecter ?',
      message: 'Vous devrez vous reconnecter pour accéder à votre compte.',
      confirmLabel: 'Déconnexion',
      destructive: true,
    );
    if (!yes || !context.mounted) return;
    try {
      await ref.read(authControllerProvider.notifier).logout();
      if (context.mounted) context.go(AppRoutes.login);
    } catch (error) {
      if (context.mounted) {
        ScaffoldMessenger.of(context)
          ..hideCurrentSnackBar()
          ..showSnackBar(SnackBar(content: Text(failureMessage(error))));
      }
    }
  }
}

class _SectionHeading extends StatelessWidget {
  const _SectionHeading({required this.title, required this.subtitle});

  final String title;
  final String subtitle;

  @override
  Widget build(BuildContext context) => Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 3),
          Text(
            subtitle,
            style: Theme.of(context)
                .textTheme
                .bodySmall
                ?.copyWith(color: AppColors.muted),
          ),
        ],
      );
}

class _SettingsGroup extends StatelessWidget {
  const _SettingsGroup({required this.items});
  final List<_SettingsItemData> items;

  @override
  Widget build(BuildContext context) => Container(
        decoration: BoxDecoration(
          color: AppColors.card,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: AppColors.border),
        ),
        child: Column(
          children: [
            for (var index = 0; index < items.length; index++) ...[
              _SettingsItem(item: items[index]),
              if (index < items.length - 1)
                const Padding(
                  padding: EdgeInsets.only(left: 68),
                  child: Divider(height: 1),
                ),
            ],
          ],
        ),
      );
}

class _SettingsItemData {
  const _SettingsItemData({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
    this.iconColor = AppColors.green,
    this.iconBackground = AppColors.greenSoft,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;
  final Color iconColor;
  final Color iconBackground;
}

class _SettingsItem extends StatelessWidget {
  const _SettingsItem({required this.item});
  final _SettingsItemData item;

  @override
  Widget build(BuildContext context) => InkWell(
        onTap: item.onTap,
        borderRadius: BorderRadius.circular(18),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
          child: Row(
            children: [
              Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: item.iconBackground,
                  borderRadius: BorderRadius.circular(13),
                ),
                child: Icon(item.icon, color: item.iconColor, size: 21),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(item.title,
                        style: Theme.of(context).textTheme.titleSmall),
                    const SizedBox(height: 3),
                    Text(
                      item.subtitle,
                      style: Theme.of(context)
                          .textTheme
                          .bodySmall
                          ?.copyWith(color: AppColors.muted),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              const Icon(AppIcons.chevron, color: AppColors.muted, size: 19),
            ],
          ),
        ),
      );
}
