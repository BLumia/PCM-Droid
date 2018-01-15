package net.blumia.pcm.privatecloudmusic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.pager_add_server_step1.view.*
import java.net.MalformedURLException
import java.net.URL

/**
 * Add server setup page, fragment 1.
 *
 * We allow user enter a simple url to add server, with a server API url or a `pcm://` schema url
 * Then we will check the valid of url user entered, parse the info and fill in the blanks at step 2
 *
 * Currently this fragment is just a placeholder, doesn't check valid at all.
 */
class AddServerStep1 : Fragment() {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null
    private var mUrlEnteredListener: UrlEnteredListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.pager_add_server_step1, container, false)
        rootView.prompt_api_url.requestFocus()
        rootView.email_sign_in_button.setOnClickListener { attemptAddServer() }
        return rootView
    }

    private fun isUrlValid(url: String): Boolean {
        //TODO: Replace this with your own logic
        return url.isNotEmpty()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            mUrlEnteredListener = context as UrlEnteredListener
        } catch (e: Exception) {
            Log.e("Listener", e.toString())
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptAddServer() {

        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        view!!.prompt_api_url.error = null
        //password.error = null

        // Store values at the time of the login attempt.
        val apiUrlStr = view!!.prompt_api_url.text.toString()
        //val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid email address.
        if (TextUtils.isEmpty(apiUrlStr)) {
            view!!.prompt_api_url.error = getString(R.string.error_field_required)
            focusView = view!!.prompt_api_url
            cancel = true
        } else if (!isUrlValid(apiUrlStr)) {
            view!!.prompt_api_url.error = getString(R.string.error_invalid_url)
            focusView = view!!.prompt_api_url
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
            mAuthTask = UserLoginTask(apiUrlStr)
            mAuthTask!!.execute(null as Void?)
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
        fun newInstance(sectionNumber: Int): AddServerStep1 {
            val fragment = AddServerStep1()
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
    inner class UserLoginTask internal constructor(private val mApiUrl: String) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.
            val apiUrl: URL?
            val fileRootUrl: URL?

            try {
                apiUrl = URL(mApiUrl)
                fileRootUrl = URL(mApiUrl)
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                return false
            } catch (e: MalformedURLException) {
                return false
            }

            val item = ServerItem(-1, "name", apiUrl, fileRootUrl, "", ServerType.SRV_PCM)
            mUrlEnteredListener?.onUrlEnteredCorrectly(item)
            return true //
        }

        override fun onPostExecute(success: Boolean?) {

            mAuthTask = null
            showProgress(false)

            if (success!!) {
                //finish()
            } else {
                view!!.prompt_api_url.error = getString(R.string.error_invalid_url)
                view!!.prompt_api_url.requestFocus()
            }
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }

    interface UrlEnteredListener {
        fun onUrlEnteredCorrectly(srvItem: ServerItem)
    }
}