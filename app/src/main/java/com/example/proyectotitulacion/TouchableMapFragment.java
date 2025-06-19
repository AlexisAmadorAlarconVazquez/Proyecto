package com.example.proyectotitulacion;

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

    private OnTouchListener mListener;

    public interface OnTouchListener {
        void onTouch();
    }

    public void setOnTouchListener(OnTouchListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);

        TouchableWrapper touchableWrapper = new TouchableWrapper(requireContext());
        touchableWrapper.setBackgroundColor(0x00000000); // Transparente

        if (layout instanceof ViewGroup) {
            ((ViewGroup) layout).addView(touchableWrapper,
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
        }

        return layout;
    }

    public class TouchableWrapper extends FrameLayout {
        public TouchableWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (mListener != null) {
                mListener.onTouch();  // Desactiva el scroll de NestedScrollView temporalmente
            }
            return super.dispatchTouchEvent(ev);
        }
    }
}
