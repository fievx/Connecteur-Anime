package com.connecteuranime;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Connector to link two views in a hierarchy.
 * Created by Jeremy on 27/12/2016.
 */
public class ConnectorView extends View {
    private Path path;
    private View v1, v2;
    private int v1Gravity, v2Gravity;
    private Paint connectorPaint;
    private int [] anchor1, anchor2;
    private int [] clearance1, clearance2;
    private int clearance;

    private static final int GRAVITY_LEFT = 1;
    private static final int GRAVITY_TOP = 2;
    private static final int GRAVITY_RIGHT = 3;
    private static final int GRAVITY_BOTTOM = 4;



    public ConnectorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init (Context context, AttributeSet attrs){
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ConnectorView, 0,0);
        try {
            v1 = getRootView().findViewById(a.getResourceId(R.styleable.ConnectorView_view_1, 0));
            v2 = getRootView().findViewById(a.getResourceId(R.styleable.ConnectorView_view_2, 0));
            v1Gravity = a.getIndex(R.styleable.ConnectorView_view_1_gravity);
            v2Gravity = a.getIndex(R.styleable.ConnectorView_view_2_gravity);
            clearance = a.getDimensionPixelSize(R.styleable.ConnectorView_clearance, 0);

        }finally {
            a.recycle();
        }

        if (v1!=null && v2!=null){

        }

        connectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        connectorPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));

        path = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //we only proceed if we have two views to attach to with gravities
        if (v1!=null && v2!=null && v1Gravity!=0 && v2Gravity!=0){
            int[][] anchorAndClearance1 = getAnchorPositionAndClearance(v1, v1Gravity);
            int[][]anchorAndClearance2 = getAnchorPositionAndClearance(v2, v2Gravity);
            anchor1 = anchorAndClearance1[0];
            clearance1 = anchorAndClearance1[1];
            anchor2 = anchorAndClearance2[0];
            clearance2 = anchorAndClearance2[1];

            setMeasuredDimension(getMaxWidth(anchorAndClearance1, anchorAndClearance2),
                    getMaxHeight(anchorAndClearance1, anchorAndClearance1));

        }else {
            setMeasuredDimension(0,0);
        }
    }

    private int getMaxWidth(int[][] anchorAndClearance1, int[][] anchorAndClearance2){
        int width = 0;
        for (int[] ints : anchorAndClearance1) {
            for (int[] ints1 : anchorAndClearance2) {
                int i = Math.abs(ints[0] - ints1[0]);
                if (i> width)
                    width = i;
            }
        }
        return width;
    }

    private int getMaxHeight(int[][] anchorAndClearance1, int[][] anchorAndClearance2){
        int height = 0;
        for (int[] ints : anchorAndClearance1) {
            for (int[] ints1 : anchorAndClearance2) {
                int i = Math.abs(ints[1] - ints1[1]);
                if (i> height)
                    height = i;
            }
        }
        return height;
    }

    /**
     * calculates the position of the anchor on the given view with the given gravity.
     * the anchor given is within the immediate parent. for this reason, both views need to be in the
     * same Layout as the Connector
     * @param v
     * @param gravity
     * @return an array representing : anchor x, anchor y, clearance x, clearance y
     */
    private int[][] getAnchorPositionAndClearance(View v, int gravity){
        int [][] coords = new int [2][2];
        switch (gravity){
            case GRAVITY_LEFT:
                coords[0][0] = v.getLeft();
                coords[0][1] = v.getBottom() - v.getTop()/2;
                coords[1][0] = coords[0][0] - clearance;
                coords[1][1] = coords[0][1];
                break;
            case GRAVITY_TOP:
                coords[0][0] = v.getRight() - v.getLeft()/2;
                coords[0][1] = v.getTop();
                coords[1][0] = coords[0][0] ;
                coords[1][1] = coords[0][1] - clearance;
                break;
            case GRAVITY_RIGHT:
                coords[0][0] = v.getRight();
                coords[0][1] = v.getBottom() - v.getTop()/2;
                coords[1][0] = coords[0][0] + clearance;
                coords[1][1] = coords[0][1];
                break;
            case GRAVITY_BOTTOM:
                coords[0][0] = v.getRight() - v.getLeft()/2;
                coords[0][1] = v.getBottom();
                coords[1][0] = coords[0][0] ;
                coords[1][1] = coords[0][1] + clearance;
                break;
        }

        return coords;
    }

    /**
     * Take a point in the parent layout and return its position relative to the view.
     * if the point is "outside" of the view, will return -1, -1
     * @param positionInParent
     * @return
     */
    private int [] getPositionInCanvas(int [] positionInParent){
        int [] position = new int [2];
        int x = positionInParent[0] - getLeft();
        int y = positionInParent[1] - getTop();
        if (x<0 || y<0){
            position [0] = -1;
            position [1] = -1;
        }

        return position;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        //we build the path
        //first we draw the clearance outside of anchor 1
        path.moveTo(getPositionInCanvas(anchor1)[0], getPositionInCanvas(anchor1)[1]);
        path.lineTo(getPositionInCanvas(clearance1)[0], getPositionInCanvas(clearance1)[1]);

        //then a

    }
}
