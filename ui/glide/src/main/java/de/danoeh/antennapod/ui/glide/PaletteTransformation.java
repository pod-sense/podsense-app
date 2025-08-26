package de.danoeh.antennapod.ui.glide;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import java.security.MessageDigest;

/**
 * Glide transformation that extracts the dominant color from an image using Android Palette API
 * and stores it for later use in UI components.
 */
public class PaletteTransformation extends BitmapTransformation {
    private static final String ID = "de.danoeh.antennapod.ui.glide.PaletteTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);
    
    public interface PaletteCallback {
        void onPaletteGenerated(Palette palette);
    }
    
    private final PaletteCallback callback;
    
    public PaletteTransformation(PaletteCallback callback) {
        this.callback = callback;
    }
    
    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        // Generate palette from the bitmap
        if (callback != null && toTransform != null && !toTransform.isRecycled()) {
            try {
                // Generate palette with more aggressive settings to extract colors
                Palette.from(toTransform)
                    .maximumColorCount(32) // Increase color count for better extraction
                    .generate(palette -> {
                        if (palette != null) {
                            callback.onPaletteGenerated(palette);
                        } else {
                            // Palette generation failed, callback with null
                            callback.onPaletteGenerated(null);
                        }
                    });
            } catch (Exception e) {
                // If palette generation fails, callback with null to trigger fallback
                callback.onPaletteGenerated(null);
            }
        }
        
        // Return the original bitmap unchanged
        return toTransform;
    }
    
    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof PaletteTransformation;
    }
    
    @Override
    public int hashCode() {
        return ID.hashCode();
    }
}
