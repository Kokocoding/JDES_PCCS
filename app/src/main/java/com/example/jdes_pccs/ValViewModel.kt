package com.example.jdes_pccs

import androidx.lifecycle.ViewModel

class ValViewModel : ViewModel() {
    val RelaybtnBool = BooleanArray(17) { false }
    val MachineBtnBool = BooleanArray(1) { false }
    val MuteBool = BooleanArray(5) { false }
}