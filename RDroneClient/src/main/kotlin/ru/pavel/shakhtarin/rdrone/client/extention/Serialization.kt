package ru.pavel.shakhtarin.rdrone.client.extention

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import ru.pavel.shakhtarin.rdrone.client.rsocket.RSocketData
import java.io.ByteArrayOutputStream

fun RSocketData.serialize():ByteArray {
    val outStream = ByteArrayOutputStream()
    ObjectMapper().writeValue(outStream, this)
    return outStream.toByteArray()
}

inline fun <reified T : RSocketData> ByteArray.deserializeData(): T {
    val objectMapper = ObjectMapper()
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    return objectMapper.readValue(this, T::class.java)
}