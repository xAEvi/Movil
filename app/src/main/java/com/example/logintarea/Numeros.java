package com.example.logintarea; // Replace with your app's package name

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

    private static final String TAG = "NumerosActivity"; // For logging

    private TextView titleTextView;
    private TextView streakTextView;
    private LinearLayout numbersContainerLinearLayout;
    private TextView attemptsTextView;
    private TextView statusTextView;
    private Button checkButton;
    private LinearLayout successOverlay; // Used for success/failure message overlay
    private TextView successMessageTextView;
    private Button playAgainButton;

    // List to hold references to the 5 TextViews defined in XML
    private List<TextView> numberTextViews = new ArrayList<>();
    // List to hold the correct sorted order of the numbers for the current round
    private List<Integer> correctOrder = new ArrayList<>();

    private int streak = 0;
    private int attempts = 2;
    private final int MAX_ATTEMPTS = 2;
    private final int NUM_COUNT = 5; // Number of TextViews/numbers in the game

    // Flag to track if the last round was successful for streak handling
    private boolean lastRoundSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numeros); // Make sure this matches your XML layout file name

        // 1. Find UI elements
        titleTextView = findViewById(R.id.titleTextView);
        streakTextView = findViewById(R.id.streakTextView);
        numbersContainerLinearLayout = findViewById(R.id.numbersContainerLinearLayout);
        attemptsTextView = findViewById(R.id.attemptsTextView);
        statusTextView = findViewById(R.id.statusTextView);
        checkButton = findViewById(R.id.checkButton);
        successOverlay = findViewById(R.id.successOverlay);
        successMessageTextView = findViewById(R.id.successMessageTextView);
        playAgainButton = findViewById(R.id.playAgainButton);

        // Find the 5 TextViews inside the LinearLayout and store references
        // Note: The order here matches the XML initially, but drag/drop will change LinearLayout child order.
        numberTextViews.add(findViewById(R.id.number1TextView));
        numberTextViews.add(findViewById(R.id.number2TextView));
        numberTextViews.add(findViewById(R.id.number3TextView));
        numberTextViews.add(findViewById(R.id.number4TextView));
        numberTextViews.add(findViewById(R.id.number5TextView));

        // 2. Set up Drag Listener for the container (where views can be dropped)
        numbersContainerLinearLayout.setOnDragListener(new NumberDragListener());

        // 3. Set up Touch Listener for each number TextView (to start the drag)
        for (TextView tv : numberTextViews) {
            // Suppress lint warning about missing performClick() as we handle touch for drag
            @SuppressLint("ClickableViewAccessibility")
            View.OnTouchListener touchListener = new NumberTouchListener();
            tv.setOnTouchListener(touchListener);
        }

        // 4. Set up Button Listeners
        checkButton.setOnClickListener(v -> checkOrder());
        playAgainButton.setOnClickListener(v -> startGame(lastRoundSuccessful)); // Start a new game, preserving streak if last round was successful

        // 5. Start the first game
        startGame(false); // Start the very first game with streak reset
    }

    /**
     * Initializes or restarts the game.
     * @param keepStreak True to keep the current streak, false to reset it to 0.
     */
    private void startGame(boolean keepStreak) {
        Log.d(TAG, "Starting new game. Keep streak: " + keepStreak);

        // Reset attempts
        attempts = MAX_ATTEMPTS;

        // Reset streak if not keeping it
        if (!keepStreak) {
            streak = 0;
        }
        lastRoundSuccessful = false; // Reset the flag for the new round

        // Generate and shuffle new numbers
        generateNewNumbers();

        // Reset the layout of the TextViews
        resetLayout();

        // Update UI elements
        updateStreakDisplay();
        updateAttemptsDisplay();
        statusTextView.setText(""); // Clear status text
        statusTextView.setTextColor(ContextCompat.getColor(this, R.color.black)); // Reset status text color (using black for default)
        successOverlay.setVisibility(View.GONE); // Hide success/failure overlay
        checkButton.setEnabled(true); // Enable check button
    }

    /**
     * Generates a new set of unique random numbers, stores them,
     * and determines the correct sorted order.
     */
    private void generateNewNumbers() {
        // currentNumbersDisplayed.clear(); // This list is populated from TextViews later
        correctOrder.clear();

        Set<Integer> uniqueNumbers = new HashSet<>();
        Random rand = new Random();
        while (uniqueNumbers.size() < NUM_COUNT) {
            uniqueNumbers.add(rand.nextInt(10) + 1);
        }

        // Put generated numbers into the correctOrder list and sort it
        correctOrder.addAll(uniqueNumbers);
        Collections.sort(correctOrder);

        // Put generated numbers into a temporary list to shuffle for display
        List<Integer> shuffledNumbers = new ArrayList<>(uniqueNumbers);
        Collections.shuffle(shuffledNumbers);

        // Assign shuffled numbers to the TextViews (this updates their text)
        // The actual order of TextViews in the layout is reset in resetLayout()
        for (int i = 0; i < NUM_COUNT; i++) {
            numberTextViews.get(i).setText(String.valueOf(shuffledNumbers.get(i)));
            // Reset background and alpha from any drag state
            numberTextViews.get(i).setBackgroundColor(ContextCompat.getColor(this, R.color.black_overlay)); // Or your default color
            numberTextViews.get(i).setAlpha(1.0f); // Reset alpha
        }

        Log.d(TAG, "Generated Numbers (Shuffled): " + shuffledNumbers);
        Log.d(TAG, "Correct Order: " + correctOrder);
    }

    /**
     * Resets the LinearLayout children back to their original order (from XML)
     * and adds the TextViews back. Their text was updated in generateNewNumbers().
     */
    private void resetLayout() {
        // Important: Before removing, ensure the TextViews still have layout params if needed.
        // Adding them back to a LinearLayout usually works fine if they had LayoutParams
        // designed for LinearLayout (like layout_weight=1). The XML defines this.
        numbersContainerLinearLayout.removeAllViews(); // Clear existing views

        // Add the TextViews back in their default order as found in onCreate
        for (TextView tv : numberTextViews) {
            // Ensure the TextView has its original layout parameters if they were lost
            // This line might be necessary if removeViewInLayout or similar was used elsewhere,
            // but removeView usually keeps them attached for later re-adding.
            if (tv.getLayoutParams() == null) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0, // width (0 because of weight)
                        ViewGroup.LayoutParams.MATCH_PARENT, // height
                        1.0f // weight
                );
                lp.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4)); // Re-add margins from XML
                tv.setLayoutParams(lp);
            }
            numbersContainerLinearLayout.addView(tv);
        }
        Log.d(TAG, "Layout reset with TextViews in default order.");
    }

    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


    /**
     * Updates the streak display TextView.
     */
    private void updateStreakDisplay() {
        // Uses the string resource with %d
        streakTextView.setText(getString(R.string.racha_inicial, streak));
    }

    /**
     * Updates the attempts display TextView.
     */
    private void updateAttemptsDisplay() {
        // Uses the string resource with %d
        attemptsTextView.setText(getString(R.string.intentos_inicial, attempts));
    }

    /**
     * Reads the numbers from the TextViews in their current order
     * within the LinearLayout.
     * @return A List of Integers representing the current order.
     */
    private List<Integer> getCurrentOrderFromViews() {
        List<Integer> order = new ArrayList<>();
        // Iterate through the children of the LinearLayout in their current layout order
        for (int i = 0; i < numbersContainerLinearLayout.getChildCount(); i++) {
            View child = numbersContainerLinearLayout.getChildAt(i);
            // Only process our number TextViews
            if (child instanceof TextView && numberTextViews.contains(child)) {
                TextView tv = (TextView) child;
                try {
                    order.add(Integer.parseInt(tv.getText().toString()));
                } catch (NumberFormatException e) {
                    // Should not happen if we only put numbers in TextViews
                    Log.e(TAG, "Failed to parse number from TextView: " + tv.getText(), e);
                    // Optionally show an error message to the user
                    // Toast.makeText(this, "Error interno: número inválido encontrado.", Toast.LENGTH_SHORT).show();
                    return Collections.emptyList(); // Indicate error
                }
            }
        }
        Log.d(TAG, "Current order from views: " + order);
        return order;
    }

    /**
     * Checks if the current order of numbers matches the correct sorted order.
     * Updates UI, streak, and attempts accordingly.
     */
    private void checkOrder() {
        List<Integer> currentOrderDisplayed = getCurrentOrderFromViews();

        // Ensure we got valid numbers (or handle the empty list case from parse error)
        if (currentOrderDisplayed.size() != NUM_COUNT) {
            Log.e(TAG, "getCurrentOrderFromViews returned incorrect number of elements: " + currentOrderDisplayed.size());
            statusTextView.setText("Error al leer el orden.");
            statusTextView.setTextColor(ContextCompat.getColor(this, R.color.red_incorrect));
            // Decrease attempts for an internal error? Maybe not. Or maybe end the game.
            return; // Stop check if there was an error
        }

        // Compare current order with correct order
        if (currentOrderDisplayed.equals(correctOrder)) {
            // Correct order!
            Log.d(TAG, "Order is correct!");
            statusTextView.setText(R.string.correcto);
            statusTextView.setTextColor(ContextCompat.getColor(this, R.color.green_correct));

            streak++; // Increase streak
            updateStreakDisplay();

            checkButton.setEnabled(false); // Disable check until next round
            successOverlay.setVisibility(View.VISIBLE);
            successMessageTextView.setText(getString(R.string.mensaje_felicitacion));
            playAgainButton.setText(R.string.jugar_otra_vez_boton); // Ensure button text is "Play Again"
            lastRoundSuccessful = true; // Set flag for next game
        } else {
            // Incorrect order
            Log.d(TAG, "Order is incorrect.");
            attempts--; // Decrease attempts
            updateAttemptsDisplay();
            statusTextView.setText(R.string.incorrecto);
            statusTextView.setTextColor(ContextCompat.getColor(this, R.color.red_incorrect));

            if (attempts <= 0) {
                // Game over for this round (failed all attempts)
                Log.d(TAG, "Out of attempts. Game over for this round.");
                checkButton.setEnabled(false); // Disable check button

                streak = 0; // Reset streak on failure
                updateStreakDisplay();

                // Show failure message in the overlay
                successOverlay.setVisibility(View.VISIBLE);
                successMessageTextView.setText(getString(R.string.mensaje_juego_terminado_fallo_simple));
                playAgainButton.setText(R.string.jugar_otra_vez_boton); // Button text remains "Play Again"
                lastRoundSuccessful = false; // Set flag for next game
            }
            // If attempts > 0, nothing else happens, user can rearrange and check again.
        }
    }


    // --- Helper method to find a child view under specific coordinates ---
    /**
     * Finds the child view within a ViewGroup whose bounds contain the given coordinates.
     * Coordinates are relative to the ViewGroup's top-left corner.
     * @param parent The ViewGroup to search within.
     * @param x The x-coordinate relative to the parent.
     * @param y The y-coordinate relative to the parent.
     * @return The child View at the given coordinates, or null if none found.
     */
    private View findChildViewUnder(ViewGroup parent, float x, float y) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            // We only care about visible number TextViews
            if (child.getVisibility() == View.VISIBLE && child instanceof TextView && numberTextViews.contains(child)) {
                // Get the bounds of the child relative to its parent
                Rect hitRect = new Rect();
                child.getHitRect(hitRect); // getHitRect gives bounds relative to the parent

                // Check if the point (x, y) is inside the child's bounds
                if (hitRect.contains((int) x, (int) y)) {
                    return child;
                }
            }
        }
        return null; // No child found under the coordinates
    }
    // --------------------------------------------------------------------


    /**
     * Custom OnTouchListener to initiate the drag operation for a TextView.
     */
    private class NumberTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (!checkButton.isEnabled()) {
                // Don't allow dragging if the game round is over (check button disabled)
                return false;
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                // Create clip data (not strictly needed for local drag but good practice)
                ClipData data = ClipData.newPlainText("", "");
                // Create a drag shadow
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                // Start the drag operation
                // The view itself is passed as local state, so we can access it in the drop listener
                view.startDragAndDrop(data, shadowBuilder, view, 0);

                // Make the original view temporarily less visible while dragging
                view.setAlpha(0.3f); // Make dragged view semi-transparent
                return true; // Consume the touch event
            }
            // Don't consume other touch actions like ACTION_MOVE, ACTION_UP etc.
            return false;
        }
    }

    /**
     * Custom OnDragListener for the LinearLayout container.
     * Handles drag entry/exit/location feedback and the drop action.
     */
    private class NumberDragListener implements View.OnDragListener {

        // Keep track of the view currently being dragged over for highlighting
        private View viewBeingDraggedOver = null;

        @Override
        public boolean onDrag(View v, DragEvent event) {
            // The container is the LinearLayout numbersContainerLinearLayout
            final LinearLayout container = (LinearLayout) v;
            // The view being dragged is passed as local state from startDragAndDrop
            final View draggedView = (View) event.getLocalState();

            // Ensure the dragged view is one of our number TextViews and container is valid
            if (draggedView == null || !(draggedView instanceof TextView) || !numberTextViews.contains(draggedView) || !(v instanceof LinearLayout)) {
                return false; // Not handling this drag
            }

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d(TAG, "Drag started");
                    // Alpha is already set in the touch listener
                    return true; // Indicate that the container can accept the drag

                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d(TAG, "Drag entered container");
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:
                    // Provide visual feedback for which view is being dragged over
                    // --- Use the helper method here ---
                    View viewUnder = findChildViewUnder(container, event.getX(), event.getY());

                    if (viewUnder != null && viewUnder != draggedView) {
                        // We are dragging over a valid target TextView, and it's not the one being dragged
                        if (viewBeingDraggedOver != viewUnder) {
                            // New view is being dragged over, reset the old one
                            if (viewBeingDraggedOver != null) {
                                // Restore alpha or background of the previously highlighted view
                                viewBeingDraggedOver.setAlpha(1.0f); // Restore normal alpha
                            }
                            // Highlight the new view
                            viewUnder.setAlpha(0.5f); // Make target semi-transparent
                            viewBeingDraggedOver = viewUnder;
                        }
                    } else {
                        // Dragging over empty space, container itself, or the dragged view's original spot
                        if (viewBeingDraggedOver != null) {
                            // Restore alpha/background of the previously highlighted view
                            viewBeingDraggedOver.setAlpha(1.0f); // Restore normal alpha
                            viewBeingDraggedOver = null;
                        }
                    }
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d(TAG, "Drag exited container");
                    // Reset highlighting on any view that was being dragged over
                    if (viewBeingDraggedOver != null) {
                        viewBeingDraggedOver.setAlpha(1.0f); // Restore normal alpha
                        viewBeingDraggedOver = null;
                    }
                    return true;

                case DragEvent.ACTION_DROP:
                    Log.d(TAG, "Drag dropped");

                    // --- Use the helper method here ---
                    View droppedOnView = findChildViewUnder(container, event.getX(), event.getY());

                    // Reset alpha for all views before potentially changing layout (including the dragged one)
                    for(TextView tv : numberTextViews) {
                        tv.setAlpha(1.0f);
                    }
                    viewBeingDraggedOver = null; // Clear the reference

                    // Check if the drop target is a valid number TextView AND is not the dragged view itself
                    if (droppedOnView != null && droppedOnView != draggedView) {
                        // Found a valid target TextView to swap with
                        int draggedIndex = container.indexOfChild(draggedView);
                        int droppedOnIndex = container.indexOfChild(droppedOnView);

                        if (draggedIndex != droppedOnIndex) {
                            // Perform the swap in the LinearLayout's children list
                            // Remove both views first
                            container.removeView(draggedView);
                            container.removeView(droppedOnView);

                            // Add them back at the opposing original indices
                            // This maintains the relative order of other views.
                            // If dragged was at index 2 and dropped on was at index 4:
                            // Add droppedOnView at index 2, then add draggedView at index 4 (which is now index 3 after the first add)
                            // Correct way to swap indices:
                            // Add draggedView at min(draggedIndex, droppedOnIndex)
                            // Add droppedOnView at max(draggedIndex, droppedOnIndex)

                            if (draggedIndex < droppedOnIndex) {
                                // Dragged was to the left of droppedOn. Dragged goes to droppedOn spot, droppedOn goes to dragged spot.
                                container.addView(droppedOnView, draggedIndex); // droppedOnView takes the original left spot
                                container.addView(draggedView, droppedOnIndex); // draggedView takes the original right spot
                            } else { // droppedOnIndex < draggedIndex
                                // Dragged was to the right of droppedOn. Dragged goes to droppedOn spot, droppedOn goes to dragged spot.
                                container.addView(draggedView, droppedOnIndex); // draggedView takes the original left spot
                                container.addView(droppedOnView, draggedIndex); // droppedOnView takes the original right spot
                            }


                            Log.d(TAG, "Swapped views. original draggedIndex=" + draggedIndex + ", original droppedOnIndex=" + droppedOnIndex);
                            return true; // Indicate drop was handled successfully
                        } else {
                            // Dropped back on itself - shouldn't happen if check droppedOnView != draggedView, but for safety
                            Log.d(TAG, "Dropped back on itself");
                            return false; // Drop not handled
                        }
                    } else {
                        // Dropped on container background, or outside the layout area, or on dragged view's initial spot (already handled)
                        Log.d(TAG, "Dropped on invalid target (or self)");
                        // Ensure dragged view is visible again (already done at the start of DROP)
                        return false; // Indicate drop was NOT handled
                    }


                case DragEvent.ACTION_DRAG_ENDED:
                    Log.d(TAG, "Drag ended. Result: " + event.getResult());
                    // Reset alpha for the dragged view whether drop was successful or not
                    if (draggedView != null) {
                        draggedView.setAlpha(1.0f); // Restore full visibility/alpha
                    }
                    // Reset highlighting on any view that was being dragged over
                    if (viewBeingDraggedOver != null) {
                        viewBeingDraggedOver.setAlpha(1.0f);
                        viewBeingDraggedOver = null;
                    }
                    return true; // Event handled
                default:
                    // Log.d(TAG, "Unknown drag action: " + event.getAction());
                    return false; // Unhandled action
            }
        }
    }
}