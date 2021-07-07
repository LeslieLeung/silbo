package com.ameow.silbo.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.ameow.silbo.encryption.KeyUtils
import com.ameow.silbo.service.message.Message
import com.ameow.silbo.service.message.MessageHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

/**
 * 启动进程与服务器进行websocket通信
 */
class MsgService : Service() {

    lateinit var msgClient: MsgClient
    private val msgClientBinder: MsgClientBinder = MsgClientBinder()

    inner class MsgClientBinder : Binder() {
        fun getService(): MsgService {
            return this@MsgService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return msgClientBinder
    }

    override fun onCreate() {
        super.onCreate()
        try {
            msgClient = object : MsgClient(URI("ws://silbo.ameow.xyz/ws")) {
                override fun onMessage(message: String?) {
                    super.onMessage(message)
                    val intent = Intent("com.ameow.silbo.servicecallback.MESSAGE")
                    var msg = MessageHelper.unpackMessage(message!!)
                    if (msg.extend.get("stage") as String == "4") {
                        val secretKey = getSharedPreferences(
                            "runtime",
                            Context.MODE_PRIVATE
                        ).getString("secretKey", null)
                        val extend = HashMap<String, Any>()

                        extend.put(
                            "msg",
                            KeyUtils.decrypt(
                                (msg.extend.get("msg") as String),
                                KeyUtils.base64ToKey(secretKey!!)
                            )
                        )
                        extend.put("stage", "4")
                        msg.extend = extend
                        intent.putExtra("message", MessageHelper.jsonMessage(msg))
                    } else {
                        intent.putExtra("message", message)
                    }
                    // 通过广播通知activity有新的消息
                    sendBroadcast(intent)
                }

                override fun onOpen(handshakedata: ServerHandshake?) {
                    super.onOpen(handshakedata)
                    val prefs = getSharedPreferences("runtime", Context.MODE_PRIVATE)
                    val fromId = prefs.getString("user_id", null).toString()
                    // 发送上线消息
                    send(MessageHelper.jsonMessage(MessageHelper.connectMessage(fromId)))
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    super.onClose(code, reason, remote)
                    val prefs = getSharedPreferences("runtime", Context.MODE_PRIVATE)
                    val fromId = prefs.getString("user_id", null).toString()
                    // 发送离线消息
                    send(MessageHelper.jsonMessage(MessageHelper.disconnectMessage(fromId)))
                }
            }
            msgClient.connect()
        } catch (e: Exception) {
            Log.d("MsgService", "unable to connect to server")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        msgClient.close()
    }
}