package com.bharatpe.catalyst.payment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.bharatpe.catalyst.BuildConfig
import com.bharatpe.catalyst.R
import com.bharatpe.catalyst.core.JsCallback
import com.bharatpe.catalyst.core.PspTransactionStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        initView()
    }

    private fun initView() {
        webView = findViewById(R.id.payment_wb)
        webView.settings.let {
            it.javaScriptEnabled = true
            it.domStorageEnabled = true
            it.loadsImagesAutomatically = true
        }
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView.addJavascriptInterface(CatalystJsAction(), "jsAction")
        loadUrl()
    }

    private fun loadUrl() {
        val url = intent.getStringExtra("url")
        if (url != null) {
            webView.loadUrl(url)
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun firePaymentIntent(intentUrl: String) {
        val uri = Uri.parse(intentUrl)
        val uriIntent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
        }
        val chooser = Intent.createChooser(uriIntent, "Pay With")
        if (chooser.resolveActivity(packageManager) != null) {
            startActivityForResult(chooser, PaymentRequestCode)
        } else {
            JsCallback.jsNoPspAvailable(webView)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PaymentRequestCode) {
            var pspResponse: HashMap<String, String> = hashMapOf()
            if (resultCode == Activity.RESULT_OK && data != null) {
                pspResponse["resultStatus"] = PspTransactionStatus.Success.name
                if (resultCode == Activity.RESULT_OK) {
                    pspResponse = parseOtherAppsPaymentResponse(data)
                } else {
                    pspResponse["status"] = PspTransactionStatus.Submitted.name
                }

            } else { // Payment failure or cancelled case
                pspResponse["resultStatus"] = PspTransactionStatus.Failure.name
            }
            val gsonStr = Gson().toJson(pspResponse)
            JsCallback.jsPaymentResult(webView, gsonStr)
        }
    }

    private fun parseOtherAppsPaymentResponse(data: Intent?): HashMap<String, String> {
        val pspResponse = HashMap<String, String>()
        if (data != null) {
            val bundle = data.extras
            if (bundle != null) {
                val keys = bundle.keySet()
                for (key in keys) {
                    val value = bundle.getString(key)
                    if (value != null) pspResponse[key] = value.toLowerCase(Locale.ENGLISH)
                }
                pspResponse["txnId"] = bundle.getString("txnId", "")
                pspResponse["txnRef"] = bundle.getString("txnRef", "")
                pspResponse["responseCode"] = bundle.getString("responseCode", "")
                val pspRespStr: String = Gson().toJson(
                    pspResponse,
                    object :
                        TypeToken<HashMap<String?, StringBuffer?>?>() {}.getType()
                )
                if (pspRespStr.contains("success") || pspRespStr.contains("submitted")) {
                    pspResponse["status"] = PspTransactionStatus.Success.name
                } else if (pspRespStr.contains("failure") || pspRespStr.contains("failed")) {
                    pspResponse["status"] = PspTransactionStatus.Submitted.name
                } else {
                    pspResponse["status"] = PspTransactionStatus.Submitted.name
                }
            } else {
                pspResponse["status"] = PspTransactionStatus.Submitted.name
            }
        } else {
            pspResponse["status"] = PspTransactionStatus.Submitted.name
        }
        return pspResponse
    }

    companion object {
        private const val PaymentRequestCode = 51
    }

    inner class CatalystJsAction {

        @JavascriptInterface
        fun fireIntent(intentUrl: String) {
            firePaymentIntent(intentUrl)
        }

        @JavascriptInterface
        fun close() {
            finish()
        }
    }

}