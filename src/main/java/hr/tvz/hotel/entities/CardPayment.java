package hr.tvz.hotel.entities;

import java.io.Serializable;

/**
 * Plaćanje karticom.
 *
 * @param maskedCardNumber maskirani broj kartice
 * @param authorizationCode autorizacijski kod transakcije
 */
public record CardPayment(String maskedCardNumber, String authorizationCode) implements PaymentMethod, Serializable {

    @Override
    public String describe() {
        return "Kartica (" + maskedCardNumber + ")";
    }
}
