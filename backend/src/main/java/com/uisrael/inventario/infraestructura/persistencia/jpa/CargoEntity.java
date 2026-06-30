package com.uisrael.inventario.infraestructura.persistencia.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "cargos")
public class CargoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idCargo;

	@Column(name = "nombre", length = 150)
	private String nombre;

	private boolean activo;

}
