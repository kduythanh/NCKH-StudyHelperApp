package com.example.nlcs
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class DeckListAdapter(
   // private val isGuest: Boolean,
    private val ctx: Context,
    resource: Int,
    private val decks: ArrayList<Deck?>
) : ArrayAdapter<Deck?>(ctx, resource, decks) {

    private class ViewHolder(view: View) {
        //val author: TextView = view.findViewById(R.id.tvDeckAuthor)
        val title: TextView = view.findViewById(R.id.tvDeckName)
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            val inflater = LayoutInflater.from(ctx)
            view = inflater.inflate(R.layout.deck_lv_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val Deck = decks[position]

        if (Deck != null) {
            if (Deck != null) {
                viewHolder.title.text = Deck.title
            }
        }

        return view
    }
}