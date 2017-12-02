package net.blumia.pcm.privatecloudmusic

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_avatar_view.view.*
import java.net.URL

/**
 * Created by wzc78 on 2017/11/30.
 */
class ServerIconListAdapter(activity: MainActivity) : RecyclerView.Adapter<ServerIconListAdapter.ViewHolder>() {

    private var list : List<Map<String, Any?>> = activity.getServerListDataFromDB()

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.bindItems(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent!!.context).inflate(R.layout.item_avatar_view, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItem(position: Int): ServerItem {
        return ServerItem(
                    (list[position]["id"] as Long).toString().toInt(),
                    list[position]["name"] as String,
                    URL(list[position]["api_url"] as String),
                    URL(list[position]["file_root_url"] as String),
                    list[position]["password"] as String,
                    ServerType.SRV_PCM
                )
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(oneItem: Map<String, Any?>) {
            itemView.av_icon.textString = oneItem["id"].toString()
        }
    }
}