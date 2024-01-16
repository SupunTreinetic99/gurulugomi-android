package com.treinetic.whiteshark.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.FragmentAboutBinding
import com.treinetic.whiteshark.databinding.FragmentBillingHistoryBinding
import com.treinetic.whiteshark.models.Book


class BillingHistoryFragment : Fragment() {

    private var _binding : FragmentBillingHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var purchasedBookList: RecyclerView
    private val type = "BILLING"
    private lateinit var count: TextView
    private lateinit var list: List<Book>


    companion object {
        fun newInstance(): BillingHistoryFragment {
            return BillingHistoryFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBillingHistoryBinding.inflate(inflater, container, false)
        mainView = binding.root
        count = binding.itemCount

       // list = BookService().getPurchasedBookList()

        setHasOptionsMenu(true)

        setValue()

        setupToolBar()

        initPurchasedBookList()
        return mainView
    }

    private fun setValue() {
        count.text = "Items (" + list.size.toString() + ")"
    }


    private fun initPurchasedBookList() {


//        purchasedBookList = mainView.purchased_book_list
//
//        val _adapter = BookAdapter(type, list)
//        _adapter.onClick = { position, book, itemview -> }
//
//        purchasedBookList.apply {
//            layoutManager = LinearLayoutManager(
//                context,
//                RecyclerView.VERTICAL,
//                false
//            )
//            bookAdapter = _adapter
//        }

    }

    private fun setupToolBar() {

        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = "Order Details"

        (activity as AppCompatActivity).setSupportActionBar(toolBar)

        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar

        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu?.findItem(R.id.action_favorite)?.isVisible = false
        menu?.findItem(R.id.action_search)?.isVisible = false
        menu?.findItem(R.id.action_category)?.isVisible = false
        menu?.findItem(R.id.action_cart)?.isVisible = false
        menu?.findItem(R.id.action_delete)?.isVisible = false
    }
}
