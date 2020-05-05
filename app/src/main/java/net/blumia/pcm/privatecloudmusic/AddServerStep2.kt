package net.blumia.pcm.privatecloudmusic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.pager_add_server_step2.view.*
import net.blumia.pcm.privatecloudmusic.SQLiteDatabaseOpenHelper.Companion.DB_TABLE_SRV_LIST
import org.jetbrains.anko.runOnUiThread
import java.net.MalformedURLException
import java.net.URL

/**
 * Add server setup page, fragment 2.
 *
 * From fragment 1 (step1) we got some server information but we don't know if it's correct.
 * So this page allow user modify server info and then add the server to server list.
 */
class AddServerStep2 : Fragment(), AddServerStep1.UrlEnteredListener {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAsyncTask: AddServerTask? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.pager_add_server_step2, container, false)
        rootView.prompt_api_url.requestFocus()
        rootView.email_sign_in_button.setOnClickListener { attemptAddServer() }
        return rootView
    }

    private fun isUrlValid(url: String): Boolean {
        try {
            URL(url)
        } catch (e: MalformedURLException) {
            return false
        }
        return url.isNotEmpty()
    }

    override fun onUrlEnteredCorrectly(srvItem: ServerItem) {
        context?.runOnUiThread {
            view!!.prompt_server_name.setText(srvItem.serverName)
            view!!.prompt_api_url.setText(srvItem.apiUrl.toString())
            view!!.prompt_www_root.setText(srvItem.fileRootUrl.toString())
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptAddServer() {

        if (mAsyncTask != null) {
            return
        }

        // Reset errors.
        view!!.prompt_api_url.error = null
        //password.error = null

        // Store values at the time of the login attempt.
        val srvName = view!!.prompt_server_name.text.toString()
        val apiUrlStr = view!!.prompt_api_url.text.toString()
        val fileRootStr = view!!.prompt_www_root.text.toString()
        //val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid api url string.
        if (TextUtils.isEmpty(apiUrlStr)) {
            view!!.prompt_api_url.error = getString(R.string.error_field_required)
            focusView = view!!.prompt_api_url
            cancel = true
        } else if (!isUrlValid(apiUrlStr)) {
            view!!.prompt_api_url.error = getString(R.string.error_invalid_url)
            focusView = view!!.prompt_api_url
            cancel = true
        }

        // Check for a valid file root url string.
        if (TextUtils.isEmpty(fileRootStr)) {
            view!!.prompt_www_root.error = getString(R.string.error_field_required)
            focusView = view!!.prompt_www_root
            cancel = true
        } else if (!isUrlValid(fileRootStr)) {
            view!!.prompt_www_root.error = getString(R.string.error_invalid_url)
            focusView = view!!.prompt_www_root
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAsyncTask = AddServerTask(srvName, apiUrlStr, fileRootStr)
            mAsyncTask!!.execute(null as Void?)
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            view!!.add_server_form.visibility = if (show) View.GONE else View.VISIBLE
            view!!.add_server_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            view!!.add_server_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            view!!.add_server_progress.visibility = if (show) View.VISIBLE else View.GONE
            view!!.add_server_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            view!!.add_server_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            view!!.add_server_progress.visibility = if (show) View.VISIBLE else View.GONE
            view!!.add_server_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(sectionNumber: Int): AddServerStep2 {
            val fragment = AddServerStep2()
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class AddServerTask internal constructor(private val mSrvName: String, private val mApiUrl: String, private val mFileRootUrl: String) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.

            try {
                val values = ContentValues()
                values.put("name", mSrvName)
                values.put("api_url", mApiUrl)
                values.put("file_root_url", mFileRootUrl)
                values.put("password", "")
                context!!.database.use {
                    insert(DB_TABLE_SRV_LIST, null, values)
                }
            } catch (e: InterruptedException) {
                return false
            }

            return true
        }

        override fun onPostExecute(success: Boolean?) {

            mAsyncTask = null
            showProgress(false)

            if (success!!) {
                Toast.makeText(view!!.context, R.string.placeholder, Toast.LENGTH_SHORT).show() // FIXME: toast not shown
                val intent = Intent()//.putExtra("asd","asd")
                activity?.setResult(616, intent)
                activity?.finish()
            } else {
                view!!.prompt_api_url.error = getString(R.string.error_incorrect_password)
                view!!.prompt_api_url.requestFocus()
            }
        }

        override fun onCancelled() {
            mAsyncTask = null
            showProgress(false)
        }
    }
}