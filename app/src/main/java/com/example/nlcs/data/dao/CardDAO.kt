package com.example.nlcs.data.dao

import android.content.Context
import android.util.Log
import com.example.nlcs.data.model.Card
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class CardDAO(context: Context) {

    private val db = FirebaseFirestore.getInstance()
    //private val cardsCollection = db.collection("cards")

    // Insert card
    suspend fun insertCard(card: Card): Long {
        val db = FirebaseFirestore.getInstance()

        val cardData = hashMapOf(
            "id" to card.GetId(),
            "front" to card.GetFront(),
            "back" to card.GetBack(),
            "status" to card.GetStatus(),
            "is_learned" to card.GetIsLearned(),
            "flashcard_id" to card.GetFlashcard_id(),
            "created_at" to card.GetCreated_at(),
            "updated_at" to card.GetUpdated_at()
        )

        return try {
            // Use the await() function from the Firestore Kotlin extension to await the completion of the operation
            db.collection("cards").add(cardData).await()
            Log.d("CardDAO", "Card successfully added.")
            1L // Return 1L on success
        } catch (e: Exception) {
            Log.e("CardDAO", "Error adding card: ${e.message}", e)
            0L // Return 0L on failure
        }
    }


    // Count cards by flashcard_id
    suspend fun countCardByFlashCardId(flashcardId: String): Int {
        val db = FirebaseFirestore.getInstance()

        return try {
            // Query Firestore to count the number of cards with the given flashcard ID
            val querySnapshot = db.collection("cards")
                .whereEqualTo("flashcard_id", flashcardId)
                .get()
                .await()

            // Return the number of documents (cards) found
            querySnapshot.size()
        } catch (e: Exception) {
            Log.e("CardDAO", "countCardByFlashCardId: ${e.message}", e)
            0 // Return 0 if an error occurs
        }
    }


    // Get cards by flashcard_id
    suspend fun getCardsByFlashCardId(flashcard_id: String): ArrayList<Card> {
        val db = FirebaseFirestore.getInstance()
        val cards = ArrayList<Card>()

        return try {
            // Query Firestore to get all cards with the given flashcard ID
            val querySnapshot = db.collection("cards")
                .whereEqualTo("flashcard_id", flashcard_id)
                .get()
                .await()

            // Convert each document in the result to a Card object
            for (document in querySnapshot.documents) {
                val card = Card().apply {
                    SetId(document.id)
                    SetFront(document.getString("front") ?: "")
                    SetBack(document.getString("back") ?: "")
                    SetFlashcard_id(document.getString("flashcard_id") ?: "")
                    SetStatus(document.getLong("status")?.toInt() ?: 0)
                    SetIsLearned(document.getLong("is_learned")?.toInt() ?: 0)
                    SetCreated_at(document.getString("created_at") ?: "")
                    SetUpdated_at(document.getString("updated_at") ?: "")
                }
                cards.add(card)
            }
            Log.d("CardDAO", "Number of cards fetched: ${cards.size}")
            cards
        } catch (e: Exception) {
            Log.e("CardDAO", "getCardsByFlashCardId: ${e.message}", e)

            ArrayList() // Return an empty list on failure
        }
    }


    // Get all cards with status != 1
    fun getAllCardByStatus(flashcardId: String, callback: (ArrayList<Card>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("cards")
            .whereEqualTo("flashcard_id", flashcardId)
            .whereNotEqualTo("status", 1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val cards = ArrayList<Card>()

                for (document in querySnapshot.documents) {
                    val card = Card().apply {
                        SetId(document.id) // Use document ID as card ID
                        SetFront(document.getString("front") ?: "")
                        SetBack(document.getString("back") ?: "")
                        SetStatus(document.getLong("status")?.toInt() ?: 0)
                        SetIsLearned(document.getLong("is_learned")?.toInt() ?: 0)
                        SetFlashcard_id(document.getString("flashcard_id") ?: "")
                        SetCreated_at(document.getString("created_at") ?: "")
                        SetUpdated_at(document.getString("updated_at") ?: "")
                    }
                    cards.add(card)
                }

                // Gọi callback với danh sách card đã tạo
                callback(cards)
            }
            .addOnFailureListener { e ->
                Log.e("CardDAO", "getAllCardByStatus: ${e.message}", e)
                callback(ArrayList()) // Trả về danh sách rỗng khi gặp lỗi
            }
    }



    // Delete card by id
    suspend fun deleteCardById(id: String): Long {
        val db = FirebaseFirestore.getInstance()

        return try {
            // Attempt to delete the card document by its ID from the "cards" collection
            db.collection("cards").document(id).delete().await()

            Log.d("CardDAO", "Card with ID: $id successfully deleted.")
            1L // Return 1 to indicate success
        } catch (e: Exception) {
            Log.e("CardDAO", "deleteCardById: ${e.message}", e)
            0L // Return 0 in case of failure
        }
    }


    // Update card status by id
    fun updateCardStatusById(id: String, status: Int, callback: (Long) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Create a map with the updated "status" value
        val updates = mapOf("status" to status)

        // Update the document in the "cards" collection with the specified ID
        db.collection("cards").document(id).update(updates)
            .addOnSuccessListener {
                Log.d("CardDAO", "Card with ID: $id successfully updated with status: $status")
                callback(1L) // Indicate success by passing 1
            }
            .addOnFailureListener { e ->
                Log.e("CardDAO", "updateCardStatusById: ${e.message}", e)
                callback(0L) // Indicate failure by passing 0
            }
    }



    // Get card by status
    suspend fun getCardByStatus(flashcardId: String, status: Int): Int {
        val db = FirebaseFirestore.getInstance()

        return try {
            // Perform Firestore query to get cards by flashcard_id and status
            val querySnapshot = db.collection("cards")
                .whereEqualTo("flashcard_id", flashcardId)
                .whereEqualTo("status", status)
                .get()
                .await()

            // Return the number of matching cards
            querySnapshot.size()
        } catch (e: Exception) {
            Log.e("CardDAO", "getCardByStatus: ${e.message}", e)
            0 // Return 0 on error
        }
    }


    // Reset status of cards by flashcard_id to 2
    fun resetStatusCardByFlashCardId(flashcardId: String, onComplete: (Long) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val cardsRef = db.collection("cards")

        // Perform Firestore query to get all cards by flashcard_id
        cardsRef.whereEqualTo("flashcard_id", flashcardId).get()
            .addOnSuccessListener { querySnapshot ->
                // Initialize a counter to track successful updates
                var updatedCount = 0L

                // Loop through each card and update its status
                for (document in querySnapshot.documents) {
                    cardsRef.document(document.id)
                        .update("status", 2)
                        .addOnSuccessListener {
                            updatedCount++
                            // If all documents are updated, invoke the callback with the count
                            if (updatedCount == querySnapshot.size().toLong()) {
                                onComplete(updatedCount)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("CardDAO", "Failed to update card with ID: ${document.id}: ${e.message}", e)
                        }
                }

                // If no documents were found, return the count immediately
                if (querySnapshot.isEmpty) {
                    onComplete(0L)
                }
            }
            .addOnFailureListener { e ->
                Log.e("CardDAO", "resetStatusCardByFlashCardId: ${e.message}", e)
                onComplete(0L) // Return 0 on error
            }
    }



    // Update is_learned by id

    suspend fun updateIsLearnedCardById(id: String, isLearned: Int): Long {
        val db = FirebaseFirestore.getInstance()

        return try {
            // Create a map with the updated "status" value
            val updates = mapOf("is_learned" to isLearned)

            // Update the document in the "cards" collection with the specified ID
            db.collection("cards").document(id).update(updates).await()

            Log.d("CardDAO", "Card with ID: $id successfully updated with isLearned: $isLearned")
            1L // Return 1 to indicate success
        } catch (e: Exception) {
            Log.e("CardDAO", "updateIsLearnedCardById: ${e.message}", e)
            0L // Return 0 in case of failure
        }
    }



    // Check if card exists by id
    suspend fun checkCardExist(card_id: String): Boolean {
        val cardRef = db.collection("cards").document(card_id)

        return try {
            // Sử dụng await() từ coroutines để đợi task hoàn thành trong luồng nền
            val snapshot = cardRef.get().await() // Firebase Task được await bằng coroutines
            snapshot.exists() // Trả về true nếu document tồn tại, ngược lại false
        } catch (e: Exception) {
            Log.e("CardDAO", "checkCardExist: ", e)
            false // Trả về false trong trường hợp có lỗi
        }
    }



    // Update card by id
    suspend fun updateCardById(card: Card): Long {
        val db = FirebaseFirestore.getInstance()
        val cardsRef = db.collection("cards")

        return try {
            // Create a map to hold the updated values
            val updatedValues = hashMapOf(
                "front" to card.GetFront(),
                "back" to card.GetBack(),
                "flashcard_id" to card.GetFlashcard_id(),
                "created_at" to card.GetCreated_at(),
                "updated_at" to card.GetUpdated_at()
            )

            // Perform the update operation on the card document with the specified id
            card.id?.let { cardsRef.document(it).update(updatedValues as Map<String, Any>).await() } // Await the update operation

            // Return 1 to indicate that the operation was successful
            1L
        } catch (e: Exception) {
            Log.e("CardDAO", "updateCardById: ${e.message}", e)
            0L // Return 0 on error
        }
    }



    // Get card with is_learned = 0
    suspend fun getCardByIsLearned(flashcardId: String, isLearned: Int): ArrayList<Card> {
        val db = FirebaseFirestore.getInstance()
        val result = ArrayList<Card>() // Khởi tạo ArrayList thay vì mutableListOf

        try {
            val snapshot = db.collection("cards")
                .whereEqualTo("flashcard_id", flashcardId)
                .whereEqualTo("is_learned", isLearned)
                .get()
                .await()

            for (document in snapshot.documents) {
                val card = document.toObject(Card::class.java)
                card?.id = document.id // Gán ID document vào thuộc tính id của đối tượng Card
                card?.let { result.add(it) }
            }
        } catch (e: Exception) {
            Log.e("CardDAO", "Error getting cards: ${e.message}", e)
        }

        return result // Trả về ArrayList<Card>
    }




    // Get all cards by flashcard_id
    suspend fun getAllCardByFlashCardId(flashcardId: String): ArrayList<Card> {
        val db = FirebaseFirestore.getInstance()
        val result = ArrayList<Card>() // Khởi tạo ArrayList thay vì mutableListOf

        try {
            val snapshot = db.collection("cards")
                .whereEqualTo("flashcard_id", flashcardId)
                .get()
                .await()

            for (document in snapshot.documents) {
                val card = document.toObject(Card::class.java)
                card?.id = document.id // Gán ID document vào thuộc tính id của đối tượng Card
                card?.let { result.add(it) }
            }
        } catch (e: Exception) {
            Log.e("CardDAO", "Error getting cards: ${e.message}", e)
        }

        return result // Trả về ArrayList<Card>
    }





    // Reset is_learned and status card by flashcard_id to 0
    suspend fun resetIsLearnedAndStatusCardByFlashCardId(flashcardId: String): Long {
        val db = FirebaseFirestore.getInstance()
        val cardsRef = db.collection("cards")

        return try {
            // Create a map to hold the reset values
            val resetValues = hashMapOf(
                "is_learned" to 0,
                "status" to 0
            )

            // Perform the update operation to reset the fields for cards with the specified flashcard_id
            cardsRef.whereEqualTo("flashcard_id", flashcardId).get().await().documents.forEach { document ->
                document.reference.update(resetValues as Map<String, Any>).await() // Await the update operation for each document
            }

            // Return 1 to indicate that the operation was successful
            1L
        } catch (e: Exception) {
            Log.e("CardDAO", "resetIsLearnedAndStatusCardByFlashCardId: ${e.message}", e)
            0L // Return 0 on error
        }
    }


    // Get cards with status != 1 and flashcard_id


    // Helper function to convert Card to Firestore data map
    private fun getCardsFromFirestore(querySnapshot: QuerySnapshot?): ArrayList<Card> {
        val cards = ArrayList<Card>()

        if (querySnapshot != null && !querySnapshot.isEmpty) { // Check if the QuerySnapshot is not empty
            for (document in querySnapshot.documents) {
                val card = Card().apply {
                    id = document.getString("id") ?: ""
                    front = document.getString("front") ?: ""
                    back = document.getString("back") ?: ""
                    flashcard_id = document.getString("flashcard_id") ?: ""
                    status = document.getLong("status")?.toInt() ?: 0
                    isLearned = document.getLong("is_learned")?.toInt() ?: 0
                    created_at = document.getString("created_at") ?: ""
                    updated_at = document.getString("updated_at") ?: ""
                }
                cards.add(card)
            }
        } else {
            Log.d("CardDAO", "No documents found in Firestore.")
        }
        return cards
    }

}
