package com.dongchyeon.timepicker.ui

data class CurveEffect(
    val alphaEnabled: Boolean = true,
    val minAlpha: Float = 0.2f,
    val scaleYEnabled: Boolean = true,
    val minScaleY: Float = 0.8f
) {
    fun calculateAlpha(distanceFromCenter: Float, maxDistance: Float): Float {
        if (!alphaEnabled) return 1f
        val ratio = (distanceFromCenter / maxDistance).coerceIn(0f, 1f)
        return ((1f - ratio) * (1f - minAlpha) + minAlpha)
    }

    fun calculateScaleY(distanceFromCenter: Float, maxDistance: Float): Float {
        if (!scaleYEnabled) return 1f
        val ratio = (distanceFromCenter / maxDistance).coerceIn(0f, 1f)
        return ((1f - ratio) * (1f - minScaleY) + minScaleY)
    }
}
