package com.madalin.notelo.core.domain.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Item decoration class used for adding space to the items of a [RecyclerView] via [getItemOffsets].
 * @param spanCount number of columns
 * @param spacing space size between items
 * @param includeEdge `true` if spacing should be applied to the edge items, `false` otherwise
 */
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    /**
     * Allows you to specify the size of the space that should be added around each item in the [RecyclerView].
     * It's called for each item, and the resulting spacing values are used to position the item and its decorations within the [RecyclerView].
     * @param outRect specifies the spacing to be added around the item
     */
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column

        // calculates the left, right, top and bottom spacing for the item based on its position and whether it's on the edge of the grid or not
        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)

            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount

            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}