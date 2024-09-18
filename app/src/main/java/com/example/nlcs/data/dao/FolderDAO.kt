package com.example.nlcs.data.dao

import android.content.Context
import android.util.Log
import androidx.room.util.copy
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.data.model.Folder
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FolderDAO(CreateFolderActivity: Context) {

    private val db = FirebaseFirestore.getInstance()

    // Insert folder

    suspend fun insertFolder(folder: Folder): Boolean {
        val db = FirebaseFirestore.getInstance()
        return try {
            val folderData = hashMapOf(
                "id" to folder.id,
                "name" to folder.name,
                "description" to folder.description,
                "is_public" to false, // Assuming '0' in SQLite translates to 'false' in Firestore
                "created_at" to folder.getCreated_at(),
                "updated_at" to folder.getUpdated_at()
            )

            // Insert the folder into Firestore in the "folders" collection
            folder.id?.let {
                db.collection("folders")
                    .document(it) // Assuming folder ID is unique and will be used as the document ID
                    .set(folderData)
                    .await()
            } // Await for asynchronous Firestore operation

            true // Return success
        } catch (e: Exception) {
            Log.e("FolderDAO", "insertFolder: ${e.message}")
            false // Return failure in case of error
        }
    }


    // Add flashcard to folder
    fun addFlashCardToFolder(folderId: String, flashcardId: String): Boolean {
        val db = FirebaseFirestore.getInstance()

        // Check if the record already exists
        if (isFlashCardInFolder(folderId, flashcardId)) {
            return false // Return false if the flashcard is already in the folder
        }

        val flashCardFolderData = hashMapOf(
            "folder_id" to folderId,
            "flashcard_id" to flashcardId
        )

        return try {
            // Add a new document to the "folders" collection
            db.collection("folders")
                .add(flashCardFolderData)
                //.await() // Await for the asynchronous Firestore operation to complete

            true // Return true if insertion was successful
        } catch (e: Exception) {
            Log.e("FolderDAO", "addFlashCardToFolder: ${e.message}")
            false // Return false in case of error
        }
    }


    // Get all flashcards by folder ID
    suspend fun getAllFlashCardsByFolderId(folderId: String): List<FlashCard> {
        val db = FirebaseFirestore.getInstance()
        val flashCards = mutableListOf<FlashCard>()

        return try {
            // Query Firestore to get flashcards associated with the folder
            val folderFlashcardsQuery = db.collection("folders_flashcards")
                .whereEqualTo("folder_id", folderId)
                .get()
                .await() // Await the asynchronous Firestore operation

            for (document in folderFlashcardsQuery) {
                val flashcardId = document.getString("flashcard_id") ?: continue

                // Fetch the flashcard details
                val flashCardDocument = db.collection("flashcards").document(flashcardId).get().await()

                val flashCard = flashCardDocument.toObject(FlashCard::class.java)?.copy(
                    id = flashCardDocument.id // Include the document ID
                ) ?: continue

                flashCards.add(flashCard)
            }

            flashCards
        } catch (e: Exception) {
            Log.e("FolderDAO", "getAllFlashCardsByFolderId: ${e.message}", e)
            emptyList() // Return an empty list in case of error
        }
    }

    // Get folder by ID
    suspend fun getFolderById(folderId: String): Folder? {
        val db = FirebaseFirestore.getInstance()
        val folderRef = db.collection("folders").document(folderId)
        var folder: Folder? = null

        return try {
            val documentSnapshot = folderRef.get().await() // Await the Firestore operation
            if (documentSnapshot.exists()) {
                folder = documentSnapshot.toObject(Folder::class.java)?.copy(
                    id = documentSnapshot.id // Set the document ID
                )
            }
            folder
        } catch (e: Exception) {
            Log.e("FolderDAO", "getFolderById: ${e.message}", e)
            null // Return null in case of an error
        }
    }

    // Check if flashcard is in the folder
    suspend fun isFlashCardInFolder(folderId: String, flashcardId: String): Boolean {
        return try {
            val folderSnapshot = db.collection("folders").document(folderId).get().await()
            val flashcards = folderSnapshot.get("flashcards") as List<String>
            flashcards.contains(flashcardId)
        } catch (e: Exception) {
            Log.e("FolderDAO", "isFlashCardInFolder: $e")
            false
        }
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
        return try {
            folder.id?.let { db.collection("folders").document(it).set(folder) }
            true
        } catch (e: Exception) {
            Log.e("FolderDAO", "updateFolder: $e")
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
    suspend fun getAllFlashCardIdsByFolderId(folderId: String): List<String> {
        return try {
            val folderSnapshot = db.collection("folders").document(folderId).get().await()
            folderSnapshot.get("flashcards") as List<String>
        } catch (e: Exception) {
            Log.e("FolderDAO", "getAllFlashCardIdsByFolderId: $e")
            emptyList()
        }
    }

    // Get all folders
    suspend fun getAllFolders(): List<Folder> {
        val db = FirebaseFirestore.getInstance()
        val folders = mutableListOf<Folder>()

        try {
            // Get all documents from the 'folders' collection, ordered by 'created_at'
            val querySnapshot = db.collection("folders")
                .orderBy("created_at")
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val folder = document.toObject(Folder::class.java)?.apply {
                    id = document.id // Set the document ID as the folder ID
                }
                folder?.let { folders.add(it) }
            }
        } catch (e: Exception) {
            Log.e("FirestoreDAO", "Error getting folders: $e")
        }

        return folders
    }
}
