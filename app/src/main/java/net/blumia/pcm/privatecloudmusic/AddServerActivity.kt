package net.blumia.pcm.privatecloudmusic

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_add_server.*

/**
 * An add server setup page that offers adding server function via http api url and `pcm://`schema.
 *
 * To make it more easy to add a server to PCM-droid client, we made this setup page.
 */
class AddServerActivity : AppCompatActivity(), AddServerStep1.UrlEnteredListener {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_server)

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
    }

    override fun onUrlEnteredCorrectly(srvItem: ServerItem) {
        mSectionsPagerAdapter?.onUrlEnteredCorrectly(srvItem)
        runOnUiThread {
            container.setCurrentItem(1, true)
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm), AddServerStep1.UrlEnteredListener {

        private val step1: AddServerStep1 = AddServerStep1.newInstance(0)
        private val step2: AddServerStep2 = AddServerStep2.newInstance(1)

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            when (position) {
                0 -> return step1
                1 -> return step2
            }
            return step2 // should be a exception
        }

        override fun getCount(): Int {
            // Show 2 total pages.
            return 2
        }

        override fun onUrlEnteredCorrectly(srvItem: ServerItem) {
            step2.onUrlEnteredCorrectly(srvItem)
        }
    }
}
