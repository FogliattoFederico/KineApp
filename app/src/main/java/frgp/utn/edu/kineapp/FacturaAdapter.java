package frgp.utn.edu.kineapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FacturaAdapter extends RecyclerView.Adapter<FacturaAdapter.ViewHolder> {

    public interface OnFacturaClickListener {
        void onClick(Factura factura);
        void onLongClick(Factura factura);
    }

    public interface OnCobradaChangeListener {
        void onChange(Factura factura, boolean cobrada);
    }

    private List<Factura> facturas;
    private OnFacturaClickListener clickListener;
    private OnCobradaChangeListener cobradaListener;

    public FacturaAdapter(List<Factura> facturas,
                          OnFacturaClickListener clickListener,
                          OnCobradaChangeListener cobradaListener) {
        this.facturas = facturas;
        this.clickListener = clickListener;
        this.cobradaListener = cobradaListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_factura, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Factura factura = facturas.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "AR"));

        holder.tvTipoNumero.setText(factura.getTipoComprobante() + " N° " + factura.getNumero());
        holder.tvObraSocial.setText(factura.getObraSocial());
        holder.tvImporte.setText(String.format(new Locale("es", "AR"),
                "$ %,.0f", factura.getImporte()));

        if (factura.getFecha() != null) {
            holder.tvFecha.setText(sdf.format(factura.getFecha().toDate()));
        }

        actualizarEstado(holder, factura.isCobrada());

        holder.ivCobrada.setOnClickListener(v -> {
            boolean nuevoCobrada = !factura.isCobrada();
            factura.setCobrada(nuevoCobrada);
            actualizarEstado(holder, nuevoCobrada);
            if (cobradaListener != null)
                cobradaListener.onChange(factura, nuevoCobrada);
        });

        // GESTOS UNIFICADOS
        holder.layoutFactura.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(factura);
        });

        holder.layoutFactura.setOnLongClickListener(v -> {
            if (clickListener != null) clickListener.onLongClick(factura);
            return true;
        });
    }

    private void actualizarEstado(ViewHolder holder, boolean cobrada) {
        if (cobrada) {
            holder.ivCobrada.setImageResource(R.drawable.ic_checkbox_checked);
            holder.layoutFactura.setBackgroundColor(Color.parseColor("#F1F8E9"));
        } else {
            holder.ivCobrada.setImageResource(R.drawable.ic_checkbox_unchecked);
            holder.layoutFactura.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return facturas != null ? facturas.size() : 0;
    }

    public void actualizar(List<Factura> nuevas) {
        this.facturas = nuevas;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipoNumero, tvObraSocial, tvImporte, tvFecha;
        ImageView ivCobrada;
        LinearLayout layoutFactura;

        ViewHolder(View v) {
            super(v);
            tvTipoNumero = v.findViewById(R.id.tv_tipo_numero);
            tvObraSocial = v.findViewById(R.id.tv_obra_social_factura);
            tvImporte = v.findViewById(R.id.tv_importe);
            tvFecha = v.findViewById(R.id.tv_fecha_factura);
            ivCobrada = v.findViewById(R.id.iv_cobrada);
            layoutFactura = v.findViewById(R.id.layout_factura);
        }
    }
}