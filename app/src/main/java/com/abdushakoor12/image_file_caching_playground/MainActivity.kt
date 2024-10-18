package com.abdushakoor12.image_file_caching_playground

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.abdushakoor12.image_file_caching_playground.ui.theme.Image_file_caching_playgroundTheme
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Image_file_caching_playgroundTheme {
                Home()
            }
        }
    }

}

@Composable
fun Home() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {

            CachedNetworkImage(
                "https://picsum.photos/id/1003/200/300?grayscale",
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            CachedNetworkImage(
                "https://user-images.githubusercontent.com/74038190/212281763-e6ecd7ef-c4aa-45b6-a97c-f33f6bb592bd.gif",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            CachedNetworkImage(
                "https://media2.giphy.com/media/v1.Y2lkPTc5MGI3NjExcHdzbTZtdzJkaXppdjlreTVxeWd1dXBuZ2xobW41Zmp0b21rcmx4MyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/gw3IWyGkC0rsazTi/200.webp",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

        }
    }
}

@Composable
fun CachedNetworkImage(
    url: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
) {
    var loading by remember(url) { mutableStateOf(true) }
    var imageType by remember { mutableStateOf<ImageType>(ImageType.NetworkImage(url)) }
    val context = LocalContext.current
    val imageDownloader = ((context as Activity).application as App).imageDownloader


    LaunchedEffect(url) {
        imageDownloader.getCachedFile(url)?.let {
            imageType = ImageType.FileImage(it)
            loading = false
        } ?: run {
            imageDownloader.downloadImage(url).let {
                when (it) {
                    is Either.Left -> {
                        loading = false
                    }

                    is Either.Right -> {
                        imageType = ImageType.FileImage(it.value)
                        loading = false
                    }
                }
            }
        }
    }

    val imageUri: String = imageType.uri

    if (!loading) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(imageUri)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    }
}

sealed class ImageType(
    val uri: String
) {
    data class NetworkImage(val link: String) : ImageType(link)
    data class FileImage(val file: File) : ImageType(file.path)
}
