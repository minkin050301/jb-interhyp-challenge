# Localization Guide - DreamBuilder App

## Overview
The DreamBuilder app now supports **German (Deutsch)** localization in addition to English. The app automatically detects the system language and displays the appropriate translations.

## Supported Languages
- **English (en)** - Default language
- **German (de)** - Deutsche Sprache

## How It Works

### String Resources Location
String resources are located in the `composeResources` directory:

```
composeApp/src/commonMain/composeResources/
├── values/
│   └── strings.xml          # English (default)
└── values-de/
    └── strings.xml          # German
```

### Using Localized Strings in Code

To use localized strings in your Compose UI code, follow these steps:

1. **Import the required dependencies:**
```kotlin
import org.jetbrains.compose.resources.stringResource
import jb_interhyp_challenge.composeapp.generated.resources.Res
import jb_interhyp_challenge.composeapp.generated.resources.*
```

2. **Use `stringResource()` in your Composables:**
```kotlin
@Composable
fun MyScreen() {
    Text(stringResource(Res.string.onboarding_title))
}
```

3. **For strings with parameters (format arguments):**
```kotlin
// String resource: <string name="step_1_of">Step 1 of %1$d</string>
Text(stringResource(Res.string.step_1_of, totalSteps))

// String resource: <string name="hi_user">Hi %1$s!</string>
Text(stringResource(Res.string.hi_user, userName))
```

### Example from OnboardingScreen

**Before (hardcoded):**
```kotlin
Text("Home Savings Setup")
Button(onClick = onSkip) { 
    Text("Skip for now") 
}
```

**After (localized):**
```kotlin
Text(stringResource(Res.string.onboarding_title))
Button(onClick = onSkip) { 
    Text(stringResource(Res.string.skip_for_now)) 
}
```

## Available String Keys

### Onboarding
- `onboarding_title` - Main onboarding title
- `welcome`, `welcome_message` - Welcome screen
- `your_target`, `house`, `apartment`, `size_sqm` - Target property
- `about_you`, `age`, `net_income_per_month` - Personal information
- `your_selfie`, `selfie_message` - Selfie step

### Navigation
- `next`, `previous`, `finish` - Navigation buttons
- `nav_home`, `nav_insights`, `nav_profile`, `nav_settings` - Bottom nav

### Common
- `loading`, `error`, `retry`, `ok`, `yes`, `no`
- `save`, `delete`, `edit`, `close`, `cancel`

### Dashboard
- `dashboard_title`, `savings_progress`, `your_dream_home`
- `target_price`, `current_savings`, `monthly_savings_goal`

### Settings
- `settings_title`, `theme`, `language`, `notifications`
- `theme_light`, `theme_dark`, `theme_system`

See `composeApp/src/commonMain/composeResources/values/strings.xml` for the complete list.

## Adding a New Language

To add support for another language (e.g., French):

1. **Create a new directory** for the language:
   ```bash
   mkdir -p composeApp/src/commonMain/composeResources/values-fr
   ```

2. **Copy the English strings.xml** to the new directory:
   ```bash
   cp composeApp/src/commonMain/composeResources/values/strings.xml \
      composeApp/src/commonMain/composeResources/values-fr/strings.xml
   ```

3. **Translate all string values** in the new `values-fr/strings.xml` file:
   ```xml
   <string name="onboarding_title">Configuration de l'épargne immobilière</string>
   <string name="welcome">Bienvenue</string>
   <!-- ... translate all strings ... -->
   ```

4. **Rebuild the project** - Compose Multiplatform will automatically generate the resource accessors.

5. **Test** - Change your device/browser language to French to see the translations.

## Language Codes Reference

Common language codes for directory naming:
- `values` - Default (English)
- `values-de` - German (Deutsch)
- `values-fr` - French (Français)
- `values-es` - Spanish (Español)
- `values-it` - Italian (Italiano)
- `values-pt` - Portuguese (Português)
- `values-nl` - Dutch (Nederlands)
- `values-pl` - Polish (Polski)
- `values-ja` - Japanese (日本語)
- `values-zh` - Chinese (中文)

## Testing Localization

### On Android
1. Go to **Settings → System → Languages & input → Languages**
2. Add German (Deutsch) and move it to the top
3. Relaunch the app

### On iOS Simulator
1. Go to **Settings → General → Language & Region**
2. Add German and set it as primary
3. Relaunch the app

### On Web/Desktop
The app will use your browser/system language settings automatically.

### Manual Testing
To force a specific locale during development, you can modify the system locale or use runtime locale switching (requires additional implementation).

## Best Practices

1. **Always use `stringResource()`** instead of hardcoded strings in UI code
2. **Keep string keys descriptive** and organized by feature
3. **Use positional format arguments** (`%1$s`, `%1$d`, `%2$s`, etc.) for dynamic content
4. **Escape special characters** properly in XML:
   - `&` → `&amp;`
   - `<` → `&lt;`
   - `>` → `&gt;`
   - `'` → `\'` (in strings)
   - `"` → `\"`
5. **Test all languages** before releasing
6. **Keep translations consistent** across all language files

## Adding More Strings

When adding new features, always add corresponding strings to **all** language files:

1. Add to `values/strings.xml` (English)
2. Add to `values-de/strings.xml` (German)
3. Add to any other language files you support

This ensures consistency and prevents missing translations.

## Current Implementation Status

✅ **Implemented:**
- English and German string resources
- OnboardingScreen partially updated with localized strings (demonstration)
- Infrastructure ready for all screens

⏳ **To Do:**
- Update remaining UI screens (Dashboard, Insights, Profile, Settings) to use `stringResource()`
- Add more languages as needed
- Implement runtime language switching in Settings

## Resources

- [Compose Multiplatform Resources Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html)
- [Android String Resources](https://developer.android.com/guide/topics/resources/string-resource)
- [ISO 639-1 Language Codes](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes)
