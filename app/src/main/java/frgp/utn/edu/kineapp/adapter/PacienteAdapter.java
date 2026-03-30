package frgp.utn.edu.kineapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.model.Paciente;

public class PacienteAdapter extends RecyclerView.Adapter<PacienteAdapter.ViewHolder> {

    private List<Paciente> pacientes;
    private OnPacienteClickListener listener;
    private boolean modoTurno = false;

    public interface OnPacienteClickListener {
        void onPacienteClick(Paciente paciente);
    }

    public PacienteAdapter(List<Paciente> pacientes, OnPacienteClickListener listener) {
        this.pacientes = pacientes;
        this.listener = listener;
    }

    public void setModoTurno(boolean modoTurno) {
        this.modoTurno = modoTurno;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paciente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Paciente paciente = pacientes.get(position);

        holder.tvNombre.setText(paciente.getNombreCompleto());
        
        // Avatar
        String inicial = paciente.getApellido() != null && !paciente.getApellido().isEmpty()
                ? String.valueOf(paciente.getApellido().charAt(0)).toUpperCase() : "?";
        holder.tvAvatar.setText(inicial);

        // Campos comunes
        holder.tvDni.setText("DNI: " + (paciente.getDni() != null ? paciente.getDni() : "—"));
        holder.tvDni.setVisibility(View.VISIBLE);

        // Reset visibilidades específicas
        holder.tvDiagnostico.setVisibility(View.GONE);
        holder.tvModalidad.setVisibility(View.GONE);
        holder.tvSesionesRealizadas.setVisibility(View.GONE);
        holder.tvObraSocial.setVisibility(View.GONE);
        holder.tvTipoCobertura.setVisibility(View.GONE);
        holder.tvAfiliado.setVisibility(View.GONE);

        if (!modoTurno) {
            // --- DISEÑO PESTAÑA TODOS ---
            
            // Obra Social
            holder.tvObraSocial.setVisibility(View.VISIBLE);
            if (paciente.getObraSocial() != null && !paciente.getObraSocial().isEmpty()) {
                holder.tvObraSocial.setText(paciente.getObraSocial());
                holder.tvObraSocial.setTextColor(Color.parseColor("#757575"));
            } else {
                holder.tvObraSocial.setText("Sin cobertura");
                holder.tvObraSocial.setTextColor(Color.parseColor("#E53935"));
            }

            // Badge CUD (solo si tiene)
            if (paciente.isCertificadoDiscapacidad()) {
                holder.tvTipoCobertura.setVisibility(View.VISIBLE);
                holder.tvTipoCobertura.setText("CUD");
                holder.tvTipoCobertura.setBackgroundResource(R.drawable.bg_badge_cud);
                holder.tvTipoCobertura.setTextColor(Color.parseColor("#0C447C"));
            }

        } else {
            // --- DISEÑO PESTAÑA TURNOS ---
            
            // Diagnóstico
            holder.tvDiagnostico.setVisibility(View.VISIBLE);
            holder.tvDiagnostico.setText(paciente.getDiagnostico() != null && !paciente.getDiagnostico().isEmpty() 
                ? paciente.getDiagnostico() : "Sin diagnóstico asignado");

            // Información de Obra Social y Afiliado (Visible en Turnos)
            if (paciente.getObraSocial() != null && !paciente.getObraSocial().isEmpty()) {
                holder.tvAfiliado.setVisibility(View.VISIBLE);
                String infoOS = paciente.getObraSocial();
                if (paciente.getNumeroAfiliado() != null && !paciente.getNumeroAfiliado().isEmpty()) {
                    infoOS += " - N°: " + paciente.getNumeroAfiliado();
                }
                holder.tvAfiliado.setText(infoOS);
            }

            // Modalidad
            if (paciente.getModalidad() != null && !paciente.getModalidad().isEmpty()) {
                holder.tvModalidad.setVisibility(View.VISIBLE);
                if ("domicilio".equals(paciente.getModalidad())) {
                    holder.tvModalidad.setText("Domicilio");
                    holder.tvModalidad.setBackgroundResource(R.drawable.bg_badge_orden);
                    holder.tvModalidad.setTextColor(Color.parseColor("#27500A"));
                } else {
                    holder.tvModalidad.setText("Consultorio");
                    holder.tvModalidad.setBackgroundResource(R.drawable.bg_badge_particular);
                    holder.tvModalidad.setTextColor(Color.parseColor("#5F5E5A"));
                }
            }

            // Cobertura Badge
            holder.tvTipoCobertura.setVisibility(View.VISIBLE);
            String tipo;
            if (paciente.isParticular()) {
                tipo = "Particular";
                holder.tvTipoCobertura.setBackgroundResource(R.drawable.bg_badge_particular);
                holder.tvTipoCobertura.setTextColor(Color.parseColor("#5F5E5A"));
            } else if (paciente.isCertificadoDiscapacidad()) {
                tipo = "CUD";
                holder.tvTipoCobertura.setBackgroundResource(R.drawable.bg_badge_cud);
                holder.tvTipoCobertura.setTextColor(Color.parseColor("#0C447C"));
            } else {
                tipo = "Orden";
                holder.tvTipoCobertura.setBackgroundResource(R.drawable.bg_badge_orden);
                holder.tvTipoCobertura.setTextColor(Color.parseColor("#27500A"));
            }
            holder.tvTipoCobertura.setText(tipo);

            // Sesiones Realizadas
            if (!paciente.isCertificadoDiscapacidad()) {
                holder.tvSesionesRealizadas.setVisibility(View.VISIBLE);
                if (!paciente.isParticular() && paciente.getSesionesOrden() > 0) {
                    holder.tvSesionesRealizadas.setText("Sesiones: " + paciente.getSesionesAtendidas() + "/" + paciente.getSesionesOrden());
                } else {
                    holder.tvSesionesRealizadas.setText("Atenciones: " + paciente.getSesionesAtendidas());
                }
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onPacienteClick(paciente));
    }

    @Override
    public int getItemCount() {
        return pacientes != null ? pacientes.size() : 0;
    }

    public void actualizarLista(List<Paciente> nuevaLista) {
        this.pacientes = nuevaLista;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvNombre, tvDiagnostico, tvObraSocial,
                tvTipoCobertura, tvModalidad, tvDni, tvSesionesRealizadas, tvAfiliado;

        ViewHolder(View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvNombre = itemView.findViewById(R.id.tv_nombre);
            tvDiagnostico = itemView.findViewById(R.id.tv_diagnostico);
            tvObraSocial = itemView.findViewById(R.id.tv_obra_social);
            tvTipoCobertura = itemView.findViewById(R.id.tv_tipo_cobertura);
            tvModalidad = itemView.findViewById(R.id.tv_modalidad);
            tvDni = itemView.findViewById(R.id.tv_dni_paciente);
            tvSesionesRealizadas = itemView.findViewById(R.id.tv_sesiones_realizadas);
            tvAfiliado = itemView.findViewById(R.id.tv_afiliado_paciente);
        }
    }
}