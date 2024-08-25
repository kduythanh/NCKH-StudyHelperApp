package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Task
import java.util.*

class AddCardsActivity : AppCompatActivity(), View.OnClickListener {
    private val cards: MutableList<List<String>> = ArrayList()
    private val deck = Deck()
    private var tvFront: TextView? = null
    private var tvBack: TextView? = null
    private var tvDone: TextView? = null
    private var tvAdd: TextView? = null
    private var edtTitle: EditText? = null
    private var edtFront: EditText? = null
    private var edtBack: EditText? = null
    private var ivSave: ImageView? = null
    private var ivAdd: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var deckId: String? = null
    private var username: String? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_cards)

        // Initialize Firebase Auth
        //val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        //username = user?.displayName

        // Initialize UI components
        tvFront = findViewById(R.id.tvFront)
        tvBack = findViewById(R.id.tvBack)
        tvDone = findViewById(R.id.tvDone)
        tvAdd = findViewById(R.id.tvAddAnother)
        edtTitle = findViewById(R.id.edtAddTitle)
        ivSave = findViewById(R.id.ivSave)
        ivAdd = findViewById(R.id.ivAdd)
        edtFront = findViewById(R.id.edtFront)
        edtBack = findViewById(R.id.edtBack)
        progressBar = findViewById(R.id.pgBar)
        progressBar?.visibility = View.INVISIBLE

        // Set click listeners
        tvAdd?.setOnClickListener(this)
        ivAdd?.setOnClickListener(this)
        ivSave?.setOnClickListener(this)
        tvDone?.setOnClickListener(this)

        // Handle the incoming bundle (if any)
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            // Set title name, deck contents (implement this if needed)
        } else {
            val date = Date()
            deckId = date.time.toString().takeLast(9)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivAdd, R.id.tvAddAnother -> {
                val frontText = edtFront?.text.toString()
                val backText = edtBack?.text.toString()
                if (frontText.isNotEmpty() && backText.isNotEmpty()) {
                    cards.add(listOf(frontText, backText))
                    edtFront?.setText("")
                    edtBack?.setText("")
                    edtFront?.requestFocus()
                } else {
                    Toast.makeText(this, "Incomplete card.", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.ivSave, R.id.tvDone -> {
                progressBar?.visibility = View.VISIBLE
                val frontText = edtFront?.text.toString()
                val backText = edtBack?.text.toString()
                if (frontText.isNotEmpty() && backText.isNotEmpty()) {
                    cards.add(listOf(frontText, backText))
                }
                deck.cards = cards
                deck.title = edtTitle?.text.toString()
                //deck.author = username
                deck.uid = FirebaseAuth.getInstance().uid
                deck.deckId = deckId

                if (cards.isEmpty()) {
                    val intent = Intent(this@AddCardsActivity, PublicDecks::class.java)
                    startActivity(intent)
                } else {
                    // Add deck to Firestore
                    db.collection("Decks").document(deckId!!)
                        .set(deck)
                        .addOnCompleteListener { task: Task<Void?> ->
                            if (task.isSuccessful) {
                                db.collection("PublicDecks").document(deckId!!).set(hashMapOf("deckId" to deckId!!))

                                val intent = Intent(this@AddCardsActivity, FlashcardActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    this@AddCardsActivity,
                                    "Failed to upload new deck.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            progressBar?.visibility = View.GONE
                        }
                }
            }
        }
    }
}