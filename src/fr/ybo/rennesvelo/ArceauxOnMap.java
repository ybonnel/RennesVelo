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
package fr.ybo.rennesvelo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import fr.ybo.opendata.rennes.DonneesKml;
import fr.ybo.opendata.rennes.exceptions.KeolisReseauException;
import fr.ybo.opendata.rennes.modele.velos.Arceau;
import fr.ybo.rennesvelo.map.FixedMyLocationOverlay;
import fr.ybo.rennesvelo.map.MapItemizedOverlayVelo;
import fr.ybo.rennesvelo.util.TacheAvecProgressDialog;

import java.util.List;

public class ArceauxOnMap extends MapActivity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        final MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);

        MapController mc = mapView.getController();
        // Creation du geo point
        final List<Overlay> mapOverlays = mapView.getOverlays();

        myLocationOverlay = new FixedMyLocationOverlay(this, mapView);
        mapOverlays.add(myLocationOverlay);
        myLocationOverlay.enableMyLocation();
        mc.setCenter(new GeoPoint(48109681, -1679277));
        mc.setZoom(14);
        gestionButtonLayout();

        new TacheAvecProgressDialog<Void, Void, List<Arceau>>(this, getString(R.string.recupArceaux)) {
            @Override
            protected List<Arceau> myDoBackground() throws KeolisReseauException {
                return new DonneesKml().getArceaux();
            }

            @Override
            protected void onPostExecute(List<Arceau> result) {
                if (result != null) {
                    Drawable drawable = getResources().getDrawable(R.drawable.markee_velo);
                    MapItemizedOverlayVelo itemizedoverlay = new MapItemizedOverlayVelo(drawable);
                    for (Arceau arceau : result) {
                        int latitude = (int) (arceau.getLatitude() * 1.0E6);
                        int longitude = (int) (arceau.getLongitude() * 1.0E6);
                        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                        OverlayItem overlayitem = new OverlayItem(geoPoint, "Arceau", "Arceau");
                        itemizedoverlay.addOverlay(overlayitem);
                    }
                    mapOverlays.add(itemizedoverlay);
                    mapView.invalidate();
                }
                super.onPostExecute(result);
            }
        }.execute((Void)null);
    }

    private boolean satelite;

    private void gestionButtonLayout() {
        final MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setSatellite(false);
        ImageButton layoutButton = (ImageButton) findViewById(R.id.layers_button);
        layoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ArceauxOnMap.this);
                String[] items = {"Satellite"};
                boolean[] checkeds = {satelite};
                builder.setMultiChoiceItems(items, checkeds, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        satelite = !satelite;
                        mapView.setSatellite(satelite);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private MyLocationOverlay myLocationOverlay;

    @Override
    protected void onResume() {
        super.onResume();
        myLocationOverlay.enableMyLocation();
    }

    @Override
    protected void onPause() {
        myLocationOverlay.disableMyLocation();
        super.onPause();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
