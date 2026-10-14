package com.tienda.ms_ordenes.dto;

import java.util.List;

/** Evento "carrito.checkout" publicado por ms-carrito. */
public record CheckoutEvent(String usuarioId, String email, String nombre, List<Item> items) {

    public record Item(Long productoId, String nombre, double precio, int cantidad) {}
}
