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

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_history, parent, false)

        val item = items[position]

        // Find all the new views
        val txtPrediction = view.findViewById<TextView>(R.id.txtPrediction)
        val txtTimestamp = view.findViewById<TextView>(R.id.txtTimestamp)
        val txtDate = view.findViewById<TextView>(R.id.txtDate)
        val txtStatus = view.findViewById<TextView>(R.id.txtStatus)
        val txtDescription = view.findViewById<TextView>(R.id.txtDescription)
        val btnViewDetails = view.findViewById<TextView>(R.id.btnViewDetails)
        val imgDisease = view.findViewById<ImageView>(R.id.imageview_disease_pic)

        // Set basic data
        txtPrediction.text = item.prediction

        // Format timestamp to show time and date separately
        formatTimestamp(item.timestamp, txtTimestamp, txtDate)

        // Set status badge based on prediction
        setStatusBadge(item.prediction, txtStatus)

        // Set description based on prediction
        setDescription(item.prediction, txtDescription)

        // Handle view details click
        btnViewDetails.setOnClickListener {
            // TODO: Navigate to detailed view or show more info
            Log.d("HistoryAdapter", "View details clicked for: ${item.prediction}")

            // Create intent to navigate
            val intent = Intent(context, ScanResultDetailsActivity::class.java)

            // Pass the ID (or any other data you need)
            intent.putExtra("analysis_id", item.id)

            // Important: if context is not an Activity, add this flag
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Start activity
            context.startActivity(intent)
        }


        // Load image with Glide
        val fullUrl = "https://papaiaapi.onrender.com${item.imageUrl}"
        Glide.with(context)
            .load(fullUrl)
            .placeholder(R.drawable.image_no_content) // fallback while loading
            .error(R.drawable.image_no_content) // if loading fails
            .centerCrop()
            .into(imgDisease)

        return view
    }

    private fun formatTimestamp(timestamp: String?, txtTimestamp: TextView, txtDate: TextView) {
        if (timestamp.isNullOrEmpty()) {
            txtTimestamp.text = "Unknown time"
            txtDate.text = "Unknown date"
            return
        }

        try {
            // Parse ISO format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            val date = inputFormat.parse(timestamp)

            if (date != null) {
                val now = Calendar.getInstance()
                val itemDate = Calendar.getInstance().apply { time = date }

                // Check if it's today
                val isToday = now.get(Calendar.YEAR) == itemDate.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == itemDate.get(Calendar.DAY_OF_YEAR)

                if (isToday) {
                    txtTimestamp.text = "Today, ${timeFormat.format(date)}"
                } else {
                    txtTimestamp.text = timeFormat.format(date)
                }

                txtDate.text = dateFormat.format(date)
            } else {
                txtTimestamp.text = timestamp
                txtDate.text = ""
            }
        } catch (e: Exception) {
            Log.e("HistoryAdapter", "Error parsing timestamp: $timestamp", e)
            txtTimestamp.text = timestamp
            txtDate.text = ""
        }
    }

    private fun setStatusBadge(prediction: String?, txtStatus: TextView) {
        val isHealthy = prediction?.lowercase()?.contains("healthy") == true ||
                prediction?.lowercase()?.contains("normal") == true

        if (isHealthy) {
            txtStatus.text = "Healthy"
            txtStatus.setBackgroundResource(R.drawable.status_badge_healthy)
            txtStatus.setTextColor(context.getColor(R.color.white))
        } else {
            txtStatus.text = "Disease Detected"
            txtStatus.setBackgroundResource(R.drawable.status_badge_diseased)
            txtStatus.setTextColor(context.getColor(R.color.white))
        }
    }

    //need change for AI generated that stored on database
    private fun setDescription(prediction: String?, txtDescription: TextView) {
        val description = when (prediction?.lowercase()) {
            "anthracnose" -> "Apply copper-based fungicides. Avoid overhead watering."
            "ring spot" -> "Use neem oil weekly. Remove infected leaves immediately."
            "powdery mildew" -> "Improve air circulation. Use sulfur sprays."
            "healthy" -> "Your crop is healthy"
            else -> "Monitor plant condition and consult agricultural expert if needed."
        }
        txtDescription.text = description
    }
}