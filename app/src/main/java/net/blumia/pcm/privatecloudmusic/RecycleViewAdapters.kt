package net.blumia.pcm.privatecloudmusic

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_avatar_view.view.*

/**
 * Created by wzc78 on 2017/11/30.
 */
class ServerIconListAdapter(val srvList: ArrayList<ServerItem>) : RecyclerView.Adapter<ServerIconListAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.bindItems(srvList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent!!.context).inflate(R.layout.item_avatar_view, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return srvList.size
    }

    // FIXME: REMOVE ME!
    fun addItem(item: ServerItem) {
        srvList.add(item)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(serverItem: ServerItem) {
            itemView.av_icon.textString = "BL" // serverItem.blah
        }
    }
}