class PedidoItemModel {
  final int id;
  final String? codigo;
  final String nombre;
  final int cantidad;
  final double precio;

  const PedidoItemModel({
    required this.id,
    required this.codigo,
    required this.nombre,
    required this.cantidad,
    required this.precio,
  });

  factory PedidoItemModel.fromJson(Map<String, dynamic> json) {
    return PedidoItemModel(
      id: (json['id'] as num?)?.toInt() ?? 0,
      codigo: json['codigo'] as String?,
      nombre: json['nombre'] as String? ?? '',
      cantidad: (json['cantidad'] as num?)?.toInt() ?? 0,
      precio: (json['precio'] as num?)?.toDouble() ?? 0,
    );
  }
}

class PedidoModel {
  final int id;
  final String cliente;
  final String direccion;
  final String estado;
  final int? conductorId;
  final String? conductorNombre;
  final String? creadorNombre;
  final String levantado;
  final String? levantadoEnMostrador;
  final bool sinLevantadoMostrador;
  final String? acceptedAt;
  final String? rejectedReason;
  final String? rejectedAt;
  final String? createdAt;
  final List<PedidoItemModel> items;

  const PedidoModel({
    required this.id,
    required this.cliente,
    required this.direccion,
    required this.estado,
    required this.conductorId,
    required this.conductorNombre,
    required this.creadorNombre,
    required this.levantado,
    required this.levantadoEnMostrador,
    required this.sinLevantadoMostrador,
    required this.acceptedAt,
    required this.rejectedReason,
    required this.rejectedAt,
    required this.createdAt,
    required this.items,
  });

  factory PedidoModel.fromJson(Map<String, dynamic> json) {
    final rawItems = json['items'] as List<dynamic>? ?? const [];

    return PedidoModel(
      id: (json['id'] as num?)?.toInt() ?? 0,
      cliente: json['cliente'] as String? ?? '',
      direccion: json['direccion'] as String? ?? '',
      estado: json['estado'] as String? ?? 'pendiente',
      conductorId: (json['conductor_id'] as num?)?.toInt(),
      conductorNombre: json['conductor_nombre'] as String?,
      creadorNombre: json['creador_nombre'] as String?,
      levantado: json['levantado'] as String? ?? 'con_mostrador',
      levantadoEnMostrador: json['levantado_en_mostrador'] as String?,
      sinLevantadoMostrador: (json['sin_levantado_mostrador'] as num?)?.toInt() == 1,
      acceptedAt: json['accepted_at'] as String?,
      rejectedReason: json['rejected_reason'] as String?,
      rejectedAt: json['rejected_at'] as String?,
      createdAt: json['created_at'] as String?,
      items: rawItems.map((item) => PedidoItemModel.fromJson(item as Map<String, dynamic>)).toList(),
    );
  }
}