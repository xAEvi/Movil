package com.example.logintarea;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class VocalTraceView extends View {

    private Paint linePaint;
    private Paint guidePointPaint;
    private Paint successLinePaint;

    private Bitmap backgroundImagePunteada;
    private Bitmap backgroundImageFormada;

    private List<PointF> currentVocalPoints;
    private List<Boolean> segmentsDrawn;
    private int nextPointToReachIndex;

    private Path userPath;
    private Path successPath;
    private PointF lastPoint;

    private static final float TOUCH_TOLERANCE_RADIUS = 60f;
    private boolean traceCompleted = false;
    private boolean showFormedVowel = false;
    private float imageOffsetX, imageOffsetY;

    public interface OnTraceCompleteListener {
        void onComplete();
    }
    private OnTraceCompleteListener onTraceCompleteListener;

    public VocalTraceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Paint para las líneas del usuario
        linePaint = new Paint();
        linePaint.setColor(Color.BLUE);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(25f);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        // Paint para las líneas ya completadas
        successLinePaint = new Paint(linePaint);
        successLinePaint.setColor(Color.GREEN);

        // Paint para los puntos guía (opcional)
        guidePointPaint = new Paint();
        guidePointPaint.setColor(Color.RED);
        guidePointPaint.setStyle(Paint.Style.FILL);

        currentVocalPoints = new ArrayList<>();
        segmentsDrawn = new ArrayList<>();
        userPath = new Path();
        successPath = new Path();
    }

    public void setVocal(int dottedImageResId, int formedImageResId, List<PointF> points) {
        // Cargar y escalar las imágenes a 500x500
        this.backgroundImagePunteada = scaleImageToSize(dottedImageResId, 500, 500);
        this.backgroundImageFormada = scaleImageToSize(formedImageResId, 500, 500);

        this.currentVocalPoints.clear();
        this.currentVocalPoints.addAll(points);

        this.segmentsDrawn.clear();
        for (int i = 0; i < points.size() - 1; i++) {
            segmentsDrawn.add(false);
        }

        this.nextPointToReachIndex = 0;
        this.traceCompleted = false;
        this.showFormedVowel = false;
        this.userPath.reset();
        this.successPath.reset();
        this.lastPoint = null;

        invalidate();
    }

    // Escalado de imagen
    private Bitmap scaleImageToSize(int resourceId, int width, int height) {
        Bitmap original = BitmapFactory.decodeResource(getResources(), resourceId);
        return Bitmap.createScaledBitmap(original, width, height, true);
    }

    public void setOnTraceCompleteListener(OnTraceCompleteListener listener) {
        this.onTraceCompleteListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Centrado de imagen
        if (backgroundImagePunteada != null) {
            imageOffsetX = (getWidth() - backgroundImagePunteada.getWidth()) / 2f;
            imageOffsetY = (getHeight() - backgroundImagePunteada.getHeight()) / 2f;
        }

        if (showFormedVowel && backgroundImageFormada != null) {
            canvas.drawBitmap(backgroundImageFormada, imageOffsetX, imageOffsetY, null);
        } else if (backgroundImagePunteada != null) {
            canvas.drawBitmap(backgroundImagePunteada, imageOffsetX, imageOffsetY, null);

            canvas.drawPath(userPath, linePaint);

            canvas.drawPath(successPath, successLinePaint);

            for (PointF point : currentVocalPoints) {
                canvas.drawCircle(point.x + imageOffsetX, point.y + imageOffsetY, 15f, guidePointPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (traceCompleted || currentVocalPoints.isEmpty()) {
            return true;
        }

        float x = event.getX();
        float y = event.getY();
        float adjustedX = x - imageOffsetX;
        float adjustedY = y - imageOffsetY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                userPath.reset();
                userPath.moveTo(x, y);
                lastPoint = new PointF(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                userPath.lineTo(x, y);

                if (nextPointToReachIndex < currentVocalPoints.size()) {
                    PointF targetPoint = currentVocalPoints.get(nextPointToReachIndex);
                    PointF canvasTargetPoint = new PointF(targetPoint.x + imageOffsetX, targetPoint.y + imageOffsetY);

                    if (isNearPoint(x, y, canvasTargetPoint, TOUCH_TOLERANCE_RADIUS)) {
                        successPath.moveTo(lastPoint.x, lastPoint.y);
                        successPath.lineTo(canvasTargetPoint.x, canvasTargetPoint.y);

                        if (nextPointToReachIndex > 0) {
                            segmentsDrawn.set(nextPointToReachIndex - 1, true);
                        }

                        lastPoint = new PointF(canvasTargetPoint.x, canvasTargetPoint.y);
                        nextPointToReachIndex++;

                        if (nextPointToReachIndex >= currentVocalPoints.size()) {
                            traceCompleted = true;
                            showFormedVowel = true;
                            if (onTraceCompleteListener != null) {
                                onTraceCompleteListener.onComplete();
                            }
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                break;
        }

        invalidate();
        return true;
    }

    private boolean isNearPoint(float touchX, float touchY, PointF point, float tolerance) {
        float dx = touchX - point.x;
        float dy = touchY - point.y;
        return (dx * dx + dy * dy) < (tolerance * tolerance);
    }

    public void animateFormedVowel() {
        this.setAlpha(0f);
        this.animate().alpha(1f).setDuration(500).start();
    }
}