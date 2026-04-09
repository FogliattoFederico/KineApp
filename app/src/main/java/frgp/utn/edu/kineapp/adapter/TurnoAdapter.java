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
import com.google.firebase.firestore.FieldValue;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import frgp.utn.edu.kineapp.ui.activity.FormularioPacienteActivity;
import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.model.Atencion;
import frgp.utn.edu.kineapp.model.HorarioAtencion;
import frgp.utn.edu.kineapp.model.Paciente;
import frgp.utn.edu.kineapp.repository.AtencionRepository;

public class TurnoAdapter extends RecyclerView.Adapter<TurnoAdapter.ViewHolder> {

    private static final Map<String, int[]> cacheSesionesHistoricas = new HashMap<>();

    public static class Turno {
        public String hora;
        public String horaFin;
        public String fecha; 
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

        public Turno(String hora, String horaFin, String fecha, String nombrePaciente, String diagnostico,
                     String obraSocial, String tipoCobertura,
                     boolean atendido, String pacienteId,
                     double valorSesion, int sesionesAtendidas,
                     int sesionesTotales, String modalidad, int horarioIndice) {
            this.hora = hora;
            this.horaFin = horaFin;
            this.fecha = fecha;
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

    private String generarClaveTurno(Turno t) {
        return t.pacienteId + "_" + t.fecha + "_" + t.hora;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_turno, parent, false);
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

        boolean tieneObraSocial = turno.obraSocial != null && !turno.obraSocial.isEmpty();
        holder.tvObraSocial.setVisibility(tieneObraSocial ? View.VISIBLE : View.GONE);
        holder.tvObraSocial.setText(tieneObraSocial ? turno.obraSocial : "");

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

        if (turno.modalidad != null && !turno.modalidad.isEmpty()) {
            holder.tvModalidad.setVisibility(View.VISIBLE);
            holder.tvModalidad.setText("domicilio".equals(turno.modalidad) ? "Domicilio" : "Consultorio");
            holder.tvModalidad.setBackgroundResource("domicilio".equals(turno.modalidad) ? R.drawable.bg_badge_orden : R.drawable.bg_badge_particular);
            holder.tvModalidad.setTextColor(Color.parseColor("domicilio".equals(turno.modalidad) ? "#27500A" : "#5F5E5A"));
        } else {
            holder.tvModalidad.setVisibility(View.GONE);
        }

        int[] cached = cacheSesionesHistoricas.get(generarClaveTurno(turno));
        if (cached != null && !turno.atendido) {
            turno.sesionesAtendidas = cached[0];
            turno.sesionesTotales = cached[1];
        }

        boolean mostrarSesiones = ("Orden".equals(turno.tipoCobertura) || "Particular".equals(turno.tipoCobertura)) 
                                    && turno.sesionesTotales > 0;
        
        if (mostrarSesiones) {
            holder.tvSesiones.setVisibility(View.VISIBLE);
            holder.tvSesiones.setText(turno.sesionesAtendidas + "/" + turno.sesionesTotales);
            int restantes = turno.sesionesTotales - turno.sesionesAtendidas;
            if (restantes <= 0) {
                holder.tvSesiones.setTextColor(Color.parseColor("#E53935"));
                holder.tvSesiones.setBackgroundResource(R.drawable.bg_badge_rojo);
            } else if ((float) restantes / turno.sesionesTotales <= 0.3f) {
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
            new AlertDialog.Builder(v.getContext(), R.style.CustomDialogTheme)
                    .setTitle("Eliminar turno")
                    .setMessage("¿Estás seguro que deseás eliminar este turno de la agenda?")
                    .setPositiveButton("Eliminar", (d, w) -> eliminarTurno(v.getContext(), turno))
                    .setNegativeButton("Cancelar", null).show();
            return true;
        });

        holder.ivMenu.setVisibility(View.GONE);
    }

    private void eliminarTurno(android.content.Context context, Turno turno) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (turno.atencionId != null) {
            db.collection("atenciones").document(turno.atencionId).get().addOnSuccessListener(doc -> {
                Atencion a = doc.toObject(Atencion.class);
                db.collection("atenciones").document(turno.atencionId).delete().addOnSuccessListener(u -> {
                    if (a != null) descontarSesionSiCorresponde(turno.pacienteId, a.getSesionesTotal());
                });
            });
        }
        db.collection("pacientes").document(turno.pacienteId).get().addOnSuccessListener(doc -> {
            Paciente p = doc.toObject(Paciente.class);
            if (p != null && p.getHorarios() != null) {
                List<HorarioAtencion> nuevosHorarios = new ArrayList<>(p.getHorarios());
                if (turno.horarioIndice >= 0 && turno.horarioIndice < nuevosHorarios.size()) {
                    nuevosHorarios.remove(turno.horarioIndice);
                }
                db.collection("pacientes").document(turno.pacienteId).update("horarios", nuevosHorarios)
                        .addOnSuccessListener(unused -> { if (listener != null) listener.onChange(turno, turno.atendido); });
            }
        });
    }

    private void descontarSesionSiCorresponde(String pacienteId, int totalDeLaAtencion) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("pacientes").document(pacienteId).get().addOnSuccessListener(doc -> {
            Paciente p = doc.toObject(Paciente.class);
            // PROTECCIÓN CUD: Solo restamos si NO es CUD y es el mismo ciclo
            if (p != null && !p.isCertificadoDiscapacidad() && p.getSesionesAtendidas() > 0 && p.getSesionesOrden() == totalDeLaAtencion) {
                db.collection("pacientes").document(pacienteId).update("sesionesAtendidas", FieldValue.increment(-1));
            }
        });
    }

    private void desmarcarAtencion(ViewHolder holder, Turno turno) {
        if (turno.atencionId == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("atenciones").document(turno.atencionId).get().addOnSuccessListener(doc -> {
            Atencion a = doc.toObject(Atencion.class);
            if (a == null) { finalizarDesmarcado(holder, turno); return; }
            
            int nroSesion = a.getSesionNumero();
            int totalSesion = a.getSesionesTotal();

            cacheSesionesHistoricas.put(generarClaveTurno(turno), new int[]{nroSesion - 1, totalSesion});

            db.collection("atenciones").document(turno.atencionId).delete().addOnSuccessListener(unused -> {
                db.collection("pacientes").document(turno.pacienteId).get().addOnSuccessListener(docP -> {
                    Paciente p = docP.toObject(Paciente.class);
                    // PROTECCIÓN CUD: Restar solo si NO es CUD y es el mismo ciclo
                    if (p != null && !p.isCertificadoDiscapacidad() && p.getSesionesAtendidas() > 0 && p.getSesionesOrden() == totalSesion) {
                        db.collection("pacientes").document(turno.pacienteId).update("sesionesAtendidas", FieldValue.increment(-1))
                            .addOnSuccessListener(u -> finalizarDesmarcado(holder, turno));
                    } else {
                        finalizarDesmarcado(holder, turno);
                    }
                });
            });
        });
    }

    private void finalizarDesmarcado(ViewHolder holder, Turno turno) {
        turno.atendido = false;
        actualizarEstado(holder, false);
        if (listener != null) listener.onChange(turno, false);
    }

    private void mostrarDialogoRegistroSesion(ViewHolder holder, Turno turno) {
        View dialogView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.dialog_registro_sesion, null);
        AlertDialog dialog = new AlertDialog.Builder(holder.itemView.getContext(), R.style.CustomDialogTheme).setView(dialogView).create();
        com.google.android.material.button.MaterialButton btnGuardar = dialogView.findViewById(R.id.btn_guardar_sesion);

        btnGuardar.setOnClickListener(v -> {
            btnGuardar.setEnabled(false);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("pacientes").document(turno.pacienteId).get().addOnSuccessListener(doc -> {
                Paciente p = doc.toObject(Paciente.class);
                if (p == null) { btnGuardar.setEnabled(true); return; }

                final int sesionNum;
                final int totalSes;
                final boolean esRestauracionFinal;

                // PROTECCIÓN CUD: Si tiene CUD, siempre es 0/0
                if (p.isCertificadoDiscapacidad()) {
                    sesionNum = 0;
                    totalSes = 0;
                    esRestauracionFinal = false;
                } else {
                    int[] cached = cacheSesionesHistoricas.get(generarClaveTurno(turno));
                    if (cached != null) {
                        sesionNum = cached[0] + 1;
                        totalSes = cached[1];
                        esRestauracionFinal = true;
                    } else {
                        sesionNum = p.getSesionesAtendidas() + 1;
                        totalSes = p.getSesionesOrden();
                        esRestauracionFinal = false;
                    }
                }

                Calendar cal = (Calendar) fechaAgenda.clone();
                String[] partes = turno.hora.split(":");
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(partes[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(partes[1]));

                Atencion atencion = new Atencion(turno.pacienteId, turno.nombrePaciente, turno.modalidad,
                        turno.tipoCobertura, turno.valorSesion, sesionNum, totalSes, "", new com.google.firebase.Timestamp(cal.getTime()));

                new AtencionRepository().guardar(atencion).addOnSuccessListener(a -> {
                    turno.atencionId = atencion.getId();
                    // PROTECCIÓN CUD: Solo sumamos al contador global si NO es CUD y es el mismo ciclo
                    if (!p.isCertificadoDiscapacidad() && !esRestauracionFinal && totalSes == p.getSesionesOrden()) {
                        db.collection("pacientes").document(turno.pacienteId).update("sesionesAtendidas", FieldValue.increment(1))
                            .addOnSuccessListener(u -> finalizarMarcado(holder, turno, dialog));
                    } else {
                        finalizarMarcado(holder, turno, dialog);
                    }
                });
            });
        });
        dialog.show();
    }

    private void finalizarMarcado(ViewHolder holder, Turno turno, AlertDialog dialog) {
        turno.atendido = true;
        actualizarEstado(holder, true);
        if (listener != null) listener.onChange(turno, true);
        dialog.dismiss();
    }

    private void actualizarEstado(ViewHolder holder, boolean atendido) {
        holder.ivAtendido.setImageResource(atendido ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
        holder.layoutTurno.setBackgroundColor(atendido ? ContextCompat.getColor(holder.itemView.getContext(), R.color.color_atendido) : Color.TRANSPARENT);
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
            tvHora = v.findViewById(R.id.tv_hora); tvNombre = v.findViewById(R.id.tv_nombre_paciente);
            tvDiagnostico = v.findViewById(R.id.tv_diagnostico_turno); tvObraSocial = v.findViewById(R.id.tv_obra_social_turno);
            tvTipoCobertura = v.findViewById(R.id.tv_tipo_cobertura); tvSesiones = v.findViewById(R.id.tv_sesiones);
            ivAtendido = v.findViewById(R.id.cb_atendido); ivMenu = v.findViewById(R.id.iv_menu_turno);
            layoutTurno = v.findViewById(R.id.layout_turno); tvModalidad = v.findViewById(R.id.tv_modalidad_turno);
        }
    }
}
