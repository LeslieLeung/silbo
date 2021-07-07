package com.ameow.silbo.service.message

import com.alibaba.fastjson.JSON
import java.util.*
import kotlin.collections.HashMap

/**
 * 消息封装帮助类
 */
object MessageHelper {
    const val SERVER_ID = "0"

    fun clientMessage(fromId: String, toId: String, msgType: String, extend: Map<String, Any>) =
        Message(UUID.randomUUID().toString(), fromId, toId, msgType, "text",
            System.currentTimeMillis().toString(), "0", extend)

    fun errorMessage(fromId: String, toId: String, msgType: String = "4000", extend: Map<String, Any>) =
        Message(UUID.randomUUID().toString(), fromId, toId, msgType, "text",
        System.currentTimeMillis().toString(), "0", extend)

    fun connectMessage(fromId: String) =
        Message(UUID.randomUUID().toString(), fromId, SERVER_ID, "1001", "text",
        System.currentTimeMillis().toString(), "0", HashMap())

    fun disconnectMessage(fromId: String) =
        Message(UUID.randomUUID().toString(), fromId, SERVER_ID, "1002", "text",
        System.currentTimeMillis().toString(), "0", HashMap())

    fun userToUserMessage(fromId: String, toId: String, msg: String): Message {
        val extend = HashMap<String, Any>()
        extend.put("msg", msg)
        return Message(UUID.randomUUID().toString(), fromId, toId, "2001", "text",
            System.currentTimeMillis().toString(), "0", extend)
    }

    fun userToUserMessage(fromId: String, toId: String, extend: Map<String, Any>): Message {
        return Message(UUID.randomUUID().toString(), fromId, toId, "2001", "text",
            System.currentTimeMillis().toString(), "0", extend)
    }

    fun pullMessage(fromId: String) =
        Message(UUID.randomUUID().toString(), fromId, SERVER_ID, "1012", "text",
        System.currentTimeMillis().toString(), "0", HashMap())


    fun jsonMessage(message: Message): String = JSON.toJSONString(message)
    
    fun unpackMessage(msg: String): Message = JSON.parseObject(msg, Message::class.java)

    fun inMessage(msg: String): Any? = unpackMessage(msg).extend.get("msg")
}