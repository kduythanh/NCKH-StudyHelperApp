package com.example.nlcs.NoteFunction

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.R
import com.example.nlcs.databinding.ActivityNoteFunctionAcitivityBinding

class NoteFunctionAcitivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteFunctionAcitivityBinding
    private var arrayItem: ArrayList<Message>  = ArrayList()
    private var MyAdapter: MyAdapter? = null
    companion object {
        const val KEY = "KEY"
        const val TYPE_EDIT = "TYPE_EDIT"
        const val TYPE_ADD = "TYPE_ADD"
    }

    private var startCheckType =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val type = result.data?.extras?.getString(KEY)
                if (type == TYPE_ADD) {
                    val message = result.data?.extras?.get("Message") as? Message
                    if (message != null) {
                        arrayItem.add(0, message)
                    }
                    MyAdapter?.notifyDataSetChanged()
                }
                if (type == TYPE_EDIT) {
                    val message = result.data?.extras?.get("Message") as? Message
                    if (message != null) {
                        for (item in arrayItem) {
                            if (item.messId == message.messId) {
                                item.messTitle = message.messTitle
                                item.messContent = message.messContent
                                break
                            }
                        }
                    }
                    MyAdapter?.notifyDataSetChanged()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_note_function_acitivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityNoteFunctionAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set toolbar
        setSupportActionBar(binding.toolbar.root)
        binding.toolbar.title.text = "Quản lý tin nhắn"  // Set the title


        //Set what type of layout is the recycle view
        binding.RecycleView.layoutManager = LinearLayoutManager(this)
        binding.RecycleView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        //Add item for test
//        arrayItem.add(Message(1,"Tổng hợp tin tức thời sự1", "tổng hợp tin tức thời sự nóng hổi nhất của tất cả các miền trên dất nước1"))
//        arrayItem.add(Message(2,"Tổng hợp tin tức thời sự2", "tổng hợp tin tức thời sự nóng hổi nhất của tất cả các miền trên dất nước2"))
//        arrayItem.add(Message(3,"Tổng hợp tin tức thời sự3", "tổng hợp tin tức thời sự nóng hổi nhất của tất cả các miền trên dất nước3"))
//        arrayItem.add(Message(4,"Tổng hợp tin tức thời sự4", "tổng hợp tin tức thời sự nóng hổi nhất của tất cả các miền trên dất nước4"))
//        arrayItem.add(Message(5,"Tổng hợp tin tức thời sự5", "tổng hợp tin tức thời sự nóng hổi nhất của tất cả các miền trên dất nước5"))
//        arrayItem.add(Message(6,"Tổng hợp tin tức thời sự6", "tổng hợp tin tức thời sự nóng hổi nhất của tất cả các miền trên dất nước6"))
//        arrayItem.add(Message(7,"Tổng hợp tin tức thời sự7", "tổng hợp tin tức thời sự nóng hổi nhất của tất cả các miền trên dất nước7"))

        //Set the content to the itemHolder in adapter
        MyAdapter = MyAdapter(this,arrayItem)
        binding.RecycleView.adapter = MyAdapter

        MyAdapter?.onItemClick = {message, position ->
            val intent = Intent(this,NoteFunctionAdjustActivity::class.java)
            intent.putExtra("Message",message)
            startCheckType.launch(intent)
        }

        binding.toolbar.AddMessage.setOnClickListener {
            // Handle add message click
            val intent = Intent(this,NoteFunctionAddActivity::class.java)
            startCheckType.launch(intent)
        }
    }
}