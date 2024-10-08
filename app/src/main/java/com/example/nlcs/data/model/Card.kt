package com.example.nlcs.data.model

import java.util.Objects


class Card {
    @JvmField
    var id: String? = null
    @JvmField
    var front: String? = null
    @JvmField
    var back: String? = null
    @JvmField
    var status: Int = 0
    @JvmField
    var isLearned: Int = 0
    @JvmField
    var flashcard_id: String? = null
    @JvmField
    var created_at: String? = null
    @JvmField
    var updated_at: String? = null

    constructor()

    constructor(
        front: String?,
        back: String?,
        status: Int,
        isLearned: Int,
        flashcard_id: String?,
        created_at: String?,
        updated_at: String?
    ) {
        this.front = front
        this.back = back
        this.status = status
        this.isLearned = isLearned
        this.flashcard_id = flashcard_id
        this.created_at = created_at
        this.updated_at = updated_at
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val card = o as Card
        return (if (id == null) card.id == null else id == card.id) &&
                (if (front == null) card.front == null else front == card.front) &&
                (if (back == null) card.back == null else back == card.back)
    }

    override fun hashCode(): Int {
        return Objects.hash(id, front, back)
    }

    fun SetFront(front: String) {
        this.front = front;
    }

    fun SetBack(back: String) {
        this.back = back;
    }

    fun SetId(id: String) {
        this.id = id;
    }

    fun SetStatus(status: Int) {
        this.status = status;
    }

    fun SetIsLearned(isLearned: Int) {
        this.isLearned = isLearned;
    }

    fun SetFlashcard_id(flashcard_id: String) {
        this.flashcard_id = flashcard_id;
    }

    fun SetCreated_at(created_at: String) {
        this.created_at = created_at;
    }

    fun SetUpdated_at(updated_at: String) {
        this.updated_at = updated_at;
    }

    fun GetFront(): String? {
        return front;
    }

    fun GetBack(): String? {
        return back;
    }

    fun GetId(): String? {
        return id;
    }

    fun GetStatus(): Int {
        return status;
    }

    fun GetIsLearned(): Int {
        return isLearned;
    }

    fun GetFlashcard_id(): String? {
        return flashcard_id;
    }

    fun GetCreated_at(): String? {
        return created_at;
    }

    fun GetUpdated_at(): String? {
        return updated_at;
    }


}
