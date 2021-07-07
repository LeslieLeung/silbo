package com.ameow.silbo.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.alibaba.fastjson.JSON
import com.ameow.silbo.SilboApplication
import com.ameow.silbo.encryption.EncryptionMessage
import com.ameow.silbo.encryption.KeyUtils
import com.ameow.silbo.logic.db.AppDataBase
import com.ameow.silbo.service.message.Message
import com.ameow.silbo.service.message.MessageHelper
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

open class MsgClient(serverUri: URI?) : WebSocketClient(serverUri) {

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d("WS client", "opened")
    }

    override fun onMessage(message: String?) {
        if (message != null) {
            Log.d("WS client", "received from server:$message")
        }
        val handler = ClientMessageHandler(JSON.parseObject(message, Message::class.java))
        send(handler.handle())
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d("WS client", "closed")
    }

    override fun onError(ex: Exception?) {
        if (ex != null) {
            Log.d("WS client", ex.printStackTrace().toString())
        }
    }

    interface MessageHandler {
        fun handle(): String?
    }

    class ClientMessageHandler(private val clientMsg: Message) :
        MessageHandler {

        override fun handle(): String? {
            val extend: Map<String, Any> = HashMap()
            var clientResponseMessage: Message? = MessageHelper.errorMessage(
                fromId = clientMsg.toId,
                toId = clientMsg.fromId,
                extend = extend
            )
            when (clientMsg.msgType) {
                "2001" -> {
                    clientResponseMessage = MessageHelper.clientMessage(
                        clientMsg.toId,
                        clientMsg.fromId,
                        "1022",
                        extend
                    )
                    when (clientMsg.extend.get("stage")) {
                        "1" -> {
                            // 甲方生成密钥，乙方根据甲方公钥生成公私钥，发送乙方公钥
                            // 甲方公钥
                            val aPublicKey = clientMsg.extend.get("key") as String
                            // 乙方密钥对
                            val bKeyPair = KeyUtils.genKeyPairByPk(KeyUtils.base64ToKey(aPublicKey))
                            // 发送乙方公钥
                            clientResponseMessage = EncryptionMessage.exchangeStage2Message(
                                KeyUtils.keyToBase64(bKeyPair.public),
                                fromId = clientMsg.toId,
                                toId = clientMsg.fromId
                            )
                            // 生成本地密钥
                            val secretKey = KeyUtils.getSecretKey(KeyUtils.base64ToKey(aPublicKey), bKeyPair.private.encoded, )
                            val editor = SilboApplication.context.getSharedPreferences("runtime", Context.MODE_PRIVATE).edit()
                            editor.putString("publicKey", KeyUtils.keyToBase64(bKeyPair.public))
                            editor.putString("privateKey", KeyUtils.keyToBase64(bKeyPair.private))
                            editor.putString("secretKey", KeyUtils.keyToBase64(secretKey))
                            editor.apply()
                        }
                        "2" -> {
                            // 甲方根据乙方公钥生成甲方的本地密钥
                            // 乙方公钥
                            val bPublicKey = clientMsg.extend.get("key") as String
                            // 读甲方私钥
                            val aPrivateKey = SilboApplication.context.getSharedPreferences("runtime", Context.MODE_PRIVATE).getString("privateKey", null)
                            // 生成本地密钥
                            val secretKey = KeyUtils.getSecretKey(KeyUtils.base64ToKey(bPublicKey), KeyUtils.base64ToKey(aPrivateKey!!))
                            // 发送验证消息
                            clientResponseMessage = EncryptionMessage.exchangeStage3Message(clientMsg.toId, clientMsg.fromId, secretKey.encoded)

                            val editor = SilboApplication.context.getSharedPreferences("runtime", Context.MODE_PRIVATE).edit()
                            editor.putString("secretKey", KeyUtils.keyToBase64(secretKey))
                            editor.apply()

                        }
                        "3" -> {
                            // 回复验证消息
                            val secretKey = SilboApplication.context.getSharedPreferences("runtime", Context.MODE_PRIVATE).getString("secretKey", null)
                            // 解密消息
                            val valid = KeyUtils.decrypt((clientMsg.extend.get("msg") as String), KeyUtils.base64ToKey(secretKey!!))
                            if (valid == clientMsg.fromId) {
                                // 验证成功
                                clientResponseMessage = EncryptionMessage.encryptedMessage(
                                    "加密成功", fromId = clientMsg.toId, toId = clientMsg.fromId, key = KeyUtils.base64ToKey(secretKey)
                                )
                            }
                        }
                        else -> {
                        }
                    }
                }
                else -> clientResponseMessage = MessageHelper.clientMessage(
                    clientMsg.toId,
                    clientMsg.fromId,
                    "1022",
                    extend
                )
            }
            return JSON.toJSONString(clientResponseMessage)
        }

    }
}