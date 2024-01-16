package com.treinetic.whiteshark.fragments.devices


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.DeviceAdapter
import com.treinetic.whiteshark.databinding.FragmentCatergoryBaseBinding
import com.treinetic.whiteshark.databinding.FragmentDeviceListBinding
import com.treinetic.whiteshark.dialog.MaterialDialogs
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.Device


class DeviceListFragment : BaseFragment() {

    private var _binding : FragmentDeviceListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var model: DeviceViewModel
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var id: String
    private lateinit var deviceId: String
    private lateinit var deviceName: String
    private lateinit var currentDevice: Device

    companion object {
        fun getInstance(): DeviceListFragment {
            return DeviceListFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeviceListBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[DeviceViewModel::class.java]

        setHasOptionsMenu(true)
        setupToolBar()
        model.fetchDeviceList()
        showException()
        getDeviceList()

        return mainView
    }

    private fun getDeviceList() {
        model.getDeviceList().observe(viewLifecycleOwner, Observer { t ->
            initDeviceList(t)
        })

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initDeviceList(deviceList: MutableList<Device>) {
        deviceAdapter = DeviceAdapter(deviceList)
        deviceAdapter.onClick = { position, device ->
            currentDevice = device
            id = device.id
            deviceId = device.deviceId
            deviceName = device.deviceName
            showDeleteDialog()
        }

        binding.deviceList.apply {
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
        var msg: String = ""
        if (currentDevice.isThisDevice) {
            msg =
                resources.getString(R.string.device_remove_dialog_message_1) + " " + deviceName + " ? " +
                        resources.getString(R.string.device_remove_dialog_message_2)
        } else {
            msg = resources.getString(R.string.device_remove_dialog_message_1) + deviceName + " ?"
        }


        context?.let {
            MaterialDialogs().getConfirmDialog(
                it,
                deviceName, msg

            ).show {
                positiveButton(R.string.btn_delete) { dialog ->
                    model.removeDevice(deviceId, id, context)
                }
                negativeButton(R.string.btn_cancel) { dialog ->

                }
            }
        }
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

    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = resources.getString(R.string.device_title)

        (activity as AppCompatActivity).setSupportActionBar(toolBar)

        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar

        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

    }

    private fun showException() {
        model.getNetException().observe(viewLifecycleOwner, Observer { t ->
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


}
