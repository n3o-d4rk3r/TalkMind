package bd.com.pseudotic.talkmind.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // Add bottom spacing to last item
        if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) {
            outRect.bottom = 30
        }

        // Add top spacing to first item
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = 30
        }
    }
}