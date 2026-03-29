import 'package:get/get.dart';
import 'package:logistica_morales/views/login_view.dart';
import 'package:logistica_morales/views/home_view.dart';

abstract class Routes {
  static const String login = '/login';
  static const String home = '/home';
}

class AppPages {
  static final pages = [
    GetPage(
      name: Routes.login,
      page: () => const LoginView(),
    ),
    GetPage(
      name: Routes.home,
      page: () => const HomeView(),
    ),
  ];
}
