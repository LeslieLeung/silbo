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
                            // ?????????????????????????????????????????????????????????????????????????????????
                            // ????????????
                            val aPublicKey = clientMsg.extend.get("key") as String
                            // ???????????????
                            val bKeyPair = KeyUtils.genKeyPairByPk(KeyUtils.base64ToKey(aPublicKey))
                            // ??????????????????
                            clientResponseMessage = EncryptionMessage.exchangeStage2Message(
                                KeyUtils.keyToBase64(bKeyPair.public),
                                fromId = clientMsg.toId,
                                toId = clientMsg.fromId
                            )
                            // ??????????????????
                            val secretKey = KeyUtils.getSecretKey(KeyUtils.base64ToKey(aPublicKey), bKeyPair.private.encoded, )
                            val editor = SilboApplication.context.getSharedPreferences("runtime", Context.MODE_PRIVATE).edit()
                            editor.putString("publicKey", KeyUtils.keyToBase64(bKeyPair.public))
                            editor.putString("privateKey", KeyUtils.keyToBase64(bKeyPair.private))
                            editor.putString("secretKey", KeyUtils.keyToBase64(secretKey))
                            editor.apply()
                        }
                        "2" -> {
                            // ???????????????????????????????????????????????????
                            // ????????????
                            val bPublicKey = clientMsg.extend.get("key") as String
                            // ???????????????
                            val aPrivateKey = SilboApplication.context.getSharedPreferences("runtime", Context.MODE_PRIVATE).getString("privateKey", null)
                            // ??????????????????
                            val secretKey = KeyUtils.getSecretKey(KeyUtils.base64ToKey(bPublicKey), KeyUtils.base64ToKey(aPrivateKey!!))
                            // ??????????????????
                            clientResponseMessage = EncryptionMessage.exchangeStage3Message(clientMsg.toId, clientMsg.fromId, secretKey.encoded)

                            val editor = SilboApplication.context.getSharedPreferences("runtime", Context.MODE_PRIVATE).edit()
                            editor.putString("secretKey", KeyUtils.keyToBase64(secretKey))
                            editor.apply()

                        }
                        "3" -> {
                            // ??????????????????
                            val secretKey = SilboApplication.context.getSharedPreferences("runtime", Context.MODE_PRIVATE).getString("secretKey", null)
                            // ????????????
                            val valid = KeyUtils.decrypt((clientMsg.extend.get("msg") as String), KeyUtils.base64ToKey(secretKey!!))
                            if (valid == clientMsg.fromId) {
                                // ????????????
                                clientResponseMessage = EncryptionMessage.encryptedMessage(
                                    "????????????", fromId = clientMsg.toId, toId = clientMsg.fromId, key = KeyUtils.base64ToKey(secretKey)
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