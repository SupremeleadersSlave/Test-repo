package hr.tvz.hotel.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Predstavlja imenovanu poslovnu vezu između dva entiteta, npr.
 * {@link User} obrađuje {@link Reservation}.
 * <p>
 * Generički tip "Pair&lt;K, V&gt;" drži samo dvije vrijednosti bez
 * značenja. Ova klasa, za razliku od njega, uz entitete čuva vrstu
 * veze i trenutak uspostave, s poslovnom semantikom domene.
 *
 * @param <A> tip izvornog entiteta veze
 * @param <B> tip ciljnog entiteta veze
 */
public class Relation<A, B> {

    private final A source;
    private final B target;
    private final String relationType;
    private final LocalDateTime establishedAt;

    /**
     * Kreira novu vezu između dva entiteta.
     *
     * @param source       izvorni entitet veze
     * @param target       ciljni entitet veze
     * @param relationType opis vrste veze: "OBRADIO", "PREFERIRA"
     */
    public Relation(A source, B target, String relationType) {
        this.source = source;
        this.target = target;
        this.relationType = relationType;
        this.establishedAt = LocalDateTime.now();
    }

    /**
     * Vraća izvorni entitet veze.
     *
     * @return izvorni entitet veze
     */
    public A getSource() {
        return source;
    }

    /**
     * Vraća ciljni entitet veze.
     *
     * @return ciljni entitet veze
     */
    public B getTarget() {
        return target;
    }

    /**
     * Vraća opis vrste veze.
     *
     * @return vrsta veze
     */
    public String getRelationType() {
        return relationType;
    }

    /**
     * Vraća trenutak kada je veza uspostavljena.
     *
     * @return vrijeme uspostave veze
     */
    public LocalDateTime getEstablishedAt() {
        return establishedAt;
    }

    /**
     * Provjerava sudjeluje li entitet u vezi, kao izvor ili cilj.
     *
     * @param entity entitet za provjeru
     * @return {@code true}: entitet sudjeluje u vezi
     */
    public boolean involves(Object entity) {
        return Objects.equals(source, entity) || Objects.equals(target, entity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Relation<?, ?> relation)) {
            return false;
        }
        return Objects.equals(source, relation.source)
                && Objects.equals(target, relation.target)
                && Objects.equals(relationType, relation.relationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, relationType);
    }

    @Override
    public String toString() {
        return "Relation{" + source + " -[" + relationType + "]-> " + target + "}";
    }
}
