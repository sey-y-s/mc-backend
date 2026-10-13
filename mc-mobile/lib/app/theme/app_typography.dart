import 'package:flutter/material.dart';

/// Deux familles seulement, changeables ici (et dans pubspec.yaml) sans toucher aux écrans :
/// - Lora : titres et chiffres (voix « document / passeport »)
/// - Poppins : interface et texte courant
class AppFonts {
  const AppFonts._();
  static const String display = 'Lora';
  static const String body = 'Poppins';
}

class AppTypography {
  const AppTypography._();

  static TextStyle _display(double size, {double height = 1.15}) => TextStyle(
        fontFamily: AppFonts.display,
        fontSize: size,
        height: height,
        fontWeight: FontWeight.w600,
        fontVariations: const [FontVariation('wght', 600)],
        letterSpacing: -0.2,
      );

  static TextStyle _body(double size, {FontWeight weight = FontWeight.w400, double height = 1.45}) =>
      TextStyle(fontFamily: AppFonts.body, fontSize: size, fontWeight: weight, height: height);

  static TextTheme textTheme() => TextTheme(
        displaySmall: _display(34),
        headlineMedium: _display(28),
        headlineSmall: _display(24),
        titleLarge: _display(21, height: 1.2),
        titleMedium: _body(16, weight: FontWeight.w500, height: 1.3),
        titleSmall: _body(14, weight: FontWeight.w500, height: 1.3),
        bodyLarge: _body(16, height: 1.5),
        bodyMedium: _body(14, height: 1.5),
        bodySmall: _body(12.5, height: 1.4),
        labelLarge: _body(14, weight: FontWeight.w500),
        labelMedium: _body(12, weight: FontWeight.w500),
        labelSmall: _body(11, weight: FontWeight.w500),
      );
}
