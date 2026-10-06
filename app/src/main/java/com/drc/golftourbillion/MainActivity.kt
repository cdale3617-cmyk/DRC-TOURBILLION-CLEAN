package com.drc.golftourbillion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.drc.golftourbillion.ui.theme.DRCGolfTourbillionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPalette.antiGlare = getSharedPreferences("tourbillion", MODE_PRIVATE)
            .getBoolean("antiGlare", false)
        setContent {
            DRCGolfTourbillionTheme {
                TourbillionApp()
            }
        }
    }
}
