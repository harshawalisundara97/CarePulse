package com.carepulse.app.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Manages the single profile photo stored in the app's internal files dir.
 * No cloud storage needed — the photo lives only on this device.
 */
object ProfilePhotoManager {

    private const val PREFS = "carepulse_prefs"
    private const val KEY_PHOTO_PATH = "profile_photo_path"
    private const val PHOTO_FILENAME = "profile_photo.jpg"
    private const val CAMERA_TEMP = "camera_temp.jpg"

    /** Returns the saved photo path, or null if none set. */
    fun getSavedPhotoPath(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PHOTO_PATH, null)

    /** Returns the file Uri this app uses for camera capture output. */
    fun cameraOutputUri(context: Context): Uri {
        val tempFile = File(context.cacheDir, CAMERA_TEMP)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    }

    /**
     * Copies a photo (from gallery URI or camera temp file) into internal
     * storage and saves the absolute path to SharedPreferences.
     * Returns the new file path on success, null on failure.
     */
    fun savePhoto(context: Context, sourceUri: Uri): String? = runCatching {
        val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val dest = File(context.filesDir, PHOTO_FILENAME)
        dest.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }

        val path = dest.absolutePath
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_PHOTO_PATH, path).apply()
        path
    }.getOrNull()

    /** Clears the saved photo (reverts to generated avatar). */
    fun clearPhoto(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY_PHOTO_PATH).apply()
        File(context.filesDir, PHOTO_FILENAME).delete()
    }
}
