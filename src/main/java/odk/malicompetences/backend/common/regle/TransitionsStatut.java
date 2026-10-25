package odk.malicompetences.backend.common.regle;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Table de transitions autorisees pour un enum de statuts.
 *
 * <p>Chaque constante de l'enum DOIT etre declaree (avec {@link Builder#de} ou {@link Builder#termine}) :
 * ajouter un statut sans decider de ses transitions fait echouer le chargement de la classe, donc les tests.
 * Usage dans un service : {@code Transitions.BESOIN.verifier(besoin.getStatut(), nouveauStatut);}
 */
public final class TransitionsStatut<E extends Enum<E>> {

    private final Class<E> type;
    private final Map<E, Set<E>> autorisees;

    private TransitionsStatut(Class<E> type, Map<E, Set<E>> autorisees) {
        this.type = type;
        this.autorisees = autorisees;
    }

    public static <E extends Enum<E>> Builder<E> pour(Class<E> type) {
        return new Builder<>(type);
    }

    /** Vrai si le passage est permis. Rester sur le meme statut n'est pas une transition. */
    public boolean autorise(E de, E vers) {
        return de != null && vers != null && autorisees.get(de).contains(vers);
    }

    /** @throws IllegalStateException si le passage est interdit. */
    public void verifier(E de, E vers) {
        if (de == vers) {
            throw new IllegalStateException("Le statut est deja " + de + " (" + type.getSimpleName() + ")");
        }
        if (!autorise(de, vers)) {
            throw new IllegalStateException(
                    "Transition interdite pour " + type.getSimpleName() + " : " + de + " -> " + vers);
        }
    }

    public Set<E> depuis(E de) {
        return Set.copyOf(autorisees.get(de));
    }

    public boolean estFinal(E statut) {
        return autorisees.get(statut).isEmpty();
    }

    public static final class Builder<E extends Enum<E>> {
        private final Class<E> type;
        private final Map<E, Set<E>> map;

        private Builder(Class<E> type) {
            this.type = type;
            this.map = new EnumMap<>(type);
        }

        @SafeVarargs
        public final Builder<E> de(E source, E... destinations) {
            if (map.containsKey(source)) {
                throw new IllegalStateException(type.getSimpleName() + " : " + source + " declare deux fois");
            }
            EnumSet<E> dest = EnumSet.noneOf(type);
            for (E destination : destinations) {
                dest.add(destination);
            }
            map.put(source, dest);
            return this;
        }

        @SafeVarargs
        public final Builder<E> termine(E... finaux) {
            for (E f : finaux) de(f);
            return this;
        }

        public TransitionsStatut<E> build() {
            Set<E> manquants = EnumSet.allOf(type);
            manquants.removeAll(map.keySet());
            if (!manquants.isEmpty()) {
                throw new IllegalStateException(type.getSimpleName()
                        + " : transitions non declarees pour " + manquants.stream().map(Enum::name).collect(Collectors.joining(", ")));
            }
            return new TransitionsStatut<>(type, map);
        }
    }
}
