package com.tienda.ms_ordenes.service;

import com.tienda.ms_ordenes.dto.CheckoutEvent;
import com.tienda.ms_ordenes.dto.OrdenCreadaEvent;
import com.tienda.ms_ordenes.messaging.OrdenPublisher;
import com.tienda.ms_ordenes.model.Orden;
import com.tienda.ms_ordenes.repository.OrdenRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrdenServiceTest {

    @Test
    void creaOrdenYPublicaOrdenCreada() {
        OrdenRepository repo = mock(OrdenRepository.class);
        OrdenPublisher publisher = mock(OrdenPublisher.class);
        when(repo.save(any(Orden.class))).thenAnswer(inv -> {
            Orden o = inv.getArgument(0);
            o.setId(10L);
            return o;
        });

        OrdenService service = new OrdenService(repo, publisher);
        Orden orden = service.crearDesdeCheckout(new CheckoutEvent("u1", "a@b.cl", "Ana",
                List.of(new CheckoutEvent.Item(1L, "Filtro", 15000, 2), new CheckoutEvent.Item(2L, "Bateria", 85000, 1))));

        assertEquals(115000.0, orden.getTotal(), 0.001);
        ArgumentCaptor<OrdenCreadaEvent> evento = ArgumentCaptor.forClass(OrdenCreadaEvent.class);
        verify(publisher).ordenCreada(evento.capture());
        assertEquals(10L, evento.getValue().ordenId());
        assertEquals("a@b.cl", evento.getValue().email());
        assertEquals(2, evento.getValue().items().size());
    }
}
