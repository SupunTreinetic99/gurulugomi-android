package com.treinetic.whiteshark.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.Keep
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.OfferViewLayoutBinding
import com.treinetic.whiteshark.databinding.ProgressLoadingLayoutBinding
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.PriceDetails
import com.treinetic.whiteshark.util.extentions.toOffer
import kotlin.math.floor

@Keep
class OfferView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding : OfferViewLayoutBinding

  //  var mainView: View = LinearLayout.inflate(context, R.layout.offer_view_layout, this)

    init {
        binding = OfferViewLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        visibility = View.GONE
    }

    var book: Book? = null
        set(value) {
            field = value
            updateUi()
        }


    fun updateUi() {
        var b = book ?: return

        var isOfferAvailble = b.isOffer || b.priceDetails.printedPrice > b.priceDetails.visiblePrice

        if (!isOfferAvailble) {
            visibility = View.GONE
            return
        }
        if (isOfferAvailble) {
            visibility = View.VISIBLE

//            getDisPre(b.priceDetails).let {
//                if (it != 0.0) {
//                    offerValue.text = it.toOffer("", "%")
//                }
//            }

//            b.getTimelyDiscountValue()?.let {
//                offerValue.text = it.toOffer("", "%")
//            }

            var offerVal = b.getTimelyDiscountValue() ?: 0.0

            getDiscountRate(b.priceDetails, offerVal).let {
                binding.offerValue.text = it?.toOffer("", "%")
            }

        }

    }

    private fun getDiscountRate(pdetails: PriceDetails, offerPercentage: Double): Double? {
        if ((pdetails.isOffer && offerPercentage > 0.0)
            || (pdetails.printedPrice > pdetails.visiblePrice)
        ) {
            val maxPrice = maxOf(pdetails.printedPrice, pdetails.originalPrice)
            return floor(((maxPrice - pdetails.visiblePrice) / (maxPrice) * 100))
        }
        return 0.0
    }
}