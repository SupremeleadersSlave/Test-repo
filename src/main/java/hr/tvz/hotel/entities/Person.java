package hr.tvz.hotel.entities;

import java.util.Objects;

/**
 * Zajednička svojstva korisnika sustava i gostiju hotela.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public abstract class Person {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    /**
     * Kreira novu osobu.
     *
     * @param id id osobe
     * @param firstName ime
     * @param lastName prezime
     * @param email email osobe
     * @param phone broj telefona
     */
    protected Person(Long id, String firstName, String lastName, String email, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    /**
     * Vraća opis vrste osobe.
     *
     * @return opis vrste osobe
     */
    public abstract String getPersonType();

    /**
     * Vraća id osobe.
     *
     * @return id osobe
     */
    public Long getId() {
        return id;
    }

    /**
     * Postavlja id osobe.
     *
     * @param id novi id osobe
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Vraća ime osobe.
     *
     * @return ime osobe
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Postavlja ime osobe.
     *
     * @param firstName novo ime osobe
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Vraća prezime osobe.
     *
     * @return prezime osobe
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Postavlja prezime osobe.
     *
     * @param lastName novo prezime osobe
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Vraća email osobe.
     *
     * @return email osobe
     */
    public String getEmail() {
        return email;
    }

    /**
     * Postavlja email osobe.
     *
     * @param email novi email osobe
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Vraća broj telefona osobe.
     *
     * @return broj telefona
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Postavlja broj telefona osobe.
     *
     * @param phone novi broj telefona
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Vraća puno ime i prezime osobe.
     *
     * @return puno ime osobe
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Person person)) {
            return false;
        }
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getPersonType() + "{id=" + id + ", ime='" + getFullName() + "'}";
    }
}
