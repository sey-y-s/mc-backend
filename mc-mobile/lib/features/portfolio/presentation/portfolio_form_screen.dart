import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/core/utils/validators.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/app_text_field.dart';
import 'package:mlc_mobile/core/widgets/file_picker_field.dart';
import 'package:mlc_mobile/core/widgets/form_scaffold.dart';
import 'package:mlc_mobile/features/portfolio/domain/portfolio_models.dart';
import 'package:mlc_mobile/features/portfolio/presentation/portfolio_providers.dart';

class PortfolioFormScreen extends ConsumerStatefulWidget {
  const PortfolioFormScreen({super.key, this.initial});
  final PortfolioRealisation? initial;
  @override
  ConsumerState<PortfolioFormScreen> createState() =>
      _PortfolioFormScreenState();
}

class _PortfolioFormScreenState extends ConsumerState<PortfolioFormScreen> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _title, _description, _url;
  DateTime? _date;
  final List<PickedMedia?> _files = List<PickedMedia?>.filled(3, null);
  final Map<int, double> _progress = {};
  final Set<String> _removeIds = {};
  final Set<int> _uploadedFiles = {};
  String? _createdId;
  bool _saving = false;
  Object? _error;
  @override
  void initState() {
    super.initState();
    final item = widget.initial;
    _title = TextEditingController(text: item?.title ?? '');
    _description = TextEditingController(text: item?.description ?? '');
    _url = TextEditingController(text: item?.linkUrl ?? '');
    _date = item?.date;
  }

  @override
  void dispose() {
    _title.dispose();
    _description.dispose();
    _url.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => FormScaffold(
    title: widget.initial == null
        ? 'Nouvelle réalisation'
        : 'Modifier la réalisation',
    formKey: _formKey,
    submitLabel: widget.initial == null
        ? 'Enregistrer'
        : 'Enregistrer les modifications',
    isLoading: _saving,
    error: _error,
    onSubmit: _save,
    children: [
      AppTextField(
        label: 'Titre',
        controller: _title,
        validator: (v) => Validators.required(v, field: 'Le titre'),
      ),
      AppTextField(label: 'Description', controller: _description, maxLines: 4),
      ListTile(
        contentPadding: EdgeInsets.zero,
        leading: const Icon(AppIcons.calendar),
        title: Text(
          _date == null
              ? 'Ajouter une date de réalisation (facultatif)'
              : _date!.toLocal().toString().split(' ').first,
        ),
        trailing: _date == null
            ? const Icon(AppIcons.chevron)
            : IconButton(
                icon: const Icon(AppIcons.close),
                onPressed: () => setState(() => _date = null),
              ),
        onTap: () async {
          final picked = await showDatePicker(
            context: context,
            initialDate: _date ?? DateTime.now(),
            firstDate: DateTime(1950),
            lastDate: DateTime.now(),
          );
          if (picked != null) setState(() => _date = picked);
        },
      ),
      AppTextField(
        label: 'Lien externe (facultatif)',
        controller: _url,
        validator: _validUrl,
        keyboardType: TextInputType.url,
      ),
      if (widget.initial != null && widget.initial!.media.isNotEmpty) ...[
        const Text(
          'Médias existants',
          style: TextStyle(fontWeight: FontWeight.w600),
        ),
        for (final media in widget.initial!.media)
          CheckboxListTile(
            value: !_removeIds.contains(media.id),
            title: Text(media.fileName ?? media.type.label),
            subtitle: const Text('Décocher pour retirer ce média'),
            controlAffinity: ListTileControlAffinity.leading,
            onChanged: (keep) => setState(
              () => keep == true
                  ? _removeIds.remove(media.id)
                  : _removeIds.add(media.id),
            ),
          ),
      ],
      const Text(
        'Ajouter des photos, vidéos ou documents (facultatif).',
        style: TextStyle(fontWeight: FontWeight.w600),
      ),
      for (var i = 0; i < _files.length; i++)
        FilePickerField(
          label: 'Ajouter un média',
          value: _files[i],
          allowed: const {MediaKind.image, MediaKind.video, MediaKind.document},
          uploadProgress: _progress[i],
          enabled: !_saving,
          onChanged: (file) => setState(() {
            _files[i] = file;
            _uploadedFiles.remove(i);
            _progress.remove(i);
          }),
        ),
    ],
  );

  String? _validUrl(String? value) {
    if (value == null || value.trim().isEmpty) return null;
    final uri = Uri.tryParse(value.trim());
    return uri != null &&
            (uri.scheme == 'https' || uri.scheme == 'http') &&
            uri.host.isNotEmpty
        ? null
        : 'Utilisez un lien HTTP ou HTTPS valide.';
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      final repo = ref.read(portfolioRepositoryProvider);
      final input = PortfolioInput(
        title: _title.text,
        description: _description.text,
        date: _date,
        linkUrl: _url.text.trim().isEmpty ? null : _url.text.trim(),
      );
      final existingId = widget.initial?.id ?? _createdId;
      final saved = existingId == null
          ? await repo.create(input)
          : await repo.update(existingId, input);
      _createdId = saved.id;

      for (final mediaId in _removeIds.toList()) {
        await repo.removeMedia(saved.id, mediaId);
        _removeIds.remove(mediaId);
      }
      for (var i = 0; i < _files.length; i++) {
        final file = _files[i];
        if (file == null || _uploadedFiles.contains(i)) continue;
        setState(() => _progress[i] = 0);
        await repo.addMedia(
          saved.id,
          file,
          onProgress: (value) {
            if (mounted) setState(() => _progress[i] = value);
          },
        );
        _uploadedFiles.add(i);
        if (mounted) setState(() => _progress[i] = 1);
      }
      ref.read(portfolioRevisionProvider.notifier).state++;
      if (mounted) context.go('/portfolio/${saved.id}');
    } catch (e) {
      if (mounted) {
        setState(() => _error = e is AppFailure ? e : const UnknownFailure());
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }
}
