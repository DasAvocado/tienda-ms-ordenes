package com.tienda.ms_ordenes.service;

import com.tienda.ms_ordenes.dto.CheckoutEvent;
import com.tienda.ms_ordenes.dto.OrdenCreadaEvent;
import com.tienda.ms_ordenes.messaging.OrdenPublisher;
import com.tienda.ms_ordenes.model.Orden;
import com.tienda.ms_ordenes.model.OrdenItem;
import com.tienda.ms_ordenes.repository.OrdenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Logica de negocio de ordenes (sin detalles de RabbitMQ). */
@Service
public class OrdenService {

    private static final Logger log = LoggerFactory.getLogger(OrdenService.class);

    private final OrdenRepository ordenes;
    private final OrdenPublisher publisher;

    public OrdenService(OrdenRepository ordenes, OrdenPublisher publisher) {
        this.ordenes = ordenes;
        this.publisher = publisher;
    }

    /** Registra la orden formal a partir del checkout y anuncia "orden creada". */
    @Transactional
    public Orden crearDesdeCheckout(CheckoutEvent evento) {
        Orden orden = new Orden();
        orden.setUsuarioId(evento.usuarioId());
        orden.setEmail(evento.email());
        orden.setNombre(evento.nombre());
        orden.setEstado("CREADA");
        orden.setFecha(Instant.now());

        double subtotal = 0;
        for (CheckoutEvent.Item i : evento.items()) {
            orden.getItems().add(new OrdenItem(i.productoId(), i.nombre(), i.precio(), i.cantidad()));
            subtotal += i.precio() * i.cantidad();
        }
        // El total viene calculado por ms-carrito (descuento + envio) y ya fue pagado en ms-pagos.
        double total = evento.total() > 0 ? evento.total() : subtotal;
        orden.setSubtotal(subtotal);
        orden.setDescuento(evento.descuento());
        orden.setCostoEnvio(evento.costoEnvio());
        orden.setTotal(total);
        orden.setPagoId(evento.pagoId());
        orden.setMetodoPago(evento.metodoPago());
        orden.setCupon(evento.cupon());
        orden.setTipoEnvio(evento.tipoEnvio());
        if (evento.pagoId() != null) orden.setEstado("PAGADA");
        orden = ordenes.save(orden);
        log.info("Orden {} creada para {} (total {})", orden.getId(), evento.email(), total);

        List<OrdenCreadaEvent.Item> items = evento.items().stream()
                .map(i -> new OrdenCreadaEvent.Item(i.productoId(), i.nombre(), i.precio(), i.cantidad()))
                .toList();
        publisher.ordenCreada(new OrdenCreadaEvent(orden.getId(), orden.getUsuarioId(), orden.getEmail(),
                orden.getNombre(), total, orden.getFecha().toString(), items));
        return orden;
    }
}
