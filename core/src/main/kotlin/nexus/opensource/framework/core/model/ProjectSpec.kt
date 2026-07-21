package nexus.opensource.framework.core.model

data class ProjectSpec(
    val projectName: String,
    val outputPath: String,
    val appType: AppType,
    val windowTitle: String = NexusBranding.windowTitle(projectName),
    val cppStandard: String = "20",
    val license: String = "Nexus-1.0",
    val scriptProtectionEnabled: Boolean = true,
    /** When set, written to the generated project as `blueprint.json` (overrides template copy). */
    val blueprint: BlueprintFile? = null,
    /** When set, written to the generated project as `flows/flows.json` (overrides template copy). */
    val flows: FlowsFile? = null,
)
