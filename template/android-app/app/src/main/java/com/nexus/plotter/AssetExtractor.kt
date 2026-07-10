package com.nexus.plotter

import android.content.Context
import android.content.res.AssetManager
import java.io.File
import java.io.IOException

/**
 * Copies APK assets (scripts/, ui/, blueprint.json) into internal storage so
 * the native SDL loop can read them with ordinary filesystem paths.
 */
object AssetExtractor {
    private const val ROOT = "nxs-assets"

    fun ensureExtracted(context: Context): File {
        val root = File(context.filesDir, ROOT)
        if (!root.exists()) {
            root.mkdirs()
            copyAssetTree(context.assets, "", root)
        }
        return root
    }

    private fun copyAssetTree(assets: AssetManager, assetPath: String, dest: File) {
        val children = assets.list(assetPath) ?: return
        if (children.isEmpty()) {
            copyAssetFile(assets, assetPath, dest)
            return
        }
        if (!dest.exists()) {
            dest.mkdirs()
        }
        for (child in children) {
            val childAssetPath = if (assetPath.isEmpty()) child else "$assetPath/$child"
            copyAssetTree(assets, childAssetPath, File(dest, child))
        }
    }

    private fun copyAssetFile(assets: AssetManager, assetPath: String, dest: File) {
        dest.parentFile?.mkdirs()
        assets.open(assetPath).use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
    }
}
