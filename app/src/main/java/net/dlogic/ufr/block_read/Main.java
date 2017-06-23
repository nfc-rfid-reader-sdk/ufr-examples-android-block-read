package net.dlogic.ufr.block_read;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import net.dlogic.ufr.lib.DlReader;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by d-logic on 15.5.2015..
 */

public class Main extends Activity {
    static Context context;
    static DlReader device;
//    static Button btnOpen;
//    static Button btnClose;
    static Button btnReaderType;
    static Button btnTagId;
    static Button btnBlockRead;
    static Button btnBlockWrite;
    static Button btnUiSignal;
    static Button btnEnterSleep;
    static Button btnLeaveSleep;
    static EditText ebBlockAddr;
    static EditText ebDeviceType;
    static EditText ebTagId;
    static EditText ebTagUid;
    static EditText ebBlockData;
    static EditText ebKey;
    static Spinner spnLightMode;
    static Spinner spnBeepMode;
    static Spinner spnAuthenticationMode;
    static int lightMode = 0;
    static int beepMode = 0;
    static int authenticationMode = DlReader.Consts.MIFARE_AUTHENT1A;
    static IncomingHandler handler = new IncomingHandler();
    static Resources res;
    static int[] authModes;
    private byte block_addr;
    static final byte[] default_key = new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
    static byte[] key = new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
    static byte[] mDataForWrite = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static ConcurrentLinkedQueue<Task> mCommandQueue = new ConcurrentLinkedQueue<Task>();
    ReaderThread mReaderThread;

    @Override
    protected void onResume() {
        super.onResume();
        mCommandQueue.add(new Task(Consts.TASK_CONNECT));
    }

    @Override
    protected void onPause() {
        mCommandQueue.add(new Task(Consts.TASK_DISCONNECT));
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = this;
        try {
            device = DlReader.getInstance(context, R.xml.accessory_filter, R.xml.dev_desc_filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mReaderThread = new ReaderThread();
        mReaderThread.start();

        // Get arrays from resources:
        res = getResources();
        authModes = res.getIntArray(R.array.authentication_mode_values);

        // Get references to UI widgets:
        ebBlockAddr = (EditText) findViewById(R.id.ebBlockAddr);
        ebDeviceType = (EditText) findViewById(R.id.ebDeviceType);
        ebDeviceType.setInputType(0);
        ebTagId = (EditText) findViewById(R.id.ebTagId);
        ebTagId.setInputType(0);
        ebTagUid = (EditText) findViewById(R.id.ebTagUid);
        ebTagUid.setInputType(0);
        ebBlockData = (EditText) findViewById(R.id.ebBlockData);
        ebKey = (EditText) findViewById(R.id.ebKey);

//        btnOpen = (Button) findViewById(R.id.btnOpen);
//        btnClose = (Button) findViewById(R.id.btnClose);
        btnReaderType = (Button) findViewById(R.id.btnDeviceType);
        btnTagId = (Button) findViewById(R.id.btnTagId);
        btnBlockRead = (Button) findViewById(R.id.btnBlockRead);
        btnBlockWrite = (Button) findViewById(R.id.btnBlockWrite);
        btnUiSignal = (Button) findViewById(R.id.btnUiSignal);
        btnEnterSleep = (Button) findViewById(R.id.btnEnterSleep);
        btnLeaveSleep = (Button) findViewById(R.id.btnLeaveSleep);

        spnLightMode = (Spinner) findViewById(R.id.spnLightMode);
        ArrayAdapter<CharSequence> spnLightAdapter = ArrayAdapter.createFromResource(context,
                R.array.light_signal_modes,
                R.layout.dl_spinner_textview);
        spnLightAdapter.setDropDownViewResource(R.layout.dl_spinner_textview);
        spnLightMode.setAdapter(spnLightAdapter);
        spnLightMode.setSelection(0);
        spnLightMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                lightMode = pos;
            }

            public void onNothingSelected(AdapterView<?> parent) { }
        });

        spnBeepMode = (Spinner) findViewById(R.id.spnBeepMode);
        ArrayAdapter<CharSequence> spnBeepAdapter = ArrayAdapter.createFromResource(context,
                R.array.beep_signal_modes,
                R.layout.dl_spinner_textview);
        spnBeepAdapter.setDropDownViewResource(R.layout.dl_spinner_textview);
        spnBeepMode.setAdapter(spnBeepAdapter);
        spnBeepMode.setSelection(0);
        spnBeepMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                beepMode = pos;
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spnAuthenticationMode = (Spinner) findViewById(R.id.spnAuthenticationMode);
        ArrayAdapter<CharSequence> spnAuthenticationAdapter = ArrayAdapter.createFromResource(context,
                R.array.authentication_mode_names,
                R.layout.dl_spinner_textview);
        spnAuthenticationAdapter.setDropDownViewResource(R.layout.dl_spinner_textview);
        spnAuthenticationMode.setAdapter(spnAuthenticationAdapter);
        spnAuthenticationMode.setSelection(0);
        spnAuthenticationMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                authenticationMode = Main.authModes[pos];
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
/*        btnOpen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {
                    Toast.makeText(context, "Device already connected.", Toast.LENGTH_SHORT).show();
                } else {
                    if (!dev_con.isConnectingInProgress()) {
                        new Thread(new ReaderThread(Consts.TASK_CONNECT)).start();
                        dev_con.beginConnection();
                    } else {
                        Toast.makeText(context, "Connecting in progress.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });*/
        btnReaderType.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (device.readerStillConnected()) {
                    try {
                        mCommandQueue.add(new Task(Consts.TASK_GET_READER_TYPE));
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnTagId.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (device.readerStillConnected()) {
                    try {
                        mCommandQueue.add(new Task(Consts.TASK_GET_CARD_ID));
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnBlockRead.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (device.readerStillConnected()) {
                    if (Tools.isNumeric(ebBlockAddr.getText().toString())) {
                        int i = Integer.parseInt(ebBlockAddr.getText().toString());
                        if ((i >= 0) && (i < Consts.MAX_BLOCK_ADDR)) {
                            block_addr = (byte) i;
                            if (setKey(ebKey.getText().toString())) {
                                try {
                                    mCommandQueue.add(new Task(Consts.TASK_BLOCK_READ, block_addr, (byte)authenticationMode, getKey()));
                                } catch (Exception e) {
                                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "Wrong key format. Key must be HEX string 6 bytes long.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(context, "Wrong block address.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Block address must be a number.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnBlockWrite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (device.readerStillConnected()) {
                    if (Tools.isNumeric(ebBlockAddr.getText().toString())) {
                        int i = Integer.parseInt(ebBlockAddr.getText().toString());
                        if ((i >= 0) && (i < Consts.MAX_BLOCK_ADDR)) {
                            block_addr = (byte) i;
                            if (setKey(ebKey.getText().toString()) && setDataForWrite(ebBlockData.getText().toString())) {
                                try {
                                    mCommandQueue.add(new Task(Consts.TASK_BLOCK_WRITE, block_addr, (byte)authenticationMode, getKey(), getDataForWrite()));
                                } catch (Exception e) {
                                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "Wrong key format. Key must be HEX string 6 bytes long.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(context, "Wrong block address.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Block address must be a number.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUiSignal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (device.readerStillConnected()) {
                    mCommandQueue.add(new Task(Consts.TASK_EMIT_UI_SIGNAL, (byte)lightMode, (byte)beepMode));
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnEnterSleep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (device.readerStillConnected()) {
                    mCommandQueue.add(new Task(Consts.TASK_ENTER_SLEEP));
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnLeaveSleep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (device.readerStillConnected()) {
                    mCommandQueue.add(new Task(Consts.TASK_LEAVE_SLEEP));
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

/*    @Override
    protected void onStop() {
        mReaderThread.stopRequest();
        try {
            mReaderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mReaderThread = null;
        super.onStop();
    }
*/
    private static byte[] getKey() {
        return key;
    }

    private static byte[] getDataForWrite() {
        return mDataForWrite;
    }

    private static boolean setKey(String keyHexStr) {

        if (keyHexStr.length() != 12) {
            return false;
        }
        if (!keyHexStr.matches("[0-9A-Fa-f]+")) {
            return false;
        }
        for (int i = 0; i < 12; i += 2) {
            key[i / 2] = (byte) ((Character.digit(keyHexStr.charAt(i), 16) << 4)
                    + Character.digit(keyHexStr.charAt(i+1), 16));
        }
        return true;
    }

    private static boolean setDataForWrite(String dataHexStr) {

        if (dataHexStr.length() != 32) {
            return false;
        }
        if (!dataHexStr.matches("[0-9A-Fa-f]+")) {
            return false;
        }
        for (int i = 0; i < 32; i += 2) {
            mDataForWrite[i / 2] = (byte) ((Character.digit(dataHexStr.charAt(i), 16) << 4)
                    + Character.digit(dataHexStr.charAt(i+1), 16));
        }
        return true;
    }

    private static void makeKeyDefault() {
        java.lang.System.arraycopy(default_key, 0, key, 0, 6);
    }

    static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Consts.RESPONSE_CONNECTED:
                    //Toast.makeText(context, "Device successfully connected.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_READER_TYPE:
                    ebDeviceType.setText(Integer.toHexString(msg.arg1));
                    Toast.makeText(context, "Device type obtained successfully.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_CARD_ID:
                    ebTagId.setText(Integer.toHexString(msg.arg1));
                    ebTagUid.setText(Tools.byteArr2Str((byte[]) msg.obj));
                    Toast.makeText(context, "Card Id obtained successfully.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_BLOCK_READ:
                    ebBlockData.setText(Tools.byteArr2Str((byte[]) msg.obj));
                    Toast.makeText(context, "Block successfully read.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_SUCESS:
                    Toast.makeText(context, "Operation completed successfully.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_DISCONNECTED:
                    ebBlockAddr.setText("0");
                    ebDeviceType.setText("");
                    ebTagId.setText("");
                    ebTagUid.setText("");
                    ebBlockData.setText("");
                    ebKey.setText("FFFFFFFFFFFF");
                    makeKeyDefault();

                    //Toast.makeText(context, "Device successfully disconnected.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_ERROR:
                    Toast.makeText(context, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_ERROR_QUIETLY:
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
        public static final int TASK_EMIT_UI_SIGNAL = 6;
        public static final int TASK_ENTER_SLEEP = 7;
        public static final int TASK_LEAVE_SLEEP = 8;
        public static final int TASK_BLOCK_WRITE = 9;

        public static final int RESPONSE_CONNECTED = 100;
        public static final int RESPONSE_READER_TYPE = 101;
        public static final int RESPONSE_CARD_ID = 102;
        public static final int RESPONSE_BLOCK_READ = 103;
        public static final int RESPONSE_DISCONNECTED = 104;
        public static final int RESPONSE_SUCESS = 105;

        public static final int RESPONSE_ERROR = 400;
        public static final int RESPONSE_ERROR_QUIETLY = 401;

        public static final int MAX_BLOCK_ADDR = 255;
        public static final byte DEFAULT_AUTH_MODE = DlReader.Consts.MIFARE_AUTHENT1A;
    }

    class ReaderThread extends Thread {
        private byte[]data;
        private boolean stop_thread = false;
        private boolean connected = false;
        DlReader.CardParams c_params = new DlReader.CardParams();

        public synchronized void stopRequest() {

            stop_thread = true;
        }

        @Override
        public void run() {
            Task local_task;

            while (!stop_thread) {
                local_task = mCommandQueue.poll();
                if (local_task != null) {

                    switch (local_task.taskCode) {
                        case Consts.TASK_CONNECT:
                            Task peek_task = mCommandQueue.peek();
                            boolean next_task_is_not_disconnect = peek_task == null;
                            if (!next_task_is_not_disconnect) {
                                next_task_is_not_disconnect = peek_task.taskCode != Consts.TASK_DISCONNECT;
                            }
                            while (!device.readerStillConnected() && next_task_is_not_disconnect) {
                                try {
                                    device.open();
                                    connected = true;
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_CONNECTED));
                                } catch (Exception e) {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR_QUIETLY, e.getMessage()));
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException ie) {
                                        ie.printStackTrace();
                                    }
                                }
                            }
                            break;

                        case Consts.TASK_DISCONNECT:
                            try {
                                device.close();
                                connected = false;
                                handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_DISCONNECTED));
                            } catch (Exception e) {
                                handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                            }
                            break;

                        case Consts.TASK_GET_READER_TYPE:
                            if (connected) {
                                try {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_READER_TYPE, device.getReaderType(), 0));
                                } catch (Exception e) {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                                }
                            }
                            break;

                        case Consts.TASK_GET_CARD_ID:
                            if (connected) {
                                try {
                                    data = device.getCardIdEx(c_params);
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_CARD_ID, c_params.getSak(), c_params.getUidSize(), data));
                                } catch (Exception e) {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                                }
                            }
                            break;

                        case Consts.TASK_BLOCK_READ:
                            if (connected) {
                                try {
                                    data = device.blockRead(local_task.byte_param1/*block_addr*/, local_task.byte_param2/*authenticationMode*/, local_task.byte_arr_param1/*getKey()*/);
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_BLOCK_READ, data));
                                } catch (Exception e) {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                                }
                            }
                            break;

                        case Consts.TASK_BLOCK_WRITE:
                            if (connected) {
                                try {
                                    device.blockWrite(local_task.byte_arr_param2/*getData()*/, local_task.byte_param1/*block_addr*/,
                                            local_task.byte_param2/*authenticationMode*/, local_task.byte_arr_param1/*getKey()*/);
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_SUCESS));
                                } catch (Exception e) {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                                }
                            }
                            break;

                        case Consts.TASK_EMIT_UI_SIGNAL:
                            if (connected) {
                                try {
                                    device.readerUiSignal(local_task.byte_param1/*lightMode*/, local_task.byte_param2/*beepMode*/);
                                } catch (Exception e) {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                                }
                            }
                            break;

                        case Consts.TASK_ENTER_SLEEP:
                            if (connected) {
                                try {
                                    device.enterSleepMode();
                                } catch (Exception e) {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                                }
                            }
                            break;

                        case Consts.TASK_LEAVE_SLEEP:
                            if (connected) {
                                try {
                                    device.leaveSleepMode();
                                } catch (Exception e) {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                                }
                            }
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }

    class Task {
        int taskCode;
        byte byte_param1, byte_param2;
        byte[] byte_arr_param1;
        byte[] byte_arr_param2;

        public Task(int code) {
            taskCode = code;
        }
        public Task(int code, byte p1, byte p2) {
            taskCode = code;
            byte_param1 = p1;
            byte_param2 = p2;
        }
        public Task(int code, byte p1, byte p2, byte[] pa1) {
            taskCode = code;
            byte_param1 = p1;
            byte_param2 = p2;
            byte_arr_param1 = pa1;
        }
        public Task(int code, byte p1, byte p2, byte[] pa1, byte[] pa2) {
            taskCode = code;
            byte_param1 = p1;
            byte_param2 = p2;
            byte_arr_param1 = pa1;
            byte_arr_param2 = pa2;
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

        public static String byteArr2Str(byte[] byteArray) {
            StringBuilder sBuilder = new StringBuilder(byteArray.length * 2);
            for(byte b: byteArray)
                sBuilder.append(String.format("%02x", b & 0xff));
            return sBuilder.toString();
        }
    }
}