package hr.tvz.hotel.entities;

import java.util.Objects;

/**
 * Korisnik sustava, djelatnik hotela, prijavljuje se korisničkim
 * imenom i lozinkom.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public class User extends Person {

    private String username;
    private String passwordHash;
    private Role role;

    private User(Builder builder) {
        super(builder.id, builder.firstName, builder.lastName, builder.email, builder.phone);
        this.username = builder.username;
        this.passwordHash = builder.passwordHash;
        this.role = builder.role;
    }

    @Override
    public String getPersonType() {
        return "Korisnik sustava";
    }

    /**
     * Vraća korisničko ime za prijavu.
     *
     * @return korisničko ime
     */
    public String getUsername() {
        return username;
    }

    /**
     * Postavlja korisničko ime za prijavu.
     *
     * @param username novo korisničko ime
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Vraća hashiranu lozinku korisnika.
     *
     * @return hashirana lozinka
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Postavlja hashiranu lozinku korisnika.
     *
     * @param passwordHash nova hashirana lozinka
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Vraća ulogu korisnika u sustavu.
     *
     * @return uloga korisnika
     */
    public Role getRole() {
        return role;
    }

    /**
     * Postavlja ulogu korisnika u sustavu.
     *
     * @param role nova uloga korisnika
     */
    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /**
     * Gradi {@link User} korak po korak (builder pattern).
     */
    public static final class Builder {

        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String username;
        private String passwordHash;
        private Role role;

        /**
         * Postavlja id korisnika.
         *
         * @param id id korisnika
         * @return ovaj builder
         */
        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        /**
         * Postavlja ime i prezime korisnika.
         *
         * @param firstName ime
         * @param lastName prezime
         * @return ovaj builder
         */
        public Builder name(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
            return this;
        }

        /**
         * Postavlja kontakt podatke korisnika.
         *
         * @param email email
         * @param phone broj telefona
         * @return ovaj builder
         */
        public Builder contact(String email, String phone) {
            this.email = email;
            this.phone = phone;
            return this;
        }

        /**
         * Postavlja podatke za prijavu korisnika.
         *
         * @param username korisničko ime
         * @param passwordHash hashirana lozinka
         * @return ovaj builder
         */
        public Builder credentials(String username, String passwordHash) {
            this.username = username;
            this.passwordHash = passwordHash;
            return this;
        }

        /**
         * Postavlja ulogu korisnika.
         *
         * @param role uloga korisnika
         * @return ovaj builder
         */
        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        /**
         * Gradi korisnika iz postavljenih podataka.
         *
         * @return novi korisnik
         */
        public User build() {
            return new User(this);
        }
    }
}
