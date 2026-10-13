import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/app_progress_bar.dart';
import 'package:mlc_mobile/core/widgets/bogolan_pattern.dart';
import 'package:mlc_mobile/core/widgets/section_header.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/competences/presentation/competences_providers.dart';

import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/features/passport/presentation/passport_providers.dart';
import 'package:mlc_mobile/features/home/presentation/blocks/notifications_block.dart';
import 'package:mlc_mobile/features/home/presentation/blocks/opportunites_block.dart';

/// Dashboard citoyen : en-tête (résumé + progression), carte « Mon passeport », accès rapide,
/// puis les blocs notifications et opportunités (voir features/home/presentation/blocks/).

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  static const _shortcuts = <(String, IconData, String)>[
    ('Compétences', AppIcons.competences, '/competences'),
    ('Expériences', AppIcons.experiences, '/experiences'),
    ('Portfolio', AppIcons.portfolio, '/portfolio'),
    ('Preuves', AppIcons.preuves, '/preuves'),
    ('Validations', AppIcons.validations, '/validations'),
    ('Tests', AppIcons.tests, '/tests'),
    ('Relations', AppIcons.relations, '/relations'),
    ('Opportunités', AppIcons.opportunites, '/opportunites'),
  ];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final comps = ref.watch(competencesProvider);
    return AnnotatedRegion<SystemUiOverlayStyle>(
      value: SystemUiOverlayStyle.light,
      child: Scaffold(
        body: ListView(
          padding: EdgeInsets.zero,
          children: [
            _Header(
              comps: comps,
              onRetry: () => ref.invalidate(competencesProvider),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 28, 20, 24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const _PassportCard(),
                  const SizedBox(height: 28),
                  const SectionHeader(title: 'Accès rapide'),
                  GridView.count(
                    crossAxisCount: 4,
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    mainAxisSpacing: 16,
                    crossAxisSpacing: 8,
                    childAspectRatio: 0.8,
                    children: [
                      for (final s in _shortcuts)
                        _Shortcut(
                          label: s.$1,
                          icon: s.$2,
                          onTap: () => context.push(s.$3),
                        ),
                    ],
                  ),
                  const SizedBox(height: 28),
                  const NotificationsBlock(),
                  const SizedBox(height: 28),
                  const OpportunitesBlock(),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({required this.comps, required this.onRetry});
  final AsyncValue<List<CitoyenCompetence>> comps;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return Container(
      decoration: const BoxDecoration(
        color: AppColors.green,
        borderRadius: BorderRadius.vertical(bottom: Radius.circular(32)),
      ),
      child: Stack(
        children: [
          const Positioned.fill(
            child: BogolanPattern(color: Color(0x2EC09427)),
          ),
          SafeArea(
            bottom: false,
            child: Padding(
              padding: const EdgeInsets.fromLTRB(24, 20, 24, 28),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Bonjour',
                    style: text.bodyMedium?.copyWith(color: Colors.white70),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Votre passeport\nde compétences',
                    style: text.headlineMedium?.copyWith(color: Colors.white),
                  ),
                  const SizedBox(height: 24),
                  comps.when(
                    loading: () => const SizedBox(
                      height: 72,
                      child: Center(
                        child: CircularProgressIndicator(color: AppColors.gold),
                      ),
                    ),
                    error: (e, _) => Row(
                      children: [
                        Expanded(
                          child: Text(
                            failureMessage(e),
                            style: const TextStyle(color: Colors.white),
                          ),
                        ),
                        TextButton(
                          onPressed: onRetry,
                          style: TextButton.styleFrom(
                            foregroundColor: AppColors.gold,
                          ),
                          child: const Text('Réessayer'),
                        ),
                      ],
                    ),
                    data: (items) {
                      int n(EtatCompetence e) =>
                          items.where((c) => c.etat == e).length;
                      return Column(
                        children: [
                          Row(
                            children: [
                              _Stat(
                                label: 'déclarées',
                                value: n(EtatCompetence.declaree),
                              ),
                              _Stat(
                                label: 'attestées',
                                value: n(EtatCompetence.attestee),
                              ),
                              _Stat(
                                label: 'validées',
                                value: n(EtatCompetence.validee),
                              ),
                            ],
                          ),
                          const SizedBox(height: 20),
                          AppProgressBar(
                            onDark: true,
                            label: 'Compétences validées',
                            value: items.isEmpty
                                ? 0
                                : n(EtatCompetence.validee) / items.length,
                          ),
                        ],
                      );
                    },
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _Stat extends StatelessWidget {
  const _Stat({required this.label, required this.value});
  final String label;
  final int value;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return Expanded(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '$value',
            style: text.displaySmall?.copyWith(color: Colors.white),
          ),
          Text(label, style: text.bodySmall?.copyWith(color: Colors.white70)),
        ],
      ),
    );
  }
}

class _Shortcut extends StatelessWidget {
  const _Shortcut({
    required this.label,
    required this.icon,
    required this.onTap,
  });
  final String label;
  final IconData icon;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => InkWell(
    onTap: onTap,
    borderRadius: BorderRadius.circular(16),
    child: Column(
      children: [
        Container(
          width: 56,
          height: 56,
          decoration: BoxDecoration(
            color: AppColors.greenSoft,
            borderRadius: BorderRadius.circular(18),
          ),
          child: Icon(icon, color: AppColors.green, size: 26),
        ),
        const SizedBox(height: 8),
        Text(
          label,
          textAlign: TextAlign.center,
          maxLines: 2,
          style: Theme.of(context).textTheme.labelSmall,
        ),
      ],
    ),
  );
}

class _PassportCard extends ConsumerWidget {
  const _PassportCard();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final text = Theme.of(context).textTheme;
    final citoyen = ref.watch(passportProvider);
    final completion = ref.watch(profileCompletionProvider);
    return AppCard(
      onTap: () => context.go('/passeport'),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            children: [
              const Icon(AppIcons.passport, color: AppColors.green),
              const SizedBox(width: 10),
              Expanded(child: Text('Mon passeport', style: text.titleMedium)),
              if (citizenCode(citoyen) != null)
                Text(
                  citizenCode(citoyen)!,
                  style: text.labelMedium?.copyWith(color: AppColors.goldDeep),
                ),
            ],
          ),
          const SizedBox(height: 14),
          completion.when(
            loading: () => const SizedBox(
              height: 40,
              child: Center(child: CircularProgressIndicator()),
            ),
            error: (e, _) => Text(failureMessage(e), style: text.bodySmall),
            data: (c) => Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                AppProgressBar(label: 'Passeport complété', value: c.ratio),
                if (c.next != null) ...[
                  const SizedBox(height: 10),
                  Text(
                    'Prochaine étape : ${c.next!.hint}',
                    style: text.bodySmall?.copyWith(color: AppColors.muted),
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  String? citizenCode(AsyncValue<dynamic> v) =>
      v.asData?.value.codePasseport as String?;
}
