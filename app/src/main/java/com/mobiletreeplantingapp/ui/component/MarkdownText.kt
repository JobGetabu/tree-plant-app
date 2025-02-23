package com.mobiletreeplantingapp.ui.component

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                movementMethod = LinkMovementMethod.getInstance()
            }
        },
        update = { textView ->
            val markwon = Markwon.builder(textView.context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TablePlugin.create(textView.context))
                .usePlugin(CoilImagesPlugin.create(textView.context))
                .usePlugin(LinkifyPlugin.create())
                .build()
            
            markwon.setMarkdown(textView, markdown)
        }
    )
} 