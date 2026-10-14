package com.tienda.ms_ordenes.model;

import jakarta.persistence.*;

@Entity
@Table(name = "orden_items")
public class OrdenItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id")
    private Long productoId;

    private String nombre;
    private Double precio;
    private Integer cantidad;

    public OrdenItem() {}

    public OrdenItem(Long productoId, String nombre, Double precio, Integer cantidad) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public Long getId() { return id; }
    public Long getProductoId() { return productoId; }
    public String getNombre() { return nombre; }
    public Double getPrecio() { return precio; }
    public Integer getCantidad() { return cantidad; }
}
