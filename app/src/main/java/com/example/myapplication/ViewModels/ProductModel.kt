package com.example.myapplication.ViewModels

import java.io.Serializable

data class ProductModel(
    var id: String = "",
    var title: String = "",
    var image: String = "",
    var ingredients: Map<String, Int> = emptyMap()
): Serializable