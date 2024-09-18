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

    fun setFront(front: String) {
        this.front = front;
    }

    fun setBack(back: String) {
        this.back = back;
    }

    fun setId(id: String) {
        this.id = id;
    }

    fun setStatus(status: Int) {
        this.status = status;
    }

    fun setIsLearned(isLearned: Int) {
        this.isLearned = isLearned;
    }

    fun setFlashcard_id(flashcard_id: String) {
        this.flashcard_id = flashcard_id;
    }

    fun setCreated_at(created_at: String) {
        this.created_at = created_at;
    }

    fun setUpdated_at(updated_at: String) {
        this.updated_at = updated_at;
    }

    fun getFront(): String? {
        return front;
    }

    fun getBack(): String? {
        return back;
    }

    fun getId(): String? {
        return id;
    }

    fun getStatus(): Int {
        return status;
    }

    fun getIsLearned(): Int {
        return isLearned;
    }

    fun getFlashcard_id(): String? {
        return flashcard_id;
    }

    fun getCreated_at(): String? {
        return created_at;
    }

    fun getUpdated_at(): String? {
        return updated_at;
    }


}
