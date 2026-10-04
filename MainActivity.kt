package com.drc.golftourbillion
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.drc.golftourbillion.ui.theme.DRCGolfTourbillionTheme
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{DRCGolfTourbillionTheme{TourbillionApp()}}}
}
