package frgp.utn.edu.kineapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RemitoAdapter extends RecyclerView.Adapter<RemitoAdapter.ViewHolder> {

    private List<Remito> listaRemitos;
    private OnRemitoClickListener listener;

    public interface OnRemitoClickListener {
        void onClick(Remito remito);
        void onLongClick(Remito remito);
    }

    public RemitoAdapter(List<Remito> listaRemitos, OnRemitoClickListener listener) {
        this.listaRemitos = listaRemitos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remito, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Remito remito = listaRemitos.get(position);

        holder.tvPeriodo.setText(remito.getNumeroRemito() != null ? remito.getNumeroRemito() : "Sin período");
        
        if (remito.getFechaCreacion() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvFecha.setText(sdf.format(remito.getFechaCreacion().toDate()));
        }

        int cantOrdenes = remito.getOrdenes() != null ? remito.getOrdenes().size() : 0;
        holder.tvCantidad.setText(cantOrdenes + (cantOrdenes == 1 ? " orden incluida" : " órdenes incluidas"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(remito);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongClick(remito);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaRemitos != null ? listaRemitos.size() : 0;
    }

    public void actualizar(List<Remito> nuevaLista) {
        this.listaRemitos = nuevaLista;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeriodo, tvFecha, tvCantidad;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPeriodo = itemView.findViewById(R.id.tv_periodo_remito);
            tvFecha = itemView.findViewById(R.id.tv_fecha_creacion);
            tvCantidad = itemView.findViewById(R.id.tv_cantidad_ordenes);
        }
    }
}
