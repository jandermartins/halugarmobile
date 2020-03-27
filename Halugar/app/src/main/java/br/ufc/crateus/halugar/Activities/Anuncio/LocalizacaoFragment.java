package br.ufc.crateus.halugar.Activities.Anuncio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.Distancia;
import br.ufc.crateus.halugar.Util.InternetCheck;

public class LocalizacaoFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    double latitudeAnuncio, longitudeAnuncio;
    double latitudeUsuario, longitudeUsuario;

    Bundle arguments;

    MapView mMapView;
    private GoogleMap googleMap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arguments = getArguments();

        if(arguments==null){
            Log.i("AQUI - FRAGMENT", "arguments==null");
            return;
        }

        // Recebendo as informações a AnuncioActivity:
        Log.i("TESTE - LOCALIZACAO = ", arguments.toString());

        latitudeAnuncio = arguments.getDouble("LatitudeAnuncio");
        longitudeAnuncio = arguments.getDouble("LongitudeAnuncio");
        latitudeUsuario = arguments.getDouble("LatitudeUsuario");
        longitudeUsuario = arguments.getDouble("LongitudeUsuario");

        if(latitudeAnuncio==0 && longitudeAnuncio==0){
            CustomToast.mostrarMensagem(getActivity().getApplicationContext(), "Falha ao localizar endereço", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getActivity().getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_LONG);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_localizacao, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                double kilometros = Distancia.calcularDistancia(latitudeUsuario, longitudeUsuario, latitudeAnuncio, longitudeAnuncio);
                String strKm = String.format("%.2f", kilometros);

                Log.i("MAPA", "km = " + kilometros);

                LatLng usuario = new LatLng(latitudeUsuario, longitudeUsuario);
                LatLng local = new LatLng(latitudeAnuncio, longitudeAnuncio);

                if(latitudeAnuncio!=0 && longitudeAnuncio!=0){

                    // For dropping a marker at a point on the Map
                    googleMap.addMarker(new MarkerOptions().
                            position(usuario).
                            title("Você está aqui")
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_aqui_foreground)))
                            .showInfoWindow();

                    googleMap.addMarker(new MarkerOptions()
                            .position(local)
                            .title("HáLugar")
                            .snippet("Distância: " + strKm + " km")
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_foreground))
                    ).showInfoWindow();

                    CameraPosition cameraPosition = new CameraPosition.Builder().target(local).zoom(15).build();
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                else{

                    // For dropping a marker at a point on the Map
                    googleMap.addMarker(new MarkerOptions().
                            position(usuario).
                            title("Você está aqui")
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_aqui_foreground)))
                            .showInfoWindow();

                    CameraPosition cameraPosition = new CameraPosition.Builder().target(usuario).zoom(15).build();
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap = googleMap;

        googleMap.setMinZoomPreference(15); // Quanto menor o valor, mais afastado...
        googleMap.setMaxZoomPreference(30);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }
}
