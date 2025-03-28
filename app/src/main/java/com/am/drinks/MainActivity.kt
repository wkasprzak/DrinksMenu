package com.am.drinks

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.am.drinks.data.Drink
import com.am.drinks.data.RetrofitInstance
import com.am.drinks.data.toDrink
import com.am.drinks.ui.theme.DrinksTheme
import com.am.drinks.ui.theme.LocalCustomColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrinksTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MenuScreen()
                }
            }
        }
    }
}

// Getting drink data from API
@Composable
fun rememberDrinkLoaderState(): Triple<List<Drink>, LazyListState, Boolean> {
    val drinks = remember { mutableStateListOf<Drink>() }
    val listState = rememberLazyListState()
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        for (letter in 'a'..'z') {
            try {
                val response = RetrofitInstance.api.getDrinksByLetter(letter)
                response.drinks?.mapNotNull { it.toDrink() }?.let { drinks.addAll(it) } // konwersja API do drinka
            } catch (_: Exception) {
            }
        }
        isLoading.value = false
    }

    return Triple(drinks, listState, isLoading.value) // lista drinków, stan scrollowania oraz sprawdzenie, czy trwa ładowanie
}

@Composable
fun MenuScreen() {
    val customColors = LocalCustomColors.current
    val context = LocalContext.current

    val (drinks, listState, isLoadingDrinks) = rememberDrinkLoaderState()

    // jeśli trwa ładowanie - spinner i komunikat
    if (drinks.isEmpty() && isLoadingDrinks) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Drink Menu🍹",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Cursive,
                    color = customColors.plum,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Fetching your favorite cocktails... 🍸\nPlease don't shake the phone!",
                    fontSize = 16.sp,
                    color = customColors.plum,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
        return
    }

    // wyświetlenie tytułu oraz listy drinków
    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "Drink Menu🍹",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Cursive,
                color = customColors.plum,
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )

            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                items(drinks) { drink ->
                    DrinkCard(drink = drink, onClick = {
                        val intent = Intent(context, DrinkDetailActivity::class.java)
                        intent.putExtra("drink", drink) // przekazanie intencji do podglądu drinka
                        context.startActivity(intent)
                    })
                }
            }
        }
    }
}

@Composable
fun DrinkCard(drink: Drink, onClick: () -> Unit) {
    val customColors = LocalCustomColors.current

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

    Card(
        onClick = onClick,
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = drink.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = drink.category, fontSize = 14.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${drink.time} min", color = Color.Black)
                Text(text = drink.type, color = Color.Black)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMenuScreen() {
    DrinksTheme {
        MenuScreen()
    }
}
