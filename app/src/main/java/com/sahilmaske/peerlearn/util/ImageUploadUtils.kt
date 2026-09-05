package com.sahilmaske.peerlearn.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min

object ImageUploadUtils {
    
    suspend fun uploadToCloudinary(context: Context, imageUri: Uri): String {
        val cloudinaryCloudName = "db7wneko6"
        val cloudinaryUploadPreset = "peerlearn_avatar"

        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext ""
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                // Crop to a centered square
                val squareSide = min(originalBitmap.width, originalBitmap.height)
                val cropStartX = (originalBitmap.width - squareSide) / 2
                val cropStartY = (originalBitmap.height - squareSide) / 2
                val squareBitmap = Bitmap.createBitmap(originalBitmap, cropStartX, cropStartY, squareSide, squareSide)

                val resizedBitmap = Bitmap.createScaledBitmap(squareBitmap, 400, 400, true)
                val jpegOutputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, jpegOutputStream)
                val jpegBytes = jpegOutputStream.toByteArray()

                val uploadUrl = URL("https://api.cloudinary.com/v1_1/$cloudinaryCloudName/image/upload")
                val multipartBoundary = "Boundary-${System.currentTimeMillis()}"
                val connection = uploadUrl.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$multipartBoundary")

                val requestBody = connection.outputStream
                requestBody.write("--$multipartBoundary\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\n$cloudinaryUploadPreset\r\n".toByteArray())
                requestBody.write("--$multipartBoundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"avatar.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n".toByteArray())
                requestBody.write(jpegBytes)
                requestBody.write("\r\n--$multipartBoundary--\r\n".toByteArray())
                requestBody.flush()

                val responseBody = connection.inputStream.bufferedReader().readText()
                JSONObject(responseBody).getString("secure_url")
                    .replace("/upload/", "/upload/w_400,h_400,c_thumb,g_face/")
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
}
