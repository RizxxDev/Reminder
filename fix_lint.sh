#!/bin/bash
sed -i 's/class MainActivity : ComponentActivity() {/@android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")\nclass MainActivity : ComponentActivity() {/' app/src/main/java/com/example/MainActivity.kt

sed -i 's/if (appWidgetManager.isRequestPinAppWidgetSupported) {/if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O \&\& appWidgetManager.isRequestPinAppWidgetSupported) {/' app/src/main/java/com/example/ui/HomeScreen.kt
