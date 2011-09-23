package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FB2LineWhiteSpace extends AbstractFB2LineElement {

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

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(WHITESPACE_ELEMENT_TAG);
        out.writeInt(height);
        out.writeInt(width);
        out.writeBoolean(sizeable);
    }

    public static FB2LineElement deserializeImpl(DataInputStream in) throws IOException {
        int height = in.readInt();
        int width = in.readInt();
        boolean sizeable = in.readBoolean();
        return new FB2LineWhiteSpace(width, height, sizeable);
    }

}
