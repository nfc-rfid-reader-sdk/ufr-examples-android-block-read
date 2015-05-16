package net.dlogic.android.ufr.block_read_example;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by zborac on 15.5.2015..
 */

public class Main extends Activity {
    static Context context;
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

    public Main() {

        context = this;
    }

    void connect() {
        new Thread(new ReaderThread()).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setContentView(R.layout.main);
        View view = inflater.inflate(R.layout.main, container, false);

        ebBlockAddr = (EditText) view.findViewById(R.id.ebBlockAddr);
        ebDeviceType = (EditText) view.findViewById(R.id.ebDeviceType);
        ebDeviceType.setInputType(0);
        ebTagId = (EditText) view.findViewById(R.id.ebTagId);
        ebTagId.setInputType(0);
        ebTagUid = (EditText) view.findViewById(R.id.ebTagUid);
        ebTagUid.setInputType(0);
        ebBlockData = (EditText) view.findViewById(R.id.ebBlockData);
        ebBlockData.setInputType(0);

        btnOpen = (Button) view.findViewById(R.id.btnOpen);
        btnReaderType = (Button) view.findViewById(R.id.btnDeviceType);
        btnTagId = (Button) view.findViewById(R.id.btnTagId);
        btnBlockRead = (Button) view.findViewById(R.id.btnBlockRead);
        btnClose = (Button) view.findViewById(R.id.btnClose);

        btnOpen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    Toast.makeText(context, "Device not open yet...", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private class Consts {
        public static final int TASK_NONE = 0;
        public static final int TASK_CONNECT = 1;
        public static final int TASK_DISCONNECT = 2;
        public static final int TASK_READ_BLOCK = 3;
    }

    class DeviceConnectionSynchronizer {
        private boolean is_connected;

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

        void ReaderConnect() {
            task = Consts.TASK_NONE;
        }

        void ReaderConnect(int ptask) {
            task = ptask;
        }

        @Override
        public void run() {

            switch (task) {
                case Consts.TASK_CONNECT:
                    break;
                case Consts.TASK_DISCONNECT:
                    break;
                case Consts.TASK_READ_BLOCK:
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