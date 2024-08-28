package com.example.nlcs

//import java.util.UUID

data class MindMap(
    var id: String? = null,
    var title: String? = null,
    var date: Long = System.currentTimeMillis(),
    var rootNode: TreeNode<String> = TreeNode(data = "Main Idea")
)

//data class MindMap(
//    var id: String? = null,
//    var title: String? = null,
//    var date: Long = System.currentTimeMillis(),
//    var rootNode: Node = Node(id = UUID.randomUUID().toString(), text = "Main Idea", children = mutableListOf())
//)
//
//data class Node(
//    var id: String? = null,
//    var text: String? = null,
//    var children: MutableList<Node> = mutableListOf()
//)

