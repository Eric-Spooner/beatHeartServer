package com.cdlaboratory.myocontrol.controller;

import com.cdlaboratory.myocontrol.Application;
import com.cdlaboratory.myocontrol.core.server.api.BlueoothDevicesApi;
import com.cdlaboratory.myocontrol.core.server.api.DisconnectBluetoothDeviceApi;
import com.cdlaboratory.myocontrol.core.server.model.Device;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;
import com.myo.myobeatz.myo.MyoBluetoothManager;
import org.freedesktop.dbus.exceptions.DBusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class BluetoothController implements BlueoothDevicesApi, DisconnectBluetoothDeviceApi {

    public static final Logger LOG = Logger.getLogger(Application.class.getName());

    private Map<Device, BluetoothDevice> bluetoothDevices;
    private BluetoothDevice connectedDevice;
    private MyoBluetoothManager myoBluetoothManager;
    @Autowired
    private ScanDevicesController scanDevicesController;
    @Autowired
    private EMGController emgController;

    public BluetoothController() {
        bluetoothDevices = new HashMap<>();
        connectedDevice = null;
    }

    @Override
    public ResponseEntity<List<Device>> getBluetoothDevices() {
        return ResponseEntity.ok(new LinkedList<>(bluetoothDevices.keySet()));
    }

    @Override
    public ResponseEntity<Void> putBluetoothDevice(@RequestBody Device body) {
        connectedDevice = bluetoothDevices.get(body);
        connectedDevice.connect();
        try {
            myoBluetoothManager = new MyoBluetoothManager(emgController, connectedDevice);
            myoBluetoothManager.discoverServices();
        } catch (DBusException e) {
            LOG.log(Level.WARNING, "Bluetooth Manager not able to initialize");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        LOG.log(Level.INFO, "Connected to bluetooth device");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public void addDevice(Device device, BluetoothDevice bluetoothDevice) {
        this.bluetoothDevices.put(device, bluetoothDevice);
    }

    public List<Device> getDevices() {
        return new LinkedList<>(this.bluetoothDevices.keySet());
    }

    public void clearList() {
        disconnectDevice();
        this.bluetoothDevices = new HashMap<>();
    }

    @Override
    public ResponseEntity<Void> disconnectBluetoothDevice() {
        disconnectDevice();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void disconnectDevice() {
        if (connectedDevice != null) {
            try {
                myoBluetoothManager.stop();
                connectedDevice.disconnect();
                emgController.reset();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Unable to disconnect from bluetooth device", e);
            }
        }
    }
}
