package com.treinetic.whiteshark.fragments.userprofile


import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.theartofdev.edmodo.cropper.CropImage
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.LoginActivity
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.activity.TermsAndPrivacyActivity
import com.treinetic.whiteshark.constance.Contants
import com.treinetic.whiteshark.databinding.FragmentSearchResultBinding
import com.treinetic.whiteshark.databinding.FragmentUserProfileBinding
import com.treinetic.whiteshark.dialog.ViewDialog
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.fragments.authorbooks.AuthorBookViewModel
import com.treinetic.whiteshark.glide.GlideApp
import com.treinetic.whiteshark.models.appupdate.Update
import com.treinetic.whiteshark.network.Net
import com.treinetic.whiteshark.services.*
import java.io.File
import java.util.*


class UserProfileFragment : BaseFragment(), View.OnClickListener {
    private val logTag = "UserProfileFragment"
    private var _binding : FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var model: UserProfileViewModel
    var imageFile: File? = null
    private lateinit var viewDialog: ViewDialog

    companion object {
        fun newInstance(): UserProfileFragment {
            return UserProfileFragment()
        }
    }

    override fun onResume() {
        super.onResume()
        getUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[UserProfileViewModel::class.java]
        viewDialog = ViewDialog()

        binding.purchaseHistory.setOnClickListener(this)
        binding.myLibrary.setOnClickListener(this)
        binding.wishList.setOnClickListener(this)
        binding.eventList.setOnClickListener(this)
        binding.bookPublishing.setOnClickListener(this)


        binding.about.setOnClickListener(this)
        binding.promotion.setOnClickListener(this)
        binding.imageUpload.setOnClickListener(this)
        binding.logout.setOnClickListener(this)
        binding.devices.setOnClickListener(this)
        binding.terms.setOnClickListener(this)
        binding.faq.setOnClickListener(this)
        binding.btnUserProfileLogin.setOnClickListener(this)
        binding.eVoucher.setOnClickListener(this)


        manageNavItems()

        setupToolBar()

//        getUser()
        showException()


        if (UserService.getInstance().isUserLogged()) {
            model.getInitUser()

            UserService.getInstance().getUser().image?.getImage()?.let {
                setProfileImage(it)
            }
        }

        return mainView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calculateViewHeight()
    }

    private fun calculateViewHeight() {
        if (UserService.getInstance().isUserLogged()) {
            return
        }
        activity?.displayMetrics()?.run {
            val height = heightPixels
            binding.userProfileLinearLayoutSupport.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (height / 100) * 15
            )

        }
    }

    private fun Activity.displayMetrics(): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics
    }

    private fun manageNavItems() {

        if (UserService.getInstance().isUserLogged()) {
            Log.d(logTag, "User login")
            binding.myLibraryLockIco.visibility = View.GONE
            binding.wishListLockIco.visibility = View.GONE
            binding.purchaseHistoryLockIco.visibility = View.GONE
            binding.devicesLockIco.visibility = View.GONE
            binding.logoutLockIco.visibility = View.GONE
            binding.promotionLockIco.visibility = View.GONE
            binding.bookPublishingLockIco.visibility = View.GONE
            binding.eventListLockIco.visibility = View.GONE
            binding.aboutLockIco.visibility = View.GONE
            binding.termsLockIco.visibility = View.GONE
            binding.faqLockIco.visibility = View.GONE
            binding.imageUpload.visibility = View.VISIBLE
            binding.userName.visibility = View.VISIBLE
            binding.userProfileLinearLayout.visibility = View.GONE
            binding.userProfileLinearLayoutSupport.visibility = View.GONE
            binding.logout.visibility = View.VISIBLE
            binding.faqDivider.visibility = View.VISIBLE
            binding.myLibraryCount.visibility = View.VISIBLE
            binding.myWishListCount.visibility = View.VISIBLE
            getListCounts()
        } else {
            Log.d(logTag, "User not login")

            //show padlocks
            binding.myLibraryLockIco.visibility = View.VISIBLE
            binding.wishListLockIco.visibility = View.VISIBLE
            binding.purchaseHistoryLockIco.visibility = View.VISIBLE
            binding.devicesLockIco.visibility = View.VISIBLE
            binding.logoutLockIco.visibility = View.VISIBLE
            binding.promotionLockIco.visibility = View.INVISIBLE
            binding.bookPublishingLockIco.visibility = View.INVISIBLE
            binding.eventListLockIco.visibility = View.INVISIBLE
            binding.aboutLockIco.visibility = View.INVISIBLE
            binding.termsLockIco.visibility = View.INVISIBLE
            binding.faqLockIco.visibility = View.INVISIBLE
            binding.imageUpload.visibility = View.INVISIBLE
            binding.userProfileLinearLayout.visibility = View.VISIBLE
            binding.userProfileLinearLayoutSupport.visibility = View.VISIBLE
            binding.logout.visibility = View.GONE
            binding.faqDivider.visibility = View.INVISIBLE
            binding.myLibraryCount.visibility = View.GONE
            binding.myWishListCount.visibility = View.GONE
        }

    }

    private fun getListCounts(){
        binding.myLibraryCount.text = BookService.getInstance().myLibrary?.total.toString()
        binding.myWishListCount.text = WishListService.getInstance().wishList?.total.toString()
    }

    override fun onClick(view: View?) {
        val fragmentNavigation = FragmentNavigation.getInstance()
        when (view) {
            binding.purchaseHistory -> {
                if (UserService.getInstance().isUserLogged()) {
                    fragmentNavigation.startPurchasedHistory(
                        requireFragmentManager(),
                        R.id.fragment_view
                    )
                } else {
                    startLoginActivity()
                }
            }
            binding.myLibrary -> {
                if (UserService.getInstance().isUserLogged()) {
                    fragmentNavigation.startMyLibrary(
                        requireFragmentManager(),
                        R.id.fragment_view
                    )
                } else {
                    startLoginActivity()
                }
            }
            binding.wishList -> {
                if (UserService.getInstance().isUserLogged()) {
                    fragmentNavigation.startWishlistFragment(
                        requireFragmentManager(),
                        R.id.fragment_view
                    )
                } else {
                    startLoginActivity()
                }
            }
            binding.eventList -> fragmentNavigation.startEventList(
                requireFragmentManager(), R.id.fragment_view
            )
            binding.bookPublishing -> fragmentNavigation.startBookPublish(
                requireFragmentManager(), R.id.fragment_view
            )
            binding.about -> fragmentNavigation.startAboutFragment(
                requireFragmentManager(), R.id.fragment_view
            )
            binding.promotion -> fragmentNavigation.startPromotionFragment(
                requireFragmentManager(), R.id.fragment_view
            )
            binding.logout -> {
                viewDialog.showLogOutDialog(requireContext(), "Hello")
                viewDialog.btnClick = {
                    if (UserService.getInstance().isUserLogged()) {
                        singOutFaceBook()
                        BookService.getInstance().clear()
                        UserService.getInstance().clear()
                        HomeService.getInstance().clear()
                        WishListService.getInstance().clear()
                        OrderService.getInstance().clear()
                        CartService.getInstance().clear()
//                LocalStorageService.getInstance().saveCurre ntUser(null)
                        LocalStorageService.getInstance().removeCurrentUser()
                        LocalStorageService.getInstance().saveToken(null)
                        Net.setTOKEN(null)
                        binding.userName.visibility = View.GONE
//                requireActivity().finish()

                        signOutGoogle()

                        navigateToHome(true)

                        Log.d(tag, "Logout...")
                    } else {
                        startLoginActivity()
                    }
                }
                viewDialog.btnCancel = {
                    viewDialog.closeDialog()
                }
            }
            binding.imageUpload -> {
                if (UserService.getInstance().isUserLogged()) {
//                startCropImageActivity()
                    uploadImagePermissionCheck()

                } else {
                    startLoginActivity()
                }
            }
            binding.devices -> {

                if (UserService.getInstance().isUserLogged()) {
                    fragmentNavigation.startDeviceList(
                        requireFragmentManager(), R.id.fragment_view
                    )
                } else {
                    startLoginActivity()
                }
            }

            binding.terms -> {
                TermsAndPrivacyActivity.show(requireContext(), "http://gurulugomi.lk/tnc.html")
            }
            binding.faq -> {
                TermsAndPrivacyActivity.show(requireContext(), "https://gurulugomi.lk/FAQ")
            }
            binding.btnUserProfileLogin -> {
                startLoginActivity()
            }
            binding.eVoucher -> {
                UpdateService.instance.appUpdate?.update?.extras?.let {
                    val link = it[Update.E_VOUCHER_LINK].toString()
                    if (URLUtil.isValidUrl(link) && link != "null") {
                        openEVoucherLink(link)
                        return
                    }
                }
                showMessageSnackBar(mainView, "eVoucher feature coming soon..")
            }

        }
    }

    private fun signOutGoogle() {
        try {
            val gso: GoogleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()

            val googleSignInClient: GoogleSignInClient =
                GoogleSignIn.getClient(mainView.context, gso)
            googleSignInClient.signOut()

        } catch (e: Exception) {
           e.printStackTrace()
        }

    }

    private fun openEVoucherLink(link: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(link)
        )
        startActivity(intent)
    }

    private fun navigateToHome(isShowMsg : Boolean = false) {
        FragmentNavigation.getInstance().startHomeFragment(
            requireFragmentManager(), R.id.fragment_view,
            isShowMsg = isShowMsg
        )
    }

    private fun startLoginActivity() {
        val intent = Intent(context, LoginActivity::class.java)
        startActivityForResult(intent, Contants.LOGIN_REQUEST_CODE)
    }

    private fun startCropImageActivity() {
        context?.let {
            CropImage.activity()
                .setMinCropResultSize(100, 100)
                .setMaxCropResultSize(10000, 10000)
                .setInitialCropWindowPaddingRatio(0f)
                .setAspectRatio(1, 1)
                .start(it, this)
        }

    }


    private fun uploadImagePermissionCheck() {

//        val permissions = arrayOf(
//            Manifest.permission.CAMERA,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        )

        val permissions = mutableListOf(
            Manifest.permission.CAMERA
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(logTag, "Permission granted TIRAMISU ")
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        Permissions.check(
            requireContext(),
            permissions.toTypedArray(),
            null,
            null,
            object : PermissionHandler() {
                override fun onGranted() {
                    startCropImageActivity()
                }

                override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                    deniedPermissions?.let {
                        if (it.size > 0) {
                            Log.d(logTag, "permission denied for image upload")
                            showMessageSnackBar(
                                mainView,
                                getString(R.string.upload_image_permission_denied_message)
                            )
                        }
                    }
                }
            })

    }


    private fun singOutFaceBook() {
        LoginManager.getInstance().logOut()
        LocalStorageService.getInstance().saveToken(null)
//        val intent = Intent(activity, LoginActivity::class.java)
//        startActivity(intent)
    }

    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = ""
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        setOptionMenuVisibility(
            menu,
            false, false, false, false, false
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                imageFile = resultUri.path?.let { File(it) }
                if (imageFile?.isFile == true) {
                    model.updateUserImage(imageFile!!)
                }
                resultUri.path?.let {
                    setProfileImage(it)
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                showErrorSnackBar(mainView, error.toString())
            }
        } else if (requestCode == Contants.LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.getStringExtra("data") == Contants.SUCCESS_CALLBACK) {

                    Log.d(logTag, "login done ")
                    //refresh
                    navigateToHome()
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(logTag, "Cancelled....")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setProfileImage(path: String) {
            GlideApp.with(this)
                .load(if(imageFile?.path != null) imageFile?.path else path)
                .placeholder(R.drawable.placeholder_user)
                .into(binding.profileImage)
    }

    private fun getUser() {
        model.getUser().observe(viewLifecycleOwner, Observer { t ->
            t.image?.getImage()?.let {
                setProfileImage(it)
            }
            binding.userName.text = t.name
        })
    }

    private fun showException() {
        model.getNetException().observe(viewLifecycleOwner, Observer { t ->
            t?.let {
                if (isErrorHandled(it)) {
                } else {
                    t.message?.let {
                        showMessageSnackBar(mainView, getString(R.string.error_msg))
                    }
                }
                model.clearError()
            }
        })
    }

}
