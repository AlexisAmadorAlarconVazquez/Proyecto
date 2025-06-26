package com.example.proyectotitulacion.Citas;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class LockableListView extends ListView {

    public LockableListView(Context context) {
        super(context);
    }

    public LockableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // BLOQUEA AL SCROLL VIEW PARA QUE SE MUEVA LA LISTA DE CITAS
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(ev);
    }}
