package net.dlogic.android.uFR;

/**
 * Created by zborac on 12.5.2015..
 */

import java.io.IOException;
import android.content.Context;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

public class DlReader {
    private static DlReader reader = null;
    private static Context parentContext;
    public static D2xxManager ftD2xx = null;
    public static FT_Device ft_device = null;
    public static int open_index = -1;

    private DlReader() {
    }

    public static synchronized DlReader getInstance(Context context) throws DlReader.DlReaderException {
        if (reader == null) {
            reader = new DlReader();
        }

        try {
            ftD2xx = D2xxManager.getInstance(context);
        } catch (D2xxManager.D2xxException ex) {
            throw new DlReader.DlReaderException("Can't open driver manager.");
        }

        parentContext = context;
        return reader;
    }

    private static class ComParams {
        public static final byte LATENCY_TIMER = 2;
        public static final int BAUD_RATE = 1000000;
        public static final long READ_TIMEOUT = 1500;
        public static final long WRITE_TIMEOUT = 1000;
        public static final byte BIT_MODE = D2xxManager.FT_BITMODE_RESET;
        public static final byte DATA_BITS = D2xxManager.FT_DATA_BITS_8;
        public static final short FLOW_CONTROL = D2xxManager.FT_FLOW_NONE;
        public static final byte PARITY = D2xxManager.FT_PARITY_NONE;
        public static final byte STOP_BITS = D2xxManager.FT_STOP_BITS_1;
    }

    public synchronized void open() throws DlReaderException {
        int dev_cnt;
        D2xxManager.FtDeviceInfoListNode dev_infolist;
        String[] dl_descriptors = parentContext.getResources().getStringArray(R.array.dl_descriptors);

        // try to close first
        close();

        dev_cnt = ftD2xx.createDeviceInfoList(parentContext);

        for (int outer_cnt = 0; outer_cnt < dev_cnt; dev_cnt++) {

            dev_infolist = ftD2xx.getDeviceInfoListDetail(outer_cnt);
            for (int inner_cnt = 0; inner_cnt < dl_descriptors.length; inner_cnt++) {

                if (dev_infolist.description.equals(dl_descriptors[inner_cnt])) {

                    ft_device = ftD2xx.openByIndex(parentContext, inner_cnt);
                    if ((ft_device != null) && ft_device.isOpen()) {

                        open_index = outer_cnt;

                        try {
                            if (!ft_device.setLatencyTimer(ComParams.LATENCY_TIMER)) {
                                throw new LocalException();
                            }
                            if (!ft_device.setBitMode((byte) 0, ComParams.BIT_MODE)) {
                                throw new LocalException();
                            }
                            if (!ft_device.setBaudRate(ComParams.BAUD_RATE)) {
                                throw new LocalException();
                            }
                            if (!ft_device.setDataCharacteristics(ComParams.DATA_BITS, ComParams.STOP_BITS, ComParams.PARITY)) {
                                throw new LocalException();
                            }
                            if (!ft_device.setFlowControl(ComParams.FLOW_CONTROL, (byte) 0, (byte) 0)) {
                                throw new LocalException();
                            }
                            if (!ft_device.resetDevice()) {
                                throw new LocalException();
                            }
                        } catch (LocalException ex) {
                            ft_device.close();
                            throw new DlReaderException("Device closed due to a device setting failure.");
                        }
                        return;
                    }
                    else {
                        throw new DlReader.DlReaderException("Can't open device.");
                    }
                }
            }
        }
        throw new DlReader.DlReaderException("There is no D-Logic devices attached.");
    }

    public synchronized void readerReset() throws DlReaderException, InterruptedException {

        try {
            if (!ft_device.setRts()) {
                throw new LocalException();
            }
            Thread.sleep(100);
            if (!ft_device.clrRts()) {
                throw new LocalException();
            }
        } catch(LocalException ex){
            ft_device.close();
            throw new DlReaderException("Can't reset device.");
        }

        Thread.sleep(1100); // ReaderReset with bootloader
        // TODO: wait for BOOTLOADER character !
        // or 1100 ms
    }

    public synchronized int getReaderType() throws DlReaderException {

        return 0;
    }

    public synchronized void getCardId() throws DlReaderException {

    }

    public synchronized void getCardIdEx() throws DlReaderException {

    }

    public synchronized void blockRead() throws DlReaderException {

    }

    public synchronized void close() throws DlReaderException {

    }

    private static class ComProtocol {

        public void ErasePort() {

            ft_device.purge((byte)(D2xxManager.FT_PURGE_RX | D2xxManager.FT_PURGE_TX));
        }

        public void portWrite(void *buffer, uint32_t buffer_size)
        {
            uint32_t bytes_written;
            FT_STATUS ft_status;

            if (NULL == hndUFR)
                return UFR_DEVICE_WRONG_HANDLE;

            ft_status = FT_Write(hndUFR->ftHandle, buffer, buffer_size,
                    (LPDWORD) &bytes_written);
            if (ft_status != FT_OK)
                return FT_STATUS_PREFIX | ft_status;
            if (bytes_written != buffer_size)
                return UFR_COMMUNICATION_BREAK;
            return UFR_OK;
        }

        public void portWriteResultBytesRet(void *buffer, uint32_t buffer_size, uint32_t *bytes_written)
        {
            FT_STATUS ft_status;

            if (NULL == hndUFR)
                return UFR_DEVICE_WRONG_HANDLE;

            ft_status = FT_Write(buffer, buffer_size,
                    (LPDWORD) bytes_written);
            if (ft_status != FT_OK)
                return FT_STATUS_PREFIX | ft_status;
            if (*bytes_written != buffer_size)
            return UFR_COMMUNICATION_BREAK;
            return UFR_OK;
        }

        public void portRead(void *buffer, uint32_t buffer_size)
        {
            uint32_t bytes_returned;
            FT_STATUS ft_status;

            if (NULL == hndUFR)
                return UFR_DEVICE_WRONG_HANDLE;

            memset(buffer, 0, buffer_size);

            ft_status = FT_Read(buffer, buffer_size,
                    (LPDWORD) &bytes_returned);

            if (ft_status != FT_OK)
                return FT_STATUS_PREFIX | ft_status;
            if (bytes_returned != buffer_size)
                return UFR_COMMUNICATION_BREAK;
            return UFR_OK;
        }

        public void portReadResultBytesRet(void *buffer,
                                          uint32_t buffer_size, uint32_t *bytes_returned)
        {
            FT_STATUS ft_status;

            if (NULL == hndUFR)
                return UFR_DEVICE_WRONG_HANDLE;

            ft_status = FT_Read(buffer, buffer_size,
                    (LPDWORD) bytes_returned);
            if (ft_status != FT_OK)
                return FT_STATUS_PREFIX | ft_status;
            if (*bytes_returned != buffer_size)
            return UFR_COMMUNICATION_BREAK;
            return UFR_OK;
        }

        public byte getChecksum_local(uint8_t *buffer, uint8_t length)
        { // Ukoliko se ne bude koristila, spojiti sa CalcChecksum()
            uint16_t i;
            uint8_t sum = buffer[0];

            for (i = 1; i < (length - 1); i++)
            {
                sum ^= buffer[i];
            }
            return sum + CHECKSUM_CONST;
        }


        public byte getChecksumFragment(uint8_t previous_checksum, uint8_t *buffer,
                                    uint8_t length)
        { // !without +7 at the end
            uint16_t i;

            for (i = 0; i < length; i++)
            {
                previous_checksum ^= buffer[i];
            }
            return previous_checksum;
        }

        public void calcChecksum(uint8_t *buffer, uint8_t length)
        {

            buffer[length - 1] = GetChecksum_local(buffer, length);
        }

        public boolean testChecksum(uint8_t *buffer, uint8_t length)
        {
            uint16_t i;
            uint8_t sum = buffer[0];

            for (i = 1; i < (length - 1); i++)
            {
                sum ^= buffer[i];
            }
            sum += CHECKSUM_CONST;
            return sum == buffer[length - 1];
        }

        public boolean testAuthMode(uint8_t auth_mode)
        {

            if ((auth_mode != MIFARE_AUTHENT1A) && (auth_mode != MIFARE_AUTHENT1B))
                return FALSE;
            return TRUE;
        }

        public void initialHandshaking(uint8_t *data,
                                      uint8_t *bytes_to_read)
        {
            // length = INTRO_SIZE, data[INTRO_SIZE] = checksum
            uint8_t command = data[1];
            UFR_STATUS status;

            if (NULL == hndUFR)
                return UFR_DEVICE_WRONG_HANDLE;

            ErasePort(hndUFR);
            Sleep(10);
            CalcChecksum(data, INTRO_SIZE);
            if ((status = PortWrite(hndUFR, data, INTRO_SIZE)) != UFR_OK)
                return status;

            if ((status = PortRead(hndUFR, data, INTRO_SIZE)) != UFR_OK)
                return status;
            if (!TestChecksum(data, INTRO_SIZE))
                return UFR_COMMUNICATION_ERROR;
            if ((data[0] == ERR_HEADER) && (data[2] == ERR_TRAILER))
                return data[1];
            if ((data[1] != command)
                    || (((data[0] != RESPONSE_HEADER) || (data[2] != RESPONSE_TRAILER))
                    && ((data[0] != ACK_HEADER) || (data[2] != ACK_TRAILER))))
                return UFR_COMMUNICATION_ERROR;

            *bytes_to_read = data[3];
            return UFR_OK;
        }

        public void getAndTestResponse(uint8_t *cmd_intro, uint8_t command)
        {
            UFR_STATUS status;

            if (NULL == hndUFR)
                return UFR_DEVICE_WRONG_HANDLE;

            if ((status = PortRead(hndUFR, cmd_intro, INTRO_SIZE)) != UFR_OK)
                return status;
            if (!TestChecksum(cmd_intro, INTRO_SIZE))
                return UFR_COMMUNICATION_ERROR;
            if ((cmd_intro[0] == ERR_HEADER) || (cmd_intro[2] == ERR_TRAILER))
                return cmd_intro[1];
            if ((cmd_intro[0] != RESPONSE_HEADER) || (cmd_intro[2] != RESPONSE_TRAILER)
                    || (cmd_intro[1] != command))
                return UFR_COMMUNICATION_ERROR;

            return UFR_OK;
        }

    }

    public static class DlReaderException extends IOException {
        private static final long serialVersionUID = 1L;

        public DlReaderException() {
        }

        public DlReaderException(String ftStatusMsg) {
            super(ftStatusMsg);
        }
    }

    private static class LocalException extends IOException {
        private static final long serialVersionUID = 1L;

        public LocalException() {
        }

        public LocalException(String ftStatusMsg) {
            super(ftStatusMsg);
        }
    }
}
