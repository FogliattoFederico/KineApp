package frgp.utn.edu.kineapp.ui.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class SimplePieChartView extends View {

    private List<Entry> entries = new ArrayList<>();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rectF = new RectF();

    public static class Entry {
        public String label;
        public float value;
        public int color;

        public Entry(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    public SimplePieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries.isEmpty()) return;

        float total = 0;
        for (Entry e : entries) total += e.value;

        float width = getWidth();
        float height = getHeight();
        
        // --- EFECTO 3D Y TORTA MÁS GRANDE ---
        // Aumentamos el tamaño de la torta (0.6f del ancho)
        float chartSize = Math.min(width, height) * 0.65f;
        float radius = chartSize / 2;
        float centerX = width / 2;
        float centerY = height * 0.35f; // Posición de la torta
        
        // Rectángulo base para la torta
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Dibujamos el "espesor" para el efecto 3D
        float depth = 25f;
        float startAngle = -90;

        // Capa inferior (Sombra/Espesor)
        for (Entry e : entries) {
            float sweepAngle = (e.value / total) * 360f;
            paint.setColor(darkenColor(e.color));
            
            // Dibujar el arco desplazado hacia abajo para simular profundidad
            RectF rectDepth = new RectF(rectF.left, rectF.top + depth, rectF.right, rectF.bottom + depth);
            canvas.drawArc(rectDepth, startAngle, sweepAngle, true, paint);
            
            startAngle += sweepAngle;
        }

        // Capa superior (Cara de la torta)
        startAngle = -90;
        for (Entry e : entries) {
            float sweepAngle = (e.value / total) * 360f;
            paint.setColor(e.color);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }

        // --- LEYENDAS (Bajadas al borde inferior) ---
        int textColor;
        int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            textColor = Color.WHITE;
        } else {
            textColor = Color.parseColor("#333333");
        }

        paint.setTextSize(42f);
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        
        float marginX = 50;
        float currentX = marginX;
        float boxSize = 40;
        
        // Calculamos la posición Y para que queden cerca del borde inferior del contenedor
        // Usamos una posición relativa al final del alto total
        float currentY = height - 60; 
        
        // Si hay muchas entradas, subimos un poco para que no se corten
        if (entries.size() > 2) {
            currentY = height - 110;
        }

        for (int i = entries.size() - 1; i >= 0; i--) {
            Entry e = entries.get(i);
            
            // Cuadradito de color
            paint.setColor(e.color);
            canvas.drawRect(currentX, currentY - boxSize, currentX + boxSize, currentY, paint);
            
            // Texto
            paint.setColor(textColor);
            String text = e.label + " (" + (int)e.value + ")";
            canvas.drawText(text, currentX + boxSize + 20, currentY, paint);
            
            float textWidth = paint.measureText(text);
            currentX += boxSize + textWidth + 80;
            
            // Si desborda horizontalmente (en sentido inverso o normal)
            if (currentX > width - marginX) {
                currentX = marginX;
                currentY -= 60; // Subimos una línea si necesitamos más espacio
            }
        }
    }

    private int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.7f; // Reducir brillo al 70%
        return Color.HSVToColor(hsv);
    }
}