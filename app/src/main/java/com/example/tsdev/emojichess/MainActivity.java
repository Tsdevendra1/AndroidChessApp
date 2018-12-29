package com.example.tsdev.emojichess;

import android.content.ClipData;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ScreenInfoHolder screenInfoHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ScreenInfoHolder screenInfoHolder = new ScreenInfoHolder(displayMetrics.widthPixels, displayMetrics.heightPixels);

        createGridStructure(screenInfoHolder);
    }
    public class ScreenInfoHolder {
        ScreenInfoHolder(int width, int height){
            this.setScreenHeight(height);
            this.setScreenWidth(width);
        }
        private int screenHeight;
        private int screenWidth;

        int getScreenWidth() {
            return screenWidth;
        }

        private void setScreenWidth(int screenWidth) {
            this.screenWidth = screenWidth;
        }

        public int getScreenHeight() {
            return screenHeight;
        }

        private void setScreenHeight(int screenHeight) {
            this.screenHeight = screenHeight;
        }
    }

    private void createGridStructure(ScreenInfoHolder screenInfoHolder) {
        GridLayout gridLayout = findViewById(R.id.tableGrid);
        int numColumns = 8;
        int numRows = 8;
        // Total number of cells
        int total = numColumns*numRows;
        gridLayout.setColumnCount(numColumns);
        gridLayout.setRowCount(numRows);

        boolean rowColorBrown = false;
        boolean columnColorBrown = false;

        for (int i = 0, c = 0, r = 0; i < total; i++, c++) {
            if (c == numColumns) {
                c = 0;
                r++;
            }
            // Add layout
            Drawable normalShape;
            RelativeLayout templateLayout = new RelativeLayout(this);

            // Set which background the tile will have
            if (c==0){
                if (rowColorBrown){
                    normalShape = getResources().getDrawable(R.drawable.shape_brown);
                    rowColorBrown = false;
                    columnColorBrown = false;
                } else {
                    normalShape = getResources().getDrawable(R.drawable.shape_lightbrown);
                    rowColorBrown = true;
                    columnColorBrown = true;
                }
            } else {
                if (columnColorBrown){
                    normalShape = getResources().getDrawable(R.drawable.shape_brown);
                    columnColorBrown = false;
                } else {
                    normalShape = getResources().getDrawable(R.drawable.shape_lightbrown);
                    columnColorBrown = true;
                }
            }

            templateLayout.setBackgroundDrawable(normalShape);

            // Set parameters for the Relativelayout
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            int sideLength = screenInfoHolder.getScreenWidth()/numColumns;
            // Use same value so that it is square
            param.width = sideLength;
            param.height = sideLength;
            param.setGravity(Gravity.CENTER);
            param.columnSpec = GridLayout.spec(c);
            param.rowSpec = GridLayout.spec(r);
            templateLayout.setLayoutParams(param);
            templateLayout.setTag(R.id.ROW_NUM, r);
            templateLayout.setTag(R.id.COL_NUM, c);


            // Create child
            ImageView childView = new ImageView(this);
            childView.setImageResource(R.drawable.ic_launcher_background);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(75, 75);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            childView.setLayoutParams(layoutParams);

            // Add child
            double randomNumber = Math.random();
            Set<Integer> startRows = new HashSet<>();
            // Only the first two and last two rows should have any images
            startRows.add(0);
            startRows.add(1);
            startRows.add(6);
            startRows.add(7);
            if (startRows.contains(r)){
                childView.setOnTouchListener(new MyTouchListener());
                templateLayout.addView(childView);
            }

            templateLayout.setOnDragListener(new MyDragListener());

            gridLayout.addView(templateLayout);
        }

    }

    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view);
                view.startDrag(data, shadowBuilder, view, 0);
                // Sets the view in the original position to invisible while the drag is being
                // carried out
                view.setVisibility(View.INVISIBLE);
                return true;
            } else {
                return false;
            }
        }
    }

    class MyDragListener implements View.OnDragListener {
        Drawable enterShape = getResources().getDrawable(
                R.drawable.shape_droptarget);
        Drawable normalShape = getResources().getDrawable(R.drawable.shape_brown);

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    System.out.println(v.getTag(R.id.ROW_NUM));
                    System.out.println(v.getTag(R.id.COL_NUM));
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    ViewGroup currentViewGroup = (ViewGroup) v;
                    int numChildren = currentViewGroup.getChildCount();

                    // This is the view being dragged
                    View view = (View) event.getLocalState();

                    // Shouldn't have any other children in the view it is being dropped into
                    if (numChildren == 0) {

                        // Need to get the parent of the view so we can remove it from that position
                        ViewGroup owner = (ViewGroup) view.getParent();
                        owner.removeView(view);

                        // Add the view to the current view it is hovering over
                        RelativeLayout container = (RelativeLayout) v;
                        container.addView(view);
                        view.setVisibility(View.VISIBLE);
                    } else {
                        view.setVisibility(View.VISIBLE);
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                default:
                    break;
            }
            return true;
        }
    }
}
