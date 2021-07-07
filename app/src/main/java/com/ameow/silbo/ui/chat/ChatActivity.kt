package com.ameow.silbo.ui.chat

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.ameow.silbo.R
import com.ameow.silbo.SilboApplication
import com.ameow.silbo.encryption.EncryptionMessage
import com.ameow.silbo.encryption.KeyUtils
import com.ameow.silbo.logic.db.AppDataBase
import com.ameow.silbo.logic.db.entity.ChatHistory
import com.ameow.silbo.logic.model.Msg
import com.ameow.silbo.service.MsgClient
import com.ameow.silbo.service.MsgService
import com.ameow.silbo.service.message.MessageHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.java_websocket.exceptions.WebsocketNotConnectedException


class ChatActivity : AppCompatActivity() {
    private val msgList = ArrayList<Msg>()

    private lateinit var adapter: MsgAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var client: MsgClient
    private lateinit var binder: MsgService.MsgClientBinder
    private lateinit var service: MsgService

    private lateinit var fromId: String
    private lateinit var toId: String

    private lateinit var chatMessageReceiver: ChatMessageReceiver

    lateinit var db: AppDataBase

    private var isDebug: Int = 0

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            binder = iBinder as MsgService.MsgClientBinder
            service = binder.getService()
            client = service.msgClient
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // TODO remove temp var
        val prefs = getSharedPreferences("runtime", Context.MODE_PRIVATE)
        fromId = prefs.getString("user_id", "").toString()
        if (fromId == "1") {
            toId = "3"
        } else {
            toId = "1"
        }

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        bindService()
        doRegisterReceiver()

        layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        val recyclerView: RecyclerView = findViewById(R.id.chatRecyclerView)
        recyclerView.layoutManager = layoutManager

        // 初始化读取聊天记录的db
        db = Room.databaseBuilder(
            this,
            AppDataBase::class.java, "CHATHISTORY"
        ).build()
        initMsg()
        adapter = MsgAdapter(msgList)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(msgList.size - 1)

        val btnSend: Button = findViewById(R.id.send)
        val inputText: EditText = findViewById(R.id.inputText)
        btnSend.setOnClickListener {
            // 发送消息
            val msg = inputText.text.toString()
            msgList.add(Msg(msg, Msg.TYPE_SENT))
            try {
                // 调用ws client发送封装的消息
                val pref = getSharedPreferences("runtime", MODE_PRIVATE)
                val secretKey = pref.getString("secretKey", null)
                val rawMsg = MessageHelper.jsonMessage(
                    EncryptionMessage.encryptedMessage(
                        msg,
                        fromId,
                        toId,
                        KeyUtils.base64ToKey(secretKey!!)
                    )
                )
                client.send(rawMsg)
                inputText.setText("")

                GlobalScope.launch {
                    db.chatHistoryDao().insertChatHistory(
                        ChatHistory(
                            id = null,
                            fromId = fromId,
                            message = msg,
                            timestamp = System.currentTimeMillis().toString(),
                            type = Msg.TYPE_SENT
                        )
                    )
                }
                adapter.notifyItemInserted(adapter.itemCount)
                recyclerView.scrollToPosition(msgList.size - 1)
            } catch (ex: WebsocketNotConnectedException) {
                // 处理未连接服务端的异常
                Toast.makeText(this, "server not connected, please retry", Toast.LENGTH_SHORT)
                    .show()
                Log.d("ws server", "server not connected")
                // 尝试重启msgService 重新连接服务器
                val msgServiceIntent = Intent(this, MsgService::class.java)
                stopService(msgServiceIntent)
                startService(msgServiceIntent)
            }
        }

        val btnGenKey: Button = findViewById(R.id.btnGenKey)
        btnGenKey.setOnClickListener {
            val keyPair = KeyUtils.genKeyPair()
            val privateKey = KeyUtils.keyToBase64(keyPair.private)
            val publicKey = KeyUtils.keyToBase64(keyPair.public)

            // 保存在pref中
            val editor = getSharedPreferences("runtime", Context.MODE_PRIVATE).edit()
            editor.putString("privateKey", privateKey)
            editor.putString("publicKey", publicKey)
            editor.apply()
            Log.d("key", "generated new key")
        }

        val btnEncrypt: Button = findViewById(R.id.btnEncrypt)
        btnEncrypt.setOnClickListener {
            // 读取密钥对
            val pref = getSharedPreferences("runtime", MODE_PRIVATE)
            val publicKey = pref.getString("publicKey", null)
            // 发送第一阶段消息
            val rawMsg = MessageHelper.jsonMessage(
                EncryptionMessage.exchangeStage1Message(
                    publicKey!!,
                    fromId,
                    toId
                )
            )
            msgList.add(Msg(rawMsg, Msg.TYPE_SENT))
            try {
                client.send(rawMsg)
                GlobalScope.launch {
                    db.chatHistoryDao().insertChatHistory(
                        ChatHistory(
                            id = null,
                            fromId = fromId,
                            message = rawMsg,
                            timestamp = System.currentTimeMillis().toString(),
                            type = Msg.TYPE_SENT
                        )
                    )
                }
                adapter.notifyItemInserted(adapter.itemCount)
                recyclerView.scrollToPosition(msgList.size - 1)
            } catch (ex: WebsocketNotConnectedException) {
                // 处理未连接服务端的异常
                Toast.makeText(this, "server not connected, please retry", Toast.LENGTH_SHORT)
                    .show()
                Log.d("ws server", "server not connected")
                // 尝试重启msgService 重新连接服务器
                val msgServiceIntent = Intent(this, MsgService::class.java)
                stopService(msgServiceIntent)
                startService(msgServiceIntent)
            }

        }

        val btnShow: Button = findViewById(R.id.btnShow)
        btnShow.setOnClickListener {
            if (isDebug == 0) {
                Toast.makeText(this, "调试信息已开启", Toast.LENGTH_SHORT).show()
                isDebug = 1
                btnShow.text = "隐藏详情"
            } else {
                Toast.makeText(this, "调试信息已隐藏", Toast.LENGTH_SHORT).show()
                isDebug = 0
                btnShow.text = "显示详情"
            }

        }

        val btnShowKey: Button = findViewById(R.id.btnShowKey)
        btnShowKey.setOnClickListener {
            val pref = getSharedPreferences("runtime", MODE_PRIVATE)
            val secretKey = pref.getString("secretKey", "")
            val publicKey = pref.getString("publicKey", "")
            val privateKey = pref.getString("privateKey", "")
            val rawMsg = "【调试信息】正在查看密钥\n本地密钥secret:${secretKey}\n公钥:${publicKey}私钥:${privateKey}"
            msgList.add(Msg(rawMsg, Msg.TYPE_SENT))
            adapter.notifyItemInserted(adapter.itemCount)
            recyclerView.scrollToPosition(msgList.size - 1)
        }

    }

    private fun bindService() {
        val bindIntent = Intent(SilboApplication.context, MsgService::class.java)
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE)
    }

    /**
     * 从数据库中读消息初始化界面
     */
    private fun initMsg() {
        if (msgList.isEmpty()) {
            GlobalScope.launch {
                val msgs = db.chatHistoryDao().getAll()
                Log.d("db", msgs.toString())
                for (msg in msgs) {
                    msgList.add(Msg(msg.message, msg.type))
                }
            }
        }
    }

    /**
     * 注册广播接收
     */
    private fun doRegisterReceiver() {
        chatMessageReceiver = ChatMessageReceiver()
        val filter = IntentFilter("com.ameow.silbo.servicecallback.MESSAGE")
        registerReceiver(chatMessageReceiver, filter)
    }

    inner class ChatMessageReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val message = intent.getStringExtra("message")
            val msg: Msg
            if (isDebug == 1) {
                msg = message?.let { Msg(it, Msg.TYPE_RECEIVED) }!!
            } else {
                val inMsg = message?.let { MessageHelper.inMessage(it) }
                msg = Msg(inMsg as String, Msg.TYPE_RECEIVED)
            }

            msgList.add(msg)
            adapter.notifyItemInserted(adapter.itemCount)
            layoutManager.scrollToPositionWithOffset(adapter.itemCount - 1, Int.MIN_VALUE)


            GlobalScope.launch {
                db.chatHistoryDao().insertChatHistory(
                    ChatHistory(
                        null,
                        fromId,
                        System.currentTimeMillis().toString(),
                        msg.content,
                        Msg.TYPE_RECEIVED
                    )
                )
            }
        }

    }
}