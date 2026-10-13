class Validators {
  const Validators._();

  static String? required(String? v, {String field = 'Ce champ'}) =>
      (v == null || v.trim().isEmpty) ? '$field est obligatoire' : null;

  /// Téléphone malien : 8 chiffres, préfixe +223 facultatif.
  static String? phone(String? v) {
    final r = required(v, field: 'Le téléphone');
    if (r != null) return r;
    final digits = v!.replaceAll(RegExp(r'[\s\-.]'), '');
    return RegExp(r'^(\+223)?\d{8}$').hasMatch(digits) ? null : 'Numéro invalide (8 chiffres, ex. 70 00 00 00)';
  }

  static String? password(String? v) =>
      (v == null || v.length < 8) ? 'Au moins 8 caractères' : null;

  static String? confirmPassword(String? v, String original) =>
      v == original ? null : 'Les mots de passe ne correspondent pas';

  static String? emailOptional(String? v) {
    if (v == null || v.trim().isEmpty) return null;
    return RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$').hasMatch(v.trim()) ? null : 'Email invalide';
  }

  /// Code reçu par SMS ou email
  static String? resetCode(String? v) {
    final r = required(v, field: 'Le code');
    if (r != null) return r;
    return RegExp(r'^\d{4,8}$').hasMatch(v!.trim()) ? null : 'Le code contient uniquement des chiffres';
  }
}
