package com.tienda.ms_ordenes.controller;

import com.tienda.ms_ordenes.dto.OrdenDto;
import com.tienda.ms_ordenes.repository.OrdenRepository;
import com.tienda.ms_ordenes.security.Seguridad;
import com.tienda.ms_ordenes.security.UsuarioActual;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *   GET /api/ordenes         -> mis ordenes                       (CLIENTE / ADMIN)
 *   GET /api/ordenes/{id}    -> una de mis ordenes (ADMIN: cualquiera)
 *   GET /api/ordenes/todas   -> todas las ordenes                 (solo ADMIN)
 */
@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenRepository ordenes;

    public OrdenController(OrdenRepository ordenes) {
        this.ordenes = ordenes;
    }

    @GetMapping
    @PreAuthorize(Seguridad.CLIENTE)
    public List<OrdenDto> misOrdenes(@AuthenticationPrincipal Jwt jwt) {
        return ordenes.findByUsuarioIdOrderByFechaDesc(UsuarioActual.id(jwt)).stream().map(OrdenDto::desde).toList();
    }

    @GetMapping("/todas")
    @PreAuthorize(Seguridad.ADMIN)
    public List<OrdenDto> todas() {
        return ordenes.findAllByOrderByFechaDesc().stream().map(OrdenDto::desde).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize(Seguridad.CLIENTE)
    public ResponseEntity<OrdenDto> porId(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        String uid = UsuarioActual.id(jwt);
        List<String> roles = jwt.getClaimAsStringList("roles");
        boolean admin = roles != null && roles.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"));
        return ordenes.findById(id)
                .filter(o -> admin || o.getUsuarioId().equals(uid))
                .map(o -> ResponseEntity.ok(OrdenDto.desde(o)))
                .orElse(ResponseEntity.notFound().build());
    }
}
