package hr.tvz.hotel.entities;

/**
 * Zapečaćeno sučelje: predstavlja način plaćanja računa.
 * <p>
 * Implementacije su ograničene na {@link CashPayment} i {@link CardPayment};
 * kompajler jamči da drugih vrsta plaćanja nema.
 */
public sealed interface PaymentMethod permits CashPayment, CardPayment {

    /**
     * Vraća tekstualni opis načina plaćanja prikladan za prikaz korisniku.
     *
     * @return opis načina plaćanja
     */
    String describe();
}
