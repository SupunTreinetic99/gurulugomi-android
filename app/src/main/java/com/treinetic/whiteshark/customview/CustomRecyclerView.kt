package com.treinetic.whiteshark.customview

import android.animation.Animator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.BottomLoginSheetBinding
import com.treinetic.whiteshark.databinding.CustomRecyclerViewLayoutBinding

/**
 * Created by Nuwan on 2/27/19.
 */
@Keep
class CustomRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var TAG = "CustomRecyclerView"
    var binding : CustomRecyclerViewLayoutBinding
    var recyclerView: RecyclerView
    var loadingView: LinearLayout
    var noDataView: LinearLayout
    var title: TextView
    var progressBar: ProgressBar
    var refreshLayout: SwipeRefreshLayout
    var onRefreshCallback: ((refreshLayout: SwipeRefreshLayout) -> Unit)? = null
    var isEnableSwipeRefresh = false
        set(value) {
            field = value
            refreshLayout.isEnabled = field
        }
    var noDataText = "No Results Found"

    init {
        binding = CustomRecyclerViewLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        recyclerView = binding.recyclerView
        loadingView = binding.loadingView
        title = binding.title
        progressBar = binding.progressBar
        noDataView = binding.noDataView
        refreshLayout = binding.refreshLayout
        refreshLayout.setOnRefreshListener { onRefresh() }
        setupRefreshLayout()
    }

    fun setupRefreshLayout() {
        refreshLayout.isEnabled = isEnableSwipeRefresh
        refreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent,
            R.color.colorReviewDate
        )
    }

    fun onRefresh() {
//        refreshLayout.isRefreshing = false
        onRefreshCallback?.let { it(refreshLayout) }
    }

    fun showLoading(title:String?="Loading...") {
        clearAllAnimations()
        if (loadingView.visibility == View.VISIBLE) return
        fadeIn(loadingView)
        fadeOut(recyclerView)
        noDataView.visibility = View.GONE

    }

    fun hideLoading() {
        clearAllAnimations()
        noDataView.visibility = View.GONE
        if (loadingView.visibility != View.GONE) {
            fadeOut(loadingView)
        }
        Log.d(TAG, "hideLoading fadeIn recyclerView")
        fadeIn(recyclerView)

    }

    fun showNoDataView() {
        clearAllAnimations()
        if (noDataView.visibility == View.VISIBLE) return
        binding.noDataView
        fadeIn(noDataView)
        fadeOut(recyclerView)
        loadingView.visibility = View.GONE
    }


    fun hideNoDataView() {
        clearAllAnimations()
//        noDataView.visibility = View.GONE
        loadingView.visibility = View.GONE
        if (noDataView.visibility != View.GONE) {
            fadeOut(noDataView)
        }

        Log.d(TAG, "hideNoDataView fadeIn recyclerView")
        fadeIn(recyclerView)


    }

    private fun fadeIn(view: View) {
        Handler(Looper.getMainLooper()).post {
            view.alpha = 0f
            view.visibility = View.VISIBLE
            val animator = view.animate()

            animator.apply {
                duration = 300
                alpha(1f)
                setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        Log.d(
                            TAG,
                            "fadeIn onAnimationEnd ...visibility= ${view.visibility}  alpha= ${view.alpha}"
                        )
                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationStart(animation: Animator) {

                    }


                })
            }.start()
        }

    }

    private fun animateLoadingContent() {
//        animateTranslateY(progressBar)
//        animateTranslateY(title)
    }


    fun animateTranslateY(view: View) {

        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.translationY = -10f
        val animator = view.animate()
        animator.apply {
            translationY = 10f
            alpha = 1f
            duration = 300
            startDelay = 200
        }.start()


    }


    private fun fadeOut(view: View) {
        Handler(Looper.getMainLooper()).post {
            view.visibility = View.VISIBLE
            val animator = view.animate()

            animator.apply {
                duration = 300
                alpha(0f)
                setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationStart(animation: Animator) {

                    }


                })
            }.start()
        }
    }


    fun hideLoadingWithoutAnimation() {

        clearAllAnimations()
        recyclerView.visibility = View.VISIBLE
        recyclerView.alpha = 1f
        noDataView.visibility = View.GONE
        noDataView.alpha = 1f
        loadingView.visibility = View.GONE
        loadingView.alpha = 1f
    }

    fun showLoadingWithoutAnimation() {
        clearAllAnimations()
        recyclerView.visibility = View.GONE
        recyclerView.alpha = 1f
        noDataView.visibility = View.GONE
        noDataView.alpha = 1f
        loadingView.visibility = View.VISIBLE
        loadingView.alpha = 1f
    }


    fun clearAllAnimations() {

        recyclerView.clearAnimation()
        noDataView.clearAnimation()
        loadingView.clearAnimation()
    }


}