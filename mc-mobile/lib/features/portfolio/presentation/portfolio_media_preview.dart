import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';

/// Aperçu d'une image locale mockée, d'une URL publique ou d'un fichier API authentifié.
class PortfolioMediaPreview extends ConsumerStatefulWidget {
  const PortfolioMediaPreview({
    super.key,
    required this.url,
    this.localBytes,
    this.width,
    this.height,
    this.fit = BoxFit.cover,
    this.cacheWidth,
  });

  final String url;
  final Uint8List? localBytes;
  final double? width;
  final double? height;
  final BoxFit fit;
  final int? cacheWidth;

  @override
  ConsumerState<PortfolioMediaPreview> createState() =>
      _PortfolioMediaPreviewState();
}

class _PortfolioMediaPreviewState extends ConsumerState<PortfolioMediaPreview> {
  Future<Uint8List?>? _apiBytes;

  @override
  void initState() {
    super.initState();
    _loadApiBytes();
  }

  @override
  void didUpdateWidget(covariant PortfolioMediaPreview oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.url != widget.url) _loadApiBytes();
  }

  String? _apiPath(String value) {
    final uri = Uri.tryParse(value.trim());
    if (uri == null) return null;
    final base = Uri.tryParse(AppConfig.apiBaseUrl);
    if (uri.hasScheme) {
      if ((uri.scheme == 'http' || uri.scheme == 'https') &&
          base != null &&
          uri.origin == base.origin) {
        return uri.toString();
      }
      return null;
    }
    return uri.path.startsWith('/api/v1/portfolio-medias/')
        ? uri.toString()
        : null;
  }

  bool _isExternalHttpUrl(String value) {
    final uri = Uri.tryParse(value.trim());
    if (uri == null || (uri.scheme != 'http' && uri.scheme != 'https')) {
      return false;
    }
    return _apiPath(value) == null;
  }

  void _loadApiBytes() {
    final path = _apiPath(widget.url);
    _apiBytes = path == null ? null : _readBytes(path);
  }

  Future<Uint8List?> _readBytes(String path) async {
    try {
      final response = await ref
          .read(dioProvider)
          .get<List<int>>(
            path,
            options: Options(responseType: ResponseType.bytes),
          );
      final data = response.data;
      return data == null ? null : Uint8List.fromList(data);
    } catch (_) {
      return null;
    }
  }

  Widget _placeholder() => SizedBox(
    width: widget.width,
    height: widget.height,
    child: Container(
      color: AppColors.greenSoft,
      alignment: Alignment.center,
      child: const Icon(AppIcons.portfolio, color: AppColors.green),
    ),
  );

  Widget _image(Uint8List bytes) => Image.memory(
    bytes,
    width: widget.width,
    height: widget.height,
    fit: widget.fit,
    cacheWidth: widget.cacheWidth,
    errorBuilder: (_, _, _) => _placeholder(),
  );

  @override
  Widget build(BuildContext context) {
    final localBytes = widget.localBytes;
    if (localBytes != null) return _image(localBytes);

    final path = _apiPath(widget.url);
    if (path != null) {
      return FutureBuilder<Uint8List?>(
        future: _apiBytes,
        builder: (context, snapshot) {
          if (snapshot.hasData) return _image(snapshot.data!);
          if (snapshot.connectionState == ConnectionState.waiting) {
            return SizedBox(
              width: widget.width,
              height: widget.height,
              child: const Center(
                child: CircularProgressIndicator(strokeWidth: 2),
              ),
            );
          }
          return _placeholder();
        },
      );
    }

    if (_isExternalHttpUrl(widget.url)) {
      return Image.network(
        widget.url,
        width: widget.width,
        height: widget.height,
        fit: widget.fit,
        cacheWidth: widget.cacheWidth,
        errorBuilder: (_, _, _) => _placeholder(),
      );
    }
    return _placeholder();
  }
}
