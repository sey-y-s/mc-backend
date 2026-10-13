import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/features/preuves/domain/preuve_models.dart';

abstract interface class PreuveRepository {
  /// Toutes les preuves du citoyen connecté, plus récentes d'abord (paginé).
  Future<List<Preuve>> listMine({int page = 0, int size = 20});

  Future<List<Preuve>> listForCompetence(String citoyenCompetenceId);

  /// Lève NotFoundFailure (inexistante) ou ForbiddenFailure (pas au citoyen).
  Future<Preuve> get(String id);

  /// Envoie le fichier avec progression (0..1). Lève NotFoundFailure si la compétence n'existe pas.
  /// Règle serveur : une première preuve rend la compétence « attestée ».
  Future<Preuve> add({
    required String citoyenCompetenceId,
    required PreuveType type,
    required PickedMedia fichier,
    void Function(double progress)? onProgress,
  });
}
