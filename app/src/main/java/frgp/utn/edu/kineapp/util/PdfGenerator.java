package frgp.utn.edu.kineapp.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.model.OrdenRemito;
import frgp.utn.edu.kineapp.model.Remito;

public class PdfGenerator {

    public static void generarRemitoPdf(Context context, Remito remito, Map<String, Object> kine) {
        PdfDocument document = new PdfDocument();
        // A4: 595 x 842 points
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // --- ENCABEZADO ---
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        canvas.drawRect(20, 20, 575, 120, paint); // Recuadro encabezado
        canvas.drawLine(150, 20, 150, 120, paint);
        canvas.drawLine(400, 20, 400, 120, paint);

        // Logo del Colegio
        try {
            int resId = context.getResources().getIdentifier("logo_colegio", "drawable", context.getPackageName());
            if (resId != 0) {
                Bitmap logo = BitmapFactory.decodeResource(context.getResources(), resId);
                if (logo != null) {
                    Rect dest = new Rect(20, 20, 150, 120);
                    canvas.drawBitmap(logo, null, dest, null);
                }
            } else {
                dibujarPlaceholderLogo(canvas, paint);
            }
        } catch (Exception e) {
            dibujarPlaceholderLogo(canvas, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(10f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Colegio de Kinesiólogos, Fisioterapeutas", 160, 40, paint);
        canvas.drawText("y Terapistas Físicos de la Pcia. de Santa Fe", 160, 55, paint);
        
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(8f);
        canvas.drawText("2ª Circunscripción", 230, 70, paint);
        canvas.drawText("Ley 3950 y todas sus modificaciones", 200, 80, paint);
        canvas.drawText("Tel.: 48111156 / Tel/Fax: 4827012", 210, 90, paint);
        canvas.drawText("www.colegiokinesio.com.ar", 220, 100, paint);
        canvas.drawText("Ocampo 560 - 2000 Rosario", 225, 115, paint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(11f);
        canvas.drawText("REMITO DE PRESTACIONES", 410, 60, paint);
        canvas.drawText("FISIO-KINESICAS", 435, 80, paint);

        // --- DATOS PROFESIONAL ---
        paint.setTextSize(10f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Profesional: " + kine.get("nombre") + " " + kine.get("apellido"), 30, 150, paint);
        canvas.drawText("Matrícula: " + kine.get("matricula"), 450, 150, paint);
        
        // Uso del campo dinámico "departamento"
        String depto = (String) kine.get("departamento");
        canvas.drawText("Departamento: " + (depto != null ? depto : ""), 30, 175, paint);
        
        String modalidad = (String) kine.get("modalidadTrabajo");
        if ("ambos".equals(modalidad)) modalidad = "Domicilio / Consultorio";
        else if ("domicilio".equals(modalidad)) modalidad = "Domicilio";
        else modalidad = "Consultorio";
        
        canvas.drawText("Lugar de atención: " + modalidad, 380, 175, paint);
        
        String periodo = remito.getPeriodoRemito() != null ? remito.getPeriodoRemito() : "";
        canvas.drawText("Quincena/Mes/Año: " + periodo, 30, 200, paint);

        // --- TABLA ---
        float tableTop = 220;
        float headerHeight = 30;
        float rowHeight = 18;
        float tableEnd = 575;
        
        float col_id = 20;
        float col_os = 45;
        float col_os_nro = 145;
        float col_practica = 195;
        float col_practica_cod = 235;
        float col_fecha = 290;
        float col_paciente = 350;

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(20, tableTop, tableEnd, tableTop + headerHeight + (28 * rowHeight), paint);
        canvas.drawLine(col_os, tableTop + 15, col_fecha, tableTop + 15, paint);
        canvas.drawLine(20, tableTop + headerHeight, tableEnd, tableTop + headerHeight, paint);

        canvas.drawLine(col_os, tableTop, col_os, tableTop + headerHeight + (28 * rowHeight), paint);
        canvas.drawLine(col_os_nro, tableTop + 15, col_os_nro, tableTop + headerHeight + (28 * rowHeight), paint);
        canvas.drawLine(col_practica, tableTop, col_practica, tableTop + headerHeight + (28 * rowHeight), paint);
        canvas.drawLine(col_practica_cod, tableTop + 15, col_practica_cod, tableTop + headerHeight + (28 * rowHeight), paint);
        canvas.drawLine(col_fecha, tableTop, col_fecha, tableTop + headerHeight + (28 * rowHeight), paint);
        canvas.drawLine(col_paciente, tableTop, col_paciente, tableTop + headerHeight + (28 * rowHeight), paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(9f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("#", col_id + 8, tableTop + 20, paint);
        canvas.drawText("Obra Social", col_os + 40, tableTop + 12, paint);
        canvas.drawText("Nombre", col_os + 30, tableTop + 25, paint);
        canvas.drawText("Nro.", col_os_nro + 10, tableTop + 25, paint);
        canvas.drawText("Práctica", col_practica + 25, tableTop + 12, paint);
        canvas.drawText("Cant", col_practica + 5, tableTop + 25, paint);
        canvas.drawText("Código", col_practica_cod + 10, tableTop + 25, paint);
        canvas.drawText("Fecha", col_fecha + 15, tableTop + 20, paint);
        canvas.drawText("Paciente/Nº Afiliado", col_paciente + 50, tableTop + 20, paint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        List<OrdenRemito> ordenes = remito.getOrdenes();
        for (int i = 0; i < 28; i++) {
            float y = tableTop + headerHeight + (i * rowHeight);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(20, y + rowHeight, tableEnd, y + rowHeight, paint);
            
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(String.valueOf(i + 1), col_id + 5, y + 13, paint);
            
            if (ordenes != null && i < ordenes.size()) {
                OrdenRemito o = ordenes.get(i);
                canvas.drawText(o.getObraSocialNombre() != null ? o.getObraSocialNombre() : "", col_os + 5, y + 13, paint);
                canvas.drawText(String.valueOf(o.getCantidadSesiones()), col_practica + 12, y + 13, paint);
                canvas.drawText(o.getCodigoPractica() != null ? o.getCodigoPractica() : "", col_practica_cod + 5, y + 13, paint);
                canvas.drawText(o.getFecha() != null ? o.getFecha() : "", col_fecha + 5, y + 13, paint);
                canvas.drawText(o.getPacienteNombreCompleto() + " / " + (o.getNumeroAfiliado() != null ? o.getNumeroAfiliado() : ""), col_paciente + 5, y + 13, paint);
            }
        }

        float footerY = tableTop + headerHeight + (28 * rowHeight) + 60;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Cantidad de órdenes: " + (ordenes != null ? ordenes.size() : 0), 30, footerY, paint);
        
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(230, footerY + 5, 370, footerY + 5, paint);
        canvas.drawLine(400, footerY + 5, 570, footerY + 5, paint);
        
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText("Firma:", 230, footerY, paint);
        canvas.drawText("Aclaración:", 400, footerY, paint);

        document.finishPage(page);
        String fileName = "Remito_" + (remito.getPeriodoRemito() != null ? remito.getPeriodoRemito().replace("/", "_") : "sin_numero") + ".pdf";
        File file = new File(context.getExternalFilesDir(null), fileName);
        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            compartirPdf(context, file);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private static void dibujarPlaceholderLogo(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(85, 70, 45, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(40f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("K", 70, 84, paint);
    }

    private static void compartirPdf(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Compartir Remito"));
    }
}