package com.garnegsoft.hubs.api

import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import okhttp3.OkHttpClient
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.drawable.toDrawable
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.File


private var loader: ImageLoader? = null
var offlineResourcesDir: File? = null
private val Context.CommonImageLoader: ImageLoader
    get() {
        if (loader == null) {
            loader = ImageLoader.Builder(this)
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                
                .okHttpClient {
                    OkHttpClient.Builder()
                        .addInterceptor {
                            val urlString = it.request().url.toString()
                            if (it.request().url.toString().startsWith("offline-article")){
                                val fileUri = urlString.split(":").last()
                                if (offlineResourcesDir == null){
                                    offlineResourcesDir = File(filesDir, "offline_resources")
                                }
                                val file = File(offlineResourcesDir, fileUri)
                                
                                if (file.exists()){
                                    file.readBytes().let {
                                        return@addInterceptor Response.Builder().body(it.toResponseBody("image/${fileUri.split(".").last()}".toMediaType())).build()
                                    }
                                }
                                return@addInterceptor Response.Builder().build()
                            } else {
                                it.proceed(it.request())
                            }
                        }.build()
                }
                .crossfade(true)
                .build()
        }
        return loader!!
    }



@Composable
fun AsyncGifImage(
    modifier: Modifier = Modifier,
    model: Any?,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null,
    onState: (AsyncImagePainter.State) -> Unit = {}
) {
    val context = LocalContext.current
    
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context)
                .data(model)
                .size(Size.ORIGINAL)
                .fetcherFactory(
                    Fetcher.Factory { data, options, imageLoader ->
                        CommonImageRequestFetcher(data, context)
                    }
                )
                .build(),
            imageLoader = context.CommonImageLoader,
            onState = onState
        ),
        contentDescription = contentDescription,
        modifier = modifier.fillMaxWidth(),
        contentScale = contentScale,
    )
}

class CommonImageRequestFetcher(val data: Any, val context: Context) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        if (data is String || data is Uri){
            val url = data.toString()
            val fileUri = url.split(":").last()
            if (offlineResourcesDir == null){
                offlineResourcesDir = File(context.filesDir, "offline_resources")
            }
            val file = File(offlineResourcesDir, fileUri)
            
            if (file.exists()){
                file.readBytes().let {
                    val image = BitmapFactory.decodeByteArray(it, 0, it.size).toDrawable(context.resources)
                    return DrawableResult(image, false, DataSource.DISK)
                }
            }
        }
        return null
    }
}