class AppUserSummary {
  final int id;
  final String nombre;
  final String email;
  final String rol;
  final String? createdAt;

  const AppUserSummary({
    required this.id,
    required this.nombre,
    required this.email,
    required this.rol,
    required this.createdAt,
  });

  factory AppUserSummary.fromJson(Map<String, dynamic> json) {
    return AppUserSummary(
      id: (json['id'] as num?)?.toInt() ?? 0,
      nombre: json['nombre'] as String? ?? '',
      email: json['email'] as String? ?? '',
      rol: json['rol'] as String? ?? '',
      createdAt: json['created_at'] as String?,
    );
  }
}