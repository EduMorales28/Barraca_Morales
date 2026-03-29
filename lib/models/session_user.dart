class SessionUser {
  final int id;
  final String nombre;
  final String email;
  final String rol;
  final String token;

  const SessionUser({
    required this.id,
    required this.nombre,
    required this.email,
    required this.rol,
    required this.token,
  });

  bool get isAdmin => rol == 'admin';
  bool get isConductor => rol == 'conductor';
  bool get canCreatePedidos => rol == 'admin' || rol == 'creador_pedidos';

  factory SessionUser.fromJson(Map<String, dynamic> json) {
    return SessionUser(
      id: json['id'] as int,
      nombre: json['nombre'] as String? ?? '',
      email: json['email'] as String? ?? '',
      rol: json['rol'] as String? ?? '',
      token: json['token'] as String? ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'nombre': nombre,
      'email': email,
      'rol': rol,
      'token': token,
    };
  }
}