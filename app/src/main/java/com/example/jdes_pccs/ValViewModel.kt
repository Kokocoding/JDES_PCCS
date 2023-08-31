package com.example.jdes_pccs

import androidx.lifecycle.ViewModel

class ValViewModel : ViewModel() {
    val btnBool = BooleanArray(17) { false }
    val MuteBool = BooleanArray(5) { false }
}