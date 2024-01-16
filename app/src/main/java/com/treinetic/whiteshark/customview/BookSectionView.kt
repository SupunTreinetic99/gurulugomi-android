package com.treinetic.whiteshark.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.HomeBookAdapter
import com.treinetic.whiteshark.databinding.FragmentBillingHistoryBinding
import com.treinetic.whiteshark.databinding.NestedRecyclerCardBinding
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Category
import com.treinetic.whiteshark.util.extentions.toArrayList

@Keep
class BookSectionView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {

    lateinit var binding : NestedRecyclerCardBinding
    private var bookList: RecyclerView
    private var heading: TextView
    private var customViewRoot: LinearLayout
    var category = Category()
    private val fragmentManager = context as MainActivity
    var onItemClick:((position:Int, book: Book,list:ArrayList<Book>?)->Unit)?=null

    init {
        try {
            binding = NestedRecyclerCardBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bookList = binding.sectionList
        heading = binding.heading
        customViewRoot = binding.customViewRoot
        binding.navBtn.setOnClickListener(this)
        binding.heading.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    fun setData(data: Category) {
        category = data
        val _adapter = HomeBookAdapter(data.books?.data!!)
        _adapter.onClick = { position, book ->
            onItemClick?.let { it(position,book,category.books?.data?.toArrayList()) }
            run {
                FragmentNavigation.getInstance()
                    .startBookProfile(
                        fragmentManager.supportFragmentManager,
                        R.id.fragment_view,
                        book
                    )
            }
        }

        bookList.apply {
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = _adapter
        }
        val count: String =
            if (data.booksCount == 0) "" else "( " + data.booksCount.toString() + " )"
        heading.text = data.category + " ${count}"

    }

    override fun onClick(view: View?) {
        when (view) {
            binding.navBtn -> {
                onClickNavigation(category.id!!)
            }
            binding.heading -> {
                onClickNavigation(category.id!!)
            }
        }
    }

    fun setBackground(isShow: Boolean) {

        val params = customViewRoot.layoutParams
        if (isShow) {
            showBackground(params)
        } else {
            hideBackground(params)
        }

    }

    private fun showBackground(params: ViewGroup.LayoutParams) {

        val h = resources.getDimension(R.dimen.height_default)
        params.height = h.toInt()
        customViewRoot.apply {
            layoutParams = params
            background = ContextCompat.getDrawable(context, R.drawable.home_background)
        }
        heading.setTextColor(resources.getColor(R.color.white))
        binding.navBtn.setImageResource(R.drawable.arrow_white)

    }

    private fun hideBackground(params: ViewGroup.LayoutParams) {
        val h = resources.getDimension(R.dimen.height_change)
        params.height = h.toInt()

        customViewRoot.apply {
            background = null
            layoutParams = params
        }
        heading.setTextColor(resources.getColor(R.color.colorSubTittle))
        binding.navBtn.setImageResource(R.drawable.arrow_grey)
    }

    private fun onClickNavigation(categoryId: String) {
        FragmentNavigation.getInstance()
            .startCategoryBookFragment(
                fragmentManager.supportFragmentManager,
                R.id.fragment_view,
                categoryId
            )
    }

}