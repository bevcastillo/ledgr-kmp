package com.bev.ledgr.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * A rounded rectangle with smoothed corners, similar to Figma's corner smoothing.
 * smoothing: Of looks like a normal rounded corner, 1f is the smoothest.
 */

class SquircleShape(private val cornerRadius: Dp, private val smoothing: Float = 0.6f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val baseRadius = with(density) { cornerRadius.toPx() }

        // Smoothing spreads the curve further along each edge...
        val r = (baseRadius * (1f + smoothing)).coerceAtMost(minOf(w, h) / 2f)
        // ...and pulls the control points toward the corner for a gentler bend
        val k = 0.552f + 0.3f * smoothing
        val c = r * (1f - k)

        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(w - r, 0f)
            cubicTo(w - c, 0f, w, c, w, r)
            lineTo(w, h - r)
            cubicTo(w, h - c, w - c, h, w - r, h)
            lineTo(r, h)
            cubicTo(c, h, 0f, h - c, 0f, h - r)
            lineTo(0f, r)
            cubicTo(0f, c, c, 0f, r, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}
