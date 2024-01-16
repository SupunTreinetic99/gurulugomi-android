package com.treinetic.whiteshark.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.DeviceListCardBinding
import com.treinetic.whiteshark.models.Device

class DeviceAdapter(private val deviceList: MutableList<Device>) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    var onClick: ((position: Int, device: Device) -> Unit)? = null


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(
            DeviceListCardBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            this
        )
    }

    override fun getItemCount() = deviceList.size

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]
        device.isCurrentDeivce()

        holder.apply {
            deviceName.text = device.deviceName

            if (device.isThisDevice) {
                thisDevice.text = binding.thisDevice.resources.getString(R.string.this_device)
            }
            deviceId.text = device.deviceId
            lastLogged.text = device.lastLogged
        }

        holder.selectDevice = device
    }

    class DeviceViewHolder(val binding: DeviceListCardBinding, val adapter: DeviceAdapter) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        val deviceName = binding.deviceName
        val deviceId = binding.deviceId
        val lastLogged = binding.lastLogged
        val thisDevice = binding.thisDevice

        lateinit var selectDevice: Device

        init {
            binding.removeIcon.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view) {
                binding.removeIcon -> {
                    adapter.onClick?.let {
                        it(adapterPosition, selectDevice)
                    }
                }
            }
        }
    }

}