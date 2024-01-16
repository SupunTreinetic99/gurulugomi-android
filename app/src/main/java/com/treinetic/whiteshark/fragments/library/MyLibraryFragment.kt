package com.treinetic.whiteshark.fragments.library


import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.ImagePreviewActivity
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.activity.splash.SplashActivity
import com.treinetic.whiteshark.adapters.CategoryBookAdapter
import com.treinetic.whiteshark.databinding.FragmentHomeBinding
import com.treinetic.whiteshark.databinding.FragmentMyLibraryBinding
import com.treinetic.whiteshark.dialog.BottomDialogs
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.models.Category
import com.treinetic.whiteshark.services.BookService
import com.treinetic.whiteshark.services.LocalStorageService
import com.treinetic.whiteshark.services.UserService
import com.treinetic.whiteshark.util.PixelConvert
import com.treinetic.whiteshark.util.ProcessEpub
import java.io.File
import java.util.ArrayList


class MyLibraryFragment : BaseFragment(), View.OnClickListener {

    private val logTag = "MyLibraryFragment"
    private var _binding : FragmentMyLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    //    private lateinit var myLibraryList: RecyclerView
    private lateinit var bookAdapter: CategoryBookAdapter
    private lateinit var model: MyLibraryViewModel
    private lateinit var processEpub: ProcessEpub
    lateinit var dialog: BottomSheetDialog
    private var isDownloadBookCalled = false
    var cancelBtn: TextView? = null
    lateinit var bottomDialogs: BottomDialogs
    private var isBookObserved: Boolean = false
    var optionMenu: Menu? = null
    var refreshInProgress = false
    var isInitialLoad = true


    companion object {
        fun newInstance(): MyLibraryFragment {
            return MyLibraryFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model = ViewModelProvider(requireActivity())[MyLibraryViewModel::class.java]
        _binding = FragmentMyLibraryBinding.inflate(inflater, container, false)
        mainView = binding.root
        setHasOptionsMenu(true)
        bottomDialogs = BottomDialogs()
        binding.shopMoreBtn.setOnClickListener(this)
        binding.myLibraryBooks.showLoading()
        model.isMyLibfetched = false
        processEpub = ProcessEpub()
        processEpub.initFolioReader()
        processEpub.onReaderClosed = ::onReaderClosed
        setHasOptionsMenu(true)
        setupToolBar()
        observeFetchBook()
        setupMyLibrary()
        progressInit()
        setBottomBar()

        binding.shopMoreBtn.setOnClickListener { clickShopMore() }
        binding.goOnline.setOnClickListener { goOnline() }
        networkListerner = {
            setBackOnline()
        }
        return mainView
    }


    fun onReaderClosed() {
        try {
            isDownloadBookCalled = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.shopMoreBtn -> clickShopMore()
        }
    }


    private fun setBottomBar() {
        if (UserService.isOfflineMode()) {
            binding.bottomCard.visibility = View.GONE
            binding.offlineMessage.visibility = View.VISIBLE
            return
        }

        binding.bottomCard.visibility = View.VISIBLE
        binding.offlineMessage.visibility = View.GONE

    }

    private fun clickShopMore() {
        FragmentNavigation.getInstance()
            .startHomeFragment(requireFragmentManager(), R.id.fragment_view)
    }

    private fun setupMyLibrary() {
        model.fetchMyLibrary()
        model.getMyLibrary().observe(viewLifecycleOwner, Observer<Books> { books ->
            refreshInProgress = false
            binding.myLibraryBooks.hideLoading()
            initMyLibrary(books)
        })

        model.getException().observe(viewLifecycleOwner, Observer<NetException> { e ->

            e?.let {
                it.printStackTrace()
                if (!isErrorHandled(it)) {
                    showErrorSnackBar(binding.myLibraryBooks, "Something went wrong")
                }
            }
        })
    }

    private fun openBookInReader(book: Book) {

        BookService.getInstance().setHighLightFont(book)
        if (model.isAlreadyFetched(book) || UserService.isOfflineMode()) {
            openBook(book)
        } else {
            showPrepareDialog()
            model.fetchBook(book)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initMyLibrary(books: Books) {
        if (books.data.isEmpty()) {
            binding.myLibraryBooks.showNoDataView()
            return
        } else {
            binding.myLibraryBooks.hideNoDataView()
            binding.myLibraryBooks.hideLoading()

        }

        if (model.isMyLibfetched) {
            bookAdapter.bookList = books.data
            bookAdapter.notifyDataSetChanged()
            return
        }
        model.isMyLibfetched = true
        val category = Category(null, null, null, books)

        bookAdapter = CategoryBookAdapter(
            CategoryBookAdapter.CategoryTypes.IMAGE_ONLY,
            category,
            true
        )
        bookAdapter.enableDownloadIndicator = UserService.isOfflineMode()
        bookAdapter.onClick = { index, book, view ->
            //            BookService.getInstance().setHighLightFont(book)
//            if (model.isAlreadyFetched(book) || UserService.isOfflineMode()) {
//                openBook(book)
//            } else {
//                model.fetchBook(book)
//            }
            isInitialLoad = false
            openBookInReader(book)

        }
        bookAdapter.onLongPress = { index, book ->
        }

        bookAdapter.endPosition = { url, id ->
            model.getNextPage()
        }

        bookAdapter.onPopupMenuClick = { index, book, view, item ->
            when (item.itemId) {
                R.id.action_profile -> {
                    openBookInReader(book)
//                    FragmentNavigation.getInstance()
//                        .startBookProfile(requireFragmentManager(), R.id.fragment_view, book)
//                    model.resetBook()
                }
                R.id.action_review -> {
                    FragmentNavigation.getInstance()
                        .startAddReview(requireFragmentManager(), R.id.fragment_view, book)
                    model.resetBook()
                }
                R.id.action_delete -> {
//                    model.deleteDownloadedBook(book) {
//                        requireActivity().runOnUiThread {
//                            if (it) {
//                                showSuccessSnackBar(mainView.parentView, "Removed Downloaded file")
//                            } else {
//                                showErrorSnackBar(mainView.parentView, "Failed to remove Downloaded file")
//                            }
//                        }
//                    }
//                    deleteDownloadedFile(book)
                    showDeleteConfirm(book)
                }
            }
        }


        binding.myLibraryBooks.recyclerView.apply {
            layoutManager = GridLayoutManager(
                context,
                PixelConvert(requireActivity()).bookInRow(resources.getDimension(R.dimen.book_width))
            )
            adapter = bookAdapter
        }

    }

    fun showDeleteConfirm(book: Book) {
        bottomDialogs.getConfirmDialog(
            requireContext(),
            "Do you want to remove downloaded copy of this book? ",
            "This book will not be removed from your purchase history and can be downloaded free when needed",
            "Ok",
            {
                deleteDownloadedFile(book)
                it.dismiss()
            },
            "Cancel",
            {
                it.dismiss()
            }
        ).show()
    }


    fun deleteDownloadedFile(book: Book) {
        if (UserService.isOfflineMode()) {
            if (book.localPath == null) {
                showOfflineModeDilaog()
                return
            }
        }
        model.deleteDownloadedBook(book) {
            requireActivity().runOnUiThread {
                if (it) {
                    showSuccessSnackBar(binding.parentView, "Removed Downloaded file")
                } else {
                    showErrorSnackBar(binding.parentView, "Failed to remove Downloaded file")
                }
            }
        }


    }

    private fun showOfflineModeDilaog() {
        requireActivity().runOnUiThread {
            bottomDialogs.getBookNotDownLoadedDialog(
                requireContext(),
                resources.getString(R.string.offline_mode),
                resources.getString(R.string.this_action_is_not_allowed)
            ).show()
        }
    }

    var progressBar: ProgressBar? = null
    private fun progressInit() {
        dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.download_progress_layout)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        progressBar = dialog.findViewById(R.id.progressBar)
        cancelBtn = dialog.findViewById(R.id.cancelBtn)
        cancelBtn?.setOnClickListener { downloadcancel() }
    }

    private fun downloadcancel() {
        model.getDownloadBook()?.let {
            AndroidNetworking.cancel(it.id)
            isDownloadBookCalled = false
        }
        dialog.dismiss()
    }

    private fun openBook(book: Book) {
        val isOffline = UserService.isOfflineMode()
        if (!book.isFill && !isOffline) {
            showPrepareDialog()
            model.fetchBook(book)
//            observeFetchBook()
            return
        }
        if (isOffline) {
            if (book.localPath != null && !File(book.localPath!!).exists()) {
                showNotAvailableDilaog()
                return
            }
        }
        Log.d(logTag, "openBook calling ->")
        showPrepareDialog()
        book.getLocallyAvailable { isAvailable, bookData ->
            if (isAvailable) {
                bookData?.getBookObj()?.localPath?.let {
                    if (it.contains(".epub")) {
                        bookData.getBookObj()?.deleteBook()
                        startDownloaBbook(book)
                        Log.d(logTag, "Deleted all ")
                        return@getLocallyAvailable
                    }
                }
            }

            var isUpdated = false
            bookData?.getBookObj()?.let {
                if (isOffline) {
                    isUpdated = true
                    return@let
                }
                isUpdated = it.isBookUpdated(model.fullBook!!.epubVersion!!)
            }
            if (isAvailable && isUpdated) {

                openLocalBook(book)
                // this part moved to openLocalBook()
                /*
                bookData?.let {
                    book.localPath = it.getBookObj()?.localPath
                    requireActivity()?.runOnUiThread {
                        processEpub.onClickImage = ::showImage
                        processEpub.openEpub(
                            requireContext(),
                            book,
                            processEpub.folioReader,
                            book.localPath!!,
                            bookData.key,
                            true
                        )
                    }
                    return@getLocallyAvailable
                }
                */

            } else {
                if (!isOffline) {
                    requireActivity().runOnUiThread {
                        if (!isDownloadBookCalled) {
                            if (isAvailable) {
                                showNewVersionAvailable(book)
                                return@runOnUiThread
                            }
                            startDownloaBbook(book)
                        } else {
                            isDownloadBookCalled = false
                        }
                    }
                } else {
                    showNotAvailableDilaog()
                }
            }
        }
    }

    private fun showPrepareDialog() {
        if (dialog != null && !dialog.isShowing && isAdded) {
            Handler(Looper.getMainLooper()).post {
                progressBar?.progress = 0
                dialog.show()
            }
        }
    }

    private fun showNewVersionAvailable(book: Book) {

        bottomDialogs.getConfirmDialog(
            requireContext(),
            "New version available",
            "Book has an updated version. Do you need to download new version ?",
            "Yes",
            { dialog: BottomSheetDialog ->
                dialog.dismiss()
                startDownloaBbook(book)
            },
            "No",
            { dialog: BottomSheetDialog ->
                dialog.dismiss()
                openLocalBook(book)
            }
        ).show()
    }

    private fun openLocalBook(book: Book) {

        book.getLocallyAvailable { isAvailable, bookData ->
            bookData?.let {
                book.localPath = it.getBookObj()?.localPath
                processEpub.prepareBookForReading(bookData) { path, isSuccess ->
                    requireActivity().runOnUiThread {
                        if (!isSuccess) {
                            dialog?.dismiss()
                            Log.e(logTag, "prepareBookForReading failed")
                            return@runOnUiThread
                        }
                        dialog.setCancelable(true)
                        processEpub.onClickImage = ::showImage
                        processEpub.openEpub(
                            requireContext(),
                            book,
                            processEpub.folioReader,
                            path!!,
                            bookData.key,
                            true
                        )
                    }
                }
            }
        }
    }

    fun showImage(url: String, context: Context?) {
        try {
            val images = ArrayList<String>()
            images.add(url)
            Log.d(logTag, "showImage $url")
            Handler(Looper.getMainLooper()).post {
                requireActivity().startActivity(
                    Intent(
                        requireContext(),
                        ImagePreviewActivity::class.java
                    ).apply {
                        putExtra("url", url)
                    })
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNotAvailableDilaog() {
        requireActivity().runOnUiThread {
            dialog.dismiss()
            bottomDialogs.getBookNotDownLoadedDialog(
                requireContext(),
                resources.getString(R.string.not_available_in_offline),
                resources.getString(R.string.this_book_is_not_available)
            ).show()
        }
    }

    private fun startDownloaBbook(book: Book) {
        isDownloadBookCalled = true
        showPrepareDialog()
        val token = LocalStorageService.getInstance().token
        model.downloadBook(book, false, token) { path: String, isPreview: Boolean ->
            //            var token: String? = LocalStorageService.getInstance().getToken()
            /*if (token == null) {
                hidePrepareDialog()
                return@downloadBook
            }
            val key = processEpub.getEncryptionKey(token)
            Log.d(
                TAG,
                "startDownloaBbook finish token : ${token}"
            )
            Log.d(TAG, "startDownloaBbook finish key : $key")
            processEpub.saveToDB(book, UserService.getInstance().getUser().user_id, key) {
                requireActivity()?.runOnUiThread {
                    //                    dialog.dismiss()
                    openBook(book)
                }
            }*/

            openBook(book)

        }
        model.getProgress().observe(viewLifecycleOwner, Observer {
            Log.d(logTag, "downloaded : $it")
            it?.let {
                progressBar?.progress = it.toInt()
            }

        })
    }

    private fun observeFetchBook() {
        model.getFetchBookException().observe(viewLifecycleOwner, Observer { exception: NetException ->
            exception.let {

                if (isErrorHandled(it)) {
                    return@let
                }

                var msg = "Something went wrong"
                exception.message?.let {
                    msg = it
                }
                showErrorSnackBar(mainView, msg)
            }
        })

        model.getbook().observe(viewLifecycleOwner, Observer { book: Book ->
            book.let {
                if (!isInitialLoad)
                    openBook(book)
                isInitialLoad = false
                return@Observer
            }
            Log.e(logTag, "Book NOT available")
        })
    }


    private fun setupToolBar() {

        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = resources.getString(R.string.my_library)

        (activity as AppCompatActivity).setSupportActionBar(toolBar)

        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar

        actionbar?.apply {
            if (UserService.isOfflineMode()) {
                setDisplayHomeAsUpEnabled(false)
                return
            }
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        optionMenu = menu
        setOptionMenuVisibility(
            menu,
            false,
            false,
            false,
            false,
            false, isConnected
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                Log.d(logTag, "onOptionsItemSelected item clicked ...")
                refreshInProgress = true
                binding.myLibraryBooks.showLoading()
                model.fetchMyLibrary()
                return false
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setBackOnline() {

        Log.e(logTag, "setBackOnline")
        if (!UserService.isOfflineMode()) return

        if (isConnected) {
            animateHide(binding.offlineMessage) {
                animateShow(binding.onlineMessage)
            }

        } else {
            animateHide(binding.onlineMessage) {
                animateShow(binding.offlineMessage)
            }
        }

    }

    private fun animateHide(view: View, onFinish: (view: View) -> Unit = {}) {

        val height = view.height
        view.animate()
            .translationY(height.toFloat())
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    onFinish(view)
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationStart(animation: Animator) {

                }
            })
    }

    private fun animateShow(view: View, onFinish: (view: View) -> Unit = {}) {

        if (view.visibility != View.VISIBLE) {
            view.translationX = view.height.toFloat()
            view.visibility = View.VISIBLE
        }
        view.animate()
            .translationY(0f)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    onFinish(view)
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationStart(animation: Animator) {
                }
            })
    }


    private fun goOnline() {
        BookService.getInstance().myLibrary = null
        startActivity(Intent(requireContext(), SplashActivity::class.java))
    }

    override fun onPause() {
        super.onPause()
        Log.d(logTag, "onPause calling ->")
    }

    override fun onResume() {
        super.onResume()
        if (dialog != null && dialog.isShowing) dialog.dismiss()
    }

}
