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

        double total = 0;
        for (CheckoutEvent.Item i : evento.items()) {
            orden.getItems().add(new OrdenItem(i.productoId(), i.nombre(), i.precio(), i.cantidad()));
            total += i.precio() * i.cantidad();
        }
        orden.setTotal(total);
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
