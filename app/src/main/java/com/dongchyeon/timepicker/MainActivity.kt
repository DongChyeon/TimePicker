package com.dongchyeon.timepicker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dongchyeon.timepicker.ui.theme.TimePickerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimePickerTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        TimePicker(
                            timeFormat = TimeFormat.TWENTY_FOUR_HOUR
                        ) { newTime ->
                            Log.d("TimePicker", "Selected Time: $newTime")
                        }

                        TimePicker(
                            selector = TimePickerDefaults.pickerSelector(
                                color = Color.Gray.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.Gray)
                            )
                        ) { newTime ->
                            Log.d("TimePicker", "Selected Time: $newTime")
                        }
                    }
                }
            }
        }
    }
}
