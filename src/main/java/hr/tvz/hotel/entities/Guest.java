package hr.tvz.hotel.entities;

/**
 * Predstavlja gosta hotela.
 *
 * @version 1.0
 */
public class Guest extends Person {

    private String documentNumber;
    private Address address;

    /**
     * Kreira novog gosta hotela.
     *
     * @param id id gosta
     * @param firstName ime
     * @param lastName prezime
     * @param email email gosta
     * @param phone broj telefona
     * @param documentNumber broj osobnog dokumenta
     * @param address adresa gosta
     */
    public Guest(Long id, String firstName, String lastName, String email, String phone,
                 String documentNumber, Address address) {
        super(id, firstName, lastName, email, phone);
        this.documentNumber = documentNumber;
        this.address = address;
    }

    @Override
    public String getPersonType() {
        return "Gost";
    }

    /**
     * Vraća broj osobnog dokumenta gosta.
     *
     * @return broj osobnog dokumenta
     */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Postavlja broj osobnog dokumenta gosta.
     *
     * @param documentNumber novi broj osobnog dokumenta
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    /**
     * Vraća adresu prebivališta gosta.
     *
     * @return adresa prebivališta
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Postavlja adresu prebivališta gosta.
     *
     * @param address nova adresa prebivališta
     */
    public void setAddress(Address address) {
        this.address = address;
    }
}
