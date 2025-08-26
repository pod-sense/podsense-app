package de.danoeh.antennapod.ui.common;

import android.graphics.Color;
import androidx.palette.graphics.Palette;

/**
 * Utility class for color manipulation and extraction from Palette API
 */
public class ColorUtils {
    
    private static final int[] FALLBACK_COLORS = {
        0xFFFF6F00, // Orange
        0xFF2196F3, // Blue  
        0xFF388E3C, // Green
        0xFF7B1FA2, // Purple
        0xFFB71C1C, // Red
        0xFF00838F, // Cyan
        0xFFFF9800, // Amber
        0xFF4CAF50, // Light Green
        0xFF9C27B0, // Deep Purple
        0xFF3F51B5, // Indigo
        0xFF009688, // Teal
        0xFFE91E63  // Pink
    };
    
    /**
     * Extract the most suitable background color from a Palette
     * @param palette The generated palette from podcast artwork
     * @param fallbackSeed A seed for consistent fallback color selection
     * @return A suitable background color
     */
    public static int extractBackgroundColor(Palette palette, String fallbackSeed) {
        if (palette != null) {
            // Try vibrant colors first (more colorful)
            Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
            if (vibrantSwatch != null) {
                return adjustColorForBackground(vibrantSwatch.getRgb());
            }
            
            // Try light vibrant
            Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
            if (lightVibrantSwatch != null) {
                return adjustColorForBackground(lightVibrantSwatch.getRgb());
            }
            
            // Try dark vibrant
            Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
            if (darkVibrantSwatch != null) {
                return adjustColorForBackground(darkVibrantSwatch.getRgb());
            }
            
            // Try dominant color
            Palette.Swatch dominantSwatch = palette.getDominantSwatch();
            if (dominantSwatch != null) {
                return adjustColorForBackground(dominantSwatch.getRgb());
            }
            
            // Try muted colors as backup
            Palette.Swatch mutedSwatch = palette.getMutedSwatch();
            if (mutedSwatch != null) {
                return adjustColorForBackground(mutedSwatch.getRgb());
            }
            
            // Try light muted
            Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
            if (lightMutedSwatch != null) {
                return adjustColorForBackground(lightMutedSwatch.getRgb());
            }
            
            // Try dark muted
            Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();
            if (darkMutedSwatch != null) {
                return adjustColorForBackground(darkMutedSwatch.getRgb());
            }
        }
        
        // Fallback to a consistent color based on seed, but ensure variety
        return getFallbackColor(fallbackSeed);
    }
    
    /**
     * Get a fallback color that ensures variety across different seeds
     */
    private static int getFallbackColor(String fallbackSeed) {
        if (fallbackSeed == null || fallbackSeed.isEmpty()) {
            return adjustColorForBackground(FALLBACK_COLORS[0]);
        }
        
        // Use a more sophisticated approach to ensure variety
        int hash = fallbackSeed.hashCode();
        int index = Math.abs(hash) % FALLBACK_COLORS.length;
        
        // Add some additional randomization based on string length and characters
        int additionalOffset = (fallbackSeed.length() + fallbackSeed.charAt(0)) % FALLBACK_COLORS.length;
        index = (index + additionalOffset) % FALLBACK_COLORS.length;
        
        return adjustColorForBackground(FALLBACK_COLORS[index]);
    }
    
    /**
     * Adjust a color to be more suitable as a background
     * Makes it more muted and adds transparency, but less aggressive than before
     */
    private static int adjustColorForBackground(int color) {
        // Make the color more muted by reducing saturation
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        
        // Less aggressive saturation and brightness reduction
        hsv[1] = Math.max(0.4f, hsv[1] * 0.8f); // Less saturation reduction
        hsv[2] = Math.max(0.5f, hsv[2] * 0.9f); // Less brightness reduction
        
        int adjustedColor = Color.HSVToColor(hsv);
        
        // Add transparency for a subtle effect (75% opacity - less transparent)
        return Color.argb(190, Color.red(adjustedColor), Color.green(adjustedColor), Color.blue(adjustedColor));
    }
    
    /**
     * Create a gradient-like effect by providing a slightly darker version of the color
     */
    public static int darkenColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor; // Reduce brightness
        return Color.HSVToColor(Color.alpha(color), hsv);
    }
}
