package whiteshark.treinetic.com.myapplication.fragments.purchased

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Order
import com.treinetic.whiteshark.services.OrderService
import com.treinetic.whiteshark.services.Service

class PurchasedViewModel : ViewModel() {
    private var purchased: MutableLiveData<MutableList<Order>> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()


    fun clearException() {
        netException.value = null
    }

    fun getPurchased(): LiveData<MutableList<Order>> {
        return purchased
    }

    fun getNetException(): MutableLiveData<NetException> {
        return netException
    }

    fun loadPurchased() {
        OrderService.getInstance().getPurchasedHistory(Service.Success { result ->
            purchased.value = result.orderList
        }, Service.Error { exception ->
            netException.value = exception
        }, requireUpdate = true)


    }

}