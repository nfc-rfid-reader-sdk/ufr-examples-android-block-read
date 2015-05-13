package net.dlogic.android.uFR;

/**
 * Created by zborac on 12.5.2015..
 */

import java.io.IOException;
import android.content.Context;
import com.ftdi.j2xx.D2xxManager;

public class DlReader {
    private static DlReader reader = null;
    private static Context parentContext;
    public static D2xxManager ftD2xx = null;

    public static int current_index = -1;
    public static int open_index = -1;

    private DlReader() {
    }

    public static synchronized DlReader getInstance(Context context) {
        if (reader == null) {
            reader = new DlReader();
        }

        try {
            ftD2xx = D2xxManager.getInstance(context);
        } catch (D2xxManager.D2xxException ex) {
            ex.printStackTrace();
        }

        parentContext = context;
        return reader;
    }

    public synchronized void open() throws DlReaderException {
        int dev_cnt;
        D2xxManager.FtDeviceInfoListNode dev_infolist;
        String[] dl_descriptors = parentContext.getResources().getStringArray(R.array.dl_descriptors);

        // try to close first
        close();

        dev_cnt = ftD2xx.createDeviceInfoList(parentContext);

        for (int outer_cnt) {
            dev_infolist = getDeviceInfoListDetail(int index);
            for (int inner_cnt = 0; inner_cnt < dl_descriptors.length; inner_cnt++) {
                if ()
            }
        }
    }

    public synchronized void getCardId() throws DlReaderException {

    }

    public synchronized void getCardIdEx() throws DlReaderException {

    }

    public synchronized void blockRead() throws DlReaderException {

    }

    public synchronized void close() throws DlReaderException {

    }

    public static class DlReaderException extends IOException {
        private static final long serialVersionUID = 0L;

        public DlReaderException() {
        }

        public DlReaderException(String ftStatusMsg) {
            super(ftStatusMsg);
        }
    }
}
