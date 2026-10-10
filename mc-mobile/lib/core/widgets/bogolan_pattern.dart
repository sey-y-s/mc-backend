import 'package:flutter/material.dart';

/// Motif inspiré du bogolan (tissu malien) dessiné en code : aucune image à télécharger.
/// À utiliser avec parcimonie : en-têtes (accueil, connexion, splash) uniquement.
class BogolanPattern extends StatelessWidget {
  const BogolanPattern({super.key, required this.color, this.cell = 34});
  final Color color;
  final double cell;

  @override
  Widget build(BuildContext context) => IgnorePointer(
        child: ClipRect(child: CustomPaint(painter: _BogolanPainter(color, cell), size: Size.infinite)),
      );
}

class _BogolanPainter extends CustomPainter {
  _BogolanPainter(this.color, this.cell);
  final Color color;
  final double cell;

  @override
  void paint(Canvas canvas, Size size) {
    final stroke = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.3
      ..strokeCap = StrokeCap.round;
    final fill = Paint()..color = color;
    final rows = (size.height / cell).ceil() + 1;
    final cols = (size.width / cell).ceil() + 1;
    for (var r = 0; r < rows; r++) {
      for (var c = 0; c < cols; c++) {
        // Une rangée sur deux est décalée d'une demi-cellule, comme sur une bande tissée.
        final dx = (r.isOdd ? cell / 2 : 0) + c * cell;
        final center = Offset(dx + cell / 2, r * cell + cell / 2);
        switch ((r * 3 + c) % 4) {
          case 0: // losange
            final p = Path()
              ..moveTo(center.dx, center.dy - 7)
              ..lineTo(center.dx + 7, center.dy)
              ..lineTo(center.dx, center.dy + 7)
              ..lineTo(center.dx - 7, center.dy)
              ..close();
            canvas.drawPath(p, stroke);
          case 1: // deux traits
            canvas.drawLine(center.translate(-7, -3), center.translate(7, -3), stroke);
            canvas.drawLine(center.translate(-7, 3), center.translate(7, 3), stroke);
          case 2: // point
            canvas.drawCircle(center, 2.2, fill);
          default: // croix
            canvas.drawLine(center.translate(-5, 0), center.translate(5, 0), stroke);
            canvas.drawLine(center.translate(0, -5), center.translate(0, 5), stroke);
        }
      }
    }
  }

  @override
  bool shouldRepaint(_BogolanPainter old) => old.color != color || old.cell != cell;
}
