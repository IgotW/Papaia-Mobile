package com.google.papaia.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.papaia.R
import com.google.papaia.response.PredictionHistoryResponse

class HistoryAdapter(private val context: Context, private val items: List<PredictionHistoryResponse>)
    : BaseAdapter() {
    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_history, parent, false)

        val item = items[position]

        val txtPrediction = view.findViewById<TextView>(R.id.txtPrediction)
        val txtTimestamp = view.findViewById<TextView>(R.id.txtTimestamp)
        val imgDisease = view.findViewById<ImageView>(R.id.imageview_disease_pic)

        txtPrediction.text = item.prediction
        txtTimestamp.text = item.timestamp

        val fullUrl = "https://papaiaapi.onrender.com${item.imageUrl}"
        Glide.with(context)
            .load(fullUrl)// fallback while loading
            .error(R.drawable.image_no_content) // if loading fails
            .into(imgDisease)

        return view
    }
}