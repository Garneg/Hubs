package com.garnegsoft.hubs.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


sealed interface ScrollUpMethods {
    companion object {
        suspend fun scrollLazyList(lazyListState: LazyListState){
            lazyListState.scrollToItem(
                0,
                lazyListState.firstVisibleItemScrollOffset
            )
            lazyListState.animateScrollToItem(0)
        }

        suspend fun scrollNormal(scrollState: ScrollState) {
            scrollState.scrollTo(0)
        }
    }
}

//@OptIn(ExperimentalEncodingApi::class)
//@Preview
//@Composable
//fun AsyncImageBase64() {
//    var encodedString by remember { mutableStateOf("") }
//    var decodedBytes by remember() {
//    }
//    var bitmapa: Bitmap? by remember { mutableStateOf(null) }
//    LaunchedEffect(key1 = Unit, block = {
//        val client = OkHttpClient.Builder()
//            .build()
//        launch(Dispatchers.IO){
//            val req = Request.Builder()
//                .url("https://hsto.org/webt/k6/nj/jz/k6njjzz4ztxhbl1vdfhvzwua2u0.jpeg")
//                .get().build()
//            val resp = client.newCall(req).execute()
//            resp?.body?.byteStream()?.let {
////                val bitmap = BitmapFactory.decodeStream(it)
////                bitmapa = bitmap
////                val byteArrayOutputStream = ByteArrayOutputStream()
////                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
////                val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
////
//////                encodedString = Base64.encode(it.readBytes())
//////                Log.e("base64", android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT))
////                encodedString = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
////                decodedBytes = android.util.Base64.decode(encodedString, android.util.Base64.DEFAULT)
//            }
//        }
//    })
//    Column() {
//        AsyncImage(
//            modifier = Modifier.fillMaxWidth(),
//            contentScale = ContentScale.Crop,
//            model = decodedBytes,
//            contentDescription = null
//        )
//        Text(text = encodedString)
//    }
//
//}