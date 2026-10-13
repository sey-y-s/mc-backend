import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/features/passport/domain/passport_models.dart';

abstract interface class PassportRepository {
  /// Citoyen connecté.
  Future<Citoyen> getMine();

  /// Lève ValidationFailure si nom ou prénom est vide.
  Future<Citoyen> updateProfile(
      {required String nom, required String prenom, Sexe? sexe});

  Future<Citoyen> updateCommune(String communeId);
  Future<Citoyen> updateAvailability(Disponibilite value);

  /// Envoi de la photo de profil avec progression (0..1).
  Future<Citoyen> updatePhoto(PickedMedia media,
      {void Function(double progress)? onProgress});

  Future<List<Region>> listRegions();
  Future<List<Commune>> listCommunes(String regionId);
}
