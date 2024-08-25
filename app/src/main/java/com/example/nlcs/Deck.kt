package com.example.nlcs

import java.io.Serializable

data class Deck(
    var deckId: String? = null,
    var uid: String? = null,
    var title: String? = null,
    var author: String? = null,
    var cards: List<List<String>> = listOf()
) : Serializable {
    // Default constructor required for calls to DataSnapshot.getValue(Deck.class)
    constructor() : this(null, null, null, null, listOf())
}