package com.dicoding.storyapp.view.maps

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.dicoding.storyapp.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    private val inflater = LayoutInflater.from(context)

    override fun getInfoContents(marker: Marker): View {
        val view = inflater.inflate(R.layout.custom_info_window, null)

        val ivStory = view.findViewById<ImageView>(R.id.imageStoryMarker)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvSnippet = view.findViewById<TextView>(R.id.tvSnippet)

        tvTitle.text = marker.title
        tvSnippet.text = marker.snippet

        val imageUrl = marker.tag as? String
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        ivStory.setImageDrawable(resource)

                        if (marker.isInfoWindowShown) {
                            marker.hideInfoWindow()
                            marker.showInfoWindow()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        ivStory.setImageDrawable(placeholder)
                    }
                })
        } else {
            ivStory.setImageResource(R.drawable.ic_placeholder)
        }

        return view
    }

    override fun getInfoWindow(marker: Marker): View? = null
}
