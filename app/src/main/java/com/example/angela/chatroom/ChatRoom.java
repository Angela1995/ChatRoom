package com.example.angela.chatroom;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatRoom extends AppCompatActivity {

    private Button btn_send_msg, btn_choose_video;
    private EditText input_msg;
    private TextView chat_conversation;
    private VideoView vidView;
    private MediaController vidControl;

    //firebase storage reference
    private StorageReference storageReference;
    private DatabaseReference root, chat_root, count_root;
    private String user_name,room_name;
    private String temp_key;

    private String chat_msg,chat_user_name;

    //a constant to track the file chooser intent
    private static final int PICK_VIDEO_REQUEST = 999;

    //a Uri object to store file path
    //private Uri filePath, downloadUri;

    //把file path轉成字串
    private String uriToString;
    private String message;

    //計算msg數量
    private int count_old;
    private int count_new;

    //跳通知
    private NotificationCompat.Builder builder;

    //進度條
    private ProgressDialog progressDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        btn_choose_video = (Button) findViewById(R.id.videoBtn);
        btn_send_msg = (Button) findViewById(R.id.btn_send);
        input_msg = (EditText) findViewById(R.id.msg_input);
        chat_conversation = (TextView) findViewById(R.id.textView);
        vidView = (VideoView) findViewById(R.id.videoView);

        //從CreateRoom接收房號跟姓名
        user_name = getIntent().getExtras().get("user_name").toString();
        room_name = getIntent().getExtras().get("room_name").toString();
        setTitle(" Room - "+room_name);

        //getting firebase database reference
        root = FirebaseDatabase.getInstance().getReference().child(room_name);
        chat_root = FirebaseDatabase.getInstance().getReference().child(room_name).child("chat");
        //getting firebase storage reference
        storageReference = FirebaseStorage.getInstance().getReference();

        //通知
        builder = new NotificationCompat.Builder(this);

        //進度條
        progressDialog = new ProgressDialog(this);

        //傳送訊息Btn
        btn_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = input_msg.getText().toString();
                sendMsg(message);
            }
        });

        //上傳影片Btn
        btn_choose_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                //選擇影片
//                Intent intent = new Intent();
//                intent.setType("video/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST);

                String file = "/storage/emulated/0/Movies/Instagram/VID_176010131_012946_851.mp4";
                File tmpFile = new File(file);
                Uri filePath = Uri.fromFile(tmpFile);

                if (filePath != null) {
                    //displaying a progress dialog while upload is going on
                    progressDialog.setTitle("Uploading");
                    progressDialog.show();

                    StorageReference ref = storageReference.child("video").child(filePath.getLastPathSegment());
                    ref.putFile(filePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    //if the upload is successfull, hide the progress dialog
                                    progressDialog.dismiss();

                                    //and displaying a success toast
                                    Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();

                                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                                    sendMsg(downloadUri.toString());

                                    //播影片
                                    vidControl = new MediaController(ChatRoom.this);
                                    vidControl.setAnchorView(vidView);
                                    vidView.setMediaController(vidControl);
                                    vidView.setVideoURI(downloadUri);
                                    vidView.start();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    //if the upload is not successfull, hide the progress dialog
                                    progressDialog.dismiss();

                                    //and displaying error message
                                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    //calculating progress percentage
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                    //displaying percentage in progress dialog
                                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                                }
                            });
                }

            }
        });

        chat_root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
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

        //抓total的值
        /*root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hash = (Count) dataSnapshot.getValue();
                hash.getClass().toString();

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });*/


        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        Iterator i = dataSnapshot.getChildren().iterator();
        //count_new = 0;
        while (i.hasNext()){
            chat_msg = (String) ((DataSnapshot)i.next()).getValue();
            chat_user_name = (String) ((DataSnapshot)i.next()).getValue();
            //count_new++;

            /*if(count_new>count_old){
                count_old = count_new;
                count_root.setValue(count_old);
            }*/
        }

//        //抓firebase的最後一個影片撥放
//        if(chat_msg.startsWith("https://firebasestorage.googleapis.com/")){
//            vidControl = new MediaController(ChatRoom.this);
//            vidControl.setAnchorView(vidView);
//            vidView.setMediaController(vidControl);
//
//            vidView.setVideoURI(Uri.parse(chat_msg));
//            vidView.start();
//        }

        chat_conversation.append(chat_user_name +" : "+chat_msg +" \n");

    }

//    //handling the image chooser activity result
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            filePath = data.getData();
//            uploadFile();
//        }
//    }
//
//    //this method will upload the file
//    private void uploadFile() {
//        //if there is a file to upload
//        if (filePath != null) {
//            //displaying a progress dialog while upload is going on
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("Uploading");
//            progressDialog.show();
//
//            StorageReference ref = storageReference.child("video").child(filePath.getLastPathSegment());
//            ref.putFile(filePath)
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            //if the upload is successfull
//                            //hiding the progress dialog
//                            progressDialog.dismiss();
//
//                            //and displaying a success toast
//                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
//
//                            //downloadUri = taskSnapshot.getDownloadUrl();
//                            sendMsg(filePath.toString());
//
//                            /*vidControl = new MediaController(ChatRoom.this);
//                            vidControl.setAnchorView(vidView);
//                            vidView.setMediaController(vidControl);
//
//                            vidView.setVideoURI(downloadUri);
//                            vidView.start();*/
//
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception exception) {
//                            //if the upload is not successfull
//                            //hiding the progress dialog
//                            progressDialog.dismiss();
//
//                            //and displaying error message
//                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    })
//                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                            //calculating progress percentage
//                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//
//                            //displaying percentage in progress dialog
//                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
//                        }
//                    });
//        }
//        //if there is not any file
//        else {
//            Toast.makeText(getApplicationContext(), "No file ", Toast.LENGTH_LONG).show();
//        }
//    }

    //傳送訊息
    private void sendMsg(String message){
        Map<String,Object> map = new HashMap<String, Object>();
        temp_key = chat_root.push().getKey();
        chat_root.updateChildren(map);

        DatabaseReference message_root = chat_root.child(temp_key);
        Map<String,Object> map2 = new HashMap<String, Object>();
        map2.put("name",user_name);
        map2.put("msg",message);

        message_root.updateChildren(map2);

        count_root = root.child("total");
        count_root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                count_old = dataSnapshot.getValue(Integer.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        count_new++;

        if(count_new!=count_old){
            count_root.setValue(count_new);

            // 通知的識別號碼
            int notifyID = 1;
            // PendingIntent的Request Code
            int requestCode = notifyID;
            // 跳到相對應的activity(房間)
            Intent intent = new Intent(getApplicationContext(),ChatRoom.class);
            intent.putExtra("room_name",room_name);
            // ONE_SHOT：PendingIntent只使用一次；CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的；NO_CREATE：沿用先前的PendingIntent，不建立新的PendingIntent；UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
            int flags = PendingIntent.FLAG_CANCEL_CURRENT;
            // 取得PendingIntent
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, intent, flags);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle("Firebase Push Notification");
            builder.setContentText(user_name+" send "+message);
            //按通知後通知消失
            builder.setAutoCancel(true);
            builder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notifyID, builder.build());
        }


        input_msg.setText("");
    }

}
