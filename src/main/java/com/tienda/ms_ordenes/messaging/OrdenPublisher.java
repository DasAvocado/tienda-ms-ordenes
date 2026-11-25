package com.tienda.ms_ordenes.messaging;

import com.tienda.ms_ordenes.dto.EventoTransaccion;
import com.tienda.ms_ordenes.dto.OrdenCreadaEvent;
import com.tienda.ms_ordenes.kafka.EventoKafkaPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Productor de eventos del dominio ORDENES. Los servicios de negocio no conocen exchanges ni topicos.
 *  - RabbitMQ "orden.creada": COMANDOS para descontar stock (ms-catalogo) y enviar el correo (ms-notificaciones).
 *  - Kafka "pedidos360.transacciones" ORDEN_CREADA: HECHO para auditoria y metricas (ms-auditoria, ms-recomendaciones).
 */
@Component
public class OrdenPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final EventoKafkaPublisher kafka;
    private final String exchange;
    private final String rkOrdenCreada;
    private final String topicoTransacciones;

    public OrdenPublisher(RabbitTemplate rabbitTemplate, EventoKafkaPublisher kafka,
                          @Value("${app.rabbit.exchange}") String exchange,
                          @Value("${app.rabbit.routing-keys.orden-creada}") String rkOrdenCreada,
                          @Value("${app.kafka.topicos.transacciones.nombre}") String topicoTransacciones) {
        this.rabbitTemplate = rabbitTemplate;
        this.kafka = kafka;
        this.exchange = exchange;
        this.rkOrdenCreada = rkOrdenCreada;
        this.topicoTransacciones = topicoTransacciones;
    }

    public void ordenCreada(OrdenCreadaEvent evento) {
        rabbitTemplate.convertAndSend(exchange, rkOrdenCreada, evento);
        kafka.publicar(topicoTransacciones, evento.usuarioId(), new EventoTransaccion(UUID.randomUUID().toString(),
                "ORDEN_CREADA", evento.usuarioId(), evento.ordenId(), null, evento.total(), evento.items().size(),
                "email=" + evento.email(), evento.fecha()));
    }
}
