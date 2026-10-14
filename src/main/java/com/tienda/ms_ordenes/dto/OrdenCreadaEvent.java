package com.tienda.ms_ordenes.dto;

import java.util.List;

/** Evento "orden.creada" que consumen ms-catalogo, ms-notificaciones y ms-auditoria. */
public record OrdenCreadaEvent(
        Long ordenId,
        String usuarioId,
        String email,
        String nombre,
        double total,
        String fecha,
        List<Item> items) {

    public record Item(Long productoId, String nombre, double precio, int cantidad) {}
}
