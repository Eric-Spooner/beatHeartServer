package com.myo.myobeatz.myo;

import com.github.hypfvieh.bluetooth.DeviceManager;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattCharacteristic;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattDescriptor;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattService;
import com.myo.myobeatz.interfaces.CallbackAble;
import com.myo.myobeatz.myo.emg.ByteReader;
import com.myo.myobeatz.myo.emg.EmgCharacteristicData;
import com.myo.myobeatz.myo.emg.EmgData;
import org.bluez.exceptions.*;
import org.freedesktop.dbus.AbstractPropertiesHandler;
import org.freedesktop.dbus.SignalAwareProperties;
import org.freedesktop.dbus.exceptions.DBusException;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyoBluetoothManager extends AbstractPropertiesHandler {

    public static final Logger LOG = Logger.getLogger(MyoBluetoothManager.class.getName());

    /**
     * Service ID
     */
    private static final String MYO_CONTROL_ID = "d5060001-a904-deb9-4748-2c7f4a124842";
    private static final String MYO_EMG_DATA_ID = "d5060005-a904-deb9-4748-2c7f4a124842";
    /**
     * Characteristics ID
     */
    private static final String MYO_INFO_ID = "d5060101-a904-deb9-4748-2c7f4a124842";
    private static final String FIRMWARE_ID = "d5060201-a904-deb9-4748-2c7f4a124842";
    private static final String COMMAND_ID = "d5060401-a904-deb9-4748-2c7f4a124842";
    private static final String EMG_0_ID = "d5060105-a904-deb9-4748-2c7f4a124842";

    private static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    private BluetoothGattCharacteristic characteristicCommand;
    private BluetoothGattCharacteristic characteristicEmg0;
    private BluetoothGattDescriptor emg0Descriptor;
    private BluetoothGattCharacteristic myoInfoCharacteristics;

    /** Last time sent NEVER_SLEEP to Myo */
    private long lastNeverSleepTime = System.currentTimeMillis();
    /** Never Sleep Send Time Delay */
    private final static long NEVER_SLEEP_SEND_TIME = 10000;

    /**
     * EMG Command List
     */
    private MyoCommandList commandList = new MyoCommandList();

    private static CallbackAble<EmgData> callbackAble;

    private BluetoothDevice bluetoothDevice;

    public MyoBluetoothManager(CallbackAble<EmgData> callback, BluetoothDevice bluetoothDevice) throws DBusException {
        callbackAble = callback;
        this.bluetoothDevice = bluetoothDevice;
        DeviceManager.getInstance().registerPropertyHandler(this);
    }

    public void discoverServices() throws BluezFailedException, BluezNotSupportedException, BluezInProgressException, BluezNotAuthorizedException, BluezNotPermittedException {
        BluetoothGattService emgDataService = bluetoothDevice.getGattServiceByUuid(MYO_EMG_DATA_ID);

        if (emgDataService == null) {
            LOG.info("No Myo EMG-Data Service !!");
        } else {
            LOG.info("Found Myo EMG-Data Service !!");
            characteristicEmg0 = emgDataService.getGattCharacteristicByUuid(EMG_0_ID);
            if (characteristicEmg0 == null) {
                LOG.info("Not Found EMG-Data Characteristic");
            } else {
                LOG.info("Found EMG-Data Characteristic");
                emg0Descriptor = characteristicEmg0.getGattDescriptorByUuid(CLIENT_CHARACTERISTIC_CONFIG);
                try {
                    emg0Descriptor.getCharacteristic().startNotify();
                } catch (BluezFailedException | BluezInProgressException | BluezNotSupportedException e) {
                    LOG.log(Level.WARNING, "Start Notification did not work", e);
                }
            }
        }


        BluetoothGattService service = bluetoothDevice.getGattServiceByUuid(MYO_CONTROL_ID);
        if (service == null) {
            LOG.info("No Myo Control Service !!");
        } else {
            LOG.info("Find Myo Control Service !!");
            // Get the MyoInfoCharacteristic
            myoInfoCharacteristics = service.getGattCharacteristicByUuid(MYO_INFO_ID);
            if (myoInfoCharacteristics == null) {
                LOG.info("Not Found MyoInfo Characteristic");
            }

            // Get CommandCharacteristic
            characteristicCommand = service.getGattCharacteristicByUuid(COMMAND_ID);
            if (characteristicCommand == null) {
                LOG.info("Not found command Characteristic !!");
            } else {
                LOG.info("Find command Characteristic !!");
                setMyoControlCommand(commandList.sendUnSleep());
            }
        }

        setMyoControlCommand(commandList.sendUnLock());
        setMyoControlCommand(commandList.sendEmgOnly());
    }

    /**
     * Send a new control command to Myo
     *
     * @param command A byte array containing myo commands
     * @return True if command have been delivered successfully
     */
    private boolean setMyoControlCommand(byte[] command) {
        try {
            if (characteristicCommand != null) {
                characteristicCommand.writeValue(command, new HashMap<>());
            }
            return true;
        } catch (BluezFailedException | BluezNotSupportedException | BluezInProgressException | BluezNotAuthorizedException | BluezNotPermittedException e) {
            LOG.log(Level.WARNING, "Unable to send command to myp", e);
            return false;
        }

    }

    @Override
    public void handle(SignalAwareProperties.PropertiesChanged s) {
        if (s.getPropertiesChanged().get("Value") != null) {
            long systemTime_ms = System.currentTimeMillis();
            byte[] emg_data = (byte[]) s.getPropertiesChanged().get("Value").getValue();
            ByteReader byteReader = new ByteReader();
            byteReader.setByteData(emg_data);
            EmgCharacteristicData emgCharacteristicData = new EmgCharacteristicData(byteReader);
            callbackAble.callback(emgCharacteristicData.getEmg8Data_abs());
            if (systemTime_ms > lastNeverSleepTime + NEVER_SLEEP_SEND_TIME) {
//                 set Myo [Never Sleep Mode]
                setMyoControlCommand(commandList.sendUnSleep());
                lastNeverSleepTime = systemTime_ms;
            }
        }
    }

    public void stop() throws BluezFailedException {
        try {
            setMyoControlCommand(commandList.sendNormalSleep());
        } catch (Exception e) {
            LOG.info("Myo normal sleep did not work");
        }

        if (emg0Descriptor != null && emg0Descriptor.getCharacteristic() != null && emg0Descriptor.getCharacteristic().isNotifying()) {
            emg0Descriptor.getCharacteristic().stopNotify();
        }
    }

}
