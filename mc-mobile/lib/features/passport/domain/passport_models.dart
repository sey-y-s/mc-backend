enum Sexe {
  femme('FEMME', 'Femme'),
  homme('HOMME', 'Homme');

  const Sexe(this.apiCode, this.label);
  final String apiCode;
  final String label;

  /// Valeurs API provisoires. null si absent ou inconnu.
  static Sexe? fromApi(String? v) {
    for (final s in Sexe.values) {
      if (s.apiCode == v) return s;
    }
    return null;
  }
}

enum Disponibilite {
  disponible('DISPONIBLE', 'Disponible'),
  bientot('EN_APPRENTISSAGE', 'Bientôt disponible'),
  indisponible('NON_DISPONIBLE', 'Indisponible');

  const Disponibilite(this.apiCode, this.label);
  final String apiCode;
  final String label;

  /// Valeurs API provisoires (à confirmer). Valeur inconnue -> disponible.
  static Disponibilite fromApi(String? v) => Disponibilite.values.firstWhere(
    (d) => d.apiCode == v,
    orElse: () => Disponibilite.disponible,
  );
}

class Region {
  const Region({required this.id, required this.nom});
  final String id;
  final String nom;

  factory Region.fromJson(Map<String, dynamic> j) =>
      Region(id: j['id'] as String, nom: j['nom'] as String);

  @override
  bool operator ==(Object other) => other is Region && other.id == id;
  @override
  int get hashCode => id.hashCode;
}

class Commune {
  const Commune({
    required this.id,
    required this.nom,
    required this.regionId,
    required this.regionNom,
  });
  final String id;
  final String nom;
  final String regionId;
  final String regionNom;

  String get fullLabel => '$nom, $regionNom';

  factory Commune.fromJson(Map<String, dynamic> j) => Commune(
    id: j['id'] as String,
    nom: j['nom'] as String,
    regionId: j['regionId'] as String,
    regionNom: j['regionNom'] as String,
  );

  @override
  bool operator ==(Object other) => other is Commune && other.id == id;
  @override
  int get hashCode => id.hashCode;
}

/// Profil métier du citoyen (le compte technique « Utilisateur » n'est pas exposé ici).
class Citoyen {
  const Citoyen({
    required this.id,
    required this.nom,
    required this.prenom,
    required this.codePasseport,
    required this.disponibilite,
    this.sexe,
    this.photoUrl,
    this.commune,
  });

  final String id;
  final String nom;
  final String prenom;
  final Sexe? sexe;
  final String? photoUrl;
  final String codePasseport;
  final Disponibilite disponibilite;
  final Commune? commune;

  String get nomComplet => '$prenom $nom';

  factory Citoyen.fromJson(Map<String, dynamic> j) => Citoyen(
    id: j['id'] as String,
    nom: j['nom'] as String,
    prenom: j['prenom'] as String,
    sexe: Sexe.fromApi(j['sexe'] as String?),
    photoUrl: j['photoUrl'] as String?,
    codePasseport: j['codePasseport'] as String,
    disponibilite: Disponibilite.fromApi(j['disponibilite'] as String?),
    commune: j['commune'] == null
        ? null
        : Commune.fromJson(j['commune'] as Map<String, dynamic>),
  );
}
