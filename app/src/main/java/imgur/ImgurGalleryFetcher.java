package imgur;

import com.rael.daniel.drc.util.Consts;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to fetch a list of imgur api links for use with Picasso
 */
public class ImgurGalleryFetcher {

    //Extracts gallery ID from a standard imgur URL found on reddit
    public static String getGalleryId(String url) {
        String tail = url.substring(url.lastIndexOf('/') + 1,
                url.length());
        if(!tail.contains(".")) return tail;
        else return tail.substring(0, tail.lastIndexOf('.'));
    }

    public static List<ImgurImage> getImagesFromGallery(
            String galleryId, boolean getThumbnails, boolean isAlbum) {
        List<ImgurImage> imageList = new ArrayList<>();
        ImgurImage currentImage = new ImgurImage();
        String url;
        String jsonResponse = ImgurConnectionManager.readContents(
                Consts.IMGUR_API_URL + (isAlbum ? "album/" : "gallery/") + galleryId);
        try {
            JSONObject data = new JSONObject(jsonResponse)
                    .getJSONObject("data");
            if(data.optString("is_album").equals("false")) {
                currentImage.setId(data.optString("id"));
                currentImage.setTitle(data.optString("title"));
                currentImage.setDescription(data.optString("description"));
                currentImage.setWidth(data.optString("width"));
                currentImage.setHeight(data.optString("height"));
                currentImage.setType(data.optString("type"));
                currentImage.setLink(data.optString("link"));
                if(getThumbnails) {
                    currentImage.setLink(currentImage.getLink()
                            .replace(currentImage.getId(), currentImage.getId() + "s"));
                }
                imageList.add(currentImage);
                return imageList;
            }
            else { //gallery is album
                int albumSize = Integer.valueOf(data.getString("images_count"));
                JSONArray albumData = data.getJSONArray("images");
                JSONObject childImage;
                for(int i = 0; i < albumSize; i++) {
                    childImage = albumData.getJSONObject(i);
                    currentImage.setId(childImage.optString("id"));
                    currentImage.setTitle(childImage.optString("title"));
                    currentImage.setDescription(childImage.optString("description"));
                    currentImage.setWidth(childImage.optString("width"));
                    currentImage.setHeight(childImage.optString("height"));
                    currentImage.setType(childImage.optString("type"));
                    currentImage.setLink(childImage.optString("link"));
                    if(getThumbnails) {
                        currentImage.setLink(currentImage.getLink()
                                .replace(currentImage.getId(), currentImage.getId() + "s"));
                    }
                    imageList.add(currentImage);
                }
                return imageList;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
