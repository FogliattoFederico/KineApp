package frgp.utn.edu.kineapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RemitoAdapter extends RecyclerView.Adapter<RemitoAdapter.ViewHolder> {

    private List<Remito> listaRemitos;
    private List<Remito> listaCompleta;
    private OnRemitoClickListener listener;

    public interface OnRemitoClickListener {
        void onClick(Remito remito);
        void onLongClick(Remito remito);
    }

    public RemitoAdapter(List<Remito> listaRemitos, OnRemitoClickListener listener) {
        this.listaRemitos = listaRemitos;
        this.listaCompleta = new ArrayList<>(listaRemitos);
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

        holder.tvPeriodo.setText(remito.getNumeroRemito() != null ? "Remito N° " + remito.getNumeroRemito() : "Sin número");
        
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
        this.listaCompleta = new ArrayList<>(nuevaLista);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        if (texto.isEmpty()) {
            listaRemitos = new ArrayList<>(listaCompleta);
        } else {
            List<Remito> filtrados = new ArrayList<>();
            String query = texto.toLowerCase();
            for (Remito r : listaCompleta) {
                boolean coincide = false;
                if (r.getNumeroRemito() != null && r.getNumeroRemito().toLowerCase().contains(query)) {
                    coincide = true;
                } else if (r.getOrdenes() != null) {
                    for (OrdenRemito orden : r.getOrdenes()) {
                        if (orden.getPacienteNombreCompleto() != null && 
                            orden.getPacienteNombreCompleto().toLowerCase().contains(query)) {
                            coincide = true;
                            break;
                        }
                    }
                }
                if (coincide) filtrados.add(r);
            }
            listaRemitos = filtrados;
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeriodo, tvCantidad;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPeriodo = itemView.findViewById(R.id.tv_periodo_remito);
            // Referencia antigua a tvFecha eliminada
            tvCantidad = itemView.findViewById(R.id.tv_cantidad_ordenes);
        }
    }
}
