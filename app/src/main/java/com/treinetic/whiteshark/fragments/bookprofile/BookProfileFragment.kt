package com.treinetic.whiteshark.fragments.bookprofile


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.androidnetworking.AndroidNetworking
import com.folioreader.Config
import com.folioreader.FolioReader
import com.folioreader.model.HighLight
import com.folioreader.model.locators.ReadLocator
import com.folioreader.util.AppUtil
import com.folioreader.util.OnHighlightListener
import com.folioreader.util.ReadLocatorListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.treinetic.google.androidx.gms
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.ImagePreviewActivity
import com.treinetic.whiteshark.activity.LoginActivity
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.ViewPagerAdapter
import com.treinetic.whiteshark.constance.Contants
import com.treinetic.whiteshark.databinding.FragmentBookProfileBinding
import com.treinetic.whiteshark.dialog.BottomDialogs
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.fragments.addreview.AddReviewFragment
import com.treinetic.whiteshark.glide.GlideApp
import com.treinetic.whiteshark.models.*
import com.treinetic.whiteshark.roomdb.models.BookData
import com.treinetic.whiteshark.services.*
import com.treinetic.whiteshark.util.ProcessEpub
import com.treinetic.whiteshark.util.extentions.toCurrency
import kotlinx.coroutines.launch

import org.readium.r2.streamer.config.Configurations
import java.io.File


class BookProfileFragment : BaseFragment(), View.OnClickListener, OnHighlightListener,
    ReadLocatorListener,
    FolioReader.OnClosedListener {
    enum class Actions {
        ADD_TO_CART, ADD_TO_LIBRARY, READ_NOW
    }

    private val logTag = "BookProfileFragment"
    private val TYPE_PROFILE = "PROFILE"
    private lateinit var currentBook: Book
    private var cartService = CartService.getInstance()
    private lateinit var viewPageAdapter: ViewPagerAdapter
    private var _binding : FragmentBookProfileBinding? = null
    private val binding get() = _binding!!
    lateinit var mainView: View
    private lateinit var bookId: String
    private var optionMenu: Menu? = null
    private var profileImage: BookImages? = null
    var imageWidth: Int = 0
    lateinit var folioReader: FolioReader
    val processEpub = ProcessEpub()
    var bottomDialogs = BottomDialogs()

    lateinit var dialog: BottomSheetDialog
    lateinit var model: BookProfileViewModel

    companion object {
        fun newInstance(book: Book): BookProfileFragment {
            val instance = BookProfileFragment()
            instance.currentBook = book
            return instance
        }

        lateinit var initbook: Book
        lateinit var font: File
        lateinit var optionMenus: Menu


    }

    fun getCurrentBook(): Book {
        return currentBook
    }

    override fun onResume() {
        super.onResume()
        setupToolBar()
        fetchBook()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        postponeEnterTransition()
        _binding = FragmentBookProfileBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[BookProfileViewModel::class.java]
        processEpub.initFolioReader()
        binding.reviewSection.visibility = View.GONE

        try {
            model.currentBook = initbook

        } catch (e: UninitializedPropertyAccessException) {
            Log.e(logTag, "UninitializedPropertyAccessException caught")
            activity?.onBackPressed()
            return mainView
        }

        if (!::currentBook.isInitialized && model.currentBook != null) currentBook =
            model.currentBook!!
        model.isFetchBookCalled = false

        binding.btnPurchaseLayout.visibility = View.INVISIBLE
        updatePurchaseStatus()
        setProfileImage(currentBook)
        imageWidth = resources.getDimensionPixelSize(R.dimen.profile_image_width)
        setImageRatio()
        setupToolBar()
        if (activity is MainActivity) optionMenu = (activity as MainActivity).optionMenus
        binding.bottomSection.visibility = View.GONE

        model.bookInit(initbook)
        bookId = currentBook.id

        initWishListData()
        setHasOptionsMenu(true)
        showException()

        getRate()
 //       fetchBook()

        observeRatings()

        binding.btnPurchase.setOnClickListener(this)
        enableClick(binding.btnPurchase)
        binding.allReviews.setOnClickListener(this)
        binding.button.setOnClickListener(this)
        changeButton()
        initFolioReader()
        startPostponedEnterTransition()
        progressInit()
        if (!isUserLogged()) {
            model.cartItemCount.value = 0
            model.getLocalCartItemCount()
            observeLocalCartItemCount()
        }
        refreshBookObserve()
        observeLibraryData(binding)
        processEpub.onClickImage = ::showImage
        observerRemoveFromToWishList()
        return mainView

    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        val json_book = Gson().toJson(initbook)
//        outState.putString("initbook", json_book)
//        Log.d(logTag, "init book json saved")
//
//    }
//
//    override fun onViewStateRestored(savedInstanceState: Bundle?) {
//        super.onViewStateRestored(savedInstanceState)
//        Log.d(logTag, "inside onViewStateRestored")
//        savedInstanceState?.let {
//            it.getString("initbook")?.let { json ->
//                initbook = Gson().fromJson(json, Book::class.java)
//                Log.d(logTag, "init book found onViewStateRestored")
//            }
//
//        }
//
//    }



    private fun observeRatings() {
        model.loadingCallBack = {
            showHideLoading(it)
        }
        model.ratingSuccess.observe(viewLifecycleOwner, Observer<MutableList<Rating>> { rate ->
            rate?.let {
                showHideLoading(false)
                currentBook.let {
                    currentBook.ratings = rate
                    currentBook.setIsMyReview()
                    initViewPager(currentBook)
                    topReviewedUsers(currentBook)
                }
                model.ratingSuccess.value = null
            }
        })
    }

    private fun updatePurchaseStatus() {
        BookService.getInstance().myLibrary?.let {
            initbook.isPurchased = BookService.getInstance().isPurchasedBook(initbook.id)
            if (initbook.isPurchased && initbook.book_url == null) {
                initbook.isFill = false
            }
        }
    }

    private fun setImageRatio() {

        binding.bookImage.apply {
            profileImage?.let {
                if (it.size == 0.00) return
            }

            layoutParams.width = imageWidth
            layoutParams.height = getImageHeight().toInt()
        }
    }

    private fun getImageHeight(): Double {
        if (currentBook.getBookImage() == "") {
            return imageWidth.toDouble()
        } else {
            try {
                profileImage?.size?.let {
                    return imageWidth / it
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return imageWidth.toDouble()
    }

    private fun initFolioReader() {
        folioReader = FolioReader.get().setOnHighlightListener(this)
            .setReadLocatorListener(this)
            .setOnClosedListener(this)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
//        Handler(Looper.getMainLooper()).postDelayed({
//            model.currentBook?.let { showFreeBookDialog(it) }
//        }, 300)

    }

    private fun getRate() {
        model.callBack = { message ->

            showSuccessSnackBar(mainView, resources.getString(R.string.delete_review_success))
            currentBook.apply {
                ratingOfUser = null
                ratings.removeAt(0)
            }
            initbook = currentBook
            model.getBook().value = currentBook
            initViewPager(currentBook)
        }
    }

    private fun setValues() {
        initbook.let {
            currentBook = it
            if (!currentBook.isFill) {
                bookId = it.id
            }
        }

        try {
            GlideApp.with(binding.bookImage)
                .load(profileImage?.image?.getImage()).placeholder(R.drawable.book_placeholder)
                .into(binding.bookImage)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }


        showPrintedPrice()


        binding.bookName.text = currentBook.title
        var authors: String? = ""
        var categories = ""

        for ((index, author) in currentBook.bookAuthors.withIndex()) {
            authors += if (currentBook.bookAuthors.size - 1 == index) {
                author.user.name
            } else {
                author.user.name + ","
            }
        }

        var translators: String? = ""
        for ((index, translator) in currentBook.translators.withIndex()) {
            translators += if (currentBook.translators.size - 1 == index) {
                translator.name
            } else {
                translator.name + ","
            }
        }

        currentBook.categories?.let {
            for ((index, category) in it.withIndex()) {
                categories += if (index == it.size - 1) {
                    category.category
                } else {
                    category.category + ","
                }
            }
        }

        binding.bookAuthor.text = authors
        currentBook.priceDetails.let {
            binding.bookPrice.text = it.visiblePrice.toCurrency(it.currency)
        }
        if (currentBook.isFree) {
            context?.let {
                binding.bookPrice.text = it.resources.getString(R.string.free)
            }
        }

        //translator
        if (currentBook.translators.isEmpty()) {
            binding.translatorLayout.visibility = View.GONE
            binding.translatorBottomBorder.visibility = View.GONE
        } else {
            binding.translatorLayout.visibility = View.VISIBLE
            binding.translatorBottomBorder.visibility = View.VISIBLE
            binding.translator.text = translators

        }
        categories = currentBook.getCategoriesToDisplay()
        binding.bookCategories.text = categories
        binding.rateValue.text = currentBook.ratingValue.toString()
        binding.rateBar.rating = currentBook.ratingValue
        var languages: String? = ""

        currentBook.languages.let {
            for ((index, language) in it.withIndex()) {
                languages += if (index == it.size - 1) {
                    language.language
                } else {
                    language.language + ","
                }
            }
        }
        binding.bookLanguage.text = languages
        binding.bookISBN.text = currentBook.isbn
    }

    private fun showPrintedPrice() {
        currentBook.priceDetails.let {
            var price = maxOf(it.printedPrice, it.originalPrice)

            if (price > it.visiblePrice) {
                binding.bookDiscount.text =
                    price.toCurrency(it.currency)

                binding.bookDiscount.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

            } else {
                binding.bookDiscount.visibility = View.GONE
            }

        }
    }

    private fun fetchBook() {
        model.getBook().observe(viewLifecycleOwner) { b ->
            enableClick(binding.btnPurchase)
            b.let { it ->

                currentBook = b

                if (b.isPurchased) {
                    isReview(b)
                } else {
                    binding.button.setText(resources.getString(R.string.read_sample))
                }
                binding.offerView.book = b

                //b.setIsMyReview()
                setValues()

                val isRefreshNeeded =
                    UserService.getInstance().isUserLogged() && b.isFetchedBeforeLogin
//                Logger.d("isRefreshNeeded = $isRefreshNeeded isFetchedBeforeLogin = ${b.isFetchedBeforeLogin} ")
//                Logger.json(b.serializedBook())
                if (isRefreshNeeded) {
                    model.isFetchBookCalled = false
                    it.isFill = !isRefreshNeeded
                }

                if (it.isFill) {

                    binding.btnPurchaseLayout.visibility = View.VISIBLE
                    Log.d(logTag, "observer book data")
//                    Logger.json(it.serializedBook())
                    showLoading(false)
                    //   observeRatings(it)
                    showMoreInfo()
                    showBottomSection()
                    setBookMore(b)
                    initViewPager(b)
                    topReviewedUsers(it)
                    setFavourite(it.isAtWishlist)
                    changeButton()
                    setValues()

                } else {
                    setFavourite(it.isAtWishlist)
                    showLoading(true)
                    initbook.let {
                        model.fetchBook(it.id)
                        initbook
                    }
                }

            }
            getFonts()
            if (currentBook.bookFont != null) {
                BookService.getInstance().setHighLightFont(currentBook)
            }

        }
    }


    /*  override fun onResume() {
          super.onResume()
          enableClick(mainView.btnPurchase)
          if (dialog.isShowing) dialog.dismiss()

          if (currentBook.isFetchedBeforeLogin && UserService.getInstance().isUserLogged()) {
              //not login before but now logged
              Log.d(logTag, "not login before now logged")
              setCartItemCount(optionMenu)
              showLoading(true)
              disableClick(mainView.btnPurchase)
              currentBook.let {
                  model.isFetchBookCalled = false
                  model.fetchBook(it.id)
              }
          }
      }*/


    private fun setProfileImage(b: Book) {
        b.bookImages.forEach { image: BookImages ->
            run {
                if (image.type == TYPE_PROFILE) {
                    profileImage = image
                }
            }
        }

    }

    fun showLoading(visible: Boolean) {
        if (visible) {
            binding.bottomLoading.visibility = View.VISIBLE
        } else {
            binding.bottomLoading.visibility = View.INVISIBLE
        }
    }

    private fun addToCart() {
        val bookList = mutableListOf<Book>()
        initbook.let {
            bookList.add(it)
        }
        if (!isUserLogged()) {
            showLoading(visible = true)
            disableClick(binding.btnPurchase)
            LocalCartService.instance.addToCart(bookList as ArrayList<Book>, success = {
                Handler(Looper.getMainLooper()).post {
                    showLoading(visible = false)
                    enableClick(binding.btnPurchase)
                    navigateToCart()
                }
            }, error = {
                Handler(Looper.getMainLooper()).post {
                    showMessageSnackBar(mainView, "Something went wrong")
                    showLoading(visible = false)
                    enableClick(binding.btnPurchase)

                }
            })
            return
        }
        cartService.addToCartRequest(bookList, { result ->
            navigateToCart()

        }, { exception ->
            exception.printStackTrace()
            showMessageSnackBar(mainView, "Something went wrong")
        })
    }

    private fun navigateToCart() {
        FragmentNavigation.getInstance().startCart(
            requireFragmentManager(), R.id.fragment_view
        )
    }

    private fun showMoreInfo() {
        val animator = binding.moreInfo.animate().alpha(1f)
        animator.duration = 350
        animator.start()
    }

    private fun showBottomSection() {
        binding.bottomSection.visibility = View.VISIBLE
        val animator = binding.bottomSection.animate().alpha(1f).translationY(0f)
        animator.duration = 350
        animator.start()
    }

    private fun setBookMore(book: Book) {
        binding.description.text = book.description
        Log.d(logTag, book.description?:"")
        binding.pages.text = book.pages.toString()
        binding.type.text = book.fileType
        if (book.fileType == "image e-pub") {
            binding.type.setOnClickListener { showImageEpubMessage() }
            binding.type.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        }
        binding.isbnCode.text = book.isbn
        setupHardCopy(book)
    }

    private fun showImageEpubMessage() {
        val msg = resources.getString(R.string.image_epub_message)
        bottomDialogs.getBookNotDownLoadedDialog(
            context = requireContext(),
            titleText = "Image Epub",
            messageText = msg
        ).show()
    }

    fun setupHardCopy(book: Book) {
        book?.purchaseLink?.let {
            binding.hardCopyBottomBorder.visibility = View.VISIBLE
            binding.hardCopy.visibility = View.VISIBLE
            binding.hardCopyIcon.visibility = View.VISIBLE
            binding.hardCopyBtn.visibility = View.VISIBLE
            binding.hardCopyBtn.setOnClickListener { clickHardCopy(book) }
            return
        }
        binding.hardCopyBottomBorder.visibility = View.GONE
        binding.hardCopyBtn.visibility = View.GONE
        binding.hardCopy.visibility = View.GONE
        binding.hardCopyIcon.visibility = View.GONE
    }

    private fun clickHardCopy(book: Book) {
        try {
            book.purchaseLink?.let {
                if (it.isNullOrEmpty()) return
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                activity?.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().log("$logTag -> exception due to the purchaseLink ")
        }

    }

    private fun setupToolBar() {
        Log.d(logTag, "*****setupToolBar book frag")
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = ""
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar
        actionbar?.apply {
            Log.d(logTag, "*****setupToolBar book frag action")
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        actionbar?.show()
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnPurchase -> {
                disableClick(binding.btnPurchase)
                addToCartOrReadNowClick()
            }
            binding.allReviews -> FragmentNavigation.getInstance().startAllReview(
                requireFragmentManager(),
                R.id.fragment_view, bookId
            )
            binding.button -> {
                bottomBtnClick()
            }

        }
    }


    var progressBar: ProgressBar? = null
    var cancelBtn: TextView? = null
    private fun progressInit() {
        dialog = BottomSheetDialog(requireContext())
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.download_progress_layout)
        progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)
        cancelBtn = dialog.findViewById(R.id.cancelBtn)
        cancelBtn?.setOnClickListener { downloadcancel() }
    }

    fun downloadcancel() {
        AndroidNetworking.cancel(initbook.id)
        dialog.dismiss()
    }

    private fun downloadBook(isPreview: Boolean) {
        if (isPreview) {
            startDownload(isPreview)
            return
        }
        showPrepareBookDialog()
        enableClick(binding.btnPurchase)
        initbook.getLocallyAvailable { isAvailable, bookData ->
            var isUpdated = false
            bookData?.let {
                isUpdated = isBookUpdated(it)
            }
            if (isAvailable && isUpdated) {
                processEpub.prepareBookForReading(
                    bookData!!
                ) { path, isSuccess ->
                    requireActivity().runOnUiThread {
                        if (!isSuccess) {
                            Log.e(logTag, "failed save to local $path")
                            startDownload(isPreview)
                            return@runOnUiThread
                        }
                        Log.d(logTag, "success save to local $path")
                        dialog.hide()
                        processEpub.onClickImage = ::showImage
                        processEpub.openEpub(
                            requireContext(),
                            currentBook,
                            folioReader,
                            path!!,
                            bookData?.key!!,
                            true
                        )
                    }
                }
                return@getLocallyAvailable
            }

            if (isAvailable) {
                Handler(Looper.getMainLooper()).post { dialog.hide() }
                showNewVersionAvailable(initbook)
                return@getLocallyAvailable
            }
            requireActivity().runOnUiThread {
                startDownload(isPreview)
            }

        }
    }

    private fun showPrepareBookDialog() {
        if (dialog != null && !dialog.isShowing) {
            Handler(Looper.getMainLooper()).post {
                progressBar?.progress = 0
                dialog.show()
            }
        }
    }

    private fun showNewVersionAvailable(book: Book) {

        requireActivity().runOnUiThread {
            bottomDialogs.getConfirmDialog(
                requireContext(),
                "New version available",
                "Book has an updated version. Do you need to download new version ?",
                "Yes",
                { dialog: BottomSheetDialog ->
                    dialog.dismiss()
                    startDownload(false)
                },
                "No",
                { dialog: BottomSheetDialog ->
                    dialog.dismiss()
                    openLocalBook(book)
                }
            ).show()
        }

    }

    private fun openLocalBook(book: Book) {

        book.getLocallyAvailable { isAvailable, bookData ->
            bookData?.let {
                book.localPath = it.getBookObj()?.localPath

                processEpub.prepareBookForReading(it) { path, isSuccess ->
                    requireActivity().runOnUiThread {
                        dialog.hide()
                        if (!isSuccess) return@runOnUiThread
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

    private fun isBookUpdated(bookData: BookData): Boolean {
        var hasUpdated = false
        model.fullBook?.let { fullBook ->
            //            hasUpdated = fullBook.isBookUpdated(bookData.getBookObj()?.epubVersion!!)
            bookData.getBookObj()?.let {
                hasUpdated = it.isBookUpdated(fullBook.epubVersion!!)
            }
        }

        return hasUpdated
    }

    private fun startDownload(isPreview: Boolean) {
        enableClick(binding.btnPurchase)
        showPrepareBookDialog()
        val token = LocalStorageService.getInstance().token
        Log.d(logTag, "token found for download book ${token}")
        model.downloadBook(initbook, isPreview, successDownload, token = token)
        model.progress.observe(viewLifecycleOwner, Observer(progress))
        enableClick(binding.btnPurchase)
    }

    private var successDownload = fun(path: String, isPreview: Boolean) {
        Handler(Looper.getMainLooper()).post {
            dialog.dismiss()
            if (isPreview) {
                processEpub.onClickImage = ::showImage
                processEpub.openEpub(requireContext(), currentBook, folioReader, path, "", false)
                return@post
            }
            /*
            val token = LocalStorageService.getInstance().getToken()
            val key = model.getEncryptionKey(token)

            val userId = UserService.getInstance().getUser()?.user_id
            model.saveBook(key, userId)
            */

            currentBook.getLocallyAvailable { isAvailable, bookData ->
                if (!isAvailable) return@getLocallyAvailable
                processEpub.prepareBookForReading(bookData!!) { p, isSuccess ->
                    if (!isSuccess) {
                        Log.d(logTag, "failed to prepare book")
                        return@prepareBookForReading
                    }
                    requireActivity().runOnUiThread {
                        processEpub.openEpub(
                            requireContext(),
                            currentBook,
                            folioReader,
                            p!!,
                            bookData!!.key,
                            true
                        )
                    }

                }
            }


        }

    }

    private var progress = fun(progress: Long) {

        progressBar?.let {
            it.progress = progress.toInt()
        }

    }


    private fun openEpub(
        folioReader: FolioReader,
        path: String,
        key: String,
        isEncrypted: Boolean
    ) {
        Log.d(logTag, "openEpub calling . path : $path")
        var config = AppUtil.getSavedConfig(context)
        if (config == null)
            config = Config()
        config.allowedDirection = Config.AllowedDirection.VERTICAL_AND_HORIZONTAL
        if (isEncrypted) {
            config.isNeedExport = isEncrypted


            config.password = key
            config.callback = object : Configurations.Callback {
                override fun export(input: String): String {
                    val exported =
                        gms()
                            .decryptEpubFile(input, FolioReader.get().config.password)
                    exported?.let {
                        Log.d(logTag, "Content Export success")
                        return exported
                    }
                    Log.e(logTag, "Content Export Failed")
                    return input
                }

            }

            config.imageClickListener = object : Configurations.ImageClickListener {
                override fun onImageClick(url: String, context: Context?) {
                    Handler().post {
                        //                            requireActivity().startActivity(Intent(requireContext(),))
                        Log.d(logTag, "onImageClick  : $url")
                        showImage(url, context)
                    }

                }

            }


        }
        folioReader.setConfig(config, true)
            .openBook(path)

    }

    fun showImage(url: String, context: Context?) {
        try {
            Log.d("showImage", "inside showImage method")
//            val images = ArrayList<String>()
//            images.add(url)
//            ImageViewer.Builder(context, images).show()
            requireActivity().startActivity(
                Intent(
                    requireContext(),
                    ImagePreviewActivity::class.java
                ).apply {
                    putExtra("url", url)
                })
            Log.d("showImage", "inside showImage method last")
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    override fun onHighlight(highlight: HighLight?, type: HighLight.HighLightAction?) {

    }

    override fun saveReadLocator(readLocator: ReadLocator?) {

        Log.i("-> saveReadLocator ->", readLocator?.toJson()!!)

    }

    override fun onFolioReaderClosed() {

    }

    private fun topReviewedUsers(book: Book) {

        book.ratings.let {
            val array = book.getRatingUSerImages()
            var topRates = array
            if (array.size > 5) {
                topRates = array.subList(0, 5)
            }

            if (array.isNotEmpty()) {
                binding.container.visibility = View.VISIBLE
                binding.container.createImageView(topRates, 80, 80, -30, 0)
                binding.topRatersCount.text =
                    array.size.toString() + " " + resources.getString(R.string.review)
                return
            }
        }

        binding.container.visibility = View.INVISIBLE
        binding.topRatersCount.visibility = View.INVISIBLE

    }

    private fun showHideLoading(isShow: Boolean) {
        if (isShow) {
            binding.loadingView.visibility = View.VISIBLE
        } else {
            binding.loadingView.visibility = View.GONE
        }
    }

    private fun initViewPager(book: Book) {


        if (book.ratings.isNullOrEmpty()) {
            binding.reviewSection.visibility = View.GONE
            binding.allReviews.visibility = View.GONE
            binding.topRatersCount.visibility = View.GONE
            return
        }

        binding.reviewSection.visibility = View.VISIBLE
        binding.allReviews.visibility = View.VISIBLE
        binding.topRatersCount.visibility = View.VISIBLE


        viewPageAdapter = ViewPagerAdapter(requireContext(), book, requireFragmentManager())
        viewPageAdapter.callback = { message ->
            currentBook.ratingOfUser?.orderItemId?.let {
                model.deleteReview(currentBook.id, it)
            }
        }

        viewPageAdapter.getPosition = { position ->

        }
        binding.viewPager.adapter = viewPageAdapter
        binding.pageIndicator.apply {
            setupWithViewPager(binding.viewPager, true)
        }

        if (book.ratings.size < 2) {
            binding.pageIndicator.visibility = View.GONE
        } else {
            binding.pageIndicator.visibility = View.VISIBLE
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        Log.d(logTag, "inside onPrepareOptionsMenu")
//        optionMenu = menu
        menu.let {
            optionMenu = it
        }

        menu.findItem(R.id.action_socialShare)?.isVisible = currentBook.shareUrl != null

        menu.findItem(R.id.action_cart)?.isVisible = true
        menu.findItem(R.id.action_search)?.isVisible = false
        menu.findItem(R.id.action_category)?.isVisible = false
        menu.findItem(R.id.action_refresh)?.isVisible = false

        setFavourite(initbook.isAtWishlist)

        if (UserService.getInstance().isUserLogged()) {
            setCount(requireContext(), CartService.getInstance().getCartSize().toString(), menu)
        } else {
            setCount(requireContext(), model.cartItemCount.value.toString(), menu)
        }
    }


    private fun observeLocalCartItemCount() {
        model.cartItemCount.observe(viewLifecycleOwner, Observer { count ->
            if (isVisible && !isRemoving) {
                optionMenu?.let {
                    requireActivity().invalidateOptionsMenu()
                }

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_socialShare -> {
                Log.d(logTag, "share on social media...")
                currentBook.shareUrl?.let { shareOnSocialMedia(it) }
            }
            R.id.action_favorite -> {
                setFavourite(true)
                initbook.let { it ->
                    model.addToWishList(it, error = {
                        showWishListError(it)
                        setFavourite(initbook.isAtWishlist)
                    })
                }
            }
            R.id.action_favorite_fill -> {
                setFavourite(false)
                initbook.let {
                    model.removeFromToWishList(it, error = {
                        showWishListError(it)
                        setFavourite(initbook.isAtWishlist)
                    })
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initShareLink() {
        model.share_link.observe(this, Observer<ShareUrl> {
            it.url?.let { it1 -> shareOnSocialMedia(it1) }
        })
    }

    private fun initWishListData() {
        model.wishListItemSuccess.observe(viewLifecycleOwner, Observer {
            it?.let {
                lifecycleScope.launch {
                    model.wishListItemSuccess.value = null
                    if (model.isAddedToWishList()) {
                        Log.d(logTag, "success***")
                        showSuccessSnackBar(mainView, resources.getString(R.string.add_wishlist))
                        return@launch
                    }
                }
            }
        })
    }

    private fun observerRemoveFromToWishList(){
        model.removeWishListItemLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                lifecycleScope.launch{
                    model.removeWishListItemLiveData.value = null
                    if (model.isRemovedFromWishList()) {
                        Log.d(logTag, "clear***")
                        showSuccessSnackBar(mainView, resources.getString(R.string.remove_wishlist))
                        return@launch
                    }
                }
            }
        })
    }

    private fun showWishListError(exception: NetException) {
        exception.printStackTrace()
        Toast.makeText(context, "Something went wrong in updating the wishList", Toast.LENGTH_SHORT)
            .show()
    }

    private fun showShareError(exception: NetException) {
        exception.printStackTrace()
        Toast.makeText(context, "Something went wrong in share", Toast.LENGTH_SHORT)
            .show()
    }

    private fun setFavourite(isLike: Boolean) {
//        Books.allBooks.forEach {
//            it.data.forEach {
//                if (it.id.equals(currentBook.id)) {
//                    currentBook.isAtWishlist = it.isAtWishlist
//                }
//            }
//        }
        optionMenu?.findItem(R.id.action_refresh)?.isVisible = false
        if (currentBook.isPurchased) {
            optionMenu?.findItem(R.id.action_favorite)?.isVisible = false
            optionMenu?.findItem(R.id.action_favorite_fill)?.isVisible = false
            return
        } else if (!UserService.getInstance().isUserLogged()) {
            optionMenu?.findItem(R.id.action_favorite)?.isVisible = false
            optionMenu?.findItem(R.id.action_favorite_fill)?.isVisible = false
            return
        } else {
            optionMenu?.findItem(R.id.action_favorite)?.isVisible = !isLike
            optionMenu?.findItem(R.id.action_favorite_fill)?.isVisible = isLike
        }

    }

    fun changeButton() {

        if (initbook.isPurchased && isUserLogged()) {
            optionMenu?.findItem(R.id.action_favorite)?.isVisible = false
            binding.btnPurchase.setText(resources.getString(R.string.read_now_btn))

            return
        }
        if (initbook.isFree && !initbook.isPurchased) {
            binding.btnPurchase.setText("ADD TO LIBRARY")
        }
    }

    private fun isReview(book: Book) {
        if (book.ratingOfUser == null) {
            binding.button.setText(resources.getString(R.string.review))
          //  binding.bottomBarText.text = resources.getString(R.string.bottom_text)

        } else {
            binding.button.setText(resources.getString(R.string.shop_more))
         //   binding.bottomBarText.text = resources.getString(R.string.shop_more_text)
        }
    }

    private fun addToCartOrReadNowClick() {
        when (currentBook.action) {
            Book.ActionType.ADD_TO_CART -> {
                addToCartOrLibrary()
            }
            Book.ActionType.READ_NOW -> {
                readNow()
            }
            else -> {
            }
        }
    }

    private fun addToCartOrLibrary() {

        if (currentBook.isFree && !currentBook.isPurchased) {
            if (!isUserLogged()) {
                model.currentTask = Actions.ADD_TO_LIBRARY
                Log.d(logTag, "addToCartOrLibrary :Not a Logged User.startLoginProcess called...")
                startLoginProcess()
                return
            }
            disableClick(binding.btnPurchase)
            disableClick(binding.button)
            showLoading(true)
            model.currentBook?.let {
                model.addToLibrary(it)
            }
            return
        }
        addToCart()
    }

    private fun readNow() {
        if (!UserService.getInstance().isUserLogged()) {
            model.currentTask = Actions.READ_NOW
            Log.d(logTag, "readNow :Not a Logged User.startLoginProcess called...")
            startLoginProcess()
            return
        }
        if (model.fullBook != null) {
            downloadBook(false)
        } else {
            dialog.show()
            model.openBookAfterFetch = true
            model.fetchBook(initbook.id, true)
        }
    }

    private fun startLoginProcess() {
        Log.d(logTag, "startLoginProcess :Not a Logged User.Login Started...")
        val intent = Intent(context, LoginActivity::class.java)
        startActivityForResult(intent, Contants.LOGIN_REQUEST_CODE)
    }

    private fun refreshBookObserve() {
        model.refreshedBook.observe(viewLifecycleOwner, Observer { book: Book ->

            book.let {
                showLoading(false)
                if (model.openBookAfterFetch) {
                    model.openBookAfterFetch = false
                    downloadBook(false)
                }

            }

        })
    }

//    private fun observeLibData(){
//        Log.e(logTag, "onServe to Lib Data")
//        model.addToLibrary.observe(viewLifecycleOwner, Observer {
//            it?.let {
//                enableClick(binding.btnPurchase)
//                enableClick(binding.button)
//                showLoading(false)
//                model.fillBook(it)
//                changeButton()
//                model.addToLibrary.value == null
//                model.addToLibraryError.value == null
//                showMessageSnackBar(
//                    mainView,
//                    "${model.currentBook?.title ?: "Book"} successfully added to library"
//                )
//                Handler(Looper.getMainLooper()).post {
//                    FragmentNavigation.getInstance()
//                        .startMyLibrary(requireFragmentManager(), R.id.fragment_view)
//                }
//            }
//        })
//        model.addToLibraryError.observe(viewLifecycleOwner, Observer {
//            it?.let {
//                enableClick(binding.btnPurchase)
//                enableClick(binding.button)
//                showLoading(false)
//                model.addToLibrary.value == null
//                model.addToLibraryError.value == null
//                showMessageSnackBar(
//                    mainView,
//                    "Failed to add ${model.currentBook?.title ?: "Book"} to library"
//                )
//
//            }
//        })
//    }

    private fun bottomBtnClick() {
        when (currentBook.bottomBtnAction) {
            Book.ActionType.REVIEW -> {
                AddReviewFragment.isAdd = true
                FragmentNavigation.getInstance()
                    .startAddReview(requireFragmentManager(), R.id.fragment_view, currentBook)
            }
            Book.ActionType.SHOP_MORE -> {
                FragmentNavigation.getInstance()
                    .startHomeFragment(requireFragmentManager(), R.id.fragment_view)
            }
            Book.ActionType.READ_NOW -> {
                downloadBook(true)
            }
            else -> {

            }
        }

    }

    private fun showException() {
        model.getNetException().observe(viewLifecycleOwner, Observer { t ->
            enableClick(binding.btnPurchase)
            t?.let {
                if (isErrorHandled(it)) {
                } else {
                    t.message?.let {
                        showErrorSnackBar(mainView, getString(R.string.error_msg))
                    }

                }
            }
        })

        model.ratingFailed.observe(viewLifecycleOwner, Observer { t ->
            enableClick(binding.btnPurchase)
            t?.let {
                if (isErrorHandled(it)) {
                } else {
                    t.message?.let {
                        showErrorSnackBar(mainView, getString(R.string.error_msg))
                    }

                }
            }
        })

    }

    private fun shareOnSocialMedia(body: String) {
        val myIntent =
            Intent(Intent.ACTION_SEND)
        myIntent.type = "text/plain"
        myIntent.putExtra(Intent.EXTRA_SUBJECT, body)
        myIntent.putExtra(Intent.EXTRA_TEXT, body)
        startActivity(Intent.createChooser(myIntent, "Share using "))
    }

    fun enableClick(view: View) {
        view.isClickable = true
    }

    private fun disableClick(view: View) {
        view.isClickable = false
    }

    private fun getFonts() {
        currentBook?.bookFont?.let {
            if (it.isNotEmpty()) {
                it.forEach { font ->
                    BookService.getInstance().downloadFont(font)
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(logTag, "onDestroy calling")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("", "")

        if (resultCode == Activity.RESULT_OK) {
            setCartItemCount(optionMenu)
            showLoading(true)
            disableClick(binding.btnPurchase)
            currentBook.let {
                model.isFetchBookCalled = false
                model.fetchBook(it.id)
            }
        }
    }

}
