package net.blumia.pcm.privatecloudmusic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.support.v7.app.AppCompatActivity
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View

import kotlinx.android.synthetic.main.activity_add_server.*

/**
 * A login screen that offers login via email/password.
 */
class AddServerActivity : AppCompatActivity() {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_server)
        prompt_api_url.requestFocus()
        /*
        prompt_api_url.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptAddServer()
                return@OnEditorActionListener true
            }
            false
        })
        */
        email_sign_in_button.setOnClickListener { attemptAddServer() }
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
        prompt_api_url.error = null
        //password.error = null

        // Store values at the time of the login attempt.
        val apiUrlStr = prompt_api_url.text.toString()
        //val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid email address.
        if (TextUtils.isEmpty(apiUrlStr)) {
            prompt_api_url.error = getString(R.string.error_field_required)
            focusView = prompt_api_url
            cancel = true
        } else if (!isUrlValid(apiUrlStr)) {
            prompt_api_url.error = getString(R.string.error_invalid_url)
            focusView = prompt_api_url
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

    private fun isUrlValid(url: String): Boolean {
        //TODO: Replace this with your own logic
        return url.isNotEmpty()
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

            add_server_form.visibility = if (show) View.GONE else View.VISIBLE
            add_server_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            add_server_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            add_server_progress.visibility = if (show) View.VISIBLE else View.GONE
            add_server_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            add_server_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            add_server_progress.visibility = if (show) View.VISIBLE else View.GONE
            add_server_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val mApiUrl: String) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                return false
            }

            return true //
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTask = null
            showProgress(false)

            if (success!!) {
                finish()
            } else {
                prompt_api_url.error = getString(R.string.error_incorrect_password)
                prompt_api_url.requestFocus()
            }
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }
}
