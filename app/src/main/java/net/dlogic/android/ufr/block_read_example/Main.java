package net.dlogic.android.ufr.block_read_example;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.dlogic.android.ufr.DlReader;

/**
 * Created by zborac on 15.5.2015..
 */

public class Main extends Activity {
    static Context context;
    DeviceConnectionSynchronizer dev_con;
    DlReader device;
    Button btnOpen;
    Button btnReaderType;
    Button btnTagId;
    Button btnBlockRead;
    Button btnClose;
    EditText ebBlockAddr;
    EditText ebDeviceType;
    EditText ebTagId;
    EditText ebTagUid;
    EditText ebBlockData;

    void connect() {
        new Thread(new ReaderThread()).start();
    }

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

        btnOpen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {
                    Toast.makeText(context, "Device already connected...", Toast.LENGTH_LONG).show();
                }
                else {
                    new Thread(new ReaderThread(Consts.TASK_CONNECT)).start();
                }
            }
        });
        btnReaderType.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {
                    try {
                        ebDeviceType.setText(Integer.toHexString(device.getReaderType()));
                    } catch (Exception e) {
                        Log.i("zborac:", e.toString());
                    }
                }
                else {
                    Toast.makeText(context, "Device not connected...", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnTagId.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {

                }
                else {
                    Toast.makeText(context, "Device not connected...", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnBlockRead.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {

                }
                else {
                    Toast.makeText(context, "Device not connected...", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dev_con.isConnected()) {
                    new Thread(new ReaderThread(Consts.TASK_DISCONNECT)).start();
                } else {
                    Toast.makeText(context, "Device not connected...", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class Consts {
        public static final int TASK_NONE = 0;
        public static final int TASK_CONNECT = 1;
        public static final int TASK_DISCONNECT = 2;
    }

    static class DeviceConnectionSynchronizer {
        private static DeviceConnectionSynchronizer dcs = null;
        private boolean is_connected;

        static synchronized DeviceConnectionSynchronizer getInstance() {
            if (dcs == null) {
                dcs = new DeviceConnectionSynchronizer();
            }
            return dcs;
        }

        void DeviceConnectionSynchronizer() {

            is_connected = false;
        }

        synchronized void connected() {

            is_connected = true;
        }

        synchronized void disconnected() {

            is_connected = false;
        }

        synchronized boolean isConnected () {

            return is_connected;
        }
    }

    class ReaderThread implements Runnable {
        private int task;

        public ReaderThread() {
            task = Consts.TASK_NONE;
        }
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
                    } catch (Exception e) {
                        Log.i("zborac:", e.toString());
                    }
                    break;

                case Consts.TASK_DISCONNECT:
                    try {
                        device.close();
                        dev_con.disconnected();
                    } catch (Exception e) {
                        Log.i("zborac:", e.toString());
                    }
                    break;

                default:
                    break;
            }
        }
    }

    static class Tools {

        public static String byteA2Str(byte[] a) {
            StringBuilder sb = new StringBuilder(a.length * 2);
            for(byte b: a)
                sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        }
    }
}