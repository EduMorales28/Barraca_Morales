class AppNotification {
  final int id;
  final int? pedidoId;
  final String tipo;
  final String mensaje;
  final bool leida;
  final String createdAt;

  const AppNotification({
    required this.id,
    required this.pedidoId,
    required this.tipo,
    required this.mensaje,
    required this.leida,
    required this.createdAt,
  });

  factory AppNotification.fromJson(Map<String, dynamic> json) {
    return AppNotification(
      id: (json['id'] as num?)?.toInt() ?? 0,
      pedidoId: (json['pedido_id'] as num?)?.toInt(),
      tipo: json['tipo'] as String? ?? '',
      mensaje: json['mensaje'] as String? ?? '',
      leida: (json['leida'] as num?)?.toInt() == 1,
      createdAt: json['created_at'] as String? ?? '',
    );
  }
}