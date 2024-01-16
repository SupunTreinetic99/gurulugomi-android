package whiteshark.treinetic.com.myapplication

import com.treinetic.whiteshark.util.extentions.mcg
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        var i =""
        i= arrayListOf(
            121,
            148,
            154,
            172,
            120,
            181,
            150,
            178,
            137,
            150,
            183,
            121,
            153,
            184,
            166,
            171,
            175,
            184,
            135,
            145,
            137,
            144,
            125,
            173,
            174,
            187,
            181,
            168,
            139,
            185,
            170,
            152,
            149,
            121,
            166,
            126,
            185,
            159,
            118,
            152,
            138,
            141,
            151,
            169
        ).mcg()

//         forEach { c->
//            i += (c - 0x45).toChar()
//        }

        println("key = $i")
        assertEquals("4OUg3pQmDQr4TsafjsBLDK8hivpcFteSP4a9tZ1SEHRd", i)
    }
    @Test
    fun keyGenerator(){
//        val id = "4p87Wt7FlBs8bMSnEu2duK4uWDK76bUn58W8tqxjS9HJ" // dev
        val id = "4OUg3pQmDQr4TsafjsBLDK8hivpcFteSP4a9tZ1SEHRd" // prod
        val intArr: MutableList<Int> = ArrayList()
        for (c in id.toCharArray()) {
            intArr.add((c + 0x45).toInt())
        }
        println(intArr)
    }

}
