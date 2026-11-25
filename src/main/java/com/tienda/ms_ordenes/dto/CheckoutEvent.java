package com.tienda.ms_ordenes.dto;

import java.util.List;

/** Evento "carrito.checkout" publicado por ms-carrito (tras el pago aprobado en ms-pagos). */
public record CheckoutEvent(
        String usuarioId,
        String email,
        String nombre,
        List<Item> items,
        Long pagoId,
        String metodoPago,
        double subtotal,
        double descuento,
        double costoEnvio,
        double total,
        String cupon,
        String tipoEnvio) {

    /** Compatibilidad con el evento de la version anterior (sin pago ni totales). */
    public CheckoutEvent(String usuarioId, String email, String nombre, List<Item> items) {
        this(usuarioId, email, nombre, items, null, null, 0, 0, 0, 0, null, null);
    }

    public record Item(Long productoId, String nombre, double precio, int cantidad) {}
}
