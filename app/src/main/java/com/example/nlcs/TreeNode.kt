package com.example.nlcs

data class TreeNode<T>(
    var data: T? = null,
    val children: MutableList<TreeNode<T>> = mutableListOf(),
    var father: TreeNode<T>? = null
){
    constructor() : this(null, mutableListOf(), null)
    fun addChild(child: TreeNode<T>){
        child.father = this
        children.add(child)
    }
}