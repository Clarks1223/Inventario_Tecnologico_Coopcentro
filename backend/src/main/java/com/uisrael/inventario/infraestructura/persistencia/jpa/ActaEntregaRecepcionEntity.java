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
@Table(name = "acta_entrega_recepcion")
public class ActaEntregaRecepcionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idActa;

	@ManyToOne
	@JoinColumn(name = "id_activo", nullable = false, foreignKey = @ForeignKey(name = "fk_acta_entrega_recepcion_id_activo_activos"))
	private ActivoEntity activo;

	@ManyToOne
	@JoinColumn(name = "id_empleado", nullable = false, foreignKey = @ForeignKey(name = "fk_acta_entrega_recepcion_id_empleado_empleados"))
	private EmpleadoEntity empleado;

	@ManyToOne
	@JoinColumn(name = "id_usuario_ti", nullable = false, foreignKey = @ForeignKey(name = "fk_acta_entrega_recepcion_id_usuario_ti_usuarios_ti"))
	private UsuarioTiEntity usuarioTi;

	@Column(name = "fecha_asignacion")
	private LocalDateTime fechaAsignacion;

	@Column(name = "fecha_devolucion")
	private LocalDateTime fechaDevolucion;

	@Column(name = "estado_asignacion", length = 20)
	private String estadoAsignacion;

	@Column(name = "motivo", columnDefinition = "TEXT")
	private String motivo;

}
