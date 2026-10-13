import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';

/// Envoi multipart avec progression (0..1). À entourer de guardDio() par le repository.
Future<Response<T>> uploadMultipart<T>(
  Dio dio,
  String path, {
  required PickedMedia file,
  String fileField = 'fichier',
  Map<String, dynamic> fields = const {},
  void Function(double progress)? onProgress,
  CancelToken? cancelToken,
}) async {
  final form = FormData.fromMap({
    ...fields,
    fileField: await _multipartFile(file),
  });
  return dio.post<T>(
    path,
    data: form,
    cancelToken: cancelToken,
    onSendProgress: (sent, total) {
      if (total > 0) onProgress?.call(sent / total);
    },
  );
}

Future<MultipartFile> _multipartFile(PickedMedia file) {
  final bytes = file.bytes;
  if (bytes != null) {
    return Future.value(MultipartFile.fromBytes(bytes, filename: file.name));
  }
  if (kIsWeb) {
    throw const FileFailure(
      'Le fichier ne peut pas être lu dans le navigateur. Sélectionnez-le à nouveau.',
    );
  }
  if (file.path.trim().isEmpty) {
    throw const FileFailure('Le chemin du fichier est indisponible.');
  }
  return MultipartFile.fromFile(file.path, filename: file.name);
}

/// Pour les repositories Mock : simule un envoi avec progression.
Future<void> simulateUpload({
  void Function(double progress)? onProgress,
  int steps = 5,
  Duration stepDelay = const Duration(milliseconds: 150),
}) async {
  for (var i = 1; i <= steps; i++) {
    await Future<void>.delayed(stepDelay);
    onProgress?.call(i / steps);
  }
}
