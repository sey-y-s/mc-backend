import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_logo.dart';
import 'package:mlc_mobile/core/widgets/bogolan_pattern.dart';
import 'package:mlc_mobile/l10n/strings.dart';

/// Affiché pendant la lecture du jeton. Le routeur redirige dès que la session est connue.
class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context) => AnnotatedRegion<SystemUiOverlayStyle>(
        value: SystemUiOverlayStyle.light,
        child: Scaffold(
          backgroundColor: AppColors.green,
          body: Stack(children: [
            const Positioned.fill(child: BogolanPattern(color: Color(0x2EC09427))),
            Center(
              child: Column(mainAxisSize: MainAxisSize.min, children: [
                const AppLogo(size: 88),
                const SizedBox(height: 20),
                Text(S.appName, style: Theme.of(context).textTheme.headlineMedium?.copyWith(color: Colors.white)),
                const SizedBox(height: 28),
                const SizedBox(
                  width: 26,
                  height: 26,
                  child: CircularProgressIndicator(strokeWidth: 2.5, color: AppColors.gold),
                ),
              ]),
            ),
          ]),
        ),
      );
}
