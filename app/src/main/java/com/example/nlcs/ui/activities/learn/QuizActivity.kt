package com.example.nlcs.ui.activities.learn

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.R
import com.example.nlcs.UsageTracker
import com.example.nlcs.data.dao.CardDAO
import com.example.nlcs.data.model.Card
import com.example.nlcs.databinding.ActivityQuizBinding
import com.example.nlcs.databinding.DialogCorrectBinding
import com.example.nlcs.databinding.DialogWrongBinding
import com.saadahmedsoft.popupdialog.PopupDialog
import com.saadahmedsoft.popupdialog.Styles
import com.saadahmedsoft.popupdialog.listener.OnDialogButtonClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuizActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityQuizBinding.inflate(layoutInflater)
    }

//    private var binding: ActivityQuizBinding? = null
    private val cardDAO by lazy {
        CardDAO(this)
    }

    private var progress = 0
    private lateinit var correctAnswer: String
    private val askedCards = mutableListOf<Card>()
    private lateinit var id: String
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    // Declare usageTracker to use UsageTracker class
    private lateinit var usageTracker: UsageTracker
    // Setting saving time start at 0
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usageTracker = UsageTracker(this)
        CoroutineScope(Dispatchers.Main).launch {
            setContentView(binding?.root)
            id = intent.getStringExtra("id") ?: ""
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            binding?.toolbar?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            setNextQuestion()
            val max = cardDAO.getCardByIsLearned(id, 0).size
            binding?.timelineProgress?.max = max
        }
    }

    private fun checkAnswer(selectedAnswer: String, cardId: String): Boolean {
        return if (selectedAnswer == correctAnswer) {
            correctDialog(correctAnswer)

            // Thực hiện việc cập nhật không đồng bộ trong CoroutineScope
            CoroutineScope(Dispatchers.Main).launch {
                val result = cardDAO.updateIsLearnedCardById(cardId, 1)
                if (result > 0) {
                    Log.d("CardDAO", "Card with ID: $cardId updated successfully")
                } else {
                    Log.e("CardDAO", "Failed to update card with ID: $cardId")
                }


            }

            setNextQuestion()
            progress++
            setUpProgressBar()
            true
        } else {
            wrongDialog(correctAnswer, binding?.tvQuestion?.text.toString(), selectedAnswer)
            setNextQuestion()
            false
        }
    }

    private fun setUpProgressBar() {
        binding?.timelineProgress?.progress = progress
    }


    private fun setNextQuestion() {
        CoroutineScope(Dispatchers.Main).launch {
            // Sử dụng DAO đã sửa để lấy dữ liệu
            val cards = cardDAO.getCardByIsLearned(id, 0) // get a list of cards that are not learned
            val randomCard = cardDAO.getAllCardByFlashCardId(id) // get all cards

            if (cards.isEmpty()) {
                finishQuiz()
                return@launch
            }

            val correctCard = cards.random() // get a random card from a list of cards that are not learned
            randomCard.remove(correctCard) // remove the correct card from a list of all cards

            val incorrectCards = randomCard.shuffled().take(3) // get 3 random cards from list of all cards

            val allCards = (listOf(correctCard) + incorrectCards).shuffled() // shuffle 4 cards
            val question = correctCard.front
            correctAnswer = correctCard.back.toString()

            withContext(Dispatchers.Main) {
                binding?.tvQuestion?.text = question
                binding?.optionOne?.text = allCards[0].back
                binding?.optionTwo?.text = allCards[1].back
                binding?.optionThree?.text = allCards[2].back
                binding?.optionFour?.text = allCards[3].back

                // In ra log để kiểm tra ID của correctCard
                Log.d("QuizDebug", "correctCard.id: ${correctCard.id}")

                binding?.optionOne?.setOnClickListener {
                    correctCard.id?.let { it1 ->
                        Log.d("QuizDebug", "Option One Selected - correctCard.id: $it1")
                        checkAnswer(binding?.optionOne?.text.toString(), it1)
                    } ?: Log.e("QuizDebug", "correctCard.id is null for Option One")
                }

                binding?.optionTwo?.setOnClickListener {
                    correctCard.id?.let { it1 ->
                        Log.d("QuizDebug", "Option Two Selected - correctCard.id: $it1")
                        checkAnswer(binding?.optionTwo?.text.toString(), it1)
                    } ?: Log.e("QuizDebug", "correctCard.id is null for Option Two")
                }

                binding?.optionThree?.setOnClickListener {
                    correctCard.id?.let { it1 ->
                        Log.d("QuizDebug", "Option Three Selected - correctCard.id: $it1")
                        checkAnswer(binding?.optionThree?.text.toString(), it1)
                    } ?: Log.e("QuizDebug", "correctCard.id is null for Option Three")
                }

                binding?.optionFour?.setOnClickListener {
                    correctCard.id?.let { it1 ->
                        Log.d("QuizDebug", "Option Four Selected - correctCard.id: $it1")
                        checkAnswer(binding?.optionFour?.text.toString(), it1)
                    } ?: Log.e("QuizDebug", "correctCard.id is null for Option Four")
                }

                askedCards.add(correctCard)
            }
        }
    }


    private fun finishQuiz() { //1 quiz, 2 learn
        runOnUiThread {

            PopupDialog.getInstance(this)
                .setStyle(Styles.SUCCESS)
                .setHeading(getString(R.string.finish))
                .setDescription(getString(R.string.finish_quiz))
                .setDismissButtonText(getString(R.string.ok))
                .setNegativeButtonText(getString(R.string.cancel))
                .setPositiveButtonText(getString(R.string.ok))
                .setCancelable(true)
                .showDialog(object : OnDialogButtonClickListener() {
                    override fun onDismissClicked(dialog: Dialog?) {
                        super.onDismissClicked(dialog)
                        dialog?.dismiss()
                        finish()
                    }
                })
        }

    }

    private fun correctDialog(answer: String) {
        // Tạo một AlertDialog.Builder
        val dialogBuilder = AlertDialog.Builder(this)

        // Sử dụng DialogCorrectBinding để tạo nội dung dialog
        val dialogBinding = DialogCorrectBinding.inflate(layoutInflater)

        // Gán giá trị cho TextView trong binding
        dialogBinding.questionTv.text = answer

        // Thiết lập view cho dialog
        dialogBuilder.setView(dialogBinding.root)

        // Tạo dialog
        val dialog = dialogBuilder.create()

        // Thiết lập sự kiện khi dialog bị đóng
        dialog.setOnDismissListener {
            // Có thể thêm hành động khi dialog bị đóng nếu cần
        }

        // Hiển thị dialog
        dialog.show()
    }

    private fun wrongDialog(answer: String, question: String, userAnswer: String) {
        val dialog = AlertDialog.Builder(this)
        val dialogBinding = DialogWrongBinding.inflate(layoutInflater)
        dialog.setView(dialogBinding.root)
        dialog.setCancelable(true)
        val builder = dialog.create()
        dialogBinding.questionTv.text = question
        dialogBinding.explanationTv.text = answer
        dialogBinding.yourExplanationTv.text = userAnswer
        dialogBinding.continueTv.setOnClickListener {
            builder.dismiss()
        }
        builder.setOnDismissListener {
            //startAnimations()
        }
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()

        // Đặt binding thành null an toàn khi Activity bị hủy
//        binding = null
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
}

