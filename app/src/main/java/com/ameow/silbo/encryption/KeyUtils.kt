package com.ameow.silbo.encryption

import android.util.Base64.*
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.interfaces.DHPublicKey
import javax.crypto.spec.SecretKeySpec

/**
 * 密钥工具
 */
object KeyUtils {
    private const val KEY_ALGORITHM = "DH"
    private const val SELECT_ALGORITHM = "AES"
    private const val KEY_SIZE = 512

    fun keyToBase64(key: Key) = encodeToString(key.encoded, URL_SAFE)

    fun base64ToKey(base64: String) = decode(base64, URL_SAFE)

    /**
     * 初始化甲方密钥
     */
    fun genKeyPair(): KeyPair {
        // 实例化密钥对生成器
        val generator = KeyPairGenerator.getInstance(KEY_ALGORITHM)
        // 初始化密钥对生成器
        generator.initialize(KEY_SIZE)
        return generator.generateKeyPair()
    }

    /**
     * 初始化乙方密钥
     */
    fun genKeyPairByPk(key: ByteArray): KeyPair {
        // 解析甲方公钥
        // 转换公钥材料
        val x509KeySpec = X509EncodedKeySpec(key)
        // 实例化密钥工厂
        val keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
        // 产生公钥
        val publicKey = keyFactory.generatePublic(x509KeySpec)
        // 由甲方公钥构建乙方密钥
        val dhParameterSpec = (publicKey as DHPublicKey).params
        // 实例化密钥对生成器
        val keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM)
        keyPairGenerator.initialize(dhParameterSpec)
        return keyPairGenerator.generateKeyPair()
    }

    /**
     * 构建本地密钥
     */
    fun getSecretKey(publicKey: ByteArray, privateKey: ByteArray): SecretKey {
        //实例化密钥工厂
        val keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
        //初始化公钥
        //密钥材料转换
        val x509KeySpec = X509EncodedKeySpec(publicKey)
        //产生公钥
        val pubKey = keyFactory.generatePublic(x509KeySpec)
        //初始化私钥
        //密钥材料转换
        val pkcs8KeySpec = PKCS8EncodedKeySpec(privateKey)
        //产生私钥
        val priKey = keyFactory.generatePrivate(pkcs8KeySpec)
        //实例化
        val keyAgree: KeyAgreement = KeyAgreement.getInstance(keyFactory.algorithm)
        //初始化
        keyAgree.init(priKey)
        keyAgree.doPhase(pubKey, true)
        //生成本地密钥
        return keyAgree.generateSecret(SELECT_ALGORITHM)
    }

    /**
     * 将输入通过secretKey加密
     */
    fun encrypt(input: String, key: ByteArray): String {
        val secretKey = SecretKeySpec(key, SELECT_ALGORITHM)
        val cipher = Cipher.getInstance(secretKey.algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypt = cipher.doFinal(input.toByteArray())
        return encodeToString(encrypt, URL_SAFE)
    }

    /**
     * 将输入通过secretKey解密
     */
    fun decrypt(input: String, key: ByteArray): String {
        val secretKey = SecretKeySpec(key, SELECT_ALGORITHM)
        val cipher = Cipher.getInstance(secretKey.algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decrypt = cipher.doFinal(decode(input, URL_SAFE))
        return String(decrypt)
    }

}

