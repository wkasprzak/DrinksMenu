package com.am.drinks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.am.drinks.data.Drink
import com.am.drinks.ui.theme.DrinksTheme
import com.am.drinks.ui.theme.LocalCustomColors

class DrinkDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val drink = intent.getSerializableExtra("drink") as? Drink
        if (drink == null) {
            finish()
            return
        }

        setContent {
            DrinksTheme {
                DrinkDetailScreen(drink) {
                    finish()
                }
            }
        }
    }
}

@Composable
fun DrinkDetailScreen(drink: Drink, onBack: () -> Unit) {
    val customColors = LocalCustomColors.current
    val isPortrait = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    val backgroundColor = when (drink.category.lowercase()) {
        "cocktail" -> customColors.peach
        "ordinary drink" -> customColors.coral
        "punch / party drink" -> customColors.punch
        "shake" -> customColors.shake
        "other / unknown" -> customColors.other
        "cocoa" -> customColors.cocoa
        "shot" -> customColors.shot
        "coffee / tea" -> customColors.tea
        "homemade liqueur" -> customColors.homemade
        "beer" -> customColors.beer
        "soft drink" -> customColors.soft
        "non alcoholic" -> customColors.vanilla
        else -> Color.LightGray
    }

    // wygląd dla trybu portretowego i horyzontalnego osobno
    if (isPortrait) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DrinkImage(drink)
            Spacer(modifier = Modifier.height(16.dp))
            DrinkCardLayout(drink, backgroundColor, onBack)
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DrinkImage(drink)
            }
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                DrinkCardContent(drink, backgroundColor, onBack)
            }
        }
    }
}

@Composable
fun DrinkImage(drink: Drink) {
    val customColors = LocalCustomColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) { // pokazywanie obrazka, w razie braku - ikona serca
        if (!drink.imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = drink.imageUrl,
                contentDescription = drink.name,
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Drink image",
                modifier = Modifier.size(64.dp),
                tint = customColors.plum.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun DrinkCardLayout(drink: Drink, backgroundColor: Color, onBack: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        DrinkCardContent(drink, backgroundColor, onBack)
    }
}

@Composable
fun DrinkCardContent(drink: Drink, backgroundColor: Color, onBack: () -> Unit) {
    val customColors = LocalCustomColors.current

    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = drink.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = customColors.plum)
        Text("Type: ${drink.type}", fontSize = 18.sp)
        Text("Category: ${drink.category}", fontSize = 18.sp)
        drink.iba?.let { Text("IBA: $it", fontSize = 18.sp) }
        drink.glass?.let { Text("Glass: $it", fontSize = 18.sp) }
        Text("Time: ${drink.time} minutes", fontSize = 18.sp)

        if (!drink.instructions.isNullOrBlank()) {
            Text("Instructions:", fontWeight = FontWeight.Bold)
            Text(drink.instructions!!)
        }

        if (drink.ingredients.isNotEmpty()) {
            Text("Ingredients:", fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                drink.ingredients.forEach { ingredient ->
                    Text("• $ingredient")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onBack() },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = customColors.plum)
        ) {
            Text("Back", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDrinkDetail() {
    DrinksTheme {
        DrinkDetailScreen(
            drink = Drink(
                id = "preview",
                name = "Bloody Mary",
                type = "Alcoholic",
                category = "Cocktail",
                time = 30,
                imageUrl = null,
                instructions = "Mix all ingredients and serve with ice.",
                glass = "Highball glass",
                iba = "Contemporary Classics",
                ingredients = listOf("Vodka", "Tomato juice", "Lemon juice")
            ),
            onBack = {}
        )
    }
}