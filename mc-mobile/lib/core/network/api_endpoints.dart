class ApiEndpoints {
  const ApiEndpoints._();

  /// Alias provisoire éviter de stocker l'id citoyen
  static const String me = 'me';

  static const String auth = '/api/v1/auth';
  static const String login = '$auth/login';
  static const String register = '$auth/register';
  static const String logout = '$auth/logout';
  static const String forgotPassword = '$auth/forgot-password';
  static const String resetPassword = '$auth/reset-password';

  static const String citoyens = '/api/v1/citoyens';
  static String citoyen(String id) => '$citoyens/$id';
  static String citoyenCompetences(String id) => '$citoyens/$id/competences';
  static String citoyenExperiences(String id) => '$citoyens/$id/experiences';
  static String citoyenPortfolio(String id) => '$citoyens/$id/portfolio';

  static String citoyenCommune(String id) => '$citoyens/$id/commune';
  static String citoyenDisponibilite(String id) =>
      '$citoyens/$id/disponibilite';
  static String citoyenPhoto(String id) => '/api/v1/citoyens/$id/photo';
  static String citoyenCv(String id) => '/api/v1/citoyens/$id/cv';
  static const String regions = '/api/v1/regions';
  static String commune(String id) => '$communes/$id';
  static String citoyenPreuves(String id) => '/api/citoyens/$id/preuves';
  static String preuve(String id) => '/api/preuves/$id';
  // Backend : GET /api/citoyens/me/validations (sans /v1)
  static String citoyenValidations(String id) =>
      '/api/citoyens/$id/validations';
  static String validation(String id) => '/api/v1/validations/$id';

  static const String organisations = '/api/organisations';
  static const String centres = '/api/v1/centres';
  static const String competences = '/api/v1/competences';
  static const String communes = '/api/v1/communes';
  static String citoyenRegionCommunes(String regionId) =>
      '$communes/region/$regionId';

  static String preuves(String citoyenCompetenceId) =>
      '/api/v1/citoyen-competences/$citoyenCompetenceId/preuves';
  static String validations(String citoyenCompetenceId) =>
      '/api/v1/citoyen-competences/$citoyenCompetenceId/validations';

  static const String besoins = '/api/besoins';
  static const String demandesMiseEnRelation = '/api/demandes-mise-en-relation';
  static const String opportunites = '/api/opportunites';
  static const String skillsGap = '/api/v1/indicateurs/skills-gap';

  // ==========================================
  // --- Notifications Endpoints ---
  // ==========================================

  // ==========================================
  // --- Opportunites Endpoints ---
  // ==========================================
  static String opportunite(String id) => '$opportunites/$id';

  // Fonctionnalités mobiles (routes confirmées et routes provisoires signalées dans docs/CONTRAT_API_PROVISOIRE.md).
  static const String mobileNotifications = '/api/notifications';
  static String mobileUserNotifications(String userId) =>
      '$mobileNotifications/utilisateur/$userId';
  static String mobileUnreadNotifications(String userId) =>
      '${mobileUserNotifications(userId)}/non-lues';
  static String mobileMarkNotificationRead(String recipientLinkId) =>
      '$mobileNotifications/$recipientLinkId/marquer-lue';

  static const String mobileOpportunities = '/api/opportunites';
  static String mobileOpportunity(String id) => '$mobileOpportunities/$id';

  static const String mobileTalentSearch = '/api/talents/recherche';
  static String mobileAnonymizedTalent(String id) => '/api/talents/$id';

  static const String mobileRelations = '/api/demandes-mise-en-relation';
  static String mobileRelation(String id) => '$mobileRelations/$id';

  static const String mobileTests = '/api/v1/tests-numeriques';
  static const String admin = '/api/admin';
  static String adminDashboard() => '$admin/dashboard';
  static String adminUsers() => '$admin/users';
  static String adminUserRole(String id) => '$admin/users/$id/role';
  static String adminValidations() => '$admin/validations';
  static String adminValidationStatus(String id) =>
      '$admin/validations/$id/statut';
  static String adminTests() => '$admin/tests-numeriques';
  static String adminTest(String id) => '$admin/tests-numeriques/$id';
  static String adminOpportunities() => '$admin/opportunites';
  static String adminOpportunity(String id) => '$opportunites/$id';
  static const String opportunityCategories = '/api/categories-opportunites';
  static const String adminCompetences = '$admin/competences';
  static String mobileTestsForCompetence(String competenceId) =>
      '$mobileTests/competence/$competenceId';
  static String mobileTest(String id) => '$mobileTests/$id';
  static String mobileTestQuestions(String id) => '${mobileTest(id)}/questions';
  static String mobileQuestionOptions(String questionId) =>
      '$mobileTests/questions/$questionId/propositions';
  static String mobileTestResults(String testId) =>
      '${mobileTest(testId)}/resultats';
  static String mobileCitizenTestResults(String citizenId) =>
      '$mobileTests/resultats/citoyen/$citizenId';
  static String mobileCitizenCompetences(String citizenId) =>
      '/api/v1/citoyens/$citizenId/competences';

  static String mobilePortfolio(String citizenId) =>
      '/api/v1/citoyens/$citizenId/portfolio';
  static String mobilePortfolioItem(String citizenId, String id) =>
      '${mobilePortfolio(citizenId)}/$id';
  static String mobilePortfolioMediaUpload(String realisationId) =>
      '/api/v1/portfolio-realisations/$realisationId/medias';
  static String mobilePortfolioMedia(String realisationId, String mediaId) =>
      '/api/v1/portfolio-realisations/$realisationId/medias/$mediaId';

  static String mobileUser(String userId) => '/api/utilisateurs/$userId';
  static String mobileUserContact(String userId) =>
      '/api/utilisateurs/$userId/contact';
  static const String mobileUserPreferences =
      '/api/utilisateurs/me/preferences';
  static const String mobilePasswordChange = '/api/v1/auth/mot-de-passe';
}
