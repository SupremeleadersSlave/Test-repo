package hr.tvz.hotel.entities;

/**
 * Nabrojani tip: predstavlja status rezervacije.
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
