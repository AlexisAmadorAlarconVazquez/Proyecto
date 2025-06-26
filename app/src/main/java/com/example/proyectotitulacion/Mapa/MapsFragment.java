package com.example.proyectotitulacion.Mapa;
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
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.proyectotitulacion.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
public class MapsFragment extends Fragment implements OnMapReadyCallback {

    // Variables del mapa y ubicación
    private GoogleMap gMap;
    private final LatLng yidamUbicacion = new LatLng(19.26495, -98.89733); // Ubicación fija
    private static final int LOCATION_PERMISSION_REQUEST = 100; // Código para permisos
    private Marker markerUsuario; // Marcador para la ubicación del usuario
    private LocationManager locationManager; // Para servicios de ubicación
    private LocationListener locationListener; // Para escuchar cambios de ubicación
    private NestedScrollView nestedScrollView; // Para el scroll que contiene el mapa

    // Crea la vista del fragmento.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    // Configura la vista después de ser creada.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            nestedScrollView = requireActivity().findViewById(R.id.scrollView);

            // Obtiene o crea el fragmento del mapa (TouchableMapFragment).
            TouchableMapFragment mapFragment = (TouchableMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapsFragmentContainer);

            if (mapFragment == null) {
                mapFragment = new TouchableMapFragment();
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.mapsFragmentContainer, mapFragment)
                        .commit();
            }

            // Configura el listener para que el mapa maneje el scroll.
            if (mapFragment != null) {
                mapFragment.setOnTouchListener(new TouchableMapFragment.OnTouchListener() {
                    @Override
                    public void onTouchStarted() {
                        if (nestedScrollView != null) {
                            // Evita que el NestedScrollView intercepte el scroll del mapa.
                            nestedScrollView.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                });

                mapFragment.getMapAsync(this); // Prepara el mapa.
            } else {
                // Manejo de error si el fragmento del mapa no se puede inicializar.
                if (getContext() != null) {
                    Toast.makeText(getContext(), "No se pudo inicializar el fragmento del mapa.", Toast.LENGTH_LONG).show();
                }
            }

        } catch (IllegalStateException e) {
            // Manejo de errores de estado del fragmento.
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error de estado del fragmento: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e) {
            // Manejo de errores generales al cargar el mapa.
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al cargar el mapa: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    // Se ejecuta cuando el mapa está listo.
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        // Configuración inicial del mapa.
        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(true);
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Añade marcador para "Centro YIDAM".
        Bitmap iconoYidam = BitmapFactory.decodeResource(getResources(), R.drawable.consultorio);
        Bitmap iconoYidamEscalado = Bitmap.createScaledBitmap(iconoYidam, 64, 64, false);
        gMap.addMarker(new MarkerOptions()
                .position(yidamUbicacion)
                .title("Centro YIDAM")
                .icon(BitmapDescriptorFactory.fromBitmap(iconoYidamEscalado)));

        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        // Verifica y solicita permisos de ubicación.
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST);
            return;
        }

        // Habilita la capa "Mi Ubicación".
        gMap.setMyLocationEnabled(true);

        // Verifica si el GPS está activado e inicia actualizaciones de ubicación.
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mostrarDialogoActivarGPS();
        } else if (locationManager != null) {
            iniciarActualizacionesDeUbicacion();
        } else {
            // Manejo de error si el servicio de ubicación no está disponible.
            if (getContext() != null) {
                Toast.makeText(getContext(), "Servicio de ubicación no disponible.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Inicia la escucha de cambios de ubicación del usuario.
    private void iniciarActualizacionesDeUbicacion() {
        // Verificaciones previas.
        if (getContext() == null || locationManager == null || gMap == null) return;
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        locationListener = new LocationListener() {
            // Se ejecuta cuando cambia la ubicación.
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (getContext() == null || getResources() == null || gMap == null) return;
                LatLng nuevaUbicacion = new LatLng(location.getLatitude(), location.getLongitude());

                // Crea o actualiza el marcador del usuario.
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

            // Se ejecuta si el proveedor de ubicación se deshabilita (ej. GPS apagado).
            @Override
            public void onProviderDisabled(@NonNull String provider) {
                if (getContext() == null) return;
                Toast.makeText(requireContext(), "GPS desactivado", Toast.LENGTH_SHORT).show();
                mostrarDialogoActivarGPS();
            }

            // Se ejecuta si el proveedor de ubicación se habilita.
            @Override
            public void onProviderEnabled(@NonNull String provider) {
                if (getContext() == null) return;
                Toast.makeText(requireContext(), "GPS activado", Toast.LENGTH_SHORT).show();
            }
        };

        // Solicita actualizaciones de ubicación.
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000, // Intervalo mínimo de tiempo (ms)
                1,    // Distancia mínima (m)
                locationListener
        );
    }

    // Muestra un diálogo para que el usuario active el GPS.
    private void mostrarDialogoActivarGPS() {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("GPS desactivado")
                .setMessage("Por favor activa la ubicación para usar esta función.")
                .setPositiveButton("Activar", (dialog, which) -> {
                    if (getContext() != null) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Maneja el resultado de la solicitud de permisos.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (getContext() == null) return;
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si se conceden los permisos, configura la ubicación.
                if (gMap != null) {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        gMap.setMyLocationEnabled(true);
                        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            mostrarDialogoActivarGPS();
                        } else if (locationManager != null) {
                            iniciarActualizacionesDeUbicacion();
                        }
                    }
                }
            } else {
                // Si se deniegan los permisos.
                Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Se ejecuta cuando la vista del fragmento se destruye.
    @Override
    public void onDestroyView() {
        // Detiene las actualizaciones de ubicación para liberar recursos.
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        super.onDestroyView();
    }
}