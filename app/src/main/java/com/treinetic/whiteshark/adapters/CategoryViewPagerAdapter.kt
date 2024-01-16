package com.treinetic.whiteshark.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.treinetic.whiteshark.fragments.authors.AuthorsFragment
import com.treinetic.whiteshark.fragments.category.CategoryFragment
import com.treinetic.whiteshark.fragments.publishers.PublishersFragment


class CategoryViewPagerAdapter(
    private val myContext: Context,
    fm: FragmentManager,
    internal var totalTabs: Int
) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                CategoryFragment()
            }
            1 -> {
                AuthorsFragment()
            }
            2 -> {
                PublishersFragment()
            }
            else -> null!!
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }



}