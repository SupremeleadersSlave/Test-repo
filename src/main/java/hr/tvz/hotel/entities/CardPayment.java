package hr.tvz.hotel.entities;

import java.io.Serializable;

/**
 * Predstavlja plaćanje karticom.
 *
 * @param maskedCardNumber broj kartice prikazan u maskiranom obliku
 * @param authorizationCode autorizacijski kod transakcije
 */
public record CardPayment(String maskedCardNumber, String authorizationCode) implements PaymentMethod, Serializable {

    @Override
    public String describe() {
        return "Kartica (" + maskedCardNumber + ")";
    }
}
