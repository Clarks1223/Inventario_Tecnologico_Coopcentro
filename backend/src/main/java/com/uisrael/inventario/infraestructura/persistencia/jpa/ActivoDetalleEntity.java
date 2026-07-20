package com.uisrael.inventario.infraestructura.persistencia.jpa;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

/**
 * PK compartida con ActivoEntity (@MapsId): id_activo siempre llega con
 * valor antes del primer guardado, asi que Spring Data no puede usar "id ==
 * null" para distinguir insert de update. Implementa Persistable para
 * decidirlo explicitamente y evitar que haga merge() sobre una fila que
 * todavia no existe.
 */
@Data
@Entity
@Table(name = "activo_detalle")
public class ActivoDetalleEntity implements Persistable<Integer> {

	@Id
	@Column(name = "id_activo")
	private Integer idActivo;

	@OneToOne
	@MapsId
	@JoinColumn(name = "id_activo", foreignKey = @ForeignKey(name = "fk_activo_detalle_id_activo_activos"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private ActivoEntity activo;

	@Transient
	private boolean nuevo = true;

	@Column(name = "tipo_conexion", length = 50)
	private String tipoConexion;

	@Column(name = "estado_bateria", length = 50)
	private String estadoBateria;

	@Column(name = "modelo_cabezal", length = 100)
	private String modeloCabezal;

	@Column(name = "tipo_dispositivo", length = 50)
	private String tipoDispositivo;

	@Column(name = "sistema_operativo", length = 100)
	private String sistemaOperativo;

	@Column(name = "imei", length = 50)
	private String imei;

	@Column(name = "numero_linea", length = 20)
	private String numeroLinea;

	@Column(name = "procesador", length = 100)
	private String procesador;

	@Column(name = "ram_gb")
	private Integer ramGb;

	@Column(name = "tipo_almacenamiento", length = 20)
	private String tipoAlmacenamiento;

	@Column(name = "ip", columnDefinition = "inet")
	@JdbcTypeCode(SqlTypes.INET)
	private String ip;

	@Column(name = "dominio", length = 150)
	private String dominio;

	@Column(name = "almacenamiento_gb")
	private Integer almacenamientoGb;

	@Override
	public Integer getId() {
		return idActivo;
	}

	@Override
	public boolean isNew() {
		return nuevo;
	}

	@PostLoad
	@PostPersist
	void marcarComoExistente() {
		this.nuevo = false;
	}

}
