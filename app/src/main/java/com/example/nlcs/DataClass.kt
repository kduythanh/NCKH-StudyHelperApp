package com.example.nlcs

data class MindMap(
    var id: String? = null,
    var mindMapID: String? = null,
    var title: String? = null,
    var date: Long = System.currentTimeMillis(),
    val userID: String? = null
)

