package whiteshark.treinetic.com.myapplication.services

import org.junit.Test

/**
 * Created by Nuwan on 3/13/19.
 */
class BookDownloadServiceTest {

    @Test
    fun downloadBook() {
        /*
        val token =
            "token -  bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkNDYzZDEwZC04N2UxLTMxNGMtYjg0Mi04YTY1ZWE3MDVjZDMiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0L0VQVUJfQkFDS0VORC9wdWJsaWMvYXBpL3YxLjAvdGVzdC91c2VyL2Q0NjNkMTBkLTg3ZTEtMzE0Yy1iODQyLThhNjVlYTcwNWNkMyIsImlhdCI6MTU1MjI5OTQ1NCwiZXhwIjoxNTUyOTA0MjU0LCJuYmYiOjE1NTIyOTk0NTQsImp0aSI6IjJoWFlVa21XY0hxcWp0Z0sifQ.FNrxgNVBu-vpJxgU0DhdyI-Z1-hs6-bAb_wE1E97DgQ"
        val url =
            "http://192.168.1.101/EPUB_BACKEND/public/api/v1.0/epub/original/02d87bc5-1e5c-3458-b81b-7276b25be652/Veediya.epub"
        var book: Book = Book(
            id = "02d87bc5-1e5c-3458-b81b-7276b25be652", title = "Veediya",
            ratingValue = 4f,
            ratingsCount = 12,
            priceDetails = PriceDetails(0.00, "", 0.00, 0.00),
            bookImages = arrayListOf(),
            languages = arrayListOf(),
            bookAuthors = arrayListOf()
        )

        BookDownloadService.getInstance().downloadBook(token, book,
            success = {
                println("download completed")
                assertTrue(true)
            }
            , error = {
                assertTrue(it != null)
            }
            , progress = { downloaded, total ->
                println("downloaded $downloaded / total : $total")
            })

        */
    }

    @Test
    fun getUserFolder() {
    }

    @Test
    fun getBookFolder() {
    }

    @Test
    fun getFileName() {
    }
}