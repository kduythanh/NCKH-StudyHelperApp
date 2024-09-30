package com.example.nlcs.data.dao

import android.content.Context
import android.util.Log
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.data.model.Folder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FolderDAO(CreateFolderActivity: Context) {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var firebaseAuth: FirebaseAuth

    init {
        // Initialize firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
    }


    // Insert folder

    fun insertFolder(folder: Folder): Boolean {
        val db = FirebaseFirestore.getInstance()
        firebaseAuth = Firebase.auth
        return try {
            val folderData = hashMapOf(
                "id" to folder.fetchId(),
                "name" to folder.fetchName(),
                "description" to folder.fetchDescription(),
                // "is_public" to 0, // Assuming '0' in SQLite translates to 'false' in Firestore
                "created_at" to folder.fetchCreated_at(),
                "updated_at" to folder.fetchUpdated_at(),
                "user_id" to (folder.fetchUser_id() ?: firebaseAuth.currentUser?.uid ?: "")
            )

            // Insert the folder into Firestore in the "folders" collection
            folder.id?.let {
                db.collection("folders")
                    .document(it) // Assuming folder ID is unique and will be used as the document ID
                    .set(folderData)
                //.await()
            } // Await for asynchronous Firestore operation

            true // Return success
        } catch (e: Exception) {
            Log.e("FolderDAO", "insertFolder: ${e.message}")
            false // Return failure in case of error
        }
    }


    // Add flashcard to folder
    suspend fun addFlashCardToFolder(folder_id: String, flashcard_id: String): Long {
        val db = FirebaseFirestore.getInstance()

        // Check if the flashcard is already in the folder
        if (isFlashCardInFolder(folder_id, flashcard_id)) {
            return -1 // Flashcard is already in the folder, so return -1 (or handle accordingly)
        }

        return try {
            // Create a map for the new flashcard entry
            val flashcardData = hashMapOf(
                "folder_id" to folder_id,
                "flashcard_id" to flashcard_id
            )

            // Add the flashcard to the collection
            db.collection("folders_flashcards")
                .add(flashcardData)
                .await()

            // Return success code (for Firestore, you can return 1 to indicate success)
            1L
        } catch (e: Exception) {
            Log.e("FolderDAO", "addFlashCardToFolder: $e")
            // Return failure code (could return 0 or -1 to indicate an error)
            0L
        }
    }


    //Get all folder by userId


    // Get all flashcards by folder ID
    fun getAllFlashCardByFolderId(folderId: String, callback: (ArrayList<FlashCard>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val flashCards = ArrayList<FlashCard>()

        // Firestore query to retrieve flashcards that are in the specified folder
        db.collection("folders_flashcards")
            .whereEqualTo("folder_id", folderId)
            .get()
            .addOnSuccessListener { folderFlashcardsSnapshot ->
                if (!folderFlashcardsSnapshot.isEmpty) {
                    val flashCardIds =
                        folderFlashcardsSnapshot.documents.map { it.getString("flashcard_id") }

                    // Fetch flashcards by their IDs
                    db.collection("flashcards")
                        .whereIn(FieldPath.documentId(), flashCardIds)
                        .get()
                        .addOnSuccessListener { flashCardsSnapshot ->
                            for (document in flashCardsSnapshot.documents) {
                                val flashCard = FlashCard().apply {
                                    SetId(document.getString("id") ?: "")
                                    SetName(document.getString("name") ?: "")
                                    SetDescription(document.getString("description") ?: "")
                                    SetCreated_at(document.getString("created_at") ?: "")
                                    SetUpdated_at(document.getString("updated_at") ?: "")
                                    SetIs_public(document.getLong("is_public")?.toInt() ?: 0)
                                    SetUser_id(firebaseAuth.currentUser?.uid ?: "")
                                }
                                flashCards.add(flashCard)
                            }
                            callback(flashCards)  // Return flashCards via the callback function
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirestoreDAO", "Error fetching flashcards: $exception")
                            callback(ArrayList())  // Return empty list on error
                        }
                } else {
                    callback(ArrayList())  // Return empty list if no flashcards found
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreDAO", "Error fetching folder flashcards: $exception")
                callback(ArrayList())  // Return empty list on error
            }
    }


    // Get folder by ID
    fun getFolderById(folderId: String, callback: (Folder?) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("folders").document(folderId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val folder = document.toObject(Folder::class.java)?.apply {
                        id = document.id
                    }
                    callback(folder)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FolderDAO", "getFolderById: Error fetching folder", e)
                callback(null)
            }
    }


    // Check if flashcard is in the folder

    suspend fun isFlashCardInFolder(folderId: String, flashcardId: String): Boolean {
        val db = FirebaseFirestore.getInstance()
        var result = false

        try {
            // Query to check if there is a flashcard with the given folderId and flashcardId
            val querySnapshot = db.collection("folders_flashcards")
                .whereEqualTo("folder_id", folderId)
                .whereEqualTo("flashcard_id", flashcardId)
                .get()
                .await()

            // If the query returns any documents, it means the flashcard is in the folder
            if (!querySnapshot.isEmpty) {
                result = true
            }
        } catch (e: Exception) {
            Log.e("FolderDAO", "isFlashCardInFolder: $e")
        }

        return result
    }


    // Delete folder
    fun deleteFolder(folderId: String): Boolean {
        return try {
            db.collection("folders").document(folderId).delete()
            true
        } catch (e: Exception) {
            Log.e("FolderDAO", "deleteFolder: $e")
            false
        }
    }

    // Update folder
    fun updateFolder(folder: Folder): Boolean {
        // Get Firestore instance
        val db = FirebaseFirestore.getInstance()

        // Create a map to hold the folder data
        val folderData = mapOf(
            "name" to folder.fetchName(),
            "description" to folder.fetchDescription(),
            "updated_at" to folder.fetchUpdated_at()
        )

        return try {
            // Update the folder document in the "folders" collection by folder.id
            folder.id?.let {
                db.collection("folders")
                    .document(it)
                    .update(folderData)
                //.await()
            } // suspend function, awaits the operation to complete

            // If the update is successful, return true
            true
        } catch (e: Exception) {
            // Log the error
            Log.e("FolderDAO", "updateFolder: ${e.message}", e)
            false
        }
    }

    // Remove flashcard from folder
    fun removeFlashCardFromFolder(folderId: String, flashcardId: String): Boolean {
        return try {
            val folderRef = db.collection("folders").document(folderId)
            folderRef.update("flashcards", FieldValue.arrayRemove(flashcardId))
            true
        } catch (e: Exception) {
            Log.e("FolderDAO", "removeFlashCardFromFolder: $e")
            false
        }
    }

    // Get all flashcards IDs by folder ID
    suspend fun getAllFlashCardIdByFolderId(folderId: String): ArrayList<String> {
        val flashCards = ArrayList<String>()
        val firestore = FirebaseFirestore.getInstance()

        try {
            // Query to get flashcards that belong to a specific folder
            val querySnapshot = firestore.collection("folders_flashcards")
                .whereEqualTo("folder_id", folderId)
                .get()
                .await() // Using coroutines for asynchronous Firestore operations

            for (document in querySnapshot.documents) {
                val flashcardId = document.getString("flashcard_id")
                if (flashcardId != null) {
                    flashCards.add(flashcardId)
                }
            }
        } catch (e: Exception) {
            Log.e("FolderDAO", "getAllFlashCardIdByFolderId: ${e.message}", e)
        }

        return flashCards
    }


    // Get all folders

    suspend fun getAllFolderByUserId(userId: String): ArrayList<Folder> {
        val folders = ArrayList<Folder>()
        val db = FirebaseFirestore.getInstance()
        firebaseAuth = Firebase.auth

        try {
            // Query Firestore to get folders where user_id matches and order by created_at in descending order
            val querySnapshot = db.collection("folders")
                .whereEqualTo("user_id", userId)
                .orderBy("created_at")
                .get()
                .await() // Use coroutine to await the result

            // Loop through documents in the query snapshot
            for (document in querySnapshot.documents) {
                val folder = Folder().apply {
                    SetId(document.getString("id") ?: "")
                    SetName(document.getString("name") ?: "")
                    SetDescription(document.getString("description") ?: "")
                    SetUser_id(firebaseAuth.currentUser?.uid ?: "")
                    SetCreated_at(document.getString("created_at") ?: "")
                    SetUpdated_at(document.getString("updated_at") ?: "")
                }
                folders.add(folder)
            }

        } catch (e: Exception) {
            Log.e("FolderDAO", "getAllFolderByUserId: ${e.message}", e)
        }

        return folders
    }

}
