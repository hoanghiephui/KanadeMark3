package com.podcast.core.network.util

import android.content.Context
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import java.io.File
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager

fun installCertificates(builder: OkHttpClient.Builder) {
    val trustManager: X509TrustManager = BackportTrustManager.create()
    builder.sslSocketFactory(PodSslSocketFactory(trustManager), trustManager)
    builder.connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
}

private const val CONNECTION_TIMEOUT = 10000
private const val READ_TIMEOUT = 30000
private const val MAX_CONNECTIONS = 8

fun newBuilder(context: Context): OkHttpClient.Builder {
    System.setProperty(
        "http.maxConnections",
        MAX_CONNECTIONS.toString()
    )
    val builder = OkHttpClient.Builder()
    builder.networkInterceptors().add(UserAgentInterceptor())

    // set cookie handler
    val cm = CookieManager()
    cm.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
    builder.cookieJar(JavaNetCookieJar(cm))

    // set timeouts
    builder.connectTimeout(
        CONNECTION_TIMEOUT.toLong(),
        TimeUnit.MILLISECONDS
    )
    builder.readTimeout(
        READ_TIMEOUT.toLong(),
        TimeUnit.MILLISECONDS
    )
    builder.writeTimeout(
        READ_TIMEOUT.toLong(),
        TimeUnit.MILLISECONDS
    )
    builder.cache(
        Cache(
            File(context.cacheDir, "okhttp"),
            20L * 1000000
        )
    ) // 20MB

    // configure redirects
    builder.followRedirects(true)
    builder.followSslRedirects(true)
    //installCertificates(builder)
    return builder
}