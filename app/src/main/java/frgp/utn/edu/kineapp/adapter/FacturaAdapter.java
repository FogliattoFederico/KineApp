package frgp.utn.edu.kineapp.adapter;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.model.Factura;

public class FacturaAdapter extends RecyclerView.Adapter<FacturaAdapter.ViewHolder> {

    public interface OnFacturaClickListener {
        void onClick(Factura factura);
        void onLongClick(Factura factura);
    }

    public interface OnCobradaChangeListener {
        void onChange(Factura factura, boolean cobrada, Timestamp fechaPago);
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
            holder.tvFecha.setText("Emisión: " + sdf.format(factura.getFecha().toDate()));
        }

        if (factura.isCobrada() && factura.getFechaPago() != null) {
            holder.tvFechaPago.setText("Pagado el: " + sdf.format(factura.getFechaPago().toDate()));
            holder.tvFechaPago.setVisibility(View.VISIBLE);
        } else {
            holder.tvFechaPago.setVisibility(View.GONE);
        }

        if (factura.getDescripcion() != null && !factura.getDescripcion().isEmpty()) {
            holder.tvDescripcion.setText(factura.getDescripcion());
            holder.tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescripcion.setVisibility(View.GONE);
        }

        actualizarEstado(holder, factura.isCobrada());

        holder.ivCobrada.setOnClickListener(v -> {
            if (!factura.isCobrada()) {
                // Seleccionar fecha de pago
                Calendar cal = Calendar.getInstance();
                new DatePickerDialog(v.getContext(), R.style.CustomDatePickerTheme, (view, year, month, day) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, day);
                    Timestamp timestampPago = new Timestamp(selected.getTime());
                    
                    factura.setCobrada(true);
                    factura.setFechaPago(timestampPago);
                    actualizarEstado(holder, true);
                    
                    if (cobradaListener != null)
                        cobradaListener.onChange(factura, true, timestampPago);
                        
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            } else {
                // Desmarcar pagada
                new AlertDialog.Builder(v.getContext(), R.style.CustomDialogTheme)
                        .setTitle("Quitar pago")
                        .setMessage("¿Deseás marcar esta factura como NO cobrada?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            factura.setCobrada(false);
                            factura.setFechaPago(null);
                            actualizarEstado(holder, false);
                            if (cobradaListener != null)
                                cobradaListener.onChange(factura, false, null);
                        })
                        .setNegativeButton("No", null).show();
            }
        });

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
            holder.layoutFactura.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.color_atendido));
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
        TextView tvTipoNumero, tvObraSocial, tvImporte, tvFecha, tvDescripcion, tvFechaPago;
        ImageView ivCobrada;
        LinearLayout layoutFactura;

        ViewHolder(View v) {
            super(v);
            tvTipoNumero = v.findViewById(R.id.tv_tipo_numero);
            tvObraSocial = v.findViewById(R.id.tv_obra_social_factura);
            tvImporte = v.findViewById(R.id.tv_importe);
            tvFecha = v.findViewById(R.id.tv_fecha_factura);
            tvFechaPago = v.findViewById(R.id.tv_fecha_pago);
            tvDescripcion = v.findViewById(R.id.tv_descripcion_factura);
            ivCobrada = v.findViewById(R.id.iv_cobrada);
            layoutFactura = v.findViewById(R.id.layout_factura);
        }
    }
}
