package com.treinetic.whiteshark.fragments.categorybase


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.CategoryViewPagerAdapter
import com.treinetic.whiteshark.databinding.FragmentCategoryBinding
import com.treinetic.whiteshark.databinding.FragmentCatergoryBaseBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.fragments.FragmentRefreshable
import com.treinetic.whiteshark.fragments.category.CategoryFragment

class CategoryBaseFragment : BaseFragment(), FragmentRefreshable {


    private var _binding : FragmentCatergoryBaseBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View


    companion object {
        fun getInstance(): CategoryBaseFragment {
            return CategoryBaseFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCatergoryBaseBinding.inflate(inflater, container, false)
        mainView = binding.root

        setHasOptionsMenu(true)

        setupToolBar()



        return mainView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupViewPager()
    }

    private fun setupViewPager() {


        binding.categoryTabLayout.addTab(
            binding.categoryTabLayout.newTab().setText("CATEGORIES")
        )
        binding.categoryTabLayout.addTab(
            binding.categoryTabLayout.newTab().setText("AUTHORS")
        )
        binding.categoryTabLayout.addTab(
            binding.categoryTabLayout.newTab().setText("PUBLISHERS")
        )

        binding.categoryTabLayout.tabGravity = TabLayout.GRAVITY_FILL

        val adapter =
            CategoryViewPagerAdapter(
                mainView.context,
                childFragmentManager,
                binding.categoryTabLayout.tabCount
            )



        binding.categoryHolder.adapter = adapter

        binding.categoryHolder.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(
                binding.categoryTabLayout
            )
        )

        binding.categoryTabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.categoryHolder.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    private fun setupToolBar() {


        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = "Categories"

        (activity as AppCompatActivity).setSupportActionBar(toolBar)

        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar


        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

    }

    override fun onRefresh() {
        setupToolBar()
    }


}
