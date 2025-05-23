# 🍹 Drinks Menu - Cocktail Companion App

A fully-featured Android app built using **Jetpack Compose**, designed to let users browse, search, and learn how to prepare cocktails. The app fetches data from an external API and offers a modern UI with responsive layouts, animations, and a built-in challenge mode timer.

---

## 📱 Features

### Navigation & UI
- **MainActivity** displays a bottom navigation with three tabs: `Home`, `Cocktails`, and `Favourites`.
- Uses `HorizontalPager` with swipe gesture support.
- **DrinkDetailActivity** shows cocktail details and includes a timer challenge feature.
- Responsive layouts for smartphones and tablets (portrait/landscape support).

### Data Source
- Data is fetched from [TheCocktailDB API](https://www.thecocktaildb.com/api.php) using **Retrofit**.
- Drinks and ingredients are dynamically loaded and cached using ViewModel and `StateFlow`.

### Cocktail Details
- Includes:
  - Name, category, type
  - Instructions and ingredients (with amounts)
  - Ingredient icons loaded via Coil
- **Floating Action Button (FAB)** allows sending ingredients via SMS.

### Search Functionality
- Search bar available on the Home screen.
- Filters cocktails based on:
  - Name
  - Category
  - Type
  - Ingredients

### Favourites
- Mark drinks as favourites.
- Favourites list updates on resume.
- Stored and managed via ViewModel.

### Challenge Mode
- Timer functionality with:
  - **Start**, **Stop**, **Interrupt** buttons (icon-based)
  - Countdown and stopwatch modes
  - "Personal best" tracking per drink
- Fully responsive and orientation-safe.

### Design & Theming
- Built with **Material 3**.
- Supports both **light** and **dark** themes.
- Custom color palette via `LocalCustomColors`.
- Consistent use of Material components like Cards, FABs, Snackbars.

---

## 🎞️ Animations & Sensors

- **Loading Screen**:
  - Rising bubbles animated with `ObjectAnimator`
  - Spinner rotating endlessly
  - Bubble direction responds to **accelerometer tilt**
- Animations follow:
  - Material Motion guidelines
  - Physics-based interpolation (`Bounce`, `Linear`)

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose
- **Architecture:** MVVM (ViewModel + StateFlow)
- **Networking:** Retrofit
- **Image Loading:** Coil
- **Navigation:** Intent-based
- **Sensors:** Accelerometer
- **Animation:** ObjectAnimator

---

## 📂 Project Structure

- `MainActivity.kt` – Root activity, holds all composables.
- `DrinkDetailActivity.kt` – Displays drink details and challenge timer.
- `DrinkViewModel.kt` – State management and API handling.
- `ui/theme/` – Custom theming and colors.
- `challengeMode/` – Timer logic and persistent "personal best".
- `drinks/` – Drink data model and API client.

---

## License

This project was developed as part of a university mobile application lab project.
