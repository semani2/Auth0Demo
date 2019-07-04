package com.sai.auth0demo

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var account: Auth0

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupAuth0()

        webLoginButton.setOnClickListener {
            performWebLogin()
        }

        nativeLoginButton.setOnClickListener {
            performNativeLogin()
        }
    }

    private fun performNativeLogin() {

    }

    /**
     * This function performs authentication using the Web Auth Provider from Auth0
     */
    private fun performWebLogin() {
        if (::account.isInitialized) {
            WebAuthProvider.login(account)
                .withScheme("demo")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .start(this, object: AuthCallback {
                    override fun onSuccess(credentials: Credentials) {
                        runOnUiThread {
                            current_auth_status_text_view.setText(R.string.logged_in)
                            token_text_view.text = credentials.accessToken

                            Log.d(TAG, "Scope ${credentials.scope}")
                            Log.d(TAG, "Type ${credentials.type}")
                            Log.d(TAG, "Expires in ${credentials.expiresIn}")
                        }
                    }

                    override fun onFailure(dialog: Dialog) {
                        runOnUiThread {
                            dialog.show()
                        }
                    }

                    override fun onFailure(exception: AuthenticationException?) {
                        runOnUiThread {
                            current_auth_status_text_view.setText(R.string.login_failed)
                            token_info_text_view.setText(R.string.failure_reason)
                            token_text_view.text = exception?.message

                            Log.e(TAG, "Login failed", exception)
                        }
                    }

                })
        }
    }

    private fun setupAuth0() {
        account = Auth0(this)
        account.isOIDCConformant = true
    }
}
