package frgp.utn.edu.kineapp.adapter;

import android.content.Intent;
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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import frgp.utn.edu.kineapp.ui.activity.FormularioPacienteActivity;
import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.model.Atencion;
import frgp.utn.edu.kineapp.model.HorarioAtencion;
import frgp.utn.edu.kineapp.model.Paciente;
import frgp.utn.edu.kineapp.repository.AtencionRepository;

public class TurnoAdapter extends RecyclerView.Adapter<TurnoAdapter.ViewHolder> {

    public static class Turno {
        public String hora;
        public String horaFin;
        public String nombrePaciente;
        public String diagnostico;
        public String obraSocial;
        public String tipoCobertura;
        public boolean atendido;
        public String pacienteId;
        public double valorSesion;
        public int sesionesAtendidas;
        public int sesionesTotales;
        public String modalidad;
        public String atencionId;
        public int horarioIndice;

        public Turno(String hora, String horaFin, String nombrePaciente, String diagnostico,
                     String obraSocial, String tipoCobertura,
                     boolean atendido, String pacienteId,
                     double valorSesion, int sesionesAtendidas,
                     int sesionesTotales, String modalidad, int horarioIndice) {
            this.hora = hora;
            this.horaFin = horaFin;
            this.nombrePaciente = nombrePaciente;
            this.diagnostico = diagnostico;
            this.obraSocial = obraSocial;
            this.tipoCobertura = tipoCobertura;
            this.atendido = atendido;
            this.pacienteId = pacienteId;
            this.valorSesion = valorSesion;
            this.sesionesAtendidas = sesionesAtendidas;
            this.sesionesTotales = sesionesTotales;
            this.modalidad = modalidad;
            this.horarioIndice = horarioIndice;
            this.atencionId = null;
        }
    }

    public interface OnAtendidoChangeListener {
        void onChange(Turno turno, boolean atendido);
    }

    private List<Turno> turnos;
    private OnAtendidoChangeListener listener;
    private Calendar fechaAgenda;
    private String userPlan = "free"; 

    public TurnoAdapter(List<Turno> turnos, OnAtendidoChangeListener listener) {
        this.turnos = turnos;
        this.listener = listener;
        this.fechaAgenda = Calendar.getInstance();
    }

    public void setFechaAgenda(Calendar fecha) {
        this.fechaAgenda = fecha != null ? (Calendar) fecha.clone() : Calendar.getInstance();
    }

    public void setUserPlan(String plan) {
        this.userPlan = plan != null ? plan : "free";
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

        String rangoHorario = turno.hora;
        if (turno.horaFin != null && !turno.horaFin.isEmpty()) {
            rangoHorario += " - " + turno.horaFin;
        }
        holder.tvHora.setText(rangoHorario);
        
        holder.tvNombre.setText(turno.nombrePaciente);
        holder.tvDiagnostico.setText(turno.diagnostico != null && !turno.diagnostico.isEmpty() ? turno.diagnostico : "Sin diagnóstico");

        // 1. Obra Social
        boolean tieneObraSocial = turno.obraSocial != null && !turno.obraSocial.isEmpty();
        holder.tvObraSocial.setVisibility(tieneObraSocial ? View.VISIBLE : View.GONE);
        holder.tvObraSocial.setText(tieneObraSocial ? turno.obraSocial : "");

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
        } else {
            holder.tvModalidad.setVisibility(View.GONE);
        }

        // Sesiones: x/m (Ahora visible para particulares también si tienen tope de sesiones)
        boolean mostrarSesiones = ("Orden".equals(turno.tipoCobertura) || "Particular".equals(turno.tipoCobertura)) 
                                    && turno.sesionesTotales > 0;
        
        if (mostrarSesiones) {
            holder.tvSesiones.setVisibility(View.VISIBLE);
            holder.tvSesiones.setText(turno.sesionesAtendidas + "/" + turno.sesionesTotales);
            
            int restantes = turno.sesionesTotales - turno.sesionesAtendidas;
            float porcentajeRestante = (float) restantes / turno.sesionesTotales;
            
            if (restantes <= 0) {
                holder.tvSesiones.setTextColor(Color.parseColor("#E53935"));
                holder.tvSesiones.setBackgroundResource(R.drawable.bg_badge_rojo);
            } else if (porcentajeRestante <= 0.3f) {
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
            if (turno.atendido) {
                desmarcarAtencion(holder, turno);
            } else {
                // Validación de sesiones para Particulares también
                boolean haySesionesExcedidas = ("Orden".equals(turno.tipoCobertura) || "Particular".equals(turno.tipoCobertura)) 
                                                && (turno.sesionesTotales - turno.sesionesAtendidas) <= 0;
                                                
                if (haySesionesExcedidas && turno.sesionesTotales > 0) {
                    new AlertDialog.Builder(v.getContext(), R.style.CustomDialogTheme)
                            .setTitle("Sesiones finalizadas")
                            .setMessage(turno.nombrePaciente + " ya completó sus sesiones autorizadas.")
                            .setPositiveButton("Registrar igual", (d, w) -> mostrarDialogoRegistroSesion(holder, turno))
                            .setNegativeButton("Cancelar", null).show();
                } else {
                    mostrarDialogoRegistroSesion(holder, turno);
                }
            }
        });

        holder.layoutTurno.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), FormularioPacienteActivity.class);
            intent.putExtra("pacienteId", turno.pacienteId);
            intent.putExtra("horarioIndice", turno.horarioIndice);
            v.getContext().startActivity(intent);
        });

        holder.layoutTurno.setOnLongClickListener(v -> {
            mostrarConfirmacionEliminar(v.getContext(), turno);
            return true;
        });

        holder.ivMenu.setVisibility(View.GONE);
    }

    private void mostrarConfirmacionEliminar(android.content.Context context, Turno turno) {
        new AlertDialog.Builder(context, R.style.CustomDialogTheme)
                .setTitle("Eliminar turno")
                .setMessage("¿Estás seguro que deseás eliminar este turno de la agenda?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarTurno(context, turno))
                .setNegativeButton("Cancelar", null).show();
    }

    private void eliminarTurno(android.content.Context context, Turno turno) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (turno.atencionId != null) {
            db.collection("atenciones").document(turno.atencionId).delete();
            // Descuenta sesión para particulares también
            if ("Orden".equals(turno.tipoCobertura) || "Particular".equals(turno.tipoCobertura)) {
                db.collection("pacientes").document(turno.pacienteId)
                        .update("sesionesAtendidas", com.google.firebase.firestore.FieldValue.increment(-1));
            }
        }
        db.collection("pacientes").document(turno.pacienteId).get().addOnSuccessListener(doc -> {
            Paciente p = doc.toObject(Paciente.class);
            if (p != null && p.getHorarios() != null) {
                List<HorarioAtencion> nuevosHorarios = new ArrayList<>(p.getHorarios());
                
                if (turno.horarioIndice >= 0 && turno.horarioIndice < nuevosHorarios.size()) {
                    HorarioAtencion h = nuevosHorarios.get(turno.horarioIndice);
                    if (h.getHoraInicio().equals(turno.hora)) {
                        nuevosHorarios.remove(turno.horarioIndice);
                    } else {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        String fechaEliminar = sdf.format(fechaAgenda.getTime());
                        for (int i = 0; i < nuevosHorarios.size(); i++) {
                            HorarioAtencion item = nuevosHorarios.get(i);
                            boolean mismoHorario = turno.hora.equals(item.getHoraInicio());
                            boolean mismaFecha = (item.getFecha() != null && item.getFecha().equals(fechaEliminar));
                            boolean mismoDiaSemanala = (item.getFecha() == null || item.getFecha().isEmpty());

                            if (mismoHorario && (mismaFecha || mismoDiaSemanala)) {
                                nuevosHorarios.remove(i);
                                break;
                            }
                        }
                    }
                }
                
                db.collection("pacientes").document(turno.pacienteId).update("horarios", nuevosHorarios)
                        .addOnSuccessListener(unused -> {
                            if (listener != null) listener.onChange(turno, turno.atendido);
                        });
            }
        });
    }

    private void desmarcarAtencion(ViewHolder holder, Turno turno) {
        AtencionRepository repo = new AtencionRepository();
        turno.atendido = false;
        boolean aplicaContador = "Orden".equals(turno.tipoCobertura) || "Particular".equals(turno.tipoCobertura);
        
        if (aplicaContador) turno.sesionesAtendidas--;
        if (turno.atencionId != null) {
            repo.eliminar(turno.atencionId);
            if (aplicaContador) {
                FirebaseFirestore.getInstance().collection("pacientes").document(turno.pacienteId)
                        .update("sesionesAtendidas", com.google.firebase.firestore.FieldValue.increment(-1));
            }
        }
        actualizarEstado(holder, false);
        if (listener != null) listener.onChange(turno, false);
    }

    private void mostrarDialogoRegistroSesion(ViewHolder holder, Turno turno) {
        View dialogView = LayoutInflater.from(holder.itemView.getContext())
                .inflate(R.layout.dialog_registro_sesion, null);

        AlertDialog dialog = new AlertDialog.Builder(holder.itemView.getContext(), R.style.CustomDialogTheme)
                .setView(dialogView)
                .create();

        com.google.android.material.textfield.TextInputEditText etObjetivos = dialogView.findViewById(R.id.et_objetivos);
        com.google.android.material.textfield.TextInputEditText etObservaciones = dialogView.findViewById(R.id.et_observaciones);
        com.google.android.material.button.MaterialButton btnGuardar = dialogView.findViewById(R.id.btn_guardar_sesion);

        btnGuardar.setOnClickListener(v -> {
            String objetivos = etObjetivos.getText().toString().trim();
            String observaciones = etObservaciones.getText().toString().trim();

            AtencionRepository repo = new AtencionRepository();
            turno.atendido = true;
            boolean aplicaContador = "Orden".equals(turno.tipoCobertura) || "Particular".equals(turno.tipoCobertura);
            
            if (aplicaContador) turno.sesionesAtendidas++;

            int sesionNum = turno.sesionesAtendidas;
            Calendar cal = (Calendar) fechaAgenda.clone();
            String[] partes = turno.hora.split(":");
            if (partes.length == 2) {
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(partes[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(partes[1]));
            }

            Atencion atencion = new Atencion(turno.pacienteId, turno.nombrePaciente, turno.modalidad,
                    turno.tipoCobertura, turno.valorSesion, sesionNum, turno.sesionesTotales, "",
                    new com.google.firebase.Timestamp(cal.getTime()));
            atencion.setObjetivos(objetivos);
            atencion.setObservaciones(observaciones);

            repo.guardar(atencion).addOnSuccessListener(a -> {
                turno.atencionId = atencion.getId();
                if (aplicaContador) {
                    FirebaseFirestore.getInstance().collection("pacientes").document(turno.pacienteId)
                            .update("sesionesAtendidas", com.google.firebase.firestore.FieldValue.increment(1));
                }
                actualizarEstado(holder, true);
                if (listener != null) listener.onChange(turno, true);
                dialog.dismiss();
            });
        });

        dialog.show();
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
        TextView tvHora, tvNombre, tvDiagnostico, tvObraSocial, tvTipoCobertura, tvSesiones, tvModalidad;
        ImageView ivAtendido, ivMenu;
        LinearLayout layoutTurno;
        ViewHolder(View v) {
            super(v);
            tvHora = v.findViewById(R.id.tv_hora);
            tvNombre = v.findViewById(R.id.tv_nombre_paciente);
            tvDiagnostico = v.findViewById(R.id.tv_diagnostico_turno);
            tvObraSocial = v.findViewById(R.id.tv_obra_social_turno);
            tvTipoCobertura = v.findViewById(R.id.tv_tipo_cobertura);
            tvSesiones = v.findViewById(R.id.tv_sesiones);
            ivAtendido = v.findViewById(R.id.cb_atendido);
            ivMenu = v.findViewById(R.id.iv_menu_turno);
            layoutTurno = v.findViewById(R.id.layout_turno);
            tvModalidad = v.findViewById(R.id.tv_modalidad_turno);
        }
    }
}
