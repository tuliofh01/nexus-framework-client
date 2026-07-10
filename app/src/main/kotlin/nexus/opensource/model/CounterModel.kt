package nexus.opensource.model

/**
 * Model layer: plain immutable data, no UI or framework types.
 */
data class CounterModel(
    val count: Int = 0,
) {
    fun incremented(): CounterModel = copy(count = count + 1)

    fun decremented(): CounterModel = copy(count = count - 1)
}
