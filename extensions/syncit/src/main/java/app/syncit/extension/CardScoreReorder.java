package app.syncit.extension;

import android.text.SpannableString;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Moves the score line out of a post's description and into its own view at the bottom of the
 * smaller card, leaving the author line and the flair between it and the title.
 *
 * <p>Sync builds the author line and the score line as one piece of text, so the layout cannot
 * place the score under the flair by itself. The score keeps the spans it was drawn with, which
 * includes the score colour.
 *
 * @noinspection unused
 */
public final class CardScoreReorder {
    private static final String SCORE_TAG = "syncit_card_score";

    private CardScoreReorder() {}

    /**
     * Called after a post description has been drawn.
     *
     * @param description The description view, holding the author line and possibly a score line.
     */
    public static void split(TextView description) {
        if (description == null || !(description.getParent() instanceof ViewGroup)) {
            return;
        }

        TextView scoreView = findScoreView((ViewGroup) description.getParent());
        if (scoreView == null) {
            // Not the layout this patch rearranges.
            return;
        }

        CharSequence text = description.getText();
        if (!(text instanceof Spanned)) {
            hide(scoreView);
            return;
        }

        Spanned full = (Spanned) text;
        String plain = full.toString();
        int newline = plain.lastIndexOf('\n');
        if (newline < 0 || newline + 1 >= plain.length()) {
            // No score line on this post, or it was turned off.
            hide(scoreView);
            return;
        }

        int start = newline + 1;
        int end = plain.length();

        SpannableString score = new SpannableString(full.subSequence(start, end));
        for (Object span : full.getSpans(start, end, Object.class)) {
            int spanStart = Math.max(0, full.getSpanStart(span) - start);
            int spanEnd = Math.min(score.length(), full.getSpanEnd(span) - start);
            if (spanEnd <= spanStart) {
                continue;
            }
            try {
                score.setSpan(span, spanStart, spanEnd, full.getSpanFlags(span));
            } catch (Throwable ignored) {
                // A span this container cannot hold is dropped; the rest still draws.
            }
        }

        // Keep the author line, and everything standing over it, in the description.
        description.setText(full.subSequence(0, newline));

        scoreView.setTextSize(TypedValue.COMPLEX_UNIT_PX, description.getTextSize());
        scoreView.setTypeface(description.getTypeface());
        scoreView.setTextColor(description.getCurrentTextColor());
        scoreView.setPadding(
                description.getPaddingLeft(), 0, description.getPaddingRight(), 0);
        scoreView.setText(score);
        scoreView.setVisibility(View.VISIBLE);
    }

    private static TextView findScoreView(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof TextView && SCORE_TAG.equals(String.valueOf(child.getTag()))) {
                return (TextView) child;
            }
        }
        return null;
    }

    private static void hide(TextView scoreView) {
        scoreView.setText("");
        scoreView.setVisibility(View.GONE);
    }
}
