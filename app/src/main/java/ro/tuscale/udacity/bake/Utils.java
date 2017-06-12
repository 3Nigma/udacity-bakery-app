package ro.tuscale.udacity.bake;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InterruptedIOException;

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

    public static boolean isInternetConnected() {
        boolean isIt = false;
        Runtime runtime = Runtime.getRuntime();

        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();

            isIt = (exitValue == 0);
        } catch (IOException | InterruptedException ignored) {
        }

        return isIt;
    }
}
