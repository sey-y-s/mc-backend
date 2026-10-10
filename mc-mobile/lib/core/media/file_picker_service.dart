import 'package:file_picker/file_picker.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';

/// Abstraction de la sélection de fichiers : l'écran ne connaît pas les plugins,
/// et les tests injectent un faux service (voir filePickerServiceProvider).
abstract interface class FilePickerService {
  /// Retourne null si l'utilisateur annule. Lève [FileFailure] si l'accès est refusé.
  Future<PickedMedia?> pick(PickSource source);
}

class DevicePickerService implements FilePickerService {
  DevicePickerService({ImagePicker? imagePicker})
    : _images = imagePicker ?? ImagePicker();

  final ImagePicker _images;

  @override
  Future<PickedMedia?> pick(PickSource source) async {
    try {
      switch (source) {
        case PickSource.camera:
          return await _fromXFile(
            await _images.pickImage(
              source: ImageSource.camera,
              maxWidth: 1600,
              maxHeight: 1600,
              imageQuality: 70,
            ),
            MediaKind.image,
          );
        case PickSource.gallery:
          return await _fromXFile(
            await _images.pickImage(
              source: ImageSource.gallery,
              maxWidth: 1600,
              maxHeight: 1600,
              imageQuality: 70,
            ),
            MediaKind.image,
          );
        case PickSource.video:
          return await _fromXFile(
            await _images.pickVideo(
              source: ImageSource.gallery,
              maxDuration: const Duration(minutes: 1),
            ),
            MediaKind.video,
          );
        case PickSource.document:
          final file = await FilePicker.pickFile(
            type: FileType.custom,
            allowedExtensions: const ['pdf', 'doc', 'docx'],
          );
          if (file == null) return null;

          final bytes = kIsWeb || file.path == null
              ? await file.readAsBytes()
              : null;
          final fileSize = (await file.length()) ?? bytes?.length ?? 0;
          return PickedMedia(
            path: file.path ?? '',
            name: file.name,
            sizeBytes: fileSize,
            kind: MediaKind.document,
            bytes: bytes,
          );
      }
    } on PlatformException catch (e) {
      throw FileFailure(_messageFor(e.code));
    } on FileFailure {
      rethrow;
    } catch (_) {
      throw const FileFailure('Impossible d’ouvrir le fichier. Réessayez.');
    }
  }

  Future<PickedMedia?> _fromXFile(XFile? file, MediaKind kind) async {
    if (file == null) return null;
    final size = await file.length();
    // Les images ont besoin de leurs octets pour l'aperçu multiplateforme.
    // Sur le Web, les chemins blob ne sont pas lisibles par MultipartFile.fromFile.
    final bytes = kIsWeb || kind == MediaKind.image
        ? await file.readAsBytes()
        : null;
    return PickedMedia(
      path: file.path,
      name: file.name,
      sizeBytes: size,
      kind: kind,
      bytes: bytes,
    );
  }

  String _messageFor(String code) => code.contains('denied')
      ? "Autorisez l'accès à la caméra ou aux photos dans les réglages du téléphone."
      : "Impossible d'ouvrir le fichier. Réessayez.";
}

final filePickerServiceProvider = Provider<FilePickerService>(
  (ref) => DevicePickerService(),
);
