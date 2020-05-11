package com.pechatnikov.telegram.bot.dsget.services.models

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.lang.IllegalArgumentException

data class Task(
    val id: String,
    val size: Long,
    @field:JsonDeserialize(using = TaskStatusDeserializer::class)
    val status: TaskStatus,
    val title: String,
    val type: String,
    val username: String,
    var endTime: String? = null,
    val additional: Additional? = null
) {
    init {
        endTime = estimateEndTime()
    }

    private fun estimateEndTime(): String {
        var estimatedEndTime = "Не определено"
        additional.let { additionalItem ->
            additionalItem?.transfer?.let { transfer ->
                val lostSize = this.size - transfer.size_downloaded
                estimatedEndTime = (lostSize / transfer.speed_download / 60).toString()
            }
        }

        return estimatedEndTime
    }
}

data class Additional(
    val transfer: Transfer
)

data class Transfer(
    val downloaded_pieces: Long,
    val size_downloaded: Long,
    val size_uploaded: Long,
    val speed_download: Long,
    val speed_upload: Long
)

class TaskStatusDeserializer(vc: Class<*>? = null) : StdDeserializer<TaskStatus>(vc) {
    override fun deserialize(parser: JsonParser, ctx: DeserializationContext): TaskStatus {
        val node = parser.codec.readTree<JsonNode>(parser)
        if (node.isTextual) {
            val status = node.asText().toUpperCase()
            return TaskStatus.valueOf(status)
        }
        throw IllegalArgumentException("text representing result code is not found")
    }
}