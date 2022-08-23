package ru.pavel.shakhtarin.rdrone.client.extention

import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import ru.pavel.shakhtarin.rdrone.client.rsocket.RSocketData

fun RSocketData.toPayload() = DefaultPayload.create(this.serialize())

fun RSocketData.toPayload(metadata: ByteArray) = DefaultPayload.create(this.serialize(), metadata)

inline fun <reified T : RSocketData> Payload.toMonoObject(): Mono<T> {
    return this.data.array().deserializeData<T>().toMono()
}