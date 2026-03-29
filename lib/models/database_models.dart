// Modelos Dart para Logística Barraca

// ============================================================================
// USUARIO
// ============================================================================
class Usuario {
  final String id;
  final String nombre;
  final String email;
  final String? telefono;
  final TipoRol rol;
  final EstadoUsuario estado;
  final String? fotoUrl;
  final DateTime createdAt;
  final DateTime updatedAt;

  Usuario({
    required this.id,
    required this.nombre,
    required this.email,
    this.telefono,
    required this.rol,
    required this.estado,
    this.fotoUrl,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Usuario.fromJson(Map<String, dynamic> json) {
    return Usuario(
      id: json['id'],
      nombre: json['nombre'],
      email: json['email'],
      telefono: json['telefono'],
      rol: TipoRol.values.firstWhere(
        (e) => e.toString().split('.').last == json['rol'],
      ),
      estado: EstadoUsuario.values.firstWhere(
        (e) => e.toString().split('.').last == json['estado'],
      ),
      fotoUrl: json['fotoUrl'],
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: DateTime.parse(json['updatedAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'nombre': nombre,
      'email': email,
      'telefono': telefono,
      'rol': rol.toString().split('.').last,
      'estado': estado.toString().split('.').last,
      'fotoUrl': fotoUrl,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
    };
  }
}

enum TipoRol { admin, conductor }
enum EstadoUsuario { activo, inactivo }

// ============================================================================
// CLIENTE
// ============================================================================
class Cliente {
  final String id;
  final String nombre;
  final String? email;
  final String? telefono;
  final String? razonSocial;
  final String? ruc;
  final String? direccionDefault;
  final EstadoCliente estado;
  final DateTime createdAt;
  final DateTime updatedAt;

  Cliente({
    required this.id,
    required this.nombre,
    this.email,
    this.telefono,
    this.razonSocial,
    this.ruc,
    this.direccionDefault,
    required this.estado,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Cliente.fromJson(Map<String, dynamic> json) {
    return Cliente(
      id: json['id'],
      nombre: json['nombre'],
      email: json['email'],
      telefono: json['telefono'],
      razonSocial: json['razonSocial'],
      ruc: json['ruc'],
      direccionDefault: json['direccionDefault'],
      estado: EstadoCliente.values.firstWhere(
        (e) => e.toString().split('.').last == json['estado'],
      ),
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: DateTime.parse(json['updatedAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'nombre': nombre,
      'email': email,
      'telefono': telefono,
      'razonSocial': razonSocial,
      'ruc': ruc,
      'direccionDefault': direccionDefault,
      'estado': estado.toString().split('.').last,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
    };
  }
}

enum EstadoCliente { activo, inactivo }

// ============================================================================
// PEDIDO
// ============================================================================
class Pedido {
  final String id;
  final String numeroPedido;
  final String clienteId;
  final String? conductorId;
  final String direccion;
  final String? referencia;
  final double? latitud;
  final double? longitud;
  final double montoTotal;
  final double montoLevantado;
  final EstadoPedido estado;
  final bool levantadoTotal;
  final double porcentajeLevantado;
  final String? observaciones;
  final DateTime fechaCreacion;
  final DateTime? fechaAsignacion;
  final DateTime? fechaEntregaEstimada;
  final DateTime? fechaEntregaReal;
  final DateTime updatedAt;
  
  // Datos calculados
  final List<ItemPedido>? items;
  final Cliente? cliente;
  final Usuario? conductor;

  Pedido({
    required this.id,
    required this.numeroPedido,
    required this.clienteId,
    this.conductorId,
    required this.direccion,
    this.referencia,
    this.latitud,
    this.longitud,
    required this.montoTotal,
    required this.montoLevantado,
    required this.estado,
    required this.levantadoTotal,
    required this.porcentajeLevantado,
    this.observaciones,
    required this.fechaCreacion,
    this.fechaAsignacion,
    this.fechaEntregaEstimada,
    this.fechaEntregaReal,
    required this.updatedAt,
    this.items,
    this.cliente,
    this.conductor,
  });

  factory Pedido.fromJson(Map<String, dynamic> json) {
    return Pedido(
      id: json['id'],
      numeroPedido: json['numeroPedido'],
      clienteId: json['clienteId'],
      conductorId: json['conductorId'],
      direccion: json['direccion'],
      referencia: json['referencia'],
      latitud: json['latitud']?.toDouble(),
      longitud: json['longitud']?.toDouble(),
      montoTotal: (json['montoTotal'] ?? 0).toDouble(),
      montoLevantado: (json['montoLevantado'] ?? 0).toDouble(),
      estado: EstadoPedido.values.firstWhere(
        (e) => e.toString().split('.').last == json['estado'],
      ),
      levantadoTotal: json['levantadoTotal'] ?? false,
      porcentajeLevantado: (json['porcentajeLevantado'] ?? 0).toDouble(),
      observaciones: json['observaciones'],
      fechaCreacion: DateTime.parse(json['fechaCreacion']),
      fechaAsignacion: json['fechaAsignacion'] != null 
        ? DateTime.parse(json['fechaAsignacion']) 
        : null,
      fechaEntregaEstimada: json['fechaEntregaEstimada'] != null 
        ? DateTime.parse(json['fechaEntregaEstimada']) 
        : null,
      fechaEntregaReal: json['fechaEntregaReal'] != null 
        ? DateTime.parse(json['fechaEntregaReal']) 
        : null,
      updatedAt: DateTime.parse(json['updatedAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'numeroPedido': numeroPedido,
      'clienteId': clienteId,
      'conductorId': conductorId,
      'direccion': direccion,
      'referencia': referencia,
      'latitud': latitud,
      'longitud': longitud,
      'montoTotal': montoTotal,
      'montoLevantado': montoLevantado,
      'estado': estado.toString().split('.').last,
      'levantadoTotal': levantadoTotal,
      'porcentajeLevantado': porcentajeLevantado,
      'observaciones': observaciones,
      'fechaCreacion': fechaCreacion.toIso8601String(),
      'fechaAsignacion': fechaAsignacion?.toIso8601String(),
      'fechaEntregaEstimada': fechaEntregaEstimada?.toIso8601String(),
      'fechaEntregaReal': fechaEntregaReal?.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
    };
  }
}

enum EstadoPedido { 
  pendiente, 
  asignado, 
  enRuta, 
  parcial, 
  completado, 
  cancelado 
}

// ============================================================================
// ITEM PEDIDO
// ============================================================================
class ItemPedido {
  final String id;
  final String pedidoId;
  final String descripcion;
  final double cantidad;
  final double precioUnitario;
  final double subtotal;
  final double cantidadLevantada;
  final EstadoItem estado;
  final DateTime createdAt;
  final DateTime updatedAt;

  ItemPedido({
    required this.id,
    required this.pedidoId,
    required this.descripcion,
    required this.cantidad,
    required this.precioUnitario,
    required this.subtotal,
    required this.cantidadLevantada,
    required this.estado,
    required this.createdAt,
    required this.updatedAt,
  });

  factory ItemPedido.fromJson(Map<String, dynamic> json) {
    return ItemPedido(
      id: json['id'],
      pedidoId: json['pedidoId'],
      descripcion: json['descripcion'],
      cantidad: (json['cantidad'] ?? 0).toDouble(),
      precioUnitario: (json['precioUnitario'] ?? 0).toDouble(),
      subtotal: (json['subtotal'] ?? 0).toDouble(),
      cantidadLevantada: (json['cantidadLevantada'] ?? 0).toDouble(),
      estado: EstadoItem.values.firstWhere(
        (e) => e.toString().split('.').last == json['estado'],
      ),
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: DateTime.parse(json['updatedAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'pedidoId': pedidoId,
      'descripcion': descripcion,
      'cantidad': cantidad,
      'precioUnitario': precioUnitario,
      'subtotal': subtotal,
      'cantidadLevantada': cantidadLevantada,
      'estado': estado.toString().split('.').last,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
    };
  }
}

enum EstadoItem { pendiente, parcial, completado }

// ============================================================================
// ENTREGA
// ============================================================================
class Entrega {
  final String id;
  final String pedidoId;
  final String? itemPedidoId;
  final String conductorId;
  final double? cantidadLevantada;
  final String? fotoUrl;
  final String? fotoFirmaUrl;
  final String? observaciones;
  final EstadoEntrega estado;
  final String? recibidoPor;
  final String? dniRecibidor;
  final double? latitud;
  final double? longitud;
  final DateTime? fechaProgramada;
  final DateTime? fechaEntrega;
  final DateTime? horaLlegada;
  final DateTime? horaSalida;
  final DateTime createdAt;
  final DateTime updatedAt;

  Entrega({
    required this.id,
    required this.pedidoId,
    this.itemPedidoId,
    required this.conductorId,
    this.cantidadLevantada,
    this.fotoUrl,
    this.fotoFirmaUrl,
    this.observaciones,
    required this.estado,
    this.recibidoPor,
    this.dniRecibidor,
    this.latitud,
    this.longitud,
    this.fechaProgramada,
    this.fechaEntrega,
    this.horaLlegada,
    this.horaSalida,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Entrega.fromJson(Map<String, dynamic> json) {
    return Entrega(
      id: json['id'],
      pedidoId: json['pedidoId'],
      itemPedidoId: json['itemPedidoId'],
      conductorId: json['conductorId'],
      cantidadLevantada: json['cantidadLevantada']?.toDouble(),
      fotoUrl: json['fotoUrl'],
      fotoFirmaUrl: json['fotoFirmaUrl'],
      observaciones: json['observaciones'],
      estado: EstadoEntrega.values.firstWhere(
        (e) => e.toString().split('.').last == json['estado'],
      ),
      recibidoPor: json['recibidoPor'],
      dniRecibidor: json['dniRecibidor'],
      latitud: json['latitud']?.toDouble(),
      longitud: json['longitud']?.toDouble(),
      fechaProgramada: json['fechaProgramada'] != null
        ? DateTime.parse(json['fechaProgramada'])
        : null,
      fechaEntrega: json['fechaEntrega'] != null
        ? DateTime.parse(json['fechaEntrega'])
        : null,
      horaLlegada: json['horaLlegada'] != null
        ? DateTime.parse(json['horaLlegada'])
        : null,
      horaSalida: json['horaSalida'] != null
        ? DateTime.parse(json['horaSalida'])
        : null,
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: DateTime.parse(json['updatedAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'pedidoId': pedidoId,
      'itemPedidoId': itemPedidoId,
      'conductorId': conductorId,
      'cantidadLevantada': cantidadLevantada,
      'fotoUrl': fotoUrl,
      'fotoFirmaUrl': fotoFirmaUrl,
      'observaciones': observaciones,
      'estado': estado.toString().split('.').last,
      'recibidoPor': recibidoPor,
      'dniRecibidor': dniRecibidor,
      'latitud': latitud,
      'longitud': longitud,
      'fechaProgramada': fechaProgramada?.toIso8601String(),
      'fechaEntrega': fechaEntrega?.toIso8601String(),
      'horaLlegada': horaLlegada?.toIso8601String(),
      'horaSalida': horaSalida?.toIso8601String(),
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
    };
  }
}

enum EstadoEntrega { planeada, enRuta, completada, rechazada }

// ============================================================================
// SEGUIMIENTO GPS
// ============================================================================
class SeguimientoGPS {
  final String id;
  final String conductorId;
  final String? pedidoId;
  final double latitud;
  final double longitud;
  final double? precision;
  final double? velocidad;
  final DateTime createdAt;

  SeguimientoGPS({
    required this.id,
    required this.conductorId,
    this.pedidoId,
    required this.latitud,
    required this.longitud,
    this.precision,
    this.velocidad,
    required this.createdAt,
  });

  factory SeguimientoGPS.fromJson(Map<String, dynamic> json) {
    return SeguimientoGPS(
      id: json['id'],
      conductorId: json['conductorId'],
      pedidoId: json['pedidoId'],
      latitud: (json['latitud'] ?? 0).toDouble(),
      longitud: (json['longitud'] ?? 0).toDouble(),
      precision: json['precision']?.toDouble(),
      velocidad: json['velocidad']?.toDouble(),
      createdAt: DateTime.parse(json['createdAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'conductorId': conductorId,
      'pedidoId': pedidoId,
      'latitud': latitud,
      'longitud': longitud,
      'precision': precision,
      'velocidad': velocidad,
      'createdAt': createdAt.toIso8601String(),
    };
  }
}
