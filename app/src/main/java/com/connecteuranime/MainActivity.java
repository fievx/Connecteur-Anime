package com.connecteuranime;

import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    Connector connector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectionAbleConstraintLayout layout = (ConnectionAbleConstraintLayout) findViewById(R.id.activity_main);
        connector = layout.addConnection(R.id.buttonLeft, R.id.buttonRight);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connector.animatePath(view);
            }
        };
        findViewById(R.id.buttonLeft).setOnClickListener(clickListener);
        findViewById(R.id.buttonRight).setOnClickListener(clickListener);
    }
}
