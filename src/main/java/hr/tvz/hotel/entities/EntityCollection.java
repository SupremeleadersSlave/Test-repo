package hr.tvz.hotel.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Kolekcija entiteta s operacijama dodavanja, uklanjanja, filtriranja i sortiranja.
 *
 * @param <T> tip entiteta
 */
public class EntityCollection<T> {

    private final List<T> items = new ArrayList<>();

    /**
     * Dodaje entitet u kolekciju.
     *
     * @param item entitet za dodavanje
     */
    public void add(T item) {
        items.add(item);
    }

    /**
     * Uklanja entitet iz kolekcije.
     *
     * @param item entitet za uklanjanje
     * @return {@code true} ako je entitet uklonjen
     */
    public boolean remove(T item) {
        return items.remove(item);
    }

    /**
     * Vraća nepromjenjivu kolekciju svih entiteta.
     *
     * @return svi entiteti
     */
    public List<T> getAll() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Filtrira entitete prema predikatu.
     *
     * @param predicate uvjet filtriranja
     * @return filtrirani entiteti
     */
    public List<T> filter(Predicate<T> predicate) {
        return items.stream().filter(predicate).toList();
    }

    /**
     * Vraća entitete sortirane prema komparatoru.
     *
     * @param comparator redoslijed sortiranja
     * @return sortirani entiteti
     */
    public List<T> sorted(Comparator<T> comparator) {
        List<T> copy = new ArrayList<>(items);
        copy.sort(comparator);
        return copy;
    }

    /**
     * Vraća entitete kao skup, bez duplikata.
     *
     * @return skup entiteta
     */
    public Set<T> toSet() {
        return new HashSet<>(items);
    }

    /**
     * Vraća broj entiteta u kolekciji.
     *
     * @return broj entiteta
     */
    public int size() {
        return items.size();
    }

    /**
     * Provjerava je li kolekcija prazna.
     *
     * @return {@code true} ako je kolekcija prazna
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Uklanja sve entitete iz kolekcije.
     */
    public void clear() {
        items.clear();
    }
}
