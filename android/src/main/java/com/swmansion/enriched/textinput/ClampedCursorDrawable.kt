package com.swmansion.enriched.textinput

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.text.Spannable
import com.swmansion.enriched.textinput.spans.spannableLineHasBlockTrailingGap
import java.lang.ref.WeakReference

// Wraps the system text cursor so it does not stretch into the trailing block
// spacing. EnrichedParagraphSpacingSpan inflates the descent of a block's last
// line, which would otherwise let the caret reach down into the gap. On those
// lines the cursor is clamped back to the glyph line; everywhere else it draws
// unchanged.
class ClampedCursorDrawable(
  private val wrapped: Drawable,
  private val viewRef: WeakReference<EnrichedTextInputView>,
) : Drawable() {
  override fun draw(canvas: Canvas) {
    val b = bounds
    var bottom = b.bottom

    val view = viewRef.get()
    val layout = view?.layout
    val text = view?.text
    val gapPx = view?.blockGapPx() ?: 0
    if (layout != null && text is Spannable && gapPx in 1 until b.height()) {
      val offset = view.selectionStart.coerceIn(0, text.length)
      val line = layout.getLineForOffset(offset)
      val lineStart = layout.getLineStart(line)
      val lineEnd = layout.getLineEnd(line)
      if (spannableLineHasBlockTrailingGap(text, lineStart, lineEnd)) {
        bottom = b.bottom - gapPx
      }
    }

    wrapped.setBounds(b.left, b.top, b.right, bottom)
    wrapped.draw(canvas)
  }

  override fun setAlpha(alpha: Int) {
    wrapped.alpha = alpha
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    wrapped.colorFilter = colorFilter
  }

  @Deprecated("Deprecated in Java", ReplaceWith("wrapped.opacity"))
  override fun getOpacity(): Int = wrapped.opacity

  override fun getIntrinsicWidth(): Int = wrapped.intrinsicWidth

  override fun getIntrinsicHeight(): Int = wrapped.intrinsicHeight
}
