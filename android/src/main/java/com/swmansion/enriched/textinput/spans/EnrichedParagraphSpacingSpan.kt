package com.swmansion.enriched.textinput.spans

import android.graphics.Paint
import android.text.Spannable
import android.text.TextPaint
import android.text.style.LineHeightSpan
import android.text.style.MetricAffectingSpan
import com.facebook.react.uimanager.PixelUtil

// Adds vertical spacing between standalone blocks (paragraphs, headings,
// blockquotes, the boundary around a list) while keeping items inside a list
// tight, mirroring the read-only note renderer. The gap is applied as extra
// space above the first line of a block; consecutive list items get no gap so
// they stay tight.
class EnrichedParagraphSpacingSpan(
  private val spacing: Float,
) : MetricAffectingSpan(),
  LineHeightSpan {
  override fun updateDrawState(tp: TextPaint?) {}

  override fun updateMeasureState(tp: TextPaint) {}

  override fun chooseHeight(
    text: CharSequence,
    start: Int,
    end: Int,
    spanstartv: Int,
    v: Int,
    fm: Paint.FontMetricsInt,
  ) {
    val spannable = text as? Spannable ?: return
    // No gap above the very first line of the document.
    if (start == 0) return
    // Only the first line of a paragraph gets the gap (skip wrapped lines).
    if (start > text.length || text[start - 1] != '\n') return

    val currentIsListItem = isListItem(spannable, start, minOf(end, start + 1))
    val prevIsListItem = isListItem(spannable, maxOf(0, start - 2), start - 1)
    // Keep items within the same list tight; only space at block boundaries.
    if (currentIsListItem && prevIsListItem) return

    val gapPx = PixelUtil.toPixelFromDIP(spacing).toInt()
    fm.ascent -= gapPx
    fm.top = minOf(fm.top, fm.ascent)
  }

  private fun isListItem(
    spannable: Spannable,
    rangeStart: Int,
    rangeEnd: Int,
  ): Boolean {
    if (rangeEnd <= rangeStart) return false
    return spannable
      .getSpans(rangeStart, rangeEnd, EnrichedInputUnorderedListSpan::class.java)
      .isNotEmpty() ||
      spannable
        .getSpans(rangeStart, rangeEnd, EnrichedInputOrderedListSpan::class.java)
        .isNotEmpty() ||
      spannable
        .getSpans(rangeStart, rangeEnd, EnrichedInputCheckboxListSpan::class.java)
        .isNotEmpty()
  }
}
