/*
 * Copyright 2014 Some Dev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.uncod.android.bypass;

import in.uncod.android.bypass.Element.Type;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;

public class Bypass {
    static {
        System.loadLibrary("bypass");
    }

    private static final float[] HEADER_SIZES = { 1.5f, 1.4f, 1.3f, 1.2f, 1.1f,
            1f, };

    public CharSequence markdownToSpannable(String markdown) {
        Document document = processMarkdown(markdown);

        CharSequence[] spans = new CharSequence[document.getElementCount()];
        for (int i = 0; i < document.getElementCount(); i++) {
            spans[i] = recurseElement(document.getElement(i));
        }

        return TextUtils.concat(spans);
    }

    private native Document processMarkdown(String markdown);

    private CharSequence recurseElement(Element element) {

        CharSequence[] spans = new CharSequence[element.size()];
        for (int i = 0; i < element.size(); i++) {
            spans[i] = recurseElement(element.children[i]);
        }

        CharSequence concat = TextUtils.concat(spans);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        String text = element.getText();
        if (element.size() == 0
                && element.getParent().getType() != Type.BLOCK_CODE) {
            text = text.replace('\n', ' ');
        }
        if (element.getParent() != null
                && element.getParent().getType() == Type.LIST_ITEM
                && element.getType() == Type.LIST) {
            builder.append("\n");
        }
        if (element.getType() == Type.LIST_ITEM) {
            builder.append("\u2022");
        }
        builder.append(text);
        builder.append(concat);
        if (element.getType() == Type.LIST && element.getParent() != null) {

        } else if (element.getType() == Type.LIST_ITEM) {
            if (element.size() > 0 && element.children[element.size()-1].isBlockElement()) {

            }
            else {
                builder.append("\n");
            }
        } else if (element.isBlockElement()) {
            builder.append("\n\n");
        }

        if (element.getType() == Type.HEADER) {
            String levelStr = element.getAttribute("level");
            int level = Integer.parseInt(levelStr);
            builder.setSpan(new RelativeSizeSpan(HEADER_SIZES[level]), 0,
                    builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (element.getType() == Type.LIST_ITEM
                && element.getParent().getParent() != null) {
            LeadingMarginSpan span = new LeadingMarginSpan.Standard(20);
            builder.setSpan(span, 0, builder.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (element.getType() == Type.EMPHASIS) {
            StyleSpan italicSpan = new StyleSpan(Typeface.ITALIC);
            builder.setSpan(italicSpan, 0, builder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (element.getType() == Type.DOUBLE_EMPHASIS) {
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            builder.setSpan(boldSpan, 0, builder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (element.getType() == Type.TRIPLE_EMPHASIS) {
            StyleSpan bolditalicSpan = new StyleSpan(Typeface.BOLD_ITALIC);
            builder.setSpan(bolditalicSpan, 0, builder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (element.getType() == Type.CODE_SPAN) {
            TypefaceSpan monoSpan = new TypefaceSpan("monospace");
            builder.setSpan(monoSpan, 0, builder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (element.getType() == Type.LINK) {
            URLSpan urlSpan = new URLSpan(element.getAttribute("link"));
            builder.setSpan(urlSpan, 0, builder.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (element.getType() == Type.BLOCK_QUOTE) {
            QuoteSpan quoteSpan = new QuoteSpan();
            builder.setSpan(quoteSpan, 0, builder.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            StyleSpan italicSpan = new StyleSpan(Typeface.ITALIC);
            builder.setSpan(italicSpan, 0, builder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }
}