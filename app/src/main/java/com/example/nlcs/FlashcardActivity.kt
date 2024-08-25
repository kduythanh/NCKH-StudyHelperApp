package com.example.nlcs
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class FlashcardActivity : AppCompatActivity() {
    private val allDecks: ArrayList<Deck> = ArrayList()
    //private var mAuth: FirebaseAuth? = null
    //private var isGuest = false
    private lateinit var db: FirebaseFirestore
    private var pbMain: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_screen)

        db = FirebaseFirestore.getInstance()
        //mAuth = FirebaseAuth.getInstance()
        pbMain = findViewById(R.id.pbLoadDecks)
        pbMain?.visibility = View.VISIBLE

        readAllDecks()
    }



    // Get all decks from Firestore:
    private fun readAllDecks() {
        db.collection("decks")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val uid: String? = document.getString("uid")
                    val author: String? = document.getString("author")
                    val title: String? = document.getString("title")
                    val cards = document.get("cards") as List<List<String>>? ?: emptyList()
                    val did: String? = document.getString("deckId")
                    val thisDeck = Deck(did, uid, title, author, cards)
                    allDecks.add(thisDeck)
                }
                pbMain?.visibility = View.GONE
                val i: Intent
                i = Intent(this@FlashcardActivity, PublicDecks::class.java)
                i.putExtra("allDecks", allDecks)
                startActivity(i)
            }
            .addOnFailureListener { e ->
                pbMain?.visibility = View.GONE
                Log.d("MainActivity:", "Failed to fetch all decks", e)
            }
    }

}