package com.bharatpe.catalyst.core

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import com.bharatpe.catalyst.payment.PaymentActivity
import kotlin.Exception

interface PaymentInit {
    fun initPayment(amount: Number, requestCode: Int, identifier: String = "")
}

internal class PaymentInitImpl : PaymentInit {

    private lateinit var activity: Activity

    @Throws
    override fun initPayment(amount: Number, requestCode: Int, identifier: String) {
        startPayment(amount, requestCode, identifier)
    }

    internal fun initSdk(context: Activity) {
        this.activity = context
    }

    private fun startPayment(amount: Number, requestCode: Int, identifier: String) {
        try {
            val pm = activity.packageManager
            val packageInfo =
                pm.getApplicationInfo(activity.packageName, PackageManager.GET_META_DATA)
            val token = packageInfo.metaData.getString(TokenKey)
            val url =
                "$CatalystUrl?amount=$amount&token=$token&paymentIdentifier=$identifier&appName=android"
            val intent = Intent(activity, PaymentActivity::class.java).apply {
                putExtra("url", url)
            }
            activity.startActivityForResult(intent, requestCode)
        } catch (ex: Exception) {
            throw Exception("")
        }
    }

    companion object {

        private const val CatalystUrl = "http://192.168.10.210:3002/"

        private const val TokenKey = "com.bharatpe.catalyst.token"
    }

}
