package hr.tvz.hotel.entities;

/**
 * Sealed sučelje za način plaćanja računa.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public sealed interface PaymentMethod permits CashPayment, CardPayment {

    /**
     * Vraća tekstualni opis načina plaćanja.
     *
     * @return opis načina plaćanja
     */
    String describe();
}
