import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';

/// En-tête de profil. Mode [anonymized] : aucune photo ni identité (recherche de talents).
class ProfileHeader extends StatelessWidget {
  const ProfileHeader(
      {super.key,
      required this.title,
      this.subtitle,
      this.codePasseport,
      this.avatarUrl,
      this.anonymized = false});
  final String title;
  final String? subtitle;
  final String? codePasseport;
  final String? avatarUrl;
  final bool anonymized;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final showPhoto = !anonymized && avatarUrl != null;
    return Row(children: [
      Container(
        padding: const EdgeInsets.all(3),
        decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: AppColors.gold, width: 2)),
        child: CircleAvatar(
          radius: 30,
          backgroundColor: AppColors.greenSoft,
          foregroundImage: showPhoto ? NetworkImage(avatarUrl!) : null,
          onForegroundImageError: showPhoto ? (_, _) {} : null,
          child: const Icon(AppIcons.person, color: AppColors.green, size: 30),
        ),
      ),
      const SizedBox(width: 16),
      Expanded(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(anonymized ? 'Profil anonymisé' : title,
              style: text.headlineSmall),
          if (subtitle != null)
            Text(subtitle!,
                style: text.bodyMedium?.copyWith(color: AppColors.muted)),
          if (!anonymized && codePasseport != null)
            Row(children: [
              Text('Code : $codePasseport',
                  style: text.labelMedium?.copyWith(color: AppColors.goldDeep)),
              IconButton(
                tooltip: 'Copier le code',
                icon: const Icon(AppIcons.copy, size: 18),
                onPressed: () async {
                  await Clipboard.setData(ClipboardData(text: codePasseport!));
                  if (context.mounted) showSuccess(context, 'Code copié');
                },
              ),
            ]),
        ]),
      ),
    ]);
  }
}
