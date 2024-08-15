package com.garnegsoft.hubs.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class HabrApi {

    companion object {

        private const val baseAddress = "https://habr.com"
        private var HttpClient: OkHttpClient = OkHttpClient.Builder().build()
        private var csrfToken: String? = null
        private var cookies: String = ""

        fun initializeWithCookies(context: Context, cookies: String){
            this.cookies = cookies
            HttpClient = OkHttpClient.Builder()
                .cache(
                    Cache(
                        directory = File(context.cacheDir, "okhttp_cache"),
                        maxSize = 30 * 1024 * 1024
                    )
                )
                .addInterceptor(Interceptor {
                    val req = it.request()
                        .newBuilder()
                        .addHeader("Cookie", cookies)
                        .build()
                    it.proceed(req)
                })
                .addInterceptor(NoConnectionInterceptor(context))
                .addInterceptor {
                    Firebase.crashlytics.log("HABRAPI ${it.request().method} url:${it.request().url} requestBody:${it.request().body}")
                    it.proceed(it.request())
                }
                .build()

        }
        
        // Shouldn't be used in production. Use only in tests
        fun setHttpClient(client: OkHttpClient) {
            HttpClient = client
        }

        fun get(
            path: String,
            args: Map<String, String>? = null, version: Int = 2,
            cacheControl: CacheControl = CacheControl.Builder()
                .maxStale(1, TimeUnit.MINUTES)
                .build()
        ): Response? {
            val finalArgs = mutableMapOf("hl" to "ru", "fl" to "ru")
            if (args != null) {
                finalArgs.putAll(args)
            }
            val paramsString = StringBuilder()
            finalArgs.keys.forEach { paramsString.append("$it=${finalArgs[it]}&") }
            
            val request = Request
                .Builder()
                .url("$baseAddress/kek/v$version/$path?$paramsString")
                .cacheControl(cacheControl)
                .build()
            try {
                return HttpClient.newCall(request).execute()
            } catch (ex: Exception) {
                if (ex is SocketTimeoutException ||
                    ex is ConnectException ||
                    ex is NoConnectionInterceptor.NoInternetException ||
                    ex is NoConnectionInterceptor.NoConnectivityException &&
                    cacheControl != CacheControl.FORCE_NETWORK) {
                    return get(path, args, version, CacheControl.FORCE_CACHE)
                } else
                    return null
            }
        }

        
        fun post(
            path: String,
            args: Map<String, String>? = null,
            requestBody: RequestBody = String().toRequestBody(),
            version: Int = 2
        ): Response? {
            val token = getCsrfToken()
            val finalArgs = mutableMapOf("hl" to "ru", "fl" to "ru")
            if (args != null) {
                finalArgs.putAll(args)
            }
            val paramsString = StringBuilder()
            finalArgs.keys.forEach({ paramsString.append("$it=${finalArgs[it]}&") })
            val request = Request
                .Builder()
                .post(requestBody)
                .url("$baseAddress/kek/v$version/$path")
                .addHeader("csrf-token", token ?: "")
                .build()
            try {
                return HttpClient.newCall(request).execute()
            } catch (ex: Exception) {
                return null
            }
        }

        fun delete(
            path: String,
            args: Map<String, String>? = null,
            requestBody: RequestBody = String().toRequestBody(),
            version: Int = 2
        ): Response? {
            val token = getCsrfToken()
            val finalArgs = mutableMapOf("hl" to "ru", "fl" to "ru")
            if (args != null) {
                finalArgs.putAll(args)
            }
            val paramsString = StringBuilder()
            finalArgs.keys.forEach({ paramsString.append("$it=${finalArgs[it]}&") })
            val request = Request
                .Builder()
                .delete(requestBody)
                .url("$baseAddress/kek/v$version/$path")
                .addHeader("csrf-token", token ?: "")
                .build()
            try {
                return HttpClient.newCall(request).execute()
            } catch (ex: Exception) {
                return null
            }
        }

        fun getCsrfToken(): String? {
            if (csrfToken == null) {
                val request = Request
                    .Builder()
                    .url("$baseAddress/ru/beta")
                    .build()
                
                val response = try {
                    HttpClient.newCall(request).execute()
                } catch (ex: Exception) {
                    return null
                }

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
        val network = connectivityManager.activeNetwork
        val connection = connectivityManager.getNetworkCapabilities(network)
        return connection != null && (
                connection.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        connection.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
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
            val address = InetAddress.getByName("yandex.ru")
            !address.equals("")
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

