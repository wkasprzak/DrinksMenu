package com.am.drinks.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class CustomColors(
    val plum: Color = Plum,
    val rose: Color = Rose,
    val coral: Color = Coral,
    val peach: Color = Peach,
    val vanilla: Color = Vanilla,
    val cocoa: Color = Cocoa,
    val shot: Color = Shot,
    val beer: Color = Beer,
    val tea: Color = Tea,
    val punch: Color = Punch,
    val shake: Color = Shake,
    val soft: Color = Soft,
    val homemade: Color = Homemade,
    val other: Color = Other
)

val LocalCustomColors = staticCompositionLocalOf { CustomColors() }