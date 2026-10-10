import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/router/app_shell.dart';
import 'package:mlc_mobile/app/router/routes.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/features.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_models.dart';

/// Routes protégées : le `redirect` ci-dessous bloque tout accès sans session authentifiée.
/// Les écrans « onglets » sont dans le ShellRoute (barre de navigation) ; les autres sont plein écran.
/// Ordre important : les routes statiques (/x/ajouter) doivent précéder les routes à paramètre (/x/:id).
final routerProvider = Provider<GoRouter>((ref) {
  final refresh = ValueNotifier<int>(0);
  ref.listen<SessionStatus>(sessionProvider, (_, _) => refresh.value++);
  ref.onDispose(refresh.dispose);

  return GoRouter(
    initialLocation: AppRoutes.splash,
    refreshListenable: refresh,
    redirect: (context, state) {
      final status = ref.read(sessionProvider);
      final loc = state.matchedLocation;
      if (status == SessionStatus.unknown) {
        return loc == AppRoutes.splash ? null : AppRoutes.splash;
      }
      final isPublic = AppRoutes.publicRoutes.contains(loc);
      if (status == SessionStatus.unauthenticated) {
        return isPublic ? null : AppRoutes.login;
      }
      if (isPublic || loc == AppRoutes.splash) return AppRoutes.home;
      return null;
    },
    errorBuilder: (context, state) => Scaffold(
      appBar: AppBar(title: const Text('Page introuvable')),
      body: const ErrorView(error: NotFoundFailure()),
    ),
    routes: [
      GoRoute(path: AppRoutes.splash, builder: (c, s) => const SplashScreen()),
      GoRoute(path: AppRoutes.login, builder: (c, s) => const LoginScreen()),
      GoRoute(
        path: AppRoutes.register,
        builder: (c, s) => const RegisterScreen(),
      ),
      GoRoute(
        path: '/competences',
        builder: (c, s) => const CompetencesListScreen(),
      ),
      ShellRoute(
        builder: (context, state, child) => AppShell(child: child),
        routes: [
          GoRoute(path: '/home', builder: (c, s) => const HomeScreen()),
          GoRoute(
            path: '/passeport',
            builder: (c, s) => const PassportScreen(),
          ),
          GoRoute(
            path: '/talents',
            builder: (c, s) => const TalentsSearchScreen(),
          ),
          GoRoute(
            path: '/notifications',
            builder: (c, s) => const NotificationsScreen(),
          ),
          GoRoute(
            path: '/parametres',
            builder: (c, s) => const SettingsScreen(),
          ),
        ],
      ),
      GoRoute(
        path: '/forgot-password',
        builder: (c, s) => const ForgotPasswordScreen(),
      ),
      GoRoute(
        path: '/reset-password',
        builder: (c, s) => ResetPasswordScreen(
          identifiant: s.extra is String ? s.extra as String : null,
        ),
      ),
      GoRoute(
        path: '/passeport/profil',
        builder: (c, s) => const ProfileEditScreen(),
      ),
      GoRoute(
        path: '/passeport/localisation',
        builder: (c, s) => const LocationScreen(),
      ),
      GoRoute(
        path: '/passeport/disponibilite',
        builder: (c, s) => const AvailabilityScreen(),
      ),
      GoRoute(
        path: '/passeport/cv',
        builder: (c, s) => const CvScreen(),
      ),
      GoRoute(
        path: '/competences/ajouter',
        builder: (c, s) => const AddCompetenceScreen(),
      ),
      GoRoute(
        path: '/competences/:id',
        builder: (c, s) => CompetenceDetailScreen(id: s.pathParameters['id']),
      ),
      GoRoute(
        path: '/experiences',
        builder: (c, s) => const ExperiencesListScreen(),
      ),
      GoRoute(
        path: '/experiences/nouvelle',
        builder: (c, s) => const ExperienceFormScreen(),
      ),
      GoRoute(
        path: '/experiences/:id',
        builder: (c, s) => ExperienceDetailScreen(id: s.pathParameters['id']),
      ),
      GoRoute(
        path: '/experiences/:id/modifier',
        builder: (c, s) => ExperienceEditScreen(id: s.pathParameters['id']),
      ),
      GoRoute(
        path: '/portfolio',
        builder: (c, s) => const PortfolioListScreen(),
      ),
      GoRoute(
        path: '/portfolio/nouvelle',
        builder: (c, s) => const PortfolioFormScreen(),
      ),
      GoRoute(
        path: '/portfolio/:id',
        builder: (c, s) => PortfolioDetailScreen(id: s.pathParameters['id']),
      ),
      GoRoute(
        path: '/portfolio/:id/modifier',
        builder: (c, s) => PortfolioEditScreen(id: s.pathParameters['id']),
      ),
      GoRoute(path: '/preuves', builder: (c, s) => const PreuvesListScreen()),
      GoRoute(
        path: '/preuves/ajouter',
        builder: (c, s) => AddPreuveScreen(
          competenceId: s.uri.queryParameters['competenceId'],
        ),
      ),
      GoRoute(
        path: '/preuves/:id',
        builder: (c, s) => PreuveDetailScreen(id: s.pathParameters['id']),
      ),
      GoRoute(
        path: '/validations',
        builder: (c, s) => const ValidationsListScreen(),
      ),
      GoRoute(
        path: '/validations/historique',
        builder: (c, s) => const ValidationsHistoryScreen(),
      ),
      GoRoute(
        path: '/validations/demander',
        builder: (c, s) => RequestValidationScreen(
          competenceId: s.uri.queryParameters['competenceId'],
          preuveId: s.uri.queryParameters['preuveId'],
        ),
      ),
      GoRoute(
        path: '/validations/:id',
        builder: (c, s) => ValidationDetailScreen(id: s.pathParameters['id']),
      ),
      GoRoute(path: '/tests', builder: (c, s) => const TestsListScreen()),
      GoRoute(
        path: '/tests/:id',
        builder: (c, s) => TestIntroScreen(id: s.pathParameters['id']),
      ),
      GoRoute(
        path: '/tests/:id/question',
        builder: (c, s) => TestQuestionScreen(id: s.pathParameters['id']),
      ),
      GoRoute(
        path: '/tests/:id/resultat',
        builder: (c, s) => TestResultScreen(
          id: s.pathParameters['id'],
          result: s.extra as ResultatTest?,
        ),
      ),
      GoRoute(
        path: '/admin',
        builder: (c, s) => AdminRouteGuard(child: const AdminHomeScreen()),
      ),
      GoRoute(
        path: '/admin/opportunites',
        builder: (c, s) => AdminRouteGuard(
          child: const AdminOpportunitiesScreen(),
        ),
      ),
      GoRoute(
        path: '/admin/tests',
        builder: (c, s) => AdminRouteGuard(child: const AdminTestsScreen()),
      ),
      GoRoute(
        path: '/admin/validations',
        builder: (c, s) => AdminRouteGuard(
          child: const AdminValidationsScreen(),
        ),
      ),
      GoRoute(
        path: '/admin/organisations',
        builder: (c, s) => AdminRouteGuard(
          child: const AdminOrganizationsScreen(),
        ),
      ),
      GoRoute(
        path: '/admin/utilisateurs',
        builder: (c, s) => AdminRouteGuard(
          superAdminOnly: true,
          child: const AdminUsersScreen(),
        ),
      ),
      GoRoute(
        path: '/talents/:id',
        builder: (c, s) => TalentProfileScreen(id: s.pathParameters['id']),
      ),
      GoRoute(path: '/relations', builder: (c, s) => const RelationsScreen()),
      GoRoute(
        path: '/relations/:id',
        builder: (c, s) => RelationDetailScreen(id: s.pathParameters['id']),
      ),
      GoRoute(
        path: '/opportunites',
        builder: (c, s) => const OpportunitesListScreen(),
      ),
      GoRoute(
        path: '/opportunites/:id',
        builder: (c, s) => OpportuniteDetailScreen(id: s.pathParameters['id']),
      ),
      GoRoute(
        path: '/notifications/:id',
        builder: (c, s) => NotificationDetailScreen(id: s.pathParameters['id']),
      ),
      GoRoute(
        path: '/parametres/compte',
        builder: (c, s) => const AccountSettingsScreen(),
      ),
      GoRoute(
        path: '/parametres/securite',
        builder: (c, s) => const SecuritySettingsScreen(),
      ),
      GoRoute(
        path: '/parametres/preferences',
        builder: (c, s) => const PreferencesScreen(),
      ),
      GoRoute(
        path: '/parametres/conditions',
        builder: (c, s) => const TermsScreen(),
      ),
    ],
  );
});
