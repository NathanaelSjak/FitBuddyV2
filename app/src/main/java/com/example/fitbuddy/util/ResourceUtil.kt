package com.example.fitbuddy.util

import android.content.Context
import android.net.Uri

object ResourceUtil {
    fun getRawResourceUri(context: Context, resourceName: String): Uri {
        val resId = getRawResourceId(context, resourceName)
        return Uri.parse("android.resource://${context.packageName}/$resId")
    }

    fun getRawResourceId(context: Context, resourceName: String): Int {
        // Extract just the filename without path or extension
        val fileName = resourceName.substringAfterLast("/").substringBeforeLast(".")
        
        // Try to find in drawable first
        var resId = context.resources.getIdentifier(
            fileName,
            "drawable",
            context.packageName
        )
        
        // If not found in drawable, try raw
        if (resId == 0) {
            resId = context.resources.getIdentifier(
                fileName,
                "raw",
                context.packageName
            )
        }
        
        if (resId == 0) {
            throw IllegalArgumentException("Resource not found: $resourceName")
        }
        return resId
    }
} 