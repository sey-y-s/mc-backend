enum ValidationStatut {
  enAttente('EN_ATTENTE', 'En attente'),
  approuvee('APPROUVEE', 'Approuvée'),
  rejetee('REJETEE', 'Rejetée');

  const ValidationStatut(this.apiCode, this.label);
  final String apiCode;
  final String label;

  static ValidationStatut fromApi(String v) =>
      ValidationStatut.values.firstWhere((e) => e.apiCode == v, orElse: () => ValidationStatut.enAttente);
}
