import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';

class LoadingView extends StatelessWidget {
  const LoadingView({super.key});
  @override
  Widget build(BuildContext context) => const Center(child: CircularProgressIndicator());
}

class EmptyView extends StatelessWidget {
  const EmptyView({super.key, required this.message, this.icon = AppIcons.empty, this.actionLabel, this.onAction});
  final String message;
  final IconData icon;
  final String? actionLabel;
  final VoidCallback? onAction;

  @override
  Widget build(BuildContext context) => _Centered(
        icon: icon,
        color: AppColors.green,
        background: AppColors.greenSoft,
        message: message,
        actionLabel: actionLabel,
        onAction: onAction,
      );
}

class ErrorView extends StatelessWidget {
  const ErrorView({super.key, required this.error, this.onRetry});
  final Object error;
  final VoidCallback? onRetry;

  @override
  Widget build(BuildContext context) {
    final icon = switch (error) {
      NetworkFailure() => AppIcons.offline,
      ForbiddenFailure() => AppIcons.forbidden,
      NotFoundFailure() => AppIcons.notFound,
      _ => AppIcons.error,
    };
    return _Centered(
      icon: icon,
      color: AppColors.error,
      background: AppColors.errorSoft,
      message: failureMessage(error),
      actionLabel: onRetry == null ? null : 'Réessayer',
      onAction: onRetry,
    );
  }
}

class _Centered extends StatelessWidget {
  const _Centered({required this.icon, required this.color, required this.background, required this.message, this.actionLabel, this.onAction});
  final IconData icon;
  final Color color;
  final Color background;
  final String message;
  final String? actionLabel;
  final VoidCallback? onAction;

  @override
  Widget build(BuildContext context) => Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(mainAxisSize: MainAxisSize.min, children: [
            Container(
              width: 72,
              height: 72,
              decoration: BoxDecoration(color: background, shape: BoxShape.circle),
              child: Icon(icon, size: 32, color: color),
            ),
            const SizedBox(height: 16),
            Text(message, textAlign: TextAlign.center, style: Theme.of(context).textTheme.bodyLarge),
            if (onAction != null) ...[
              const SizedBox(height: 16),
              SizedBox(width: 200, child: AppButton(label: actionLabel!, onPressed: onAction, variant: AppButtonVariant.secondary)),
            ],
          ]),
        ),
      );
}

/// Gère loading / error / empty / data d'un AsyncValue en un seul widget.
class AsyncValueView<T> extends StatelessWidget {
  const AsyncValueView({
    super.key,
    required this.value,
    required this.data,
    required this.onRetry,
    this.isEmpty,
    this.emptyMessage = 'Rien à afficher pour le moment.',
  });

  final AsyncValue<T> value;
  final Widget Function(T data) data;
  final VoidCallback onRetry;
  final bool Function(T data)? isEmpty;
  final String emptyMessage;

  @override
  Widget build(BuildContext context) => value.when(
        loading: () => const LoadingView(),
        error: (e, _) => ErrorView(error: e, onRetry: onRetry),
        data: (d) => (isEmpty?.call(d) ?? false) ? EmptyView(message: emptyMessage) : data(d),
      );
}
