package com.example.nlcs.data.dao

import android.util.Log
import com.example.nlcs.data.model.FlashCard
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.content.Context
import com.example.nlcs.data.model.Card
import com.example.nlcs.data.model.Folder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.firestore.Query
import java.util.UUID

class FlashCardDAO (ViewSetActivity: Context) {
    private val db = FirebaseFirestore.getInstance()
    //private val flashcardCollection = db.collection("flashcards")
    private lateinit var firebaseAuth: FirebaseAuth


    // Insert flashcard
    suspend fun insertFlashCard(flashcard: FlashCard): Long {
        val db = FirebaseFirestore.getInstance()
        firebaseAuth = Firebase.auth
        val userId = flashcard.GetUser_id() ?: firebaseAuth.currentUser?.uid ?: ""
        val flashcardData = hashMapOf(
            "id" to " ",
            "name" to flashcard.GetName(),
            "description" to flashcard.GetDescription(),
            "user_id" to userId,
            "created_at" to flashcard.GetCreated_at(),
            "updated_at" to flashcard.GetUpdated_at(),
            "is_public" to flashcard.GetIs_public()
        )

        return try {
            // Thêm flashcard vào Firestore và đợi hoàn tất
            val documentReference = db.collection("flashcards").add(flashcardData).await()

            // Lấy document ID sau khi thành công
            val documentId = documentReference.id

            // Cập nhật ID của flashcard và trong flashcardData
            flashcard.SetId(documentId) // Cập nhật ID cho đối tượng flashcard
            flashcardData["id"] = documentId // Cập nhật ID trong dữ liệu đã lưu

            // Cập nhật lại document trong Firestore với ID mới
            documentReference.update("id", documentId).await()

            Log.d("FlashCardDAO", "Insert flashcard successfully with ID: $documentId")
            1L // Trả về 1L khi thành công
        } catch (e: Exception) {
            Log.e("FlashCardDAO", "Error inserting flashcard: ${e.message}", e)
            0L // Trả về 0L khi gặp lỗi
        }
    }



    // Delete flashcard and its cards

    suspend fun deleteFlashcardAndCards(flashcardId: String): Boolean {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        var result = false

        try {
            // Reference to the flashcard document
            val flashcardRef = db.collection("flashcards").document(flashcardId)

            // Delete cards that belong to the flashcard
            val cardsQuerySnapshot = db.collection("cards")
                .whereEqualTo("flashcard_id", flashcardId)
                .get()
                .await()

            // Add delete operations for all matching cards
            for (document in cardsQuerySnapshot.documents) {
                batch.delete(document.reference)
            }

            // Add delete operation for the flashcard itself
            batch.delete(flashcardRef)

            // Commit the batch
            batch.commit().await()

            result = true
        } catch (e: Exception) {
            Log.e("FlashCardDAO", "deleteFlashcardAndCards: $e")
        }

        return result
    }



    // Get flashcard by ID
    suspend fun getFlashCardById(id: String): FlashCard? {
        val db = FirebaseFirestore.getInstance()
        firebaseAuth = Firebase.auth

        return try {
            // Truy vấn Firestore để lấy flashcard có giá trị field 'id' trùng với id đầu vào
            val querySnapshot = db.collection("flashcards")
                .whereEqualTo("id", id) // So sánh field 'id' với giá trị đầu vào
                .get()
                .await()

            // Kiểm tra nếu có tài liệu được trả về
            val document = querySnapshot.documents.firstOrNull()

            // Nếu document tồn tại, tạo đối tượng FlashCard từ document
            document?.let {
                FlashCard().apply {
                    SetId(it.id) // Đây là document ID
                    SetName(it.getString("name") ?: "")
                    SetDescription(it.getString("description") ?: "")
                    SetUser_id(firebaseAuth.currentUser?.uid ?: "")
                    SetCreated_at(it.getString("created_at") ?: "")
                    SetUpdated_at(it.getString("updated_at") ?: "")
                    SetIs_public(it.getLong("is_public")?.toInt() ?: 0)
                }
            }
        } catch (e: Exception) {
            Log.e("FlashCardDAO", "getFlashCardById: ${e.message}", e)
            null // Return null on error
        }
    }



    // Get all flashcards
    suspend fun getAllFlashCardByUserId(userId: String): ArrayList<FlashCard> {
        val flashCards = ArrayList<FlashCard>()
        val db = FirebaseFirestore.getInstance()

        try {
            // Query Firestore to get flashcards where "user_id" matches the provided userId
            val querySnapshot = db.collection("flashcards")
                .whereEqualTo("user_id", userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .await() // Use await to run it as a suspend function

            // Loop through documents and map them to FlashCard objects
            for (document in querySnapshot.documents) {
                val flashCard = document.toObject(FlashCard::class.java)
                if (flashCard != null) {
                    flashCard.SetId(document.id) // Set the document ID if necessary
                    flashCards.add(flashCard)
                }
            }

        } catch (e: Exception) {
            Log.e("FlashCardDAO", "getAllFlashCardByUserId: ${e.message}", e)
        }

        return flashCards
    }


    // Update flashcard
    suspend fun updateFlashCard(flashcard: FlashCard): Long {
        // Create a TaskCompletionSource to manage the asynchronous operation
        val taskCompletionSource = TaskCompletionSource<Long>()

        // Get an instance of Firestore
        val firestore = FirebaseFirestore.getInstance()

        // Reference to the Firestore document for the flashcard
        val flashcardRef = flashcard.GetId()?.let { firestore.collection("flashcards").document(it) }

        // Convert the flashcard object to a map
        val flashcardMap = mapOf(
            "id" to flashcard.GetId(),
            "name" to flashcard.GetName(),
            "description" to flashcard.GetDescription(),
            "user_id" to (flashcard.GetUser_id() ?: firebaseAuth.currentUser?.uid ?: ""),
            "created_at" to flashcard.GetCreated_at(),
            "updated_at" to flashcard.GetUpdated_at(),
            "is_public" to flashcard.GetIs_public()

        )

        // Attempt to update the flashcard in Firestore
        if (flashcardRef != null) {
            flashcardRef.set(flashcardMap)
                .addOnSuccessListener {
                    // If the update is successful, set the result to 1L
                    taskCompletionSource.setResult(1L)
                }
                .addOnFailureListener { exception ->
                    // Log the exception and set the result to 0L
                    Log.e("FlashCardDAO", "updateFlashCard: ${exception.message}", exception)
                    taskCompletionSource.setResult(0L)
                }
        }

        // Await the result of the asynchronous operation
        return try {
            // Await the task completion and return the result
            taskCompletionSource.task.await()
        } catch (e: Exception) {
            // Handle any exceptions during await
            Log.e("FlashCardDAO", "updateFlashCard: ${e.message}", e)
            0L
        }
    }

    // Get all public flashcards
    fun getAllFlashCardPublic(): ArrayList<FlashCard> {
        // Reference to the Firestore collection

        val flashCards = ArrayList<FlashCard>()
        firebaseAuth = Firebase.auth

        // Firestore query to get all documents from the "flashcards" collection, ordered by "created_at"
        db.collection("flashcards")
            .whereEqualTo("is_public", 0)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                // Iterate over each document in the result
                for (document in result) {
                    // Convert Firestore document to FlashCard object
                    val flashCard = FlashCard().apply {
                        SetId(document.getString("id") ?: "")
                        SetName(document.getString("name") ?: "")
                        SetDescription(document.getString("description") ?: "")
                        SetUser_id(firebaseAuth.currentUser?.uid ?: "")
                        SetCreated_at(document.getString("created_at") ?: "")
                        SetUpdated_at(document.getString("updated_at") ?: "")
                        SetIs_public(document.getLong("is_public")?.toInt() ?: 0)
                    }

                    // Add the flashcard to the list
                    flashCards.add(flashCard)
                }
            }
            .addOnFailureListener { exception ->
                // Log any exceptions
                Log.e("FlashCardDAO", "getAllPublicFlashCards: ${exception.message}", exception)
            }
        return flashCards
    }
}
