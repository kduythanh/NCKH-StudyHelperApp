package com.example.nlcs.data.model

import java.util.UUID

class FlashCard {
    fun setIs_public(is_public: Int) {
        this.is_public = is_public;

    }

    var id: String? = null
    var name: String? = null
    var description: String? = null
    var is_public: Int = 0
    var created_at: String? = null
    var updated_at: String? = null

    constructor()

    constructor(
        id: String?,
        name: String?,
        description: String?,
        user_id: String?,
        is_public: Int,
        created_at: String?,
        updated_at: String?
    ) {
        this.id = UUID.randomUUID().toString()
        this.name = name
        this.description = description
        this.is_public = is_public
        this.created_at = created_at
        this.updated_at = updated_at
    }
}
