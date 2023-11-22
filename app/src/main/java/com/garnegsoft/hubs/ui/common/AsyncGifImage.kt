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
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import okhttp3.OkHttpClient
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar


private var loader: ImageLoader? = null
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
                            Log.e("AsyncImageLoader_url", it.request().url.toString())
                            it.proceed(it.request())
                        }
                        .build()
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
                .build(),
            imageLoader = context.CommonImageLoader,
            onState = onState
        ),
        contentDescription = contentDescription,
        modifier = modifier.fillMaxWidth(),
        contentScale = contentScale,
    )
}