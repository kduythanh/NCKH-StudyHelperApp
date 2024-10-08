package com.example.nlcs.ui.activities.set

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.R
import com.example.nlcs.adapter.card.ViewSetAdapter
import com.example.nlcs.adapter.card.ViewTermsAdapter
import com.example.nlcs.data.dao.CardDAO
import com.example.nlcs.data.dao.FlashCardDAO
import com.example.nlcs.data.model.Card
import com.example.nlcs.databinding.ActivityViewSetBinding
import com.example.nlcs.ui.activities.folder.AddToFolderActivity
import com.example.nlcs.ui.activities.learn.LearnActivity
import com.example.nlcs.ui.activities.learn.QuizActivity
import com.example.nlcs.ui.activities.learn.TrueFalseFlashCardsActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.kennyc.bottomsheet.BottomSheetListener
import com.kennyc.bottomsheet.BottomSheetMenuDialogFragment
import com.saadahmedsoft.popupdialog.PopupDialog
import com.saadahmedsoft.popupdialog.Styles
import com.saadahmedsoft.popupdialog.listener.OnDialogButtonClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID

class ViewSetActivity : AppCompatActivity() {
    private var binding: ActivityViewSetBinding? = null
    private var cardDAO: CardDAO? = null
    private var flashCardDAO: FlashCardDAO? = null
    private var cards: ArrayList<Card>? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var listPosition = 0




    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewSetBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)

        flashCardDAO = FlashCardDAO(this)

        setupRecyclerView(savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            setupCardData()

            setupNavigationListener()
            setupScrollListeners()
            setupOnScrollListener()
            setupUserDetails()
            setupReviewClickListener()
            setupLearnClickListener()
            setTrueFalseClickListener()
            setupToolbarNavigation()
        }
    }

    private fun setTrueFalseClickListener() {
        binding!!.trueFalseCl.setOnClickListener { v: View? ->
            if (!isUserOwner) {
                showLearnErrorDialog()
            } else {
                val intent = Intent(this, TrueFalseFlashCardsActivity::class.java)
                intent.putExtra("id", getIntent().getStringExtra("id"))
                startActivity(intent)
            }
        }
    }

    private fun setupOnScrollListener() {
        // Check if binding, linearLayoutManager, and cards are initialized before setting the scroll listener
        //if (cards == null) {
         //   Log.e("Error", "Binding, LinearLayoutManager, or Cards is null")
        //    return
       // }

        // Safely add a scroll listener
        binding?.recyclerViewSet?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Safely get the center position using a safe call
                val centerPosition = linearLayoutManager?.findFirstVisibleItemPosition()?.plus(1) ?: return

                // Safely update the TextViews
                binding?.centerTv?.text = centerPosition.toString()

                // Previous item: if centerPosition > 1, display the previous item, otherwise display an empty string
                binding?.previousTv?.text = if (centerPosition > 1) (centerPosition - 1).toString() else ""

                // Next item: if centerPosition < the size of the cards, display the next item, otherwise display an empty string
                binding?.nextTv?.text = if (centerPosition < (cards?.size ?: 0)) (centerPosition + 1).toString() else ""
            }
        })
    }


    @SuppressLint("SetTextI18n")
    private fun setupUserDetails() {
        val id = intent.getStringExtra("id") ?: return // Safely handle null id
        CoroutineScope(Dispatchers.Main).launch {
            flashCardDAO = FlashCardDAO(this@ViewSetActivity)

            // Set flashcard description
            val flashCardDescription = flashCardDAO?.getFlashCardById(id)?.GetDescription()
            binding?.descriptionTv?.setText(flashCardDescription)

            // Set flashcard name
            val flashCardName = flashCardDAO?.getFlashCardById(id)?.GetName()
            binding?.setNameTv?.setText(flashCardName)

            // Set up cardDAO and fetch card count asynchronously
            cardDAO = CardDAO(this@ViewSetActivity)
            cardDAO?.countCardByFlashCardId(id) { count ->
                // Update UI after the card count is fetched
                binding?.termCountTv?.text = "$count ${getString(R.string.term)}"
            }
        }
    }

    private fun setupReviewClickListener() {
        binding!!.reviewCl.setOnClickListener { v: View? ->
            if (!isUserOwner) {
                showLearnErrorDialog()
            } else {
                val intent = Intent(this, LearnActivity::class.java)
                intent.putExtra("id", getIntent().getStringExtra("id"))
                startActivity(intent)
            }
        }
    }

    private fun setupLearnClickListener() {
        binding?.learnCl?.setOnClickListener {
            // Initialize cardDAO
            cardDAO = CardDAO(this@ViewSetActivity)

            // Check if the user is not the owner
            if (!isUserOwner) {
                showLearnErrorDialog()
                return@setOnClickListener
            }

            // Safely get the ID from the intent
            val flashcardId = intent.getStringExtra("id") ?: return@setOnClickListener

            // Fetch the card count asynchronously using the callback function
            cardDAO?.countCardByFlashCardId(flashcardId) { cardCount ->
                // Check if the card count is less than 4
                if (cardCount < 4) {
                    showReviewErrorDialog()
                } else {
                    // Start the QuizActivity if there are enough cards
                    val quizIntent = Intent(this@ViewSetActivity, QuizActivity::class.java)
                    quizIntent.putExtra("id", flashcardId)
                    startActivity(quizIntent)
                }
            }
        }
    }


    private fun showReviewErrorDialog() {
        PopupDialog.getInstance(this)
            .setStyle(Styles.FAILED)
            .setHeading(getString(R.string.error))
            .setDescription(getString(R.string.learn_error))
            .setDismissButtonText(getString(R.string.ok))
            .setCancelable(true)
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onDismissClicked(dialog: Dialog) {
                    super.onDismissClicked(dialog)
                    dialog.dismiss()
                }
            })
    }

    private fun showLearnErrorDialog() {
        PopupDialog.getInstance(this)
            .setStyle(Styles.STANDARD)
            .setHeading(getString(R.string.error))
            .setDescription(getString(R.string.review_error))
            .setPopupDialogIcon(R.drawable.baseline_error_24)
            .setDismissButtonText(getString(R.string.ok))
            .setNegativeButtonText(getString(R.string.cancel))
            .setPositiveButtonText(getString(R.string.ok))
            .setCancelable(true)
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onNegativeClicked(dialog: Dialog) {
                    super.onNegativeClicked(dialog)
                }

                override fun onPositiveClicked(dialog: Dialog) {
                    super.onPositiveClicked(dialog)
                    CoroutineScope(Dispatchers.Main).launch {
                        copyFlashCard()
                        PopupDialog.getInstance(this@ViewSetActivity)
                            .setStyle(Styles.SUCCESS)
                            .setHeading(getString(R.string.success))
                            .setDescription(getString(R.string.review_success))
                            .setCancelable(false)
                            .setDismissButtonText(getString(R.string.view))
                            .showDialog(object : OnDialogButtonClickListener() {
                                override fun onDismissClicked(dialog: Dialog) {
                                    super.onDismissClicked(dialog)
                                    dialog.dismiss()
                                }
                            })
                    }
                    }

            })
    }


    private fun setupToolbarNavigation() {
        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // No need for getOnBackPressedDispatcher()
        }
    }


    private fun setupRecyclerView(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            listPosition = savedInstanceState.getInt(LIST_POSITION)
        }
        linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager!!.orientation = RecyclerView.HORIZONTAL
        binding!!.recyclerViewSet.layoutManager = linearLayoutManager
        binding!!.recyclerViewSet.scrollToPosition(listPosition)

        val linearLayoutManagerVertical = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        binding!!.recyclerViewTerms.layoutManager = linearLayoutManagerVertical
        binding!!.recyclerViewTerms.isNestedScrollingEnabled = false
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setupCardData() {

        val id = intent.getStringExtra("id") // Sử dụng ID đã truyền qua Intent

        // Kiểm tra nếu id null hoặc rỗng
        if (id == null || id.isEmpty()) {
            Log.e("setupCardData", "Flashcard ID is null or empty")
            return
        } else {
            Log.d("setupCardData", "Flashcard ID: $id")
        }
        CoroutineScope(Dispatchers.Main).launch {
            // Khởi tạo DAO
            cardDAO = CardDAO(this@ViewSetActivity)

            // Sử dụng ID để lấy dữ liệu các thẻ (cards)
            cards = cardDAO?.getCardsByFlashCardId(id)

            // Kiểm tra nếu có dữ liệu thẻ trả về
            if (cards != null && cards!!.isNotEmpty()) {
                setUpProgress(cards!!) // Thiết lập tiến độ nếu cần

                // Thiết lập adapter cho RecyclerViewSet
                val viewSetAdapter = ViewSetAdapter(this@ViewSetActivity, cards!!)
                binding!!.recyclerViewSet.adapter = viewSetAdapter
                viewSetAdapter.notifyDataSetChanged()

                // Thiết lập adapter cho RecyclerViewTerms
                val viewTermsAdapter = ViewTermsAdapter(cards!!)
                binding!!.recyclerViewTerms.adapter = viewTermsAdapter
                viewTermsAdapter.notifyDataSetChanged()
            } else {
                Log.e("setupCardData", "No cards found for flashcard ID: $id")
            }
        }


    }


    private fun setupNavigationListener() {
        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Correct method to trigger back press
        }
    }


    private fun setupScrollListeners() {
        // Safely access previous and next image view bindings
        binding?.previousIv?.setOnClickListener {
            val currentPosition = linearLayoutManager?.findFirstCompletelyVisibleItemPosition() ?: -1
            if (currentPosition > 0) {
                binding?.recyclerViewSet?.scrollToPosition(currentPosition - 1)
            }
        }

        binding?.nextIv?.setOnClickListener {
            val currentPosition = linearLayoutManager?.findLastCompletelyVisibleItemPosition() ?: -1
            // Ensure cards is initialized and has elements
            if (currentPosition < (cards?.size ?: 0) - 1) {
                binding?.recyclerViewSet?.scrollToPosition(currentPosition + 1)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(LIST_POSITION, linearLayoutManager!!.findFirstVisibleItemPosition())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_set, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu) {
            BottomSheetMenuDialogFragment.Builder(this)
                .setSheet(R.menu.menu_bottom_view_set)
                .setListener(object : BottomSheetListener {
                    override fun onSheetShown(bottomSheetMenuDialogFragment: BottomSheetMenuDialogFragment, o: Any?) {
                        // Optional: Handle when the sheet is shown
                    }

                    override fun onSheetItemSelected(
                        bottomSheetMenuDialogFragment: BottomSheetMenuDialogFragment,
                        menuItem: MenuItem,
                        o: Any?
                    ) {
                        val id = intent.getStringExtra("id")
                        val itemId = menuItem.itemId

                        when (itemId) {
                            R.id.edit -> {
                                if (isUserOwner) {
                                    handleEditOption(id)
                                } else {
                                    Toast.makeText(this@ViewSetActivity, getString(R.string.edit_error), Toast.LENGTH_SHORT).show()
                                }
                            }
                            R.id.delete_set -> {
                                if (isUserOwner) {
                                    handleDeleteSetOption(id)
                                } else {
                                    Toast.makeText(this@ViewSetActivity, getString(R.string.edit_error), Toast.LENGTH_SHORT).show()
                                }
                            }
                            R.id.add_to_folder -> {
                                if (isUserOwner) {
                                    handleAddToFolderOption(id)
                                } else {
                                    Toast.makeText(this@ViewSetActivity, getString(R.string.edit_error), Toast.LENGTH_SHORT).show()
                                }
                            }
                            R.id.reset -> {
                                if (isUserOwner) {
                                    handleResetOption(id)
                                } else {
                                    Toast.makeText(this@ViewSetActivity, getString(R.string.edit_error), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    override fun onSheetDismissed(bottomSheetMenuDialogFragment: BottomSheetMenuDialogFragment, o: Any?, i: Int) {
                        // Optional: Handle when the sheet is dismissed
                    }
                })
                .setCloseTitle(getString(R.string.close))
                .setAutoExpand(true)
                .setCancelable(true)
                .show(supportFragmentManager)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun handleEditOption(id: String?) {
        if (isUserOwner) {
            val intent = Intent(this@ViewSetActivity, EditFlashCardActivity::class.java)
            intent.putExtra("flashcard_id", id)
            startActivity(intent)
        } else {
            Toast.makeText(this@ViewSetActivity, getString(R.string.edit_error), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun handleDeleteSetOption(id: String?) {
        if (isUserOwner) {
            //dialog are you sure?
            showDeleteSetDialog(id)
        } else {
            Toast.makeText(this@ViewSetActivity, getString(R.string.edit_error), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun handleAddToFolderOption(id: String?) {
        val intent = Intent(this@ViewSetActivity, AddToFolderActivity::class.java)
        intent.putExtra("flashcard_id", id)
        startActivity(intent)
    }


    private fun handleResetOption(id: String?) {
        if (isUserOwner) {
            // Check if id is null before proceeding
            if (id != null) {
                // Launch a coroutine to handle the suspend function
                CoroutineScope(Dispatchers.Main).launch {
                    cardDAO = CardDAO(this@ViewSetActivity)

                    // Call the suspend function to reset the cards
                    val result = cardDAO!!.resetIsLearnedAndStatusCardByFlashCardId(id)

                    if (result > 0L) {
                        Toast.makeText(
                            this@ViewSetActivity,
                            getString(R.string.reset_success),
                            Toast.LENGTH_SHORT
                        ).show()
                        setupCardData() // Refresh card data after resetting
                    } else {
                        Toast.makeText(
                            this@ViewSetActivity,
                            getString(R.string.reset_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@ViewSetActivity,
                    getString(R.string.reset_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this@ViewSetActivity, getString(R.string.edit_error), Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun showDeleteSetDialog(id: String?) {
        PopupDialog.getInstance(this@ViewSetActivity)
            .setStyle(Styles.STANDARD)
            .setHeading(getString(R.string.delete_set))
            .setDescription(getString(R.string.delete_set_description))
            .setPopupDialogIcon(R.drawable.ic_delete)
            .setCancelable(true)
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog) {
                    super.onPositiveClicked(dialog)
                    deleteSet(id)
                }

                override fun onNegativeClicked(dialog: Dialog) {
                    super.onNegativeClicked(dialog)
                    dialog.dismiss()
                }
            })
    }

    private fun deleteSet(id: String?) {
        // Check if ID is null to avoid crashes
        if (id == null) {
            PopupDialog.getInstance(this@ViewSetActivity)
                .setStyle(Styles.FAILED)
                .setHeading(getString(R.string.error))
                .setDescription(getString(R.string.delete_set_error))
                .setCancelable(true)
                .showDialog(null)
            return
        }

        // Launch a coroutine to handle the suspend function
        CoroutineScope(Dispatchers.Main).launch {
            val flashCardDAO = FlashCardDAO(this@ViewSetActivity)

            // Call the suspend function to delete flashcard and its cards
            val isDeleted = flashCardDAO.deleteFlashcardAndCards(id)

            if (isDeleted) {
                // If deletion is successful, show success dialog
                PopupDialog.getInstance(this@ViewSetActivity)
                    .setStyle(Styles.SUCCESS)
                    .setHeading(getString(R.string.success))
                    .setDescription(getString(R.string.delete_set_success))
                    .setCancelable(false)
                    .setDismissButtonText(getString(R.string.ok))
                    .showDialog(object : OnDialogButtonClickListener() {
                        override fun onDismissClicked(dialog: Dialog) {
                            super.onDismissClicked(dialog)
                            finish() // Close the activity after dismissal
                        }
                    })
            } else {
                // If deletion failed, show error dialog
                PopupDialog.getInstance(this@ViewSetActivity)
                    .setStyle(Styles.FAILED)
                    .setHeading(getString(R.string.error))
                    .setDescription(getString(R.string.delete_set_error))
                    .setCancelable(true)
                    .showDialog(object : OnDialogButtonClickListener() {
                        override fun onPositiveClicked(dialog: Dialog) {
                            super.onPositiveClicked(dialog)
                        }
                    })
            }
        }
    }

    private suspend fun copyFlashCard() {
        val id = intent.getStringExtra("id")
        flashCardDAO = FlashCardDAO(this)
        val flashCard = flashCardDAO!!.getFlashCardById(id!!)
        val idCard = genUUID()
        flashCard!!.id = idCard
        flashCardDAO!!.insertFlashCard(flashCard)

        val cardDAO = CardDAO(this)
        val cards = cardDAO.getCardsByFlashCardId(
            id
        )
        for (card in cards) {
            card.SetId(genUUID())
            card.SetFlashcard_id(flashCard.id!!)
            card.SetIsLearned(0)
            card.SetStatus(0)
            card.SetCreated_at(currentDate)
            card.SetUpdated_at(currentDate)
            if (cardDAO.insertCard(card) < 0L) {
                Toast.makeText(this, getString(R.string.review_error), Toast.LENGTH_SHORT).show()
            }
        }
        Toast.makeText(this, getString(R.string.review_success), Toast.LENGTH_SHORT).show()
    }


    private val currentDate: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentDateNewApi
        } else {
            currentDateOldApi
        }

    private fun genUUID(): String {
        return UUID.randomUUID().toString()
    }


    @get:RequiresApi(api = Build.VERSION_CODES.O)
    private val currentDateNewApi: String
        get() {
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            return currentDate.format(formatter)
        }

    private val currentDateOldApi: String
        get() {
            @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("dd/MM/yyyy")
            return sdf.format(Date())
        }

    private val isUserOwner: Boolean
        get() = true

    @SuppressLint("SetTextI18n")
    private fun setUpProgress(cards: ArrayList<Card>) {
        var notLearned = 0
        var learning = 0
        var learned = 0
        for (card in cards) {
            if (card.GetStatus() == 0) {
                notLearned++
            } else if (card.GetStatus() == 1) {
                learned++
            } else {
                learning++
            }
        }

        if (isUserOwner) {
            binding!!.notLearnTv.text = "Chưa học: $notLearned"
            binding!!.isLearningTv.text = "Đang học: $learning"
            binding!!.learnedTv.text = "Đã học: $learned"
        } else {
            binding!!.notLearnTv.text = "Chưa học: " + cards.size
            binding!!.isLearningTv.text = "Đang học: " + 0
            binding!!.learnedTv.text = "Đã học: " + 0
            binding!!.notLearnTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            binding!!.isLearningTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            binding!!.learnedTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        setupCardData()
        setupUserDetails()
    }

    companion object {
        private const val LIST_POSITION = "list_position"
    }
}