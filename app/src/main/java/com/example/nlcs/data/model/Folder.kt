package com.example.nlcs.data.model

import com.example.nlcs.data.dao.FolderDAO
import java.util.UUID

class Folder {
    var id: String? = null
    var name: String? = null
    var description: String? = null
   // var is_public: Int = 0
    var created_at: String? = null
    var updated_at: String? = null
    var user_id: String? = null
    constructor()

    constructor(
        id: String?,
        name: String?,
        description: String?,
     //   is_public: Int,
        created_at: String?,
        updated_at: String?,
        user_id: String?
    ) {
        this.id = id
        this.name = name
        this.description = description
      //  this.is_public = is_public
        this.created_at = created_at
        this.updated_at = updated_at
        this.user_id = user_id
    }

    //fun SetIs_public(is_public: Int) {
       // this.is_public = is_public;

   // }

    fun SetUser_id(user_id: String) {
        this.user_id = user_id;

    }

    fun SetId(id: String) {
        this.id = id;

    }
    fun SetDescription(description: String) {
        this.description = description;

    }

    fun SetName(name: String) {
        this.name = name;

    }
    fun SetCreated_at(created_at: String) {
        this.created_at = created_at;

    }
    fun SetUpdated_at(updated_at: String) {
        this.updated_at = updated_at;

    }


    fun fetchCreated_at(): String? {
        return created_at;
    }

    fun fetchId(): String? {
        return id;
    }
    fun fetchName(): String? {
        return name;
    }

    fun fetchDescription(): String? {
        return description;
    }

    fun fetchUpdated_at(): String? {
        return updated_at;
    }

    fun fetchUser_id(): String? {
        return user_id
    }




}
