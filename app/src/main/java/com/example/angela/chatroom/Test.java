package com.example.angela.chatroom;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class Test extends AppCompatActivity {

    private Button enter_room;
    private EditText room_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        enter_room = (Button) findViewById(R.id.room_btn);
        room_name = (EditText) findViewById(R.id.room_edt);

        enter_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),TestRoom.class);
                intent.putExtra("room_name",room_name.getText().toString());
                startActivity(intent);
            }
        });



    }

}
