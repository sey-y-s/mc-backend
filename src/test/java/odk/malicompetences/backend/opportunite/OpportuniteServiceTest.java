package odk.malicompetences.backend.opportunite;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OpportuniteServiceTest {

    private OpportuniteRepository repository;
    private CategorieOpportuniteRepository categorieRepository;
    private OpportuniteMapper mapper;
    private OpportuniteService service;

    @BeforeEach
    void setUp() {
        repository = mock(OpportuniteRepository.class);
        categorieRepository = mock(CategorieOpportuniteRepository.class);
        mapper = mock(OpportuniteMapper.class);
        service = new OpportuniteService(repository, categorieRepository, mapper);
    }

    @Test
    void listePubliqueDemandeUniquementLesOpportunitesPublieesEtNonExpirees() {
        when(repository.findVisibles(eq(StatutOpportunite.PUBLIEE), any(LocalDate.class)))
                .thenReturn(List.of());

        assertTrue(service.listerToutes().isEmpty());

        verify(repository).findVisibles(eq(StatutOpportunite.PUBLIEE), any(LocalDate.class));
        verify(repository, never()).findAll();
    }

    @Test
    void detailPublicMasqueUneOpportuniteNonVisible() {
        UUID id = UUID.randomUUID();
        when(repository.findVisibleById(eq(id), eq(StatutOpportunite.PUBLIEE), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.trouverParId(id));
    }

    @Test
    void gestionPeutListerToutesLesOpportunites() {
        when(repository.findAll()).thenReturn(List.of());

        assertTrue(service.listerPourGestion().isEmpty());
        verify(repository).findAll();
    }
}
