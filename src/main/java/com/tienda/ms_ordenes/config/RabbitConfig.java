package com.tienda.ms_ordenes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia RabbitMQ de ms-ordenes. Los NOMBRES se definen solo en application.yml (app.rabbit.*).
 *
 *   [pedidos360.events] --carrito.checkout--> ordenes.checkout      (consume CheckoutConsumer)
 *   ordenes.checkout --(rechazo/reintentos agotados)--> [pedidos360.dlx] --> ordenes.checkout.dlq
 *   ms-ordenes PUBLICA orden.creada en [pedidos360.events] (OrdenPublisher)
 */
@Configuration
public class RabbitConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitConfig.class);

    private final String exchange;
    private final String dlx;
    private final String rkCheckout;
    private final String colaCheckout;
    private final String colaCheckoutDlq;

    public RabbitConfig(@Value("${app.rabbit.exchange}") String exchange,
                        @Value("${app.rabbit.dlx}") String dlx,
                        @Value("${app.rabbit.routing-keys.checkout}") String rkCheckout,
                        @Value("${app.rabbit.queues.checkout}") String colaCheckout,
                        @Value("${app.rabbit.queues.checkout-dlq}") String colaCheckoutDlq) {
        this.exchange = exchange;
        this.dlx = dlx;
        this.rkCheckout = rkCheckout;
        this.colaCheckout = colaCheckout;
        this.colaCheckoutDlq = colaCheckoutDlq;
    }

    // ---------- Exchanges ----------
    @Bean
    public TopicExchange eventosExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlx, true, false);
    }

    // ---------- Caso de uso: crear orden desde el checkout ----------
    @Bean
    public Queue colaCheckout() {
        return QueueBuilder.durable(colaCheckout)
                .deadLetterExchange(dlx)
                .deadLetterRoutingKey(colaCheckoutDlq)
                .build();
    }

    @Bean
    public Binding bindingCheckout() {
        return BindingBuilder.bind(colaCheckout()).to(eventosExchange()).with(rkCheckout);
    }

    @Bean
    public Queue colaCheckoutDlq() {
        return QueueBuilder.durable(colaCheckoutDlq).build();
    }

    @Bean
    public Binding bindingCheckoutDlq() {
        return BindingBuilder.bind(colaCheckoutDlq()).to(deadLetterExchange()).with(colaCheckoutDlq);
    }

    // ---------- Infraestructura comun ----------
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /** RabbitTemplate con confirmaciones del broker: registra en log los mensajes NO entregados. */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        template.setMandatory(true);
        template.setConfirmCallback((correlacion, ack, causa) -> {
            if (!ack) log.error("[RabbitMQ] El broker NO confirmo el mensaje: {}", causa);
        });
        template.setReturnsCallback(r -> log.error(
                "[RabbitMQ] Mensaje no enrutable (sin cola destino): exchange={} routingKey={} motivo={}",
                r.getExchange(), r.getRoutingKey(), r.getReplyText()));
        return template;
    }
}
