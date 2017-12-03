package net.blumia.pcm.privatecloudmusic

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.drawer_container.*
import android.preference.PreferenceActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.content_main.*
import net.blumia.pcm.privatecloudmusic.SQLiteDatabaseOpenHelper.Companion.DB_TABLE_SRV_LIST
import okhttp3.*
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import org.jetbrains.anko.doAsync
import java.io.IOException
import java.net.URL
import java.util.prefs.Preferences


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var curServerItem: ServerItem? = null
    private var prefs: Prefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = Prefs(this)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val serverIconListAdapter = ServerIconListAdapter(this)
        serverIconListAdapter.setOnItemClickListener(object: ServerIconListAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                fetchFolderList(serverIconListAdapter.getItem(position))
            }
        })
        rv_server_icon_list.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rv_server_icon_list.adapter = serverIconListAdapter

        rv_folder_list.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rv_folder_list.adapter = FolderListAdapter(this)

        rv_song_list.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rv_song_list.adapter = SongListAdapter(this)

        btn_serverPopupMenu.setOnClickListener(this)
        btn_options.setOnClickListener(this)

        val serverCnt = serverIconListAdapter.itemCount
        if (serverCnt > 0) {
            curServerItem = if (prefs!!.curSrvIndex in 0..(serverCnt - 1)) {
                serverIconListAdapter.getItem(prefs!!.curSrvIndex)
            } else {
                serverIconListAdapter.getItem(0)
            }
        } else {
            // open add server activity?
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        when (v?.id) {
            R.id.btn_serverPopupMenu -> run {
                val popup = PopupMenu(this, v)
                popup.menuInflater.inflate(R.menu.server_options, popup.menu)
                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.mi_update_server_info -> run {
                            jumpToAddServerActivity()
                            Log.e("test", "update srv info")
                        }
                        R.id.mi_delete_server -> run {
                            Log.e("test", "delete srv")
                        }
                    }
                    true
                }
                popup.show()
            }
            R.id.btn_options -> run {
                jumpToSettingActivity()
                drawer_layout.closeDrawer(GravityCompat.START)
            }
        }
    }

    fun getServerListDataFromDB(): List<Map<String, Any?>> {
        var srvList:List<Map<String, Any?>> = ArrayList()
        database.use {
            select(DB_TABLE_SRV_LIST).exec {
                srvList = parseList(
                    object: MapRowParser<Map<String, Any?>> {
                        override fun parseRow(columns : Map<String, Any?>) : Map<String, Any?> {
                            //srvList.add(columns)
                            Log.e("asd", columns.toString())
                            return columns
                        }
                    }
                )
            }
        }
        return srvList
    }

    private fun fetchSongList(folderItem: PlaylistItem) {
        if (curServerItem == null) return
        val folderOrPlaylist = if (folderItem.type == PlaylistType.FOLDER) "folder" else "playlist"
        val httpClient = OkHttpClient()
        val formBody = FormBody.Builder()
                .add("do", "getfilelist")
                .add(folderOrPlaylist, folderItem.folderPath)
                .build()
        val request = Request.Builder()
                .url(curServerItem!!.apiUrl)
                .post(formBody)
                .build()
        httpClient.newCall(request).enqueue(object: okhttp3.Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response?) {
                val result = response!!.body()!!.string()
                this@MainActivity.runOnUiThread {
                    Log.e("Response", result)

                    val songListAdapter = rv_song_list.adapter as SongListAdapter
                    songListAdapter.updateListFromJsonString(result)
                    songListAdapter.notifyDataSetChanged()

                }
            }
        })
    }

    private fun fetchFolderList(srvItem: ServerItem) {
        val httpClient = OkHttpClient()
        val formBody = FormBody.Builder()
                .add("do", "getfilelist")
                .build()
        val request = Request.Builder()
                .url(srvItem.apiUrl)
                .post(formBody)
                .build()
        httpClient.newCall(request).enqueue(object: okhttp3.Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response?) {
                val result = response!!.body()!!.string()
                this@MainActivity.runOnUiThread {
                    Log.e("Response", result)

                    val folderListAdapter = rv_folder_list.adapter as FolderListAdapter
                    folderListAdapter.updateListFromJsonString(result)
                    folderListAdapter.notifyDataSetChanged()

                    if (folderListAdapter.itemCount > 0) {
                        fetchSongList(folderListAdapter.getItem(0))
                    }
                }
            }
        })
    }

    private fun jumpToAddServerActivity() {
        val intent = Intent(this, AddServerActivity::class.java)
        startActivity(intent)
    }

    private fun jumpToSettingActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment::class.java.name)
        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true)
        startActivity(intent)
    }
}
