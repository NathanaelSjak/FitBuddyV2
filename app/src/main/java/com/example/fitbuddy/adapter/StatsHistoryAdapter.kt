package com.example.fitbuddy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R
import com.example.fitbuddy.data.UserStatsEntity
import java.text.SimpleDateFormat
import java.util.*

class StatsHistoryAdapter(private var stats: List<UserStatsEntity>) :
    RecyclerView.Adapter<StatsHistoryAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.tvDate)
        val height: TextView = view.findViewById(R.id.tvHeight)
        val weight: TextView = view.findViewById(R.id.tvWeight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stats_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stat = stats[position]
        try {
            val date = dateFormat.parse(stat.date)
            holder.date.text = displayDateFormat.format(date!!)
            holder.height.text = String.format("Height: %.1f cm", stat.height)
            holder.weight.text = String.format("Weight: %.1f kg", stat.weight)
        } catch (e: Exception) {
            holder.date.text = stat.date
            holder.height.text = String.format("Height: %.1f cm", stat.height)
            holder.weight.text = String.format("Weight: %.1f kg", stat.weight)
        }
    }

    override fun getItemCount() = stats.size

    fun updateData(newStats: List<UserStatsEntity>) {
        stats = newStats
        notifyDataSetChanged()
    }

    fun getStats(): List<UserStatsEntity> = stats
} 