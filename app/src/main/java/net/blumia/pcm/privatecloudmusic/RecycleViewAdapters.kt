package net.blumia.pcm.privatecloudmusic

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_avatar_view.view.*
import kotlinx.android.synthetic.main.item_rv_folder.view.*
import java.net.URL
import org.json.JSONException
import android.content.ContentValues.TAG
import android.util.Log
import kotlinx.android.synthetic.main.item_rv_music_items.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder


/**
 * Created by wzc78 on 2017/11/30.
 */
class ServerIconListAdapter(activity: MainActivity) : RecyclerView.Adapter<ServerIconListAdapter.ViewHolder>() {

    private var list : List<Map<String, Any?>> = activity.getServerListDataFromDB()
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.bindItems(list[position])

        // on click listener
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener {
                val position = holder.layoutPosition // 1
                mOnItemClickListener!!.onItemClick(holder.itemView, position) // 2
            }
        }
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

class FolderListAdapter(activity: MainActivity) : RecyclerView.Adapter<FolderListAdapter.ViewHolder>() {

    private var list : ArrayList<PlaylistItem> = ArrayList()
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.bindItems(list[position])

        // on click listener
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener {
                val position = holder.layoutPosition // 1
                mOnItemClickListener!!.onItemClick(holder.itemView, position) // 2
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent!!.context).inflate(R.layout.item_rv_folder, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItem(position: Int): PlaylistItem {
        return list[position]
    }

    fun updateListFromJsonString(json: String) {
        try {
            val json = JSONObject(json)
            val result = json.getJSONObject("result")
            val data = result.getJSONObject("data")
            val folders = data.getJSONArray("subFolderList")
            list.clear()
            for (i in 0 until folders.length()) {
                val rawFolderName = folders.get(i) as String
                val folderName = URLDecoder.decode(rawFolderName, "UTF-8")
                list.add(PlaylistItem(folderName, rawFolderName, PlaylistType.FOLDER))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(oneItem: PlaylistItem) {
            itemView.rvi_folder_name.text = oneItem.name
        }
    }
}

class SongListAdapter(activity: MainActivity) : RecyclerView.Adapter<SongListAdapter.ViewHolder>() {

    private var list : ArrayList<MusicItem> = ArrayList()
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.bindItems(list[position])

        // on click listener
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener {
                val position = holder.layoutPosition // 1
                mOnItemClickListener!!.onItemClick(holder.itemView, position) // 2
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent!!.context).inflate(R.layout.item_rv_music_items, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItem(position: Int): MusicItem {
        return list[position]
    }

    fun updateListFromJsonString(json: String) {
        try {
            val json = JSONObject(json)
            val result = json.getJSONObject("result")
            val data = result.getJSONObject("data")
            val folders = data.getJSONArray("subFolderList")
            val musicList = data.getJSONArray("musicList")
            list.clear()
            for (i in 0 until folders.length()) {
                val rawFolderName = folders.get(i) as String
                val folderName = URLDecoder.decode(rawFolderName, "UTF-8")
                list.add(MusicItem(folderName, rawFolderName, 0L, 0L, true, MusicItemType.SUB_FOLDER))
            }
            for (i in 0 until musicList.length()) {
                val fileObject = musicList.getJSONObject(i)
                val rawFileName = fileObject.getString("fileName")
                val fileName = URLDecoder.decode(rawFileName, "UTF-8")
                val modifiedTime = fileObject.getLong("modifiedTime")
                val fileSize = fileObject.getLong("fileSize")
                list.add(MusicItem(fileName, rawFileName, modifiedTime, fileSize, true, MusicItemType.MUSIC))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(oneItem: MusicItem) {
            itemView.rvi_song_item_name.text = oneItem.name
        }
    }
}