package com.tienda.ms_ordenes.messaging;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Verifica las decisiones ACK / reintento / NACK->DLQ. */
class ConfirmacionMensajesTest {

    private RabbitTemplate rabbitTemplate;
    private Channel canal;
    private ConfirmacionMensajes confirmacion;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        canal = mock(Channel.class);
        confirmacion = new ConfirmacionMensajes(rabbitTemplate, 3);
    }

    private static Message mensaje(Integer reintentos) {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(7L);
        props.setConsumerQueue("ordenes.checkout");
        props.setReceivedRoutingKey("carrito.checkout");
        if (reintentos != null) props.setHeader(ConfirmacionMensajes.HEADER_REINTENTOS, reintentos);
        return new Message("{}".getBytes(), props);
    }

    @Test
    void exitoHaceAck() throws Exception {
        confirmacion.procesar(mensaje(null), canal, () -> { });
        verify(canal).basicAck(7L, false);
        verify(canal, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void mensajeInvalidoVaDirectoADlq() throws Exception {
        confirmacion.procesar(mensaje(null), canal, () -> { throw new MensajeInvalidoException("malo"); });
        verify(canal).basicNack(7L, false, false);
        verify(rabbitTemplate, never()).send(anyString(), anyString(), any(Message.class));
    }

    @Test
    void errorRecuperableSeReintenta() throws Exception {
        confirmacion.procesar(mensaje(1), canal, () -> { throw new IllegalStateException("BD caida"); });
        verify(rabbitTemplate).send(eq(""), eq("ordenes.checkout"), any(Message.class));
        verify(canal).basicAck(7L, false);
    }

    @Test
    void reintentosAgotadosVaADlq() throws Exception {
        confirmacion.procesar(mensaje(3), canal, () -> { throw new IllegalStateException("BD caida"); });
        verify(canal).basicNack(7L, false, false);
        verify(rabbitTemplate, never()).send(anyString(), anyString(), any(Message.class));
    }
}
