package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;

public class FB2LineWhiteSpace implements FB2LineElement {

    private final int height;
    private int width;
    private final boolean sizeable;

    public FB2LineWhiteSpace(final int width, final int height, final boolean sizeable) {
        this.width = width;
        this.height = height;
        this.sizeable = sizeable;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void render(final Canvas c, final int y, final int x) {
    }

    @Override
    public void adjustWidth(final int w) {
        if (sizeable) {
            width += w;
        }
    }

    @Override
    public boolean isSizeable() {
        return sizeable;
    }

}
