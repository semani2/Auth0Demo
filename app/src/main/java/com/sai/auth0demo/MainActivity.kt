package com.sai.auth0demo

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.callback.BaseCallback
import com.auth0.android.lock.AuthenticationCallback
import com.auth0.android.lock.Lock
import com.auth0.android.lock.LockCallback
import com.auth0.android.lock.utils.LockException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var account: Auth0
    private var credManager: SecureCredentialsManager? = null
    private var displayLogout: Boolean = false
    private lateinit var lock: Lock

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupAuth0()

        setupUI()

        webLoginButton.setOnClickListener {
            performWebLogin()
        }

        nativeLoginButton.setOnClickListener {
            performNativeLogin()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::lock.isInitialized) {
            lock.onDestroy(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.menu_logout)?.isVisible = displayLogout

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                credManager?.clearCredentials()
                setupUI()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupUI() {
        toggleProgress(true)
        if (credManager?.hasValidCredentials()!!) {
            credManager?.getCredentials(object: BaseCallback<Credentials, CredentialsManagerException> {
                override fun onSuccess(payload: Credentials?) {
                    runOnUiThread {
                        webLoginButton.visibility = View.GONE
                        nativeLoginButton.visibility = View.GONE

                        current_auth_status_text_view.setText(R.string.logged_in)
                        token_text_view.text = payload?.accessToken
                        token_info_text_view.setText(R.string.token_info_title)

                        allowLogout(true)
                        toggleProgress(false)
                    }
                }

                override fun onFailure(error: CredentialsManagerException?) {
                    runOnUiThread {
                        webLoginButton.visibility = View.VISIBLE
                        nativeLoginButton.visibility = View.VISIBLE

                        current_auth_status_text_view.setText(R.string.not_logged_in)
                        token_text_view.setText(R.string.not_available)


                        allowLogout(false)
                        toggleProgress(false)

                        Toast.makeText(applicationContext,
                            "Failed to fetch stored credentials",
                            Toast.LENGTH_LONG).show()

                        Log.e(TAG, "Failed to fetch stored credentials", error)
                    }
                }

            })
        } else {
            runOnUiThread {
                webLoginButton.visibility = View.VISIBLE
                nativeLoginButton.visibility = View.VISIBLE

                current_auth_status_text_view.setText(R.string.not_logged_in)
                token_text_view.setText(R.string.not_available)

                allowLogout(false)
                toggleProgress(false)
            }
        }
    }

    private fun performNativeLogin() {
        account = Auth0("ynyVUzYQQ5Lhq4kPs42gky1ESNxTOtwE", "dev-n03idre2.auth0.com")
        //Configure the account in OIDC conformant mode
        account.isOIDCConformant = true
        //Use the account to launch Lock

        lock = Lock.newBuilder(account, object: AuthenticationCallback() {
            override fun onAuthentication(credentials: Credentials?) {
                runOnUiThread {
                    current_auth_status_text_view.setText(R.string.logged_in)
                    token_text_view.text = credentials?.accessToken

                    webLoginButton.visibility = View.GONE
                    nativeLoginButton.visibility = View.GONE

                    credManager?.saveCredentials(credentials!!)

                    allowLogout(true)
                    toggleProgress(false)

                    Log.d(TAG, "Scope ${credentials?.scope}")
                    Log.d(TAG, "Type ${credentials?.type}")
                    Log.d(TAG, "Expires in ${credentials?.expiresIn}")
                }
            }

            override fun onCanceled() {
                runOnUiThread {
                    allowLogout(false)
                    toggleProgress(false)
                }
            }

            override fun onError(error: LockException?) {
                runOnUiThread {
                    current_auth_status_text_view.setText(R.string.login_failed)
                    token_info_text_view.setText(R.string.failure_reason)
                    token_text_view.text = error?.message

                    Log.e(TAG, "Login failed", error)

                    allowLogout(false)
                    toggleProgress(false)
                }
            }

        })
            .closable(true)
            .withScheme("demo")
            .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
            .build(this)

        startActivity(lock.newIntent(this))
    }

    /**
     * This function performs authentication using the Web Auth Provider from Auth0
     */
    private fun performWebLogin() {
        toggleProgress(true)
        if (::account.isInitialized) {
            WebAuthProvider.login(account)
                .withScheme("demo")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .withScope("openid profile email read:current_user update:current_user_metadata")
                .withParameters(mapOf(Pair("prompt", "login")))
                .start(this, object: AuthCallback {
                    override fun onSuccess(credentials: Credentials) {
                        runOnUiThread {
                            current_auth_status_text_view.setText(R.string.logged_in)
                            token_text_view.text = credentials.accessToken

                            webLoginButton.visibility = View.GONE
                            nativeLoginButton.visibility = View.GONE

                            credManager?.saveCredentials(credentials)

                            allowLogout(true)
                            toggleProgress(false)

                            Log.d(TAG, "Scope ${credentials.scope}")
                            Log.d(TAG, "Type ${credentials.type}")
                            Log.d(TAG, "Expires in ${credentials.expiresIn}")
                        }
                    }

                    override fun onFailure(dialog: Dialog) {
                        runOnUiThread {
                            dialog.show()

                            allowLogout(false)
                            toggleProgress(false)
                        }
                    }

                    override fun onFailure(exception: AuthenticationException?) {
                        runOnUiThread {
                            current_auth_status_text_view.setText(R.string.login_failed)
                            token_info_text_view.setText(R.string.failure_reason)
                            token_text_view.text = exception?.message

                            Log.e(TAG, "Login failed", exception)

                            allowLogout(false)
                            toggleProgress(false)
                        }
                    }

                })
        }
        toggleProgress(false)
    }

    private fun setupAuth0() {
        account = Auth0(this)
        account.isOIDCConformant = true

        val apiClient = AuthenticationAPIClient(account)
        credManager = SecureCredentialsManager(this, apiClient, SharedPreferencesStorage(this))
    }

    private fun allowLogout(allow: Boolean) {
        displayLogout = allow
        invalidateOptionsMenu()
    }

    private fun toggleProgress(isBusy: Boolean) {
        progressBar.visibility = if (isBusy) View.VISIBLE else View.GONE
    }
}
