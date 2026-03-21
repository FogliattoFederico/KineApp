package frgp.utn.edu.kineapp;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TurnoAdapter extends RecyclerView.Adapter<TurnoAdapter.ViewHolder> {

    public static class Turno {
        public String hora;
        public String nombrePaciente;
        public String diagnostico;
        public String obraSocial;
        public String tipoCobertura;
        public boolean atendido;
        public String pacienteId;
        public double valorSesion;
        public int sesionesRestantes;
        public int sesionesOrden;
        public String modalidad;
        public String atencionId;

        public Turno(String hora, String nombrePaciente, String diagnostico,
                     String obraSocial, String tipoCobertura,
                     boolean atendido, String pacienteId,
                     double valorSesion, int sesionesRestantes,
                     int sesionesOrden, String modalidad) {
            this.hora = hora;
            this.nombrePaciente = nombrePaciente;
            this.diagnostico = diagnostico;
            this.obraSocial = obraSocial;
            this.tipoCobertura = tipoCobertura;
            this.atendido = atendido;
            this.pacienteId = pacienteId;
            this.valorSesion = valorSesion;
            this.sesionesRestantes = sesionesRestantes;
            this.sesionesOrden = sesionesOrden;
            this.modalidad = modalidad;
            this.atencionId = null;
        }
    }

    public interface OnAtendidoChangeListener {
        void onChange(Turno turno, boolean atendido);
    }

    private List<Turno> turnos;
    private OnAtendidoChangeListener listener;
    private Calendar fechaAgenda;

    public TurnoAdapter(List<Turno> turnos, OnAtendidoChangeListener listener) {
        this.turnos = turnos;
        this.listener = listener;
        this.fechaAgenda = Calendar.getInstance();
    }

    public void setFechaAgenda(Calendar fecha) {
        this.fechaAgenda = fecha != null ? (Calendar) fecha.clone() : Calendar.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_turno, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Turno turno = turnos.get(position);

        holder.tvHora.setText(turno.hora);
        holder.tvNombre.setText(turno.nombrePaciente);
        holder.tvDiagnostico.setText(turno.diagnostico);

        // 1. Obra Social
        boolean tieneObraSocial = turno.obraSocial != null && !turno.obraSocial.isEmpty();
        holder.tvObraSocial.setVisibility(tieneObraSocial ? View.VISIBLE : View.GONE);
        holder.tvObraSocial.setText(tieneObraSocial ? turno.obraSocial : "");
        holder.tvSep1.setVisibility(tieneObraSocial ? View.VISIBLE : View.GONE);

        // 2. Cobertura (Badge)
        holder.tvTipoCobertura.setVisibility(View.VISIBLE);
        switch (turno.tipoCobertura) {
            case "Particular":
                holder.tvTipoCobertura.setBackgroundResource(R.drawable.bg_badge_particular);
                holder.tvTipoCobertura.setTextColor(Color.parseColor("#5F5E5A"));
                break;
            case "CUD":
                holder.tvTipoCobertura.setBackgroundResource(R.drawable.bg_badge_cud);
                holder.tvTipoCobertura.setTextColor(Color.parseColor("#0C447C"));
                break;
            default:
                holder.tvTipoCobertura.setBackgroundResource(R.drawable.bg_badge_orden);
                holder.tvTipoCobertura.setTextColor(Color.parseColor("#27500A"));
                break;
        }
        holder.tvTipoCobertura.setText(turno.tipoCobertura);

        // 3. Modalidad
        if (turno.modalidad != null && !turno.modalidad.isEmpty()) {
            holder.tvModalidad.setVisibility(View.VISIBLE);
            if ("domicilio".equals(turno.modalidad)) {
                holder.tvModalidad.setText("Domicilio");
                holder.tvModalidad.setBackgroundResource(R.drawable.bg_badge_orden);
                holder.tvModalidad.setTextColor(Color.parseColor("#27500A"));
            } else {
                holder.tvModalidad.setText("Consultorio");
                holder.tvModalidad.setBackgroundResource(R.drawable.bg_badge_particular);
                holder.tvModalidad.setTextColor(Color.parseColor("#5F5E5A"));
            }
            holder.tvSep2.setVisibility(View.VISIBLE);
        } else {
            holder.tvModalidad.setVisibility(View.GONE);
            holder.tvSep2.setVisibility(View.GONE);
        }

        // 4. Diagnóstico
        holder.tvDiagnostico.setText(turno.diagnostico != null && !turno.diagnostico.isEmpty() ? turno.diagnostico : "Sin diagnóstico");

        // Sesiones
        if ("Orden".equals(turno.tipoCobertura) && turno.sesionesOrden > 0) {
            holder.tvSesiones.setVisibility(View.VISIBLE);
            holder.tvSesiones.setText(turno.sesionesRestantes + "/" + turno.sesionesOrden);
            float porcentaje = (float) turno.sesionesRestantes / turno.sesionesOrden;
            if (porcentaje <= 0) {
                holder.tvSesiones.setTextColor(Color.parseColor("#E53935"));
                holder.tvSesiones.setBackgroundResource(R.drawable.bg_badge_rojo);
            } else if (porcentaje <= 0.3f) {
                holder.tvSesiones.setTextColor(Color.parseColor("#E65100"));
                holder.tvSesiones.setBackgroundResource(R.drawable.bg_badge_naranja);
            } else {
                holder.tvSesiones.setTextColor(Color.parseColor("#27500A"));
                holder.tvSesiones.setBackgroundResource(R.drawable.bg_badge_orden);
            }
        } else {
            holder.tvSesiones.setVisibility(View.GONE);
        }

        actualizarEstado(holder, turno.atendido);

        // Click en el Checkbox para marcar como ATENDIDO
        holder.ivAtendido.setOnClickListener(v -> {
            if ("Orden".equals(turno.tipoCobertura) && turno.sesionesRestantes <= 0 && !turno.atendido) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Sesiones finalizadas")
                        .setMessage(turno.nombrePaciente + " no tiene sesiones restantes.")
                        .setPositiveButton("Registrar igual", (d, w) -> procesarAtencion(holder, turno))
                        .setNegativeButton("Cancelar", null).show();
                return;
            }
            procesarAtencion(holder, turno);
        });

        // --- GESTOS UNIFICADOS ---
        
        // TOQUE SIMPLE: Editar turno
        holder.layoutTurno.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), FormularioPacienteActivity.class);
            intent.putExtra("pacienteId", turno.pacienteId);
            v.getContext().startActivity(intent);
        });

        // TOQUE MANTENIDO: Eliminar turno
        holder.layoutTurno.setOnLongClickListener(v -> {
            mostrarConfirmacionEliminar(v.getContext(), turno);
            return true;
        });

        // Ocultamos el icono de menú ya que no se usa más
        holder.ivMenu.setVisibility(View.GONE);
    }

    private void mostrarConfirmacionEliminar(android.content.Context context, Turno turno) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar turno")
                .setMessage("¿Estás seguro que deseás eliminar este turno?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarTurno(context, turno))
                .setNegativeButton("Cancelar", null).show();
    }

    private void eliminarTurno(android.content.Context context, Turno turno) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (turno.atencionId != null) {
            db.collection("atenciones").document(turno.atencionId).delete();
            if ("Orden".equals(turno.tipoCobertura)) {
                db.collection("pacientes").document(turno.pacienteId)
                        .update("sesionesAtendidas", com.google.firebase.firestore.FieldValue.increment(-1));
            }
        }
        db.collection("pacientes").document(turno.pacienteId).get().addOnSuccessListener(doc -> {
            Paciente p = doc.toObject(Paciente.class);
            if (p != null && p.getHorarios() != null) {
                List<HorarioAtencion> nuevosHorarios = new ArrayList<>();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                String fechaEliminar = sdf.format(fechaAgenda.getTime());
                for (HorarioAtencion h : p.getHorarios()) {
                    if (!(turno.hora.equals(h.getHoraInicio()) && fechaEliminar.equals(h.getFecha()))) {
                        nuevosHorarios.add(h);
                    }
                }
                db.collection("pacientes").document(turno.pacienteId).update("horarios", nuevosHorarios)
                        .addOnSuccessListener(unused -> {
                            if (listener != null) listener.onChange(turno, turno.atendido);
                        });
            }
        });
    }

    private void procesarAtencion(ViewHolder holder, Turno turno) {
        AtencionRepository repo = new AtencionRepository();
        turno.atendido = !turno.atendido;
        if (turno.atendido) {
            if ("Orden".equals(turno.tipoCobertura)) turno.sesionesRestantes--;
            int sesionNum = turno.sesionesOrden > 0 ? turno.sesionesOrden - turno.sesionesRestantes : 0;
            Calendar cal = (Calendar) fechaAgenda.clone();
            String[] partes = turno.hora.split(":");
            if (partes.length == 2) {
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(partes[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(partes[1]));
            }
            Atencion atencion = new Atencion(turno.pacienteId, turno.nombrePaciente, turno.modalidad, turno.tipoCobertura, turno.valorSesion, sesionNum, turno.sesionesOrden, "", new com.google.firebase.Timestamp(cal.getTime()));
            repo.guardar(atencion).addOnSuccessListener(a -> {
                turno.atencionId = atencion.getId();
                if ("Orden".equals(turno.tipoCobertura)) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("pacientes").document(turno.pacienteId)
                            .update("sesionesAtendidas", com.google.firebase.firestore.FieldValue.increment(1));
                }
            });
        } else {
            if ("Orden".equals(turno.tipoCobertura)) turno.sesionesRestantes++;
            if (turno.atencionId != null) {
                repo.eliminar(turno.atencionId);
                if ("Orden".equals(turno.tipoCobertura)) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("pacientes").document(turno.pacienteId)
                            .update("sesionesAtendidas", com.google.firebase.firestore.FieldValue.increment(-1));
                }
            }
        }
        actualizarEstado(holder, turno.atendido);
        if (listener != null) listener.onChange(turno, turno.atendido);
    }

    private void actualizarEstado(ViewHolder holder, boolean atendido) {
        if (atendido) {
            holder.ivAtendido.setImageResource(R.drawable.ic_checkbox_checked);
            holder.layoutTurno.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.color_atendido));
        } else {
            holder.ivAtendido.setImageResource(R.drawable.ic_checkbox_unchecked);
            holder.layoutTurno.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() { return turnos != null ? turnos.size() : 0; }

    public void actualizar(List<Turno> nuevos) { this.turnos = nuevos; notifyDataSetChanged(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHora, tvNombre, tvDiagnostico, tvObraSocial, tvTipoCobertura, tvSep1, tvSep2, tvSesiones, tvModalidad;
        ImageView ivAtendido, ivMenu;
        LinearLayout layoutTurno;
        ViewHolder(View v) {
            super(v);
            tvHora = v.findViewById(R.id.tv_hora);
            tvNombre = v.findViewById(R.id.tv_nombre_paciente);
            tvDiagnostico = v.findViewById(R.id.tv_diagnostico_turno);
            tvObraSocial = v.findViewById(R.id.tv_obra_social_turno);
            tvTipoCobertura = v.findViewById(R.id.tv_tipo_cobertura);
            tvSep1 = v.findViewById(R.id.tv_sep1);
            tvSep2 = v.findViewById(R.id.tv_sep2);
            tvSesiones = v.findViewById(R.id.tv_sesiones);
            ivAtendido = v.findViewById(R.id.cb_atendido);
            ivMenu = v.findViewById(R.id.iv_menu_turno);
            layoutTurno = v.findViewById(R.id.layout_turno);
            tvModalidad = v.findViewById(R.id.tv_modalidad_turno);
        }
    }
}