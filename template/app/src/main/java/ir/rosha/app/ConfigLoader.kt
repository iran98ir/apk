package ir.rosha.app

import android.content.Context
import org.json.JSONObject

object ConfigLoader {

    private var config: JSONObject? = null

    fun load(context: Context): JSONObject {
        if (config != null) return config!!

        val json = context.assets.open("config.json")
            .bufferedReader()
            .use { it.readText() }

        config = JSONObject(json)
        return config!!
    }

    fun get(): JSONObject = config ?: JSONObject()

    fun string(path: String, def: String = ""): String {
        return try {
            val parts = path.split(".")
            var obj: JSONObject = config!!
            for (i in 0 until parts.size - 1) {
                obj = obj.getJSONObject(parts[i])
            }
            obj.optString(parts.last(), def)
        } catch (e: Exception) { def }
    }

    fun int(path: String, def: Int = 0): Int {
        return try {
            val parts = path.split(".")
            var obj: JSONObject = config!!
            for (i in 0 until parts.size - 1) {
                obj = obj.getJSONObject(parts[i])
            }
            obj.optInt(parts.last(), def)
        } catch (e: Exception) { def }
    }

    fun bool(path: String, def: Boolean = false): Boolean {
        return try {
            val parts = path.split(".")
            var obj: JSONObject = config!!
            for (i in 0 until parts.size - 1) {
                obj = obj.getJSONObject(parts[i])
            }
            obj.optBoolean(parts.last(), def)
        } catch (e: Exception) { def }
    }
}
