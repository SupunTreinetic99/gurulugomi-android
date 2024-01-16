package whiteshark.treinetic.com.myapplication.util

import android.app.Application
import android.app.Instrumentation
import android.graphics.BitmapFactory

import android.util.Log
import com.treinetic.whiteshark.MyApp
import com.treinetic.whiteshark.util.ProcessEpub
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Test
import java.util.*

class ProcessEpubTest {

    @Test
    fun testReplaceBase64ImageWithUrl() {

        var inputStream = javaClass.getResourceAsStream("image_epub_file.xhtml")
        var content = inputStream.bufferedReader().use { it.readText() }
        println(content)
        assert(content != null)
        replaceBase64ImagesWithUrl(content)
    }

    fun replaceBase64ImagesWithUrl(html: String) {
        var doc: Document = Jsoup.parse(html)
        var elements = doc.getElementsByTag("img")
        elements?.forEach {
            var base64Image = it.attr("src")
            println("base64Image")
            println(base64Image)
            saevBase64ToImage(base64Image)
            var url: String = "localhost/myimage.png"
            it.attr("src", url)

        }
        println()
        println("********************** \n")
        println(doc.toString())
    }



    fun saevBase64ToImage(base64Image: String){
//        var pureBase64Encoded = base64Image.substring(base64Image.indexOf(",") + 1)
        var pureBase64Encoded = base64Image.replace("data:image/png;base64,","")
        println(base64Image)
        println(pureBase64Encoded)
        var decodedString = Base64.getDecoder().decode(pureBase64Encoded.toByteArray())

        var bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        assert(bitmap!=null)
    }


}