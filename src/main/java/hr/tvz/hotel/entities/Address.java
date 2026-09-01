package hr.tvz.hotel.entities;

import java.io.Serializable;

/**
 * Nepromjenjiva poštanska adresa gosta.
 *
 * @param street naziv ulice i kućni broj
 * @param city grad
 * @param postalCode poštanski broj
 * @param country država
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public record Address(String street, String city, String postalCode, String country) implements Serializable {
}
