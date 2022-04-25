package com.slipkprojects.gostvpn.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.slipkprojects.gostvpn.R

class LogsAdapterList: ListAdapter<String, LogsAdapterList.LogsViewHolder>(DIFF_CALLBACK) {
    class LogsViewHolder(view: View): ViewHolder(view) {
        val logMessage: TextView = view.findViewById(R.id.tv_message_log)
        val logDivider: View = view.findViewById(R.id.view_logs_divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logs_list, parent, false)

        return LogsViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogsViewHolder, position: Int) {
        val log = getItem(position)

        holder.apply {
            logMessage.text = log
        }
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<String> = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem.contentEquals(newItem)
        }
    }
}