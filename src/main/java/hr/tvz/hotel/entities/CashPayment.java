package hr.tvz.hotel.entities;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Plaćanje gotovinom.
 *
 * @param amountReceived iznos primljene gotovine
 *
 * @version 1.0
 */
public record CashPayment(BigDecimal amountReceived) implements PaymentMethod, Serializable {

    @Override
    public String describe() {
        return "Gotovina (primljeno: " + amountReceived + ")";
    }
}
