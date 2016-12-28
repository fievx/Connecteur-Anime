package com.connecteuranime;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Connector to link two views in a hierarchy.
 * Created by Jeremy on 27/12/2016.
 */
public class Connector {
    private Path path, animPath;
    private View v1, v2, startView;
    private View leftView, rightView, topView, bottomView;
    private int gravity;
    private Paint connectorPaint, animPaint;
    private WeakReference <Context> contextWeakReference;
    private boolean needResize = true;
    private OnInvalidateNeededListener onInvalidateNeededListener;

    private static final int GRAVITY_LEFT_RIGHT = 1;
    private static final int GRAVITY_TOP_BOTTOM = 2;


    public Connector(Context context, View v1, View v2, OnInvalidateNeededListener listener) {
        contextWeakReference = new WeakReference<>(context);
        this.v1 = v1;
        this.v2 = v2;
        this.onInvalidateNeededListener = listener;

        connectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        connectorPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        connectorPaint.setStyle(Paint.Style.STROKE);
        connectorPaint.setStrokeWidth(6);
        connectorPaint.setPathEffect(new DashPathEffect(new float[] { 15, 5, 8, 5 }, 10));

        path = new Path();
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

    public void onDraw(Canvas canvas) {
        if (needResize)
            computeSize();

        //we draw the path
        canvas.drawPath(path, connectorPaint);

        // and the anim path on top of it
        if (animPath!=null && animPaint!=null) {
            canvas.drawPath(animPath, animPaint);
        }

    }

    private void computeSize(){
        if (v1!=null && v2!=null) {
            leftView = v1.getLeft() < v2.getLeft() ? v1 : v2;
            rightView = leftView == v1 ? v2 : v1;

            topView = v1.getTop() < v2.getTop() ? v1 : v2;
            bottomView = topView == v1 ? v2 : v1;

            //to decide whether the connector should be attached to the top/bottom or right/left sides
            //we look if their sides overlap. if yes, then the connectors need to be attached on top/bottom
            gravity = leftView.getRight() > rightView.getLeft() ? GRAVITY_TOP_BOTTOM : GRAVITY_LEFT_RIGHT;

            path.reset();

            //for each gravity, we determine the start view (needed for animation later) and then draw the path
            switch (gravity){
                case GRAVITY_TOP_BOTTOM :
                    startView = topView;
                    path.moveTo(topView.getRight() - topView.getWidth()/2, topView.getBottom());
                    path.lineTo(topView.getRight() - topView.getWidth()/2,
                            bottomView.getTop() - (bottomView.getTop() - topView.getBottom())/2); // move down to half way between the views
                    path.lineTo(bottomView.getRight() - bottomView.getWidth()/2,
                            bottomView.getTop() - (bottomView.getTop() - topView.getBottom())/2); // move sideways to on top of the bottom view
                    path.lineTo(bottomView.getRight() - bottomView.getWidth()/2, bottomView.getTop());
                    break;
                case GRAVITY_LEFT_RIGHT:
                    startView = leftView;
                    path.moveTo(leftView.getRight(),
                            leftView.getBottom() - leftView.getHeight()/2); //place the cursor on the right side of the left view
                    path.lineTo(rightView.getLeft() - (rightView.getLeft() - leftView.getRight())/2,
                            leftView.getBottom() - leftView.getHeight()/2); // move to the right to half way but don't change heigh
                    path.lineTo(rightView.getLeft() - (rightView.getLeft() - leftView.getRight())/2,
                            rightView.getBottom() - rightView.getHeight()/2); //move up/down to the height of the right view
                    path.lineTo(rightView.getLeft(), rightView.getBottom() - rightView.getHeight()/2); //finish at the left side of the right view
                    break;
            }
        }
    }

    /**
     * in charge of defining which view is left/right and deciding what gravity give to the connector.
     * Also creates the path that will be drawn during onDraw
     */
    public void onChangeOrganization(){
        needResize = true;
    }

    /**
     * Animates a path from the given start point to the other point. what it does is create another
     * path following the same route and animate the "phase" param in the path effect to animate the
     * clipping of the path
     * @param startPoint
     */
    public void animatePath(View startPoint){
        animPath = new Path(path);

        animPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Measure the path
        PathMeasure measure = new PathMeasure(animPath, false);
        final float length = measure.getLength();

        // Apply the dash effect
        final PathEffect effect = new DashPathEffect(new float[] { length, length }, length);
        animPaint.setColor(ContextCompat.getColor(contextWeakReference.get(), R.color.colorPrimary));
        animPaint.setStyle(Paint.Style.STROKE);
        animPaint.setStrokeWidth(6);
        animPaint.setPathEffect(effect);
        onInvalidateNeededListener.onRedrawNeeded();

        ValueAnimator animator;
        if (startPoint==startView)
            animator = ValueAnimator.ofFloat(1.0f, 0.0f);
        else animator = ValueAnimator.ofFloat(-1.0f, 0.0f);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final PathEffect pathEffect = new DashPathEffect(new float[] { length, length },
                        length * (float)valueAnimator.getAnimatedValue());
                animPaint.setPathEffect(pathEffect);
                onInvalidateNeededListener.onRedrawNeeded();
            }
        });
        animator.setDuration(3000);
        animator.start();
    }

    public interface OnInvalidateNeededListener {
        void onRedrawNeeded();
    }
}
