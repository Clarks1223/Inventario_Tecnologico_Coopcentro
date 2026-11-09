package com.uisrael.inventario.infraestructura.persistencia.jpa;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "grupos_activos")
public class GrupoActivosEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idGrupo;

	@Column(name = "nombre", length = 100, nullable = false, unique = true)
	private String nombre;

	@Column(name = "descripcion", columnDefinition = "TEXT")
	private String descripcion;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

}
