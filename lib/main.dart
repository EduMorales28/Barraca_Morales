import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:logistica_morales/controllers/auth_controller.dart';
import 'package:logistica_morales/views/home_view.dart';
import 'package:logistica_morales/views/login_view.dart';
import 'config/app_bindings.dart';
import 'config/routes.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return GetMaterialApp(
      title: 'Barraca Morales',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF0F766E)),
        useMaterial3: true,
      ),
      initialBinding: AppBindings(),
      getPages: AppPages.pages,
      home: const RootView(),
      debugShowCheckedModeBanner: false,
    );
  }
}

class RootView extends GetView<AuthController> {
  const RootView({super.key});

  @override
  Widget build(BuildContext context) {
    return Obx(() {
      if (!controller.isReady.value) {
        return const Scaffold(
          body: Center(child: CircularProgressIndicator()),
        );
      }

      return controller.isAuthenticated ? const HomeView() : const LoginView();
    });
  }
}
