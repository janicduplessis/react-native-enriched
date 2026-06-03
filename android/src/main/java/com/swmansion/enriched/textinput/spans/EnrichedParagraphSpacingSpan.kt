package com.swmansion.enriched.textinput.spans

import android.graphics.Paint
import android.text.Spannable
import android.text.TextPaint
import android.text.style.LineHeightSpan
import android.text.style.MetricAffectingSpan
import com.facebook.react.uimanager.PixelUtil

// Whether the line [start, end) is the last line of a standalone block that
// carries trailing spacing: it ends a paragraph (not a wrapped continuation
// line), is not the final block in the document, and is not followed by another
// item in the same list. Shared by the spacing span and the cursor clamp so both
// agree on which lines are inflated by the trailing gap.
fun spannableLineHasBlockTrailingGap(
  spannable: Spannable,
  start: Int,
  end: Int,
): Boolean {
  val length = spannable.length
  // The line range includes its trailing newline, so a paragraph's last line
  // ends with '\n' at end - 1. Wrapped continuation lines break mid-paragraph
  // and stay tight.
  val isLastLineOfParagraph = end >= length || (end >= 1 && spannable[end - 1] == '\n')
  if (!isLastLineOfParagraph) return false
  // No trailing gap after the final block in the document.
  if (end >= length) return false

  // The next paragraph begins right after the trailing newline, at end.
  val currentIsListItem = isBlockListItem(spannable, start, minOf(end, start + 1))
  val nextIsListItem = isBlockListItem(spannable, end, minOf(length, end + 2))
  // Keep items within the same list tight; only space at block boundaries.
  return !(currentIsListItem && nextIsListItem)
}

private fun isBlockListItem(
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
    if (!spannableLineHasBlockTrailingGap(spannable, start, end)) return

    val gapPx = PixelUtil.toPixelFromDIP(spacing).toInt()
    fm.descent += gapPx
    fm.bottom = maxOf(fm.bottom, fm.descent)
  }
}
