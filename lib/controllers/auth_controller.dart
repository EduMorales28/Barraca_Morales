import 'package:get/get.dart';
import 'package:logistica_morales/config/routes.dart';
import 'package:logistica_morales/models/session_user.dart';
import 'package:logistica_morales/services/auth_service.dart';

class AuthController extends GetxController {
  AuthController(this._authService);

  final AuthService _authService;

  final Rxn<SessionUser> currentUser = Rxn<SessionUser>();
  final RxBool isReady = false.obs;
  final RxBool isLoading = false.obs;
  final RxString errorMessage = ''.obs;

  @override
  void onInit() {
    super.onInit();
    restoreSession();
  }

  Future<void> restoreSession() async {
    currentUser.value = await _authService.restoreSession();
    isReady.value = true;
  }

  Future<void> login({required String email, required String password}) async {
    try {
      isLoading.value = true;
      errorMessage.value = '';
      currentUser.value = await _authService.login(email: email, password: password);
      Get.offAllNamed(Routes.home);
    } catch (error) {
      errorMessage.value = error.toString().replaceFirst('Exception: ', '');
    } finally {
      isLoading.value = false;
    }
  }

  Future<void> logout() async {
    await _authService.logout();
    currentUser.value = null;
    Get.offAllNamed(Routes.login);
  }

  SessionUser get user => currentUser.value!;
  bool get isAuthenticated => currentUser.value != null;

  String roleLabel(String rol) {
    switch (rol) {
      case 'admin':
        return 'Administrador';
      case 'conductor':
        return 'Conductor';
      case 'creador_pedidos':
        return 'Creador de pedidos';
      default:
        return rol;
    }
  }
}