import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/filter_chips.dart';
import 'package:mlc_mobile/core/widgets/paginated_list_view.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/talents/domain/talent_models.dart';
import 'package:mlc_mobile/features/talents/presentation/talent_providers.dart';

class TalentsSearchScreen extends ConsumerStatefulWidget {
  const TalentsSearchScreen({super.key});
  @override
  ConsumerState<TalentsSearchScreen> createState() =>
      _TalentsSearchScreenState();
}

class _TalentsSearchScreenState extends ConsumerState<TalentsSearchScreen> {
  late final TextEditingController _query;
  @override
  void initState() {
    super.initState();
    _query = TextEditingController();
  }

  @override
  void dispose() {
    _query.dispose();
    super.dispose();
  }

  Future<void> _filters() async {
    final current = ref.read(talentFiltersProvider);
    final result = await showModalBottomSheet<TalentFilters>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (ctx) => _TalentFilterSheet(initial: current),
    );
    if (result == null || !mounted) return;
    ref.read(talentFiltersProvider.notifier).state = result;
    ref.read(talentSearchRequestedProvider.notifier).state = true;
  }

  @override
  Widget build(BuildContext context) {
    final filters = ref.watch(talentFiltersProvider);
    final requested = ref.watch(talentSearchRequestedProvider);
    return Scaffold(
      appBar: AppBar(
        title: const Text('Recherche de talents'),
        actions: [
          IconButton(
            tooltip: 'Filtres',
            icon: const Icon(AppIcons.filter),
            onPressed: _filters,
          ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _query,
                    textInputAction: TextInputAction.search,
                    onSubmitted: _runSearch,
                    decoration: const InputDecoration(
                      labelText: 'Compétence ou métier',
                      hintText: 'Ex. soudure, couture, solaire',
                      prefixIcon: Icon(AppIcons.search),
                      border: OutlineInputBorder(),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton.filled(
                  tooltip: 'Rechercher',
                  onPressed: () => _runSearch(_query.text),
                  icon: const Icon(AppIcons.search),
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
            child: Row(
              children: [
                Expanded(
                  child: Text(
                    filters.regionId == null
                        ? 'Résultats anonymisés'
                        : 'Région : ${filters.regionId!}',
                    style: const TextStyle(
                      color: AppColors.muted,
                      fontSize: 12,
                    ),
                  ),
                ),
                if (filters.availability != null)
                  StatusBadge(
                    label: filters.availability!.label,
                    tone: BadgeTone.success,
                  ),
              ],
            ),
          ),
          Expanded(
            child: !requested
                ? const Center(
                    child: Text(
                      'Recherchez un métier ou une compétence pour voir les profils anonymisés.',
                    ),
                  )
                : PaginatedListView<TalentSummary>(
                    key: ValueKey(filters),
                    pageSize: 20,
                    emptyMessage: 'Aucun profil ne correspond à ces critères.',
                    fetchPage: (page, size) async {
                      await Future<void>.delayed(
                        const Duration(milliseconds: 400),
                      );
                      return ref
                          .read(talentRepositoryProvider)
                          .search(filters, page: page, size: size);
                    },
                    itemBuilder: (context, talent) => AppCard(
                      onTap: () => context.push('/talents/${talent.id}'),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              const CircleAvatar(
                                radius: 21,
                                backgroundColor: AppColors.greenSoft,
                                child: Icon(
                                  AppIcons.person,
                                  color: AppColors.green,
                                ),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    const Text(
                                      'Profil anonymisé',
                                      style: TextStyle(
                                        fontWeight: FontWeight.w700,
                                      ),
                                    ),
                                    Text(
                                      '${talent.communeName}, ${talent.regionName}',
                                      style: const TextStyle(
                                        fontSize: 12,
                                        color: AppColors.muted,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                              const Icon(
                                AppIcons.chevron,
                                color: AppColors.muted,
                              ),
                            ],
                          ),
                          const SizedBox(height: 12),
                          Wrap(
                            spacing: 6,
                            runSpacing: 6,
                            children: [
                              for (final skill in talent.skills.take(3))
                                Chip(
                                  label: Text(
                                    '${skill.name} · ${skill.level.label}',
                                  ),
                                  visualDensity: VisualDensity.compact,
                                ),
                            ],
                          ),
                          Text(
                            talent.availability.label,
                            style: const TextStyle(
                              fontSize: 12,
                              color: AppColors.green,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
          ),
        ],
      ),
    );
  }

  void _runSearch(String value) {
    final filters = ref.read(talentFiltersProvider);
    ref.read(talentFiltersProvider.notifier).state = filters.copyWith(
      query: value.trim(),
    );
    ref.read(talentSearchRequestedProvider.notifier).state = true;
  }
}

class _TalentFilterSheet extends ConsumerStatefulWidget {
  const _TalentFilterSheet({required this.initial});
  final TalentFilters initial;
  @override
  ConsumerState<_TalentFilterSheet> createState() => _TalentFilterSheetState();
}

class _TalentFilterSheetState extends ConsumerState<_TalentFilterSheet> {
  String? _regionId;
  TalentAvailability? _availability;
  Niveau? _level;
  @override
  void initState() {
    super.initState();
    _regionId = widget.initial.regionId;
    _availability = widget.initial.availability;
    _level = widget.initial.minimumLevel;
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Padding(
    padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom),
    child: ListView(
      shrinkWrap: true,
      padding: const EdgeInsets.all(20),
      children: [
        Text(
          'Filtres de recherche',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 12),
        ref
            .watch(talentRegionsProvider)
            .when(
              loading: () => const LinearProgressIndicator(),
              error: (e, _) => Row(
                children: [
                  Expanded(child: Text(failureMessage(e))),
                  TextButton(
                    onPressed: () => ref.invalidate(talentRegionsProvider),
                    child: const Text('Réessayer'),
                  ),
                ],
              ),
              data: (regions) => DropdownButtonFormField<String>(
                initialValue: _regionId ?? '',
                decoration: const InputDecoration(labelText: 'Région'),
                items: [
                  const DropdownMenuItem(
                    value: '',
                    child: Text('Toutes les régions'),
                  ),
                  for (final region in regions)
                    DropdownMenuItem(
                      value: region.id,
                      child: Text(region.label),
                    ),
                ],
                onChanged: (value) => setState(
                  () =>
                      _regionId = value == null || value.isEmpty ? null : value,
                ),
              ),
            ),
        const SizedBox(height: 16),
        const Text('Disponibilité'),
        const SizedBox(height: 8),
        FilterChips<TalentAvailability>(
          options: TalentAvailability.values,
          labelOf: (v) => v.label,
          selected: _availability,
          allLabel: 'Toutes',
          onChanged: (v) => setState(() => _availability = v),
        ),
        const SizedBox(height: 16),
        const Text('Niveau minimum'),
        const SizedBox(height: 8),
        FilterChips<Niveau>(
          options: Niveau.values,
          labelOf: (v) => v.label,
          selected: _level,
          allLabel: 'Tous',
          onChanged: (v) => setState(() => _level = v),
        ),
        const SizedBox(height: 20),
        FilledButton(
          onPressed: () {
            final old = widget.initial;
            Navigator.pop(
              context,
              old.copyWith(
                regionId: _regionId,
                clearRegion: _regionId == null,
                availability: _availability,
                clearAvailability: _availability == null,
                minimumLevel: _level,
                clearMinimumLevel: _level == null,
              ),
            );
          },
          child: const Text('Appliquer'),
        ),
      ],
    ),
  );
}
