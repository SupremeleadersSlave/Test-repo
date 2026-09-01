package hr.tvz.hotel.entities;

/**
 * Korisnik sustava, djelatnik hotela, prijavljuje se korisničkim
 * imenom i lozinkom.
 */
public class User extends Person {

    private String username;
    private String passwordHash;
    private Role role;

    /**
     * Kreira novog korisnika sustava.
     *
     * @param id           identifikator korisnika
     * @param firstName    ime
     * @param lastName     prezime
     * @param email        adresa elektroničke pošte
     * @param phone        broj telefona
     * @param username     korisničko ime za prijavu
     * @param passwordHash hashirana lozinka
     * @param role         uloga korisnika u sustavu
     */
    public User(Long id, String firstName, String lastName, String email, String phone,
                String username, String passwordHash, Role role) {
        super(id, firstName, lastName, email, phone);
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
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
}
