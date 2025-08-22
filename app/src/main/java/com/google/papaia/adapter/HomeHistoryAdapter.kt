package com.google.papaia.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.papaia.R
import com.google.papaia.response.PredictionHistoryResponse

class HomeHistoryAdapter(private val context: Context, private val items: List<PredictionHistoryResponse>)
    : BaseAdapter() {
    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_home_history, parent, false)

        val item = items[position]

        val diseaseName = view.findViewById<TextView>(R.id.textview_disease_name)
        val timestamp = view.findViewById<TextView>(R.id.textview_timestamp)
        val statusView = view.findViewById<TextView>(R.id.textview_status)
        val imageview_scan = view.findViewById<ImageView>(R.id.imageview_scan)

        diseaseName.text = item.prediction
        timestamp.text = item.timestamp

        // ✅ Change status background + text color
        if (item.prediction.equals("Healthy", ignoreCase = true)) {
            statusView.text = "Healthy"
            statusView.setBackgroundResource(R.drawable.status_badge_healthy) // new drawable
            statusView.setTextColor(Color.parseColor("#00712D"))
        } else {
            statusView.text = "Diseased"
            statusView.setBackgroundResource(R.drawable.status_badge_diseased) // existing drawable
            statusView.setTextColor(Color.parseColor("#F97316"))
        }

        val fullUrl = "https://papaiaapi.onrender.com${item.imageUrl}"
        Glide.with(context)
            .load(fullUrl)// fallback while loading
            .error(R.drawable.image_no_content) // if loading fails
            .into(imageview_scan)

        return view
    }
}