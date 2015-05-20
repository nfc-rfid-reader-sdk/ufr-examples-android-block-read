package net.dlogic.android.ufr;

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
            throw new DlReaderException("Can't open driver manager.");
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

    public static class Consts {

        // Lengths and ranges:
        public static final int BUFFER_LOCAL_SIZE = 256;
        public static final int BUFFER_SIZE = 192;
        public static final byte INTRO_SIZE = 7;
        public static final int EXT_SIZE_INDEX = 3;
        public static final int PARAM0_INDEX = 4;
        public static final int PARAM1_INDEX = 5;
        public static final int VAL0_INDEX = 4;
        public static final int VAL1_INDEX = 5;
        public static final int INTRO_CMD_INDEX = 1;
        public static final int CMD_EXT_LENGTH_INDEX = 3;
        public static final int CMD_PARAM0_INDEX = 4;
        public static final int CMD_EXT_PROVIDED_KEY_INDEX = 4;
        public static final int RESPONSE_EXT_LENGTH_INDEX = 3;
        public static final int RESPONSE_VAL0_INDEX = 4;

        // Protocol consts:
        public static final byte CMD_HEADER = 0x55;
        public static final byte CMD_TRAILER = (byte)0xAA;
        public static final byte ACK_HEADER = (byte)0xAC;
        public static final byte ACK_TRAILER = (byte)0xCA;
        public static final byte RESPONSE_HEADER = (byte)0xDE;
        public static final byte RESPONSE_TRAILER = (byte)0xED;
        public static final byte ERR_HEADER = (byte)0xEC;
        public static final byte ERR_TRAILER = (byte)0xCE;
        public static final byte CHECKSUM_CONST = (byte)7;

        // Auth consts:
        public static final byte RKA_AUTH1A = 0x00;     // reder keys addressing mode, authentication using key A
        public static final byte RKA_AUTH1B = 0x01;     // reder keys addressing mode, authentication using key B
        public static final byte AKM1_AUTH1A = 0x20;    // auto keys, searching mode 1, authentication using key A
        public static final byte AKM1_AUTH1B = 0x21;    // auto keys, searching mode 1, authentication using key B
        public static final byte AKM2_AUTH1A = 0x40;    // auto keys, searching mode 2, authentication using key A
        public static final byte AKM2_AUTH1B = 0x41;    // auto keys, searching mode 2, authentication using key B
        public static final byte PK_AUTH1A = 0x60;      // provided keys, authentication using key A
        public static final byte PK_AUTH1B = 0x61;      // provided keys, authentication using key B

        public static final byte MIFARE_AUTHENT1A = 0x60;
        public static final byte MIFARE_AUTHENT1B = 0x61;

        // uFR Commands:
        public static final byte GET_READER_TYPE = 0x10;
        public static final byte GET_READER_SERIAL = 0x11;
        public static final byte GET_HARDWARE_VERSION = 0x2A;
        public static final byte GET_FIRMWARE_VERSION = 0x29;
        public static final byte GET_CARD_ID = 0x13;
        public static final byte GET_CARD_ID_EX = 0x2C;
        public static final byte BLOCK_READ = 0x16;
        public static final byte SOFT_RESTART = 0x30;
        public static final byte USER_INTERFACE_SIGNAL = 0x26;

        public static final int DL_READER_GENERAL_EXCEPTION = 1000;
    }

    public synchronized void open() throws DlReaderException {
        int dev_cnt;
        D2xxManager.FtDeviceInfoListNode dev_infolist;
        String[] dl_descriptors = new String[] {"nFR RS232 CLASSIC (OEM)",
                                                "nFR RS232 CLASSIC",
                                                "nFR USB CLASSIC",
                                                "uFR ADVANCE",
                                                "uFR CLASSIC",
                                                "uFR PRO",
                                                "uFR XR CLASSICu",
                                                "uFR XRC  CLASSIC"
                                                };

        dev_cnt = ftD2xx.createDeviceInfoList(parentContext);

        for (int outer_cnt = 0; outer_cnt < dev_cnt; dev_cnt++) {

            dev_infolist = ftD2xx.getDeviceInfoListDetail(outer_cnt);
            for (int inner_cnt = 0; inner_cnt < dl_descriptors.length; inner_cnt++) {

                if (dev_infolist.description.equals(dl_descriptors[inner_cnt])) {
                    ft_device = ftD2xx.openByIndex(parentContext, outer_cnt);

                    if ((ft_device != null) && ft_device.isOpen()) {

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

                        open_index = outer_cnt;
                        return;
                    }
                    else {
                        throw new DlReaderException("Can't open device.");
                    }
                }
            }
        }
        throw new DlReaderException("There is no D-Logic devices attached.");
    }

    public synchronized void readerReset() throws DlReaderException, InterruptedException {

        if (open_index < 0) {
            throw new DlReaderException("Device not opened.");
        }

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

    public synchronized int getReaderType() throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] {Consts.CMD_HEADER, Consts.GET_READER_TYPE, Consts.CMD_TRAILER, 0, (byte)0xAA, (byte)0xCC, 0};
        byte bytes_to_read;

        if (open_index < 0) {
            throw new DlReaderException("Device not opened.");
        }

        bytes_to_read = ComProtocol.initialHandshaking(buffer);
        buffer = ComProtocol.portRead(bytes_to_read);
        if (!ComProtocol.testChecksum(buffer, bytes_to_read))
            throw new DlReaderException("UFR_COMMUNICATION_ERROR");

        return (buffer[0] & 0xFF) | (buffer[1] & 0xFF) << 8 | (buffer[2] & 0xFF) << 16 | (buffer[3] & 0xFF) << 24;
    }

    public synchronized void readerUiSignal(byte lightSignalMode, byte beepSignalMode) throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] {Consts.CMD_HEADER, Consts.USER_INTERFACE_SIGNAL, Consts.CMD_TRAILER, 0, lightSignalMode, beepSignalMode, 0};

        ComProtocol.initialHandshaking(buffer);
    }

    public synchronized byte[] getCardIdEx(CardParams c_params) throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] {Consts.CMD_HEADER, Consts.GET_CARD_ID_EX, Consts.CMD_TRAILER, 0, (byte)0xAA, (byte)0xCC, 0 };
        byte[] tmp_buff;
        byte[] result;
        byte bytes_to_read;
        byte sak, uid_size;

        if (open_index < 0) {
            throw new DlReaderException("Device not opened.");
        }

        bytes_to_read = ComProtocol.initialHandshaking(buffer);
        sak = buffer[Consts.VAL0_INDEX];
        uid_size = buffer[Consts.VAL1_INDEX];

        tmp_buff = ComProtocol.portRead(bytes_to_read);

        if (!ComProtocol.testChecksum(tmp_buff, bytes_to_read))
            throw new DlReaderException("UFR_COMMUNICATION_ERROR");
        if (uid_size > 10)
            throw new DlReaderException("UFR_BUFFER_OVERFLOW");

        c_params.setSak(sak);
        c_params.setUidSize(uid_size);
        result = java.util.Arrays.copyOf(tmp_buff, uid_size);
        return result;
    }

    public synchronized byte[] blockRead(byte block_address, byte auth_mode, byte[] key) throws DlReaderException, InterruptedException {
        byte[] cmd_intro = new byte[] { Consts.CMD_HEADER, Consts.BLOCK_READ, Consts.CMD_TRAILER, 11, (byte)0xAA, (byte)0xCC, 0 };
        byte[] cmd_ext = new byte[11];

        if (open_index < 0) {
            throw new DlReaderException("Device not opened.");
        }

        cmd_intro[4] = auth_mode;
        cmd_ext[0] = block_address;

        if (!ComProtocol.testAuthMode(auth_mode))
            throw new DlReaderException("UFR_PARAMETERS_ERROR");

        java.lang.System.arraycopy(key, 0, cmd_ext, Consts.CMD_EXT_PROVIDED_KEY_INDEX, 6);
        return ComProtocol.commonBlockRead(cmd_intro, cmd_ext, (byte)17);
    }

    public synchronized void close() throws DlReaderException {

        open_index = -1;
        ft_device.close();
    }

    private static class ComProtocol {

        public static void erasePort() {

            ft_device.purge((byte)(D2xxManager.FT_PURGE_RX | D2xxManager.FT_PURGE_TX));
        }

        public static void portWrite(byte[] buffer, int buffer_size) throws DlReaderException
        {
            if (ft_device.write(buffer, buffer_size) != buffer_size) {
                throw new DlReaderException("UFR_COMMUNICATION_BREAK");
            }
        }

        public static byte[] portRead(int buffer_size) throws DlReaderException
        {
            byte[] buffer = new byte[buffer_size];
            java.util.Arrays.fill(buffer, (byte) 0);

            if (ft_device.read(buffer, buffer_size, ComParams.READ_TIMEOUT) != buffer_size) {
                throw new DlReaderException("UFR_COMMUNICATION_BREAK");
            }

            return buffer;
        }

        public static byte getChecksum_local(byte[] buffer, byte length)
        { // Ukoliko se ne bude koristila, spojiti sa CalcChecksum()
            short i;
            byte sum = buffer[0];

            for (i = 1; i < (length - 1); i++)
            {
                sum ^= buffer[i];
            }
            return (byte)(sum + Consts.CHECKSUM_CONST);
        }


        public static byte getChecksumFragment(byte previous_checksum, byte[] buffer, byte length)
        { // !without +7 at the end
            short i;

            for (i = 0; i < length; i++)
            {
                previous_checksum ^= buffer[i];
            }
            return previous_checksum;
        }

        public static void calcChecksum(byte[] buffer, byte length)
        {

            buffer[length - 1] = getChecksum_local(buffer, length);
        }

        public static boolean testChecksum(byte[] buffer, byte length)
        {
            short i;
            byte sum = buffer[0];

            for (i = 1; i < (length - 1); i++)
            {
                sum ^= buffer[i];
            }
            sum += Consts.CHECKSUM_CONST;
            return sum == buffer[length - 1];
        }

        public static boolean testAuthMode(byte auth_mode)
        {
            if ((auth_mode != Consts.MIFARE_AUTHENT1A) && (auth_mode != Consts.MIFARE_AUTHENT1B))
                return false;

            return true;
        }

        public static byte initialHandshaking(byte[] data) throws InterruptedException, DlReaderException
        {
            byte command = data[1];
            byte[] rcv_data;

            erasePort();
            Thread.sleep(10);
            calcChecksum(data, Consts.INTRO_SIZE);
            portWrite(data, Consts.INTRO_SIZE);
            rcv_data = portRead(Consts.INTRO_SIZE);
            if (!testChecksum(rcv_data, Consts.INTRO_SIZE))
                throw new DlReaderException("UFR_COMMUNICATION_ERROR");
            if ((rcv_data[0] == Consts.ERR_HEADER) && (rcv_data[2] == Consts.ERR_TRAILER))
                throw new DlReaderException("Reader error code: " + rcv_data[1], rcv_data[1] & 0xFF);

            if ((rcv_data[1] != command)
                    || (((rcv_data[0] != Consts.RESPONSE_HEADER) || (rcv_data[2] != Consts.RESPONSE_TRAILER))
                    && ((rcv_data[0] != Consts.ACK_HEADER) || (rcv_data[2] != Consts.ACK_TRAILER))))
                throw new DlReaderException("UFR_COMMUNICATION_ERROR");

            java.lang.System.arraycopy(rcv_data, 0, data, 0, 7);
            return data[3];
        }

        public static void getAndTestResponse(byte[] cmd_intro, byte command) throws DlReaderException
        {
            java.lang.System.arraycopy(portRead(Consts.INTRO_SIZE), 0, cmd_intro, 0, Consts.INTRO_SIZE);

            if (!testChecksum(cmd_intro, Consts.INTRO_SIZE))
                throw new DlReaderException("UFR_COMMUNICATION_ERROR");
            if ((cmd_intro[0] == Consts.ERR_HEADER) || (cmd_intro[2] == Consts.ERR_TRAILER))
                throw new DlReaderException("Reader error code: " + cmd_intro[1], cmd_intro[1] & 0xFF);
            if ((cmd_intro[0] != Consts.RESPONSE_HEADER) || (cmd_intro[2] != Consts.RESPONSE_TRAILER)
                    || (cmd_intro[1] != command))
                throw new DlReaderException("UFR_COMMUNICATION_ERROR");
        }

        public static byte[] commonBlockRead(byte[] cmd_intro, byte[] cmd_ext, byte response_ext_length) throws InterruptedException, DlReaderException
        {
            byte command = cmd_intro[Consts.INTRO_CMD_INDEX];
            byte cmd_ext_length = cmd_intro[Consts.CMD_EXT_LENGTH_INDEX];
            byte bytes_to_read;
            byte[] checksum;
            byte[] data;

            bytes_to_read = initialHandshaking(cmd_intro);

            calcChecksum(cmd_ext, cmd_ext_length);
            portWrite(cmd_ext, cmd_ext_length);

            getAndTestResponse(cmd_intro, command);

            if (cmd_intro[Consts.RESPONSE_EXT_LENGTH_INDEX] != response_ext_length)
                throw new DlReaderException("UFR_COMMUNICATION_ERROR");

            data = portRead(response_ext_length - 1);
            checksum = portRead(1);
            if (checksum[0] != (getChecksumFragment((byte)0, data, (byte)(response_ext_length - 1)) + Consts.CHECKSUM_CONST))
                throw new DlReaderException("UFR_COMMUNICATION_ERROR");

            return data;
        }
    }

    public static class DlReaderException extends IOException {
        private static final long serialVersionUID = 1L;
        public int err_code;

        public DlReaderException() {
        }

        public DlReaderException(String ftStatusMsg) {
            super(ftStatusMsg);
            err_code = Consts.DL_READER_GENERAL_EXCEPTION;
        }

        public DlReaderException(String ftStatusMsg, int p_err_code) {
            super(ftStatusMsg);
            err_code = p_err_code;
        }

        public int getErrCode() {
            return err_code;
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

    public static class CardParams {
        private byte sak;
        private byte uid_size;

        public CardParams() {
            sak = 0;
            uid_size = 0;
        }

        public void setSak(byte p_sak) {
            sak = p_sak;
        }

        public byte getSak() {;
            return sak;
        }

        public void setUidSize(byte p_uid_size) {
            uid_size = p_uid_size;
        }

        public byte getUidSize() {
            return uid_size;
        }
    }
}
