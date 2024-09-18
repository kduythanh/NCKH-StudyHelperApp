package com.example.nlcs.data.model

import com.example.nlcs.data.dao.FolderDAO
import java.util.UUID

class Folder {
    var id: String? = null
    var name: String? = null
    var description: String? = null
    var created_at: String? = null
    var updated_at: String? = null

    constructor()

    constructor(
        id: String?,
        name: String?,
        description: String?,
        created_at: String?,
        updated_at: String?
    ) {
        this.id = id
        this.name = name
        this.description = description
        this.created_at = created_at
        this.updated_at = updated_at
    }

    fun getCreated_at(): String? {
        return created_at;
    }

    fun getUpdated_at(): String? {
        return updated_at;
    }

    suspend fun copy(id: String): Folder? {
        // Assuming you have a FolderDAO or some way to get a Folder by its ID
        val folderDAO = FolderDAO()

        // Retrieve the folder from the data source by ID
        val originalFolder = folderDAO.getFolderById(id) ?: return null

        // Create a new folder with the same properties (deep copy)
        val newFolder = Folder(
            id = UUID.randomUUID().toString(), // Generate a new ID for the copied folder
            name = originalFolder.name + " - Copy",
            created_at = System.currentTimeMillis(), // Set the current time for the new folder
            // Add other properties that need to be copied
        )

        // Optionally, save the new folder to the database
        folderDAO.insertFolder(newFolder)

        // Return the newly created folder
        return newFolder
    }
}
