import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/admin/domain/admin_models.dart';
import 'package:mlc_mobile/features/admin/presentation/admin_providers.dart';
import 'package:mlc_mobile/features/opportunites/domain/opportunite_models.dart';

class AdminOpportunitiesScreen extends ConsumerWidget {
  const AdminOpportunitiesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final opportunities = ref.watch(adminOpportunitiesProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Gestion des opportunités')),
      body: opportunities.when(
        loading: () => const LoadingView(),
        error: (error, _) => ErrorView(
          error: error,
          onRetry: () => ref.invalidate(adminOpportunitiesProvider),
        ),
        data: (items) => RefreshIndicator(
          onRefresh: () => ref.refresh(adminOpportunitiesProvider.future),
          child: items.isEmpty
              ? const EmptyView(message: 'Aucune opportunité enregistrée.')
              : ListView.separated(
                  padding: const EdgeInsets.all(16),
                  itemCount: items.length,
                  separatorBuilder: (_, _) => const SizedBox(height: 10),
                  itemBuilder: (context, index) {
                    final opportunity = items[index];
                    return AppCard(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            opportunity.title,
                            style: const TextStyle(fontWeight: FontWeight.w700),
                          ),
                          Text(
                            '${opportunity.type.label} · ${_statusLabel(opportunity.status)}',
                            style: const TextStyle(color: AppColors.muted),
                          ),
                          if (opportunity.description.isNotEmpty) ...[
                            const SizedBox(height: 6),
                            Text(
                              opportunity.description,
                              maxLines: 3,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ],
                        ],
                      ),
                    );
                  },
                ),
        ),
      ),
    );
  }

  static String _statusLabel(OpportuniteStatus status) => switch (status) {
    OpportuniteStatus.brouillon => 'Brouillon',
    OpportuniteStatus.publiee => 'Publiée',
    OpportuniteStatus.expiree => 'Expirée',
    OpportuniteStatus.archivee => 'Archivée',
    OpportuniteStatus.annulee => 'Archivée',
  };
}

class AdminOpportunityEditorScreen extends ConsumerStatefulWidget {
  const AdminOpportunityEditorScreen({super.key, this.opportunity});

  final AdminOpportunityDraft? opportunity;

  @override
  ConsumerState<AdminOpportunityEditorScreen> createState() =>
      _AdminOpportunityEditorScreenState();
}

class _AdminOpportunityEditorScreenState
    extends ConsumerState<AdminOpportunityEditorScreen> {
  final _formKey = GlobalKey<FormState>();
  late final _title = TextEditingController(
    text: widget.opportunity?.title ?? '',
  );
  late final _description = TextEditingController(
    text: widget.opportunity?.description ?? '',
  );
  String? _categoryId;
  OpportuniteType _type = OpportuniteType.formationGratuite;
  OpportuniteStatus _status = OpportuniteStatus.brouillon;
  DateTime? _expirationDate;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _categoryId = widget.opportunity?.categoryId;
    _type = widget.opportunity?.type ?? OpportuniteType.formationGratuite;
    _status = widget.opportunity?.status ?? OpportuniteStatus.brouillon;
    _expirationDate = widget.opportunity?.expirationDate;
  }

  @override
  void dispose() {
    _title.dispose();
    _description.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    if (_categoryId == null || _categoryId!.isEmpty) {
      showError(context, 'Choisissez une catégorie.');
      return;
    }
    setState(() => _saving = true);
    try {
      await ref
          .read(apiAdminRepositoryProvider)
          .saveOpportunity(
            AdminOpportunityDraft(
              id: widget.opportunity?.id,
              categoryId: _categoryId!,
              title: _title.text,
              description: _description.text,
              type: _type,
              status: _status,
              expirationDate: _expirationDate,
            ),
          );
      ref.invalidate(adminOpportunitiesProvider);
      ref.invalidate(adminDashboardProvider);
      if (mounted) {
        showSuccess(context, 'Opportunité enregistrée');
        context.pop();
      }
    } catch (error) {
      if (mounted) showError(context, failureMessage(error));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final categories = ref.watch(adminOpportunityCategoriesProvider);
    return Scaffold(
      appBar: AppBar(
        title: Text(
          widget.opportunity == null
              ? 'Nouvelle opportunité'
              : 'Modifier l’opportunité',
        ),
      ),
      body: categories.when(
        loading: () => const LoadingView(),
        error: (error, _) => ErrorView(
          error: error,
          onRetry: () => ref.invalidate(adminOpportunityCategoriesProvider),
        ),
        data: (items) => Form(
          key: _formKey,
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              AppCard(
                child: Column(
                  children: [
                    DropdownButtonFormField<String>(
                      initialValue: items.any((item) => item.id == _categoryId)
                          ? _categoryId
                          : null,
                      decoration: const InputDecoration(
                        labelText: 'Catégorie',
                        prefixIcon: Icon(Icons.category_outlined),
                      ),
                      items: [
                        for (final item in items)
                          DropdownMenuItem(
                            value: item.id,
                            child: Text(item.name),
                          ),
                      ],
                      onChanged: _saving
                          ? null
                          : (value) => setState(() => _categoryId = value),
                      validator: (value) =>
                          value == null ? 'Choisissez une catégorie.' : null,
                    ),
                    const SizedBox(height: 14),
                    TextFormField(
                      controller: _title,
                      decoration: const InputDecoration(labelText: 'Titre'),
                      textCapitalization: TextCapitalization.sentences,
                      validator: (value) => value == null || value.trim().isEmpty
                          ? 'Le titre est obligatoire.'
                          : null,
                    ),
                    const SizedBox(height: 14),
                    TextFormField(
                      controller: _description,
                      decoration: const InputDecoration(
                        labelText: 'Description',
                        alignLabelWithHint: true,
                      ),
                      minLines: 4,
                      maxLines: 7,
                      validator: (value) => value == null || value.trim().isEmpty
                          ? 'La description est obligatoire.'
                          : null,
                    ),
                    const SizedBox(height: 14),
                    DropdownButtonFormField<OpportuniteType>(
                      initialValue: _type,
                      decoration: const InputDecoration(labelText: 'Type'),
                      items: [
                        for (final type in OpportuniteType.values)
                          DropdownMenuItem(
                            value: type,
                            child: Text(type.label),
                          ),
                      ],
                      onChanged: _saving
                          ? null
                          : (value) {
                              if (value != null) setState(() => _type = value);
                            },
                    ),
                    const SizedBox(height: 14),
                    DropdownButtonFormField<OpportuniteStatus>(
                      initialValue: _status,
                      decoration: const InputDecoration(labelText: 'Statut'),
                      items: const [
                        DropdownMenuItem(
                          value: OpportuniteStatus.brouillon,
                          child: Text('Brouillon'),
                        ),
                        DropdownMenuItem(
                          value: OpportuniteStatus.publiee,
                          child: Text('Publiée'),
                        ),
                        DropdownMenuItem(
                          value: OpportuniteStatus.expiree,
                          child: Text('Expirée'),
                        ),
                        DropdownMenuItem(
                          value: OpportuniteStatus.archivee,
                          child: Text('Archivée'),
                        ),
                      ],
                      onChanged: _saving
                          ? null
                          : (value) {
                              if (value != null) setState(() => _status = value);
                            },
                    ),
                    const SizedBox(height: 14),
                    ListTile(
                      contentPadding: EdgeInsets.zero,
                      title: const Text('Date d’expiration'),
                      subtitle: Text(
                        _expirationDate == null
                            ? 'Aucune date définie'
                            : MaterialLocalizations.of(context).formatMediumDate(
                                _expirationDate!,
                              ),
                      ),
                      trailing: Wrap(
                        children: [
                          if (_expirationDate != null)
                            IconButton(
                              tooltip: 'Effacer la date',
                              onPressed: _saving
                                  ? null
                                  : () => setState(() => _expirationDate = null),
                              icon: const Icon(Icons.close),
                            ),
                          IconButton(
                            tooltip: 'Choisir la date',
                            onPressed: _saving ? null : _chooseExpirationDate,
                            icon: const Icon(Icons.calendar_month_outlined),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              FilledButton(
                onPressed: _saving ? null : _save,
                child: Text(_saving ? 'Enregistrement…' : 'Enregistrer'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _chooseExpirationDate() async {
    final now = DateTime.now();
    final chosen = await showDatePicker(
      context: context,
      initialDate: _expirationDate ?? now,
      firstDate: DateTime(now.year - 1),
      lastDate: DateTime(now.year + 10),
    );
    if (chosen != null && mounted) setState(() => _expirationDate = chosen);
  }
}
