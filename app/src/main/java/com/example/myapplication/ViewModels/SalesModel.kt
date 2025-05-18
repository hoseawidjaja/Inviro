package com.example.myapplication.ViewModels

import java.io.Serializable

data class SalesModel(
    var date: String = "",
    var menu: Map<String, Int> = emptyMap()
): Serializable