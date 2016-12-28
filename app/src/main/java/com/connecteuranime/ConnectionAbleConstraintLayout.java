package com.connecteuranime;

import android.content.Context;
import android.graphics.Canvas;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremy on 27/12/2016.
 */
public class ConnectionAbleConstraintLayout extends ConstraintLayout implements Connector.OnInvalidateNeededListener {
    List<Connector> connectorList = new ArrayList<>();

    public ConnectionAbleConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        for (Connector connector : connectorList) {
            connector.onChangeOrganization();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        for (Connector connector : connectorList) {
            connector.onDraw(canvas);
        }
        super.dispatchDraw(canvas);
    }

    /**
     * Create a connection between two views that are children of this layout
     * @param view1Id
     * @param view2Id
     * @return the Connection View if created
     */
    public Connector addConnection (int view1Id, int view2Id){
        View view1 = findViewById(view1Id);
        View view2 = findViewById(view2Id);

        if (view1!=null && view2!=null){
            Connector connector = new Connector(getContext(), view1, view2, this);
            connectorList.add(connector);
            invalidate();
            return connector;
        }

        return null;
    }

    @Override
    public void onRedrawNeeded() {
        invalidate();
    }

}
