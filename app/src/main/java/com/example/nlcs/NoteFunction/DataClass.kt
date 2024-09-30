package com.example.nlcs.NoteFunction

import android.os.Parcel
import android.os.Parcelable

data class Message(
    var messId: String? = null,
    var messTitle: String = "",
    var messContent: String = "",
    var userId: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(messId)
        parcel.writeString(messTitle)
        parcel.writeString(messContent)
        parcel.writeString(userId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }
}





