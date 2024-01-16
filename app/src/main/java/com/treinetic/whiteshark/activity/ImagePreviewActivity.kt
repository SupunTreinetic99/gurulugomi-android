package com.treinetic.whiteshark.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.stfalcon.frescoimageviewer.ImageViewer
import com.treinetic.whiteshark.R
import java.util.ArrayList

class ImagePreviewActivity : AppCompatActivity() {

    private val TAG = "ImagePreviewActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)
        initImageViewer()
    }


    fun initImageViewer() {
        try {
            Log.d(TAG, "inside initImageViewer")
            var url: String = getIntent().getStringExtra("url") ?: return
            val images = ArrayList<String>()
            images.add(url!!)
            Log.d(TAG, "showImage $url")
            Handler(Looper.getMainLooper()).post {
//                Toast.makeText(this, "Showing image", Toast.LENGTH_SHORT).show()
                ImageViewer.Builder(this, images).setStartPosition(0)
                    .setBackgroundColorRes(R.color.white)
                    .setOnDismissListener {
                        finish()
                    }

                    .show()
            }


//            StfalconImageViewer.Builder<String>(this, images) { view, image ->
//                GlideApp.with(this).load(image).into(view)
//            }
//                .withBackgroundColorResource(R.color.white)
//                .show()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
