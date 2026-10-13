import 'package:flutter/material.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';

/// Écran provisoire des fonctionnalités à implémenter. Le TODO est dans le fichier de l'écran.
class PlaceholderScreen extends StatelessWidget {
  const PlaceholderScreen({super.key, required this.title});
  final String title;

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: Text(title)),
        body: const Center(
          child: Padding(
            padding: EdgeInsets.all(24),
            child: Column(mainAxisSize: MainAxisSize.min, children: [
              Icon(AppIcons.construction, size: 48),
              SizedBox(height: 12),
              Text('Écran à implémenter.\nVoir le TODO dans le fichier source.',
                  textAlign: TextAlign.center),
            ]),
          ),
        ),
      );
}
