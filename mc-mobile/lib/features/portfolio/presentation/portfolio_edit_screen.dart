import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/portfolio/domain/portfolio_models.dart';
import 'package:mlc_mobile/features/portfolio/presentation/portfolio_form_screen.dart';
import 'package:mlc_mobile/features/portfolio/presentation/portfolio_providers.dart';

class PortfolioEditScreen extends ConsumerWidget {
  const PortfolioEditScreen({super.key, this.id});
  final String? id;
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final itemId = id;
    if (itemId == null || itemId.isEmpty) {
      return const Scaffold(body: ErrorView(error: NotFoundFailure()));
    }
    final item = ref.watch(portfolioDetailProvider(itemId));
    return AsyncValueView<PortfolioRealisation>(
      value: item,
      onRetry: () => ref.invalidate(portfolioDetailProvider(itemId)),
      data: (value) => PortfolioFormScreen(initial: value),
    );
  }
}
