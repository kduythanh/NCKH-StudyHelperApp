package com.example.nlcs.ui.activities.create

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.R
import com.example.nlcs.adapter.card.CardAdapter
import com.example.nlcs.data.dao.CardDAO
import com.example.nlcs.data.dao.FlashCardDAO
import com.example.nlcs.data.model.Card
import com.example.nlcs.data.model.FlashCard
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.example.nlcs.databinding.ActivityCreateSetBinding
import com.example.nlcs.ui.activities.set.ViewSetActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID

class CreateSetActivity : AppCompatActivity() {
    private var cardAdapter: CardAdapter? = null
    private var cards: ArrayList<Card>? = null
    private var binding: ActivityCreateSetBinding? = null
    private val id = genUUID()
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateSetBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)

        setupToolbar()
        setupSubjectEditText()
        setupDescriptionTextView()
        setupCardsList()
        setupCardAdapter()
        setupAddFab()
        setupItemTouchHelper()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding!!.toolbar)
        binding!!.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // No need to call `getOnBackPressedDispatcher()`
        }
    }

    private fun setupSubjectEditText() {
        if (binding!!.subjectEt.text.toString().isEmpty()) {
            binding!!.subjectEt.requestFocus()
        }
    }

    private fun setupDescriptionTextView() {
        binding!!.descriptionTv.setOnClickListener { v: View? ->
            if (binding!!.descriptionTil.visibility == View.GONE) {
                binding!!.descriptionTil.visibility = View.VISIBLE
            } else {
                binding!!.descriptionTil.visibility = View.GONE
            }
        }
    }

    private fun setupCardsList() {
        //create list two set
        cards = ArrayList()
        cards!!.add(Card())
        cards!!.add(Card())
        updateTotalCards()
    }

    private fun updateTotalCards() {
        binding!!.totalCardsTv.text = String.format("Total Cards: %s", cards!!.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupCardAdapter() {
        cardAdapter = CardAdapter(this, cards!!)
        binding!!.cardsLv.adapter = cardAdapter
        binding!!.cardsLv.layoutManager = LinearLayoutManager(this)
        binding!!.cardsLv.setHasFixedSize(true)
        cardAdapter!!.notifyDataSetChanged()
    }

    private fun setupAddFab() {
        binding!!.addFab.setOnClickListener { v: View? ->
            if (!checkTwoCardsEmpty()) {
                val newCard = Card()
                cards!!.add(newCard)
                //scroll to last item
                binding!!.cardsLv.smoothScrollToPosition(cards!!.size - 1)
                //notify adapter
                cardAdapter!!.notifyItemInserted(cards!!.size - 1)
                updateTotalCards()
            } else {
                Toast.makeText(this, "Please enter front and back", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupItemTouchHelper() {
        val callback = createItemTouchHelperCallback()
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding!!.cardsLv)
    }

    private fun createItemTouchHelperCallback(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                handleOnSwiped(viewHolder)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                handleOnChildDraw(c, viewHolder, dX)
            }
        }
    }

    private fun handleOnSwiped(viewHolder: RecyclerView.ViewHolder) {
        val position = viewHolder.getBindingAdapterPosition()

        // Backup of removed item for undo purpose
        val deletedItem = cards!![position]

        // Removing item from recycler view
        cards!!.removeAt(position)
        updateTotalCards()
        cardAdapter!!.notifyItemRemoved(position)

        // Showing Snack bar with an Undo option
        val snackbar =
            Snackbar.make(binding!!.root, "Item was removed from the list.", Snackbar.LENGTH_LONG)
        snackbar.setAction("UNDO") { view: View? ->

            // Check if the position is valid before adding the item back
            if (position >= 0 && position <= cards!!.size) {
                cards!!.add(position, deletedItem)
                cardAdapter!!.notifyItemInserted(position)
                updateTotalCards()
            } else {
                // If the position isn't valid, show a message or handle the error appropriately
                Toast.makeText(applicationContext, "Error restoring item", Toast.LENGTH_LONG).show()
            }
        }
        snackbar.setActionTextColor(Color.YELLOW)
        snackbar.show()
    }

    private fun handleOnChildDraw(c: Canvas, viewHolder: RecyclerView.ViewHolder, dX: Float) {
        val icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_delete)
        val itemView = viewHolder.itemView
        checkNotNull(icon)
        val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
        val iconBottom = iconTop + icon.intrinsicHeight

        if (dX < 0) { // Swiping to the left
            val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            val background = ColorDrawable(Color.WHITE)
            background.setBounds(
                itemView.right + (dX.toInt()),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)
        } else { // No swipe
            icon.setBounds(0, 0, 0, 0)
        }

        icon.draw(c)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_set, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.done) {
            CoroutineScope(Dispatchers.Main).launch {
                saveChanges()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private suspend fun saveChanges() {
        val subject = binding!!.subjectEt.text.toString()
        val description = binding!!.descriptionEt.text.toString()

        if (subject.isEmpty()) {
            binding!!.subjectTil.error = "Please enter subject"
            binding!!.subjectEt.requestFocus()
            return
        } else {
            binding!!.subjectTil.error = null
        }

        // Lưu flashcard và nhận về document ID (flashcard_id)
        val flashcardId = saveFlashCard(subject, description)
        if (flashcardId == null) {
            Toast.makeText(this, "Insert flashcard failed", Toast.LENGTH_SHORT).show()
            return
        }

        // Lưu tất cả các card với flashcardId
        if (!saveAllCards(flashcardId)) {
            return
        }

        // Chuyển sang ViewSetActivity và truyền flashcardId vào Intent
        val intent = Intent(this, ViewSetActivity::class.java)
        intent.putExtra("id", flashcardId)
        startActivity(intent)
        finish()
    }



    private suspend fun saveAllCards(flashcardId: String): Boolean {
        for (card in cards!!) {
            if (!saveCard(card, flashcardId)) {
                return false
            }
        }
        return true
    }




    //Save cards
    private suspend fun saveCard(card: Card, flashcardId: String): Boolean {
        val front: String = card.GetFront().toString()
        val back: String = card.GetBack().toString()

        if (front.isEmpty()) {
            binding!!.cardsLv.requestFocus()
            Toast.makeText(this, "Please enter front", Toast.LENGTH_SHORT).show()
            return false
        }

        if (back.isEmpty()) {
            binding!!.cardsLv.requestFocus()
            Toast.makeText(this, "Please enter back", Toast.LENGTH_SHORT).show()
            return false
        }

        val cardDAO = CardDAO(this)
        card.SetId(genUUID()) // Tạo ID duy nhất cho card
        card.SetFront(front)
        card.SetBack(back)
        card.SetStatus(0) // Trạng thái ban đầu
        card.SetIsLearned(0) // Trạng thái học tập ban đầu
        card.SetFlashcard_id(flashcardId) // Lưu flashcardId vào field flashcard_id của card
        card.SetCreated_at(getCurrentDate())
        card.SetUpdated_at(getCurrentDate())

        // Chèn card vào Firestore qua CardDAO
        if (cardDAO.insertCard(card) <= 0) {
            Toast.makeText(this, "Insert card failed with flashcardId: $flashcardId", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }



    private suspend fun saveFlashCard(subject: String, description: String): String? {
        val flashCardDAO = FlashCardDAO(this)
        val flashCard = FlashCard()
            flashCard.name = subject
            flashCard.description = description
            flashCard.created_at = getCurrentDate()
            flashCard.updated_at = getCurrentDate()


        // Set trạng thái công khai hoặc riêng tư
        binding!!.privateSwitch.setOnCheckedChangeListener { _, isChecked ->
            flashCard.SetIs_public(if (isChecked) 1 else 0)
        }

        // Gọi insertFlashCard và nhận về document ID sau khi thêm thành công
        return try {
            val result = flashCardDAO.insertFlashCard(flashCard) // Gọi hàm DAO để lưu flashcard
            if (result > 0) {
                flashCard.GetId() // Trả về ID của flashcard
            } else {
                null // Trả về null nếu thất bại
            }
        } catch (e: Exception) {
            Log.e("saveFlashCard", "Error saving flashcard: ${e.message}")
            null
        }
    }


    private fun getCurrentDate(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getCurrentDateNewApi()
        } else {
            getCurrentDateOldApi()
        }
    }

    fun checkTwoCardsEmpty(): Boolean {
        var emptyCount = 0
        for (card in cards!!) {
            if (card.front.isNullOrEmpty() || card.back.isNullOrEmpty()) {
                emptyCount++
                if (emptyCount == 2) {
                    return true
                }
            }
        }
        return false
    }


    private fun genUUID(): String {
        return UUID.randomUUID().toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDateNewApi(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return currentDate.format(formatter)
    }


    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDateOldApi(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        return sdf.format(Date())
    }

}


