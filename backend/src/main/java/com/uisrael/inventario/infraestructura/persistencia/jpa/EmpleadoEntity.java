package com.uisrael.inventario.infraestructura.persistencia.jpa;

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
@Table(name = "empleados")
public class EmpleadoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idEmpleado;

	@Column(name = "nombre", length = 100)
	private String nombre;

	@Column(name = "apellido", length = 100)
	private String apellido;

	@Column(name = "cedula", length = 20)
	private String cedula;

	@Column(name = "correo", length = 254)
	private String correo;

	@Column(name = "extension_telefonica", length = 20)
	private String extensionTelefonica;

	@ManyToOne
	@JoinColumn(name = "id_oficina", foreignKey = @ForeignKey(name = "fk_empleados_id_oficina_oficinas"))
	private OficinaEntity oficina;

	@ManyToOne
	@JoinColumn(name = "id_cargo", foreignKey = @ForeignKey(name = "fk_empleados_id_cargo_cargos"))
	private CargoEntity cargo;

	@Column(name = "numero_telefono", length = 20)
	private String numeroTelefono;

	private boolean activo;

}
