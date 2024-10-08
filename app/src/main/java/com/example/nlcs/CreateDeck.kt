package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.*

class CreateDeck : AppCompatActivity(), View.OnClickListener {
    private lateinit var btnSave: Button
    private lateinit var tvAddCards: TextView
    private lateinit var lvCards: ListView
    private lateinit var edtTitle: EditText
    private val uid: String? = FirebaseAuth.getInstance().uid
    private var thisDeck: Deck? = null
    private var deckId: String? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_deck)

        btnSave = findViewById(R.id.btnSaveDeck)
        tvAddCards = findViewById(R.id.tvAdd)
        lvCards = findViewById(R.id.lvEditCards)
        edtTitle = findViewById(R.id.edtDeckTitle)

        btnSave.setOnClickListener(this)
        tvAddCards.setOnClickListener(this)

        // Check if we are editing a pre-existing deck
        val bundle = intent.extras
        if (bundle != null) {
            thisDeck = bundle.getSerializable("Deck") as Deck?
            deckId = bundle.getString("did")
            deckId?.let { id ->
                loadDeck(id)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnSaveDeck -> {
                saveDeck()
            }
            R.id.tvAdd -> {
                val intent = Intent(this, AddCardsActivity::class.java)
                intent.putExtra("DeckId", deckId) // Pass deckId if editing
                startActivity(intent)
            }
        }
    }

    private fun saveDeck() {
        val title = edtTitle.text.toString()
        if (title.isEmpty()) {
            edtTitle.error = "Title is required"
            return
        }

        val cards = thisDeck?.cards ?: emptyList() // You should update this with actual card data

        val newDeck = Deck(
            deckId = deckId ?: UUID.randomUUID().toString(), // Generate new ID if not editing
            uid = uid,
            title = title,
           // author = FirebaseAuth.getInstance().currentUser?.displayName,
            cards = cards
        )

        newDeck.deckId?.let {
            db.collection("decks")
                .document(it)
                .set(newDeck, SetOptions.merge()) // Use SetOptions.merge() to update existing documents
                .addOnSuccessListener {
                    // Handle success
                    val intent = Intent(this, PublicDecks::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    e.printStackTrace()
                }
        }
    }

    private fun loadDeck(deckId: String) {
        db.collection("decks").document(deckId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val deck = document.toObject(Deck::class.java)
                    thisDeck = deck
                    edtTitle.setText(deck?.title)
                    // You should also update the ListView with the cards
                } else {
                    // Handle no such document
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                e.printStackTrace()
            }
    }
}