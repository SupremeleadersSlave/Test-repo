package hr.tvz.hotel.app;

/**
 * Pokretač aplikacije koji ne nasljeđuje JavaFX klasu Application.
 * Time se izbjegava provjera modula JavaFX runtimea pri pokretanju
 * aplikacije izravno iz razvojnog okruženja.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public final class Launcher {

    private Launcher() {
    }

    /**
     * Ulazna točka programa, prosljeđuje pokretanje klasi {@link MainApp}.
     *
     * @param args argumenti naredbenog retka
     */
    public static void main(String[] args) {
        MainApp.main(args);
    }
}