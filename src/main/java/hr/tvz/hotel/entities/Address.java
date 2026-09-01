package hr.tvz.hotel.entities;

import java.io.Serializable;

/**
 * Predstavlja nepromjenjivu poštansku adresu gosta.
 *
 * @param street     naziv ulice i kućni broj
 * @param city       grad
 * @param postalCode poštanski broj
 * @param country    država
 */
public record Address(String street, String city, String postalCode, String country) implements Serializable {
}
