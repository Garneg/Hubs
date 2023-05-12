package com.garnegsoft.hubs.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_RESPONSE
import org.jsoup.Jsoup
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class HabrApi {

    companion object {

        private const val baseAddress = "https://habr.com"
        lateinit var HttpClient: OkHttpClient
        private var csrfToken: String? = null

        fun get(path: String, args: Map<String, String>? = null, version: Int = 2): Response? {
            val finalArgs = mutableMapOf("hl" to "ru", "fl" to "ru")
            if (args != null) {
                finalArgs.putAll(args)
            }
            val paramsString = StringBuilder()
            finalArgs.keys.forEach { paramsString.append("$it=${finalArgs[it]}&") }

            val request = Request
                .Builder()
                .url("$baseAddress/kek/v$version/$path?$paramsString")
                .build()
            try {
                return HttpClient.newCall(request).execute()

            } catch (ex: Exception) {
                return null
            }
        }

        fun post(
            path: String,
            args: Map<String, String>? = null,
            requestBody: RequestBody = String().toRequestBody(),
            version: Int = 2
        ): Response {
            val token = getCsrfToken()
//            val finalArgs = mutableMapOf("hl" to "ru", "fl" to "ru")
//            if (args != null) {
//                finalArgs.putAll(args)
//            }
            val paramsString = StringBuilder()
//            finalArgs.keys.forEach({ paramsString.append("$it=${finalArgs[it]}&") })
            val request = Request
                .Builder()
                .post(requestBody)
                .url("$baseAddress/kek/v$version/$path")
                .addHeader("csrf-token", token ?: "")
                .build()
            return HttpClient.newCall(request).execute()
        }

        fun getCsrfToken(): String? {
            if (csrfToken == null) {
                var request = Request
                    .Builder()
                    .url("$baseAddress/ru/beta")
                    .build()
                val response = HttpClient.newCall(request).execute()

                response.body?.string()?.let {
                    Jsoup.parse(it).getElementsByTag("meta")
                        .find { it.attr("name") == "csrf-token" }?.let {
                        csrfToken = it.attr("content")
                    }
                } ?: return null
            }
            return csrfToken
        }

    }
}


class NoConnectionInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (!isConnectionOn()) {
            throw NoConnectivityException()

        } else if (!isInternetAvailable()) {
            throw NoInternetException()
        } else {
            chain.proceed(chain.request())
        }
    }

    private fun isConnectionOn(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val connection = connectivityManager.getNetworkCapabilities(network)
            return connection != null && (
                    connection.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            connection.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val activeNetwork = connectivityManager.activeNetworkInfo
            if (activeNetwork != null) {
                return (activeNetwork.type == ConnectivityManager.TYPE_WIFI ||
                        activeNetwork.type == ConnectivityManager.TYPE_MOBILE)
            }
            return false
        }
    }

    private fun isInternetAvailable(): Boolean {
        return try {
            val timeoutMs = 1500
            val sock = Socket()
            val sockaddr = InetSocketAddress("8.8.8.8", 53)

            sock.connect(sockaddr, timeoutMs)
            sock.close()

            true
        } catch (e: IOException) {
            false
        }

    }

    class NoConnectivityException : IOException() {
        override val message: String
            get() = "No network available, please check your WiFi or Data connection"
    }

    class NoInternetException() : IOException() {
        override val message: String
            get() = "No internet available, please check your connected WIFi or Data"
    }
}
