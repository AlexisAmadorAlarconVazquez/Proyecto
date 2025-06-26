package com.example.proyectotitulacion.Citas;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.proyectotitulacion.R;

import java.util.List;

public class CitaAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final List<String> citas;
    private final List<Integer> ids;
    private final MainActivity mainActivity;

    public CitaAdapter(Activity context, List<String> citas, List<Integer> ids, MainActivity mainActivity) {
        super(context, R.layout.item_cita, citas);
        this.context = context;
        this.citas = citas;
        this.ids = ids;
        this.mainActivity = mainActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        if (item == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            item = inflater.inflate(R.layout.item_cita, parent, false);
        }

        TextView tvTexto = item.findViewById(R.id.tvCitaTexto);
        Button btnCancelar = item.findViewById(R.id.btnCancelarCita);

        tvTexto.setText(citas.get(position));
        int idCita = ids.get(position);

        // AL TOCAR EL TEXTO SE HABRE EL DIALOGO
        tvTexto.setOnClickListener(v -> mainActivity.abrirDialogoEditar(idCita));

        // SE ABRE EL BOTON DE CANCELAR
        btnCancelar.setOnClickListener(v -> mainActivity.mostrarDialogoCancelar(idCita));

        return item;
    }
}
