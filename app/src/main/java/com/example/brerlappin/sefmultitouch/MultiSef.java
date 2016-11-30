package com.example.brerlappin.sefmultitouch;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.util.UUID;

public class MultiSef extends Activity implements View.OnTouchListener{
    final static int NONE = 0;
    final static int DRAG = 1;
    final static int ZOOM = 2;
    ImageView imgView;
    FrameLayout mainLayout, fragmentContainer;
    LinearLayout optionsLayout, blueToothLayout, blueDevicesLayout;
    Button localButton, blueButton;
    Button blueDiscButton, bluePairButton, blueReciveButton, blueSendButton;
    TextView blueDevicesTitle, blueDevicesTextList, blueNotificationText;
    ScrollView blueDevicesScroll;
    long optionsTimer;

    int touchState = NONE;
    Matrix matrix = new Matrix();
    Matrix eventMatrix = new Matrix();

    boolean waitingResult = false;
    boolean useSystemFilePicker = true;

    boolean usesBluetooth = true;
    MyBluetooth soBlue;
    boolean discoveryStarted = false;

    TextView debugPressure;
    long debugTimer;
    private int typeDebug = 0; //0=Hide, 1=Pressure, 2=Movement matrix




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_sef);

        imgView = (ImageView) findViewById(R.id.imageView);
        imgView.setOnTouchListener(this);

        mainLayout = (FrameLayout) findViewById(R.id.main_layout);
        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

        initOptionsMenu();
        disableOptionsMenu();

//        initBlueOptionsMenu();
//        disableBluetoothOptions();

        initDevicesList();
        disableDevicesList();

        soBlue = new MyBluetooth(this, blueDevicesTextList, imgView);

        imgView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        debugPressure = (TextView) findViewById(R.id.textView);
        debugPressure.setText(" ");
    }

    public void bluetoothCheckEnabled(){
        if(!soBlue.checkEnabled()){
            usesBluetooth = false;
        }else {
//            soBlue.discovery();
//            soBlue.setDiscoverable();
            disableOptionsMenu();
            enableBluetoothOptions();
            //waitingResult = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0){
            waitingResult = false;
            disableOptionsMenu();
            if (resultCode == RESULT_OK) {
                //Local File Request
                matrix.reset();
                eventMatrix.reset();
                imgView.setImageMatrix(matrix);

                String tmp;
                Bitmap imageToChange;
                if(useSystemFilePicker){
                    Uri imageUri = data.getData();
                    ContentResolver content = getContentResolver();
                    try {
                        imageToChange = BitmapFactory.decodeStream(content.openInputStream(imageUri));
                    } catch (FileNotFoundException e) {
                        Log.e("Decode Image", "************************* "+e.toString());
                        return;
                    }
                }else{
                    tmp = data.getExtras().getString("clickedFile");
                    imageToChange = BitmapFactory.decodeFile(tmp);
                }

                Log.v("Image loader", "New image size: "+imageToChange.getAllocationByteCount()+" bytes");
                imgView.setImageBitmap(imageToChange);
            }

//            imgView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        }else if(requestCode == 101){
            if (resultCode == RESULT_CANCELED) {
                //Bluetooth request failed
                Log.d("Bluetooth info", "************************* Failed to enable bluetooth");
                usesBluetooth = false;
            } else if(resultCode == RESULT_OK){
                Log.d("Bluetooth info", "************************* Bluetooth enabled successfully");
                usesBluetooth = true;
//                soBlue.discovery();
//                soBlue.setDiscoverable();
                enableBluetoothOptions();
            }

//            imgView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }else if(requestCode == 102){
            if (resultCode == RESULT_CANCELED) {
                Log.d("Bluetooth info", "************************* Failed to set discoverable");
            } else if(resultCode == RESULT_OK){
                Log.d("Bluetooth info", "************************* Device is discoverable!!");
            }
        }

        imgView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    final static float MIN_DISTANCE = 50;
    static float eventDistance = 0;
    static float centerX=0, centerY=0;
    @Override
    public boolean onTouch(View v, MotionEvent event){
        ImageView view = (ImageView) v;

        //DEBUG
//        System.out.println("\n"+event);
//        System.out.println("Pointers: "+event.getPointerCount());
//        System.out.println("Pressure: "+event.getPressure());
        if(typeDebug==1)
            debugPressure.setText("Pressure: "+event.getPressure());
        else if(typeDebug >= 2)
            debugPressure.setText(matrix.toString());

        if(event.getPointerCount() >= 3){
            if (System.currentTimeMillis()-debugTimer > 500) {
//                if(typeDebug == 0)
//                    typeDebug = event.getPointerCount()-2;
//                else
//                    typeDebug = 0;
                typeDebug++;
                if(typeDebug > 3)
                    typeDebug = 0;

                if(typeDebug == 0) {
                    debugPressure.setText(" ");
                    debugPressure.setBackgroundColor(Color.TRANSPARENT);
                }else
                    debugPressure.setBackgroundColor(Color.WHITE);
                debugTimer = System.currentTimeMillis();
            }
            return false;
        }

        if(event.getPressure() >= 0.40f && System.currentTimeMillis()-optionsTimer > 1000){
            if (!waitingResult) {
                enableOptionsMenu();
                waitingResult = true;
            } else {
                disableOptionsMenu();
                disableBluetoothOptions();
                disableDevicesList();
                waitingResult = false;
            }
            optionsTimer = System.currentTimeMillis();
            return false;
        }

        if(waitingResult)
            return true;

        switch(event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                touchState = DRAG;
                centerX = event.getX();
                centerY = event.getY();
                eventMatrix.set(matrix);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                eventDistance = calcDistance(event);
                calcMidpoint(centerX, centerY, event);
                if(eventDistance > MIN_DISTANCE){
                    eventMatrix.set(matrix);
                    touchState = ZOOM;
                }
                break;
            case MotionEvent.ACTION_MOVE:
//                float[] tempValues = new float[9];
//                float tmpX, tmpY;
                if(touchState == DRAG){
                    if(centerX!=event.getX() || centerY!=event.getY()){
                        matrix.set(eventMatrix);
                        matrix.postTranslate(event.getX(0)-centerX, event.getY(0)-centerY);

//                        matrix.getValues(tempValues);
//                        tmpX = event.getX(0)-centerX;
//                        tmpY = event.getY(0)-centerY;
//
//                        if(tempValues[2]+tmpX < 0-(view.getWidth()/1.2))
//                            tmpX = (float)(Math.abs(tempValues[2])-(view.getWidth()/1.2));
//                        else if(tempValues[2]+tmpX > 960f)
//                            tmpX = 960f-tempValues[2];
//
//                        if(tempValues[5]+tmpY < 0-(view.getHeight()/1.2))
//                            tmpY = (float)(Math.abs(tempValues[5])-(view.getHeight()/1.2));
//                        else if (tempValues[5] > 600f)
//                            tmpY = 600f-tempValues[5];
//
//                        matrix.postTranslate(tmpX, tmpY);
                    }
                }
                else if(touchState == ZOOM){
                    float dist = calcDistance(event);
                    if(dist > MIN_DISTANCE){
                        matrix.set(eventMatrix);
                        float scale = dist/eventDistance;
                        matrix.postScale(scale, scale, centerX, centerY);

//                        matrix.getValues(tempValues);
//                        if(scale < 1.0f){
//                            if(scale < 0.1f)
//                                scale = 0.1f;
//                        }else if(tempValues[0]+scale > 50.0f)
//                            scale = 50.0f-tempValues[0];
//
//                        matrix.postScale(scale, scale, centerX, centerY);
                    }
                }
                view.setImageMatrix(matrix);
                break;
            case MotionEvent.ACTION_POINTER_UP:
//                touchState = DRAG;
//                centerX = event.getX();
//                centerY = event.getY();
//                eventMatrix.set(matrix);
//                break;
            case MotionEvent.ACTION_UP:
                touchState = NONE;
                break;
        }
        return true;
    }

    private float calcDistance(MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x*x + y*y);
    }

    private void calcMidpoint(float centerX, float centerY, MotionEvent event){
        centerX = (event.getX(0) + event.getX(1))/2;
        centerY = (event.getY(0) + event.getY(1))/2;
    }

    public void openImage(){
        String imageDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DevTemp/";

        Intent tmpInt= new Intent(this, ListFiles.class);
        tmpInt.putExtra("directory", imageDir);
        startActivityForResult(tmpInt, 0);
    }
    public void openImageSystem(){
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    public void blueChangeImage(Bitmap image){
        matrix.reset();
        eventMatrix.reset();
        imgView.setImageMatrix(matrix);

        imgView.setImageBitmap(image);
        disableBlueNotification();
    }
    public void disableBlueNotification(){
        mainLayout.bringChildToFront(imgView);
        blueNotificationText.setAlpha(0);
    }

    private void initBlueOptionsMenu(){
        blueToothLayout = (LinearLayout) findViewById(R.id.bluetooth_options);
        //blueAbleButton = (Button) findViewById(R.id.button_discoverable);
        blueDiscButton = (Button) findViewById(R.id.discover_button);
        bluePairButton = (Button) findViewById(R.id.pair_button);
        blueReciveButton = (Button) findViewById(R.id.recive_button);
        blueSendButton = (Button) findViewById(R.id.send_button);
        blueNotificationText = (TextView) findViewById(R.id.blue_notification);
        blueNotificationText.setAlpha(0);

//        blueAbleButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                soBlue.setDiscoverable();
//            }
//        });
        blueDiscButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableDevicesList();
                soBlue.discovery();
                discoveryStarted = true;
            }
        });
        bluePairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //enablePairedList();
                soBlue.prePairDevices();
            }
        });
        blueReciveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blueNotificationText.setText("Receiving image, please wait...");
                mainLayout.bringChildToFront(blueNotificationText);
                blueNotificationText.setAlpha(1);
                soBlue.serverConnection();
            }
        });
        blueSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blueNotificationText.setText("Sending image, please wait...");
                mainLayout.bringChildToFront(blueNotificationText);
                blueNotificationText.setAlpha(1);
                soBlue.clientConnection();
            }
        });
    }
    private void initOptionsMenu(){
        optionsLayout = (LinearLayout) findViewById(R.id.options_layout);
        localButton = (Button) findViewById(R.id.local_button);
        blueButton = (Button) findViewById(R.id.blue_button);

        localButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!useSystemFilePicker) {
                    openImage();
                } else {
                    openImageSystem();
                }
            }
        });
        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(usesBluetooth){
                    usesBluetooth = soBlue.initBluetooth();
                    if(usesBluetooth)
                        bluetoothCheckEnabled();
                    else
                        Toast.makeText(MultiSef.this, "Failed to start Bluetooth service", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MultiSef.this, "This version does not support Bluetooth", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
    private void initDevicesList(){
        blueDevicesLayout = (LinearLayout) findViewById(R.id.blue_devices_window);
        blueDevicesTitle = (TextView) findViewById(R.id.blue_devices_title);
        blueDevicesScroll = (ScrollView) findViewById(R.id.blue_devices_scroll);
        blueDevicesTextList = (TextView) findViewById(R.id.blue_devices);
        blueDevicesTextList.setText(" ");//setDevicesListText(" ", false);
    }
//    public void setDevicesListText(String text, boolean addText){
//        String tmpText;
//        if(addText){
//            tmpText = (String) blueDevicesTextList.getText();
//            blueDevicesTextList.setText(tmpText +"\n"+ text);
//        }else{
//            blueDevicesTextList.setText(text);
//        }
//    }
    public void disableDevicesList(){
        mainLayout.bringChildToFront(imgView);
        disableButton(blueDevicesScroll);
        blueDevicesLayout.setEnabled(false);
        blueDevicesLayout.setAlpha(0);
    }
    public void enableDevicesList(){
        mainLayout.bringChildToFront(blueDevicesLayout);
        enableButton(blueDevicesScroll);
        blueDevicesLayout.setEnabled(true);
        blueDevicesLayout.setAlpha(1);
    }
    public void disableBluetoothOptions(){
        mainLayout.bringChildToFront(imgView);
        //disableButton(blueAbleButton);
        disableButton(blueDiscButton);
        disableButton(bluePairButton);
        disableButton(blueReciveButton);
        disableButton(blueSendButton);
        blueToothLayout.setEnabled(false);
        blueToothLayout.setAlpha(0);
    }
    public void enableBluetoothOptions(){
        mainLayout.bringChildToFront(fragmentContainer);

        BlueFragment blueFragment = new BlueFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, blueFragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }
    public void disableOptionsMenu(){
        mainLayout.bringChildToFront(imgView);
        disableButton(localButton);
        disableButton(blueButton);
        optionsLayout.setEnabled(false);
        optionsLayout.setAlpha(0);
    }
    public void enableOptionsMenu(){
        mainLayout.bringChildToFront(optionsLayout);
        enableButton(localButton);
        enableButton(blueButton);
        optionsLayout.setEnabled(true);
        optionsLayout.setAlpha(1);
    }
    public void disableButton(View tmpBtt){
        tmpBtt.setEnabled(false);
        tmpBtt.setActivated(false);
        tmpBtt.setClickable(false);
        tmpBtt.setFocusable(false);
    }
    public void enableButton(View tmpBtt){
        tmpBtt.setEnabled(true);
        tmpBtt.setActivated(true);
        tmpBtt.setClickable(true);
        tmpBtt.setFocusable(true);
    }

    @Override
    protected void onDestroy(){
        if(discoveryStarted)
            unregisterReceiver(soBlue.tootBrReceiver);

        super.onDestroy();
    }
}
