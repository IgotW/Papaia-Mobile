package com.google.papaia.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.papaia.R
import com.google.papaia.response.PredictionHistoryResponse

class HomeHistoryAdapter(private val context: Context, private val items: List<PredictionHistoryResponse>)
    : BaseAdapter() {
    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view = convertView ?: LayoutInflater.from(context)
//            .inflate(R.layout.item_home_history, parent, false)

        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_home_history, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val item = items[position]

//        val diseaseName = view.findViewById<TextView>(R.id.textview_disease_name)
//        val timestamp = view.findViewById<TextView>(R.id.textview_timestamp)
//        val statusView = view.findViewById<TextView>(R.id.textview_status)
//        val imageview_scan = view.findViewById<ImageView>(R.id.imageview_scan)

        holder.diseaseName.text = item.prediction
        holder.timestamp.text = item.timestamp

        // ✅ Change status background + text color
        if (item.prediction.equals("Healthy", ignoreCase = true)) {
            holder.statusView.text = "Healthy"
            holder.statusView.setBackgroundResource(R.drawable.status_badge_healthy) // new drawable
            holder.statusView.setTextColor(Color.parseColor("#00712D"))
        } else {
            holder.statusView.text = "Diseased"
            holder.statusView.setBackgroundResource(R.drawable.status_badge_diseased) // existing drawable
            holder.statusView.setTextColor(Color.parseColor("#F97316"))
        }

        val fullUrl = if (item.imageUrl.startsWith("http")) {
            item.imageUrl // already a full URL from Firebase/Google Storage
        } else {
            "https://papaiaapi.onrender.com${item.imageUrl}" // relative path case
        }

        Glide.with(context)
            .load(fullUrl)// fallback while loading
            .placeholder(R.drawable.image_no_content)
            .error(R.drawable.image_no_content) // if loading fails
            .into(holder.imageview_scan)

        return view
    }

    private class ViewHolder(view: View) {
        val diseaseName: TextView = view.findViewById(R.id.textview_disease_name)
        val timestamp: TextView = view.findViewById(R.id.textview_timestamp)
        val statusView: TextView = view.findViewById(R.id.textview_status)
        val imageview_scan: ImageView = view.findViewById(R.id.imageview_scan)
    }
}