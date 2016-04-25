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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by d-logic on 15.5.2015..
 */

public class Main extends Activity {
    static Context context;
    static DeviceConnectionSynchronizer dev_con;
    static DlReader device;
//    static Button btnOpen;
//    static Button btnClose;
    static Button btnReaderType;
    static Button btnTagId;
    static Button btnBlockRead;
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

    @Override
    protected void onResume() {
        super.onResume();

        Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
        // Konekcija
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

    @Override
    protected void onPause() {
        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
        if (dev_con.isConnected()) {
//        if (!device.readerStillConnected()) {
            new Thread(new ReaderThread(Consts.TASK_DISCONNECT)).start();
        }
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
        dev_con = DeviceConnectionSynchronizer.getInstance();

        //device.openAccessory();

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
        ebBlockData.setInputType(0);
        ebKey = (EditText) findViewById(R.id.ebKey);

//        btnOpen = (Button) findViewById(R.id.btnOpen);
//        btnClose = (Button) findViewById(R.id.btnClose);
        btnReaderType = (Button) findViewById(R.id.btnDeviceType);
        btnTagId = (Button) findViewById(R.id.btnTagId);
        btnBlockRead = (Button) findViewById(R.id.btnBlockRead);
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
                if (dev_con.isConnected()) {
                    try {
                        new Thread(new ReaderThread(Consts.TASK_GET_READER_TYPE)).start();
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
                if (dev_con.isConnected()) {
                    try {
                        new Thread(new ReaderThread(Consts.TASK_GET_CARD_ID)).start();
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
                if (dev_con.isConnected()) {
                    if (Tools.isNumeric(ebBlockAddr.getText().toString())) {
                        int i = Integer.parseInt(ebBlockAddr.getText().toString());
                        if ((i >= 0) && (i < Consts.MAX_BLOCK_ADDR)) {
                            dev_con.setBlockAddr((byte) i);
                            if (dev_con.setKey(ebKey.getText().toString())) {
                                try {
                                    new Thread(new ReaderThread(Consts.TASK_BLOCK_READ)).start();
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
                if (dev_con.isConnected()) {
                    new Thread(new ReaderThread(Consts.TASK_EMIT_UI_SIGNAL)).start();
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnEnterSleep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {
                    new Thread(new ReaderThread(Consts.TASK_ENTER_SLEEP)).start();
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnLeaveSleep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {
                    new Thread(new ReaderThread(Consts.TASK_LEAVE_SLEEP)).start();
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
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
                    ebTagUid.setText(Tools.byteArr2Str((byte[]) msg.obj));
                    Toast.makeText(context, "Card Id obtained successfully.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_BLOCK_READ:
                    ebBlockData.setText(Tools.byteArr2Str((byte[]) msg.obj));
                    Toast.makeText(context, "Block successfully read.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_DISCONNECTED:
                    ebBlockAddr.setText("0");
                    ebDeviceType.setText("");
                    ebTagId.setText("");
                    ebTagUid.setText("");
                    ebBlockData.setText("");
                    ebKey.setText("FFFFFFFFFFFF");
                    dev_con.makeKeyDefault();

                    Toast.makeText(context, "Device successfully disconnected.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_ERROR:
                    Toast.makeText(context, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    if (dev_con.isConnectingInProgress()) {
                        dev_con.abortConnection();
                    }
                    break;

                case Consts.RESPONSE_ERROR_QUIETLY:
                    if (dev_con.isConnectingInProgress()) {
                        dev_con.abortConnection();
                    }
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

        public static final int RESPONSE_CONNECTED = 100;
        public static final int RESPONSE_READER_TYPE = 101;
        public static final int RESPONSE_CARD_ID = 102;
        public static final int RESPONSE_BLOCK_READ = 103;
        public static final int RESPONSE_DISCONNECTED = 104;

        public static final int RESPONSE_ERROR = 400;
        public static final int RESPONSE_ERROR_QUIETLY = 401;

        public static final int MAX_BLOCK_ADDR = 255;
        public static final byte DEFAULT_AUTH_MODE = DlReader.Consts.MIFARE_AUTHENT1A;
    }

    class ReaderThread implements Runnable {
        private int task;
        private byte[]data;
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
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR/* DEBUG _QUIETLY*/, e.getMessage()));
                    }
                    break;

                case Consts.TASK_GET_READER_TYPE:
                    try {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_READER_TYPE, device.getReaderType(), 0));
                    } catch(Exception e) {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                    }
                    break;

                case Consts.TASK_GET_CARD_ID:
                    try {
                        data = device.getCardIdEx(c_params);
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_CARD_ID, c_params.getSak(), c_params.getUidSize(), data));
                    } catch(Exception e) {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                    }
                    break;

                case Consts.TASK_BLOCK_READ:
                    try {
                        data = device.blockRead(dev_con.getBlockAddr(), (byte) authenticationMode, dev_con.getKey());
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_BLOCK_READ, data));
                    } catch(Exception e) {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                    }
                    break;

                case Consts.TASK_DISCONNECT:
                    try {
                        device.close();
                        dev_con.disconnected();
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_DISCONNECTED));
                    } catch (Exception e) {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                    }
                    break;

                case Consts.TASK_EMIT_UI_SIGNAL:
                    try {
                        device.readerUiSignal((byte) lightMode, (byte) beepMode);
                    } catch (Exception e) {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                    }
                    break;

                case Consts.TASK_ENTER_SLEEP:
                    try {
                        device.enterSleepMode();
                    } catch (Exception e) {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                    }
                    break;

                case Consts.TASK_LEAVE_SLEEP:
                    try {
                        device.leaveSleepMode();
                    } catch (Exception e) {
                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
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
        private boolean disconnecting_in_progress = false;
        static final byte[] default_key = new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        static byte[] key = new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};

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

        synchronized void beginDisconnection() {
            disconnecting_in_progress = true;
        }

        synchronized boolean isConnectingInProgress() {
            return connecting_in_progress;
        }

        synchronized boolean isDisconnectingInProgress() {
            return disconnecting_in_progress;
        }

        synchronized boolean isTransitionInProgress() {
            return connecting_in_progress && disconnecting_in_progress;
        }

        synchronized void abortConnection() {
            connecting_in_progress = false;
        }

        synchronized void makeKeyDefault() {
            java.lang.System.arraycopy(default_key, 0, key, 0, 6);
        }

        synchronized boolean setKey(String keyHexStr) {

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

        synchronized byte[] getKey() {
            return key;
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