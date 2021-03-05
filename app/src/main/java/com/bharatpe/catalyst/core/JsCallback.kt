package com.bharatpe.catalyst.core

import android.webkit.WebView

object JsCallback {

    private const val JavaScript = "javascript: "

    private fun evaluate(webView: WebView?, script: String) {
        if (webView == null) {
            return
        }
        webView.post(Runnable {
            webView.evaluateJavascript(script, null)
        })
    }

    fun jsPaymentResult(webView: WebView?, result: String) {
        val script =
            JavaScript + "callbackPaymentResult('$result')"
        evaluate(webView, script)
    }

    fun jsNoPspAvailable(webView: WebView?) {
        val script =
            JavaScript + "callbackNoPspAvailable()"
        evaluate(webView, script)
    }
}