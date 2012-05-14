package org.emdev.ui.progress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.emdev.utils.FileUtils;
import org.emdev.utils.FileUtils.CopingProgress;

public class UIFileCopying implements CopingProgress {

    private final int stringId;
    private final int bufsize;
    private final IProgressIndicator delegate;

    private long contentLength;
    private long copied;
    private long indicated;

    public UIFileCopying(final int stringId, final int bufsize, final IProgressIndicator delegate) {
        this.stringId = stringId;
        this.delegate = delegate;
        this.bufsize = bufsize;
    }

    public void copy(final long contentLength, final InputStream source, final OutputStream target) throws IOException {
        this.contentLength = contentLength;
        this.copied = 0;
        this.indicated = 0;

        FileUtils.copy(source, target, bufsize, this);

        final String fileSize = FileUtils.getFileSize(contentLength);
        delegate.setProgressDialogMessage(stringId, fileSize, fileSize);
    }

    @Override
    public void progress(final long bytes) {
        copied = bytes;
        if (copied - indicated >= bufsize) {
            indicated = copied;
            final String val1 = FileUtils.getFileSize(Math.min(indicated, contentLength));
            final String val2 = FileUtils.getFileSize(contentLength);
            delegate.setProgressDialogMessage(stringId, val1, val2);
        }

    }
}
