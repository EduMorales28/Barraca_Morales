import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:logistica_morales/main.dart';

void main() {
  testWidgets('Login view smoke test', (WidgetTester tester) async {
    SharedPreferences.setMockInitialValues({});
    await tester.pumpWidget(const MyApp());
    await tester.pumpAndSettle();

    expect(find.text('Barraca Morales'), findsOneWidget);
    expect(find.text('Ingreso móvil para Android Studio'), findsOneWidget);
    expect(find.text('Iniciar sesión'), findsOneWidget);
  });
}
