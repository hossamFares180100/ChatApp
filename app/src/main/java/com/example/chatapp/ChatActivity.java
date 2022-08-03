package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.capybaralabs.swipetoreply.ISwipeControllerActions;
import com.capybaralabs.swipetoreply.SwipeController;
import com.example.chatapp.adapters.MessageAdapter;
import com.example.chatapp.adapters.MessageAdapterListener;
import com.example.chatapp.model.AudioMessage;
import com.example.chatapp.model.FcmNotificationsSender;
import com.example.chatapp.model.ImageMessage;
import com.example.chatapp.model.TextMessage;
import com.example.chatapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.base.MoreObjects;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.keenfin.audioview.AudioService;
import com.tougee.recorderview.AudioRecordView;
import com.vanniktech.emoji.EmojiPopup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements MessageAdapterListener, AudioRecordView.Callback{

    CircleImageView userImage;
    TextView name, reply_to, reply_message;
    ImageView back, imageSend, emoji, fabCamera, fab, attach, music, location, docs, video;
    AudioRecordView fabSpeech;
    Vibrator vibrator;
    EmojiPopup popup;
    LinearLayout rl_attach;
    EditText message;
    ImageButton icon_re;
    RelativeLayout rl_reply;
    ConstraintLayout cl_edit;
    String mrecipientId, currentUserId;
    private FirebaseFirestore firestoreInstance;
    Map<String, Object> map;
    CollectionReference chatChannelCollectionRef;
    ArrayList<TextMessage> textmessages;
    ArrayList<ImageMessage> imageMessages;
    private int BACKUPIMAGE = 2, x = 0;
    TextMessage textM;
    ImageMessage imageM;
    private static final int RECOGNIZER_RESULT = 1;
    private static final int CAMERA_REQUEST = 1888;
    User user;
    RecyclerView messageRecycle;
    MessageAdapter messageAdapter;
    private StorageReference currentImageRef, currentAudioRef;
    private FirebaseStorage firebaseStorage;
    ImageMessage imageMessage;
    ArrayList<Pair<Pair<TextMessage, AudioMessage>, Pair<ImageMessage, AudioMessage>>> messagePair;
    //DocumentReference currentUserDocRef;
    RequestQueue mRequestQueue;
    User currentUser;
    SpeechRecognizer mSpeechRecognizer;
    Intent mSpeechRecognizerIntent;
    private MediaRecorder mRecorder;
    private String mFileName = null;
    private static final String LOG_TAG = "Record_log";
    private ProgressDialog mProgress;
    AudioMessage audioMessage, audioMessage2;
    boolean audioP = true, readP = true, cameraP = false;
    String sharedText;
    Uri shareImage;
    boolean isSelected = false;
    String URL = "https://fcm.googleapis.com/fcm/send";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
        if (Build.VERSION.SDK_INT >= 23) {
            askPermission();
        }
        Intent audioService = new Intent(this, AudioService.class);
        audioService.setAction(AudioService.ACTION_START_AUDIO);
        startService(audioService);
        //getUserInfo();

        createChatChannel();
        chatChannelCollectionRef = firestoreInstance.collection("chatChannels");
        setImage();

        message.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                rl_attach.setVisibility(View.GONE);
                return false;
            }
        });
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (message.getText().toString().length() > 0) {
                    fab.setVisibility(View.GONE);
                    fabCamera.setVisibility(View.GONE);
                    fabSpeech.setVisibility(View.GONE);
                    fabSpeech.setVisibility(View.GONE);
                    imageSend.setVisibility(View.VISIBLE);

                } else {
                    fabSpeech.setVisibility(View.VISIBLE);
                    imageSend.setVisibility(View.GONE);
                    fab.setVisibility(View.VISIBLE);
                    fabCamera.setVisibility(View.VISIBLE);
                    fabSpeech.setVisibility(View.VISIBLE);
                }

            }
        });



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String m = message.getText().toString();
                rl_reply.setVisibility(View.GONE);
                if (!m.isEmpty()) {
                    TextMessage messageSend = new TextMessage(m, currentUserId, MainActivity.user.getName(), mrecipientId, "", "TEXT", Calendar.getInstance().getTime());
                    messageSend.setType("TEXT");
                    sentMessage(messageSend);
                    message.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, "Empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (x == 0) {
                    rl_attach.setVisibility(View.VISIBLE);
                    x = 4;
                } else {
                    rl_attach.setVisibility(View.GONE);
                    x = 0;
                }

            }
        });

        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Audio"), 10);
            }
        });

        docs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("docx/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Docx"), 15);
            }
        });

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), 20);
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, BACKUPIMAGE);


            }
        });

        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra("crop", "true");
                cameraIntent.putExtra("aspectX", 1);
                cameraIntent.putExtra("aspectY", 1);
                cameraIntent.putExtra("outputX", 200);
                cameraIntent.putExtra("outputY", 200);
                cameraIntent.putExtra("return-data", true);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        /*fabSpeech.setOnTouchListener((View.OnTouchListener) (v, event) -> {

            if (audioP && readP) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        stopRecording();
                        Toast.makeText(this, "recording stopped", Toast.LENGTH_SHORT).show();
                        break;
                    case MotionEvent.ACTION_DOWN:
                        startRecording();
                        Toast.makeText(this, "recording started", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            return false;
        });*/


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        linearLayoutManager.setReverseLayout(true);
        messageRecycle.setLayoutManager(linearLayoutManager);
        messageRecycle.setHasFixedSize(true);
        messageRecycle.setAdapter(messageAdapter);


        SwipeController controller = new SwipeController(this, new ISwipeControllerActions() {
            @Override
            public void onSwipePerformed(int position) {
                message.setPressed(true);
                    /*reply_to.setText("Replying to "+messagePair.get(position).first.getSenderName());
                    reply_message.setText(messagePair.get(position).first.getText());
                    rl_reply.setVisibility(View.VISIBLE);*/

            }
        });

        icon_re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_reply.setVisibility(View.GONE);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(controller);
        itemTouchHelper.attachToRecyclerView(messageRecycle);
    }

    void init() {
        cl_edit=findViewById(R.id.cl_edit);
        docs = findViewById(R.id.iv_docs);
        video = findViewById(R.id.iv_video);
        music = findViewById(R.id.iv_music);
        location = findViewById(R.id.iv_location);
        attach = findViewById(R.id.fab_attach);
        rl_attach = findViewById(R.id.rl_attach);
        reply_to = findViewById(R.id.reply_to);
        reply_message = findViewById(R.id.messageReply);
        rl_reply = findViewById(R.id.rl_reply);
        icon_re = findViewById(R.id.remove);
        audioMessage = new AudioMessage();
        audioMessage2 = new AudioMessage();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mProgress = new ProgressDialog(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            mFileName = this.getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/" + "/recorded_audio.3gp";
        } else {
            mFileName = Environment.getExternalStorageDirectory().toString() + "/" + "/recorded_audio.3gp";
        }
        //  mFileName= Environment.getExternalStorageDirectory().getAbsolutePath();

        //mFileName+="/recorded_audio.3gp";

        fab = findViewById(R.id.fab_image);
        fabSpeech = findViewById(R.id.fab_speech);
        fabSpeech.activity = this;
        fabSpeech.callback = this;
        fabSpeech.setMicIcon(R.drawable.ic_mic);
        fabSpeech.setMicActiveIcon(R.drawable.ic_play_arrow);
        fabSpeech.setMicHintEnable(true);
        fabSpeech.setMicHintText("Hold to record, release to send");
        fabSpeech.setMicHintColor(R.color.colorPrimary);
        fabSpeech.setMicHintBg(R.drawable.rcv_msg);
        fabSpeech.setBlinkColor(ContextCompat.getColor(this, R.color.color_blue));
        fabSpeech.setCircleColor(ContextCompat.getColor(this, R.color.colorPrimary));
        fabSpeech.setCancelIconColor(ContextCompat.getColor(this, R.color.color_blue));
        fabSpeech.setSlideCancelText("Slide to cancel");
        fabSpeech.setCancelText("Cancel");
        fabSpeech.setVibrationEnable(false);
        fabSpeech.setTimeoutSeconds(60);
        fabCamera = findViewById(R.id.fab_camera);
        userImage = findViewById(R.id.image_view_profile_picture);
        emoji = findViewById(R.id.imageView_emoji);

        name = findViewById(R.id.txt_view_username);
        back = findViewById(R.id.image_view_back);
        imageSend = findViewById(R.id.imageview_send);
        message = findViewById(R.id.et_message);
        popup = EmojiPopup.Builder.fromRootView(
                findViewById(R.id.root_view)
        ).build(message);
        emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //toggle between text and emoji
                popup.toggle();
            }
        });
        textmessages = new ArrayList<>();
        imageMessages = new ArrayList<>();
        messagePair = new ArrayList<>();
        messageRecycle = findViewById(R.id.recycle_chat_message);
        messageAdapter = new MessageAdapter(messagePair, this, this);
        map = new HashMap<>();
        user = (User) getIntent().getSerializableExtra("user");
        if (getIntent().getStringExtra("sharedText") != null) {
            sharedText = getIntent().getStringExtra("sharedText");
            message.setText(sharedText);
            MainActivity.sharedText = null;
        }

        currentUser = new User();
        mrecipientId = user.getId();
        firestoreInstance = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        currentImageRef = firebaseStorage.getReference();
        currentAudioRef = firebaseStorage.getReference();
        // currentUserDocRef = firestoreInstance.document("users/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
        imageMessage = new ImageMessage();
        mRequestQueue = Volley.newRequestQueue(this);
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        if (getIntent().getParcelableExtra("sharedImage") != null) {
            shareImage = getIntent().getParcelableExtra("sharedImage");
            Bitmap selectedImageBitmap = null;
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), shareImage);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream);
                byte[] selectedImageBytes = outputStream.toByteArray(); // now image is in this byte array
                uploadImage(selectedImageBytes);


            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("errorImage", e.getMessage());
            }
        }

    }

    private void askPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 100);
        }

    }


    private void startRecording() {
        vibrator.vibrate(500);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOnErrorListener(errorListener);
        mRecorder.setOnInfoListener(infoListener);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            Log.e(LOG_TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            uploadAudio();
        }
    }

    private final MediaRecorder.OnErrorListener errorListener = (mr, what, extra) -> Toast.makeText(ChatActivity.this, "Error: " + what + ", " + extra, Toast.LENGTH_SHORT).show();

    private final MediaRecorder.OnInfoListener infoListener = (mr, what, extra) -> Toast.makeText(ChatActivity.this, "Warning: " + what + ", " + extra, Toast.LENGTH_SHORT).show();

    private void uploadAudio() {
        mProgress.setMessage("Uploading Audio ...");
        mProgress.show();
        Uri uri = Uri.fromFile(new File(mFileName));
        UUID audioName = UUID.randomUUID();
        currentAudioRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/audios/" + audioName)
                .putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    saveAudioUri(audioName);
                    mProgress.dismiss();
                    Toast.makeText(ChatActivity.this, "uploading finished", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void saveAudioUri(UUID audioName) {
        currentAudioRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/audios/" + audioName)
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        audioMessage.setAudioUri(uri.toString());
                        audioMessage.setSenderId(currentUserId);
                        audioMessage.setRecipientId(mrecipientId);
                        audioMessage.setDate(Calendar.getInstance().getTime());
                        audioMessage.setType("AUDIO");
                        audioMessage.setSenderName(currentUser.getName());
                        audioMessage.setRecipientName("");
                        audioMessage.setAudioName(audioName.toString());
                        chatChannelCollectionRef.document(map.get("channelId").toString()).collection("messages").add(audioMessage);

                    }
                });
    }

    private void uploadImage(byte[] selectedImageBytes) {
        currentImageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/images/" + UUID.nameUUIDFromBytes(selectedImageBytes))
                .putBytes(selectedImageBytes)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            saveUri(selectedImageBytes);
                        }
                    }
                });
    }

    private void saveUri(byte[] selectedImageBytes) {
        UUID imageName = UUID.nameUUIDFromBytes(selectedImageBytes);
        currentImageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/images/" + imageName)
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageMessage.setImageUri(uri.toString());
                        imageMessage.setSenderId(currentUserId);
                        imageMessage.setRecipientId(mrecipientId);
                        imageMessage.setDate(Calendar.getInstance().getTime());
                        imageMessage.setType("IMAGE");
                        imageMessage.setSenderName(currentUser.getName());
                        imageMessage.setRecipientName(user.getName());
                        imageMessage.setMessageId(String.valueOf(imageName));
                        chatChannelCollectionRef.document(map.get("channelId").toString()).collection("messages").add(imageMessage);
                        sendNotification(user.getToken(), null, imageMessage);
                    }
                });

    }

    private void setImage() {
        if (user.getProfileImage() != null) {
            Glide.with(this).load(user.getProfileImage()).into(userImage);
            name.setText(user.getName());
        } else {
            userImage.setImageResource(R.drawable.ic_account);
        }
    }

    private void sentMessage(TextMessage message) {

        sendNotification(user.getToken(), message, null);
        chatChannelCollectionRef.document(map.get("channelId").toString()).collection("messages").add(message);

    }

    void sendNotification(String token, TextMessage message, ImageMessage imageMessage) {

        // to send to all users
        //FcmNotificationsSender notificationsSender = new FcmNotificationsSender("/topics/all","message from "+message.getSenderName(),message.getText(),getApplicationContext(),this);
        // to send to all users
        if (imageMessage == null) {
            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(token, "message from " + message.getSenderName(), message.getText(), getApplicationContext(), this, null);
            notificationsSender.SendNotifications();
        } else {
            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(token, "message from " + imageMessage.getSenderName(), "", getApplicationContext(), this, imageMessage.getImageUri());
            notificationsSender.SendNotifications();
        }


    }

    private void createChatChannel() {

        firestoreInstance.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("sharedChat")
                .document(mrecipientId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        if (documentSnapshot.exists()) {
                            //Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show();
                            map.put("channelId", documentSnapshot.get("channelId"));
                            getMessages(map.get("channelId").toString());

                        } else {

                            DocumentReference newChatChannel = firestoreInstance.collection("users").document();
                            map.put("channelId", newChatChannel.getId());
                            firestoreInstance.collection("users")
                                    .document(mrecipientId)
                                    .collection("sharedChat")
                                    .document(currentUserId)
                                    .set(map);


                            firestoreInstance.collection("users")
                                    .document(currentUserId)
                                    .collection("sharedChat")
                                    .document(mrecipientId)
                                    .set(map);

                            getMessages(map.get("channelId").toString());


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "fail", Toast.LENGTH_SHORT).show();
            }
        });


    }


    void getMessages(String channelId) {

        Query query = chatChannelCollectionRef.document(channelId).collection("messages").
                orderBy("date", Query.Direction.DESCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                messagePair.clear();
                if (error != null) {
                    return;
                }
                if (value.getDocuments() != null) {
                    for (DocumentSnapshot document : value.getDocuments()) {
                        if (document.get("type").equals("TEXT")) {
                            textM = document.toObject(TextMessage.class);
                            if (textM.getSenderId().equals(currentUserId)) {
                                textM.setMessageId(document.getId());
                                textM.setRecipientId(mrecipientId);
                                textM.setUser(0);
                                messagePair.add(new Pair<>(new Pair<>(textM, null), null));
                            } else {
                                textM.setMessageId(document.getId());
                                textM.setRecipientId(mrecipientId);
                                textM.setUser(1);
                                messagePair.add(new Pair<>(new Pair<>(textM, null), null));
                            }
                        } else if (document.get("type").equals("IMAGE")) {
                            imageM = document.toObject(ImageMessage.class);
                            assert imageM != null;
                            if (imageM.getSenderId().equals(currentUserId)) {
                                imageM.setMessageId(document.getId());
                                imageM.setRecipientId(mrecipientId);
                                imageM.setUser(0);
                                messagePair.add(new Pair<>(null, new Pair<>(imageM, null)));
                            } else {
                                imageM.setMessageId(document.getId());
                                imageM.setRecipientId(mrecipientId);
                                imageM.setUser(1);
                                messagePair.add(new Pair<>(null, new Pair<>(imageM, null)));
                            }


                        } else if (document.get("type").equals("AUDIO")) {
                            audioMessage2 = document.toObject(AudioMessage.class);
                            assert audioMessage2 != null;
                            if (audioMessage2.getSenderId().equals(currentUserId)) {
                                audioMessage2.setMessageId(document.getId());
                                audioMessage2.setRecipientId(mrecipientId);
                                audioMessage2.setUser(0);
                                messagePair.add(new Pair<>(null, new Pair<>(null, audioMessage2)));
                            } else {
                                audioMessage2.setMessageId(document.getId());
                                audioMessage2.setRecipientId(mrecipientId);
                                audioMessage2.setUser(1);
                                messagePair.add(new Pair<>(null, new Pair<>(null, audioMessage2)));
                            }


                        } else if (document.get("type").equals("video")) {
                            audioMessage2 = document.toObject(AudioMessage.class);
                            assert audioMessage2 != null;
                            if (audioMessage2.getSenderId().equals(currentUserId)) {
                                audioMessage2.setMessageId(document.getId());
                                audioMessage2.setRecipientId(mrecipientId);
                                audioMessage2.setUser(0);
                                messagePair.add(new Pair<>(new Pair<>(null, audioMessage2), null));
                            } else {
                                audioMessage2.setMessageId(document.getId());
                                audioMessage2.setRecipientId(mrecipientId);
                                audioMessage2.setUser(1);
                                messagePair.add(new Pair<>(new Pair<>(null, audioMessage2), null));
                            }
                        }

                    }
                    messageAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    @Override
    public void onImageClicked(ImageMessage imageMessage) {
        Intent intent = new Intent(this, ShowImageActivity.class);
        intent.putExtra("imageMessage", imageMessage);
        startActivity(intent);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0) {
                    audioP = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    readP = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    cameraP = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                }
                break;

        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            uploadMusic(uri);
        }

        if (requestCode == 15 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            uploaddocx(uri);
        }

        if (requestCode == 20 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            uploadVideo(uri);
        }

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            assert data != null;
            Bitmap theImage = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            theImage.compress(Bitmap.CompressFormat.JPEG, 25, outputStream);
            byte[] selectedImageBytes = outputStream.toByteArray(); // now image is in this byte array
            uploadImage(selectedImageBytes);

        }

        if (requestCode == RECOGNIZER_RESULT && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            message.setText(matches.get(0).toString());
        }

        if (requestCode == BACKUPIMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImagePath = data.getData();
            //to compress the image
            Bitmap selectedImageBitmap = null;
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImagePath);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream);
                byte[] selectedImageBytes = outputStream.toByteArray(); // now image is in this byte array
                uploadImage(selectedImageBytes);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void uploadVideo(Uri uri) {

        mProgress.setMessage("Uploading Video ...");
        mProgress.show();
        UUID uuid = UUID.randomUUID();
        currentAudioRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/video/" + uuid)
                .putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    saveVideoUri(uuid);
                    mProgress.dismiss();
                    Toast.makeText(ChatActivity.this, "uploading finished", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void saveVideoUri(UUID uuid) {


        currentAudioRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/video/" + uuid)
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        audioMessage.setAudioUri(uri.toString());
                        audioMessage.setSenderId(currentUserId);
                        audioMessage.setRecipientId(mrecipientId);
                        audioMessage.setDate(Calendar.getInstance().getTime());
                        audioMessage.setType("video");
                        audioMessage.setSenderName(currentUser.getName());
                        audioMessage.setRecipientName("");
                        audioMessage.setAudioName(uuid.toString());
                        chatChannelCollectionRef.document(map.get("channelId").toString()).collection("messages").add(audioMessage);

                    }
                });
    }

    private void uploaddocx(Uri uri) {

        mProgress.setMessage("Uploading Audio ...");
        mProgress.show();
        UUID uuid = UUID.randomUUID();
        currentAudioRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/docx/" + uuid)
                .putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    saveDocxUri(uuid);
                    mProgress.dismiss();
                    Toast.makeText(ChatActivity.this, "uploading finished", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void saveDocxUri(UUID uuid) {
        UUID uuid1 = UUID.randomUUID();
        currentAudioRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/docx/" + uuid1)
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        audioMessage.setAudioUri(uri.toString());
                        audioMessage.setSenderId(currentUserId);
                        audioMessage.setRecipientId(mrecipientId);
                        audioMessage.setDate(Calendar.getInstance().getTime());
                        audioMessage.setType("docx");
                        audioMessage.setSenderName(currentUser.getName());
                        audioMessage.setRecipientName("");
                        audioMessage.setAudioName(uuid1.toString());
                        chatChannelCollectionRef.document(map.get("channelId").toString()).collection("messages").add(audioMessage);

                    }
                });
    }

    private void uploadMusic(Uri uri) {
        mProgress.setMessage("Uploading Audio ...");
        mProgress.show();
        UUID uuid = UUID.randomUUID();
        currentAudioRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/audios/" + uuid)
                .putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    saveAudioUri(uuid);
                    mProgress.dismiss();
                    Toast.makeText(ChatActivity.this, "uploading finished", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }



    @Override
    public boolean isReady() {
        //Toast.makeText(this, "ready", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onRecordCancel() {
      //  Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show();
        fab.setVisibility(View.VISIBLE);
        fabCamera.setVisibility(View.VISIBLE);
        cl_edit.setVisibility(View.VISIBLE);
        attach.setVisibility(View.VISIBLE);
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder=null;

    }

    @Override
    public void onRecordEnd() {
       // Toast.makeText(this, "end", Toast.LENGTH_SHORT).show();
        fab.setVisibility(View.VISIBLE);
        fabCamera.setVisibility(View.VISIBLE);
        cl_edit.setVisibility(View.VISIBLE);
        attach.setVisibility(View.VISIBLE);
        stopRecording();


    }

    @Override
    public void onRecordStart() {
        fab.setVisibility(View.GONE);
        fabCamera.setVisibility(View.GONE);
        attach.setVisibility(View.GONE);
        cl_edit.setVisibility(View.GONE);
        //Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();

        startRecording();
    }
}








/* mSpeechRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
         mSpeechRecognizerIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(ChatActivity.this, "ready", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onBeginningOfSpeech() {
                Toast.makeText(ChatActivity.this, "begin", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {
                Toast.makeText(getApplicationContext(), error+"", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String>matches=results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(matches!=null){
                    Log.e("match",matches.get(0));
                    message.setText(matches.get(0));
                }else{
                    Toast.makeText(ChatActivity.this, "null", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert...");


        fabSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent speechIntent= new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert...");
                startActivityForResult(speechIntent,RECOGNIZER_RESULT);
            }
        });


        */


