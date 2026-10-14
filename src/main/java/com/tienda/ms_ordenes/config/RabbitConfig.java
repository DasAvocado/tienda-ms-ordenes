package com.tienda.ms_ordenes.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** ms-ordenes CONSUME "carrito.checkout" y PUBLICA "orden.creada". */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pedidos360.events";
    public static final String RK_CHECKOUT = "carrito.checkout";
    public static final String RK_ORDEN_CREADA = "orden.creada";
    public static final String QUEUE_CHECKOUT = "ordenes.checkout";

    @Bean
    public TopicExchange eventosExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue colaCheckout() {
        return QueueBuilder.durable(QUEUE_CHECKOUT).build();
    }

    @Bean
    public Binding bindingCheckout() {
        return BindingBuilder.bind(colaCheckout()).to(eventosExchange()).with(RK_CHECKOUT);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
