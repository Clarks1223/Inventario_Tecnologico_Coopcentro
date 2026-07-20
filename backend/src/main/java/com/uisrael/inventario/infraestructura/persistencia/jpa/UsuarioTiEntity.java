package com.uisrael.inventario.infraestructura.persistencia.jpa;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios_ti")
public class UsuarioTiEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idUsuarioTi;

	@OneToOne
	@JoinColumn(name = "id_empleado", unique = true, nullable = false, foreignKey = @ForeignKey(name = "fk_usuarios_ti_id_empleado_empleados"))
	private EmpleadoEntity empleado;

	@Column(name = "correo", length = 254, unique = true, nullable = false)
	private String correo;

	@Column(name = "contrasena", length = 255, nullable = false)
	private String contrasena;

	@Column(name = "fecha_creacion", nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(name = "fecha_actualizacion", nullable = false)
	private LocalDateTime fechaActualizacion;

}
