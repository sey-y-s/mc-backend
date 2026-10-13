import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/features/passport/data/api_cv_repository.dart';

class CvScreen extends ConsumerStatefulWidget {
  const CvScreen({super.key});

  @override
  ConsumerState<CvScreen> createState() => _CvScreenState();
}

class _CvScreenState extends ConsumerState<CvScreen> {
  bool _isDownloading = false;

  Future<void> _download() async {
    setState(() => _isDownloading = true);
    try {
      if (AppConfig.useMocks) {
        throw const FeatureUnavailableFailure(
          'Connectez-vous au backend pour générer votre CV.',
        );
      }
      final bytes = await ref.read(cvRepositoryProvider).download();
      final path = await FilePicker.saveFile(
        dialogTitle: 'Enregistrer mon CV',
        fileName: 'mon-cv.pdf',
        type: FileType.custom,
        allowedExtensions: const ['pdf'],
        bytes: bytes,
      );
      if (path != null && mounted) {
        showSuccess(context, 'Votre CV a été téléchargé.');
      }
    } catch (error) {
      if (mounted) showError(context, failureMessage(error));
    } finally {
      if (mounted) setState(() => _isDownloading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: const Text('Mon CV')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Icon(
            Icons.description_outlined,
            size: 56,
            color: theme.colorScheme.primary,
          ),
          const SizedBox(height: 20),
          Text(
            'Un CV à jour, prêt à partager',
            style: theme.textTheme.headlineSmall,
          ),
          const SizedBox(height: 12),
          Text(
            'Votre CV est généré à partir des informations de votre passeport : '
            'photo de profil, coordonnées, compétences validées, expériences '
            'et résultats de tests numériques.',
            style: theme.textTheme.bodyLarge,
          ),
          const SizedBox(height: 24),
          AppButton(
            label: 'Générer et télécharger le CV',
            icon: Icons.download_outlined,
            isLoading: _isDownloading,
            onPressed: _download,
          ),
          const SizedBox(height: 12),
          Text(
            'Le fichier sera enregistré au format PDF sur votre appareil.',
            style: theme.textTheme.bodySmall,
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}
