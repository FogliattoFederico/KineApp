package frgp.utn.edu.kineapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.model.OrdenRemito;
import frgp.utn.edu.kineapp.model.Remito;

public class RemitoAdapter extends RecyclerView.Adapter<RemitoAdapter.ViewHolder> {

    private List<Remito> listaRemitos;
    private List<Remito> listaCompleta;
    private OnRemitoClickListener listener;

    public interface OnRemitoClickListener {
        void onClick(Remito remito);
        void onLongClick(Remito remito);
        void onExportClick(Remito remito);
    }

    public RemitoAdapter(List<Remito> listaRemitos, OnRemitoClickListener listener) {
        this.listaRemitos = new ArrayList<>(listaRemitos);
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

        String periodo = remito.getPeriodoRemito() != null ? remito.getPeriodoRemito() : "S/N";
        holder.tvPeriodo.setText(periodo);
        
        int cantOrdenes = remito.getOrdenes() != null ? remito.getOrdenes().size() : 0;
        holder.tvCantidad.setText(cantOrdenes + (cantOrdenes == 1 ? " orden incluida" : " órdenes incluidas"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(remito);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongClick(remito);
            return true;
        });

        holder.btnExportar.setOnClickListener(v -> {
            if (listener != null) listener.onExportClick(remito);
        });
    }

    @Override
    public int getItemCount() {
        return listaRemitos != null ? listaRemitos.size() : 0;
    }

    public void actualizar(List<Remito> nuevaLista) {
        this.listaCompleta = new ArrayList<>(nuevaLista);
        this.listaRemitos = new ArrayList<>(nuevaLista);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            listaRemitos = new ArrayList<>(listaCompleta);
        } else {
            List<Remito> filtrados = new ArrayList<>();
            String query = normalizarTexto(texto);
            for (Remito r : listaCompleta) {
                boolean coincide = false;
                if (r.getPeriodoRemito() != null && normalizarTexto(r.getPeriodoRemito()).contains(query)) {
                    coincide = true;
                } else if (r.getOrdenes() != null) {
                    for (OrdenRemito orden : r.getOrdenes()) {
                        if (orden.getPacienteNombreCompleto() != null && 
                            normalizarTexto(orden.getPacienteNombreCompleto()).contains(query)) {
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

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeriodo, tvCantidad;
        View btnExportar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPeriodo = itemView.findViewById(R.id.tv_periodo_remito);
            tvCantidad = itemView.findViewById(R.id.tv_cantidad_ordenes);
            btnExportar = itemView.findViewById(R.id.btn_exportar_pdf);
        }
    }
}
