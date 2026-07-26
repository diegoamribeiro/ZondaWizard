package com.dmribeiro.zondatuner.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Sistema de shapes do app:
 * - 8dp: elementos pequenos (chips retangulares, campos compactos)
 * - 12dp: cards e containers de conteúdo
 * - 16dp: sheets, dialogs e superfícies elevadas
 */
val ZondaShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
