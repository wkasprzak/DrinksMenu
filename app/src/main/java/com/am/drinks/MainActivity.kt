package com.am.drinks

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.am.drinks.drinks.Drink
import com.am.drinks.drinks.DrinkViewModel
import com.am.drinks.ui.theme.DrinksTheme
import com.am.drinks.ui.theme.LocalCustomColors
import com.am.drinks.ui.theme.getCategoryColor
import com.google.gson.Gson
import java.time.LocalDate
import kotlin.jvm.java
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable fullscreen mode for landscape orientation
        if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        enableEdgeToEdge()
        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[DrinkViewModel::class.java]

        setContent {
            DrinksTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Wrap content with loading animation while data is being fetched
                    LoadingAnimationWrapper(viewModel) {
                        MainContent(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingAnimationWrapper(viewModel: DrinkViewModel, content: @Composable () -> Unit) {
    val drinks by viewModel.drinks.collectAsState()
    val hasError by viewModel.hasError.collectAsState()
    val isLoading = drinks.isEmpty() && !hasError
    val customColors = LocalCustomColors.current

    if (isLoading) {
        // Get accelerometer sensor
        val context = LocalContext.current
        val sensorManager = remember {
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }
        val accelerometer = remember {
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
        var xTilt by remember { mutableStateOf(0f) }

        // Register sensor listener
        DisposableEffect(Unit) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    xTilt = event?.values?.get(0) ?: 0f
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        }

        // Bubbles + rotating icon + text
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    val screenWidth = ctx.resources.displayMetrics.widthPixels
                    val screenHeight = ctx.resources.displayMetrics.heightPixels

                    val container = FrameLayout(ctx)

                    // Create 50 animated bubbles
                    repeat(50) {
                        val size = Random.nextInt(20, 50)
                        val bubble = View(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(size, size).apply {
                                leftMargin = Random.nextInt(0, screenWidth - size)
                                topMargin = Random.nextInt(screenHeight, screenHeight * 2)
                            }
                            background = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setColor(customColors.plum.toArgb())
                                alpha = (60..160).random()
                            }
                        }

                        container.addView(bubble)

                        // Vertical animation
                        ObjectAnimator.ofFloat(
                            bubble,
                            "translationY",
                            0f,
                            -screenHeight.toFloat() - size
                        ).apply {
                            duration = (5000L..9000L).random()
                            interpolator = BounceInterpolator() // Physical animation
                            repeatCount = ObjectAnimator.INFINITE
                            start()
                        }

                        // Horizontal wobble animation
                        ObjectAnimator.ofFloat(
                            bubble,
                            "translationX",
                            (-50..50).random().toFloat() + xTilt * 10 // Sensor
                        ).apply {
                            duration = (4000L..8000L).random()
                            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                            repeatCount = ObjectAnimator.INFINITE
                            repeatMode = ObjectAnimator.REVERSE
                            start()
                        }
                    }

                    container
                },
                modifier = Modifier.fillMaxSize()
            )

            // Spinner + loading text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AndroidView(
                    factory = { ctx ->
                        val imageView = ImageView(ctx).apply {
                            setImageResource(R.drawable.drink_spinner)
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            setColorFilter(customColors.plum.toArgb())
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        }

                        // Rotation animation for spinner
                        ObjectAnimator.ofFloat(imageView, View.ROTATION, 0f, 360f).apply {
                            duration = 1000
                            repeatCount = ObjectAnimator.INFINITE
                            interpolator = LinearInterpolator()
                            start()
                        }

                        imageView
                    },
                    modifier = Modifier.size(80.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Fetching your favorite cocktails... 🍹",
                    style = MaterialTheme.typography.bodyLarge,
                    color = customColors.plum
                )
            }
        }
    } else {
        // When loaded, show app content
        content()
    }
}

fun View.findActivity(): Activity? {
    var context = this.context
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun MainContent(viewModel: DrinkViewModel) {
    val isTablet = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val drinks by viewModel.drinks.collectAsState()
    val hasError by viewModel.hasError.collectAsState()

    if (drinks.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            LoadingState(hasError = hasError, onRetry = { viewModel.retryLoading() })
        }
        return
    }

    if (isTablet) {
        TabletLayout(
            drinks = drinks,
            hasError = hasError,
            onRetry = { viewModel.retryLoading() },
            viewModel
        )
    } else {
        PhoneLayout(
            drinks = drinks,
            hasError = hasError,
            onRetry = { viewModel.retryLoading() },
            viewModel
        )
    }
}

@Composable
fun PagerLayout(pagerState: PagerState, viewModel: DrinkViewModel, drinks: List<Drink>, hasError: Boolean, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) { page ->
        when (page) {
            0 -> HomeScreen(viewModel)
            1 -> CocktailsPhoneScreen(drinks, hasError, onRetry, viewModel)
            2 -> FavouritesScreen(viewModel)
        }
    }
}

@Composable
fun ObserveResumeForFavouritesRefresh(currentPage: Int, lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current, onRefresh: () -> Unit) {
    DisposableEffect(lifecycleOwner, currentPage) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && currentPage == 2) {
                onRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(currentPage) {
        if (currentPage == 2) {
            onRefresh()
        }
    }
}

@Composable
fun TabletLayout(drinks: List<Drink>, hasError: Boolean, onRetry: () -> Unit, viewModel: DrinkViewModel) {
    val customColors = LocalCustomColors.current

    // Pager for switching screens
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isLargeTablet = screenWidthDp >= 1000

    // Refresh favourites on resume
    ObserveResumeForFavouritesRefresh(
        currentPage = pagerState.currentPage,
        onRefresh = { viewModel.refreshFavourites() }
    )

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(if (isLargeTablet) 200.dp else 96.dp)
                .background(customColors.plum)
        ) {
            val iconSize = if (isLargeTablet) 36.dp else 24.dp
            val textSize = if (isLargeTablet) 18.sp else 12.sp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                listOf("Home", "Cocktails", "Favourites").forEachIndexed { index, label ->
                    val icon = when (index) {
                        0 -> Icons.Default.Home
                        1 -> Icons.Default.Search
                        else -> Icons.Default.Favorite
                    }

                    NavigationRailItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = Color.White,
                                modifier = Modifier.size(iconSize)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = textSize,
                                color = Color.White
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White,
                            unselectedTextColor = Color.White,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }

        Divider(
            color = Color.LightGray,
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
        )

        PagerLayout(pagerState, viewModel, drinks, hasError, onRetry, Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
fun PhoneLayout(drinks: List<Drink>, hasError: Boolean, onRetry: () -> Unit, viewModel: DrinkViewModel) {
    val view = LocalView.current
    val customColors = LocalCustomColors.current

    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    // Set navigation bar color
    SideEffect {
        view.findActivity()?.window?.navigationBarColor = customColors.plum.toArgb()
    }

    // Refresh favourites on resume
    ObserveResumeForFavouritesRefresh(
        currentPage = pagerState.currentPage,
        onRefresh = { viewModel.refreshFavourites() }
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = customColors.plum) {
                listOf("Home", "Cocktails", "Favourites").forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            val icon = when (index) {
                                0 -> Icons.Default.Home
                                1 -> Icons.Default.Search
                                else -> Icons.Default.Favorite
                            }
                            Icon(icon, contentDescription = label)
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.White,
                            indicatorColor = customColors.plum
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        PagerLayout(pagerState = pagerState, viewModel = viewModel, drinks = drinks, hasError = hasError, onRetry = onRetry, modifier = Modifier.padding(innerPadding).fillMaxSize())
    }
}

@Composable
fun ResetChallengeButton() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = LocalCustomColors.current.plum)
        ) {
            Text("Reset Challenge Results", color = Color.White)
        }

        // If user clicks the button, show a confirmation dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete all challenge results?") },
                confirmButton = {
                    TextButton(onClick = {
                        context.getSharedPreferences("challenge_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                        showDialog = false
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("No")
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(viewModel: DrinkViewModel) {
    val drinks by viewModel.drinks.collectAsState()
    val recentlyViewed by viewModel.recentlyViewed.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val customColors = LocalCustomColors.current
    var query by rememberSaveable { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    // Calculate a "drink of the day" based on current day of year
    val dailyDrink = remember(drinks) {
        if (drinks.isNotEmpty()) {
            val seed = LocalDate.now().dayOfYear
            drinks[seed % drinks.size]
        } else null
    }

    // Refresh recently viewed drinks when the screen resumes
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentViewModel by rememberUpdatedState(viewModel)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentViewModel.refreshRecentlyViewed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Filter drinks based on user query
    val suggestions = remember(query, drinks) {
        if (query.isBlank()) emptyList()
        else drinks.filter { drink ->
            val lowerQuery = query.lowercase()
            drink.name.contains(lowerQuery, ignoreCase = true) ||
                    drink.category.contains(lowerQuery, ignoreCase = true) ||
                    drink.type.contains(lowerQuery, ignoreCase = true) ||
                    drink.ingredients.any { (ingredient, _) ->
                        ingredient.contains(lowerQuery, ignoreCase = true)
                    }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Welcome to the",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Cursive,
                    color = customColors.plum
                )
                Text(
                    text = "Drink Menu App!🍹",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Cursive,
                    color = customColors.plum
                )
            }

            // Search bar
            item {
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = {
                        Text(
                            "Search by name, ingredient or category",
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customColors.plum,
                        cursorColor = customColors.plum,
                        focusedLabelColor = customColors.plum
                    )
                )
            }

            // Search suggestions
            items(suggestions.take(5)) { drink ->
                TextButton(
                    onClick = {
                        viewModel.onDrinkViewed(drink)
                        val intent = Intent(context, DrinkDetailActivity::class.java)
                        intent.putExtra("drink", drink)
                        context.startActivity(intent)
                        query = ""
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${drink.name} – ${drink.category}")
                }
            }

            // Daily featured drink
            item {
                Spacer(Modifier.height(24.dp))
                dailyDrink?.let {
                    Column {
                        Text(
                            text = "🍸 Drink of the day ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = customColors.plum
                        )
                        Spacer(Modifier.height(16.dp))
                        DrinkCard(drink = it) {
                            viewModel.onDrinkViewed(it)
                            val intent = Intent(context, DrinkDetailActivity::class.java)
                            intent.putExtra("drink", it)
                            context.startActivity(intent)
                        }
                    }
                }
            }

            // Recently viewed section
            item {
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "🕒 Recently Viewed ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = customColors.plum
                    )
                    Spacer(Modifier.height(16.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(recentlyViewed.take(10)) { drink ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(140.dp)
                                    .clickable {
                                        viewModel.onDrinkViewed(drink)
                                        val intent = Intent(context, DrinkDetailActivity::class.java)
                                        intent.putExtra("drink", drink)
                                        context.startActivity(intent)
                                    }
                            ) {
                                AsyncImage(
                                    model = drink.imageUrl,
                                    contentDescription = drink.name,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = drink.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 10,
                                    color = customColors.plum,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                ResetChallengeButton();
            }
        }
    }
}

@Composable
fun CocktailsPhoneScreen(drinks: List<Drink>, hasError: Boolean, onRetry: () -> Unit, viewModel: DrinkViewModel) {
    val context = LocalContext.current
    val customColors = LocalCustomColors.current

    val categories = drinks.groupBy { it.category }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 16.dp)) {

        AppTitle("Categories 🍸", Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.height(16.dp))

        // Grid of drink categories (2 columns)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
            // For each category, create a card showing its first drink
            items(categories.entries.toList()) { (category, categoryDrinks) ->
                val previewDrink = categoryDrinks.firstOrNull()
                if (previewDrink != null) {
                    Card(
                        onClick = {
                            // Serialize drinks list to JSON and open new activity
                            val gson = Gson()
                            val drinkListJson = gson.toJson(categoryDrinks)
                            val intent = Intent(context, CategoryDrinkListActivity::class.java)
                            intent.putExtra("category", category)
                            intent.putExtra("drinks_json", drinkListJson)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = getCategoryColor(category, customColors))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                model = previewDrink.imageUrl,
                                contentDescription = category,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = category,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavouritesScreen(viewModel: DrinkViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentViewModel by rememberUpdatedState(viewModel)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentViewModel.refreshFavourites()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val favourites by viewModel.favourites.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AppTitle("Favourites ❤️")
        }

        Spacer(Modifier.height(16.dp))

        if (favourites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text("No favourites yet ❤️", fontSize = 20.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favourites) { drink ->
                    DrinkCard(drink = drink, onClick = {
                        viewModel.onDrinkViewed(drink)
                        val intent = Intent(context, DrinkDetailActivity::class.java)
                        intent.putExtra("drink", drink)
                        context.startActivity(intent)
                    })
                }
            }
        }
    }
}

@Composable
fun LoadingState(hasError: Boolean, onRetry: () -> Unit) {
    if (hasError) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Failed to load drinks.\nPress retry to try again.",
                fontSize = 20.sp,
                color = LocalCustomColors.current.plum,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Fetching your favorite cocktails... 🍸",
                fontSize = 16.sp,
                color = LocalCustomColors.current.plum,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun AppTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 48.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Cursive,
        color = LocalCustomColors.current.plum,
        modifier = modifier
    )
}

@Composable
fun DrinkCard(drink: Drink, onClick: () -> Unit) {
    val customColors = LocalCustomColors.current
    val backgroundColor = getCategoryColor(drink.category, customColors)

    Card(
        onClick = onClick,
        modifier = Modifier.padding(12.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.padding(start = 12.dp, end = 8.dp).size(80.dp), contentAlignment = Alignment.Center) {
                if (!drink.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = drink.imageUrl,
                        contentDescription = drink.name,
                        modifier = Modifier.size(80.dp).clip(CircleShape)
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(drink.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(drink.category, fontSize = 14.sp, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(drink.type, color = Color.White)
            }
        }
    }
}
