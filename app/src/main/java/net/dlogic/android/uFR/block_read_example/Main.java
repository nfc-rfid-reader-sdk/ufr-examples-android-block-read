package net.dlogic.android.uFR.block_read_example;

import android.app.Activity;

/**
 * Created by zborac on 15.5.2015..
 */

public class Main extends Activity {

    void connect() {
        new Thread(new ReaderConnect()).start();
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
}