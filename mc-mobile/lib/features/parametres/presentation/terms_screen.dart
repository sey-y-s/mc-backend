import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';

class TermsScreen extends StatelessWidget {
  const TermsScreen({super.key});
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Conditions d’utilisation')),
    body: ListView(
      padding: const EdgeInsets.all(20),
      children: const [
        AppCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Icon(AppIcons.document, color: AppColors.green, size: 30),
              SizedBox(height: 14),
              Text(
                'Le texte officiel des conditions d’utilisation n’est pas encore fourni par le serveur ou le projet.',
              ),
              SizedBox(height: 8),
              Text(
                'Cette page affichera le document approuvé dès qu’il sera disponible.',
                style: TextStyle(color: AppColors.muted),
              ),
            ],
          ),
        ),
      ],
    ),
  );
}
