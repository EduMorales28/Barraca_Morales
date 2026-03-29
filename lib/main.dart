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
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFFEF6B3E),
          primary: const Color(0xFFEF6B3E),
          secondary: const Color(0xFF2D3748),
          surface: Colors.white,
          error: const Color(0xFFF56565),
        ),
        useMaterial3: true,
        scaffoldBackgroundColor: const Color(0xFFF7FAFC),
        // ── AppBar igual al sidebar del admin panel ──
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFF2D3748),
          foregroundColor: Colors.white,
          elevation: 0,
          centerTitle: false,
        ),
        // ── Bottom Navigation Bar como el header blanco ──
        navigationBarTheme: NavigationBarThemeData(
          backgroundColor: Colors.white,
          elevation: 4,
          shadowColor: Colors.black12,
          indicatorColor: const Color(0xFFEF6B3E).withValues(alpha: 0.18),
        ),
        // ── Cards igual al admin panel ──
        cardTheme: CardThemeData(
          color: Colors.white,
          elevation: 1,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
            side: const BorderSide(color: Color(0xFFE2E8F0)),
          ),
        ),
        // ── Botones filled: naranja primario ──
        filledButtonTheme: FilledButtonThemeData(
          style: FilledButton.styleFrom(
            backgroundColor: const Color(0xFFEF6B3E),
            foregroundColor: Colors.white,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          ),
        ),
        // ── Botones outlined: borde naranja ──
        outlinedButtonTheme: OutlinedButtonThemeData(
          style: OutlinedButton.styleFrom(
            foregroundColor: const Color(0xFFEF6B3E),
            side: const BorderSide(color: Color(0xFFEF6B3E)),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          ),
        ),
        // ── Inputs con foco naranja ──
        inputDecorationTheme: InputDecorationTheme(
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(8),
            borderSide: const BorderSide(color: Color(0xFFEF6B3E), width: 2),
          ),
          floatingLabelStyle: const TextStyle(color: Color(0xFFEF6B3E)),
        ),
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
