package com.example.nlcs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs

class ViewCard : AppCompatActivity(), View.OnClickListener {
    private var cards: List<List<String>> = ArrayList()
    private var tvShuffle: TextView? = null
    private var tvOrder: TextView? = null
    private var tvInfo: TextView? = null
    private var tvTitle: TextView? = null
    private var tvCard: TextView? = null
    private var ivShuffle: ImageView? = null
    private var ivOrder: ImageView? = null
    private var ivInfo: ImageView? = null
    private var currCard: List<String>? = null
    private var front = true
    private var deckId: String? = null
    private var inc = 0

    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_card)

        tvShuffle = findViewById(R.id.tvShuffle)
        tvOrder = findViewById(R.id.tvOrder)
        tvInfo = findViewById(R.id.tvInfo)
        tvTitle = findViewById(R.id.tvCardTitle)
        tvCard = findViewById(R.id.tvCard)

        // Initialize UI components
       // ivShuffle = findViewById(R.id.ivShuffle)
        //ivOrder = findViewById(R.id.ivOrder)
       // ivInfo = findViewById(R.id.ivInfo)

        // Get deckId from intent
        deckId = intent.getStringExtra("DeckId")

        // Load deck from Firestore
        loadDeck()

        tvCard?.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                nextCard()
            }

            override fun onSwipeRight() {
                previousCard()
            }

            override fun onClick() {
                flipCard()
            }
        })

        // Set click listeners for other UI elements if needed
    }

    private fun loadDeck() {
        if (deckId == null) {
            Log.d("ViewCard", "No deckId provided")
            return
        }

        db.collection("decks").document(deckId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val deck = document.toObject(Deck::class.java)
                    deck?.let {
                        cards = it.cards
                        currCard = cards.getOrNull(inc)
                        tvTitle?.text = it.title
                        tvCard?.text = currCard?.get(FRONT)
                    }
                } else {
                    Log.d("ViewCard", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.d("ViewCard", "Failed to load deck: ", e)
            }
    }

    private fun flipCard() {
        if (front) {
            front = false
            tvCard?.text = currCard?.get(BACK)
            tvCard?.setBackgroundColor(Color.parseColor("#d3d3d3"))
            tvCard?.setTypeface(Typeface.DEFAULT)
        } else {
            front = true
            tvCard?.setBackgroundColor(Color.parseColor("#A3FFFFFF"))
            tvCard?.setTypeface(Typeface.DEFAULT_BOLD)
            tvCard?.text = currCard?.get(FRONT)
        }
    }

    private fun nextCard() {
        inc = (inc + 1) % cards.size
        currCard = cards[inc]
        tvCard?.text = currCard?.get(FRONT)
    }

    private fun previousCard() {
        inc = if (inc - 1 < 0) cards.size - 1 else inc - 1
        currCard = cards[inc]
        tvCard?.text = currCard?.get(FRONT)
    }

    override fun onClick(v: View) {
        // Add your click handling logic here if necessary
    }

    open inner class OnSwipeTouchListener(context: Context?) : OnTouchListener {
        private val gestureDetector: GestureDetector = GestureDetector(context, GestureListener())

        open fun onClick() {}

        open fun onSwipeLeft() {}

        open fun onSwipeRight() {}

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                onClick()
                return super.onSingleTapUp(e)
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val distanceX = e2.x - e1!!.x
                val distanceY = e2.y - e1.y
                if (abs(distanceX) > abs(distanceY) && abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0) onSwipeRight() else onSwipeLeft()
                    return true
                }
                return false
            }
        }
    }

    companion object {
        private const val FRONT = 0
        private const val BACK = 1
        private const val SWIPE_DISTANCE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }
}