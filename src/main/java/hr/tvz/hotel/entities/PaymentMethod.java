package hr.tvz.hotel.entities;

/**
 * Sealed sučelje za način plaćanja računa.
 */
public sealed interface PaymentMethod permits CashPayment, CardPayment {

    /**
     * Vraća tekstualni opis načina plaćanja.
     *
     * @return opis načina plaćanja
     */
    String describe();
}
