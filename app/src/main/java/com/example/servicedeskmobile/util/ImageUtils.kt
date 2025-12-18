package com.example.servicedeskmobile.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    
    fun uriToMultipartBodyPart(context: Context, uri: Uri, partName: String = "images"): MultipartBody.Part? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            
            // Get file name
            val fileName = getFileName(context, uri) ?: "image_${System.currentTimeMillis()}.jpg"
            
            // Create temp file
            val tempFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)
            
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            // Get MIME type
            val mimeType = contentResolver.getType(uri) ?: "image/*"
            
            val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, fileName, requestBody)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun urisToMultipartBodyParts(context: Context, uris: List<Uri>, partName: String = "images"): List<MultipartBody.Part> {
        return uris.mapNotNull { uri ->
            uriToMultipartBodyPart(context, uri, partName)
        }
    }
    
    private fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
    
    fun getImageUrl(filePath: String): String {
        return "http://192.168.1.68:3000/uploads/$filePath"
    }
}
