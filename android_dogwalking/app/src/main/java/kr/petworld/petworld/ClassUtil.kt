package kr.petworld.petworld

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken

import java.lang.reflect.Type
import java.util.HashMap

/**
 * Created by seminhong on 07/07/2017.
 */

object ClassUtil {
    fun classToMap(target: Any): HashMap<String, Any> {
        val gson = GsonBuilder().registerTypeAdapter(
            Int::class.java,
            JsonSerializer<Int> { src, typeOfSrc, context ->
                if (src == src!!.toInt()) JsonPrimitive(
                    src.toString()
                ) else JsonPrimitive(src)
            }).create()

        val str = gson.toJson(target)
        return gson.fromJson<HashMap<String, Any>>(
            str,
            object : TypeToken<HashMap<String, Any>>() {

            }.rawType
        ) as HashMap<String, Any>
    }
}
