package com.example.proyectotitulacion.Mapa;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.SupportMapFragment;

public class TouchableMapFragment extends SupportMapFragment {

    // Listener para notificar cuando el usuario comienza a tocar el mapa.
    private OnTouchListener mListener;
    // Listener opcional para notificar cuando la interacción táctil con el mapa ha finalizado.
    private OnMapInteractionCompleteListener mInteractionCompleteListener;

    //Interfaz para ser notificado cuando un evento táctil (ACTION_DOWN) ocurre en el mapa.

    public interface OnTouchListener {
        void onTouchStarted();
    }

    // Interfaz opcional para ser notificado cuando una interacción táctil (ACTION_UP o ACTION_CANCEL) en el mapa ha terminado.
    public interface OnMapInteractionCompleteListener {
        void onTouchEnded();
    }

    // Establece el listener que será notificado cuando el usuario comience a tocar el mapa.@param listener El listener a notificar.
    public void setOnTouchListener(OnTouchListener listener) {
        mListener = listener;
    }

    // Establece el listener opcional que será notificado cuando el usuario termine la interacción táctil con el mapa.

    public void setOnMapInteractionCompleteListener(OnMapInteractionCompleteListener listener) {
        mInteractionCompleteListener = listener;
    }

    // Crea la vista del fragmento. Se superpone un FrameLayout (TouchableWrapper)
    // para interceptar los eventos táctiles antes de que lleguen al mapa.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);

        // TouchableWrapper es un FrameLayout que interceptará los toques.
        // Se usa requireContext() ya que onCreateView se llama después de onAttach.
        TouchableWrapper touchableWrapper = new TouchableWrapper(requireContext());
        touchableWrapper.setBackgroundColor(0x00000000); // Hacer el wrapper transparente

        // Añade el TouchableWrapper encima de la vista del mapa.
        if (layout instanceof ViewGroup) {
            ((ViewGroup) layout).addView(touchableWrapper,
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, // Ocupa todo el espacio del mapa
                            ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return layout;
    }

    // FrameLayout personalizado que intercepta los eventos táctiles.

    public class TouchableWrapper extends FrameLayout {
        public TouchableWrapper(Context context) {
            super(context);
        }

        // Este método se llama para todos los eventos táctiles.
        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (mListener != null) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Cuando el usuario presiona el mapa, notifica al listener principal.
                        // Esto se usa para que el NestedScrollView deje de interceptar los gestos.
                        mListener.onTouchStarted();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Notifica cuando el usuario levanta el dedo o se cancela el gesto.
                        if (mInteractionCompleteListener != null) {
                            mInteractionCompleteListener.onTouchEnded();
                        }

                        break;
                }
            }
            // Llama a super.dispatchTouchEvent(ev) para asegurar que el mapa también reciba y procese el evento táctil (para panning, zooming, etc.).
            return super.dispatchTouchEvent(ev);
        }
    }
}