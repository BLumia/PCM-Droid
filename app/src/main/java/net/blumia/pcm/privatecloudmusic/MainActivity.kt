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
import java.net.URL
import java.util.prefs.Preferences


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val srvList = ArrayList<ServerItem>()
        srvList.add(ServerItem(1,"1", URL("http://baidu.com"),
                URL("http://baidu.com"),"1"))
        srvList.add(ServerItem(1,"1", URL("http://baidu.com"),
                URL("http://baidu.com"),"1"))
        val srvListAdapter = ServerIconListAdapter(srvList)
        rv_server_icon_list.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rv_server_icon_list.adapter = srvListAdapter

        btn_serverPopupMenu.setOnClickListener(this)
        btn_options.setOnClickListener(this)
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
                            uiAddServer()
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

    private fun uiAddServer() {
        // FIXME: this is NOT the right way to do do insert, it actually doesn't works
        var a: ServerIconListAdapter = rv_server_icon_list.adapter as ServerIconListAdapter
        a.addItem(ServerItem(1,"1", URL("http://baidu.com"),
                URL("http://baidu.com"),"1"))
        a.addItem(ServerItem(1,"1", URL("http://baidu.com"),
                URL("http://baidu.com"),"1"))
    }
}
