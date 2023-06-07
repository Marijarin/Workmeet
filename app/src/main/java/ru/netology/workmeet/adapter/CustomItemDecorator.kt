package ru.netology.workmeet.adapter

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CustomItemDecorator(context: Context) : RecyclerView.ItemDecoration() {
    companion object {
        private const val OFFSET = -15
    }

    override fun getItemOffsets(
        rect: Rect,
        view: View,
        parent: RecyclerView,
        s: RecyclerView.State
    ) {
        super.getItemOffsets(rect, view, parent, s)
        if (parent.getChildAdapterPosition(view) != (parent.adapter?.itemCount?.minus(1))) {
            rect.right = OFFSET
        }
    }
}