import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_dialogs.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/portfolio/domain/portfolio_models.dart';
import 'package:mlc_mobile/features/portfolio/presentation/portfolio_media_preview.dart';
import 'package:mlc_mobile/features/portfolio/presentation/portfolio_providers.dart';

class PortfolioDetailScreen extends ConsumerWidget {
  const PortfolioDetailScreen({super.key, this.id});
  final String? id;
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final itemId = id;
    if (itemId == null || itemId.isEmpty) {
      return const Scaffold(body: ErrorView(error: NotFoundFailure()));
    }
    final value = ref.watch(portfolioDetailProvider(itemId));
    return Scaffold(
      appBar: AppBar(title: const Text('Réalisation')),
      body: AsyncValueView<PortfolioRealisation>(
        value: value,
        onRetry: () => ref.invalidate(portfolioDetailProvider(itemId)),
        data: (item) => ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Text(item.title, style: Theme.of(context).textTheme.headlineSmall),
            if (item.date != null)
              Padding(
                padding: const EdgeInsets.only(top: 6),
                child: Text(
                  formatDateFr(item.date!),
                  style: const TextStyle(color: AppColors.muted),
                ),
              ),
            const SizedBox(height: 16),
            AppCard(
              child: Text(
                item.description.isEmpty
                    ? 'Aucune description.'
                    : item.description,
                style: const TextStyle(height: 1.5),
              ),
            ),
            if (item.linkUrl != null && item.linkUrl!.isNotEmpty)
              ListTile(
                leading: const Icon(AppIcons.copy),
                title: Text(item.linkUrl!),
              ),
            if (item.media.isNotEmpty) ...[
              const SizedBox(height: 16),
              Text('Médias', style: Theme.of(context).textTheme.titleMedium),
              const SizedBox(height: 8),
              for (final media in item.media)
                Padding(
                  padding: const EdgeInsets.only(bottom: 12),
                  child: AppCard(
                    child: Column(
                      children: [
                        if (media.type == PortfolioMediaType.image)
                          ClipRRect(
                            borderRadius: BorderRadius.circular(12),
                            child: PortfolioMediaPreview(
                              url: media.url,
                              localBytes: media.localBytes,
                              height: 210,
                              width: double.infinity,
                              fit: BoxFit.cover,
                              cacheWidth: 720,
                            ),
                          )
                        else
                          ListTile(
                            leading: Icon(
                              media.type == PortfolioMediaType.video
                                  ? AppIcons.video
                                  : AppIcons.document,
                            ),
                            title: Text(media.fileName ?? media.type.label),
                          ),
                        if (media.caption != null) Text(media.caption!),
                      ],
                    ),
                  ),
                ),
            ] else
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 16),
                child: Text(
                  'Aucun média joint.',
                  style: TextStyle(color: AppColors.muted),
                ),
              ),
            const SizedBox(height: 16),
            AppButton(
              label: 'Modifier',
              icon: AppIcons.edit,
              onPressed: () =>
                  context.push('/portfolio/${item.id}/modifier'),
            ),
            const SizedBox(height: 8),
            AppButton(
              label: 'Supprimer cette réalisation',
              icon: AppIcons.delete,
              variant: AppButtonVariant.secondary,
              onPressed: () async {
                final confirmed = await confirmDialog(
                  context,
                  title: 'Supprimer cette réalisation ?',
                  message: 'Cette action supprime la réalisation de votre portfolio.',
                  confirmLabel: 'Supprimer',
                  destructive: true,
                );
                if (!confirmed || !context.mounted) return;
                try {
                  await ref.read(portfolioRepositoryProvider).delete(item.id);
                  ref.read(portfolioRevisionProvider.notifier).state++;
                  if (context.mounted) context.go('/portfolio');
                } catch (e) {
                  if (context.mounted) {
                    ScaffoldMessenger.of(
                      context,
                    ).showSnackBar(SnackBar(content: Text(failureMessage(e))));
                  }
                }
              },
            ),
          ],
        ),
      ),
    );
  }
}
