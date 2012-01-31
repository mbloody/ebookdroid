package org.ebookdroid.common.cache;

import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.core.codec.CodecPageInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PageCacheFile extends File {

    private static final long serialVersionUID = 6836895806027391288L;

    private static final LogContext LCTX = CacheManager.LCTX;

    PageCacheFile(final File dir, final String name) {
        super(dir, name);
    }

    public CodecPageInfo[] load() {
        try {
            final DataInputStream in = new DataInputStream(new FileInputStream(this));
            try {
                final int pages = in.readInt();
                final CodecPageInfo[] infos = new CodecPageInfo[pages];

                for (int i = 0; i < infos.length; i++) {
                    final CodecPageInfo cpi = new CodecPageInfo();
                    cpi.width = (in.readInt());
                    cpi.height = (in.readInt());
                    if (cpi.width != -1 && cpi.height != -1) {
                        infos[i] = cpi;
                    }
                }
                return infos;
            } catch (final EOFException ex) {
                LCTX.e("Loading pages cache failed: " + ex.getMessage());
            } catch (final IOException ex) {
                LCTX.e("Loading pages cache failed: " + ex.getMessage());
            } finally {
                try {
                    in.close();
                } catch (final IOException ex) {
                }
            }
        } catch (final FileNotFoundException ex) {
            LCTX.e("Loading pages cache failed: " + ex.getMessage());
        }
        return null;
    }

    public void save(final CodecPageInfo[] infos) {
        try {
            final DataOutputStream out = new DataOutputStream(new FileOutputStream(this));
            try {
                out.writeInt(infos.length);
                for (int i = 0; i < infos.length; i++) {
                    if (infos[i] != null) {
                        out.writeInt(infos[i].width);
                        out.writeInt(infos[i].height);
                    } else {
                        out.writeInt(-1);
                        out.writeInt(-1);
                    }
                }
            } catch (final IOException ex) {
                LCTX.e("Saving pages cache failed: " + ex.getMessage());
            } finally {
                try {
                    out.close();
                } catch (final IOException ex) {
                }
            }
        } catch (final IOException ex) {
            LCTX.e("Saving pages cache failed: " + ex.getMessage());
        }
    }
}
