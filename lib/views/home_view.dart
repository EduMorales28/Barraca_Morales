import 'dart:async';

import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:image_picker/image_picker.dart';
import 'package:intl/intl.dart';
import 'package:logistica_morales/controllers/auth_controller.dart';
import 'package:logistica_morales/controllers/dashboard_controller.dart';
import 'package:logistica_morales/models/pedido_model.dart';
import 'package:logistica_morales/models/session_user.dart';

class HomeView extends StatefulWidget {
  const HomeView({super.key});

  @override
  State<HomeView> createState() => _HomeViewState();
}

class _HomeViewState extends State<HomeView> {
  final AuthController _authController = Get.find<AuthController>();
  final DashboardController _dashboardController = Get.find<DashboardController>();
  Timer? _pollTimer;
  int _lastUnreadCount = 0;
  int _selectedIndex = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _dashboardController.loadAll().then((_) {
        _lastUnreadCount = _dashboardController.unreadNotifications;
      });
    });

    _pollTimer = Timer.periodic(const Duration(seconds: 20), (_) async {
      await _dashboardController.loadAll();
      final unread = _dashboardController.unreadNotifications;
      if (_authController.user.isConductor && unread > _lastUnreadCount) {
        Get.snackbar(
          'Pedidos',
          'Tienes nuevas notificaciones de pedidos asignados',
          backgroundColor: const Color(0xFFEF6B3E),
          colorText: Colors.white,
          snackPosition: SnackPosition.TOP,
        );
      }
      _lastUnreadCount = unread;
    });
  }

  @override
  void dispose() {
    _pollTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final user = _authController.user;
    final sections = _buildSections(user);
    if (_selectedIndex >= sections.length) {
      _selectedIndex = 0;
    }

    return Obx(() {
      return Scaffold(
        appBar: AppBar(
          title: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Barraca Morales'),
              Text(
                _authController.roleLabel(user.rol),
                style: Theme.of(context).textTheme.labelMedium,
              ),
            ],
          ),
          actions: [
            IconButton(
              onPressed: _dashboardController.loadAll,
              icon: const Icon(Icons.refresh),
            ),
            Stack(
              children: [
                IconButton(
                  onPressed: () => _showNotifications(context),
                  icon: const Icon(Icons.notifications_outlined),
                ),
                if (_dashboardController.unreadNotifications > 0)
                  Positioned(
                    right: 10,
                    top: 10,
                    child: CircleAvatar(
                      radius: 9,
                      backgroundColor: Colors.redAccent,
                      child: Text(
                        '${_dashboardController.unreadNotifications}',
                        style: const TextStyle(fontSize: 10, color: Colors.white),
                      ),
                    ),
                  ),
              ],
            ),
            IconButton(
              onPressed: _authController.logout,
              icon: const Icon(Icons.logout),
            ),
          ],
        ),
        body: sections[_selectedIndex].builder(),
        bottomNavigationBar: sections.length < 2
            ? null
            : NavigationBar(
                selectedIndex: _selectedIndex,
                onDestinationSelected: (value) => setState(() => _selectedIndex = value),
                destinations: sections
                    .map(
                      (section) => NavigationDestination(
                        icon: Icon(section.icon),
                        label: section.label,
                      ),
                    )
                    .toList(),
              ),
      );
    });
  }

  List<_HomeSection> _buildSections(SessionUser user) {
    final sections = <_HomeSection>[];

    // ── Conductores: tab dedicado con sus pedidos agrupados ──
    if (user.isConductor) {
      sections.add(
        _HomeSection(
          label: 'Mis Pedidos',
          icon: Icons.local_shipping_outlined,
          builder: () => _ConductorTab(
            dashboardController: _dashboardController,
            onAccept: (p) => _dashboardController.acceptPedido(p),
            onReject: (p) => _showRejectDialog(context, p),
            onDeliver: (p) => _showDeliverDialog(context, p),
          ),
        ),
      );
    } else {
      sections.add(
        _HomeSection(
          label: 'Pedidos',
          icon: Icons.local_shipping_outlined,
          builder: () => _PedidosTab(
            authController: _authController,
            dashboardController: _dashboardController,
            onAssign: (pedido) => _showAssignSheet(context, pedido),
            onReject: (pedido) => _showRejectDialog(context, pedido),
            onDeliver: (pedido) => _showDeliverDialog(context, pedido),
          ),
        ),
      );
    }

    if (user.canCreatePedidos) {
      sections.add(
        _HomeSection(
          label: 'Crear',
          icon: Icons.add_box_outlined,
          builder: () => CreatePedidoTab(dashboardController: _dashboardController),
        ),
      );
    }

    if (user.isAdmin) {
      sections.add(
        _HomeSection(
          label: 'Usuarios',
          icon: Icons.manage_accounts_outlined,
          builder: () => UsersTab(dashboardController: _dashboardController),
        ),
      );
    }

    return sections;
  }

  Future<void> _showNotifications(BuildContext context) async {
    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (_) => SafeArea(
        child: Obx(() {
          final notifications = _dashboardController.notifications;
          if (notifications.isEmpty) {
            return const SizedBox(
              height: 220,
              child: Center(child: Text('No hay notificaciones todavía.')),
            );
          }

          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemBuilder: (_, index) {
              final notification = notifications[index];
              return ListTile(
                tileColor: notification.leida ? null : const Color(0xFFFFF7ED),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                title: Text(notification.mensaje),
                subtitle: Text(notification.createdAt),
                trailing: notification.leida
                    ? const Icon(Icons.done_all, color: Colors.green)
                    : TextButton(
                        onPressed: () => _dashboardController.markNotificationAsRead(notification.id),
                        child: const Text('Marcar'),
                      ),
              );
            },
            separatorBuilder: (_, index) => const SizedBox(height: 8),
            itemCount: notifications.length,
          );
        }),
      ),
    );
  }

  Future<void> _showAssignSheet(BuildContext context, PedidoModel pedido) async {
    final conductores = _dashboardController.conductores;
    if (conductores.isEmpty) {
      Get.snackbar('Conductores', 'No hay conductores disponibles para asignar');
      return;
    }

    int? selectedConductorId = conductores.first.id;

    await showModalBottomSheet<void>(
      context: context,
      builder: (_) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Asignar pedido #${pedido.id}', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 16),
              DropdownButtonFormField<int>(
                initialValue: selectedConductorId,
                items: conductores
                    .map(
                      (item) => DropdownMenuItem<int>(
                        value: item.id,
                        child: Text('${item.nombre} · ${item.email}'),
                      ),
                    )
                    .toList(),
                onChanged: (value) => selectedConductorId = value,
                decoration: const InputDecoration(labelText: 'Conductor'),
              ),
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                child: FilledButton(
                  onPressed: () async {
                    if (selectedConductorId == null) return;
                    Navigator.of(context).pop();
                    await _dashboardController.assignPedido(
                      pedidoId: pedido.id,
                      conductorId: selectedConductorId!,
                    );
                  },
                  child: const Text('Asignar'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _showRejectDialog(BuildContext context, PedidoModel pedido) async {
    final motivoController = TextEditingController();
    await showDialog<void>(
      context: context,
      builder: (_) => AlertDialog(
        title: Text('Rechazar pedido #${pedido.id}'),
        content: TextField(
          controller: motivoController,
          maxLines: 3,
          decoration: const InputDecoration(
            labelText: 'Motivo del rechazo',
            hintText: 'Ej: cliente ausente, dirección incorrecta, vehículo sin capacidad',
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () async {
              final motivo = motivoController.text.trim();
              if (motivo.isEmpty) {
                Get.snackbar('Pedido', 'Debes ingresar el motivo del rechazo');
                return;
              }
              Navigator.of(context).pop();
              await _dashboardController.rejectPedido(pedido: pedido, motivo: motivo);
            },
            child: const Text('Confirmar rechazo'),
          ),
        ],
      ),
    );
    motivoController.dispose();
  }

  Future<void> _showDeliverDialog(BuildContext context, PedidoModel pedido) async {
    final picker = ImagePicker();
    final observacionesController = TextEditingController();

    // Preguntar si tomar foto nueva o desde galería
    final source = await showDialog<ImageSource>(
      context: context,
      builder: (_) => AlertDialog(
        title: Text('Entregar pedido #${pedido.id}'),
        content: const Text('¿Cómo querés subir la foto de entrega?'),
        actions: [
          TextButton.icon(
            onPressed: () => Navigator.of(context).pop(ImageSource.gallery),
            icon: const Icon(Icons.photo_library_outlined),
            label: const Text('Galería'),
          ),
          FilledButton.icon(
            onPressed: () => Navigator.of(context).pop(ImageSource.camera),
            icon: const Icon(Icons.photo_camera),
            label: const Text('Tomar foto'),
          ),
        ],
      ),
    );

    if (source == null) {
      observacionesController.dispose();
      return;
    }

    if (!context.mounted) {
      observacionesController.dispose();
      return;
    }

    final foto = await picker.pickImage(source: source, imageQuality: 80);
    if (!context.mounted) {
      observacionesController.dispose();
      return;
    }
    if (foto == null) {
      observacionesController.dispose();
      return;
    }

    final observaciones = await showDialog<String>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Observaciones (opcional)'),
        content: TextField(
          controller: observacionesController,
          maxLines: 3,
          decoration: const InputDecoration(
            labelText: 'Observaciones',
            hintText: 'Ej: entregado al portero, firmado por el cliente…',
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(''),
            child: const Text('Omitir'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(observacionesController.text.trim()),
            child: const Text('Confirmar entrega'),
          ),
        ],
      ),
    );

    if (!context.mounted) {
      observacionesController.dispose();
      return;
    }

    await _dashboardController.markPedidoEntregado(
      pedido: pedido,
      foto: foto,
      observaciones: observaciones,
    );

    observacionesController.dispose();
  }
}

class _ConductorTab extends StatelessWidget {
  const _ConductorTab({
    required this.dashboardController,
    required this.onAccept,
    required this.onReject,
    required this.onDeliver,
  });

  final DashboardController dashboardController;
  final ValueChanged<PedidoModel> onAccept;
  final ValueChanged<PedidoModel> onReject;
  final ValueChanged<PedidoModel> onDeliver;

  @override
  Widget build(BuildContext context) {
    return Obx(() {
      if (dashboardController.isLoading.value && dashboardController.pedidos.isEmpty) {
        return const Center(child: CircularProgressIndicator());
      }

      final nuevas = dashboardController.pedidos
          .where((p) => p.estado == 'asignado')
          .toList();
      final enReparto = dashboardController.pedidos
          .where((p) => p.estado == 'pendiente_entrega')
          .toList();
      final historial = dashboardController.pedidos
          .where((p) => p.estado == 'entregado' || p.estado == 'rechazado')
          .toList();

      final todoVacio = nuevas.isEmpty && enReparto.isEmpty;

      return RefreshIndicator(
        onRefresh: dashboardController.loadAll,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // ── Sección 1: En reparto → botón grande "Entregar" ──
            if (enReparto.isNotEmpty) ...[
              _SectionHeader(
                title: 'Pendientes de entrega',
                count: enReparto.length,
                color: const Color(0xFF3730A3),
                bgColor: const Color(0xFFE0E7FF),
              ),
              const SizedBox(height: 12),
              ...enReparto.map(
                (p) => Padding(
                  padding: const EdgeInsets.only(bottom: 12),
                  child: _ConductorDeliveryCard(
                    pedido: p,
                    onDeliver: () => onDeliver(p),
                  ),
                ),
              ),
              const SizedBox(height: 8),
            ],

            // ── Sección 2: Nuevas asignaciones ──
            if (nuevas.isNotEmpty) ...[
              _SectionHeader(
                title: 'Nuevas asignaciones',
                count: nuevas.length,
                color: const Color(0xFF1E40AF),
                bgColor: const Color(0xFFDBEAFE),
              ),
              const SizedBox(height: 12),
              ...nuevas.map(
                (p) => Padding(
                  padding: const EdgeInsets.only(bottom: 12),
                  child: _ConductorAssignedCard(
                    pedido: p,
                    onAccept: () => onAccept(p),
                    onReject: () => onReject(p),
                  ),
                ),
              ),
              const SizedBox(height: 8),
            ],

            // ── Sin pedidos activos ──
            if (todoVacio)
              Center(
                child: Padding(
                  padding: const EdgeInsets.symmetric(vertical: 48),
                  child: Column(
                    children: [
                      Icon(Icons.check_circle_outline, size: 64, color: Colors.grey.shade300),
                      const SizedBox(height: 16),
                      Text(
                        'No tenés pedidos activos',
                        style: TextStyle(fontSize: 16, color: Colors.grey.shade500),
                      ),
                    ],
                  ),
                ),
              ),

            // ── Historial ──
            if (historial.isNotEmpty) ...[
              const Divider(height: 32),
              _SectionHeader(
                title: 'Historial',
                count: historial.length,
                color: const Color(0xFF6B7280),
                bgColor: const Color(0xFFF3F4F6),
              ),
              const SizedBox(height: 12),
              ...historial.map(
                (p) => Padding(
                  padding: const EdgeInsets.only(bottom: 8),
                  child: _PedidoCard(
                    pedido: p,
                    canAssign: false,
                    canAccept: false,
                    canReject: false,
                    canDeliver: false,
                    onAssign: () {},
                    onAccept: () {},
                    onReject: () {},
                    onDeliver: () {},
                  ),
                ),
              ),
            ],
          ],
        ),
      );
    });
  }
}

class _SectionHeader extends StatelessWidget {
  const _SectionHeader({
    required this.title,
    required this.count,
    required this.color,
    required this.bgColor,
  });

  final String title;
  final int count;
  final Color color;
  final Color bgColor;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Text(
            title,
            style: TextStyle(
              fontSize: 15,
              fontWeight: FontWeight.w700,
              color: const Color(0xFF2D3748),
            ),
          ),
        ),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
          decoration: BoxDecoration(color: bgColor, borderRadius: BorderRadius.circular(20)),
          child: Text(
            '$count',
            style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold, color: color),
          ),
        ),
      ],
    );
  }
}

/// Tarjeta para pedidos en estado `pendiente_entrega` — botón cámara prominente
class _ConductorDeliveryCard extends StatelessWidget {
  const _ConductorDeliveryCard({required this.pedido, required this.onDeliver});

  final PedidoModel pedido;
  final VoidCallback onDeliver;

  @override
  Widget build(BuildContext context) {
    final formatter = DateFormat('dd/MM/yyyy HH:mm');
    final dateText = pedido.createdAt == null
        ? 'Sin fecha'
        : formatter.format(DateTime.parse(pedido.createdAt!));

    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: const BorderSide(color: Color(0xFFC7D2FE), width: 1.5),
      ),
      color: const Color(0xFFF5F3FF),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Icon(Icons.local_shipping, color: Color(0xFF4F46E5), size: 20),
                const SizedBox(width: 8),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        pedido.cliente,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                          color: Color(0xFF1E1B4B),
                        ),
                      ),
                      const SizedBox(height: 4),
                      Row(
                        children: [
                          const Icon(Icons.location_on_outlined, size: 14, color: Color(0xFF6B7280)),
                          const SizedBox(width: 4),
                          Expanded(
                            child: Text(
                              pedido.direccion,
                              style: const TextStyle(fontSize: 13, color: Color(0xFF4B5563)),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                const _StatusChip(status: 'pendiente_entrega'),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                const Icon(Icons.inventory_2_outlined, size: 14, color: Color(0xFF6B7280)),
                const SizedBox(width: 4),
                Text(
                  '${pedido.items.length} item${pedido.items.length != 1 ? 's' : ''}',
                  style: const TextStyle(fontSize: 12, color: Color(0xFF6B7280)),
                ),
                const SizedBox(width: 16),
                const Icon(Icons.calendar_today_outlined, size: 14, color: Color(0xFF6B7280)),
                const SizedBox(width: 4),
                Text(dateText, style: const TextStyle(fontSize: 12, color: Color(0xFF6B7280))),
              ],
            ),
            if (pedido.items.isNotEmpty) ...[
              const SizedBox(height: 8),
              ...pedido.items.take(2).map(
                    (item) => Padding(
                      padding: const EdgeInsets.only(top: 2),
                      child: Text(
                        '• ${item.cantidad}x ${item.nombre}',
                        style: const TextStyle(fontSize: 13, color: Color(0xFF374151)),
                      ),
                    ),
                  ),
              if (pedido.items.length > 2)
                Text(
                  '  +${pedido.items.length - 2} más',
                  style: const TextStyle(fontSize: 12, color: Color(0xFF9CA3AF)),
                ),
            ],
            const SizedBox(height: 16),
            // ── Botón grande de entrega ──
            SizedBox(
              height: 52,
              child: FilledButton.icon(
                onPressed: onDeliver,
                icon: const Icon(Icons.photo_camera, size: 22),
                label: const Text(
                  'Marcar como Entregado',
                  style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                ),
                style: FilledButton.styleFrom(
                  backgroundColor: const Color(0xFFEF6B3E),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Tarjeta para pedidos en estado `asignado` — Aceptar o Rechazar
class _ConductorAssignedCard extends StatelessWidget {
  const _ConductorAssignedCard({
    required this.pedido,
    required this.onAccept,
    required this.onReject,
  });

  final PedidoModel pedido;
  final VoidCallback onAccept;
  final VoidCallback onReject;

  @override
  Widget build(BuildContext context) {
    final formatter = DateFormat('dd/MM/yyyy HH:mm');
    final dateText = pedido.createdAt == null
        ? 'Sin fecha'
        : formatter.format(DateTime.parse(pedido.createdAt!));

    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: const BorderSide(color: Color(0xFFBFDBFE), width: 1.5),
      ),
      color: const Color(0xFFF0F9FF),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Icon(Icons.new_releases_outlined, color: Color(0xFF1D4ED8), size: 20),
                const SizedBox(width: 8),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        pedido.cliente,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                          color: Color(0xFF1E3A5F),
                        ),
                      ),
                      const SizedBox(height: 4),
                      Row(
                        children: [
                          const Icon(Icons.location_on_outlined, size: 14, color: Color(0xFF6B7280)),
                          const SizedBox(width: 4),
                          Expanded(
                            child: Text(
                              pedido.direccion,
                              style: const TextStyle(fontSize: 13, color: Color(0xFF4B5563)),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                const _StatusChip(status: 'asignado'),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                const Icon(Icons.inventory_2_outlined, size: 14, color: Color(0xFF6B7280)),
                const SizedBox(width: 4),
                Text(
                  '${pedido.items.length} item${pedido.items.length != 1 ? 's' : ''}',
                  style: const TextStyle(fontSize: 12, color: Color(0xFF6B7280)),
                ),
                const SizedBox(width: 16),
                const Icon(Icons.calendar_today_outlined, size: 14, color: Color(0xFF6B7280)),
                const SizedBox(width: 4),
                Text(dateText, style: const TextStyle(fontSize: 12, color: Color(0xFF6B7280))),
              ],
            ),
            if (pedido.levantadoEnMostrador != null && pedido.levantadoEnMostrador!.isNotEmpty) ...[
              const SizedBox(height: 6),
              Row(
                children: [
                  const Icon(Icons.storefront_outlined, size: 14, color: Color(0xFF6B7280)),
                  const SizedBox(width: 4),
                  Text(
                    'Mostrador: ${pedido.levantadoEnMostrador!}',
                    style: const TextStyle(fontSize: 12, color: Color(0xFF6B7280)),
                  ),
                ],
              ),
            ],
            if (pedido.items.isNotEmpty) ...[
              const SizedBox(height: 8),
              ...pedido.items.take(3).map(
                    (item) => Padding(
                      padding: const EdgeInsets.only(top: 2),
                      child: Text(
                        '• ${item.cantidad}x ${item.nombre}',
                        style: const TextStyle(fontSize: 13, color: Color(0xFF374151)),
                      ),
                    ),
                  ),
              if (pedido.items.length > 3)
                Text(
                  '  +${pedido.items.length - 3} más',
                  style: const TextStyle(fontSize: 12, color: Color(0xFF9CA3AF)),
                ),
            ],
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: SizedBox(
                    height: 48,
                    child: OutlinedButton.icon(
                      onPressed: onReject,
                      icon: const Icon(Icons.cancel_outlined, size: 18),
                      label: const Text('Rechazar'),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: const Color(0xFFDC2626),
                        side: const BorderSide(color: Color(0xFFDC2626)),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  flex: 2,
                  child: SizedBox(
                    height: 48,
                    child: FilledButton.icon(
                      onPressed: onAccept,
                      icon: const Icon(Icons.check_circle_outline, size: 18),
                      label: const Text(
                        'Aceptar pedido',
                        style: TextStyle(fontWeight: FontWeight.bold),
                      ),
                      style: FilledButton.styleFrom(
                        backgroundColor: const Color(0xFF1D4ED8),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _PedidosTab extends StatelessWidget {
  const _PedidosTab({
    required this.authController,
    required this.dashboardController,
    required this.onAssign,
    required this.onReject,
    required this.onDeliver,
  });

  final AuthController authController;
  final DashboardController dashboardController;
  final ValueChanged<PedidoModel> onAssign;
  final ValueChanged<PedidoModel> onReject;
  final ValueChanged<PedidoModel> onDeliver;

  @override
  Widget build(BuildContext context) {
    return Obx(() {
      if (dashboardController.isLoading.value && dashboardController.pedidos.isEmpty) {
        return const Center(child: CircularProgressIndicator());
      }

      if (dashboardController.errorMessage.isNotEmpty && dashboardController.pedidos.isEmpty) {
        return Center(child: Text(dashboardController.errorMessage.value));
      }

      if (dashboardController.pedidos.isEmpty) {
        return const Center(child: Text('No hay pedidos disponibles.'));
      }

      return RefreshIndicator(
        onRefresh: dashboardController.loadAll,
        child: ListView.separated(
          padding: const EdgeInsets.all(16),
          itemBuilder: (context, index) {
            final pedido = dashboardController.pedidos[index];
            return _PedidoCard(
              pedido: pedido,
              canAssign: authController.user.isAdmin,
              canAccept: authController.user.isConductor && pedido.estado == 'asignado',
              canReject: authController.user.isConductor && pedido.estado == 'asignado',
              canDeliver: authController.user.isConductor && pedido.estado == 'pendiente_entrega',
              onAssign: () => onAssign(pedido),
              onAccept: () => dashboardController.acceptPedido(pedido),
              onReject: () => onReject(pedido),
              onDeliver: () => onDeliver(pedido),
            );
          },
          separatorBuilder: (context, index) => const SizedBox(height: 12),
          itemCount: dashboardController.pedidos.length,
        ),
      );
    });
  }
}

class _PedidoCard extends StatelessWidget {
  const _PedidoCard({
    required this.pedido,
    required this.canAssign,
    required this.canAccept,
    required this.canReject,
    required this.canDeliver,
    required this.onAssign,
    required this.onAccept,
    required this.onReject,
    required this.onDeliver,
  });

  final PedidoModel pedido;
  final bool canAssign;
  final bool canAccept;
  final bool canReject;
  final bool canDeliver;
  final VoidCallback onAssign;
  final VoidCallback onAccept;
  final VoidCallback onReject;
  final VoidCallback onDeliver;

  @override
  Widget build(BuildContext context) {
    final formatter = DateFormat('dd/MM/yyyy HH:mm');
    final dateText = pedido.createdAt == null
        ? 'Sin fecha'
        : formatter.format(DateTime.parse(pedido.createdAt!));

    return Card(
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(pedido.cliente, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                      const SizedBox(height: 4),
                      Text(pedido.direccion),
                    ],
                  ),
                ),
                _StatusChip(status: pedido.estado),
              ],
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 12,
              runSpacing: 8,
              children: [
                _InfoPill(label: 'Items', value: '${pedido.items.length}'),
                _InfoPill(label: 'Conductor', value: pedido.conductorNombre ?? 'Sin asignar'),
                _InfoPill(label: 'Creador', value: pedido.creadorNombre ?? 'N/D'),
                _InfoPill(label: 'Alta', value: dateText),
              ],
            ),
            if (pedido.levantadoEnMostrador != null && pedido.levantadoEnMostrador!.isNotEmpty) ...[
              const SizedBox(height: 12),
              Text('Levantado en mostrador: ${pedido.levantadoEnMostrador!}'),
            ],
            const SizedBox(height: 12),
            ...pedido.items.take(3).map(
                  (item) => Padding(
                    padding: const EdgeInsets.only(bottom: 4),
                    child: Text('${item.cantidad}x ${item.nombre} · \$${item.precio.toStringAsFixed(2)}'),
                  ),
                ),
            if (pedido.items.length > 3)
              Text('+${pedido.items.length - 3} items más'),
            if (pedido.rejectedReason != null && pedido.rejectedReason!.trim().isNotEmpty) ...[
              const SizedBox(height: 8),
              Text('Motivo de rechazo: ${pedido.rejectedReason!}'),
            ],
            const SizedBox(height: 16),
            Wrap(
              spacing: 12,
              runSpacing: 8,
              children: [
                if (canAssign)
                  OutlinedButton.icon(
                    onPressed: onAssign,
                    icon: const Icon(Icons.assignment_ind_outlined),
                    label: const Text('Asignar'),
                  ),
                if (canAccept)
                  FilledButton.icon(
                    onPressed: onAccept,
                    icon: const Icon(Icons.check_circle_outline),
                    label: const Text('Aceptar'),
                  ),
                if (canReject)
                  OutlinedButton.icon(
                    onPressed: onReject,
                    icon: const Icon(Icons.cancel_outlined),
                    label: const Text('Rechazar'),
                  ),
                if (canDeliver)
                  FilledButton.icon(
                    onPressed: onDeliver,
                    icon: const Icon(Icons.photo_camera_back_outlined),
                    label: const Text('Pedido entregado'),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class CreatePedidoTab extends StatefulWidget {
  const CreatePedidoTab({super.key, required this.dashboardController});

  final DashboardController dashboardController;

  @override
  State<CreatePedidoTab> createState() => _CreatePedidoTabState();
}

class _CreatePedidoTabState extends State<CreatePedidoTab> {
  final _formKey = GlobalKey<FormState>();
  final _clienteController = TextEditingController();
  final _direccionController = TextEditingController();
  final _levantadoController = TextEditingController();
  final List<_PedidoItemDraft> _items = [_PedidoItemDraft()];
  bool _sinLevantadoMostrador = false;
  int? _conductorId;
  bool _submitting = false;

  @override
  void dispose() {
    _clienteController.dispose();
    _direccionController.dispose();
    _levantadoController.dispose();
    for (final item in _items) {
      item.dispose();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text('Crear pedido', style: Theme.of(context).textTheme.titleLarge),
                const SizedBox(height: 16),
                Obx(() {
                  final conductores = widget.dashboardController.conductores;
                  if (conductores.isNotEmpty &&
                      (_conductorId == null || !conductores.any((c) => c.id == _conductorId))) {
                    _conductorId = conductores.first.id;
                  }

                  if (conductores.isEmpty) {
                    return const Padding(
                      padding: EdgeInsets.symmetric(vertical: 8),
                      child: Row(
                        children: [
                          SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2)),
                          SizedBox(width: 12),
                          Text('Cargando conductores…'),
                        ],
                      ),
                    );
                  }

                  return DropdownButtonFormField<int>(
                    key: ValueKey(_conductorId),
                    initialValue: _conductorId,
                    items: conductores
                        .map(
                          (item) => DropdownMenuItem<int>(
                            value: item.id,
                            child: Text('${item.nombre} · ${item.email}'),
                          ),
                        )
                        .toList(),
                    onChanged: (value) => setState(() => _conductorId = value),
                    decoration: const InputDecoration(labelText: 'Conductor asignado *'),
                    validator: (value) => value == null ? 'Debes seleccionar un conductor' : null,
                  );
                }),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _clienteController,
                  decoration: const InputDecoration(labelText: 'Cliente'),
                  validator: (value) => (value == null || value.trim().isEmpty) ? 'Ingresa un cliente' : null,
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _direccionController,
                  decoration: const InputDecoration(labelText: 'Dirección completa'),
                  validator: (value) => (value == null || value.trim().isEmpty) ? 'Ingresa una dirección' : null,
                ),
                const SizedBox(height: 12),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  value: _sinLevantadoMostrador,
                  onChanged: (value) => setState(() => _sinLevantadoMostrador = value),
                  title: const Text('Sin levantado en mostrador'),
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _levantadoController,
                  enabled: !_sinLevantadoMostrador,
                  decoration: const InputDecoration(labelText: 'Levantado en mostrador'),
                ),
                const SizedBox(height: 20),
                Text('Items', style: Theme.of(context).textTheme.titleMedium),
                const SizedBox(height: 12),
                ..._items.asMap().entries.map((entry) {
                  return Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: _PedidoItemEditor(
                      draft: entry.value,
                      onRemove: _items.length == 1
                          ? null
                          : () => setState(() => _items.removeAt(entry.key)),
                    ),
                  );
                }),
                Align(
                  alignment: Alignment.centerLeft,
                  child: OutlinedButton.icon(
                    onPressed: () => setState(() => _items.add(_PedidoItemDraft())),
                    icon: const Icon(Icons.add),
                    label: const Text('Agregar item'),
                  ),
                ),
                const SizedBox(height: 16),
                FilledButton(
                  onPressed: _submitting ? null : _submit,
                  child: _submitting
                      ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2))
                      : const Text('Crear pedido'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Future<void> _submit() async {
    if (!(_formKey.currentState?.validate() ?? false)) {
      return;
    }

    final payloadItems = _items
        .where((item) => item.nombreController.text.trim().isNotEmpty)
        .map(
          (item) => {
            'codigo': item.codigoController.text.trim().isEmpty ? null : item.codigoController.text.trim(),
            'nombre': item.nombreController.text.trim(),
            'cantidad': int.tryParse(item.cantidadController.text.trim()) ?? 1,
            'precio': double.tryParse(item.precioController.text.trim()) ?? 0,
          },
        )
        .toList();

    if (payloadItems.isEmpty) {
      Get.snackbar('Pedido', 'Agrega al menos un item válido');
      return;
    }

    try {
      setState(() => _submitting = true);
      await widget.dashboardController.createPedido(
        cliente: _clienteController.text.trim(),
        direccion: _direccionController.text.trim(),
        conductorId: _conductorId!,
        sinLevantadoMostrador: _sinLevantadoMostrador,
        levantadoEnMostrador: _levantadoController.text.trim(),
        items: payloadItems,
      );
      _clienteController.clear();
      _direccionController.clear();
      _levantadoController.clear();
      setState(() {
        _sinLevantadoMostrador = false;
        _conductorId = widget.dashboardController.conductores.isEmpty
            ? null
            : widget.dashboardController.conductores.first.id;
        _items
            ..forEach((item) => item.dispose())
            ..clear()
          ..add(_PedidoItemDraft());
      });
    } catch (error) {
      Get.snackbar('Pedido', error.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) {
        setState(() => _submitting = false);
      }
    }
  }
}

class UsersTab extends StatefulWidget {
  const UsersTab({super.key, required this.dashboardController});

  final DashboardController dashboardController;

  @override
  State<UsersTab> createState() => _UsersTabState();
}

class _UsersTabState extends State<UsersTab> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  String _rol = 'conductor';
  bool _submitting = false;

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Obx(() {
      return ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text('Crear usuario', style: Theme.of(context).textTheme.titleLarge),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _nameController,
                      decoration: const InputDecoration(labelText: 'Nombre'),
                      validator: (value) => (value == null || value.trim().isEmpty) ? 'Ingresa un nombre' : null,
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _emailController,
                      decoration: const InputDecoration(labelText: 'Email'),
                      validator: (value) => (value == null || value.trim().isEmpty) ? 'Ingresa un email' : null,
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _passwordController,
                      obscureText: true,
                      decoration: const InputDecoration(labelText: 'Contraseña'),
                      validator: (value) => (value == null || value.trim().length < 4) ? 'Mínimo 4 caracteres' : null,
                    ),
                    const SizedBox(height: 12),
                    DropdownButtonFormField<String>(
                      initialValue: _rol,
                      items: const [
                        DropdownMenuItem(value: 'conductor', child: Text('Conductor')),
                        DropdownMenuItem(value: 'creador_pedidos', child: Text('Creador de pedidos')),
                        DropdownMenuItem(value: 'admin', child: Text('Administrador')),
                      ],
                      onChanged: (value) => setState(() => _rol = value ?? 'conductor'),
                      decoration: const InputDecoration(labelText: 'Rol'),
                    ),
                    const SizedBox(height: 16),
                    FilledButton(
                      onPressed: _submitting ? null : _submit,
                      child: _submitting
                          ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2))
                          : const Text('Crear usuario'),
                    ),
                  ],
                ),
              ),
            ),
          ),
          const SizedBox(height: 16),
          Text('Usuarios registrados', style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 12),
          ...widget.dashboardController.users.map(
            (user) => Card(
              child: ListTile(
                leading: CircleAvatar(child: Text(user.nombre.isEmpty ? '?' : user.nombre[0].toUpperCase())),
                title: Text(user.nombre),
                subtitle: Text('${user.email}\n${user.rol}'),
                isThreeLine: true,
              ),
            ),
          ),
        ],
      );
    });
  }

  Future<void> _submit() async {
    if (!(_formKey.currentState?.validate() ?? false)) {
      return;
    }

    try {
      setState(() => _submitting = true);
      await widget.dashboardController.createUser(
        nombre: _nameController.text.trim(),
        email: _emailController.text.trim(),
        password: _passwordController.text.trim(),
        rol: _rol,
      );
      _nameController.clear();
      _emailController.clear();
      _passwordController.clear();
      setState(() => _rol = 'conductor');
    } catch (error) {
      Get.snackbar('Usuarios', error.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) {
        setState(() => _submitting = false);
      }
    }
  }
}

class _StatusChip extends StatelessWidget {
  const _StatusChip({required this.status});

  final String status;

  @override
  Widget build(BuildContext context) {
    // Colores mapeados igual que el admin panel (tailwind.config.js)
    final (Color bg, Color fg, String label) = switch (status) {
      'pendiente'         => (const Color(0xFFFEF3C7), const Color(0xFF92400E), 'Pendiente'),
      'asignado'          => (const Color(0xFFDBEAFE), const Color(0xFF1E40AF), 'Asignado'),
      'aceptado'          => (const Color(0xFFD1FAE5), const Color(0xFF065F46), 'Aceptado'),
      'pendiente_entrega' => (const Color(0xFFE0E7FF), const Color(0xFF3730A3), 'En Reparto'),
      'entregado'         => (const Color(0xFFD1FAE5), const Color(0xFF065F46), 'Entregado'),
      'rechazado'         => (const Color(0xFFFEE2E2), const Color(0xFF991B1B), 'Rechazado'),
      _                   => (const Color(0xFFF3F4F6), const Color(0xFF6B7280), status),
    };

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        label,
        style: TextStyle(
          color: fg,
          fontSize: 12,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}

class _InfoPill extends StatelessWidget {
  const _InfoPill({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: const Color(0xFFF1F5F9),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: RichText(
        text: TextSpan(
          style: const TextStyle(fontSize: 12, color: Color(0xFF64748B)),
          children: [
            TextSpan(text: '$label: '),
            TextSpan(
              text: value,
              style: const TextStyle(fontWeight: FontWeight.w600, color: Color(0xFF2D3748)),
            ),
          ],
        ),
      ),
    );
  }
}

class _HomeSection {
  const _HomeSection({required this.label, required this.icon, required this.builder});

  final String label;
  final IconData icon;
  final Widget Function() builder;
}

class _PedidoItemDraft {
  _PedidoItemDraft()
      : codigoController = TextEditingController(),
        nombreController = TextEditingController(),
        cantidadController = TextEditingController(text: '1'),
        precioController = TextEditingController(text: '0');

  final TextEditingController codigoController;
  final TextEditingController nombreController;
  final TextEditingController cantidadController;
  final TextEditingController precioController;

  void dispose() {
    codigoController.dispose();
    nombreController.dispose();
    cantidadController.dispose();
    precioController.dispose();
  }
}

class _PedidoItemEditor extends StatelessWidget {
  const _PedidoItemEditor({required this.draft, this.onRemove});

  final _PedidoItemDraft draft;
  final VoidCallback? onRemove;

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Colors.grey.shade50,
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          children: [
            TextField(
              controller: draft.codigoController,
              decoration: const InputDecoration(labelText: 'Código'),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: draft.nombreController,
              decoration: const InputDecoration(labelText: 'Nombre'),
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: draft.cantidadController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(labelText: 'Cantidad'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: TextField(
                    controller: draft.precioController,
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(labelText: 'Precio'),
                  ),
                ),
              ],
            ),
            if (onRemove != null) ...[
              const SizedBox(height: 8),
              Align(
                alignment: Alignment.centerRight,
                child: TextButton.icon(
                  onPressed: onRemove,
                  icon: const Icon(Icons.delete_outline),
                  label: const Text('Quitar'),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}