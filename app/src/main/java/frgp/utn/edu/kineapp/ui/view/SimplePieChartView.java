package frgp.utn.edu.kineapp.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import java.util.ArrayList;
import java.util.List;

public class SimplePieChartView extends View {

    private List<Entry> entries = new ArrayList<>();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rectF = new RectF();
    private float animationSweep = 0f; // 0.0 a 1.0

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
        animateChart();
    }

    public void animateChart() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animationSweep = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries.isEmpty()) return;

        float total = 0;
        for (Entry e : entries) total += e.value;

        float width = getWidth();
        float height = getHeight();
        
        // --- GRÁFICO ---
        float chartSize = Math.min(width, height) * 0.60f; 
        float radius = chartSize / 2;
        float centerX = width / 2;
        float centerY = height * 0.38f; 
        
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        float depth = 30f;
        float startAngle = -90;

        // Espesor 3D
        for (Entry e : entries) {
            float sweepAngle = (e.value / total) * 360f * animationSweep;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(darkenColor(e.color, 0.6f));
            for (int i = 1; i <= depth; i++) {
                RectF sideRect = new RectF(rectF.left, rectF.top + i, rectF.right, rectF.bottom + i);
                canvas.drawArc(sideRect, startAngle, sweepAngle, true, paint);
            }
            startAngle += sweepAngle;
        }

        // Cara Superior
        startAngle = -90;
        for (Entry e : entries) {
            float sweepAngle = (e.value / total) * 360f * animationSweep;
            int colorStart = e.color;
            int colorEnd = darkenColor(e.color, 0.85f);
            Shader shader = new LinearGradient(rectF.left, rectF.top, rectF.right, rectF.bottom, 
                                             colorStart, colorEnd, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            paint.setShader(null);
            startAngle += sweepAngle;
        }

        // Hueco Central (Donut)
        float holeRadius = radius * 0.55f;
        int backgroundColor;
        int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        backgroundColor = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) ? Color.parseColor("#1E1E1E") : Color.WHITE;
        
        paint.setColor(Color.argb(30, 0, 0, 0));
        canvas.drawCircle(centerX, centerY + depth, holeRadius, paint);
        paint.setColor(backgroundColor);
        canvas.drawCircle(centerX, centerY, holeRadius, paint);

        // --- PORCENTAJES SOBRE LAS PORCIONES ---
        if (animationSweep > 0.8f) { // Solo mostrar cuando la animación esté terminando
            paint.setColor(Color.WHITE);
            paint.setTextSize(34f);
            paint.setAlpha((int)((animationSweep - 0.8f) * 5 * 255)); // Fade in al final
            paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
            paint.setTextAlign(Paint.Align.CENTER);
            
            startAngle = -90;
            for (Entry e : entries) {
                float sweepAngle = (e.value / total) * 360f * animationSweep;
                if (sweepAngle > 15) { 
                    float middleAngle = startAngle + sweepAngle / 2;
                    double radians = Math.toRadians(middleAngle);
                    float textRadius = holeRadius + (radius - holeRadius) / 2;
                    float tx = (float) (centerX + textRadius * Math.cos(radians));
                    float ty = (float) (centerY + textRadius * Math.sin(radians)) + 12f;
                    
                    String percentText = Math.round((e.value / total) * 100) + "%";
                    canvas.drawText(percentText, tx, ty, paint);
                }
                startAngle += sweepAngle;
            }
            paint.setAlpha(255);
        }

        // --- LEYENDAS ---
        int textColor = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) ? Color.WHITE : Color.parseColor("#333333");
        paint.setTextSize(38f); 
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        
        float marginX = 40;
        float currentX = marginX;
        float boxSize = 35;
        float currentY = height - 40; 

        for (Entry e : entries) {
            String text = e.label + " (" + (int)e.value + ")";
            float textWidth = paint.measureText(text);
            float itemWidth = boxSize + 20 + textWidth;

            if (currentX + itemWidth > width - marginX) {
                currentX = marginX;
                currentY -= 60;
            }

            paint.setColor(e.color);
            canvas.drawRoundRect(currentX, currentY - boxSize, currentX + boxSize, currentY, 8, 8, paint);
            
            paint.setColor(textColor);
            canvas.drawText(text, currentX + boxSize + 15, currentY - 5, paint);
            
            currentX += itemWidth + 50;
        }
    }

    private int darkenColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor;
        return Color.HSVToColor(hsv);
    }
}