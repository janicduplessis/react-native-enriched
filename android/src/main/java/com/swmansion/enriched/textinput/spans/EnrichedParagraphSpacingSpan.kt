package com.swmansion.enriched.textinput.spans

import android.graphics.Paint
import android.text.Spannable
import android.text.TextPaint
import android.text.style.LineHeightSpan
import android.text.style.MetricAffectingSpan
import com.facebook.react.uimanager.PixelUtil

// Adds vertical spacing between standalone blocks (paragraphs, headings,
// blockquotes, the boundary around a list) while keeping items inside a list
// tight. The gap is applied as extra space below the last line of a block, so
// wrapped lines within a paragraph stay tight and consecutive list items get no
// gap. This mirrors the trailing paragraph spacing used by read-only renderers.
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

    // The line range includes its trailing newline, so a paragraph's last line
    // ends with '\n' at end - 1. Only that line gets the trailing gap; wrapped
    // continuation lines (which break mid-paragraph) stay tight.
    val isLastLineOfParagraph = end >= text.length || (end >= 1 && text[end - 1] == '\n')
    if (!isLastLineOfParagraph) return
    // No trailing gap after the final block in the document.
    if (end >= text.length) return

    // The next paragraph begins right after the trailing newline, at end.
    val currentIsListItem = isListItem(spannable, start, minOf(end, start + 1))
    val nextIsListItem = isListItem(spannable, end, minOf(text.length, end + 2))
    // Keep items within the same list tight; only space at block boundaries.
    if (currentIsListItem && nextIsListItem) return

    val gapPx = PixelUtil.toPixelFromDIP(spacing).toInt()
    fm.descent += gapPx
    fm.bottom = maxOf(fm.bottom, fm.descent)
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
