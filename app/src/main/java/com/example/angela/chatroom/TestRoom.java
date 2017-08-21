package com.example.angela.chatroom;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class TestRoom extends AppCompatActivity {

    private String room_name;
    private String chat_msg,chat_user_name;
    private Uri uri;

    private DatabaseReference root ;
    private StorageReference storageReference;
    private NotificationCompat.Builder builder;
    private DownloadManager downloadManager;

    private Button downloadBtn;
    private TextView chat_conversation;
    private VideoView vidView;
    private MediaController vidControl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_room);

        chat_conversation = (TextView) findViewById(R.id.textView);
        vidView = (VideoView) findViewById(R.id.videoView);
        downloadBtn = (Button) findViewById(R.id.downloadBtn);

        storageReference = FirebaseStorage.getInstance().getReference();

        room_name = getIntent().getExtras().get("room_name").toString();
        setTitle(" Room - "+room_name);

        Toast.makeText(this, "You are in Room -"+room_name, Toast.LENGTH_SHORT).show();

        //download Btn
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                downloadManager.enqueue(request);
            }
        });

        //getting firebase database reference
        root = FirebaseDatabase.getInstance().getReference().child(room_name).child("chat");

        //通知
        builder = new NotificationCompat.Builder(this);

        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
//                // 通知的識別號碼
//                int notifyID = 1;
//                // PendingIntent的Request Code
//                int requestCode = notifyID;
//                // 跳到相對應的activity(房間)
//                Intent intent = new Intent(getApplicationContext(),TestRoom.class);
//                intent.putExtra("room_name",room_name);
//                // ONE_SHOT：PendingIntent只使用一次；CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的；NO_CREATE：沿用先前的PendingIntent，不建立新的PendingIntent；UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
//                int flags = PendingIntent.FLAG_CANCEL_CURRENT;
//                // 取得PendingIntent
//                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, intent, flags);
//                builder.setSmallIcon(R.mipmap.ic_launcher);
//                builder.setContentTitle("Firebase Push Notification");
//                builder.setContentText(chat_user_name+" send new message!");
//                //按通知後通知消失
//                builder.setAutoCancel(true);
//                builder.setContentIntent(pendingIntent);
//                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.notify(notifyID, builder.build());
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }
    private void append_chat_conversation(DataSnapshot dataSnapshot) {

        Iterator i = dataSnapshot.getChildren().iterator();

        while (i.hasNext()){
            chat_msg = (String) ((DataSnapshot)i.next()).getValue();
            chat_user_name = (String) ((DataSnapshot)i.next()).getValue();
        }

        if(chat_msg.startsWith("https://firebasestorage.googleapis.com/")){
            vidControl = new MediaController(TestRoom.this);
            vidControl.setAnchorView(vidView);
            vidView.setMediaController(vidControl);
            uri = Uri.parse(chat_msg);
            vidView.setVideoURI(uri);
            //直接撥放影片
            // vidView.start();
        }

        chat_conversation.append(chat_user_name +" : "+chat_msg +" \n");
    }


}
