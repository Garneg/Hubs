package com.garnegsoft.hubs.ui.common

import android.net.Uri
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.decode.SvgDecoder
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.ImageRequest
import com.garnegsoft.hubs.data.offlineResourcesDir
import okio.Path.Companion.toPath
import java.io.File

private val colorMatrix = floatArrayOf(
    -0.7f, 0f, 0f, 0f, 255f,
    0f, -0.7f, 0f, 0f, 255f,
    0f, 0f, -0.7f, 0f, 255f,
    0f, 0f, 0f, 1f, 0f
)

@Composable
fun AsyncSvgImage(
    modifier: Modifier = Modifier,
    data: Any?,
    contentScale: ContentScale,
    revertColorsOnDarkTheme: Boolean = true,
    contentDescription: String? = null
) {
    
    val context = LocalContext.current
    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(context)
            .data(data)
            .decoderFactory(SvgDecoder.Factory())
            .fetcherFactory(
                Fetcher.Factory { factoryData, _, _ ->
                    object : Fetcher {
                        override suspend fun fetch(): FetchResult? {
                            if (factoryData is String || factoryData is Uri){
                                val url = factoryData.toString()
                                val fileUri = url.split(":").last()
                                if (offlineResourcesDir == null){
                                    offlineResourcesDir = File(context.filesDir, "offline_resources")
                                }
                                val file = File(offlineResourcesDir, fileUri)
                                
                                if (file.exists()){
                                   return SourceResult(ImageSource(file.absolutePath.toPath()), "image/svg", DataSource.DISK)
                                }
                            }
                            return null
                        }
                    }
                }
            )
            .build(),
        contentDescription = contentDescription,
        colorFilter = if (MaterialTheme.colors.isLight || !revertColorsOnDarkTheme) null else ColorFilter
            .colorMatrix(ColorMatrix(colorMatrix)),
        contentScale = contentScale
    )
}