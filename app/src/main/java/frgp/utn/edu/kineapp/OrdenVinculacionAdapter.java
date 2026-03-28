package frgp.utn.edu.kineapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class OrdenVinculacionAdapter extends RecyclerView.Adapter<OrdenVinculacionAdapter.ViewHolder> {

    private List<OrdenRemito> lista;
    private OnOrdenClickListener listener;

    public interface OnOrdenClickListener {
        void onToggleAsociada(OrdenRemito orden);
        void onEdit(OrdenRemito orden);
        void onDelete(OrdenRemito orden);
    }

    public OrdenVinculacionAdapter(List<OrdenRemito> lista, OnOrdenClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_orden_vinculacion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrdenRemito o = lista.get(position);
        holder.tvPaciente.setText(o.getPacienteNombreCompleto());
        holder.tvDetalle.setText(String.format("OS: %s | Sesiones: %d | Fecha: %s\nAfiliado: %s | Código: %s", 
                o.getObraSocialNombre(), o.getCantidadSesiones(), o.getFecha(),
                o.getNumeroAfiliado() != null ? o.getNumeroAfiliado() : "-",
                o.getCodigoPractica() != null ? o.getCodigoPractica() : "-"));
        
        holder.btnAccion.setText(o.isAsociadaAPago() ? "Marcar Pendiente" : "Marcar Asociada");
        
        holder.btnAccion.setOnClickListener(v -> listener.onToggleAsociada(o));
        
        holder.itemView.setOnClickListener(v -> listener.onEdit(o));
        
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(o);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPaciente, tvDetalle;
        MaterialButton btnAccion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaciente = itemView.findViewById(R.id.tv_orden_paciente);
            tvDetalle = itemView.findViewById(R.id.tv_orden_detalle);
            btnAccion = itemView.findViewById(R.id.btn_orden_accion);
        }
    }
}