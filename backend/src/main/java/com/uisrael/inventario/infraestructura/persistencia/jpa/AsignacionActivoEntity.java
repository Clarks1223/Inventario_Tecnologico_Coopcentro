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
@Table(name = "asignaciones_activo")
public class AsignacionActivoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idAsignacion;

	@ManyToOne
	@JoinColumn(name = "id_activo", foreignKey = @ForeignKey(name = "fk_asignaciones_activo_id_activo_activos"))
	private ActivoEntity activo;

	@ManyToOne
	@JoinColumn(name = "id_empleado", foreignKey = @ForeignKey(name = "fk_asignaciones_activo_id_empleado_empleados"))
	private EmpleadoEntity empleado;

	@Column(name = "fecha_asignacion")
	private LocalDateTime fechaAsignacion;

	@Column(name = "fecha_devolucion")
	private LocalDateTime fechaDevolucion;

	@Column(name = "estado_asignacion", length = 20)
	private String estadoAsignacion;

	@Column(name = "motivo", columnDefinition = "TEXT")
	private String motivo;

}
