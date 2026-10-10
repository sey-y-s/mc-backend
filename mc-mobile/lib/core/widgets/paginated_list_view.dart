import 'package:flutter/material.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';

/// Liste paginée générique (low-data : une page à la fois, chargement au scroll).
/// Gère : chargement initial, vide, erreur (+ réessayer), chargement de la page suivante,
/// erreur sur page suivante, pull-to-refresh.
///
/// Pour relancer la liste quand des filtres changent, donner une nouvelle `key`
/// (ex. `ValueKey(filters)`) : l'état est alors réinitialisé.
class PaginatedListView<T> extends StatefulWidget {
  const PaginatedListView({
    super.key,
    required this.fetchPage,
    required this.itemBuilder,
    this.pageSize = 20,
    this.emptyMessage = 'Rien à afficher pour le moment.',
    this.padding = const EdgeInsets.all(16),
  });

  /// Retourne les éléments de la page demandée (0-based). Moins de [pageSize] éléments = dernière page.
  final Future<List<T>> Function(int page, int size) fetchPage;
  final Widget Function(BuildContext context, T item) itemBuilder;
  final int pageSize;
  final String emptyMessage;
  final EdgeInsetsGeometry padding;

  @override
  State<PaginatedListView<T>> createState() => _PaginatedListViewState<T>();
}

class _PaginatedListViewState<T> extends State<PaginatedListView<T>> {
  final _items = <T>[];
  final _controller = ScrollController();
  int _nextPage = 0;
  bool _loading = false;
  bool _hasMore = true;
  bool _started = false;
  Object? _error;

  @override
  void initState() {
    super.initState();
    _controller.addListener(_onScroll);
    _load();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_controller.hasClients && _controller.position.extentAfter < 300 && _hasMore && !_loading && _error == null) {
      _load();
    }
  }

  Future<void> _load({bool reset = false}) async {
    if (_loading) return;
    final page = reset ? 0 : _nextPage;
    setState(() {
      _loading = true;
      _started = true;
      _error = null;
    });
    try {
      final data = await widget.fetchPage(page, widget.pageSize);
      if (!mounted) return;
      setState(() {
        if (reset) _items.clear();
        _items.addAll(data);
        _hasMore = data.length >= widget.pageSize;
        _nextPage = page + 1;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _error = e;
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_items.isEmpty) {
      if (_loading || !_started) return const LoadingView();
      if (_error != null) return ErrorView(error: _error!, onRetry: () => _load(reset: true));
      return EmptyView(message: widget.emptyMessage);
    }
    final hasFooter = _loading || _error != null;
    return RefreshIndicator(
      onRefresh: () => _load(reset: true),
      child: ListView.separated(
        controller: _controller,
        padding: widget.padding,
        physics: const AlwaysScrollableScrollPhysics(),
        itemCount: _items.length + (hasFooter ? 1 : 0),
        separatorBuilder: (_, _) => const SizedBox(height: 12),
        itemBuilder: (context, i) {
          if (i < _items.length) return widget.itemBuilder(context, _items[i]);
          if (_error != null) {
            return Column(children: [
              Text(failureMessage(_error!), textAlign: TextAlign.center),
              TextButton(onPressed: _load, child: const Text('Réessayer')),
            ]);
          }
          return const Padding(padding: EdgeInsets.all(8), child: Center(child: CircularProgressIndicator()));
        },
      ),
    );
  }
}
