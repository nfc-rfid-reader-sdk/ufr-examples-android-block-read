package net.dlogic.android.ufr.block_read_example;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dlogic.android.ufr.DlReader;

/**
 * Created by d-logic on 15.5.2015..
 */

public class Main extends Activity {
    static Context context;
    DeviceConnectionSynchronizer dev_con;
    DlReader device;
    String err_msg_tag;
    Button btnOpen;
    Button btnReaderType;
    Button btnTagId;
    Button btnBlockRead;
    Button btnClose;
    static EditText ebBlockAddr;
    static EditText ebDeviceType;
    static EditText ebTagId;
    static EditText ebTagUid;
    static EditText ebBlockData;
    private static IncomingHandler handler = new IncomingHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = this;
        try {
            device = DlReader.getInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dev_con = DeviceConnectionSynchronizer.getInstance();

        // Get references to UI widgets:
        ebBlockAddr = (EditText) findViewById(R.id.ebBlockAddr);
        ebDeviceType = (EditText) findViewById(R.id.ebDeviceType);
        ebDeviceType.setInputType(0);
        ebTagId = (EditText) findViewById(R.id.ebTagId);
        ebTagId.setInputType(0);
        ebTagUid = (EditText) findViewById(R.id.ebTagUid);
        ebTagUid.setInputType(0);
        ebBlockData = (EditText) findViewById(R.id.ebBlockData);
        ebBlockData.setInputType(0);

        btnOpen = (Button) findViewById(R.id.btnOpen);
        btnReaderType = (Button) findViewById(R.id.btnDeviceType);
        btnTagId = (Button) findViewById(R.id.btnTagId);
        btnBlockRead = (Button) findViewById(R.id.btnBlockRead);
        btnClose = (Button) findViewById(R.id.btnClose);

        Resources res = getResources();
        err_msg_tag = res.getString(R.string.app_name);

        btnOpen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {
                    Toast.makeText(context, "Device already connected...", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (!dev_con.isConnectingInProgress()) {
                        new Thread(new ReaderThread(Consts.TASK_CONNECT)).start();
                        dev_con.beginConnection();
                    }
                    else {
                        Toast.makeText(context, "Connecting in progress.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btnReaderType.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {
                    try {
                        new Thread(new ReaderThread(Consts.TASK_GET_READER_TYPE)).start();
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(context, "Device not connected...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnTagId.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {
                    try {
                        new Thread(new ReaderThread(Consts.TASK_GET_CARD_ID)).start();
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(context, "Device not connected...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnBlockRead.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {
                    if (Tools.isNumeric(ebBlockAddr.getText().toString())) {
                        int i = Integer.parseInt(ebBlockAddr.getText().toString());
                        if ((i >= 0) && (i < Consts.MAX_BLOCK_ADDR)) {
                            dev_con.setBlockAddr((byte) i);
                            try {
                                new Thread(new ReaderThread(Consts.TASK_BLOCK_READ)).start();
                            } catch (Exception e) {
                                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(context, "Wrong block address.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(context, "Block address must be a number.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(context, "Device not connected...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {
                    ebBlockAddr.setText("0");
                    ebDeviceType.setText("");
                    ebTagId.setText("");
                    ebTagUid.setText("");
                    ebBlockData.setText("");
                    new Thread(new ReaderThread(Consts.TASK_DISCONNECT)).start();
                } else {
                    Toast.makeText(context, "Device not connected...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Consts.RESPONSE_CONNECTED:
                    Toast.makeText(context, "Device successfully connected.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_READER_TYPE:
                    ebDeviceType.setText(Integer.toHexString(msg.arg1));
                    Toast.makeText(context, "Device type obtained successfully.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_CARD_ID:
                    ebTagId.setText(Integer.toHexString(msg.arg1));
                    ebTagUid.setText(Tools.byteA2Str((byte[])msg.obj));
                    Toast.makeText(context, "Card Id obtained successfully.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_BLOCK_READ:
                    ebBlockData.setText(Tools.byteA2Str((byte[])msg.obj));
                    Toast.makeText(context, "Block successfully read.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_DISCONNECTED:
                    Toast.makeText(context, "Device successfully disconnected.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_ERROR:
                    Toast.makeText(context, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Toast.makeText(context, "Unknown response.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private class Consts {
        public static final int TASK_CONNECT = 1;
        public static final int TASK_GET_READER_TYPE = 2;
        public static final int TASK_GET_CARD_ID = 3;
        public static final int TASK_BLOCK_READ = 4;
        public static final int TASK_DISCONNECT = 5;

        public static final int RESPONSE_CONNECTED = 100;
        public static final int RESPONSE_READER_TYPE = 101;
        public static final int RESPONSE_CARD_ID = 102;
        public static final int RESPONSE_BLOCK_READ = 103;
        public static final int RESPONSE_DISCONNECTED = 104;

        public static final int RESPONSE_ERROR = 400;

        public static final int MAX_BLOCK_ADDR = 255;
        public static final byte DEFAULT_AUTH_MODE = DlReader.Consts.MIFARE_AUTHENT1A;
    }

    class ReaderThread implements Runnable {
        private int task;
        private byte[]data;
        byte[] default_key = new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        DlReader.CardParams c_params = new DlReader.CardParams();

        public ReaderThread(int ptask)  {
            task = ptask;
        }

        @Override
        public void run() {

            switch (task) {
                case Consts.TASK_CONNECT:
                    try {
                        device.open();
                        dev_con.connected();
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_CONNECTED));
                    } catch (Exception e) {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, 0, 0, e.getMessage()));
                    }
                    break;

                case Consts.TASK_GET_READER_TYPE:
                    try {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_READER_TYPE, device.getReaderType(), 0));
                    } catch(Exception e) {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, 0, 0, e.getMessage()));
                    }
                    break;

                case Consts.TASK_GET_CARD_ID:
                    try {
                        data = device.getCardIdEx(c_params);
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_CARD_ID, c_params.getSak(), c_params.getUidSize(), data));
                    } catch(Exception e) {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, 0, 0, e.getMessage()));
                    }
                    break;

                case Consts.TASK_BLOCK_READ:
                    try {
                        data = device.blockRead(dev_con.getBlockAddr(), Consts.DEFAULT_AUTH_MODE, default_key);
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_BLOCK_READ, 0, 0, data));
                    } catch(Exception e) {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, 0, 0, e.getMessage()));
                    }
                    break;

                case Consts.TASK_DISCONNECT:
                    try {
                        device.close();
                        dev_con.disconnected();
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_DISCONNECTED));
                    } catch (Exception e) {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, 0, 0, e.getMessage()));
                    }
                    break;

                default:
                    break;
            }
        }
    }

    static class DeviceConnectionSynchronizer {
        private static DeviceConnectionSynchronizer dcs = null;
        private byte block_addr;
        private boolean is_connected = false;
        private boolean connecting_in_progress = false;

        static synchronized DeviceConnectionSynchronizer getInstance() {
            if (dcs == null) {
                dcs = new DeviceConnectionSynchronizer();
            }
            return dcs;
        }

        synchronized void connected() {
            connecting_in_progress = false;
            is_connected = true;
        }

        synchronized void disconnected() {
            is_connected = false;
        }

        synchronized boolean isConnected () {
            return is_connected;
        }

        synchronized void setBlockAddr(byte b) {
            block_addr = b;
        }

        synchronized byte getBlockAddr() {
            return block_addr;
        }

        synchronized void beginConnection() {
            connecting_in_progress = true;
        }

        synchronized boolean isConnectingInProgress() {
            return connecting_in_progress;
        }
    }

    static class Tools {

        public static boolean isNumeric(String s){
            if(TextUtils.isEmpty(s)){
                return false;
            }
            Pattern p = Pattern.compile("[-+]?[0-9]*");
            Matcher m = p.matcher(s);
            return m.matches();
        }

        public static String byteA2Str(byte[] a) {
            StringBuilder sb = new StringBuilder(a.length * 2);
            for(byte b: a)
                sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        }
    }
}