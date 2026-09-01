package hr.tvz.hotel.entities;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Plaćanje gotovinom.
 *
 * @param amountReceived iznos primljene gotovine
 */
public record CashPayment(BigDecimal amountReceived) implements PaymentMethod, Serializable {

    @Override
    public String describe() {
        return "Gotovina (primljeno: " + amountReceived + ")";
    }
}
