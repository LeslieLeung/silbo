package com.ameow.silbo.service.message

/**
 * 消息结构定义
 */
data class Message(
    val msgId: String,
    val fromId: String,
    val toId: String,
    val msgType: String,
    val msgContentType: String,
    val timestamp: String,
    val statusReport: String,
    var extend: Map<String, Any>
)
