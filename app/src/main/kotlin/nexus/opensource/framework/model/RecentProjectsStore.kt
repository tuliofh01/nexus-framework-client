package nexus.opensource.framework.model

import androidx.compose.runtime.mutableStateListOf
import nexus.opensource.framework.core.RepoRoot
import nexus.opensource.framework.core.model.BlueprintJson
import nexus.opensource.framework.core.model.FlowsJson
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.isDirectory
import kotlin.io.path.name

/**
 * Represents a recently opened or generated Nexus project.
 */
data class RecentProject(
    val name: String,
    val path: String,
    val appType: String,
    val lastOpenedEpochMs: Long,
) {
    fun formattedDate(): String = formattedLastOpened()

    fun formattedLastOpened(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
            .withZone(ZoneId.systemDefault())
        return formatter.format(Instant.ofEpochMilli(lastOpenedEpochMs))
    }
}

/**
 * Recent projects backed by a scan of `builds/framework/` plus in-session opens.
 */
class RecentProjectsStore {
    private val _projects = mutableStateListOf<RecentProject>()

    fun loadRecent(): List<RecentProject> {
        if (_projects.isEmpty()) {
            refresh()
        }
        return _projects.toList()
    }

    fun refresh() {
        val scanned = scanGeneratedProjects()
        val byPath = linkedMapOf<String, RecentProject>()
        for (p in scanned) byPath[p.path] = p
        for (p in _projects) {
            // Keep session-recorded opens ahead of scan order
            byPath.remove(p.path)
            byPath[p.path] = p
        }
        _projects.clear()
        _projects.addAll(byPath.values.sortedByDescending { it.lastOpenedEpochMs }.take(MAX_RECENT))
    }

    fun recordOpen(project: RecentProject) {
        _projects.removeAll { it.path == project.path }
        _projects.add(0, project.copy(lastOpenedEpochMs = System.currentTimeMillis()))
        while (_projects.size > MAX_RECENT) {
            _projects.removeAt(_projects.lastIndex)
        }
    }

    /**
     * Record the project as recently opened (in-app load). Does not open the OS file manager.
     */
    fun markOpened(project: RecentProject) {
        recordOpen(project)
    }

    companion object {
        const val MAX_RECENT = 12

        fun scanGeneratedProjects(): List<RecentProject> {
            val root = runCatching { RepoRoot.resolve() }.getOrNull() ?: return emptyList()
            val frameworks = root.resolve("builds/framework")
            if (!Files.isDirectory(frameworks)) return emptyList()

            return Files.list(frameworks).use { stream ->
                stream
                    .filter { it.isDirectory() }
                    .map { dir -> toRecentProject(dir) }
                    .filter { it != null }
                    .map { it!! }
                    .sorted { a, b -> b.lastOpenedEpochMs.compareTo(a.lastOpenedEpochMs) }
                    .toList()
            }
        }

        private fun toRecentProject(dir: Path): RecentProject? {
            val blueprint = dir.resolve(BlueprintJson.FILE_NAME)
            val flows = dir.resolve(FlowsJson.DEFAULT_PATH)
            val config = dir.resolve("nxs_config.json")
            if (!Files.isRegularFile(blueprint) &&
                !Files.isRegularFile(flows) &&
                !Files.isRegularFile(config)
            ) {
                return null
            }
            val mtime = sequenceOf(blueprint, flows, config)
                .filter { Files.isRegularFile(it) }
                .map { Files.getLastModifiedTime(it).toMillis() }
                .maxOrNull() ?: System.currentTimeMillis()

            val appType = when {
                Files.isDirectory(dir.resolve("app/src/main")) -> "Android"
                Files.isRegularFile(config) &&
                    runCatching { Files.readString(config) }.getOrNull()
                        ?.contains("android", ignoreCase = true) == true -> "Android"
                else -> "Desktop"
            }

            return RecentProject(
                name = dir.name,
                path = dir.toAbsolutePath().normalize().toString(),
                appType = appType,
                lastOpenedEpochMs = mtime,
            )
        }
    }
}
