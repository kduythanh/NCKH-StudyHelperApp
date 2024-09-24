package com.example.nlcs.NoteFunction

import android.os.Parcel
import android.os.Parcelable

data class Message(
    var messId: String? = null, // Use String? for Firestore document ID
    var messTitle: String = "",
    var messContent: String = "",
    var imageUrl: String? = null  // Add this line for storing image URL
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(messId)
        parcel.writeString(messTitle)
        parcel.writeString(messContent)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }
}



