package com.example.animaldex.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import com.example.animaldex.model.Animal
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.text.Normalizer
import org.json.JSONObject

internal data class Identification(val scientificName: String?, val commonName: String?)

internal fun matchAnimal(animals: List<Animal>, identification: Identification): Animal? {
    fun normalize(value: String): String = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "").lowercase().replace("\\s+".toRegex(), " ")
    val scientific = identification.scientificName?.takeIf { it.isNotBlank() }?.let(::normalize)
    if (scientific != null) {
        animals.singleOrNull { normalize(it.scientificName) == scientific }?.let { return it }
    }
    val common = identification.commonName?.takeIf { it.isNotBlank() }?.let(::normalize)
    if (common != null) {
        val matches = animals.filter { animal ->
            listOfNotNull(animal.nameFr, animal.commonNameEN).any { names ->
                names.split('|').any { normalize(it) == common }
            }
        }
        if (matches.size == 1) return matches.first()
    }
    return null
}

internal fun compressPhoto(file: File): ByteArray {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Photo illisible" }
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1600) sample *= 2
    val bitmap = BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample })
        ?: error("Photo illisible")
    val orientation = ExifInterface(file.absolutePath).getAttributeInt(
        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
    )
    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
        }
    }
    val oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    val scale = minOf(1f, 1280f / maxOf(oriented.width, oriented.height))
    val resized = if (scale < 1f) Bitmap.createScaledBitmap(
        oriented, (oriented.width * scale).toInt(), (oriented.height * scale).toInt(), true
    ) else oriented
    val bytes = ByteArrayOutputStream().use { output ->
        resized.compress(Bitmap.CompressFormat.JPEG, 78, output)
        output.toByteArray()
    }
    if (resized !== oriented) resized.recycle()
    if (oriented !== bitmap) oriented.recycle()
    bitmap.recycle()
    return bytes
}

internal fun identifyPhoto(context: Context, jpeg: ByteArray): Identification {
    val prefs = context.getSharedPreferences("scanner_config", Context.MODE_PRIVATE)
    val apiKey = prefs.getString("openai_api_key", "") ?: ""
    require(apiKey.isNotBlank()) { "Entre ta clé API dans Caméra → ⚙." }
    val schema = JSONObject()
        .put("type", "object")
        .put("properties", JSONObject()
            .put("scientific_name", JSONObject().put("type", org.json.JSONArray().put("string").put("null")))
            .put("common_name", JSONObject().put("type", org.json.JSONArray().put("string").put("null"))))
        .put("required", org.json.JSONArray().put("scientific_name").put("common_name"))
        .put("additionalProperties", false)
    val content = org.json.JSONArray()
        .put(JSONObject().put("type", "input_text").put("text",
            "Identifie l'animal principal sur cette photo. Donne le nom scientifique binomial le plus précis justifiable visuellement et son nom commun. Si l'image est ambiguë ou sans animal, retourne null pour les deux. Ne devine pas l'espèce."))
        .put(JSONObject().put("type", "input_image")
            .put("image_url", "data:image/jpeg;base64," + Base64.encodeToString(jpeg, Base64.NO_WRAP))
            .put("detail", "high"))
    val payload = JSONObject()
        .put("model", "gpt-5-mini")
        .put("store", false)
        .put("reasoning", JSONObject().put("effort", "low"))
        .put("max_output_tokens", 500)
        .put("input", org.json.JSONArray().put(JSONObject().put("role", "user").put("content", content)))
        .put("text", JSONObject().put("format", JSONObject()
            .put("type", "json_schema").put("name", "animal_identification")
            .put("strict", true).put("schema", schema)))
    val connection = java.net.URL("https://api.openai.com/v1/responses").openConnection() as HttpURLConnection
    try {
        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 60000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
        val code = connection.responseCode
        val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()?.use { it.readText() } ?: ""
        if (code !in 200..299) {
            val apiError = runCatching { JSONObject(body).getJSONObject("error").optString("message") }.getOrNull()
            error("OpenAI ($code) : ${apiError?.take(160) ?: "vérifie ta clé API et ton crédit."}")
        }
        val response = JSONObject(body)
        val outputs = response.getJSONArray("output")
        var resultText: String? = null
        for (i in 0 until outputs.length()) {
            val item = outputs.getJSONObject(i)
            if (item.optString("type") != "message") continue
            val parts = item.optJSONArray("content") ?: continue
            for (j in 0 until parts.length()) {
                val part = parts.getJSONObject(j)
                if (part.optString("type") == "output_text") resultText = part.optString("text")
            }
        }
        val result = JSONObject(resultText ?: error("Aucune réponse d'identification."))
        return Identification(
            if (result.isNull("scientific_name")) null else result.optString("scientific_name").ifBlank { null },
            if (result.isNull("common_name")) null else result.optString("common_name").ifBlank { null }
        )
    } finally { connection.disconnect() }
}