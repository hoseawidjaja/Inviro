package com.example.myapplication.ViewModels

import java.io.Serializable

data class StockModel(
    var id: String = "",
    var title: String = "",
    var stock: Int = 0,
    var unit: String = "pcs",
    var image: String = ""
): Serializable