package hr.tvz.hotel.entities;

/**
 * Status rezervacije.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public enum ReservationStatus {

    /** Rezervacija je kreirana, ali još nije potvrđena. */
    PENDING,

    /** Rezervaciju je potvrdio djelatnik recepcije. */
    CONFIRMED,

    /** Rezervacija je otkazana. */
    CANCELLED,

    /** Boravak gosta je završen. */
    COMPLETED
}