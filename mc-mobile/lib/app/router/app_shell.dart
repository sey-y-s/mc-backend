import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/app_nav_bar.dart';
import 'package:mlc_mobile/features/notifications/presentation/notification_providers.dart';

/// Navigation principale (5 onglets) : Accueil, Passeport, Talents, Alertes, Plus.
class AppShell extends ConsumerWidget {
  const AppShell({super.key, required this.child});
  final Widget child;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final unreadCount =
        ref.watch(unreadNotificationCountProvider).value ?? 0;

    final tabs = <(AppNavItem, String)>[
      (
        const AppNavItem(
            label: 'Accueil',
            icon: AppIcons.home,
            activeIcon: AppIcons.homeActive),
        '/home'
      ),
      (
        const AppNavItem(
            label: 'Passeport',
            icon: AppIcons.passport,
            activeIcon: AppIcons.passportActive),
        '/passeport'
      ),
      (
        const AppNavItem(
            label: 'Talents',
            icon: AppIcons.talents,
            activeIcon: AppIcons.talentsActive),
        '/talents'
      ),
      (
        AppNavItem(
          label: 'Alertes',
          icon: AppIcons.notifications,
          activeIcon: AppIcons.notificationsActive,
          badgeCount: unreadCount,
        ),
        '/notifications'
      ),
      (
        const AppNavItem(
            label: 'Plus',
            icon: AppIcons.more,
            activeIcon: AppIcons.moreActive),
        '/parametres'
      ),
    ];

    final path = GoRouterState.of(context).uri.path;
    final index = tabs.indexWhere((t) => path.startsWith(t.$2));
    return Scaffold(
      body: child,
      bottomNavigationBar: AppNavBar(
        items: [for (final t in tabs) t.$1],
        currentIndex: index < 0 ? 0 : index,
        onTap: (i) => context.go(tabs[i].$2),
      ),
    );
  }
}
