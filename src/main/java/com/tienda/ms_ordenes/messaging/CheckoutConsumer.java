package com.tienda.ms_ordenes.messaging;

import com.rabbitmq.client.Channel;
import com.tienda.ms_ordenes.dto.CheckoutEvent;
import com.tienda.ms_ordenes.service.OrdenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Dominio ORDENES - consumidor de "carrito.checkout".
 * Solo recibe el mensaje y delega: la logica de negocio esta en OrdenService y el ACK en ConfirmacionMensajes.
 */
@Component
public class CheckoutConsumer {

    private static final Logger log = LoggerFactory.getLogger(CheckoutConsumer.class);

    private final OrdenService ordenService;
    private final ConfirmacionMensajes confirmacion;

    public CheckoutConsumer(OrdenService ordenService, ConfirmacionMensajes confirmacion) {
        this.ordenService = ordenService;
        this.confirmacion = confirmacion;
    }

    @RabbitListener(queues = "${app.rabbit.queues.checkout}", ackMode = "MANUAL")
    public void onCheckout(@Payload CheckoutEvent evento, Message mensaje, Channel canal) throws IOException {
        log.info("[RabbitMQ] carrito.checkout recibido de {}", evento.email());
        confirmacion.procesar(mensaje, canal, () -> {
            if (evento.usuarioId() == null || evento.items() == null || evento.items().isEmpty()) {
                throw new MensajeInvalidoException("Checkout sin usuario o sin items");
            }
            ordenService.crearDesdeCheckout(evento);
        });
    }
}
