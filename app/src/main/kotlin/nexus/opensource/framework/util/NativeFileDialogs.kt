package nexus.opensource.framework.util

import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Path
import javax.swing.SwingUtilities

/**
 * AWT file dialogs for Compose Desktop (no extra file-picker dependency).
 */
object NativeFileDialogs {
    fun pickOpenFile(
        title: String,
        fileFilterHint: String = "*.json",
    ): Path? = onEdt {
        val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
        dialog.file = fileFilterHint
        dialog.isVisible = true
        pathFrom(dialog)
    }

    fun pickSaveFile(
        title: String,
        suggestedName: String = "blueprint.json",
    ): Path? = onEdt {
        val dialog = FileDialog(null as Frame?, title, FileDialog.SAVE)
        dialog.file = suggestedName
        dialog.isVisible = true
        pathFrom(dialog)
    }

    private fun pathFrom(dialog: FileDialog): Path? {
        val dir = dialog.directory ?: return null
        val file = dialog.file ?: return null
        return Path.of(dir, file)
    }

    private fun <T> onEdt(block: () -> T): T {
        if (SwingUtilities.isEventDispatchThread()) return block()
        var result: T? = null
        var error: Throwable? = null
        SwingUtilities.invokeAndWait {
            try {
                result = block()
            } catch (t: Throwable) {
                error = t
            }
        }
        error?.let { throw it }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }
}
