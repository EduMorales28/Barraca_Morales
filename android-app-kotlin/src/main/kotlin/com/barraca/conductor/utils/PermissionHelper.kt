package com.barraca.conductor.utils

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat

/**
 * Helper para manejar permisos en tiempo real
 */
class PermissionHelper(private val context: Context) {

    /**
     * Verificar si un permiso está concedido
     */
    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verificar múltiples permisos
     */
    fun hasPermissions(vararg permissions: String): Boolean {
        return permissions.all { hasPermission(it) }
    }

    companion object {
        const val CAMERA = Manifest.permission.CAMERA
        const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
        const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        const val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    }
}

/**
 * Comosable para solicitar permiso de cámara
 * @param onPermissionGranted callback cuando se concede el permiso
 * @param onPermissionDenied callback cuando se deniega el permiso
 * @param content composable a mostrar cuando el permiso está concedido
 */
@Composable
fun CameraPermissionComposable(
    onPermissionGranted: (Boolean) -> Unit,
    onPermissionDenied: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted(true)
            content()
        } else {
            onPermissionDenied(true)
        }
    }

    // Solicitar permiso automáticamente
    androidx.compose.runtime.LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }
}

/**
 * Composable para solicitar múltiples permisos
 */
@Composable
fun MultiplePermissionsComposable(
    permissions: List<String>,
    onPermissionsGranted: (Map<String, Boolean>) -> Unit,
    content: @Composable () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        onPermissionsGranted(results)
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        launcher.launch(permissions.toTypedArray())
    }
}
