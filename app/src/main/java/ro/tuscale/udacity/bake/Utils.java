package ro.tuscale.udacity.bake;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

public class Utils {
    public static Picasso getPicasso(@NonNull Context ctx) {
        OkHttpDownloader okHttp = new OkHttpDownloader(ctx);
        Picasso picasso;

        picasso = new Picasso.Builder(ctx)
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        // TODO: now what?
                        Timber.w("Failed to load image: ", uri);
                    }
                })
                .downloader(okHttp)
                .build();

        return picasso;
    }
}
