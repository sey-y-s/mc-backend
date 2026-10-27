package odk.malicompetences.backend.localisation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/** Limiteur à fenêtre glissante par clé (ici l'adresse IP), en mémoire : suffisant pour une route publique légère. */
final class LimiteurDebit {

    private final int maximum;
    private final long fenetreMs;
    private final ConcurrentHashMap<String, Deque<Long>> appels = new ConcurrentHashMap<>();

    LimiteurDebit(int maximum, long fenetreMs) {
        this.maximum = maximum;
        this.fenetreMs = fenetreMs;
    }

    boolean autoriser(String cle) {
        long maintenant = System.currentTimeMillis();
        Deque<Long> file = appels.computeIfAbsent(cle == null ? "?" : cle, k -> new ArrayDeque<>());
        synchronized (file) {
            while (!file.isEmpty() && maintenant - file.peekFirst() > fenetreMs) file.pollFirst();
            if (file.size() >= maximum) return false;
            file.addLast(maintenant);
            return true;
        }
    }
}
