import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';

final cvRepositoryProvider = Provider<ApiCvRepository>(
  (ref) => ApiCvRepository(ref.watch(dioProvider)),
);

class ApiCvRepository {
  const ApiCvRepository(this._dio);
  final Dio _dio;

  Future<Uint8List> download() async {
    final response = await guardDio(
      () => _dio.get<List<int>>(
        ApiEndpoints.citoyenCv('me'),
        // The Dio client defaults to Accept: application/json.
        // Explicitly request the PDF representation for this download.
        options: Options(
          responseType: ResponseType.bytes,
          headers: const {'Accept': 'application/pdf'},
        ),
      ),
    );
    final bytes = response.data;
    if (bytes == null || bytes.isEmpty) {
      throw const FileFailure('Le serveur a renvoyé un CV vide.');
    }
    return Uint8List.fromList(bytes);
  }
}
