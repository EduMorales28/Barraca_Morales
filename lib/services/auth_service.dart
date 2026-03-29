import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:logistica_morales/models/app_notification.dart';
import 'package:logistica_morales/models/app_user_summary.dart';
import 'package:logistica_morales/models/pedido_model.dart';
import 'package:logistica_morales/models/session_user.dart';
import 'package:logistica_morales/services/api_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AuthService {
  AuthService(this._apiService);

  final ApiService _apiService;
  static const _sessionKey = 'app_session_user';

  Future<SessionUser?> restoreSession() async {
    final preferences = await SharedPreferences.getInstance();
    final rawSession = preferences.getString(_sessionKey);

    if (rawSession == null || rawSession.isEmpty) {
      _apiService.setToken(null);
      return null;
    }

    final session = SessionUser.fromJson(jsonDecode(rawSession) as Map<String, dynamic>);
    _apiService.setToken(session.token);
    return session;
  }

  Future<SessionUser> login({required String email, required String password}) async {
    try {
      final response = await _apiService.client.post(
        '/login',
        data: {
          'email': email.trim(),
          'password': password,
        },
      );

      final session = SessionUser.fromJson(Map<String, dynamic>.from(response.data as Map));
      _apiService.setToken(session.token);

      final preferences = await SharedPreferences.getInstance();
      await preferences.setString(_sessionKey, jsonEncode(session.toJson()));

      return session;
    } on DioException catch (error) {
      throw Exception(_extractError(error, 'No se pudo iniciar sesión'));
    }
  }

  Future<void> logout() async {
    final preferences = await SharedPreferences.getInstance();
    await preferences.remove(_sessionKey);
    _apiService.setToken(null);
  }

  Future<List<PedidoModel>> getPedidos() async {
    try {
      final response = await _apiService.client.get('/pedidos');
      final data = List<Map<String, dynamic>>.from(response.data as List);
      return data.map(PedidoModel.fromJson).toList();
    } on DioException catch (error) {
      throw Exception(_extractError(error, 'No se pudieron cargar los pedidos'));
    }
  }

  Future<List<AppNotification>> getNotifications() async {
    try {
      final response = await _apiService.client.get('/notificaciones');
      final data = List<Map<String, dynamic>>.from(response.data as List);
      return data.map(AppNotification.fromJson).toList();
    } on DioException catch (error) {
      throw Exception(_extractError(error, 'No se pudieron cargar las notificaciones'));
    }
  }

  Future<void> markNotificationAsRead(int id) async {
    try {
      await _apiService.client.post('/notificaciones/$id/leer');
    } on DioException catch (error) {
      throw Exception(_extractError(error, 'No se pudo actualizar la notificación'));
    }
  }

  Future<List<AppUserSummary>> getUsers() async {
    try {
      final response = await _apiService.client.get('/usuarios');
      final data = List<Map<String, dynamic>>.from(response.data as List);
      return data.map(AppUserSummary.fromJson).toList();
    } on DioException catch (error) {
      throw Exception(_extractError(error, 'No se pudieron cargar los usuarios'));
    }
  }

  Future<void> createUser({
    required String nombre,
    required String email,
    required String password,
    required String rol,
  }) async {
    try {
      await _apiService.client.post(
        '/usuarios',
        data: {
          'nombre': nombre,
          'email': email,
          'password': password,
          'rol': rol,
        },
      );
    } on DioException catch (error) {
      throw Exception(_extractError(error, 'No se pudo crear el usuario'));
    }
  }

  Future<void> createPedido({
    required String cliente,
    required String direccion,
    required bool sinLevantadoMostrador,
    required String levantadoEnMostrador,
    required List<Map<String, dynamic>> items,
  }) async {
    try {
      await _apiService.client.post(
        '/pedidos',
        data: {
          'cliente': cliente,
          'direccion': direccion,
          'levantado': sinLevantadoMostrador ? 'sin_mostrador' : 'con_mostrador',
          'levantado_en_mostrador': sinLevantadoMostrador ? '' : levantadoEnMostrador,
          'sin_levantado_mostrador': sinLevantadoMostrador,
          'items': items,
        },
      );
    } on DioException catch (error) {
      throw Exception(_extractError(error, 'No se pudo crear el pedido'));
    }
  }

  Future<void> assignPedido({required int pedidoId, required int conductorId}) async {
    try {
      await _apiService.client.post(
        '/pedidos/$pedidoId/asignar',
        data: {'conductor_id': conductorId},
      );
    } on DioException catch (error) {
      throw Exception(_extractError(error, 'No se pudo asignar el pedido'));
    }
  }

  Future<void> acceptPedido(int pedidoId) async {
    try {
      await _apiService.client.post('/pedidos/$pedidoId/aceptar');
    } on DioException catch (error) {
      throw Exception(_extractError(error, 'No se pudo aceptar el pedido'));
    }
  }

  String _extractError(DioException error, String fallback) {
    final data = error.response?.data;
    if (data is Map && data['error'] != null) {
      return data['error'].toString();
    }
    return fallback;
  }
}
