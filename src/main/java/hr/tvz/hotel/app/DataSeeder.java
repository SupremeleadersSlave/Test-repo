package hr.tvz.hotel.app;

import hr.tvz.hotel.entities.Address;
import hr.tvz.hotel.entities.CardPayment;
import hr.tvz.hotel.entities.CashPayment;
import hr.tvz.hotel.entities.Capacity;
import hr.tvz.hotel.entities.Guest;
import hr.tvz.hotel.entities.Invoice;
import hr.tvz.hotel.entities.PaymentMethod;
import hr.tvz.hotel.entities.Reservation;
import hr.tvz.hotel.entities.ReservationStatus;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.Room;
import hr.tvz.hotel.entities.RoomStatus;
import hr.tvz.hotel.entities.RoomType;
import hr.tvz.hotel.exceptions.ReservationNotAvailableException;
import hr.tvz.hotel.service.GuestService;
import hr.tvz.hotel.service.InvoiceService;
import hr.tvz.hotel.service.ReservationService;
import hr.tvz.hotel.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Puni bazu demo podacima prilikom prvog pokretanja: sobe, goste,
 * rezervacije i račune. Podaci se dodaju kroz servise kako bi prošli
 * kroz svu poslovnu logiku (validacija, računanje cijene, log promjena).
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public final class DataSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);
    private static final Role SEED_ROLE = Role.ADMIN;

    private final RoomService roomService;
    private final GuestService guestService;
    private final ReservationService reservationService;
    private final InvoiceService invoiceService;

    /**
     * Kreira novi seeder demo podataka.
     *
     * @param context kontekst usluga aplikacije
     */
    public DataSeeder(ServiceContext context) {
        this.roomService = context.roomService();
        this.guestService = context.guestService();
        this.reservationService = context.reservationService();
        this.invoiceService = context.invoiceService();
    }

    /**
     * Puni bazu demo podacima ako još nema soba. Na sljedećim
     * pokretanjima ne radi ništa.
     */
    public void seedIfEmpty() {
        if (!roomService.findAll().isEmpty()) {
            return;
        }
        LOGGER.info("SEED START");
        seedRooms();
        seedGuests();
        seedReservationsAndInvoices();
        LOGGER.info("SEED GOTOV");
    }

    private void seedRooms() {
        addRoom("101", RoomType.SINGLE, "45.00", Capacity.SINGLE, RoomStatus.AVAILABLE);
        addRoom("102", RoomType.SINGLE, "45.00", Capacity.SINGLE, RoomStatus.MAINTENANCE);
        addRoom("103", RoomType.SINGLE, "50.00", Capacity.DOUBLE, RoomStatus.AVAILABLE);
        addRoom("201", RoomType.DOUBLE, "70.00", Capacity.DOUBLE, RoomStatus.AVAILABLE);
        addRoom("202", RoomType.DOUBLE, "75.00", Capacity.DOUBLE, RoomStatus.AVAILABLE);
        addRoom("203", RoomType.DOUBLE, "75.00", Capacity.TRIPLE, RoomStatus.RENOVATION);
        addRoom("301", RoomType.TWIN, "80.00", Capacity.DOUBLE, RoomStatus.AVAILABLE);
        addRoom("302", RoomType.TWIN, "85.00", Capacity.TRIPLE, RoomStatus.AVAILABLE);
        addRoom("401", RoomType.SUITE, "140.00", Capacity.TRIPLE, RoomStatus.AVAILABLE);
        addRoom("402", RoomType.SUITE, "160.00", Capacity.QUAD, RoomStatus.MAINTENANCE);
        addRoom("501", RoomType.DELUXE, "200.00", Capacity.DOUBLE, RoomStatus.AVAILABLE);
        addRoom("502", RoomType.DELUXE, "240.00", Capacity.QUAD, RoomStatus.AVAILABLE);
    }

    private void addRoom(String number, RoomType type, String price, Capacity capacity, RoomStatus status) {
        roomService.addRoom(new Room(null, number, type, new BigDecimal(price), capacity, status), SEED_ROLE);
    }

    private void seedGuests() {
        addGuest("Ivan", "Horvat", "ivan.horvat@email.hr", "0912345678", "HR1234567",
                "Ilica 5", "Zagreb", "10000", "Hrvatska");
        addGuest("Ana", "Kovač", "ana.kovac@email.hr", "0913456789", "HR2345678",
                "Vukovarska 12", "Split", "21000", "Hrvatska");
        addGuest("Marko", "Babić", "marko.babic@email.hr", "0914567890", "HR3456789",
                "Kralja Zvonimira 8", "Rijeka", "51000", "Hrvatska");
        addGuest("Petra", "Jurić", "petra.juric@email.hr", "0915678901", "HR4567890",
                "Zrinjevac 3", "Osijek", "31000", "Hrvatska");
        addGuest("Luka", "Novak", "luka.novak@email.hr", "0916789012", "HR5678901",
                "Tkalčićeva 20", "Zagreb", "10000", "Hrvatska");
        addGuest("Marija", "Marić", "marija.maric@email.hr", "0917890123", "HR6789012",
                "Riva 1", "Zadar", "23000", "Hrvatska");
        addGuest("Josip", "Knežević", "josip.knezevic@email.hr", "0918901234", "HR7890123",
                "Korzo 15", "Rijeka", "51000", "Hrvatska");
        addGuest("Ivana", "Vuković", "ivana.vukovic@email.hr", "0919012345", "HR8901234",
                "Gundulićeva 7", "Dubrovnik", "20000", "Hrvatska");
        addGuest("Tomislav", "Bošnjak", "tomislav.bosnjak@email.hr", "0910123456", "HR9012345",
                "Kaptol 2", "Zagreb", "10000", "Hrvatska");
        addGuest("Katarina", "Pavlović", "katarina.pavlovic@email.hr", "0911234560", "HR0123456",
                "Cvjetni trg 4", "Split", "21000", "Hrvatska");
    }

    private void addGuest(String first, String last, String email, String phone, String document,
                          String street, String city, String postal, String country) {
        Address address = new Address(street, city, postal, country);
        guestService.addGuest(new Guest(null, first, last, email, phone, document, address), SEED_ROLE);
    }

    private void seedReservationsAndInvoices() {
        List<Guest> guests = guestService.findAll();
        List<Room> rooms = roomService.findAll();

        // Prošlost (za povijest zarade i popunjenosti), sadašnjost i budućnost.
        book(guests, rooms, "101", 0, LocalDate.now(ZoneId.systemDefault()).minusMonths(3).withDayOfMonth(4),
                6, ReservationStatus.CONFIRMED, true, "CASH");
        book(guests, rooms, "201", 1, LocalDate.now(ZoneId.systemDefault()).minusMonths(3).withDayOfMonth(15),
                4, ReservationStatus.CONFIRMED, true, "CARD");
        book(guests, rooms, "501", 2, LocalDate.now(ZoneId.systemDefault()).minusMonths(2).withDayOfMonth(2),
                3, ReservationStatus.CONFIRMED, true, "CARD");
        book(guests, rooms, "301", 3, LocalDate.now(ZoneId.systemDefault()).minusMonths(2).withDayOfMonth(20),
                5, ReservationStatus.CONFIRMED, true, "CASH");
        book(guests, rooms, "202", 4, LocalDate.now(ZoneId.systemDefault()).minusMonths(1).withDayOfMonth(8),
                2, ReservationStatus.CONFIRMED, true, "CARD");
        book(guests, rooms, "401", 5, LocalDate.now(ZoneId.systemDefault()).minusMonths(1).withDayOfMonth(25),
                7, ReservationStatus.CONFIRMED, true, "CARD");
        book(guests, rooms, "103", 6, LocalDate.now(ZoneId.systemDefault()).minusMonths(1).withDayOfMonth(12),
                3, ReservationStatus.CANCELLED, false, null);
        book(guests, rooms, "502", 7, LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(3),
                4, ReservationStatus.CONFIRMED, true, "CARD");
        book(guests, rooms, "302", 8, LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(10),
                2, ReservationStatus.CONFIRMED, true, "CASH");
        book(guests, rooms, "201", 9, LocalDate.now(ZoneId.systemDefault()).plusDays(5),
                3, ReservationStatus.PENDING, false, null);
        book(guests, rooms, "501", 0, LocalDate.now(ZoneId.systemDefault()).plusMonths(1).withDayOfMonth(6),
                5, ReservationStatus.PENDING, false, null);
        book(guests, rooms, "401", 1, LocalDate.now(ZoneId.systemDefault()).plusMonths(1).withDayOfMonth(18),
                4, ReservationStatus.CONFIRMED, false, null);
        book(guests, rooms, "202", 2, LocalDate.now(ZoneId.systemDefault()).plusMonths(2).withDayOfMonth(2),
                6, ReservationStatus.PENDING, false, null);
        book(guests, rooms, "103", 3, LocalDate.now(ZoneId.systemDefault()).plusMonths(2).withDayOfMonth(22),
                3, ReservationStatus.CONFIRMED, false, null);
    }

    private void book(List<Guest> guests, List<Room> rooms, String roomNumber, int guestIndex,
                      LocalDate checkIn, int nights, ReservationStatus status, boolean withInvoice, String paymentType) {
        Room room = findRoom(rooms, roomNumber);
        Guest guest = guests.get(guestIndex % guests.size());
        try {
            Reservation reservation = reservationService.createReservation(
                    guest, room, checkIn, checkIn.plusDays(nights), SEED_ROLE);
            if (status != ReservationStatus.PENDING) {
                reservationService.changeStatus(reservation, status, SEED_ROLE);
            }
            if (withInvoice) {
                addInvoice(reservation, paymentType);
            }
        } catch (ReservationNotAvailableException e) {
            LOGGER.warn("seed rezervacija preskocena: {}", e.getMessage());
        }
    }

    private void addInvoice(Reservation reservation, String paymentType) {
        BigDecimal amount = reservation.getTotalPrice();
        PaymentMethod method = "CASH".equals(paymentType)
                ? new CashPayment(amount.add(new BigDecimal("20.00")))
                : new CardPayment("**** **** **** 1234", "AUTH" + reservation.getRoom().getRoomNumber());
        LocalDateTime issued = reservation.getCheckInDate().atStartOfDay();
        invoiceService.addInvoice(new Invoice(null, reservation, amount, method, issued), SEED_ROLE);
    }

    private Room findRoom(List<Room> rooms, String roomNumber) {
        return rooms.stream()
                .filter(r -> r.getRoomNumber().equals(roomNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("seed soba ne postoji: " + roomNumber));
    }
}
