package com.example.angela.chatroom;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //創建/進入房間
        Button createRoomBtn =(Button)findViewById(R.id.createRoomBtn);
        createRoomBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CreateRoom.class);
                startActivity(intent);
            }
        });

        //上傳照片
        Button uploadBtn =(Button)findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,UploadImage.class);
                startActivity(intent);
            }
        });

        //上傳照片
        Button goBtn =(Button)findViewById(R.id.goBtn);
        goBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,Test.class);
                startActivity(intent);
            }
        });
    }

}
