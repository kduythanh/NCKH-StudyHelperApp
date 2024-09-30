package com.example.nlcs.ui.activities.learn

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.nlcs.R
import com.example.nlcs.data.dao.CardDAO
import com.example.nlcs.data.model.Card
import com.example.nlcs.databinding.ActivityTrueFalseFlashCardsBinding
import com.example.nlcs.databinding.DialogCorrectBinding
import com.example.nlcs.databinding.DialogWrongBinding
import com.saadahmedsoft.popupdialog.PopupDialog
import com.saadahmedsoft.popupdialog.Styles
import com.saadahmedsoft.popupdialog.listener.OnDialogButtonClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrueFalseFlashCardsActivity : AppCompatActivity() {
    private val binding by lazy { ActivityTrueFalseFlashCardsBinding.inflate(layoutInflater) }
    private val cardDAO by lazy { CardDAO(this) }
    private lateinit var cardList: ArrayList<Card>
    private var progress = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            setContentView(binding.root)
            setSupportActionBar(binding.toolbar)
            binding.toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()

            }
            setUpQuestion()
            setUpProgressBar()
        }

    }

    private suspend fun setUpProgressBar(): Int {
        val id = intent.getStringExtra("id")
            cardList = id?.let { cardDAO.getCardByIsLearned(it, 0) } as ArrayList<Card>

        binding.timelineProgress.max = cardList.size
        return cardList.size
    }

    private suspend fun setUpQuestion() {
        val id = intent.getStringExtra("id")
        cardList = id?.let { cardDAO.getCardByIsLearned(it, 0) } as ArrayList<Card>

        if (cardList.isEmpty()) {
            finishQuiz()
            return // Thêm return để thoát nếu không có câu hỏi
        }

        val randomCard = cardList.random()
        val cardListAll = id?.let { cardDAO.getAllCardByFlashCardId(it) }
        cardListAll?.remove(randomCard)

        val incorrectAnswer = cardListAll?.shuffled()?.take(1)

        val random = (0..1).random()
        binding.questionTv.text = randomCard.front

        if (random == 0) {
            binding.answerTv.text = randomCard.back
        } else {
            binding.answerTv.text = incorrectAnswer?.get(0)?.back
        }

        binding.trueBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                handleAnswer(random, randomCard, incorrectAnswer, true)
            }
        }

        binding.falseBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                handleAnswer(random, randomCard, incorrectAnswer, false)
            }
        }
    }

    private suspend fun handleAnswer(random: Int, randomCard: Card, incorrectAnswer: List<Card>?, isTrue: Boolean) {
        if ((isTrue && random == 0) || (!isTrue && random == 1)) {
            randomCard.back?.let { correctAnswer ->
                correctDialog(correctAnswer)
                randomCard.id?.let { cardId ->
                    val result = cardDAO.updateIsLearnedCardById(cardId, 1)
                    if (result > 0) {
                        progress++
                        increaseProgress()
                    }
                }
            }
        } else {
            randomCard.back?.let { correctAnswer ->
                randomCard.front?.let { question ->
                    incorrectAnswer?.get(0)?.back?.let { wrongAnswer ->
                        wrongDialog(correctAnswer, question, wrongAnswer)
                    }
                }
            }
        }
        setUpQuestion()
    }

    private fun increaseProgress() {
        binding.timelineProgress.progress = progress
    }

    private suspend fun finishQuiz() { //1 quiz, 2 learn
        binding.timelineProgress.progress = setUpProgressBar()
        runOnUiThread {

            PopupDialog.getInstance(this)
                .setStyle(Styles.SUCCESS)
                .setHeading(getString(R.string.finish))
                .setDescription(getString(R.string.finish_quiz))
                .setDismissButtonText(getString(R.string.ok))
                .setNegativeButtonText(getString(R.string.cancel))
                .setPositiveButtonText(getString(R.string.ok))
                .setCancelable(false)
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

        }
        builder.show()
    }
}