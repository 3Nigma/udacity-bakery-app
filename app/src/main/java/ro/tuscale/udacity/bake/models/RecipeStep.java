package ro.tuscale.udacity.bake.models;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

public class RecipeStep {
    @SerializedName("id")
    private int mId;
    @SerializedName("shortDescription")
    private String mShortDescription;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("videoURL")
    private String mVideoUrl;
    @SerializedName("thumbnailURL")
    private String mThumbnailUrl;

    public int getId() {
        return mId;
    }
    public String getShortDescription() {
        return mShortDescription;
    }
    public String getDescription() {
        return mDescription;
    }
    public Uri getVideoUri() {
        return Uri.parse(mVideoUrl);
    }
    public String getThumbnailAddress() {
        return mThumbnailUrl;
    }
}
