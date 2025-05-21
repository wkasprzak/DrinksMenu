package com.am.drinks.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class CustomColors(
    val plum: Color = Color.Unspecified,
    val rose: Color = Color.Unspecified,
    val coral: Color = Color.Unspecified,
    val peach: Color = Color.Unspecified,
    val shot: Color = Color.Unspecified,
    val beer: Color = Color.Unspecified,
    val tea: Color = Color.Unspecified,
    val vanilla: Color = Color.Unspecified,
    val cocoa: Color = Color.Unspecified,
    val punch: Color = Color.Unspecified,
    val shake: Color = Color.Unspecified,
    val soft: Color = Color.Unspecified,
    val homemade: Color = Color.Unspecified,
    val other: Color = Color.Unspecified
)
val LightCustomColors = CustomColors(
    plum = PlumLight,
    rose = RoseLight,
    coral = CoralLight,
    peach = PeachLight,
    shot = ShotLight,
    beer = BeerLight,
    tea = TeaLight,
    vanilla = VanillaLight,
    cocoa = CocoaLight,
    punch = PunchLight,
    shake = ShakeLight,
    soft = SoftLight,
    homemade = HomemadeLight,
    other = OtherLight
)

val DarkCustomColors = CustomColors(
    plum = PlumDark,
    rose = RoseDark,
    coral = CoralDark,
    peach = PeachDark,
    shot = ShotDark,
    beer = BeerDark,
    tea = TeaDark,
    vanilla = VanillaDark,
    cocoa = CocoaDark,
    punch = PunchDark,
    shake = ShakeDark,
    soft = SoftDark,
    homemade = HomemadeDark,
    other = OtherDark
)

fun getCategoryColor(category: String, colors: CustomColors): Color {
    return when (category.lowercase()) {
        "cocktail" -> colors.peach
        "ordinary drink" -> colors.coral
        "punch / party drink" -> colors.punch
        "shake" -> colors.shake
        "other / unknown" -> colors.other
        "cocoa" -> colors.cocoa
        "shot" -> colors.shot
        "coffee / tea" -> colors.tea
        "homemade liqueur" -> colors.homemade
        "beer" -> colors.beer
        "soft drink" -> colors.soft
        "non alcoholic" -> colors.vanilla
        else -> Color.LightGray
    }
}

val LocalCustomColors = staticCompositionLocalOf { CustomColors() }