package com.ameow.silbo.encryption

import com.ameow.silbo.service.message.Message
import com.ameow.silbo.service.message.MessageHelper

/**
 * 加密消息封装
 */
object EncryptionMessage {

    /**
     * 加密第一阶段，甲方生成公私钥，发送甲方公钥
     */
    fun exchangeStage1Message(publicKey: String, fromId: String, toId: String): Message {
        val extend = HashMap<String, Any>()
        extend.put("stage", "1")
        extend.put("key", publicKey)
        extend.put("msg", "")
        return MessageHelper.userToUserMessage(fromId, toId, extend)
    }

    /**
     * 加密第二阶段，乙方用甲方公钥生成公私钥，发送乙方公钥
     */
    fun exchangeStage2Message(publicKey: String, fromId: String, toId: String): Message {
        val extend = HashMap<String, Any>()
        extend.put("stage", "2")
        extend.put("key", publicKey)
        extend.put("msg", "")
        return MessageHelper.userToUserMessage(fromId, toId, extend)
    }

    /**
     * 加密第三阶段，发送验证消息
     */
    fun exchangeStage3Message(fromId: String, toId: String, key: ByteArray): Message {
        val extend = HashMap<String, Any>()
        extend.put("stage", "3")
        // 验证消息中的消息为自己的id加密
        extend.put("msg", KeyUtils.encrypt(fromId, key))
        return MessageHelper.userToUserMessage(fromId, toId, extend)
    }

    /**
     * 完成密钥交换，开始加密聊天
     */
    fun encryptedMessage(msg: String, fromId: String, toId: String, key: ByteArray): Message {
        val extend = HashMap<String, Any>()
        extend.put("stage", "4")
        extend.put("msg", KeyUtils.encrypt(msg, key))
        return MessageHelper.userToUserMessage(fromId, toId, extend)
    }
}