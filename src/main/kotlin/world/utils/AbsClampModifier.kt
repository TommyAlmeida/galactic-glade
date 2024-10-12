package io.poin.game.world.utils

import de.articdive.jnoise.core.api.modifiers.NoiseModifier

class AbsClampNoiseModifier : NoiseModifier {
    override fun apply(result: Double): Double {
        return (result + 1.0) * 0.5
    }
}