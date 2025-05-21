package com.am.drinks

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.am.drinks.drinks.Drink
import com.am.drinks.drinks.DrinkViewModel
import com.am.drinks.ui.theme.DrinksTheme
import com.am.drinks.ui.theme.LocalCustomColors
import com.am.drinks.ui.theme.getCategoryColor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Activity that displays a list of drinks for a selected category
class CategoryDrinkListActivity : ComponentActivity() {
    private val viewModel: DrinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Getting the category and drinks list from the Intent
        val category = intent.getStringExtra("category") ?: ""
        val json = intent.getStringExtra("drinks_json") ?: "[]"
        val drinks: List<Drink> = Gson().fromJson(json, object : TypeToken<List<Drink>>() {}.type)

        setContent {
            DrinksTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CategoryDrinkListScreen(category = category, drinks = drinks, viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDrinkListScreen(category: String, drinks: List<Drink>, viewModel: DrinkViewModel) {
    val context = LocalContext.current
    val customColors = LocalCustomColors.current
    val cardColor = getCategoryColor(category, customColors)

    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val filteredDrinks = remember(query, drinks) {
        if (query.isBlank()) drinks else drinks.filter { drink ->
            val lowerQuery = query.lowercase()
            drink.name.contains(lowerQuery, ignoreCase = true) ||
                    drink.category.contains(lowerQuery, ignoreCase = true) ||
                    drink.type.contains(lowerQuery, ignoreCase = true) ||
                    drink.glass?.contains(lowerQuery, ignoreCase = true) == true ||
                    drink.ingredients.any { (ingredient, _) ->
                        ingredient.contains(lowerQuery, ignoreCase = true)
                    }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Back to Categories") },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) query = ""
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            if (showSearch) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search in this category", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cardColor,
                        cursorColor = cardColor,
                        focusedLabelColor = cardColor
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredDrinks) { drink ->
                    DrinkCard(drink, cardColor) {
                        viewModel.onDrinkViewed(drink)
                        val intent = Intent(context, DrinkDetailActivity::class.java)
                        intent.putExtra("drink", drink)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun DrinkCard(drink: Drink, cardColor: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            AsyncImage(
                model = drink.imageUrl,
                contentDescription = drink.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(24.dp))
            Column {
                Text(
                    text = drink.name,
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White)
                )
                Text(
                    text = drink.type,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )
            }
        }
    }
}
