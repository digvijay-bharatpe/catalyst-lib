package com.bharatpe.catalyst.core

import android.app.Activity

object CatalystHelper {

    fun getPaymentInit(activity: Activity) : PaymentInit {
        return PaymentInitImpl().apply {
            initSdk(activity)
        }
    }
}