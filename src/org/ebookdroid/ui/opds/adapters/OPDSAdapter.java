package org.ebookdroid.ui.opds.adapters;

import org.ebookdroid.R;
import org.ebookdroid.common.cache.CacheManager;
import org.ebookdroid.common.cache.ThumbnailFile;
import org.ebookdroid.opds.Book;
import org.ebookdroid.opds.Entry;
import org.ebookdroid.opds.Feed;
import org.ebookdroid.opds.OPDSClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import org.emdev.ui.adapters.BaseViewHolder;
import org.emdev.ui.widget.TextViewMultilineEllipse;
import org.emdev.utils.LengthUtils;
import org.emdev.utils.listeners.ListenerProxy;

public class OPDSAdapter extends BaseAdapter {

    private final Context context;
    private final OPDSClient client;
    private final List<? extends Entry> rootFeeds;

    private Feed currentFeed;

    private final ListenerProxy listeners = new ListenerProxy(FeedListener.class);

    public OPDSAdapter(final Context context, final Feed... feeds) {
        this.context = context;
        this.client = new OPDSClient();

        this.rootFeeds = Arrays.asList(feeds);
        this.currentFeed = null;
    }

    @Override
    protected void finalize() {
        close();
    }

    public void close() {
        client.close();
    }

    public void setCurrentFeed(final Feed feed) {
        if (feed != null && feed == this.currentFeed) {
            feed.books.clear();
            feed.children.clear();
            feed.loadedAt = 0;
        }
        this.currentFeed = feed;

        notifyDataSetInvalidated();

        if (feed != null && feed.loadedAt == 0) {
            new LoadTask().execute(feed);
        }
    }

    public Feed getCurrentFeed() {
        return currentFeed;
    }

    @Override
    public int getCount() {
        return currentFeed != null ? currentFeed.children.size() + currentFeed.books.size() : rootFeeds.size();
    }

    @Override
    public Entry getItem(final int i) {
        if (currentFeed == null) {
            return rootFeeds.get(i);
        }
        if (i < currentFeed.children.size()) {
            return currentFeed.children.get(i);
        }
        final int bookindex = i - currentFeed.children.size();
        if (bookindex < currentFeed.books.size()) {
            return currentFeed.books.get(bookindex);
        }
        return null;
    }

    @Override
    public long getItemId(final int i) {
        return i;
    }

    @Override
    public View getView(final int i, final View view, final ViewGroup parent) {

        final ViewHolder holder = BaseViewHolder.getOrCreateViewHolder(ViewHolder.class, R.layout.opdsitem, view,
                parent);

        final Entry entry = getItem(i);

        holder.textView.setText(entry.title);

        if (entry instanceof Feed) {
            holder.imageView.setImageResource(R.drawable.folderopen);
        } else {
            final ThumbnailFile thumbnailFile = CacheManager.getThumbnailFile(entry.id);
            if (thumbnailFile.exists()) {
                holder.imageView.setImageBitmap(thumbnailFile.getRawImage());
            } else {
                holder.imageView.setImageResource(R.drawable.book);
            }
        }

        if (entry.content != null) {
            final String decoded = URLDecoder.decode(entry.content.content);
            holder.info.setText(Html.fromHtml(decoded));
        } else {
            holder.info.setText("");
        }

        return holder.getView();
    }

    protected void loadBookThumbnail(final Book book) {
        if (book.thumbnail == null) {
            return;
        }
        final ThumbnailFile thumbnailFile = CacheManager.getThumbnailFile(book.id);
        if (thumbnailFile.exists()) {
            return;
        }

        try {
            final File file = client.loadFile(book.thumbnail);
            if (file == null) {
                return;
            }

            final Options opts = new Options();
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            opts.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(new FileInputStream(file), null, opts);

            opts.inSampleSize = getScale(opts, 200, 200);
            opts.inJustDecodeBounds = false;

            final Bitmap image = BitmapFactory.decodeStream(new FileInputStream(file), null, opts);
            if (image != null) {
                thumbnailFile.setImage(image);
                image.recycle();
            }
        } catch (final Throwable ex) {
            ex.printStackTrace();
        }
    }

    protected int getScale(final Options opts, final float requiredWidth, final float requiredHeight) {
        int scale = 1;
        int widthTmp = opts.outWidth;
        int heightTmp = opts.outHeight;
        while (true) {
            if (widthTmp / 2 < requiredWidth || heightTmp / 2 < requiredHeight) {
                break;
            }
            widthTmp /= 2;
            heightTmp /= 2;

            scale *= 2;
        }
        return scale;
    }

    public void addListener(final FeedListener listener) {
        listeners.addListener(listener);
    }

    public void removeListener(final FeedListener listener) {
        listeners.removeListener(listener);
    }

    public static class ViewHolder extends BaseViewHolder {

        TextView textView;
        ImageView imageView;
        TextViewMultilineEllipse info;

        @Override
        public void init(final View convertView) {
            super.init(convertView);
            textView = (TextView) convertView.findViewById(R.id.opdsItemText);
            imageView = (ImageView) convertView.findViewById(R.id.opdsItemIcon);
            info = (TextViewMultilineEllipse) convertView.findViewById(R.id.opdsDescription);
        }
    }

    final class LoadTask extends AsyncTask<Feed, String, Feed> implements OnCancelListener {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            onProgressUpdate("Loading...");
        }

        @Override
        public void onCancel(final DialogInterface dialog) {
            this.cancel(true);
        }

        @Override
        protected Feed doInBackground(final Feed... params) {
            final Feed feed = client.load(params[0]);
            for (final Book book : feed.books) {
                loadBookThumbnail(book);
            }
            return feed;
        }

        @Override
        protected void onPostExecute(final Feed result) {
            if (progressDialog != null) {
                try {
                    progressDialog.dismiss();
                } catch (final Throwable th) {
                }
            }

            final FeedListener l = listeners.getListener();
            l.feedLoaded(result);

            notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(final String... values) {
            final int length = LengthUtils.length(values);
            if (length == 0) {
                return;
            }
            final String last = values[length - 1];
            if (progressDialog == null || !progressDialog.isShowing()) {
                progressDialog = ProgressDialog.show(context, "", last, true);
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.setOnCancelListener(this);
            } else {
                progressDialog.setMessage(last);
            }
        }
    }

    public static interface FeedListener {

        void feedLoaded(Feed feed);
    }
}
