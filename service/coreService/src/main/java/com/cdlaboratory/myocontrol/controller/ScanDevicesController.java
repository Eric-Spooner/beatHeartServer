package com.cdlaboratory.myocontrol.controller;

import com.cdlaboratory.myocontrol.Application;
import com.cdlaboratory.myocontrol.core.server.api.ScanDevicesApi;
import com.cdlaboratory.myocontrol.core.server.model.Device;
import com.github.hypfvieh.bluetooth.DeviceManager;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;
import org.freedesktop.dbus.exceptions.DBusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@RestController
public class ScanDevicesController implements ScanDevicesApi {

    public static final Logger LOG = Logger.getLogger(Application.class.getName());

    @Autowired
    private BluetoothController bluetoothController;

    public ScanDevicesController() {
        try {
            DeviceManager.createInstance(false);
        } catch (DBusException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ResponseEntity<Void> scanBluetoothDevices() {
        bluetoothController.clearList();

        LOG.info("Search Devices");
        DeviceManager.getInstance().scanForBluetoothDevices(4000);
        List<BluetoothDevice> foundDevices = DeviceManager.getInstance().getDevices();
        LOG.info("Search Devices finished");

        foundDevices.forEach(device -> {
            Device bluetoothDevice = new Device().name(device.getName()).UUID(device.getAddress());
            if (!bluetoothController.getDevices().contains(bluetoothDevice)) {
                bluetoothController.addDevice(bluetoothDevice, device);
            }
        });

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
