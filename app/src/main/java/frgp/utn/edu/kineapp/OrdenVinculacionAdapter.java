package frgp.utn.edu.kineapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Locale;

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
        
        // Solo mostrar información de remito si NO es una orden directa
        String remitoTexto = "";
        if (!o.isEsDeRemitoDirecto() && o.getNombreRemito() != null) {
            remitoTexto = "[Remito: " + o.getNombreRemito() + "] ";
        }

        String importeTexto = "";
        if (o.isEsDeRemitoDirecto() && o.getImporte() > 0) {
            importeTexto = String.format(new Locale("es", "AR"), " | Importe: $%,.2f", o.getImporte());
        }
        
        String detalle = String.format("%sOS: %s | Sesiones: %d | Fecha: %s%s\nAfiliado: %s | Código: %s", 
                remitoTexto,
                o.getObraSocialNombre(), o.getCantidadSesiones(), o.getFecha(),
                importeTexto,
                o.getNumeroAfiliado() != null ? o.getNumeroAfiliado() : "-",
                o.getCodigoPractica() != null ? o.getCodigoPractica() : "-");
        
        // Habilitar siempre por defecto antes de aplicar lógica
        holder.btnAccion.setEnabled(true);
        holder.btnAccion.setAlpha(1.0f);

        if (o.isAsociadaAPago()) {
            String linkInfo = "";
            String fechaInfo = o.getFechaVinculo() != null ? "\nFecha de pago : " + o.getFechaVinculo() : "";
            
            if ("COLEGIO".equals(o.getTipoVinculo())) {
                linkInfo = "\nESTADO: Presentada en Colegio" + fechaInfo;
                holder.tvDetalle.setTextColor(Color.parseColor("#388E3C")); // Verde
            } else if ("DIRECTO".equals(o.getTipoVinculo())) {
                String factInfo = o.getDetalleVinculo() != null ? ": " + o.getDetalleVinculo() : "";
                String mesInfo = o.getMesVinculo() != null ? " (" + o.getMesVinculo() + ")" : "";
                linkInfo = "\nESTADO: Vinculada a Factura" + factInfo + mesInfo; // No mostramos fecha de pago aquí
                holder.tvDetalle.setTextColor(Color.parseColor("#1976D2")); // Azul
            }
            holder.tvDetalle.setText(detalle + linkInfo);
            holder.btnAccion.setText("Desvincular");
        } else {
            holder.tvDetalle.setText(detalle);
            holder.tvDetalle.setTextColor(Color.parseColor("#757575")); // Gris normal
            
            if (o.isEsDeRemitoDirecto()) {
                holder.btnAccion.setText("Vincular");
            } else {
                holder.btnAccion.setText("Vía Colegio");
                holder.btnAccion.setEnabled(false);
                holder.btnAccion.setAlpha(0.5f);
            }
        }
        
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