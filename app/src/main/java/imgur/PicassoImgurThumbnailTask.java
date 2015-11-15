package imgur;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class PicassoImgurThumbnailTask extends AsyncTask<Void, Void, Void> {
    List<ImgurImage> imageList;
    ImageView iView;
    String galleryId;
    Context applicationContext;

    public PicassoImgurThumbnailTask(ImageView iView, String galleryId,
                                     Context applicationContext) {
        super();
        this.iView = iView;
        this.galleryId = galleryId;
        this.applicationContext = applicationContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        imageList = ImgurGalleryFetcher.getImagesFromGallery(galleryId, true, false);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (imageList != null) {
            Picasso.with(applicationContext).load(imageList
                    .get(0).getLink()).fit().into(iView);
        }
    }
}
