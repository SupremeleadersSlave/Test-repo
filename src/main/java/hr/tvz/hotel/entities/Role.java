package hr.tvz.hotel.entities;

/**
 * Nabrojani tip: predstavlja ulogu korisnika u sustavu.
 * <p>
 * Zahtijeva barem dvije uloge: administratora za korisničke račune
 * i djelatnika recepcije za goste i rezervacije.
 */
public enum Role {

    /** Administrator sustava s punim pravima upravljanja. */
    ADMIN,

    /** Djelatnik recepcije: upravlja gostima i rezervacijama. */
    RECEPTIONIST
}
