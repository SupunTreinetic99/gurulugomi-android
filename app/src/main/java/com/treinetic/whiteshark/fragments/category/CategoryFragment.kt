package com.treinetic.whiteshark.fragments.category


import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.CategoryAdapter
import com.treinetic.whiteshark.databinding.FragmentCategoryBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.Category

class CategoryFragment : BaseFragment() {

    private var _binding : FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var categoryList: RecyclerView
    private lateinit var fragmentNavigation: FragmentNavigation
    private lateinit var model: CategoryViewModel
    private var categoryAdapter: CategoryAdapter? = null


    companion object {
        fun getInstance(): CategoryFragment {
            return CategoryFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        model = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]
        mainView = binding.root

        setHasOptionsMenu(true)

        fragmentNavigation = FragmentNavigation.getInstance()

//        setupToolBar()

        fetchCategory()

        return mainView
    }

    override fun onResume() {
        super.onResume()
        fetchCategory()
    }

    private fun fetchCategory() {

        model.fetchCategoryList()
        model.getCategoryList().observe(viewLifecycleOwner, Observer<List<Category>> { list ->
            initCategoryList()
        })
    }

    private fun initCategoryList() {

        model.getCategoryList().value?.let {
            categoryAdapter = CategoryAdapter(it)
            categoryAdapter?.onClick = { position, category ->
                run {
                    category.id?.let {
                        fragmentNavigation.startCategoryBookFragment(
                            requireParentFragment().requireFragmentManager(), R.id.fragment_view, it
                        )
                    }

                }
            }

            categoryList = binding.categoryList.apply {
                layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.VERTICAL,
                    false
                )
                adapter = categoryAdapter

            }

            if (it.isNotEmpty()) {
                showList()
            }


        }

    }

    private fun showList() {

        val animatror: ViewPropertyAnimator = binding.categoryList.animate()
        animatror.duration = 350
        animatror.alpha(1f).start()

    }


    private fun setupToolBar() {


        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = "Book Category"

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
            false,
            false,
            false,
            false,
            false
        )
    }


}
