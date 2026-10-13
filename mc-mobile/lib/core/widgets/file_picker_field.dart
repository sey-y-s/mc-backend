import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/app/theme/app_theme.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/media/file_picker_service.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/core/widgets/app_dialogs.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/app_progress_bar.dart';

/// Champ de sélection de fichier (preuves, portfolio...) :
/// choix de la source, contrôle de taille, aperçu, suppression, progression d'envoi.
///
/// Le champ NE fait PAS l'envoi : l'écran garde le [PickedMedia] choisi, l'envoie via
/// `uploadMultipart(... onProgress: ...)` et repasse la progression dans [uploadProgress].
/// Types autorisés et tailles : [allowed] et [limits]. Exemple d'usage : docs/COMPOSANTS.md.
class FilePickerField extends ConsumerStatefulWidget {
  const FilePickerField({
    super.key,
    required this.value,
    required this.onChanged,
    this.label = 'Ajouter un fichier',
    this.allowed = const {MediaKind.image, MediaKind.document},
    this.limits = const FileLimits(),
    this.uploadProgress,
    this.errorText,
    this.enabled = true,
  });

  final PickedMedia? value;
  final ValueChanged<PickedMedia?> onChanged;
  final String label;
  final Set<MediaKind> allowed;
  final FileLimits limits;

  /// null = pas d'envoi en cours ; 0..1 = envoi en cours (champ verrouillé).
  final double? uploadProgress;

  /// Erreur de validation fournie par l'écran (ex. « Le fichier est obligatoire »).
  final String? errorText;
  final bool enabled;

  @override
  ConsumerState<FilePickerField> createState() => _FilePickerFieldState();
}

class _FilePickerFieldState extends ConsumerState<FilePickerField> {
  String? _error;
  bool _picking = false;

  bool get _uploading =>
      widget.uploadProgress != null && widget.uploadProgress! < 1;
  bool get _locked => !widget.enabled || _uploading || _picking;

  Future<void> _open() async {
    if (_locked) return;
    final source = await showAppBottomSheet<PickSource>(
      context,
      builder: (ctx) => _SourceSheet(allowed: widget.allowed),
    );
    if (source == null) return;
    setState(() => _picking = true);
    try {
      final media = await ref.read(filePickerServiceProvider).pick(source);
      if (media == null) return; // annulé
      final max = widget.limits.maxBytesFor(media.kind);
      if (media.sizeBytes > max) {
        setState(
          () => _error =
              'Fichier trop lourd (${media.sizeLabel}). Maximum : ${formatBytes(max)}.',
        );
        return;
      }
      setState(() => _error = null);
      widget.onChanged(media);
    } on AppFailure catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Impossible d’ouvrir le fichier. Réessayez.');
    } finally {
      if (mounted) setState(() => _picking = false);
    }
  }

  String get _hint {
    final names = [
      if (widget.allowed.contains(MediaKind.image)) 'photo',
      if (widget.allowed.contains(MediaKind.video)) 'vidéo',
      if (widget.allowed.contains(MediaKind.document)) 'document',
    ];
    final joined = names.length <= 1
        ? names.join()
        : '${names.sublist(0, names.length - 1).join(', ')} ou ${names.last}';
    return joined.isEmpty
        ? ''
        : '${joined[0].toUpperCase()}${joined.substring(1)}';
  }

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final error = widget.errorText ?? _error;
    final media = widget.value;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Semantics(
          button: true,
          label: media == null
              ? widget.label
              : 'Changer le fichier ${media.name}',
          child: InkWell(
            onTap: _locked ? null : _open,
            borderRadius: BorderRadius.circular(AppRadius.field),
            child: Container(
              constraints: const BoxConstraints(minHeight: 88),
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: AppColors.card,
                borderRadius: BorderRadius.circular(AppRadius.field),
                border: Border.all(
                  color: error != null ? AppColors.error : AppColors.border,
                  width: error != null ? 1.6 : 1,
                ),
              ),
              child: media == null ? _empty(text) : _selected(text, media),
            ),
          ),
        ),
        if (widget.uploadProgress != null) ...[
          const SizedBox(height: 12),
          AppProgressBar(
            value: widget.uploadProgress!,
            label: _uploading ? 'Envoi en cours' : 'Envoyé',
          ),
        ],
        if (error != null) ...[
          const SizedBox(height: 8),
          Text(error, style: text.bodySmall?.copyWith(color: AppColors.error)),
        ],
      ],
    );
  }

  Widget _empty(TextTheme text) => Row(
    children: [
      _IconBox(icon: _picking ? null : AppIcons.upload),
      const SizedBox(width: 14),
      Expanded(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(widget.label, style: text.titleSmall),
            if (_hint.isNotEmpty)
              Text(
                _hint,
                style: text.bodySmall?.copyWith(color: AppColors.muted),
              ),
          ],
        ),
      ),
    ],
  );

  Widget _selected(TextTheme text, PickedMedia media) => Row(
    children: [
      _Thumbnail(media: media),
      const SizedBox(width: 14),
      Expanded(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              media.name,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: text.titleSmall,
            ),
            Text(
              media.sizeLabel,
              style: text.bodySmall?.copyWith(color: AppColors.muted),
            ),
          ],
        ),
      ),
      IconButton(
        tooltip: 'Retirer le fichier',
        icon: const Icon(AppIcons.close),
        onPressed: _locked
            ? null
            : () {
                setState(() => _error = null);
                widget.onChanged(null);
              },
      ),
    ],
  );
}

class _IconBox extends StatelessWidget {
  const _IconBox({required this.icon});
  final IconData? icon;

  @override
  Widget build(BuildContext context) => Container(
    width: 52,
    height: 52,
    decoration: BoxDecoration(
      color: AppColors.greenSoft,
      borderRadius: BorderRadius.circular(16),
    ),
    child: icon == null
        ? const Padding(
            padding: EdgeInsets.all(14),
            child: CircularProgressIndicator(strokeWidth: 2.5),
          )
        : Icon(icon, color: AppColors.green),
  );
}

class _Thumbnail extends StatelessWidget {
  const _Thumbnail({required this.media});
  final PickedMedia media;

  @override
  Widget build(BuildContext context) {
    final fallback = _IconBox(
      icon: switch (media.kind) {
        MediaKind.image => AppIcons.gallery,
        MediaKind.video => AppIcons.video,
        MediaKind.document => AppIcons.document,
      },
    );
    final bytes = media.bytes;
    if (media.kind != MediaKind.image || bytes == null) return fallback;
    return ClipRRect(
      borderRadius: BorderRadius.circular(16),
      child: Image.memory(
        bytes,
        width: 52,
        height: 52,
        fit: BoxFit.cover,
        cacheWidth: 160,
        errorBuilder: (_, _, _) => fallback,
      ),
    );
  }
}

class _SourceSheet extends StatelessWidget {
  const _SourceSheet({required this.allowed});
  final Set<MediaKind> allowed;

  @override
  Widget build(BuildContext context) {
    Widget option(IconData icon, String label, PickSource source) => ListTile(
      leading: Icon(icon, color: AppColors.green),
      title: Text(label),
      onTap: () => Navigator.of(context).pop(source),
    );
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        if (allowed.contains(MediaKind.image)) ...[
          option(AppIcons.camera, 'Prendre une photo', PickSource.camera),
          option(AppIcons.gallery, 'Choisir une photo', PickSource.gallery),
        ],
        if (allowed.contains(MediaKind.video))
          option(AppIcons.video, 'Choisir une vidéo', PickSource.video),
        if (allowed.contains(MediaKind.document))
          option(
            AppIcons.document,
            'Choisir un document (PDF, Word)',
            PickSource.document,
          ),
      ],
    );
  }
}
