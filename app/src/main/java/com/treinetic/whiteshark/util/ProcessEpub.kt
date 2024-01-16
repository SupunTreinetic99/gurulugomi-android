package com.treinetic.whiteshark.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Handler
import android.util.Base64
import android.util.Log
import androidx.annotation.Keep
//import com.crashlytics.android.Crashlytics
import com.folioreader.Config
import com.folioreader.FolioReader
import com.folioreader.model.HighLight
import com.folioreader.model.locators.ReadLocator
import com.folioreader.util.AppUtil
import com.folioreader.util.BookMarkListener
import com.folioreader.util.OnHighlightListener
import com.folioreader.util.ReadLocatorListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.treinetic.google.androidx.Decryptor
import com.treinetic.google.androidx.FasterDecryptor
import com.treinetic.google.androidx.Shifter
import com.treinetic.google.androidx.gms
import com.treinetic.whiteshark.MyApp
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.roomdb.models.BookData
import com.treinetic.whiteshark.services.BookService
import com.treinetic.whiteshark.services.LocalBookService
import com.treinetic.whiteshark.services.LocalStorageService
import com.treinetic.whiteshark.util.extentions.getFileName
import com.treinetic.whiteshark.util.extentions.moveTo
import com.treinetic.whiteshark.util.extentions.save
import com.treinetic.whiteshark.util.extentions.saveBytes
import ir.mahdi.mzip.zip.ZipArchive
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.readium.r2.streamer.config.Configurations
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread

/**
 * Created by Nuwan on 3/29/19.
 */
@Keep
class ProcessEpub : ReadLocatorListener, FolioReader.OnClosedListener, OnHighlightListener,
    BookMarkListener {

    val readLocationPath =
        MyApp.getAppContext().filesDir.absolutePath + File.separator + "readBooks"
    private val TAG = "ProcessEpub"
    private var encrypted = false

    lateinit var folioReader: FolioReader
    private var currentbook: Book? = null
    var onClickImage: ((url: String, context: Context?) -> Unit)? = null
    var onReaderClosed: (() -> Unit)? = null
    fun initFolioReader() {
        FolioReader.clear()
        if (!::folioReader.isInitialized) {
            folioReader = FolioReader.get()

        }

        folioReader.setOnHighlightListener(this)
            .setReadLocatorListener(this)
            .setBookMarkListener(this)
            .setOnClosedListener(this)


    }

    override fun onHighlight(highlight: HighLight?, type: HighLight.HighLightAction?) {

    }

    override fun onFolioReaderClosed() {
        Log.d(TAG, " calling onFolioReaderClosed ...")
        FolioReader.clear()
        onReaderClosed?.let { it() }
        deleteAllFilesInDir(readLocationPath)
    }

    override fun saveReadLocator(readLocator: ReadLocator?) {
        Log.d(TAG, " calling saveReadLocator ...")
//        readLocator?.let {
//            Log.d(TAG, "Calling read locator")
//            Log.d(TAG, it.toJson())
//            currentbook?.let { book ->
//                saveReadLocation(book.id, it.toJson()!!)
//            }
//
//        }

    }

    override fun saveBookMark(readLocator: ReadLocator?) {
        Log.d(TAG, " calling saveBookMark ...")
        if (!encrypted) return
        readLocator?.let {
            Log.d(TAG, "Calling saveBookMark read locator")
            Log.d(TAG, it.toJson()?:" no json")
            currentbook?.let { book ->
                saveReadLocation(book.id, it.toJson()!!)
            }

        }
    }

    fun openEpub(
        context: Context,
        book: Book,
        folioReader: FolioReader,
        path: String,
        key: String,
        isEncrypted: Boolean
    ) {
        Log.d(TAG, "openEpub calling . path : $path")
        initFolioReader()
        encrypted = isEncrypted
        currentbook = book
        setReadLocation(folioReader)
        var config = AppUtil.getSavedConfig(context)
        if (config == null)
            config = Config()
        config.allowedDirection = Config.AllowedDirection.VERTICAL_AND_HORIZONTAL
        if (isEncrypted) {
            config.isNeedExport = isEncrypted

            config.password = key
            config.callback = object : Configurations.Callback {
                override fun export(input: String): String {
                    var exported = gms()
                        .decryptEpubFile(input, config.password)
                    exported?.let {
                        Log.d(TAG, "Content Export success")
//                        exported = replaceBase64ImagesWithUrl(book, it)
                        return it
                    }
                    Log.e(TAG, "Content Export Failed")
                    return input
                }
            }
        }

        config.setBookTitle(book.title)
        config.isShowBookmark = isEncrypted
        config.isEnableHighLight = isEncrypted
        config.isAutoSaveReadLocator = false
        config.setShowTts(false)
        config.imageClickListener = object : Configurations.ImageClickListener {
            override fun onImageClick(url: String, context: Context?) {
                Log.d(TAG, "onImageClick invoked ${context==null}")
                Log.d(TAG, "onImageClick invoked method ${onClickImage==null}")
                Handler().post {
                    //requireActivity().startActivity(Intent(requireContext(),))
                    Log.d(TAG, "onImageClick  : $url")
                    onClickImage?.let { it(url, context) }
                }

            }

        }
//        setHighLightFont(config, book, context)
        BookService.getInstance().setHighLightFont(book)
        folioReader.setConfig(config, true)
            .openBook(path, book.id)

    }


    private fun setReadLocation(folioReader: FolioReader) {
        getSaveReadLocation(currentbook!!.id)?.let {
            folioReader.setReadLocator(it)
        }
    }


    fun replaceBase64ImagesWithUrl(book: Book, html: String): String {
        var doc: Document = Jsoup.parse(html)
        var elements = doc.getElementsByTag("img")
        elements?.forEach {
            var base64Image = it.attr("src")
            var url: String = saveImages(book, base64Image)
            it.attr("src", url)
        }
        Log.d(TAG, "Replace Html Content")
        Log.d(TAG, doc.toString())
        return doc.toString()
    }

    fun saveImages(book: Book, base64Image: String): String {
        var basePath: String =
            MyApp.context.filesDir.absolutePath + File.separator + "Images" + File.separator + book.id
        var compressFormat = Bitmap.CompressFormat.JPEG
        var extention = when {
            base64Image.contains("image/jpg") -> {
                ".jpg"
            }
            base64Image.contains("image/png") -> {
                compressFormat = Bitmap.CompressFormat.PNG
                ".png"
            }
            base64Image.contains("image/jpeg") -> {
                ".jpeg"
            }
            else -> {
                ".jpg"
            }
        }
        var path =
            "$basePath${File.separator}${File.separator}${System.currentTimeMillis()}-image$extention"
        var file = File(path)
        File(basePath).mkdirs()

        var pureBase64Encoded =
            base64Image.substring(base64Image.indexOf(",") + 1)
        Log.d(TAG, base64Image)
        Log.d(TAG, pureBase64Encoded)
        var decodedString: ByteArray =
            Base64.decode(pureBase64Encoded.toByteArray(), Base64.DEFAULT)
        var bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        var fos = FileOutputStream(file)
        bitmap.compress(compressFormat, 100, fos)
        fos.flush()
        fos.close()
        return path
    }


//    private fun setHighLightFont(config: Config, book: Book, context: Context) {
//        try {
//            book?.bookFont?.first()?.fontUrl?.let { url: String ->
//                Uri.parse(url).lastPathSegment?.let {
//                    val fileExt = it.split("")[1]
//                    val fontName = book.bookFont.first().id + "" + fileExt
//
//                    val file = File(context?.filesDir, fontName)
//                    config.fontPath = file.path
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//    }

    fun saveToDB(book: Book, userId: String, key: String, onFinish: () -> Unit) {
        LocalBookService.getInstance()
            .saveOrUpdateBook(book, userId, key) { isSuccess: Boolean ->
                Log.d(TAG, "Book saved : $isSuccess")
                onFinish()
            }

    }

    fun getEncryptionKey(token: String): String {
        val encryptUtil = gms()
        var key = encryptUtil.buildKey(token)
        gms.bookEncryptKey = key
//        Log.d(TAG, "token : $token")
//        Log.d(TAG, "generated key : $key")
        return key
    }


    fun saveReadLocation(bookId: String, json: String) {
        LocalStorageService.getInstance().saveReadLocation(bookId, json)
    }

    fun getSaveReadLocation(bookId: String): ReadLocator? {
        val json: String? = LocalStorageService.getInstance().getSavedReadLocation(bookId)

        json?.let {
            Log.d(TAG, "Have saved json : \n $it")
            return Gson().fromJson(json, ReadLocator::class.java)
        }

        return null
    }

    fun prepareBookForReading(
        bookData: BookData,
        onFinish: (path: String?, isSuccess: Boolean) -> Unit
    ) {
        var path = bookData.getBookObj()?.localPath!!
        Log.d(TAG, "file path : $path")
        if (path.contains(".epub")) {
            Log.d(TAG, "old file")
            onFinish(path, true)
            return
        }
        FirebaseCrashlytics.getInstance().log("book : ${bookData.getBookObj()?.title} id: ${bookData.getBookObj()?.id}")
        Log.d(TAG, "bytes : ${bookData.getBookObj()?.byteLength} ")
        thread(start = true) {
            /*var savedPath = if (!path.contains(".epub")) {
                extractZipFile(
                    bookData?.getBookObj()?.localPath ?: "",
                    readLocationPath,
                    bookData.key, bookData.getBookObj()!!
                )
            } else {
                saveBookToInternalStorage(path)
            }*/
            var savedPath = saveBookToInternalStorage(path, bookData.key)
            Log.d(TAG, "process path : $savedPath")
            onFinish(savedPath, savedPath != null)
        }
    }

    fun saveBookToInternalStorage(path: String, key: String): String? {
        try {
            var fileName = path.split(File.separator).last()
            if (!fileName.contains(".epub")) fileName = fileName + ".epub"
            var basePath = File(readLocationPath)
            basePath.mkdirs()
            deleteAllFilesInDir(readLocationPath)
            basePath.mkdirs()
//        var file = File(readLocationPath + File.separator + fileName)
            var file = File(path)
            var outputFile = File(readLocationPath + File.separator + fileName)
//        var d = FasterDecryptor(file.absolutePath, key).decrypt(outputFile.absolutePath)
            var dc = Decryptor(file.absolutePath, key).decrypt().save(outputFile.absolutePath)
            return outputFile.absolutePath
//        return File(path).moveTo(readLocationPath, fileName)
            /*try {
                var fin = FileInputStream(File(path))
                var fos = FileOutputStream(file)
                fin.copyTo(out = fos)
                fos.flush()
                fos.close()
                return file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun bytesToFile() {

//        var fos = FileOutputStream(outputFile)
//        var bos = BufferedOutputStream(fos)
//        bos.write(bytes)
//        bos.flush()
//        bos.close()

    }


    fun extractZipFile(path: String, destination: String, key: String = "", book: Book): String? {
        deleteAllFilesInDir(readLocationPath)
        var destinationDir = File(destination)
        if (!destinationDir.exists()) destinationDir.mkdirs()
//        deleteAllFilesInDir(destination)
        ZipArchive.unzip(path, destination, key)
        var fileDir = File(destination)
        if (!fileDir.isDirectory) return ""

        var rawFile = File(destination + File.separator + book.id)
        if (!rawFile.exists()) {
            Log.e(TAG, "raw file not available")
            return null
        }
        return rawFile.moveTo(destination, rawFile.absolutePath.getFileName() + ".epub")

    }


    fun deleteAllFilesInDir(path: String) {
        try {
            var fileDir = File(path)
            if (fileDir.isDirectory) {
                fileDir.listFiles().forEach {
                    it.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}