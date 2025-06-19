package com.example.proyectotitulacion;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment {

    private GoogleMap gMap;
    private final LatLng yidamUbicacion = new LatLng(19.26495, -98.89733);
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private Marker markerUsuario;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private NestedScrollView nestedScrollView;

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private final OnMapReadyCallback callback = googleMap -> {
        gMap = googleMap;

        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(true);
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Marcador personalizado
        Bitmap iconoYidam = BitmapFactory.decodeResource(getResources(), R.drawable.consultorio);
        Bitmap iconoYidamEscalado = Bitmap.createScaledBitmap(iconoYidam, 64, 64, false);

        gMap.addMarker(new MarkerOptions()
                .position(yidamUbicacion)
                .title("Centro YIDAM")
                .icon(BitmapDescriptorFactory.fromBitmap(iconoYidamEscalado)));

        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST);
            return;
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mostrarDialogoActivarGPS();
        } else {
            gMap.setMyLocationEnabled(true);
            iniciarActualizacionesDeUbicacion();
        }
    };

    private void iniciarActualizacionesDeUbicacion() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng nuevaUbicacion = new LatLng(location.getLatitude(), location.getLongitude());

                if (markerUsuario == null) {
                    Bitmap iconoPersona = BitmapFactory.decodeResource(getResources(), R.drawable.personita);
                    Bitmap iconoEscalado = Bitmap.createScaledBitmap(iconoPersona, 64, 64, false);

                    markerUsuario = gMap.addMarker(new MarkerOptions()
                            .position(nuevaUbicacion)
                            .title("Tu ubicación")
                            .icon(BitmapDescriptorFactory.fromBitmap(iconoEscalado)));

                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nuevaUbicacion, 16));
                } else {
                    markerUsuario.setPosition(nuevaUbicacion);
                }
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Toast.makeText(requireContext(), "GPS desactivado", Toast.LENGTH_SHORT).show();
                mostrarDialogoActivarGPS();
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Toast.makeText(requireContext(), "GPS activado", Toast.LENGTH_SHORT).show();
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000,
                    1,
                    locationListener
            );
        }
    }

    private void mostrarDialogoActivarGPS() {
        new AlertDialog.Builder(requireContext())
                .setTitle("GPS desactivado")
                .setMessage("Por favor activa la ubicación para usar esta función.")
                .setPositiveButton("Activar", (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            nestedScrollView = getActivity().findViewById(R.id.scrollView);

            TouchableMapFragment mapFragment = new TouchableMapFragment();
            mapFragment.setOnTouchListener(() -> {
                if (nestedScrollView != null) {
                    nestedScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.mapsFragmentContainer, mapFragment)
                    .commit();

            mapFragment.getMapAsync(callback);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al cargar el mapa: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (gMap != null) {
                    gMap.setMyLocationEnabled(true);
                }
                iniciarActualizacionesDeUbicacion();
            } else {
                Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
