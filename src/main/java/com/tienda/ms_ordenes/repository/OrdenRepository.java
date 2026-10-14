package com.tienda.ms_ordenes.repository;

import com.tienda.ms_ordenes.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    List<Orden> findByUsuarioIdOrderByFechaDesc(String usuarioId);

    List<Orden> findAllByOrderByFechaDesc();
}
