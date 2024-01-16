package com.treinetic.whiteshark.services

import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.androidnetworking.interfaces.DownloadProgressListener
import com.treinetic.whiteshark.BuildConfig
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.User
import com.treinetic.google.androidx.gms
import com.treinetic.whiteshark.MyApp
import java.io.File
import java.lang.Exception
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


/**
 * Created by Nuwan on 3/7/19.
 */
class BookDownloadService {

    private val TAG = "BookDownloadService"
    private val bookDataFolder = BuildConfig.APPLICATION_ID
    private val bookBasePath =
        MyApp.getAppContext().filesDir.absolutePath + File.separator + bookDataFolder


    private var map: Map<String, Any> = mapOf()
    lateinit var request: Unit

    companion object {
        private var instance = BookDownloadService()

        fun getInstance(): BookDownloadService {
            return instance
        }
    }


    fun downloadBook(
        token: String? = null,
        book: Book,
        deviceId: String,
        isPreview: Boolean,
        success: (path: String) -> Unit,
        error: (error: NetException) -> Unit,
        progress: (downloaded: Long, total: Long) -> Unit
    ) {

        if (!book.isFill) {
            error(NetException("Cannot find url", "OBJECT_NOT_FILLED", 404))
            return
        }

        try {

            var path =
                if (isPreview) getPreviewBookFolder() else getBookFolder(
                    UserService.getInstance().getUser()!!,
                    book
                )
            var url = if (isPreview) book.book_preview_url else book.book_url
            Log.d(TAG, " book URL : $url")

            var fileName = getFileName(url!!)
            if (!isPreview) fileName = fileName.replace(".epub", "")
            Log.d(TAG, "DownloadPAth : " + path + File.separator + fileName)
            val okHttpClient = OkHttpClient().newBuilder()
                .connectTimeout(3000, TimeUnit.SECONDS)
                .readTimeout(3000, TimeUnit.SECONDS)
                .writeTimeout(3000, TimeUnit.SECONDS)
                .build()

//            var deviceId = "12345"//for testing

            var req = AndroidNetworking.download(url, path, fileName)
                .setTag(book.id)
                .addHeaders("t-device-id", deviceId)
                .addHeaders("Accept-Encoding", "identity")
                .setOkHttpClient(okHttpClient)

            if (token != null) {
                Log.d("BOOKDOWNLOAD", token)
                req.addHeaders("Authorization", token)
            }
            request = req.build()
                .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                    Log.e(TAG, "bytesDownloaded $bytesDownloaded | totalBytes: $totalBytes")
                    progress(bytesDownloaded, totalBytes)
                    book.byteLength = totalBytes.toDouble()
                }

                .startDownload(object : DownloadListener {
                    override fun onDownloadComplete() {
                        Log.d(TAG, "Book Download Success .....")
                        val path = path + File.separator + fileName
                        if (isPreview) {
                            success(path)
                            return
                        }

                        book.localPath = path
                        book.downloadedEpubVersion = book.epubVersion
                        book.validationKey = LocalBookService.VALIDATION_KEY
                        updateUserBooks(book, token!!, success, path)
//                        success(path)
                    }

                    override fun onError(anError: ANError?) {
                        Log.e(TAG, "Book Download FAILED ....")
                        anError?.printStackTrace()

                        var message = "Something went wrong"
                        var code = -1
                        anError?.let {
                            message = if (it.message == null) "" else it.message!!
                        }
                        error(NetException(message, "", code))
                    }
                })


        } catch (e: Exception) {
            e.printStackTrace()
            error(NetException("Something went wrong", "", 400))
        }

    }

    fun updateUserBooks(book: Book, token: String, success: (path: String) -> Unit, path: String) {
        Log.d(TAG, "updateUserBooks ")
        var userId = UserService.getInstance().getUser().user_id
        var key = getEncryptionKey(token)
        LocalBookService.getInstance()
            .saveOrUpdateBook(
                book,
                userId,
                key
            ) { isSuccess: Boolean ->
                Log.d(TAG, "Book saved : $isSuccess")
                success(path)
            }
    }


    fun getUserFolder(user: User): String {

        val userId = UserService.getInstance().getUser().user_id
        val path = bookBasePath + File.separator + userId

        var file = File(path)
        var isCreated = file.mkdirs()


        if (isCreated) {
            Log.d(TAG, " user folder  created $path")
        } else {
            Log.e(TAG, " user folder not created $path")
        }

        return file.absolutePath
    }


    fun getBookFolder(user: User, book: Book): String {
        var userFolder = getUserFolder(user)
        var bookFolder = userFolder + File.separator + book.id

        var file = File(bookFolder)
        var isCreated = file.mkdirs()

        if (isCreated) {
            Log.d(TAG, " book folder  created $bookFolder")
        } else {
            Log.e(TAG, " book folder not created $bookFolder")
        }
        return file.absolutePath
    }

    fun getFileName(url: String): String {
        return url.split(File.separator).last() + ".epub"
//        return url.split(File.separator).last() + ".zip"
    }

    fun getPreviewBookFolder(): String {
        return bookBasePath + File.separator + "preview"
    }

    fun getEncryptionKey(token: String): String {
        val encryptUtil = gms()
        var key = encryptUtil.buildKey(token)
        gms.bookEncryptKey = key
//        Log.d(TAG, "token : $token")
//        Log.d(TAG, "generated key : $key")
        return key
    }


}