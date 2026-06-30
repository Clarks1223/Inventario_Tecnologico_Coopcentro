package com.uisrael.inventario.infraestructura.persistencia.jpa;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "activos")
public class ActivoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idActivo;

	@Column(name = "codigo_inventario", length = 50)
	private String codigoInventario;

	@Column(name = "tipo_activo", length = 50)
	private String tipoActivo;

	@Column(name = "marca", length = 100)
	private String marca;

	@Column(name = "modelo", length = 100)
	private String modelo;

	@Column(name = "serial", length = 100)
	private String serial;

	@Column(name = "estado", length = 50)
	private String estado;

	@ManyToOne
	@JoinColumn(name = "id_oficina", foreignKey = @ForeignKey(name = "fk_activos_id_oficina_oficinas"))
	private OficinaEntity oficina;

	@Column(name = "observaciones", columnDefinition = "TEXT")
	private String observaciones;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

}
