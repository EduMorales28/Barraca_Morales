import 'package:get/get.dart';
import 'package:logistica_morales/controllers/auth_controller.dart';
import 'package:logistica_morales/models/app_notification.dart';
import 'package:logistica_morales/models/app_user_summary.dart';
import 'package:logistica_morales/models/pedido_model.dart';
import 'package:logistica_morales/services/auth_service.dart';
import 'package:image_picker/image_picker.dart';

class DashboardController extends GetxController {
  DashboardController(this._authService);

  final AuthService _authService;
  final AuthController _authController = Get.find<AuthController>();

  final RxList<PedidoModel> pedidos = <PedidoModel>[].obs;
  final RxList<AppNotification> notifications = <AppNotification>[].obs;
  final RxList<AppUserSummary> users = <AppUserSummary>[].obs;
  final RxBool isLoading = false.obs;
  final RxString errorMessage = ''.obs;

  List<AppUserSummary> get conductores =>
      users.where((user) => user.rol == 'conductor').toList();

  int get unreadNotifications => notifications.where((item) => !item.leida).length;

  Future<void> loadAll() async {
    try {
      isLoading.value = true;
      errorMessage.value = '';

      final results = await Future.wait([
        _authService.getPedidos(),
        _authService.getNotifications(),
        _authController.user.isAdmin
            ? _authService.getUsers()
            : _authController.user.canCreatePedidos
                ? _authService.getConductores()
                : Future.value(<AppUserSummary>[]),
      ]);

      pedidos.assignAll(results[0] as List<PedidoModel>);
      notifications.assignAll(results[1] as List<AppNotification>);
      users.assignAll(results[2] as List<AppUserSummary>);
    } catch (error) {
      errorMessage.value = error.toString().replaceFirst('Exception: ', '');
    } finally {
      isLoading.value = false;
    }
  }

  Future<void> markNotificationAsRead(int id) async {
    try {
      await _authService.markNotificationAsRead(id);
      await loadAll();
    } catch (error) {
      Get.snackbar('Notificaciones', error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<void> acceptPedido(PedidoModel pedido) async {
    try {
      await _authService.acceptPedido(pedido.id);
      await loadAll();
      Get.snackbar('Pedido', 'Pedido aceptado correctamente');
    } catch (error) {
      Get.snackbar('Pedido', error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<void> assignPedido({required int pedidoId, required int conductorId}) async {
    try {
      await _authService.assignPedido(pedidoId: pedidoId, conductorId: conductorId);
      await loadAll();
      Get.snackbar('Pedido', 'Pedido asignado correctamente');
    } catch (error) {
      Get.snackbar('Pedido', error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<void> createPedido({
    required String cliente,
    required String direccion,
    required int conductorId,
    required bool sinLevantadoMostrador,
    required String levantadoEnMostrador,
    required List<Map<String, dynamic>> items,
  }) async {
    try {
      await _authService.createPedido(
        cliente: cliente,
        direccion: direccion,
        conductorId: conductorId,
        sinLevantadoMostrador: sinLevantadoMostrador,
        levantadoEnMostrador: levantadoEnMostrador,
        items: items,
      );
      await loadAll();
      Get.snackbar('Pedido', 'Pedido creado correctamente');
    } catch (error) {
      rethrow;
    }
  }

  Future<void> rejectPedido({required PedidoModel pedido, required String motivo}) async {
    try {
      await _authService.rejectPedido(pedidoId: pedido.id, motivo: motivo);
      await loadAll();
      Get.snackbar('Pedido', 'Pedido rechazado correctamente');
    } catch (error) {
      Get.snackbar('Pedido', error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<void> markPedidoEntregado({
    required PedidoModel pedido,
    required XFile foto,
    String? observaciones,
  }) async {
    try {
      await _authService.markPedidoEntregado(
        pedidoId: pedido.id,
        foto: foto,
        observaciones: observaciones,
      );
      await loadAll();
      Get.snackbar('Pedido', 'Pedido marcado como entregado');
    } catch (error) {
      Get.snackbar('Pedido', error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<void> createUser({
    required String nombre,
    required String email,
    required String password,
    required String rol,
  }) async {
    try {
      await _authService.createUser(
        nombre: nombre,
        email: email,
        password: password,
        rol: rol,
      );
      await loadAll();
      Get.snackbar('Usuarios', 'Usuario creado correctamente');
    } catch (error) {
      rethrow;
    }
  }
}