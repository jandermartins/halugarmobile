package br.ufc.crateus.halugar.Activities.Anuncio;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.MapView;

public class LocalizacaoActivity  extends ViewModel {

    private MapView mapView;

    public MapView getMapView() {
        return mapView;
    }

    public void setMapView(MapView mapView) {
        this.mapView = mapView;
    }
}
