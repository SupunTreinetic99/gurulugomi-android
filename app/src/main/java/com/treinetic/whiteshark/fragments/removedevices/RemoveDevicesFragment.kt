package com.treinetic.whiteshark.fragments.removedevices

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.facebook.login.LoginManager
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.DeviceAdapter
import com.treinetic.whiteshark.databinding.FragmentRegisterBinding
import com.treinetic.whiteshark.databinding.FragmentRemoveDevicesBinding
import com.treinetic.whiteshark.dialog.MaterialDialogs
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.Device
import com.treinetic.whiteshark.network.Net
import com.treinetic.whiteshark.services.*


class RemoveDevicesFragment : BaseFragment() {
    private var _binding : FragmentRemoveDevicesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var BUTTON_BACKROUND: Drawable
    private lateinit var model: RemoveDevicesViewModel
    private lateinit var id: String
    private lateinit var deviceId: String
    private lateinit var deviceName: String
    private lateinit var currentDevice: Device
    private lateinit var token: String

    companion object {
        @SuppressLint("StaticFieldLeak")
        val instance = RemoveDevicesFragment()
        fun newInstance(token: String): RemoveDevicesFragment {
            var args = Bundle()
            args.putString("token", token)
            instance.arguments = args
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        handleArgs()
        _binding = FragmentRemoveDevicesBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[RemoveDevicesViewModel::class.java]

        setHasOptionsMenu(true)
        showToolbarBackButton()
        setupToolBar()

        model.token = token

        BUTTON_BACKROUND = resources.getDrawable(R.drawable.button_circular_shape)
        setButtonBkg(binding.btnContinue, BUTTON_BACKROUND)

        binding.removeDeviceList.showLoading()

        model.fetchDeviceList()
        showException()
        getDeviceList()
        getDeviceListAfterDelete()

        binding.btnContinue.setOnClickListener {
            continueFlow()
        }
        return mainView
    }

    private fun setupToolBar() {
        val toolBar = binding.removeDeviceToolBar
        toolBar.title = "Remove Devices"

        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar

        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

    }


    fun handleArgs() {
        arguments?.let {
            token = it.getString("token", null)
        }
    }


    private fun getDeviceList() {
        model.deviceList.observe(viewLifecycleOwner, Observer { t ->
            Net.setTOKEN(null)
            LocalStorageService.getInstance().saveToken(null)
            initDeviceList(t)
        //    model.deviceList.value = null
        })
    }

    private fun getDeviceListAfterDelete(){
        model.deviceListForDelete.observe(viewLifecycleOwner, Observer { t ->
            Net.setTOKEN(null)
            LocalStorageService.getInstance().saveToken(null)
            initDeviceList(t)
            //    model.deviceList.value = null
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initDeviceList(deviceList: MutableList<Device>) {
        if (deviceList.isEmpty()) {
            binding.removeDeviceList.showNoDataView()
            return
        } else {
            binding.removeDeviceList.hideLoading()
        }
        deviceAdapter = DeviceAdapter(deviceList)
        deviceAdapter.onClick = { position, device ->
            currentDevice = device
            id = device.id
            deviceId = device.deviceId
            deviceName = device.deviceName
            showDeleteDialog()
        }

         binding.removeDeviceList.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = deviceAdapter
        }
        deviceAdapter.notifyDataSetChanged()

    }

    private fun showDeleteDialog() {
        var msg: String =
            resources.getString(R.string.device_remove_dialog_message_1) + deviceName + " ?"
        context?.let {
            MaterialDialogs().getConfirmDialog(
                it,
                deviceName, msg

            ).show {
                positiveButton(R.string.btn_delete) { dialog ->
                    binding.removeDeviceList.showLoading()
                    model.removeDevice(deviceId, id, context)
                }
                negativeButton(R.string.btn_cancel) { dialog ->

                }
            }
        }
    }

    private fun setButtonBkg(view: View, drawable: Drawable) {
        val button = view as CircularProgressButton
        button.background = drawable
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        setOptionMenuVisibility(
            menu,
            false,
            false,
            false,
            false,
            false
        )
    }


    private fun showException() {
        model.netException.observe(viewLifecycleOwner, Observer { t ->
            binding.removeDeviceList.hideLoading()
            t?.let {
                if (isErrorHandled(it)) {
                } else {
                    t.message?.let {
                        showErrorSnackBar(mainView, getString(R.string.error_msg))
                    }

                }
                model.netException.value = null
            }
        })
    }

    private fun continueFlow() {
        Log.e("RemoveDevicesFragment", "${model.deviceAllList.size}")
            if (model.deviceAllList.size >= 3) {
                showMessageSnackBar(mainView, "At least, remove one device to continue!")
                return
            }else{
                navigateToLogin()
            }
//            model.deviceList.value = null
    }

    private fun navigateToLogin() {
        Log.e("RemoveDevicesFragment", "onBackPressed!")
        requireActivity().finish()
    }
}