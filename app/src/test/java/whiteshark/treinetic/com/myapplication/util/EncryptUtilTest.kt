package whiteshark.treinetic.com.myapplication.util

import com.treinetic.google.androidx.gms
import org.junit.Test

import org.junit.Assert.*

/**
 * Created by Nuwan on 3/8/19.
 */
class EncryptUtilTest {

    val TAG = "EncryptUtilTest"
    var encryptUtil: gms

    init {
        encryptUtil = gms()
    }

    @Test
    fun encrypt() {
    }

    @Test
    fun decrypt() {
    }

    @Test
    fun generateKey() {
    }

    @Test
    fun generateAlias() {
    }

    @Test
    fun generateSecretKey() {
    }

    @Test
    fun encryptRSA() {
    }

    @Test
    fun decryptRSA() {
    }

    @Test
    fun buildKey() {
        val token =
            "bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmOGQwZDYyMDQ5M2QxMWU5ODg0ZDEzNWE1YjY4MTNjOSIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3QvRVBVQl9CQUNLRU5EL3B1YmxpYy9hcGkvdjEuMC9sb2dpbiIsImlhdCI6MTU1Mjg4NjY2NiwiZXhwIjoxNTUzNDkxNDY2LCJuYmYiOjE1NTI4ODY2NjYsImp0aSI6IlllY3Rla3dwWk1BOHlVN1MifQ.ZZACQd9foIVXmtIfTXcstvwmo9ej1bIPcagRrDy24Ww"
        val expectedKey = "WFiVzIQiy0HI1MTjJiIlIljjoz6zNOHi4M2iEtLXlO20VeDcCdtLkCTJ1pxdz6JWTUOvYiAkiE"
        val generatedKey = encryptUtil.buildKey(token)
        println("generated key : $generatedKey")
        assertTrue(expectedKey.equals(generatedKey))

    }

//    @Test
//    fun testRSA() {
//        val myString = "nuwan"
//        encryptUtil.generateAlias()
//        var encryptByteArray = encryptUtil.encryptRSA(myString.toByteArray())
//        println(" encrypted : " + encryptByteArray.toString())
//        var decryptByteArray = encryptUtil.decryptRSA(encryptByteArray)
//        println(" decrypted : " + decryptByteArray.toString())
//
//        assert(myString.equals(decryptByteArray.toString()))
//
//    }
}