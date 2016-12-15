package com.example.brerlappin.sefmultitouch;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by BRERLAPPIN on 12/11/2016.
 */

public class MyBluetooth {
    private BluetoothAdapter blueTooth;
    private MultiSef mainActivity;
    private ImageView mainImage;
    private ArrayList<BluetoothDevice> devicesList, bondedList;
    private BluetoothDevice bondedDevice;
    private final String UUID_STRING = "6014de2c-f402-44d5-8073-326cd153d59f";
    private UUID myUUID;
    //String deviceName = "NiggaBluethootTest";

    //MyBluetooth(MultiSef actv, TextView textView, ImageView tmpimg){
    MyBluetooth(MultiSef actv, ImageView tmpimg){
        mainActivity = actv;
        mainImage = tmpimg;
        devicesList = new ArrayList<BluetoothDevice>();
        bondedList = new ArrayList<BluetoothDevice>(3);
        initBluetooth();
    }

    public boolean initBluetooth(){
        if (blueTooth == null) {
            BluetoothManager manager = (BluetoothManager) mainActivity.getSystemService(Activity.BLUETOOTH_SERVICE);
            blueTooth = manager.getAdapter();
        }
        if(blueTooth == null)
            return false;
        else {
            if(myUUID == null)
                myUUID = UUID.fromString(UUID_STRING);
            return true;
        }
    }
    public boolean checkEnabled(){
        if(!blueTooth.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mainActivity.startActivityForResult(enableIntent, 101);
            return false;
        }else {
            return true;
        }
    }
    public void prePairDevices(){
//        Set<BluetoothDevice> pairedDevices = blueTooth.getBondedDevices();
//        for(BluetoothDevice tmpDevice: pairedDevices){
//            bondedDevice = tmpDevice;
//        }
//        if(bondedDevice == null)
//            Toast.makeText(mainActivity.getApplicationContext(), "No devices found to pair to", Toast.LENGTH_LONG).show();
//        else
//            Toast.makeText(mainActivity.getApplicationContext(), "Devices paired successfully", Toast.LENGTH_LONG).show();


        if(devicesList.isEmpty()){
            Toast.makeText(mainActivity.getApplicationContext(), "No devices found to pair to", Toast.LENGTH_LONG).show();
        }else {
            Set<BluetoothDevice> pairedDevices = blueTooth.getBondedDevices();
            for(BluetoothDevice tmpDevice: devicesList){
                if(pairedDevices.contains(tmpDevice)){
                    bondedList.add(tmpDevice);
                }
            }
            if(bondedList.isEmpty()){
                Toast.makeText(mainActivity.getApplicationContext(), "You have not bonded with a near device", Toast.LENGTH_LONG).show();
            }else{
                //Pair directly with found device, otherwise, ask the user to select a device to pair to.
                if(bondedList.size() > 1){
                    //Show a list of bonded devices
                    mainActivity.enableDevicesSelection(bondedList);
                    //Toast.makeText(mainActivity.getApplicationContext(), "Here you should see a list of bonded devices", Toast.LENGTH_LONG).show();
                }else{
                    bondedDevice = bondedList.get(0);
                    Toast.makeText(mainActivity.getApplicationContext(), "Devices paired successfully", Toast.LENGTH_LONG).show();
                    //This will be done after sending of receiving selection //pairDevices();
                }
            }
        }
    }
    private void pairDevices(){
        //Do the actual pairing
    }
    public void serverConnection(){
        if(bondedDevice == null){
            Toast.makeText(mainActivity.getApplicationContext(), "Please pair with a device first", Toast.LENGTH_LONG).show();
        }else{
            //Need to be in a Thread!!!!!
            //Create an isntance of ServerThread
            ServerThread server = new ServerThread();
            server.start();
        }
    }
    public void clientConnection(){
        if(bondedDevice == null){
            Toast.makeText(mainActivity.getApplicationContext(), "Please pair with a device first", Toast.LENGTH_LONG).show();
        }else{
            ClientThread client = new ClientThread(bondedDevice);
            client.start();
        }
    }

    public void setDiscoverable(){
        int mode = blueTooth.getScanMode();
        if(mode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            mainActivity.startActivityForResult(discIntent, 102);
        }else
            Toast.makeText(mainActivity.getApplicationContext(), "Device is already discoverable", Toast.LENGTH_SHORT).show();
    }

    public interface OnDiscoveryListener{
        void onDiscoveryStatusChanged(String txt, boolean statusBar);
    }
    public void discovery(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mainActivity.registerReceiver(tootBrReceiver, filter);

        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mainActivity.registerReceiver(tootBrReceiver, filter2);

        IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mainActivity.registerReceiver(tootBrReceiver, filter3);

        blueTooth.startDiscovery();
    }
    public void stopBlueDiscovery(){
        blueTooth.cancelDiscovery();
        Log.v("Bluetooth", "Bluetooth device discovery canceled!!");
    }

    public final BroadcastReceiver tootBrReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String tmpTxt = " ";
            Log.d("Bluetooth Testing", "************************* Action received: "+action);
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devicesList.add(device);
                Log.v("Bluetooth Testing", "************************* "+device.getName()+"\n"+device.getAddress());

                mainActivity.onDiscoveryStatusChanged(device.getName(), false);

            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                mainActivity.onDiscoveryStatusChanged(mainActivity.getString(R.string.status_searching), true);

            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                tmpTxt = "Search finished. ";
                if(devicesList.isEmpty()){
                    tmpTxt = tmpTxt + "No devices found.";
                }
                mainActivity.onDiscoveryStatusChanged(tmpTxt, true);
            }
        }
    };

    public static Message createMessage(char msg){
        Message newMsg = Message.obtain();
        Bundle bundle = new Bundle(1);
        bundle.putChar("textID", msg);
        newMsg.setData(bundle);
        return newMsg;
    }
    private class ServerThread extends Thread{
        private BluetoothServerSocket serverSocket;
        public ServerThread(){
            try {
                serverSocket = blueTooth.listenUsingRfcommWithServiceRecord("ImageService", myUUID);
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e("Bluetooth connection", e.getMessage());
                Toast.makeText(mainActivity.getApplicationContext(), "Error while creating connection", Toast.LENGTH_LONG).show();
            }
        }

        public void run(){
            BluetoothSocket socket;
            if(serverSocket == null) {
                mainActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(mainActivity.getApplicationContext(), "Connection Socket is null, aborting!!", Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }
            //mainActivity.blueNotificationText.setText(mainActivity.getString(R.string.transfer_status_waiting));
            mainActivity.blueStatusHandler.sendMessage(MyBluetooth.createMessage('w'));
            //mainActivity.enableBlueNotification();
            while(true){
                try {
                    socket = serverSocket.accept();

                    if(socket != null){
                        //Call method to receive image
                        DataTransmitterThread dataThread = new DataTransmitterThread(socket, mainImage);
                        //mainActivity.blueNotificationText.setText(mainActivity.getString(R.string.transfer_status_receiving));
                        mainActivity.blueStatusHandler.sendMessage(MyBluetooth.createMessage('r'));
                        //mainActivity.enableBlueNotification();
                        dataThread.start();
                        serverSocket.close();
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    mainActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(mainActivity.getApplicationContext(), "Error while trying to establish connection", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                }
            }
            mainActivity.disableBlueNotification();
        }

        public void cancel(){
            try {
                serverSocket.close();
            } catch (IOException e) {
                Toast.makeText(mainActivity.getApplicationContext(), "Connection was canceled", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private class ClientThread extends Thread{
        private BluetoothSocket socket;
        private BluetoothDevice device;

        public ClientThread(BluetoothDevice tmpDevice){
            device = tmpDevice;

            try {
                socket = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e("Bluetooth connection", e.getMessage());
                Toast.makeText(mainActivity.getApplicationContext(), "Error while creating connection", Toast.LENGTH_LONG).show();
            }
        }

        public void run(){
            stopBlueDiscovery();//Por si las dudas

            try {
                socket.connect();
                DataTransmitterThread dataThread = new DataTransmitterThread(socket, mainImage);
                //mainActivity.blueNotificationText.setText(mainActivity.getString(R.string.transfer_status_send));
                mainActivity.blueStatusHandler.sendMessage(MyBluetooth.createMessage('s'));
                mainActivity.enableBlueNotification();
                dataThread.write();
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e("Bluetooth connection", e.getMessage());

                mainActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(mainActivity.getApplicationContext(), "Error while making connection", Toast.LENGTH_LONG).show();
                    }
                });
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            mainActivity.disableBlueNotification();
        }

        public void cancel(){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class DataTransmitterThread extends Thread{
        private BluetoothSocket socket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private ImageView imageView;
        private Bitmap image;

        public DataTransmitterThread(BluetoothSocket tmpSkt, ImageView img){
            imageView = img;
            BitmapDrawable tmpbmp = (BitmapDrawable)imageView.getDrawable();
            image = tmpbmp.getBitmap();
            socket = tmpSkt;

            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e("Bluetooth connection", e.getMessage());
                Toast.makeText(mainActivity.getApplicationContext(), "Error while starting sending/receiving process", Toast.LENGTH_LONG).show();
            }
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            image = BitmapFactory.decodeStream(inputStream);
//            try {
//                bytes = inputStream.read(buffer);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            mainActivity.blueChangeImage(image);
        }

        public void write(){
            ByteBuffer byteBuffer = ByteBuffer.allocate(image.getAllocationByteCount());
            image.copyPixelsToBuffer(byteBuffer);
            try {
                outputStream.write(byteBuffer.array());
                mainActivity.disableBlueNotification();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel(){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
