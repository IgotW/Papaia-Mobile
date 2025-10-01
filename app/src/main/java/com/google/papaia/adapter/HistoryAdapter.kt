package com.google.papaia.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.papaia.R
import com.google.papaia.activity.ScanResultDetailsActivity
import com.google.papaia.response.PredictionHistoryResponse
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val context: Context,
    private val items: List<PredictionHistoryResponse>
) : BaseAdapter() {

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val parser = SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.getDefault()) // backend format

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_history, parent, false)

        val item = items[position]

        // Find views
        val txtPrediction = view.findViewById<TextView>(R.id.txtPrediction)
        val txtTime = view.findViewById<TextView>(R.id.txtTimestamp) // now shows only time
        val txtDate = view.findViewById<TextView>(R.id.txtDate)      // now shows Today/Yesterday/Date
        val txtStatus = view.findViewById<TextView>(R.id.txtStatus)
        val txtDescription = view.findViewById<TextView>(R.id.txtDescription)
        val btnViewDetails = view.findViewById<TextView>(R.id.btnViewDetails)
        val imgDisease = view.findViewById<ImageView>(R.id.imageview_disease_pic)

        // Prediction
        txtPrediction.text = item.prediction

        // Format timestamp into separate date & time
        formatTimestamp(item.timestamp, txtTime, txtDate)

        // Status badge
        setStatusBadge(item.prediction, txtStatus)

        // Description / suggestions
        txtDescription.text = getShortSuggestion(item.suggestions)

        // View details button
        btnViewDetails.setOnClickListener {
            Log.d("HistoryAdapter", "View details clicked for: ${item.prediction}")
            val intent = Intent(context, ScanResultDetailsActivity::class.java)
            intent.putExtra("analysis_id", item.id)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }


        // Image with Glide
        val fullUrl = if (item.imageUrl.startsWith("http")) {
            item.imageUrl
        } else {
            "https://papaiaapi.onrender.com${item.imageUrl}"
        }

        Glide.with(context)
            .load(fullUrl)
            .placeholder(R.drawable.image_no_content)
            .error(R.drawable.image_no_content)
            .centerCrop()
            .into(imgDisease)

        return view
    }

    private fun formatTimestamp(timestamp: String?, txtTime: TextView, txtDate: TextView) {
        if (timestamp.isNullOrEmpty()) {
            txtTime.text = "Unknown time"
            txtDate.text = "Unknown date"
            return
        }

        try {
            val date = parser.parse(timestamp)
            if (date != null) {
                val now = Calendar.getInstance()
                val itemDate = Calendar.getInstance().apply { time = date }

                val isToday = now.get(Calendar.YEAR) == itemDate.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == itemDate.get(Calendar.DAY_OF_YEAR)

                now.add(Calendar.DAY_OF_YEAR, -1)
                val isYesterday = now.get(Calendar.YEAR) == itemDate.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == itemDate.get(Calendar.DAY_OF_YEAR)

                txtTime.text = timeFormat.format(date)

                txtDate.text = when {
                    isToday -> "Today"
                    isYesterday -> "Yesterday"
                    else -> dateFormat.format(date)
                }
            } else {
                txtTime.text = "Invalid"
                txtDate.text = "Invalid"
            }
        } catch (e: Exception) {
            Log.e("HistoryAdapter", "Error parsing timestamp: $timestamp", e)
            txtTime.text = timestamp
            txtDate.text = ""
        }
    }

    private fun setStatusBadge(prediction: String?, txtStatus: TextView) {
        val isHealthy = prediction?.lowercase()?.contains("healthy") == true ||
                prediction?.lowercase()?.contains("normal") == true

        if (isHealthy) {
            txtStatus.text = "Healthy"
            txtStatus.setBackgroundResource(R.drawable.status_badge_healthy)
            txtStatus.setTextColor(context.getColor(R.color.primary))
        } else {
            txtStatus.text = "Diseased"
            txtStatus.setBackgroundResource(R.drawable.status_badge_diseased)
            txtStatus.setTextColor(context.getColor(R.color.tertiary))
        }
    }

    private fun getShortSuggestion(suggestion: String?): String {
        if (suggestion.isNullOrBlank()) return "No suggestion available"

        // 1. Remove "*" bullets and clean newlines/spaces
        var clean = suggestion.replace("*", "")
            .replace("\n", " ")
            .replace("\\s+".toRegex(), " ")
            .trim()

        // Get first sentence or limit to 80 chars
        val firstSentence = clean.split(".").firstOrNull()?.trim()
        val short = if (!firstSentence.isNullOrBlank()) {
            firstSentence
        } else {
            clean
        }

        return if (short.length > 60) short.take(60) + "..." else short
    }

}
