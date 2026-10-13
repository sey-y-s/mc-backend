import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/paginated_list_view.dart';
import 'package:mlc_mobile/features/portfolio/domain/portfolio_models.dart';
import 'package:mlc_mobile/features/portfolio/presentation/portfolio_media_preview.dart';
import 'package:mlc_mobile/features/portfolio/presentation/portfolio_providers.dart';

class PortfolioListScreen extends ConsumerWidget {
  const PortfolioListScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) => Scaffold(
    appBar: AppBar(
      title: const Text('Mon portfolio'),
      actions: [
        IconButton(
          tooltip: 'Ajouter une réalisation',
          icon: const Icon(AppIcons.add),
          onPressed: () => context.push('/portfolio/nouvelle'),
        ),
      ],
    ),
    body: PaginatedListView<PortfolioRealisation>(
      key: ValueKey(ref.watch(portfolioRevisionProvider)),
      pageSize: 20,
      emptyMessage: 'Votre portfolio est facultatif. Ajoutez une réalisation quand vous souhaitez présenter votre travail.',
      fetchPage: (page, size) =>
          ref.read(portfolioRepositoryProvider).list(page: page, size: size),
      itemBuilder: (context, item) {
        final image = item.media
            .where((e) => e.type == PortfolioMediaType.image)
            .firstOrNull;
        return AppCard(
          onTap: () => context.push('/portfolio/${item.id}'),
          child: Row(
            children: [
              if (image != null)
                ClipRRect(
                  borderRadius: BorderRadius.circular(12),
                  child: PortfolioMediaPreview(
                    url: image.url,
                    localBytes: image.localBytes,
                    width: 68,
                    height: 68,
                    cacheWidth: 160,
                  ),
                )
              else
                _MediaIcon(
                  type: item.media.isEmpty ? null : item.media.first.type,
                ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      item.title,
                      style: const TextStyle(fontWeight: FontWeight.w700),
                    ),
                    if (item.date != null)
                      Text(
                        formatDateFr(item.date!),
                        style: const TextStyle(
                          color: AppColors.muted,
                          fontSize: 12,
                        ),
                      ),
                    Text(
                      item.media.isEmpty
                          ? 'Sans média'
                          : '${item.media.length} média(s)',
                      style: const TextStyle(
                        color: AppColors.muted,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ),
              const Icon(AppIcons.chevron, color: AppColors.muted),
            ],
          ),
        );
      },
    ),
  );
}

class _MediaIcon extends StatelessWidget {
  const _MediaIcon({this.type});
  final PortfolioMediaType? type;
  @override
  Widget build(BuildContext context) => Container(
    width: 68,
    height: 68,
    decoration: BoxDecoration(
      color: AppColors.greenSoft,
      borderRadius: BorderRadius.circular(12),
    ),
    child: Icon(
      type == PortfolioMediaType.video
          ? AppIcons.video
          : type == PortfolioMediaType.document
          ? AppIcons.document
          : AppIcons.portfolio,
      color: AppColors.green,
    ),
  );
}
