package com.ameow.silbo.ui.login

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.core.view.isGone
import com.ameow.silbo.R
import com.ameow.silbo.logic.model.Request
import com.ameow.silbo.logic.model.Response
import com.ameow.silbo.logic.network.SilboNetwork
import com.ameow.silbo.logic.network.UserService
import com.ameow.silbo.service.MsgService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.RuntimeException

class LoginActivity : AppCompatActivity() {
    private var switchStatus = 0 // 0为登录状态，1为注册状态

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin: Button = findViewById(R.id.login)
        val btnRegister: Button = findViewById(R.id.register)
        val repeatLayout: LinearLayout = findViewById(R.id.repeat)
        val usernameEditText: EditText = findViewById(R.id.username)
        val passwordEditText: EditText = findViewById(R.id.password)

        // 标题渐变
        val loginTitle: TextView = findViewById(R.id.loginTitle)
        val paint = loginTitle.paint
        val width = paint.measureText(loginTitle.text.toString())
        val textShader: Shader = LinearGradient(
            0f,
            0f,
            width,
            loginTitle.textSize,
            intArrayOf(Color.parseColor("#FFB8B8"), Color.parseColor("#6C48D2")),
            null,
            Shader.TileMode.CLAMP
        )
        loginTitle.paint.shader = textShader

        btnLogin.setOnClickListener {
            if (switchStatus == 0) {
                GlobalScope.launch(Dispatchers.Main) {
                    val data = HashMap<String, String>()
                    data.put("name", usernameEditText.text.toString())
                    data.put("password", passwordEditText.text.toString())
                    val request = Request(data)
                    val resp: Response
                    try {
                        resp = SilboNetwork.login(request)
                        if (resp.code == "0") {
                            Toast.makeText(applicationContext, "登录成功", Toast.LENGTH_SHORT).show()
                            val editor =
                                getSharedPreferences("runtime", Context.MODE_PRIVATE).edit()
                            editor.putString("username", usernameEditText.text.toString())
                            editor.putString("user_id", resp.data.get("user_id"))
                            editor.putString("isLoggedIn", "1")
                            editor.apply()

                            // 启动MsgService
                            Log.d("MsgService", "starting msgService")
                            val msgServiceIntent =
                                Intent(applicationContext, MsgService::class.java)
                            startService(msgServiceIntent)

                            finish()
                        } else {
                            Toast.makeText(applicationContext, resp.msg, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: RuntimeException) {
                        Toast.makeText(applicationContext, "网络暂时不可用，请重试", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                GlobalScope.launch(Dispatchers.Main) {
                    val name = usernameEditText.text.toString()
                    val password = passwordEditText.text.toString()
                    val repeatEditText: EditText = findViewById(R.id.repeatPassword)
                    val repeat = repeatEditText.text.toString()

                    if (!password.equals(repeat)) {
                        Toast.makeText(applicationContext, "两次密码不一致，请重试", Toast.LENGTH_SHORT).show()
                    } else {
                        val data = HashMap<String, String>()
                        data.put("name", name)
                        data.put("password", password)
                        val request = Request(data)
                        val resp = SilboNetwork.register(request)
                        if (resp.code == "0") {
                            Toast.makeText(applicationContext, "注册成功，请前往登录", Toast.LENGTH_SHORT)
                                .show()
                            btnLogin.text = "登录"
                            btnRegister.text = "注册"
                            repeatLayout.isGone = true
                            switchStatus = 0
                        } else {
                            Toast.makeText(applicationContext, resp.msg, Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }


        btnRegister.setOnClickListener {
            if (switchStatus == 0) {
                // 登录状态转为注册状态
                // 注册状态为返回键
                btnLogin.text = "注册"
                btnRegister.text = "返回"
                repeatLayout.isGone = false
                switchStatus = 1
            } else {
                // 注册状态转为登录状态
                btnLogin.text = "登录"
                btnRegister.text = "注册"
                repeatLayout.isGone = true
                switchStatus = 0
            }

        }

    }

    override fun onBackPressed() {
        // 屏蔽返回键 未登录不允许操作
    }
}