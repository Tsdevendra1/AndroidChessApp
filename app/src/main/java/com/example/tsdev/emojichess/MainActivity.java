package com.example.tsdev.emojichess;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.icu.util.ChineseCalendar;
import android.provider.ContactsContract;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.system.ErrnoException;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ScreenInfoHolder screenInfoHolder;
    private int PAWN_TYPE = 0;
    private int ROOK_TYPE = 1;
    private int QUEEN_TYPE = 2;
    private int KING_TYPE = 3;
    private int KNIGHT_TYPE = 4;
    private int BISHOP_TYPE = 5;
    private int WHITE_PIECE = 0;
    private int BLACK_PIECE = 1;
    // Forward counts as moving to a tile with which has a greater row number than the current one
    private int MOVING_FORWARD = 0;
    // Backward counts as moving to a tile with which has a lower row number than the current one
    private int MOVING_BACKWARD = 1;


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
        ScreenInfoHolder(int width, int height) {
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
        int total = numColumns * numRows;
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
            if (c == 0) {
                if (rowColorBrown) {
                    normalShape = getResources().getDrawable(R.drawable.shape_brown);
                    rowColorBrown = false;
                    columnColorBrown = false;
                } else {
                    normalShape = getResources().getDrawable(R.drawable.shape_lightbrown);
                    rowColorBrown = true;
                    columnColorBrown = true;
                }
            } else {
                if (columnColorBrown) {
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
            int sideLength = screenInfoHolder.getScreenWidth() / numColumns;
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
            if (r == 1 || r == 6) {
                childView.setImageResource(R.drawable.pawn);
                childView.setTag(R.id.PIECE_TYPE, PAWN_TYPE);
            } else if (c == 0 || c == 7) {
                childView.setImageResource(R.drawable.rook);
                childView.setTag(R.id.PIECE_TYPE, ROOK_TYPE);
            } else if (c == 1 || c == 6) {
                childView.setImageResource(R.drawable.knight);
                childView.setTag(R.id.PIECE_TYPE, KNIGHT_TYPE);
            } else if (c == 2 || c == 5) {
                childView.setImageResource(R.drawable.bishop);
                childView.setTag(R.id.PIECE_TYPE, BISHOP_TYPE);
            } else if (c == 3) {
                childView.setImageResource(R.drawable.king);
                childView.setTag(R.id.PIECE_TYPE, KING_TYPE);
            } else if (c == 4) {
                childView.setImageResource(R.drawable.queen);
                childView.setTag(R.id.PIECE_TYPE, QUEEN_TYPE);
            }

            // Save current co-ordinates of piece
            childView.setTag(R.id.PIECE_COL_POSITION, c);
            childView.setTag(R.id.PIECE_ROW_POSITION, r);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(75, 75);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            childView.setLayoutParams(layoutParams);

            // Add child
            Set<Integer> startRows = new HashSet<>();
            // Only the first two and last two rows should have any images
            startRows.add(0);
            startRows.add(1);
            startRows.add(6);
            startRows.add(7);
            if (startRows.contains(r)) {
                childView.setOnTouchListener(new MyTouchListener());
                if (r == 0 || r == 1) {
                    childView.setTag(R.id.PIECE_COLOUR, BLACK_PIECE);
                    childView.setTag(R.id.MOVEMENT_DIRECTION, MOVING_FORWARD);
                } else {
                    childView.setTag(R.id.PIECE_COLOUR, WHITE_PIECE);
                    childView.setTag(R.id.MOVEMENT_DIRECTION, MOVING_BACKWARD);
                    // See piece colour to white
                    childView.setColorFilter(childView.getContext().getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                }
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

        boolean checkTileHasPieceChild(ViewGroup gridLayout, int row, int col) {
            int index = (8 * row + col);
            ViewGroup currentTile = (ViewGroup) gridLayout.getChildAt(index);

            // Need to explicitly check that the child is a chess piece and not a random view
            for (int i = 0; i < currentTile.getChildCount(); i++) {
                View child = currentTile.getChildAt(i);
                int pieceColor = (int) child.getTag(R.id.PIECE_COLOUR);
                if (pieceColor == WHITE_PIECE || pieceColor == BLACK_PIECE) {
                    return true;
                }
            }
            return false;
        }

        boolean checkPathBlocked(View piece, View tile) {
            /*
              This function checks if the position the user moved to was blocked by a piece of the
              same colour
             */

            boolean pathBlocked = false;

            // Piece info
            final int pieceType = (int) piece.getTag(R.id.PIECE_TYPE);
            final int currentPieceRow = (int) piece.getTag(R.id.PIECE_ROW_POSITION);
            final int currentPieceCol = (int) piece.getTag(R.id.PIECE_COL_POSITION);
            final int pieceMovementDirection = (int) piece.getTag(R.id.MOVEMENT_DIRECTION);

            // Tile info
            final int currentTileRow = (int) tile.getTag(R.id.ROW_NUM);
            final int currentTileCol = (int) tile.getTag(R.id.COL_NUM);

            // Will be used to get every square of the grid
            ViewGroup gridLayout = findViewById(R.id.tableGrid);

            // Check piece type first
            if (pieceType == PAWN_TYPE) {
                // Seperate logic going backwards because we have to see the numbers going the
                // other direction
                if (pieceMovementDirection == MOVING_FORWARD) {
                    for (int row = currentPieceRow + 1; row < currentTileRow; row++) {
                        pathBlocked = checkTileHasPieceChild(gridLayout, row, currentPieceCol);
                        if (pathBlocked) {
                            return pathBlocked;
                        }
                    }
                } else {
                    for (int row = currentPieceRow - 1; row > currentTileRow; row--) {
                        pathBlocked = checkTileHasPieceChild(gridLayout, row, currentPieceCol);
                        if (pathBlocked) {
                            return pathBlocked;
                        }
                    }
                }
            } else if (pieceType == BISHOP_TYPE) {
                if (currentPieceRow < currentTileRow) {
                    // Right direction
                    if (currentPieceCol < currentTileCol) {
                        for (int row = currentPieceRow + 1; row < currentTileRow; row++) {
                            for (int col = currentPieceCol + 1; col < currentTileCol; col++) {
                                if (row == currentPieceRow + Math.abs(currentPieceCol - col)) {
                                    pathBlocked = checkTileHasPieceChild(gridLayout, row, col);
                                    if (pathBlocked) {
                                        return pathBlocked;
                                    }
                                }
                            }
                        }
                    } else {
                        // Left direction
                        for (int row = currentPieceRow + 1; row < currentTileRow; row++) {
                            for (int col = currentPieceCol - 1; col > currentTileCol; col--) {
                                if (row == currentPieceRow + Math.abs(currentPieceCol - col)) {
                                    pathBlocked = checkTileHasPieceChild(gridLayout, row, col);
                                    if (pathBlocked) {
                                        return pathBlocked;
                                    }
                                }
                            }
                        }

                    }
                    // Diagonal downwards
                } else {
                    // Right direction
                    if (currentPieceCol < currentTileCol) {
                        for (int row = currentPieceRow - 1; row > currentTileRow; row--) {
                            for (int col = currentPieceCol + 1; col < currentTileCol; col++) {
                                if (row == currentPieceRow - Math.abs(currentPieceCol - col)) {
                                    pathBlocked = checkTileHasPieceChild(gridLayout, row, col);
                                }
                            }
                        }
                        // Left direction
                    } else {
                        for (int row = currentPieceRow - 1; row > currentTileRow; row--) {
                            for (int col = currentPieceCol + 1; col > currentTileCol; col--) {
                                if (row == currentPieceRow - Math.abs(currentPieceCol - col)) {
                                    pathBlocked = checkTileHasPieceChild(gridLayout, row, col);
                                }
                            }
                        }
                    }
                }

            } else if (pieceType == KING_TYPE)
            // Can only ever move one position so can never be blocked
            {
            } else if (pieceType == QUEEN_TYPE) {
                // Case where it moves sideways only
                if (currentPieceRow == currentTileRow) {
                    if (currentPieceCol < currentTileCol) {
                        for (int col = currentPieceCol + 1; col < currentTileCol; col++) {
                            pathBlocked = checkTileHasPieceChild(gridLayout, currentPieceRow, col);
                            if (pathBlocked) {
                                return pathBlocked;
                            }
                        }
                    } else {
                        for (int col = currentPieceCol - 1; col > currentTileCol; col--) {
                            pathBlocked = checkTileHasPieceChild(gridLayout, currentPieceRow, col);
                            if (pathBlocked) {
                                return pathBlocked;
                            }
                        }
                    }

                    // Case where it moves vertically only
                } else if (currentPieceCol == currentTileCol) {
                    if (currentPieceRow < currentTileRow) {
                        for (int row = currentPieceRow + 1; row < currentTileRow; row++) {
                            pathBlocked = checkTileHasPieceChild(gridLayout, row, currentPieceCol);
                        }
                    } else {
                        for (int row = currentPieceRow - 1; row > currentTileRow; row--) {
                            pathBlocked = checkTileHasPieceChild(gridLayout, row, currentPieceCol);
                            if (pathBlocked) {
                                return pathBlocked;
                            }
                        }
                    }
                    // Diagonal upwards
                } else if (currentPieceRow < currentTileRow) {
                    // Right direction
                    if (currentPieceCol < currentTileCol) {
                        for (int row = currentPieceRow + 1; row < currentTileRow; row++) {
                            for (int col = currentPieceCol + 1; col < currentTileCol; col++) {
                                if (row == currentPieceRow + Math.abs(currentPieceCol - col)) {
                                    pathBlocked = checkTileHasPieceChild(gridLayout, row, col);
                                    if (pathBlocked) {
                                        return pathBlocked;
                                    }
                                }
                            }
                        }
                    } else {
                        // Left direction
                        for (int row = currentPieceRow + 1; row < currentTileRow; row++) {
                            for (int col = currentPieceCol - 1; col > currentTileCol; col--) {
                                if (row == currentPieceRow + Math.abs(currentPieceCol - col)) {
                                    pathBlocked = checkTileHasPieceChild(gridLayout, row, col);
                                    if (pathBlocked) {
                                        return pathBlocked;
                                    }
                                }
                            }
                        }

                    }
                    // Diagonal downwards
                } else {
                    // Right direction
                    if (currentPieceCol < currentTileCol) {
                        for (int row = currentPieceRow - 1; row > currentTileRow; row--) {
                            for (int col = currentPieceCol + 1; col < currentTileCol; col++) {
                                if (row == currentPieceRow - Math.abs(currentPieceCol - col)) {
                                    pathBlocked = checkTileHasPieceChild(gridLayout, row, col);
                                }
                            }
                        }
                        // Left direction
                    } else {
                        for (int row = currentPieceRow - 1; row > currentTileRow; row--) {
                            for (int col = currentPieceCol + 1; col > currentTileCol; col--) {
                                if (row == currentPieceRow - Math.abs(currentPieceCol - col)) {
                                    pathBlocked = checkTileHasPieceChild(gridLayout, row, col);
                                }
                            }
                        }
                    }
                }
            } else if (pieceType == ROOK_TYPE) {
                if (currentPieceCol == currentTileCol) {
                    if (currentPieceRow < currentTileRow) {
                        for (int row = currentPieceRow + 1; row < currentTileRow; row++) {
                            pathBlocked = checkTileHasPieceChild(gridLayout, row, currentPieceCol);
                            if (pathBlocked) {
                                return pathBlocked;
                            }
                        }
                    } else {
                        for (int row = currentPieceRow - 1; row > currentTileRow; row--) {
                            pathBlocked = checkTileHasPieceChild(gridLayout, row, currentPieceCol);
                            if (pathBlocked) {
                                return pathBlocked;
                            }
                        }
                    }
                } else if (currentTileRow == currentPieceRow) {
                    if (currentPieceCol < currentTileCol) {
                        for (int col = currentPieceCol + 1; col < currentTileCol; col++) {
                            pathBlocked = checkTileHasPieceChild(gridLayout, currentPieceRow, col);
                            if (pathBlocked) {
                                return pathBlocked;
                            }
                        }
                    } else {
                        for (int col = currentPieceCol - 1; col > currentTileCol; col--) {
                            pathBlocked = checkTileHasPieceChild(gridLayout, currentPieceRow, col);
                            if (pathBlocked) {
                                return pathBlocked;
                            }
                        }
                    }

                }
            } else if (pieceType == KNIGHT_TYPE) {
                // DO nothing because knight cannot be blocked
            }
            return pathBlocked;
        }

        boolean checkLegalMove(View piece, View tile, int childCount) {

            // Piece info
            final int pieceType = (int) piece.getTag(R.id.PIECE_TYPE);
            final int currentPieceRow = (int) piece.getTag(R.id.PIECE_ROW_POSITION);
            final int currentPieceCol = (int) piece.getTag(R.id.PIECE_COL_POSITION);
            final int pieceMovementDirection = (int) piece.getTag(R.id.MOVEMENT_DIRECTION);

            if (childCount != 0 && childCount != 1) {
                throw new java.lang.Error("There should only ever be a maximum of one view on the tile");
            }


            // Tile info
            final int currentTileRow = (int) tile.getTag(R.id.ROW_NUM);
            final int currentTileCol = (int) tile.getTag(R.id.COL_NUM);


            // Keeps track if the movement is legal
            boolean legalMove = false;

            if (pieceType == PAWN_TYPE) {
                if (pieceMovementDirection == MOVING_BACKWARD) {
                    legalMove = ((currentPieceRow - 2 <= currentTileRow && currentTileRow <= currentPieceRow) && currentPieceCol == currentTileCol);
                } else {
                    legalMove = ((currentPieceRow <= currentTileRow && currentTileRow <= currentPieceRow + 2) && currentPieceCol == currentTileCol);
                }
            } else if (pieceType == BISHOP_TYPE) {
                if (currentPieceCol != currentTileCol) {
                    legalMove = (currentTileRow == (currentPieceRow + Math.abs(currentPieceCol - currentTileCol)) || currentTileRow == (currentPieceRow - Math.abs(currentPieceCol - currentTileCol)));
                }
            } else if (pieceType == KING_TYPE) {
                legalMove = ((currentPieceRow - 1 <= currentTileRow && currentTileRow <= currentPieceRow + 1) && (currentPieceCol - 1 <= currentTileCol && currentTileCol <= currentPieceCol + 1));

            } else if (pieceType == QUEEN_TYPE) {
                if (currentTileCol != currentPieceCol) {
                    legalMove = (currentTileRow == currentPieceRow || currentTileRow == (currentPieceRow + Math.abs(currentPieceCol - currentTileCol)) || currentTileRow == (currentPieceRow - Math.abs(currentPieceCol - currentTileCol)));
                } else if (currentTileRow != currentPieceRow) {
                    legalMove = (currentPieceCol == currentTileCol || currentTileRow == (currentPieceRow + Math.abs(currentPieceCol - currentTileCol)) || currentTileRow == (currentPieceRow - Math.abs(currentPieceCol - currentTileCol)));
                }
            } else if (pieceType == ROOK_TYPE) {
                if (currentTileCol != currentPieceCol) {
                    legalMove = (currentTileRow == currentPieceRow);
                }
                if (currentPieceRow != currentTileRow) {
                    legalMove = (currentPieceCol == currentTileCol);
                }
            } else if (pieceType == KNIGHT_TYPE) {
                if (currentTileRow != currentPieceRow) {
                    int absDiffRow = Math.abs(currentPieceRow - currentTileRow);
                    if (absDiffRow == 1) {
                        legalMove = (Math.abs(currentPieceCol - currentTileCol) == 2);
                    } else if (absDiffRow == 2) {
                        legalMove = (Math.abs(currentPieceCol - currentTileCol) == 1);
                    }
                }
            }
            // Check if the move was blocked by another piece
            if (legalMove) {
                if (!checkPathBlocked(piece, tile)) {
                    piece.setTag(R.id.PIECE_ROW_POSITION, currentTileRow);
                    piece.setTag(R.id.PIECE_COL_POSITION, currentTileCol);
                } else {
                    legalMove = false;
                }
            }
            return legalMove;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    ViewGroup currentViewGroup = (ViewGroup) v;
                    int numChildren = currentViewGroup.getChildCount();


                    // This is the view being dragged
                    View view = (View) event.getLocalState();

                    boolean legalMove = checkLegalMove(view, v, numChildren);


                    // Shouldn't have any other children in the view it is being dropped into
                    if (numChildren == 0 && legalMove) {

                        final int[] locationArray = new int[2];
                        v.getLocationOnScreen(locationArray);
                        System.out.println(locationArray);

//                        TranslateAnimation animate = new TranslateAnimation(
//                                100,                 // fromXDelta
//                                50,                 // toXDelta
//                                10,  // fromYDelta
//                                100);
//                        animate.setDuration(900);
//                        animate.setFillAfter(true);
//                        v.startAnimation(animate);

                        // Need to get the parent of the view so we can remove it from that position
                        ViewGroup owner = (ViewGroup) view.getParent();
                        owner.removeView(view);

                        // Add the view to the current view it is hovering over
                        RelativeLayout container = (RelativeLayout) v;
                        container.addView(view);
                        view.setVisibility(View.VISIBLE);
                    } else {
                        view.setVisibility(View.VISIBLE);
                        Context context = getApplicationContext();
                        String text = "Illegal Move";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
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
