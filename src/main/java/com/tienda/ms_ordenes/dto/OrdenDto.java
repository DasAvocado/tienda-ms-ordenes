package com.tienda.ms_ordenes.dto;

import com.tienda.ms_ordenes.model.Orden;

import java.util.List;

/** Respuesta publica de una orden (evita exponer entidades JPA directamente). */
public record OrdenDto(Long id, String usuarioId, String email, String estado, double total, String fecha, List<Item> items,
                       Long pagoId, String metodoPago, double subtotal, double descuento, double costoEnvio,
                       String cupon, String tipoEnvio) {

    public record Item(Long productoId, String nombre, double precio, int cantidad) {}

    public static OrdenDto desde(Orden o) {
        List<Item> items = o.getItems().stream()
                .map(i -> new Item(i.getProductoId(), i.getNombre(), i.getPrecio(), i.getCantidad()))
                .toList();
        double subtotal = o.getSubtotal() != null ? o.getSubtotal()
                : items.stream().mapToDouble(i -> i.precio() * i.cantidad()).sum();
        return new OrdenDto(o.getId(), o.getUsuarioId(), o.getEmail(), o.getEstado(), o.getTotal(),
                o.getFecha().toString(), items, o.getPagoId(), o.getMetodoPago(), subtotal,
                valor(o.getDescuento()), valor(o.getCostoEnvio()), o.getCupon(), o.getTipoEnvio());
    }

    private static double valor(Double d) {
        return d == null ? 0 : d;
    }
}
