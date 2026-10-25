package odk.malicompetences.backend.opportunite;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.territoire.Territoire; import odk.malicompetences.backend.territoire.TerritoireRepository;
import odk.malicompetences.backend.utilisateur.Utilisateur; import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate; import java.util.List; import java.util.UUID;
@Service @Transactional public class OpportuniteService {
 private final OpportuniteRepository repository; private final CategorieOpportuniteRepository categories; private final OpportuniteMapper mapper; private final UtilisateurRepository utilisateurs; private final TerritoireRepository territoires;
 public OpportuniteService(OpportuniteRepository repository,CategorieOpportuniteRepository categories,OpportuniteMapper mapper,UtilisateurRepository utilisateurs,TerritoireRepository territoires){this.repository=repository;this.categories=categories;this.mapper=mapper;this.utilisateurs=utilisateurs;this.territoires=territoires;}
 public OpportuniteResponse creerOpportunite(OpportuniteRequest r,UUID auteurId){CategorieOpportunite c=categories.findById(r.categorieId()).orElseThrow(()->new ResourceNotFoundException("Catégorie introuvable"));Utilisateur auteur=utilisateurs.findById(auteurId).orElseThrow(()->new ResourceNotFoundException("Compte auteur introuvable"));Territoire t=chargerTerritoire(r.territoireId());return mapper.toResponse(repository.save(mapper.toEntity(r,c,t,auteur)));}
 @Transactional(readOnly=true) public List<OpportuniteResponse> listerToutes(){return repository.findVisibles(StatutOpportunite.PUBLIEE,LocalDate.now()).stream().map(mapper::toResponse).toList();}
 @Transactional(readOnly=true) public List<OpportuniteResponse> listerPourGestion(){return repository.findAll().stream().map(mapper::toResponse).toList();}
 @Transactional(readOnly=true) public OpportuniteResponse trouverParId(UUID id){return repository.findVisibleById(id,StatutOpportunite.PUBLIEE,LocalDate.now()).map(mapper::toResponse).orElseThrow(()->new ResourceNotFoundException("Opportunité non trouvée."));}
 @Transactional(readOnly=true) public OpportuniteResponse trouverPourGestion(UUID id){return mapper.toResponse(repository.findById(id).orElseThrow(()->new ResourceNotFoundException("Opportunité introuvable")));}
 public OpportuniteResponse modifierOpportunite(UUID id,OpportuniteRequest r){Opportunite o=repository.findById(id).orElseThrow(()->new ResourceNotFoundException("Opportunité introuvable"));CategorieOpportunite c=categories.findById(r.categorieId()).orElseThrow(()->new ResourceNotFoundException("Catégorie introuvable"));mapper.updateEntity(r,o,c,chargerTerritoire(r.territoireId()));return mapper.toResponse(repository.save(o));}
 public void supprimerOpportunite(UUID id){if(!repository.existsById(id))throw new ResourceNotFoundException("Opportunité introuvable");repository.deleteById(id);}
 private Territoire chargerTerritoire(UUID id){if(id==null)return null;return territoires.findById(id).orElseThrow(()->new ResourceNotFoundException("Territoire introuvable"));}
}
