package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;

public interface FB2LineElement extends FB2MarkupElement {

    int getHeight();

    int getWidth();

    void render(Canvas c, int y, int x);

    void adjustWidth(int w);

    boolean isSizeable();

}
