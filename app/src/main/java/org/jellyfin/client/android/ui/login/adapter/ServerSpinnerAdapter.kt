package org.jellyfin.client.android.ui.login.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.jellyfin.client.android.R
import org.jellyfin.client.android.domain.models.display_model.Server

class ServerSpinnerAdapter(ctx: Context, resource: Int, private val items: List<Server>) : ArrayAdapter<Server>(ctx, resource, items) {

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Server? {
        return items[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
        val textView: TextView = view.findViewById(R.id.txtLabel)
        textView.text = getItem(position)?.name
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView: TextView = super.getView(position, convertView, parent) as TextView
        textView.text = getItem(position)?.name
        return textView
    }
}