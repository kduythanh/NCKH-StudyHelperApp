package com.example.nlcs.data.dao

import android.content.Context
import android.util.Log
import com.example.nlcs.data.model.Card
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class CardDAO(quizActivity: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val cardsCollection = db.collection("cards")

    // Insert card
    fun insertCard(card: Card): Task<Void>? {
        val cardMap = hashMapOf(
            "id" to card.getId(),
            "front" to card.getFront(),
            "back" to card.getBack(),
            "status" to card.getStatus(),
            "is_learned" to card.getIsLearned(),
            "flashcard_id" to card.getFlashcard_id(),
            "created_at" to card.getCreated_at(),
            "updated_at" to card.getUpdated_at()
        )

        return card.getId()?.let {
            cardsCollection.document(it).set(cardMap)
                .addOnSuccessListener {
                    Log.d("CardDAO", "Card successfully added!")
                }
                .addOnFailureListener { e ->
                    Log.e("CardDAO", "Error adding card", e)
                }
        }
    }

    // Count cards by flashcard_id
    fun countCardByFlashCardId(flashcard_id: String): Task<QuerySnapshot> {
        return cardsCollection.whereEqualTo("flashcard_id", flashcard_id).get()
            .addOnSuccessListener { querySnapshot ->
                val count = querySnapshot.size()
                Log.d("CardDAO", "Number of cards: $count")
            }
            .addOnFailureListener { e ->
                Log.e("CardDAO", "Error counting cards", e)
            }
    }

    // Get cards by flashcard_id
    fun getCardsByFlashCardId(flashcard_id: String): Task<QuerySnapshot> {
        return cardsCollection.whereEqualTo("flashcard_id", flashcard_id).get()
            .addOnSuccessListener { querySnapshot ->
                val cards = querySnapshot.toObjects(Card::class.java)
                Log.d("CardDAO", "Retrieved cards: ${cards.size}")
            }
            .addOnFailureListener { e ->
                Log.e("CardDAO", "Error retrieving cards", e)
            }
    }

    // Get all cards with status = 0 or 2
    fun  getAllCardByStatus(flashcard_id: String): Task<QuerySnapshot> {
        return cardsCollection.whereEqualTo("flashcard_id", flashcard_id)
            .whereNotEqualTo("status", 1).get()
            .addOnSuccessListener { querySnapshot ->
                val cards = querySnapshot.toObjects(Card::class.java)
                Log.d("CardDAO", "Retrieved cards by status: ${cards.size}")
            }
            .addOnFailureListener { e ->
                Log.e("CardDAO", "Error retrieving cards by status", e)
            }
    }

    // Delete card by id
    fun deleteCardById(id: String): Task<Void> {
        return cardsCollection.document(id).delete()
            .addOnSuccessListener {
                Log.d("CardDAO", "Card successfully deleted!")
            }
            .addOnFailureListener { e ->
                Log.e("CardDAO", "Error deleting card", e)
            }
    }

    // Update card status by id
    fun updateCardStatusById(id: String, status: Int): Task<Void> {
        return cardsCollection.document(id).update("status", status)
            .addOnSuccessListener {
                Log.d("CardDAO", "Card status successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.e("CardDAO", "Error updating card status", e)
            }
    }

    // Get card by status
    fun  getCardByStatus(flashcard_id: String, status: Int): Task<QuerySnapshot> {
        return cardsCollection.whereEqualTo("flashcard_id", flashcard_id)
            .whereEqualTo("status", status).get()
            .addOnSuccessListener { querySnapshot ->
                val cards = querySnapshot.toObjects(Card::class.java)
                Log.d("CardDAO", "Retrieved cards with status $status: ${cards.size}")
            }
            .addOnFailureListener { e ->
                Log.e("CardDAO", "Error retrieving cards by status", e)
            }
    }

    // Reset status of cards by flashcard_id to 2
    fun resetStatusCardByFlashCardId(flashcard_id: String): Task<QuerySnapshot> {
        return cardsCollection.whereEqualTo("flashcard_id", flashcard_id).get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach { doc ->
                    cardsCollection.document(doc.id).update("status", 2)
                }
                Log.d("CardDAO", "Card statuses reset successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("CardDAO", "Error resetting card statuses", e)
            }
    }

    // Update is_learned by id
    fun updateIsLearnedCardById(id: String, is_learned: Int): Task<Void> {
        return cardsCollection.document(id).update("is_learned", is_learned)
            .addOnSuccessListener {
                Log.d("CardDAO", "Card is_learned status successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.e("CardDAO", "Error updating card is_learned status", e)
            }
    }

    // Check if card exists by id
    fun checkCardExist(card_id: String): Task<QuerySnapshot> {
        return cardsCollection.whereEqualTo("id", card_id).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    Log.d("CardDAO", "Card exists")
                } else {
                    Log.d("CardDAO", "Card does not exist")
                }
            }
            .addOnFailureListener { e ->
                Log.e("CardDAO", "Error checking card existence", e)
            }
    }

    // Update card by id
    fun  updateCardById(card: Card): Task<Void>? {
        val cardMap = hashMapOf(
            "front" to card.getFront(),
            "back" to card.getBack(),
            "flashcard_id" to card.getFlashcard_id(),
            "created_at" to card.getCreated_at(),
            "updated_at" to card.getUpdated_at()
        )

        return card.getId()?.let {
            cardsCollection.document(it).update(cardMap as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("CardDAO", "Card successfully updated!")
                }
                .addOnFailureListener { e ->
                    Log.e("CardDAO", "Error updating card", e)
                }
        }
    }
    //get card have is_learned = 0
    suspend fun getCardByIsLearned(flashcardId: String, isLearned: Int): List<Card> {
        val db = FirebaseFirestore.getInstance()
        val cards = mutableListOf<Card>()

        return try {
            val querySnapshot = db.collection("cards")
                .whereEqualTo("flashcard_id", flashcardId)
                .whereEqualTo("is_learned", isLearned)
                .get()
                .await()

            for (document in querySnapshot) {
                val card = document.toObject(Card::class.java)
                cards.add(card)
            }
            cards
        } catch (e: Exception) {
            Log.e("CardDAO", "getCardByIsLearned: ${e.message}")
            emptyList()
        }
    }
    //get all cards by flashcard_id

    suspend fun getAllCardByFlashCardId(flashcardId: String): List<Card> {
        val db = FirebaseFirestore.getInstance()
        val cards = mutableListOf<Card>()

        return try {
            val querySnapshot = db.collection("cards")
                .whereEqualTo("flashcard_id", flashcardId)
                .get()
                .await() // Await to get the result synchronously inside a coroutine

            for (document in querySnapshot) {
                val card = document.toObject(Card::class.java)
                cards.add(card)
            }

            cards
        } catch (e: Exception) {
            Log.e("CardDAO", "getAllCardByFlashCardId: ${e.message}")
            emptyList() // Return an empty list in case of an error
        }
    }

}
