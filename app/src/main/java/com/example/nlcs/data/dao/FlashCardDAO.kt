package com.example.nlcs.data.dao

import android.util.Log
import com.example.nlcs.data.model.FlashCard
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.content.Context

class FlashCardDAO (CreateFlashcardActivity: Context){
    private val db = FirebaseFirestore.getInstance()
    private val flashcardCollection = db.collection("flashcards")



    // Insert flashcard
    suspend fun insertFlashCard(flashcard: FlashCard): Boolean {
        return try {
            flashcard.id?.let { flashcardCollection.document(it).set(flashcard).await() }
            true
        } catch (e: Exception) {
            Log.e("FlashCardDAO", "insertFlashCard: ${e.message}")
            false
        }
    }

    // Delete flashcard and its cards
    suspend fun deleteFlashcardAndCards(flashcardId: String): Boolean {
        return try {
            val batch = db.batch()

            // Delete all cards associated with the flashcard
            val cards = db.collection("cards")
                .whereEqualTo("flashcard_id", flashcardId)
                .get().await()

            for (document in cards) {
                batch.delete(document.reference)
            }

            // Delete the flashcard itself
            val flashcardRef = flashcardCollection.document(flashcardId)
            batch.delete(flashcardRef)

            // Commit the batch operation
            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.e("FlashCardDAO", "deleteFlashcardAndCards: ${e.message}")
            false
        }
    }

    // Get flashcard by ID
    suspend fun getFlashCardById(id: String): FlashCard? {
        return try {
            val documentSnapshot = flashcardCollection.document(id).get().await()
            documentSnapshot.toObject(FlashCard::class.java)
        } catch (e: Exception) {
            Log.e("FlashCardDAO", "getFlashCardById: ${e.message}")
            null
        }
    }

    // Get all flashcards
    suspend fun getAllFlashCards(): List<FlashCard> {
        return try {
            val querySnapshot = flashcardCollection.orderBy("created_at").get().await()
            querySnapshot.toObjects(FlashCard::class.java)
        } catch (e: Exception) {
            Log.e("FlashCardDAO", "getAllFlashCards: ${e.message}")
            emptyList()
        }
    }

    // Update flashcard
    suspend fun updateFlashCard(flashcard: FlashCard): Boolean {
        return try {
            flashcard.id?.let { flashcardCollection.document(it).set(flashcard).await() }
            true
        } catch (e: Exception) {
            Log.e("FlashCardDAO", "updateFlashCard: ${e.message}")
            false
        }
    }

    // Get all public flashcards
    suspend fun getAllFlashCardPublic(): List<FlashCard> {
        return try {
            val querySnapshot = flashcardCollection
                .whereEqualTo("is_public", true)
                .orderBy("created_at")
                .get().await()
            querySnapshot.toObjects(FlashCard::class.java)
        } catch (e: Exception) {
            Log.e("FlashCardDAO", "getAllPublicFlashCards: ${e.message}")
            emptyList()
        }
    }
}
