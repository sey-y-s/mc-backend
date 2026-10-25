package odk.malicompetences.backend.common.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContactCompteTest {

    @Test
    void telephoneSeulEstNormalise() {
        ContactCompte c = ContactCompte.normaliser("70 00 00 00", null);
        assertEquals("+22370000000", c.telephone());
        assertNull(c.email());
    }

    @Test
    void emailSeulEstNormaliseEnMinuscules() {
        ContactCompte c = ContactCompte.normaliser("", "  Awa.Diallo@Example.ML ");
        assertNull(c.telephone());
        assertEquals("awa.diallo@example.ml", c.email());
    }

    @Test
    void chainesVidesNeSontPasUnContact() {
        assertThrows(IllegalArgumentException.class, () -> ContactCompte.normaliser("", "   "));
        assertThrows(IllegalArgumentException.class, () -> ContactCompte.normaliser(null, null));
    }

    @Test
    void valeursInvalidesSontRefusees() {
        assertThrows(IllegalArgumentException.class, () -> ContactCompte.normaliser("123", null));
        assertThrows(IllegalArgumentException.class, () -> ContactCompte.normaliser(null, "pas-un-email"));
        assertThrows(IllegalArgumentException.class, () -> ContactCompte.normaliser(null, "a".repeat(200) + "@x.ml"));
    }
}
