package com.example.nlcs.ui.activities.learn

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.nlcs.UsageTracker
import com.example.nlcs.adapter.card.CardLeanAdapter
import com.example.nlcs.data.dao.CardDAO
import com.example.nlcs.data.model.Card
import com.example.nlcs.databinding.ActivityLearnBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.yuyakaido.android.cardstackview.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LearnActivity : AppCompatActivity(), CardStackListener {
    private val binding: ActivityLearnBinding by lazy {
        ActivityLearnBinding.inflate(layoutInflater)
    }

//    private var binding: ActivityLearnBinding? = null
    private val manager by lazy { CardStackLayoutManager(this, this) }
    private val adapter by lazy { CardLeanAdapter(emptyList()) } // Initialize with empty list
    private val cardDAO by lazy { CardDAO(this) }

    private lateinit var size: String
    //private var size: Int = 0 // or any default value you need

    // Declare usageTracker to use UsageTracker class
    private lateinit var usageTracker: UsageTracker
    // Setting saving time start at 0
    private var startTime: Long = 0


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usageTracker = UsageTracker(this)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbar)

        // Fetch initial cards
        createCards { cards ->
            if (cards.isEmpty()) {
                showHide()
                Toast.makeText(this@LearnActivity, "Không có thẻ để học!", Toast.LENGTH_SHORT).show()
            } else {
                // Update UI with card size
                getSize { size ->
                    binding?.cardsLeftTv?.text = "Số thẻ còn lại: $size"
                }
                adapter.setCards(cards)
                adapter.notifyDataSetChanged()
            }

            binding?.toolbar?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            setupCardStackView()
            setupButton()

            // Handle "Keep Learning" button click
            binding?.keepLearnBtn?.setOnClickListener {
                createCards { updatedCards ->
                    if (updatedCards.isEmpty()) {
                        Toast.makeText(this@LearnActivity, "Không có thẻ để học!", Toast.LENGTH_SHORT).show()
                    } else {
                        showContainer()
                        getSize { size ->
                            binding?.cardsLeftTv?.text = "Số thẻ còn lại: ${size - 1}"
                        }
                        adapter.setCards(updatedCards)
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            // Handle "Reset Learning" button click
            binding?.resetLearnBtn?.setOnClickListener {
                val flashcardId = intent.getStringExtra("id")
                flashcardId?.let { id ->
                    // Call the non-suspend `resetStatusCardByFlashCardId` function
                    cardDAO.resetStatusCardByFlashCardId(id) { updatedCount ->
                        if (updatedCount > 0) {
                            // Fetch reset cards asynchronously after reset
                            createCards { resetCards ->
                                showContainer()
                                adapter.setCards(resetCards)
                                adapter.notifyDataSetChanged()
                                getSize { size ->
                                    binding?.cardsLeftTv?.text = "Số thẻ còn lại: $size"
                                }
                            }
                        } else {
                          //  Toast.makeText(this@LearnActivity, "Failed to reset card statuses", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }






    override fun onCardDragging(direction: Direction?, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: d = $direction, r = $ratio")
    }

    // Declare size as a nullable string and initialize it later
    //private var size: String? = null

    @SuppressLint("SetTextI18n")


    override fun onCardSwiped(direction: Direction?) {
        val card = adapter.getCards()[manager.topPosition - 1]

        // Đảm bảo size đã được khởi tạo
        if (!::size.isInitialized) {
            getSize { calculatedSize ->
                size = calculatedSize.toString() // Khởi tạo lại nếu chưa có
            }
        }

        if (direction == Direction.Right) {
            val learnValue = binding?.learnTv?.text.toString().toInt() + 1
            binding?.learnTv?.text = learnValue.toString()
            card.status = 1

            card.id?.let { cardId ->
                cardDAO.updateCardStatusById(cardId, card.status) { result ->
                    // Xử lý cập nhật trạng thái card
                }
            }

            // Cập nhật size một cách an toàn
            size = (size.toIntOrNull()?.minus(1)?.coerceAtLeast(0) ?: 0).toString()
            binding?.cardsLeftTv?.text = "Số thẻ còn lại: $size"
        } else if (direction == Direction.Left) {
            val studyValue = binding?.studyTv?.text.toString().toInt() + 1
            binding?.studyTv?.text = studyValue.toString()
            card.status = 2

            card.id?.let { cardId ->
                cardDAO.updateCardStatusById(cardId, card.status) { result ->
                    // Xử lý cập nhật trạng thái card
                }
            }

            // Cập nhật size một cách an toàn
            size = (size.toIntOrNull()?.minus(1)?.coerceAtLeast(0) ?: 0).toString()
            binding?.cardsLeftTv?.text = "Số thẻ còn lại: $size"
        }

        if (manager.topPosition == adapter.getCount()) {
            showHide() // Ẩn UI khi tất cả các card đã bị swipe
        }
    }



    @SuppressLint("SetTextI18n")
    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager.topPosition}")

        // Ensure size is initialized
        if (!::size.isInitialized) {
            size = binding?.cardsLeftTv?.text.toString() // Initialize from UI or set a default value
        }

        if (manager.topPosition < adapter.itemCount) {
            val card = adapter.getCards()[manager.topPosition]

            if (card.status == 1) {
                card.status = 0
                card.id?.let { cardId ->
                    cardDAO.updateCardStatusById(cardId, card.status) { result ->
                        if (result == 1L) {
                            Log.d("CardDAO", "Card with ID: $cardId updated successfully")
                        } else {
                            Log.e("CardDAO", "Failed to update card with ID: $cardId")
                            Toast.makeText(this@LearnActivity, "Cập nhật trạng thái thất bại!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                if (binding?.learnTv?.text.toString().toInt() > 0) {
                    binding?.learnTv?.text = (binding?.learnTv?.text.toString().toInt() - 1).toString()
                }
            } else if (card.status == 2) {
                card.status = 0
                card.id?.let { cardId ->
                    cardDAO.updateCardStatusById(cardId, card.status) { result ->
                        if (result == 1L) {
                            Log.d("CardDAO", "Card with ID: $cardId updated successfully")
                        } else {
                            Log.e("CardDAO", "Failed to update card with ID: $cardId")
                            Toast.makeText(this@LearnActivity, "Cập nhật trạng thái thất bại!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                if (binding?.studyTv?.text.toString().toInt() > 0) {
                    binding?.studyTv?.text = (binding?.studyTv?.text.toString().toInt() - 1).toString()
                }
            }
        } else {
            Toast.makeText(this@LearnActivity, "Không có thẻ để quay lại!", Toast.LENGTH_SHORT).show()
        }

        // Safely update the size
        size = (size.toIntOrNull()?.plus(1) ?: 1).toString() // Use toIntOrNull for safety
        binding?.cardsLeftTv?.text = "Số thẻ còn lại: $size"
    }

    override fun onCardCanceled() {
    }

    override fun onCardAppeared(view: View?, position: Int) {
    }

    override fun onCardDisappeared(view: View?, position: Int) {
    }

    @SuppressLint("SetTextI18n")
    private fun setupButton() {
        binding?.skipButton?.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            binding?.cardStackView?.swipe()

        }

        binding?.rewindButton?.setOnClickListener {
            val setting = RewindAnimationSetting.Builder()
                .setDirection(Direction.Bottom)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(DecelerateInterpolator())
                .build()
            manager.setRewindAnimationSetting(setting)
            binding?.cardStackView?.rewind()
        }

        binding?.likeButton?.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            binding?.cardStackView?.swipe()
        }
    }

    private fun setupCardStackView() {
        initialize()
    }

    private fun createCards(onCardsLoaded: (List<Card>) -> Unit) {
        val id = intent.getStringExtra("id")

        // Kiểm tra nếu id null hoặc rỗng
        if (id == null || id.isEmpty()) {
            Log.e("createCards", "Flashcard ID is null or empty")
            onCardsLoaded(emptyList()) // Return empty list if ID is invalid
            return
        } else {
            Log.d("createCards", "Flashcard ID: $id")
        }

        // Fetch cards asynchronously using Firestore query
        cardDAO.getAllCardByStatus(id) { cards ->
            // Gọi lại hàm `onCardsLoaded` với danh sách các card đã lấy được
            onCardsLoaded(cards)
        }
    }





    private fun initialize() {
        manager.setStackFrom(StackFrom.Bottom)
        manager.setVisibleCount(1)
        manager.setTranslationInterval(8.0f)
        manager.setScaleInterval(0.95f)
        manager.setSwipeThreshold(0.3f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(true)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
        binding?.cardStackView?.layoutManager = manager
        binding?.cardStackView?.adapter = adapter
        binding?.cardStackView?.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    private fun showHide() {
        val learn = binding?.learnTv?.visibility == View.VISIBLE
        val cardSlack = binding?.cardStackView?.visibility == View.VISIBLE
        val button = binding?.buttonContainer?.visibility == View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            if (learn && cardSlack && button) {
                binding?.leanLl?.visibility = View.GONE
                binding?.cardStackView?.visibility = View.GONE
                binding?.buttonContainer?.visibility = View.GONE
                binding?.reviewContainer?.visibility = View.VISIBLE
                preview()
            }
        }

    }

    private fun showContainer() {
        if (binding?.cardStackView?.visibility == View.GONE) {
            binding?.cardStackView?.visibility = View.VISIBLE
            binding?.buttonContainer?.visibility = View.VISIBLE
            binding?.leanLl?.visibility = View.VISIBLE
            binding?.reviewContainer?.visibility = View.GONE
            binding?.learnTv?.text = "0"
            binding?.studyTv?.text = "0"
        }
    }

    private suspend fun preview() {
        binding?.knowNumberTv?.text = getCardStatus(1).toString()
        binding?.stillLearnNumberTv?.text = getCardStatus(2).toString()
        binding?.termsLeftNumberTv?.text = getCardStatus(0).toString()
        val sum =
            (getCardStatus(1).toFloat() / (getCardStatus(0).toFloat() + getCardStatus(1).toFloat() + getCardStatus(2))) * 100
        binding?.reviewProgress?.setSpinningBarLength(sum)
        binding?.reviewProgress?.isEnabled = false
        binding?.reviewProgress?.isFocusableInTouchMode = false
        binding?.reviewProgress?.setValueAnimated(sum, 1000)
    }

    private fun getSize(onSizeCalculated: (Int) -> Unit) {
        createCards { cards ->
            val sizeInt = cards.size // Tính toán số lượng card
            onSizeCalculated(sizeInt)
            size = sizeInt.toString() // Gán giá trị cho biến size
            binding?.cardsLeftTv?.text = "Số thẻ còn lại: $size" // Hiển thị lên UI
        }
    }



    private suspend fun getCardStatus(status: Int): Int {
        val id = intent.getStringExtra("id") ?: return 0 // Return 0 if id is null
        return cardDAO.getCardByStatus(id, status) // Call the DAO method
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.example.nlcs.R.menu.menu_tick, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == com.example.nlcs.R.id.done) {
            showHide()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        // Lưu thời gian bắt đầu (mốc thời gian hiện tại) để tính thời gian sử dụng khi Activity bị tạm dừng
        startTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()

        // Tính toán thời gian sử dụng Sơ đồ tư duy
        val endTime = System.currentTimeMillis()
        val durationInMillis = endTime - startTime
        val durationInSeconds = (durationInMillis / 1000).toInt() // Chuyển đổi thời gian từ milliseconds sang giây

        // Kiểm tra nếu thời gian sử dụng hợp lệ (lớn hơn 0 giây) thì lưu vào UsageTracker
        if (durationInSeconds > 0) {
            usageTracker.addUsageTime("Thẻ ghi nhớ", durationInSeconds)
        } else {
            usageTracker.addUsageTime("Thẻ ghi nhớ", 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Đặt binding thành null an toàn khi Activity bị hủy
//        binding = null
    }

}



