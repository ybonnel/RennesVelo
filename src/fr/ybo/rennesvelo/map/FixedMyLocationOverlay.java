/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.ybo.rennesvelo.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Projection;
import fr.ybo.rennesvelo.R;

public class FixedMyLocationOverlay extends MyLocationOverlay {
    private boolean bugged;

    private Paint accuracyPaint;
    private Point center;
    private Point left;
    private Drawable drawable;
    private int width;
    private int height;

    public FixedMyLocationOverlay(Context context, MapView mapView) {
        super(context, mapView);
    }

    @Override
    protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLoc, long when) {
        if (!bugged) {
            try {
                super.drawMyLocation(canvas, mapView, lastFix, myLoc, when);
            } catch (Exception ignore) {
                bugged = true;
            }
        }

        if (bugged) {
            if (drawable == null) {
                accuracyPaint = new Paint();
                accuracyPaint.setAntiAlias(true);
                accuracyPaint.setStrokeWidth(2.0f);

                drawable = mapView.getContext().getResources().getDrawable(R.drawable.mylocation);
                width = drawable.getIntrinsicWidth();
                height = drawable.getIntrinsicHeight();
                center = new Point();
                left = new Point();
            }
            Projection projection = mapView.getProjection();

            double latitude = lastFix.getLatitude();
            double longitude = lastFix.getLongitude();
            float accuracy = lastFix.getAccuracy();

            float[] result = new float[1];

            Location.distanceBetween(latitude, longitude, latitude, longitude + 1, result);
            float longitudeLineDistance = result[0];

            GeoPoint leftGeo = new GeoPoint((int) (latitude * 1.0e6), (int) ((longitude - accuracy
                    / longitudeLineDistance) * 1.0e6));
            projection.toPixels(leftGeo, left);
            projection.toPixels(myLoc, center);
            int radius = center.x - left.x;

            accuracyPaint.setColor(0xff6666ff);
            accuracyPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

            accuracyPaint.setColor(0x186666ff);
            accuracyPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

            drawable.setBounds(center.x - width / 2, center.y - height / 2, center.x + width / 2, center.y + height / 2);
            drawable.draw(canvas);
        }
    }

}
