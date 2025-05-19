package com.example.logintarea;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Numeros extends AppCompatActivity {

    private static final String TAG = "NumerosActivity";

    private TextView titleTextView;
    private TextView streakTextView;
    private LinearLayout numbersContainerLinearLayout;
    private TextView attemptsTextView;
    private TextView statusTextView;
    private Button checkButton;
    private LinearLayout successOverlay;
    private TextView successMessageTextView;
    private Button playAgainButton;

    private List<TextView> numberTextViews = new ArrayList<>();
    private List<Integer> correctOrder = new ArrayList<>();

    private int streak = 0;
    private int attempts = 2;
    private final int MAX_ATTEMPTS = 2;
    private final int NUM_COUNT = 5;

    private boolean lastRoundSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numeros);

        titleTextView = findViewById(R.id.titleTextView);
        streakTextView = findViewById(R.id.streakTextView);
        numbersContainerLinearLayout = findViewById(R.id.numbersContainerLinearLayout);
        attemptsTextView = findViewById(R.id.attemptsTextView);
        statusTextView = findViewById(R.id.statusTextView);
        checkButton = findViewById(R.id.checkButton);
        successOverlay = findViewById(R.id.successOverlay);
        successMessageTextView = findViewById(R.id.successMessageTextView);
        playAgainButton = findViewById(R.id.playAgainButton);

        numberTextViews.add(findViewById(R.id.number1TextView));
        numberTextViews.add(findViewById(R.id.number2TextView));
        numberTextViews.add(findViewById(R.id.number3TextView));
        numberTextViews.add(findViewById(R.id.number4TextView));
        numberTextViews.add(findViewById(R.id.number5TextView));

        numbersContainerLinearLayout.setOnDragListener(new NumberDragListener());

        for (TextView tv : numberTextViews) {
            @SuppressLint("ClickableViewAccessibility")
            View.OnTouchListener touchListener = new NumberTouchListener();
            tv.setOnTouchListener(touchListener);
        }

        checkButton.setOnClickListener(v -> checkOrder());
        playAgainButton.setOnClickListener(v -> startGame(lastRoundSuccessful));

        startGame(false);
    }

    private void startGame(boolean keepStreak) {
        Log.d(TAG, "Starting new game. Keep streak: " + keepStreak);
        attempts = MAX_ATTEMPTS;
        if (!keepStreak) {
            streak = 0;
        }
        lastRoundSuccessful = false;
        generateNewNumbers();
        resetLayout();
        updateStreakDisplay();
        updateAttemptsDisplay();
        statusTextView.setText("");
        statusTextView.setTextColor(ContextCompat.getColor(this, R.color.black));
        successOverlay.setVisibility(View.GONE);
        checkButton.setEnabled(true);
    }

    private void generateNewNumbers() {
        correctOrder.clear();
        Set<Integer> uniqueNumbers = new HashSet<>();
        Random rand = new Random();
        while (uniqueNumbers.size() < NUM_COUNT) {
            uniqueNumbers.add(rand.nextInt(10) + 1);
        }
        correctOrder.addAll(uniqueNumbers);
        Collections.sort(correctOrder);
        List<Integer> shuffledNumbers = new ArrayList<>(uniqueNumbers);
        Collections.shuffle(shuffledNumbers);
        for (int i = 0; i < NUM_COUNT; i++) {
            numberTextViews.get(i).setText(String.valueOf(shuffledNumbers.get(i)));
            numberTextViews.get(i).setBackgroundColor(ContextCompat.getColor(this, R.color.black_overlay));
            numberTextViews.get(i).setAlpha(1.0f);
        }
        Log.d(TAG, "Generated Numbers (Shuffled): " + shuffledNumbers);
        Log.d(TAG, "Correct Order: " + correctOrder);
    }

    private void resetLayout() {
        numbersContainerLinearLayout.removeAllViews();
        for (TextView tv : numberTextViews) {
            if (tv.getLayoutParams() == null) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1.0f
                );
                lp.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                tv.setLayoutParams(lp);
            }
            numbersContainerLinearLayout.addView(tv);
        }
        Log.d(TAG, "Layout reset with TextViews in default order.");
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void updateStreakDisplay() {
        streakTextView.setText(getString(R.string.racha_inicial, streak));
    }

    private void updateAttemptsDisplay() {
        attemptsTextView.setText(getString(R.string.intentos_inicial, attempts));
    }

    private List<Integer> getCurrentOrderFromViews() {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < numbersContainerLinearLayout.getChildCount(); i++) {
            View child = numbersContainerLinearLayout.getChildAt(i);
            if (child instanceof TextView && numberTextViews.contains(child)) {
                TextView tv = (TextView) child;
                try {
                    order.add(Integer.parseInt(tv.getText().toString()));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Failed to parse number from TextView: " + tv.getText(), e);
                    return Collections.emptyList();
                }
            }
        }
        Log.d(TAG, "Current order from views: " + order);
        return order;
    }

    private void checkOrder() {
        List<Integer> currentOrderDisplayed = getCurrentOrderFromViews();
        if (currentOrderDisplayed.size() != NUM_COUNT) {
            Log.e(TAG, "getCurrentOrderFromViews returned incorrect number of elements: " + currentOrderDisplayed.size());
            statusTextView.setText("Error al leer el orden.");
            statusTextView.setTextColor(ContextCompat.getColor(this, R.color.red_incorrect));
            return;
        }

        if (currentOrderDisplayed.equals(correctOrder)) {
            Log.d(TAG, "Order is correct!");
            statusTextView.setText(R.string.correcto);
            statusTextView.setTextColor(ContextCompat.getColor(this, R.color.green_correct));
            streak++;
            updateStreakDisplay();
            checkButton.setEnabled(false);
            successOverlay.setVisibility(View.VISIBLE);
            successMessageTextView.setText(getString(R.string.mensaje_felicitacion));
            playAgainButton.setText(R.string.jugar_otra_vez_boton);
            lastRoundSuccessful = true;
        } else {
            Log.d(TAG, "Order is incorrect.");
            attempts--;
            updateAttemptsDisplay();
            statusTextView.setText(R.string.incorrecto);
            statusTextView.setTextColor(ContextCompat.getColor(this, R.color.red_incorrect));
            if (attempts <= 0) {
                Log.d(TAG, "Out of attempts. Game over for this round.");
                checkButton.setEnabled(false);
                streak = 0;
                updateStreakDisplay();
                successOverlay.setVisibility(View.VISIBLE);
                successMessageTextView.setText(getString(R.string.mensaje_juego_terminado_fallo_simple));
                playAgainButton.setText(R.string.jugar_otra_vez_boton);
                lastRoundSuccessful = false;
            }
        }
    }

    private View findChildViewUnder(ViewGroup parent, float x, float y) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getVisibility() == View.VISIBLE && child instanceof TextView && numberTextViews.contains(child)) {
                Rect hitRect = new Rect();
                child.getHitRect(hitRect);
                if (hitRect.contains((int) x, (int) y)) {
                    return child;
                }
            }
        }
        return null;
    }

    private class NumberTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (!checkButton.isEnabled()) {
                return false;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDragAndDrop(data, shadowBuilder, view, 0);
                view.setAlpha(0.3f);
                return true;
            }
            return false;
        }
    }

    private class NumberDragListener implements View.OnDragListener {
        private View viewBeingDraggedOver = null;

        @Override
        public boolean onDrag(View v, DragEvent event) {
            final LinearLayout container = (LinearLayout) v;
            final View draggedView = (View) event.getLocalState();
            if (draggedView == null || !(draggedView instanceof TextView) || !numberTextViews.contains(draggedView) || !(v instanceof LinearLayout)) {
                return false;
            }

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d(TAG, "Drag started");
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d(TAG, "Drag entered container");
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    View viewUnder = findChildViewUnder(container, event.getX(), event.getY());
                    if (viewUnder != null && viewUnder != draggedView) {
                        if (viewBeingDraggedOver != viewUnder) {
                            if (viewBeingDraggedOver != null) {
                                viewBeingDraggedOver.setAlpha(1.0f);
                            }
                            viewUnder.setAlpha(0.5f);
                            viewBeingDraggedOver = viewUnder;
                        }
                    } else {
                        if (viewBeingDraggedOver != null) {
                            viewBeingDraggedOver.setAlpha(1.0f);
                            viewBeingDraggedOver = null;
                        }
                    }
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d(TAG, "Drag exited container");
                    if (viewBeingDraggedOver != null) {
                        viewBeingDraggedOver.setAlpha(1.0f);
                        viewBeingDraggedOver = null;
                    }
                    return true;
                case DragEvent.ACTION_DROP:
                    Log.d(TAG, "Drag dropped");
                    View droppedOnView = findChildViewUnder(container, event.getX(), event.getY());
                    for(TextView tv : numberTextViews) {
                        tv.setAlpha(1.0f);
                    }
                    viewBeingDraggedOver = null;
                    if (droppedOnView != null && droppedOnView != draggedView) {
                        int draggedIndex = container.indexOfChild(draggedView);
                        int droppedOnIndex = container.indexOfChild(droppedOnView);
                        if (draggedIndex != droppedOnIndex) {
                            container.removeView(draggedView);
                            container.removeView(droppedOnView);
                            if (draggedIndex < droppedOnIndex) {
                                container.addView(droppedOnView, draggedIndex);
                                container.addView(draggedView, droppedOnIndex);
                            } else {
                                container.addView(draggedView, droppedOnIndex);
                                container.addView(droppedOnView, draggedIndex);
                            }
                            Log.d(TAG, "Swapped views. original draggedIndex=" + draggedIndex + ", original droppedOnIndex=" + droppedOnIndex);
                            return true;
                        } else {
                            Log.d(TAG, "Dropped back on itself");
                            return false;
                        }
                    } else {
                        Log.d(TAG, "Dropped on invalid target (or self)");
                        return false;
                    }
                case DragEvent.ACTION_DRAG_ENDED:
                    Log.d(TAG, "Drag ended. Result: " + event.getResult());
                    if (draggedView != null) {
                        draggedView.setAlpha(1.0f);
                    }
                    if (viewBeingDraggedOver != null) {
                        viewBeingDraggedOver.setAlpha(1.0f);
                        viewBeingDraggedOver = null;
                    }
                    return true;
                default:
                    return false;
            }
        }
    }
}