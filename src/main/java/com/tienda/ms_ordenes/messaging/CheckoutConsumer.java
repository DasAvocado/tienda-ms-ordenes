package com.tienda.ms_ordenes.messaging;

import com.tienda.ms_ordenes.config.RabbitConfig;
import com.tienda.ms_ordenes.dto.CheckoutEvent;
import com.tienda.ms_ordenes.dto.OrdenCreadaEvent;
import com.tienda.ms_ordenes.model.Orden;
import com.tienda.ms_ordenes.model.OrdenItem;
import com.tienda.ms_ordenes.repository.OrdenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** carrito.checkout -> registra la orden formal -> publica orden.creada */
@Component
public class CheckoutConsumer {

    private static final Logger log = LoggerFactory.getLogger(CheckoutConsumer.class);

    private final OrdenRepository ordenes;
    private final RabbitTemplate rabbitTemplate;

    public CheckoutConsumer(OrdenRepository ordenes, RabbitTemplate rabbitTemplate) {
        this.ordenes = ordenes;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_CHECKOUT)
    @Transactional
    public void onCheckout(CheckoutEvent evento) {
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
        log.info("[RabbitMQ] Orden {} creada para {} (total {})", orden.getId(), evento.email(), total);

        List<OrdenCreadaEvent.Item> items = evento.items().stream()
                .map(i -> new OrdenCreadaEvent.Item(i.productoId(), i.nombre(), i.precio(), i.cantidad()))
                .toList();

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_ORDEN_CREADA,
                new OrdenCreadaEvent(orden.getId(), orden.getUsuarioId(), orden.getEmail(), orden.getNombre(),
                        total, orden.getFecha().toString(), items));
    }
}
