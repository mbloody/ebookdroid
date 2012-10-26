package org.emdev.common.textmarkup;


import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.line.Line;


public class MarkupEndDocument implements MarkupElement {

    @Override
    public void publishToLines(ArrayList<Line> lines, LineCreationParams params) {
    }

    @Override
    public void publishToStream(DataOutputStream out) throws IOException {
        write(out);
    }

    public static void write(DataOutputStream out) throws IOException {
        out.writeByte(MarkupTag.MarkupEndDocument.ordinal());
    }
}
