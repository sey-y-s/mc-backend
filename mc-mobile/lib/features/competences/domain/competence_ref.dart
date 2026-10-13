/// Compétence du référentiel national (ce que le citoyen peut choisir d'ajouter).
class CompetenceRef {
  const CompetenceRef({required this.id, required this.nom, this.secteurNom});
  final String id;
  final String nom;
  final String? secteurNom;

  factory CompetenceRef.fromJson(Map<String, dynamic> j) => CompetenceRef(
        id: j['id'] as String,
        nom: j['nom'] as String,
        secteurNom: j['secteurNom'] as String?,
      );
}
