package com.sai.auth0demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.auth0.android.Auth0
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

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

    }

    private fun setupAuth0() {
        val account = Auth0(this)
        account.isOIDCConformant = true
    }
}
