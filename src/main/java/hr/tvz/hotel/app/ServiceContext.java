package hr.tvz.hotel.app;

import hr.tvz.hotel.db.DatabaseConnection;
import hr.tvz.hotel.persistence.ChangeLogManager;
import hr.tvz.hotel.service.AuthService;
import hr.tvz.hotel.service.GuestService;
import hr.tvz.hotel.service.InvoiceService;
import hr.tvz.hotel.service.ReservationService;
import hr.tvz.hotel.service.RoomService;
import hr.tvz.hotel.service.UserService;

/**
 * Objedinjuje usluge i upravitelje potrebne UI-u.
 *
 * @param databaseConnection konekcija prema bazi
 * @param roomService        usluga za upravljanje sobama
 * @param guestService       usluga za upravljanje gostima
 * @param reservationService usluga za upravljanje rezervacijama
 * @param invoiceService     usluga za upravljanje računima
 * @param userService        usluga za upravljanje usera
 * @param authService        usluga za prijavu usera
 * @param changeLogManager   upravitelj loga
 */
public record ServiceContext(
        DatabaseConnection databaseConnection,
        RoomService roomService,
        GuestService guestService,
        ReservationService reservationService,
        InvoiceService invoiceService,
        UserService userService,
        AuthService authService,
        ChangeLogManager changeLogManager
) {
}
