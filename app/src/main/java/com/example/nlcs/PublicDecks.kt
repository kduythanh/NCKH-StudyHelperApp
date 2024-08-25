package com.example.nlcs

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class PublicDecks : AppCompatActivity(), View.OnClickListener {
    private var allDecks: ArrayList<Deck?> = ArrayList()
   // private var personalDecks: ArrayList<Deck?> = ArrayList()
   // private var inPublic = true

    private var lvDecks: ListView? = null
    private var addDeck: ImageView? = null
    private var myDecks: TextView? = null
    private var svDecks: SearchView? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_decks)

        initViews()
        setupListeners()

        //inPublic = intent.getBooleanExtra("isPublic", true)

        // Fetch decks from Firestore
        fetchDecks()
    }

    private fun initViews() {
        svDecks = findViewById(R.id.svSearchPublic)
        myDecks = findViewById(R.id.tvPublicDecks)
        addDeck = findViewById(R.id.abPlusPublic)
        lvDecks = findViewById(R.id.lvDecksPublic)
    }

    private fun setupListeners() {
        addDeck?.setOnClickListener(this)
        myDecks?.setOnClickListener(this)
    }

    private fun fetchDecks() {
            // Fetch public decks
            db.collection("decks")
                //.whereEqualTo("isPublic", true)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    allDecks.clear()
                    for (document in querySnapshot) {
                        val deck = document.toObject(Deck::class.java)
                        allDecks.add(deck)
                    }
                    updateDecksList()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching decks: ${e.message}", Toast.LENGTH_LONG).show()
                }

    }

    fun updateDecksList() {
        val adapter = DeckListAdapter(this, R.layout.deck_lv_item, allDecks )
        lvDecks?.adapter = adapter

        lvDecks?.setOnItemClickListener { _, _, position, _ ->
            //val sendDeck = if (inPublic) allDecks else personalDecks
            val intent = Intent(this@PublicDecks, ViewCard::class.java)
            //intent.putExtra("Deck", sendDeck[position])
            startActivity(intent)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvPublicDecks -> handlePublicDecksClick()
            R.id.abPlusPublic -> handleAddDeckClick()
        }
    }

    private fun handlePublicDecksClick() {
        //inPublic = true
        fetchDecks()
       // switchToPublic()
    }

    private fun handleAddDeckClick() {
        showPopup(R.layout.create_deck_popup, "Create Deck?") {
            addCards()
        }
    }

    private fun showPopup(layoutRes: Int, message: String, onClick: () -> Unit) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(layoutRes, null)

        val tvPopup = popupView.findViewById<TextView>(R.id.tvPopup)
        tvPopup.text = message

        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0)

        popupView.setOnClickListener { onClick() }
    }

    private fun addCards() {
        val intent = Intent(this@PublicDecks, AddCardsActivity::class.java)
        startActivity(intent)
    }

    //private fun switchToPublic() {
      //  inPublic = true
    //}

   // private fun switchToMine() {
     //   inPublic = false
    //}
}