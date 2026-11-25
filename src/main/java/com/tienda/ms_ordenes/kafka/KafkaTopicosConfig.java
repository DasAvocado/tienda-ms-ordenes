package com.tienda.ms_ordenes.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Topico que PRODUCE ms-ordenes (ORDEN_CREADA). */
@Configuration
public class KafkaTopicosConfig {

    @Bean
    public NewTopic topicoTransacciones(@Value("${app.kafka.topicos.transacciones.nombre}") String nombre,
                                        @Value("${app.kafka.topicos.transacciones.particiones}") int particiones,
                                        @Value("${app.kafka.replicas}") int replicas,
                                        @Value("${app.kafka.topicos.transacciones.retencion-ms}") long retencion,
                                        @Value("${app.kafka.topicos.transacciones.min-isr}") int minIsr) {
        return Topicos.crear(nombre, particiones, replicas, retencion, "delete", minIsr);
    }
}
