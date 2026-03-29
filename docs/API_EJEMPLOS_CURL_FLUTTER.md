# Ejemplos de Uso - API con cURL y Flutter

## 🔐 AUTENTICACIÓN

### cURL: Login
```bash
curl -X POST http://localhost:3000/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "conductor@logistica.com",
    "password": "securePassword123"
  }'

# Respuesta:
# {
#   "success": true,
#   "message": "Login exitoso",
#   "data": {
#     "token": "eyJhbGciOiJIUzI1NiIs...",
#     "expiresIn": 86400,
#     "usuario": {
#       "id": "USER_001",
#       "nombre": "Juan Pérez",
#       "rol": "conductor"
#     }
#   }
# }
```

Guardar el token:
```bash
TOKEN="eyJhbGciOiJIUzI1NiIs..."
```

---

## 📦 PEDIDOS

### cURL: Crear Pedido
```bash
curl -X POST http://localhost:3000/v1/pedidos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "clienteId": "CLIENT_ABC",
    "numero": "PED-2026-001",
    "direccion": "Av. Principal 123, Lima",
    "referencia": "Casa con reja azul",
    "latitud": -12.0496,
    "longitud": -77.0265,
    "montoTotal": 1000.00,
    "fechaEntregaEstimada": "2026-03-30",
    "observaciones": "Frágil",
    "items": [
      {
        "descripcion": "Descarga A",
        "cantidad": 5,
        "precioUnitario": 200.00
      }
    ]
  }'
```

### cURL: Listar Pedidos
```bash
curl -X GET "http://localhost:3000/v1/pedidos?estado=pendiente&page=1&limit=20" \
  -H "Authorization: Bearer $TOKEN"
```

### cURL: Obtener Pedido por ID
```bash
curl -X GET http://localhost:3000/v1/pedidos/PED_UUID_001 \
  -H "Authorization: Bearer $TOKEN"
```

### cURL: Actualizar Estado
```bash
curl -X PUT http://localhost:3000/v1/pedidos/PED_UUID_001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "estado": "en_ruta",
    "observaciones": "Conductor en camino"
  }'
```

### cURL: Asignar Conductor
```bash
curl -X POST http://localhost:3000/v1/pedidos/PED_UUID_001/asignar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "conductorId": "COND_001"
  }'
```

---

## 🚗 CONDUCTOR

### cURL: Obtener Mis Pedidos
```bash
curl -X GET "http://localhost:3000/v1/conductor/mis-pedidos?estado=en_ruta,parcial" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📸 ENTREGAS

### cURL: Crear Entrega (con foto)
```bash
curl -X POST http://localhost:3000/v1/entregas \
  -H "Authorization: Bearer $TOKEN" \
  -F "pedidoId=PED_UUID_001" \
  -F "itemPedidoId=ITEM_001" \
  -F "cantidadLevantada=5" \
  -F "observaciones=Cliente satisfecho" \
  -F "recibidoPor=Juan Pérez" \
  -F "dniRecibidor=12345678" \
  -F "foto=@/ruta/a/foto.jpg" \
  -F "fotoFirma=@/ruta/a/firma.jpg"
```

---

## 📱 FLUTTER - Ejemplos de Implementación

### 1. Crear Servicio de API
```dart
// services/api_service.dart
import 'package:dio/dio.dart';
import 'package:logistica_morales/models/database_models.dart';

class ApiService {
  final Dio _dio = Dio();
  String? _token;

  ApiService() {
    _dio.options.baseUrl = 'http://localhost:3000/v1';
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) {
        if (_token != null) {
          options.headers['Authorization'] = 'Bearer $_token';
        }
        return handler.next(options);
      },
      onError: (error, handler) {
        print('Error: ${error.response?.statusCode} - ${error.message}');
        return handler.next(error);
      },
    ));
  }

  // Login
  Future<Map<String, dynamic>> login(String email, String password) async {
    try {
      final response = await _dio.post('/auth/login', data: {
        'email': email,
        'password': password,
      });

      if (response.statusCode == 200) {
        _token = response.data['data']['token'];
        return response.data['data'];
      }
      throw Exception('Login fallido');
    } on DioException catch (e) {
      throw Exception(e.response?.data['error']['details'] ?? 'Error desconocido');
    }
  }

  // Crear pedido
  Future<Pedido> crearPedido({
    required String clienteId,
    required String numero,
    required String direccion,
    required double latitud,
    required double longitud,
    required double montoTotal,
    required List<Map<String, dynamic>> items,
  }) async {
    try {
      final response = await _dio.post('/pedidos', data: {
        'clienteId': clienteId,
        'numero': numero,
        'direccion': direccion,
        'latitud': latitud,
        'longitud': longitud,
        'montoTotal': montoTotal,
        'items': items,
      });

      if (response.statusCode == 201) {
        return Pedido.fromJson(response.data['data']);
      }
      throw Exception('Error al crear pedido');
    } on DioException catch (e) {
      throw Exception(e.response?.data['error']['details'] ?? 'Error desconocido');
    }
  }

  // Obtener pedidos
  Future<List<Pedido>> obtenerPedidos({
    String? estado,
    int page = 1,
    int limit = 20,
  }) async {
    try {
      final response = await _dio.get('/pedidos', queryParameters: {
        if (estado != null) 'estado': estado,
        'page': page,
        'limit': limit,
      });

      if (response.statusCode == 200) {
        final pedidos = (response.data['data']['pedidos'] as List)
            .map((p) => Pedido.fromJson(p))
            .toList();
        return pedidos;
      }
      throw Exception('Error al obtener pedidos');
    } on DioException catch (e) {
      throw Exception(e.response?.data['error']['details'] ?? 'Error desconocido');
    }
  }

  // Obtener pedido por ID
  Future<Pedido> obtenerPedidoById(String pedidoId) async {
    try {
      final response = await _dio.get('/pedidos/$pedidoId');

      if (response.statusCode == 200) {
        return Pedido.fromJson(response.data['data']);
      }
      throw Exception('Pedido no encontrado');
    } on DioException catch (e) {
      throw Exception(e.response?.data['error']['details'] ?? 'Error desconocido');
    }
  }

  // Asignar conductor
  Future<void> asignarConductor(String pedidoId, String conductorId) async {
    try {
      final response = await _dio.post(
        '/pedidos/$pedidoId/asignar',
        data: {'conductorId': conductorId},
      );

      if (response.statusCode != 200) {
        throw Exception('Error al asignar conductor');
      }
    } on DioException catch (e) {
      throw Exception(e.response?.data['error']['details'] ?? 'Error desconocido');
    }
  }

  // Obtener mis pedidos (conductor)
  Future<Map<String, dynamic>> obtenerMisPedidos({String? estado}) async {
    try {
      final response = await _dio.get(
        '/conductor/mis-pedidos',
        queryParameters: {
          if (estado != null) 'estado': estado,
        },
      );

      if (response.statusCode == 200) {
        return response.data['data'];
      }
      throw Exception('Error al obtener mis pedidos');
    } on DioException catch (e) {
      throw Exception(e.response?.data['error']['details'] ?? 'Error desconocido');
    }
  }

  // Crear entrega (con foto)
  Future<Entrega> crearEntrega({
    required String pedidoId,
    required String itemPedidoId,
    required double cantidadLevantada,
    required String observaciones,
    required String recibidoPor,
    required String dniRecibidor,
    required File fotoFile,
    File? fotoFirmaFile,
  }) async {
    try {
      final formData = FormData.fromMap({
        'pedidoId': pedidoId,
        'itemPedidoId': itemPedidoId,
        'cantidadLevantada': cantidadLevantada,
        'observaciones': observaciones,
        'recibidoPor': recibidoPor,
        'dniRecibidor': dniRecibidor,
        'foto': await MultipartFile.fromFile(fotoFile.path),
        if (fotoFirmaFile != null)
          'fotoFirma': await MultipartFile.fromFile(fotoFirmaFile.path),
      });

      final response = await _dio.post('/entregas', data: formData);

      if (response.statusCode == 201) {
        return Entrega.fromJson(response.data['data']);
      }
      throw Exception('Error al crear entrega');
    } on DioException catch (e) {
      throw Exception(e.response?.data['error']['details'] ?? 'Error desconocido');
    }
  }

  // Obtener token
  String? get token => _token;

  // Establecer token (para recuperar sesión)
  void setToken(String token) {
    _token = token;
  }

  // Logout
  void logout() {
    _token = null;
  }
}
```

### 2. Controlador GetX
```dart
// controllers/pedido_controller.dart
import 'package:get/get.dart';
import 'package:logistica_morales/models/database_models.dart';
import 'package:logistica_morales/services/api_service.dart';

class PedidoController extends GetxController {
  final apiService = Get.find<ApiService>();

  var pedidos = <Pedido>[].obs;
  var isLoading = false.obs;
  var error = ''.obs;

  Future<void> obtenerPedidos({String? estado}) async {
    try {
      isLoading.value = true;
      error.value = '';

      final lista = await apiService.obtenerPedidos(estado: estado);
      pedidos.value = lista;
    } catch (e) {
      error.value = e.toString();
      Get.snackbar('Error', error.value);
    } finally {
      isLoading.value = false;
    }
  }

  Future<void> asignarConductor(String pedidoId, String conductorId) async {
    try {
      await apiService.asignarConductor(pedidoId, conductorId);
      await obtenerPedidos();
      Get.snackbar('Éxito', 'Conductor asignado');
    } catch (e) {
      Get.snackbar('Error', e.toString());
    }
  }
}
```

### 3. Vista
```dart
// views/pedidos_view.dart
import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:logistica_morales/controllers/pedido_controller.dart';

class PedidosView extends StatelessWidget {
  final controller = Get.put(PedidoController());

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Pedidos')),
      body: Obx(() {
        if (controller.isLoading.value) {
          return const Center(child: CircularProgressIndicator());
        }

        if (controller.error.value.isNotEmpty) {
          return Center(child: Text('Error: ${controller.error.value}'));
        }

        return ListView.builder(
          itemCount: controller.pedidos.length,
          itemBuilder: (context, index) {
            final pedido = controller.pedidos[index];
            return ListTile(
              title: Text(pedido.numero),
              subtitle: Text('Estado: ${pedido.estado}'),
              trailing: Text('\$${pedido.montoTotal}'),
              onTap: () {
                // Ir a detalle
              },
            );
          },
        );
      }),
      floatingActionButton: FloatingActionButton(
        onPressed: () => controller.obtenerPedidos(),
        child: const Icon(Icons.refresh),
      ),
    );
  }
}
```

---

## 🧪 Testing

### Usando Postman
1. Importar `Postman_Collection.json` en Postman
2. Configurar variable `baseUrl` → `http://localhost:3000/v1`
3. Ejecutar endpoint LOGIN (guarda token automáticamente)
4. Ejecutar otros endpoints

### Usando cURL + Shell Script
```bash
#!/bin/bash

# Config
API="http://localhost:3000/v1"
EMAIL="conductor@logistica.com"
PASSWORD="securePassword123"

# 1. Login
echo "🔐 Realizando login..."
LOGIN=$(curl -s -X POST $API/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo $LOGIN | grep -o '"token":"[^"]*' | grep -o '[^"]*$')
echo "✅ Token: $TOKEN"

# 2. Crear pedido
echo "📦 Creando pedido..."
PEDIDO=$(curl -s -X POST $API/pedidos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "clienteId": "CLIENT_ABC",
    "numero": "PED-2026-001",
    "direccion": "Av. Principal 123",
    "latitud": -12.0496,
    "longitud": -77.0265,
    "montoTotal": 1000,
    "items": [{"descripcion": "Item A", "cantidad": 5, "precioUnitario": 200}]
  }')

echo $PEDIDO | jq .
```

---

## ⚠️ Manejo de Errores en Flutter

```dart
try {
  await apiService.crearPedido(...);
} on FormatException catch (e) {
  // Error de formato JSON
  print('Error de formato: $e');
} on IllegalAccessException catch (e) {
  // Error de autenticación
  print('No autorizado: $e');
} on DioException catch (e) {
  if (e.response?.statusCode == 400) {
    // Error de validación
    final details = e.response?.data['error']['details'];
    print('Validación: $details');
  } else if (e.response?.statusCode == 401) {
    // Token expirado
    // Ir a login
  } else {
    print('Error: ${e.message}');
  }
} catch (e) {
  // Otros errores
  print('Error inesperado: $e');
}
```

---

## 🔒 Seguridad

✅ Usar HTTPS en producción
✅ No guardar contraseña en texto plano
✅ Guardar token en keychain/keystore seguro
✅ Refrescar token antes de expirar
✅ Validar datos en cliente y servidor
✅ Usar CORS apropiadamente
