package com.google.mlkit.md

import java.io.Serializable

data class BarcodeData(
    val value: String,
    val timestamp: Long
) : Serializable
