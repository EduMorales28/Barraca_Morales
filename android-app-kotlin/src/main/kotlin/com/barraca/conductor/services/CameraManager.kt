package com.barraca.conductor.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCancellableCoroutine

/**
 * Manager para manejar la cámara con CameraX
 */
class CameraManager(private val context: Context) {

    private var imageCapture: ImageCapture? = null

    /**
     * Inicializar cámara con preview
     */
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewSurfaceProvider: androidx.camera.core.Preview.SurfaceProvider,
        onCameraReady: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewSurfaceProvider)
                }

                // Image capture
                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(context.display?.rotation ?: 0)
                    .build()

                // Selector: usar cámara frontal
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Unbind use cases antes de rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases a la cámara
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    onCameraReady()
                } catch (exc: Exception) {
                    onError(exc)
                }
            } catch (exc: Exception) {
                onError(exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Tomar foto y guardarla en archivo
     */
    suspend fun capturePhoto(): Result<File> = suspendCancellableCoroutine { continuation ->
        val imageCapture = imageCapture ?: run {
            continuation.resume(Result.failure(Exception("Cámara no inicializada")))
            return@suspendCancellableCoroutine
        }

        // Crear archivo temporal
        val photoFile = createImageFile()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    continuation.resume(Result.success(photoFile))
                }

                override fun onError(exc: ImageCaptureException) {
                    continuation.resume(Result.failure(exc))
                }
            }
        )
    }

    /**
     * Crear archivo temporal para la foto
     */
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        val storageDir: File? = context.getExternalFilesDir(null)
        return File.createTempFile(
            "FOTO_${timeStamp}",
            ".jpg",
            storageDir
        )
    }

    /**
     * Comprimir imagen
     */
    fun compressImage(
        inputFile: File,
        quality: Int = 85,
        maxWidth: Int = 1920,
        maxHeight: Int = 1080
    ): Result<File> = try {
        // Decodificar bitmap
        val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath)
            ?: return Result.failure(Exception("No se pudo decodificar la imagen"))

        // Reducir tamaño si es necesario
        val scaledBitmap = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val (newWidth, newHeight) = if (bitmap.width > bitmap.height) {
                maxWidth to (maxWidth / aspectRatio).toInt()
            } else {
                (maxHeight * aspectRatio).toInt() to maxHeight
            }

            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        // Guardar comprimido
        val compressedFile = File(
            inputFile.parent,
            "compressed_${inputFile.nameWithoutExtension}.jpg"
        )

        FileOutputStream(compressedFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }

        scaledBitmap.recycle()
        bitmap.recycle()

        Result.success(compressedFile)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Rotar imagen si es necesario
     */
    fun rotateImageIfNeeded(inputFile: File, degrees: Int): Result<File> = try {
        if (degrees == 0) return Result.success(inputFile)

        val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath)
            ?: return Result.failure(Exception("No se pudo decodificar la imagen"))

        val matrix = Matrix().apply {
            postRotate(degrees.toFloat())
        }

        val rotatedBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )

        FileOutputStream(inputFile).use { out ->
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }

        rotatedBitmap.recycle()
        bitmap.recycle()

        Result.success(inputFile)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Obtener tamaño del archivo en MB
     */
    fun getFileSizeMB(file: File): Double {
        return file.length() / (1024.0 * 1024.0)
    }

    /**
     * Eliminar archivo de foto
     */
    fun deletePhotoFile(file: File): Boolean {
        return file.delete()
    }
}
