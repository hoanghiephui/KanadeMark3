package com.podcast.core.network.util

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class IndexInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.clear()
        val now = Date()
        calendar.time = now
        val secondsSinceEpoch = calendar.timeInMillis / 1000L
        val apiHeaderTime = secondsSinceEpoch.toString()
        val data4Hash = "XTMMQGA2YZ4WJUBYY4HKXAaAhk4^2YBsTE33vdbwbZNj82ZRLABDDqFdKe7x$apiHeaderTime"
        val hashString = sha1(data4Hash)
        return chain.proceed(
            chain.request().newBuilder()
                .addHeader("X-Auth-Date", apiHeaderTime)
                .addHeader("X-Auth-Key", "XTMMQGA2YZ4WJUBYY4HK")
                .addHeader("Authorization", hashString ?: "")
                .addHeader("User-Agent", "AntennaPod")
                .build()
        )
    }

    companion object {
        private fun sha1(clearString: String): String? {
            return try {
                val messageDigest = MessageDigest.getInstance("SHA-1")
                messageDigest.update(clearString.toByteArray(StandardCharsets.UTF_8))
                toHex(messageDigest.digest())
            } catch (ex: Exception) {
                Timber.e(ex)
                null
            }
        }

        private fun toHex(bytes: ByteArray): String {
            val buffer = StringBuilder()
            for (b in bytes) {
                buffer.append(String.format(Locale.getDefault(), "%02x", b))
            }
            return buffer.toString()
        }
    }
}
