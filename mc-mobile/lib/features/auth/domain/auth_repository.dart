class RegisterData {
  const RegisterData({
    required this.nom,
    required this.prenom,
    required this.telephone,
    required this.password,
    this.email,
    this.communeId,
  });
  final String nom;
  final String prenom;
  final String telephone;
  final String password;
  final String? email;
  final String? communeId;
}

abstract interface class AuthRepository {
  /// [identifiant] = téléphone ou email. Enregistre les jetons en stockage sécurisé.
  Future<void> login({required String identifiant, required String password});

  /// Contrat provisoire : crée Utilisateur + Citoyen et renvoie les mêmes jetons que login.
  Future<void> register(RegisterData data);

  Future<void> logout();
  Future<void> requestPasswordReset(String identifiant);
  /// [identifiant] = téléphone ou email saisi à l'étape « mot de passe oublié » (le serveur en a besoin pour retrouver le compte).
  Future<void> resetPassword({
    required String identifiant,
    required String code,
    required String newPassword,
  });
}
