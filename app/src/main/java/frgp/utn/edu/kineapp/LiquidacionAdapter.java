package frgp.utn.edu.kineapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LiquidacionAdapter extends RecyclerView.Adapter<LiquidacionAdapter.ViewHolder> {

    private List<LiquidacionColegio> liquidaciones;
    private OnLiquidacionClickListener listener;

    public interface OnLiquidacionClickListener {
        void onDelete(LiquidacionColegio liq);
        void onMarkAsFacturada(LiquidacionColegio liq);
    }

    public LiquidacionAdapter(List<LiquidacionColegio> liquidaciones, OnLiquidacionClickListener listener) {
        this.liquidaciones = liquidaciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_liquidacion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LiquidacionColegio liq = liquidaciones.get(position);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvFecha.setText(liq.getFechaLiquidacion() != null ? sdf.format(liq.getFechaLiquidacion().toDate()) : "—");
        holder.tvImporte.setText(String.format(new Locale("es", "AR"), "$ %,.2f", liq.getImporte()));

        long dias = liq.getDiasPendientes();
        holder.tvDias.setText(dias + " días");

        if (dias >= 60) {
            holder.tvDias.setTextColor(Color.RED);
            holder.tvDias.setText(dias + " días (RIESGO DE RETENCIÓN)");
        } else if (dias >= 45) {
            holder.tvDias.setTextColor(Color.parseColor("#EF6C00")); // Naranja
        } else {
            holder.tvDias.setTextColor(Color.parseColor("#757575"));
        }

        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(liq);
            return true;
        });

        holder.itemView.setOnClickListener(v -> listener.onMarkAsFacturada(liq));
    }

    @Override
    public int getItemCount() {
        return liquidaciones.size();
    }

    public void actualizar(List<LiquidacionColegio> nuevas) {
        this.liquidaciones = nuevas;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvImporte, tvDias;

        ViewHolder(View v) {
            super(v);
            tvFecha = v.findViewById(R.id.tv_fecha_liquidacion);
            tvImporte = v.findViewById(R.id.tv_importe_liquidacion);
            tvDias = v.findViewById(R.id.tv_dias_pendiente);
        }
    }
}