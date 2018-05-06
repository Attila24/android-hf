package hf.thewalkinglife.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class MapTextIconFactory {
    public static BitmapDescriptor createPureTextIcon(String text) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(60);
        textPaint.setColor(Color.BLUE);

        int textWidth = (int) textPaint.measureText(text);
        int textHeight = (int) textPaint.getTextSize();

        Bitmap image = Bitmap.createBitmap(textWidth, textHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        canvas.translate(0, textHeight);
        canvas.drawText(text, 0, 0, textPaint);
        return BitmapDescriptorFactory.fromBitmap(image);
    }
}
