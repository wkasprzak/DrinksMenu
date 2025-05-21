package com.am.drinks

import com.am.drinks.challengeMode.ChallengeViewModel
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.am.drinks.challengeMode.ChallengeComponent
import com.am.drinks.drinks.Drink
import com.am.drinks.drinks.DrinkViewModel
import com.am.drinks.ui.theme.DrinksTheme
import com.am.drinks.ui.theme.LocalCustomColors
import com.am.drinks.ui.theme.getCategoryColor
import androidx.core.net.toUri

class DrinkDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val drink = intent.getSerializableExtra("drink") as? Drink ?: return finish()

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[DrinkViewModel::class.java]

        // save as last viewed
        viewModel.onDrinkViewed(drink)

        setContent {
            DrinksTheme {
                DrinkDetailScreen(drink = drink) {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkDetailScreen(drink: Drink, onBack: () -> Unit) {
    val customColors = LocalCustomColors.current
    val isPortrait = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
    var showChallenge by rememberSaveable { mutableStateOf(false) }

    val backgroundColor = getCategoryColor(drink.category, customColors)

    val context = LocalContext.current
    SideEffect {
        val activity = (context as? Activity) ?: return@SideEffect
        activity.window.statusBarColor = backgroundColor.toArgb()
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel = viewModel<DrinkViewModel>()
    val isFav by viewModel.favourites.collectAsState()
    val isFavourite = isFav.any { it.id == drink.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(drink.name, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.toggleFavourite(drink) }) {
                        Icon(
                            imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favourite",
                            tint = Color.White
                        )
                    }
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val message = buildString {
                        append("Ingredients for ${drink.name}:\n")
                        drink.ingredients.forEach {
                            append("- ${it.first}")
                            it.second?.let { amt -> append(" ($amt)") }
                            append("\n")
                        }
                    }

                    val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = "sms:".toUri()
                        putExtra("sms_body", message.trim())
                    }

                    context.startActivity(smsIntent)
                },
                containerColor = backgroundColor
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Share Ingredients", tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isPortrait) {
            PortraitContent(drink, backgroundColor, showChallenge, { showChallenge = it }, innerPadding)
        } else {
            LandscapeContent(drink, backgroundColor, showChallenge, { showChallenge = it }, innerPadding)
        }
    }

}

@Composable
fun LandscapeContent(drink: Drink, backgroundColor: Color, showChallenge: Boolean, onToggleChallenge: (Boolean) -> Unit, innerPadding: PaddingValues) {
    val context = LocalContext.current
    val challengeViewModel: ChallengeViewModel = viewModel()
    val pb = challengeViewModel.getPersonalBest(context, drink.id)

    Row(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DrinkImage(drink, size = 220.dp)
            Spacer(Modifier.height(24.dp))
            Text("Challenge Mode", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = backgroundColor)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Personal Best:", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                Text(pb?.let { "${it}s" } ?: "∞", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = backgroundColor)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onToggleChallenge(true) },
                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start Challenge", tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Start Challenge", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        // Challenge section
        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(32.dp))
            InfoBlock("Type", drink.type, backgroundColor)
            InfoBlock("Category", drink.category, backgroundColor)
            drink.iba?.let { InfoBlock("IBA", it, backgroundColor) }
            drink.glass?.let { InfoBlock("Glass", it, backgroundColor) }

            drink.instructions?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(24.dp))
                Text("Instructions", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = backgroundColor)
                Spacer(Modifier.height(8.dp))
                Text(it, fontSize = 18.sp, color = Color.Black, textAlign = TextAlign.Start)
            }

            Spacer(Modifier.height(24.dp))
            Text("Ingredients", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = backgroundColor)
            IngredientsGrid(drink.ingredients, backgroundColor)
        }
    }

    if (showChallenge) {
        val challengeViewModel = viewModel<ChallengeViewModel>()
        ChallengeComponent(drink.id, { onToggleChallenge(false) }, backgroundColor, challengeViewModel)
    }
}

@Composable
fun PortraitContent(drink: Drink, backgroundColor: Color, showChallenge: Boolean, onToggleChallenge: (Boolean) -> Unit, innerPadding: PaddingValues) {
    val context = LocalContext.current
    val challengeViewModel: ChallengeViewModel = viewModel()
    val pb = challengeViewModel.getPersonalBest(context, drink.id)

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.weight(1f).padding(top = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                InfoBlock("Type", drink.type, backgroundColor)
                InfoBlock("Category", drink.category, backgroundColor)
                drink.iba?.let { InfoBlock("IBA", it, backgroundColor) }
                drink.glass?.let { InfoBlock("Glass", it, backgroundColor) }
            }

            Box(
                modifier = Modifier.width(220.dp).offset(x = 40.dp).padding(top = 32.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                DrinkImage(drink, size = 220.dp)
            }
        }

        Spacer(Modifier.height(32.dp))

        // Challenge section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Challenge Mode", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = backgroundColor)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Personal Best:", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                Text(pb?.let { "${it}s" } ?: "∞", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = backgroundColor)
            }
            Button(
                onClick = { onToggleChallenge(true) },
                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start Challenge", tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Start Challenge", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(32.dp))

        drink.instructions?.takeIf { it.isNotBlank() }?.let {
            Text("Instructions", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = backgroundColor)
            Spacer(Modifier.height(8.dp))
            Text(it, fontSize = 18.sp, color = Color.Black, textAlign = TextAlign.Start)
            Spacer(Modifier.height(24.dp))
        }

        // Ingredients section
        Text("Ingredients", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = backgroundColor)
        Spacer(Modifier.height(8.dp))
        IngredientsGrid(drink.ingredients, backgroundColor)

        if (showChallenge) {
            ChallengeComponent(drinkId = drink.id, onDismiss = { onToggleChallenge(false) }, backgroundColor = backgroundColor)
        }
    }
}

@Composable
fun InfoBlock(label: String, value: String, valueColor: Color) {
    Column {
        Text(label, fontSize = 18.sp, color = Color.Black)
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun IngredientsGrid(ingredients: List<Pair<String, String?>>, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ingredients.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { ingredient ->
                    IngredientItem(
                        ingredient = ingredient,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun IngredientItem(ingredient: Pair<String, String?>, color: Color, modifier: Modifier = Modifier) {
    val (name, amount) = ingredient
    val context = LocalContext.current
    val imageUrl = "https://www.thecocktaildb.com/images/ingredients/${name}-Small.png"
    var imageExists by remember { mutableStateOf(true) }

    LaunchedEffect(imageUrl) {
        imageExists = try {
            val request = coil.request.ImageRequest.Builder(context).data(imageUrl).allowHardware(false).build()
            coil.ImageLoader(context).execute(request).drawable != null
        } catch (_: Exception) { false }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(64.dp),
            tonalElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (imageExists) {
                    AsyncImage(model = imageUrl, contentDescription = name, modifier = Modifier.size(40.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
        amount?.let {
            Text(
                it,
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun DrinkImage(drink: Drink, size: Dp = 160.dp) {
    val customColors = LocalCustomColors.current
    Box(
        modifier = Modifier.size(size).clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!drink.imageUrl.isNullOrEmpty()) {
            AsyncImage(model = drink.imageUrl, contentDescription = drink.name, modifier = Modifier.size(size).clip(CircleShape))
        } else {
            Icon(Icons.Default.Favorite, contentDescription = "Drink image", modifier = Modifier.size(size / 2), tint = customColors.plum.copy(alpha = 0.5f))
        }
    }
}