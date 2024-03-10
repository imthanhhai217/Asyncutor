package com.juhalion.asyncutor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tvDemo = findViewById(R.id.tvDemo);
        tvDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DemoAsyncutor2 demoAsyncutor = new DemoAsyncutor2() {
                    @Override
                    protected void onPostExecuted(Integer integer) {
                        super.onPostExecuted(integer);
                        Log.d("TAG", "onPostExecuted: " + integer);
                    }
                };
                demoAsyncutor.execute(1000);
            }
        });

    }
}