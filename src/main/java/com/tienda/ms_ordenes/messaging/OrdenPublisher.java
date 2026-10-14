package com.tienda.ms_ordenes.messaging;

import com.tienda.ms_ordenes.dto.OrdenCreadaEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Productor de eventos del dominio ORDENES. Los servicios de negocio no conocen exchanges ni routing keys. */
@Component
public class OrdenPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String rkOrdenCreada;

    public OrdenPublisher(RabbitTemplate rabbitTemplate,
                          @Value("${app.rabbit.exchange}") String exchange,
                          @Value("${app.rabbit.routing-keys.orden-creada}") String rkOrdenCreada) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.rkOrdenCreada = rkOrdenCreada;
    }

    public void ordenCreada(OrdenCreadaEvent evento) {
        rabbitTemplate.convertAndSend(exchange, rkOrdenCreada, evento);
    }
}
