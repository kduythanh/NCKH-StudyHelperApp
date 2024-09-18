package com.example.nlcs.ui.activities.set

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher.onBackPressed
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
import com.example.nlcs.data.model.Card.setCreated_at
import com.example.nlcs.data.model.Card.setFlashcard_id
import com.example.nlcs.data.model.Card.setId
import com.example.nlcs.data.model.Card.setIsLearned
import com.example.nlcs.data.model.Card.setStatus
import com.example.nlcs.data.model.Card.setUpdated_at
import com.example.nlcs.databinding.ActivityViewSetBinding
import com.example.nlcs.ui.activities.folder.AddToFolderActivity
import com.example.nlcs.ui.activities.learn.LearnActivity
import com.example.nlcs.ui.activities.learn.QuizActivity
import com.example.nlcs.ui.activities.learn.TrueFalseFlashCardsActivity
import com.kennyc.bottomsheet.BottomSheetListener
import com.kennyc.bottomsheet.BottomSheetMenuDialogFragment
import com.kennyc.bottomsheet.BottomSheetMenuDialogFragment.Builder.setAutoExpand
import com.kennyc.bottomsheet.BottomSheetMenuDialogFragment.Builder.setCancelable
import com.kennyc.bottomsheet.BottomSheetMenuDialogFragment.Builder.setCloseTitle
import com.kennyc.bottomsheet.BottomSheetMenuDialogFragment.Builder.setListener
import com.kennyc.bottomsheet.BottomSheetMenuDialogFragment.Builder.setSheet
import com.kennyc.bottomsheet.BottomSheetMenuDialogFragment.Builder.setTitle
import com.kennyc.bottomsheet.BottomSheetMenuDialogFragment.Builder.show
import com.saadahmedsoft.popupdialog.PopupDialog
import com.saadahmedsoft.popupdialog.Styles
import com.saadahmedsoft.popupdialog.listener.OnDialogButtonClickListener
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
        binding!!.recyclerViewSet.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val centerPosition = linearLayoutManager!!.findFirstVisibleItemPosition() + 1
                binding!!.centerTv.text = centerPosition.toString()
                binding!!.previousTv.text =
                    if (centerPosition > 1) (centerPosition - 1).toString() else ""
                binding!!.nextTv.text =
                    if (centerPosition < cards!!.size) (centerPosition + 1).toString() else ""
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setupUserDetails() {
        val id = intent.getStringExtra("id")
        flashCardDAO = FlashCardDAO(this)
        binding!!.descriptionTv.setText(flashCardDAO!!.getFlashCardById(id!!).getDescription())
        cardDAO = CardDAO(this)
        binding!!.termCountTv.text = cardDAO!!.countCardByFlashCardId(intent.getStringExtra("id")!!)
            .toString() + " " + getString(R.string.term)
        flashCardDAO = FlashCardDAO(this)
        binding!!.setNameTv.setText(
            flashCardDAO!!.getFlashCardById(intent.getStringExtra("id")!!).getName()
        )
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
        binding!!.learnCl.setOnClickListener { v: View? ->
            cardDAO = CardDAO(this)
            if (!isUserOwner) {
                showLearnErrorDialog()
                return@setOnClickListener
            }
            if (cardDAO!!.countCardByFlashCardId(intent.getStringExtra("id")!!) < 4) {
                showReviewErrorDialog()
            } else {
                val intent = Intent(this, QuizActivity::class.java)
                intent.putExtra("id", getIntent().getStringExtra("id"))
                startActivity(intent)
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
            })
    }


    private fun setupToolbarNavigation() {
        binding!!.toolbar.setNavigationOnClickListener { v: View? -> getOnBackPressedDispatcher().onBackPressed() }
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
        val id = intent.getStringExtra("id")
        cardDAO = CardDAO(this)
        cards = cardDAO!!.getCardsByFlashCardId(id!!)
        setUpProgress(cards!!)
        val viewSetAdapter = ViewSetAdapter(this, cards!!)
        binding!!.recyclerViewSet.adapter = viewSetAdapter
        viewSetAdapter.notifyDataSetChanged()

        val viewTermsAdapter = ViewTermsAdapter(cards!!)
        binding!!.recyclerViewTerms.adapter = viewTermsAdapter
        viewTermsAdapter.notifyDataSetChanged()
    }

    private fun setupNavigationListener() {
        binding!!.toolbar.setNavigationOnClickListener { v: View? -> getOnBackPressedDispatcher() }
    }

    private fun setupScrollListeners() {
        binding!!.previousIv.setOnClickListener { v: View? ->
            val currentPosition = linearLayoutManager!!.findFirstCompletelyVisibleItemPosition()
            if (currentPosition > 0) {
                binding!!.recyclerViewSet.scrollToPosition(currentPosition - 1)
            }
        }

        binding!!.nextIv.setOnClickListener { v: View? ->
            val currentPosition = linearLayoutManager!!.findLastCompletelyVisibleItemPosition()
            if (currentPosition < cards!!.size - 1) {
                binding!!.recyclerViewSet.scrollToPosition(currentPosition + 1)
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
        if (item.itemId == R.id.menu) {
            Builder(this)
                .setSheet(R.menu.menu_bottom_view_set)
                .setTitle(R.string.book)
                .setListener(object : BottomSheetListener {
                    override fun onSheetShown(
                        bottomSheetMenuDialogFragment: BottomSheetMenuDialogFragment,
                        o: Any?
                    ) {
                    }

                    override fun onSheetItemSelected(
                        bottomSheetMenuDialogFragment: BottomSheetMenuDialogFragment,
                        menuItem: MenuItem,
                        o: Any?
                    ) {
                        val id = intent.getStringExtra("id")

                        val itemId = menuItem.itemId
                        if (itemId == R.id.edit) {
                            if (this.isUserOwner) {
                                handleEditOption(id)
                            } else {
                                Toast.makeText(
                                    this@ViewSetActivity,
                                    getString(R.string.edit_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else if (itemId == R.id.delete_set) {
                            if (this.isUserOwner) {
                                handleDeleteSetOption(id)
                            } else {
                                Toast.makeText(
                                    this@ViewSetActivity,
                                    getString(R.string.edit_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else if (itemId == R.id.add_to_folder) {
                            if (this.isUserOwner) {
                                handleAddToFolderOption(id)
                            } else {
                                Toast.makeText(
                                    this@ViewSetActivity,
                                    getString(R.string.edit_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else if (itemId == R.id.reset) {
                            if (this.isUserOwner) {
                                handleResetOption(id)
                            } else {
                                Toast.makeText(
                                    this@ViewSetActivity,
                                    getString(R.string.edit_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onSheetDismissed(
                        bottomSheetMenuDialogFragment: BottomSheetMenuDialogFragment,
                        o: Any?,
                        i: Int
                    ) {
                    }
                })
                .setCloseTitle(getString(R.string.close))
                .setAutoExpand(true)
                .setCancelable(true)
                .show(supportFragmentManager)
            return true
        }
        return super.onOptionsItemSelected(item)
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
            cardDAO = CardDAO(this@ViewSetActivity)
            if (cardDAO.resetIsLearnedAndStatusCardByFlashCardId(id) > 0L) {
                Toast.makeText(
                    this@ViewSetActivity,
                    getString(R.string.reset_success),
                    Toast.LENGTH_SHORT
                ).show()
                setupCardData()
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
        val flashCardDAO = FlashCardDAO(this@ViewSetActivity)
        if (flashCardDAO.deleteFlashcardAndCards(id!!)) {
            PopupDialog.getInstance(this@ViewSetActivity)
                .setStyle(Styles.SUCCESS)
                .setHeading(getString(R.string.success))
                .setDescription(getString(R.string.delete_set_success))
                .setCancelable(false)
                .setDismissButtonText(getString(R.string.ok))
                .showDialog(object : OnDialogButtonClickListener() {
                    override fun onDismissClicked(dialog: Dialog) {
                        super.onDismissClicked(dialog)
                        finish()
                    }
                })
        } else {
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

    private fun copyFlashCard() {
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
            card.setId(genUUID())
            card.setFlashcard_id(flashCard.id!!)
            card.setIsLearned(0)
            card.setStatus(0)
            card.setCreated_at(currentDate)
            card.setUpdated_at(currentDate)
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
            if (card.getStatus() == 0) {
                notLearned++
            } else if (card.getStatus() == 1) {
                learned++
            } else {
                learning++
            }
        }

        if (isUserOwner) {
            binding!!.notLearnTv.text = "Not learned: $notLearned"
            binding!!.isLearningTv.text = "Learning: $learning"
            binding!!.learnedTv.text = "Learned: $learned"
        } else {
            binding!!.notLearnTv.text = "Not learned: " + cards.size
            binding!!.isLearningTv.text = "Learning: " + 0
            binding!!.learnedTv.text = "Learned: " + 0
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
