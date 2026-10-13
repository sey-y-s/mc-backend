import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';

class AppLogo extends StatelessWidget {
  const AppLogo({super.key, this.size = 80});
  final double size;
  @override
  Widget build(BuildContext context) => SvgPicture.asset(
        'assets/svg/logo.svg',
        width: size,
        height: size,
        semanticsLabel: 'Logo MaliCompétences',
      );
}
