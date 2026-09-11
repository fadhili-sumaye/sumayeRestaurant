package com.example.sumayerestaurant.util;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.sumayerestaurant.R;

import java.util.Locale;

public class ImageHelper {

    /**
     * Resolves an image URL so it works seamlessly on physical devices and emulators.
     * Rewrites localhost/10.0.2.2/old LAN IPs to the active Constants.BASE_URL host.
     */
    public static String resolveImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        url = url.trim();

        // If the URL is already pointing to localhost or emulator IP, rewrite it to active BASE_URL host
        if (url.contains("localhost:8080") || url.contains("127.0.0.1:8080") || url.contains("10.0.2.2:8080")) {
            int pathStart = url.indexOf(":8080");
            if (pathStart != -1) {
                String relativePath = url.substring(pathStart + 5);
                return joinBaseAndPath(Constants.BASE_URL, relativePath);
            }
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        return joinBaseAndPath(Constants.BASE_URL, url);
    }

    private static String joinBaseAndPath(String base, String path) {
        if (base == null) base = "";
        if (path == null) path = "";

        if (base.endsWith("/") && path.startsWith("/")) {
            return base.substring(0, base.length() - 1) + path;
        }
        if (!base.endsWith("/") && !path.startsWith("/")) {
            return base + "/" + path;
        }
        return base + path;
    }

    /**
     * Maps known food items to local high-resolution drawables bundled with the app.
     * Provides an instant, offline-ready fallback when backend images are loading or fail.
     */
    @DrawableRes
    public static int getFoodFallbackDrawable(String imageUrl, String foodName) {
        String query = "";
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            query += imageUrl.toLowerCase(Locale.ROOT) + " ";
        }
        if (foodName != null && !foodName.trim().isEmpty()) {
            query += foodName.toLowerCase(Locale.ROOT);
        }

        if (query.contains("pilau")) {
            return R.drawable.food_pilau_kuku;
        } else if (query.contains("ugali")) {
            return R.drawable.food_ugali_samaki;
        } else if (query.contains("chips")) {
            return R.drawable.food_chips_kuku;
        } else if (query.contains("biryani") || query.contains("ngombe") || query.contains("ng'ombe")) {
            return R.drawable.food_biryani_ngombe;
        } else if (query.contains("chai") || query.contains("maziwa")) {
            return R.drawable.food_chai_ya_maziwa;
        } else if (query.contains("juisi") || query.contains("embe")) {
            return R.drawable.food_juisi_ya_embe;
        } else if (query.contains("soda")) {
            return R.drawable.food_soda;
        } else if (query.contains("maji")) {
            return R.drawable.food_maji_ya_kunywa;
        } else if (query.contains("sambusa")) {
            return R.drawable.food_sambusa_ya_nyama;
        } else if (query.contains("mandazi")) {
            return R.drawable.food_mandazi;
        }

        return R.drawable.placeholder_food;
    }

    /**
     * Loads food image with Glide using the local high-res food image as placeholder and fallback.
     */
    public static void loadFoodImage(Context context, String imageUrl, String foodName, ImageView imageView, int cornerRadius) {
        if (context == null || imageView == null) return;

        int fallbackRes = getFoodFallbackDrawable(imageUrl, foodName);
        String resolvedUrl = resolveImageUrl(imageUrl);

        Object loadSource = (resolvedUrl != null && !resolvedUrl.isEmpty()) ? resolvedUrl : fallbackRes;

        if (cornerRadius > 0) {
            Glide.with(context)
                    .load(loadSource)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new CenterCrop(), new RoundedCorners(cornerRadius))
                    .placeholder(fallbackRes)
                    .error(fallbackRes)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .load(loadSource)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new CenterCrop())
                    .placeholder(fallbackRes)
                    .error(fallbackRes)
                    .into(imageView);
        }
    }
}
