package com.madalin.notelo.core.domain.util

object MapNullKeyReplacer {
    /**
     * Replaces the null key of the given [originalMap] with the given [replacementKey] and moves it
     * at the beginning of the map.
     * @return The new map.
     */
    fun <K, V> replaceNullKeyAndMoveToBeginning(
        originalMap: Map<K?, V>,
        replacementKey: K
    ): Map<K, V> {
        val newMap = LinkedHashMap<K, V>()
        originalMap.forEach { (key, value) ->
            if (key == null) {
                newMap[replacementKey] = value
            }
        }
        originalMap.forEach { (key, value) ->
            if (key != null) {
                newMap[key] = value
            }
        }
        return newMap
    }
}