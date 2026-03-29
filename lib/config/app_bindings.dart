import 'package:get/get.dart';
import 'package:logistica_morales/controllers/auth_controller.dart';
import 'package:logistica_morales/controllers/dashboard_controller.dart';
import 'package:logistica_morales/services/api_service.dart';
import 'package:logistica_morales/services/auth_service.dart';

class AppBindings extends Bindings {
  @override
  void dependencies() {
    Get.put(ApiService(), permanent: true);
    Get.put(AuthService(Get.find<ApiService>()), permanent: true);
    Get.put(AuthController(Get.find<AuthService>()), permanent: true);
    Get.put(DashboardController(Get.find<AuthService>()), permanent: true);
  }
}
