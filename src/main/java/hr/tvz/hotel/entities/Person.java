package hr.tvz.hotel.entities;

import java.util.Objects;

/**
 * Predstavlja zajednička svojstva svih osoba u sustavu: korisnika
 * sustava i gostiju hotela.
 */
public abstract class Person {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    /**
     * Kreira novu osobu sa zadanim osobnim podacima.
     *
     * @param id        identifikator osobe
     * @param firstName ime
     * @param lastName  prezime
     * @param email     adresa elektroničke pošte
     * @param phone     broj telefona
     */
    protected Person(Long id, String firstName, String lastName, String email, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    /**
     * Vraća opis vrste osobe, npr. "Korisnik sustava" ili "Gost".
     *
     * @return opis vrste osobe
     */
    public abstract String getPersonType();

    /**
     * Vraća identifikator osobe.
     *
     * @return identifikator osobe
     */
    public Long getId() {
        return id;
    }

    /**
     * Postavlja identifikator osobe.
     *
     * @param id novi identifikator osobe
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
     * Vraća adresu elektroničke pošte osobe.
     *
     * @return adresa elektroničke pošte
     */
    public String getEmail() {
        return email;
    }

    /**
     * Postavlja adresu elektroničke pošte osobe.
     *
     * @param email nova adresa elektroničke pošte
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
