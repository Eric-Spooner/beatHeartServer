package com.cdlaboratory.myocontrol.controller;

import com.cdlaboratory.myocontrol.core.server.api.GetEMGDataApi;
import com.cdlaboratory.myocontrol.core.server.model.EMGData;
import com.myo.myobeatz.interfaces.CallbackAble;
import com.myo.myobeatz.myo.emg.EmgData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
public class EMGController implements GetEMGDataApi, CallbackAble {

    private Map<Integer, LinkedList<Double>> myoSensorValueMap;
    private Map<Integer, EMGData> sensorToValue;
    private static int myoSensorValueIndex;

    public EMGController() {
        this.myoSensorValueMap = new HashMap<>();
        this.sensorToValue = new HashMap<>();
        myoSensorValueIndex = 0;
        initMyoSensorValueMap();
    }

    public void reset() {
        this.myoSensorValueMap = new HashMap<>();
        myoSensorValueIndex = 0;
        initMyoSensorValueMap();
    }

    /**
     * Initializes the Sensor Value Map.
     */
    private void initMyoSensorValueMap() {
        for (int i = 0; i < 8; i++) {
            LinkedList<Double> list = new LinkedList<>();
            for (int j = 0; j < 8; j++) {
                list.add(0.0D);
            }
            myoSensorValueMap.put(i, list);
            sensorToValue.put(i, new EMGData().emgValue(0.0d).maxValue(0.0d).id(i));
        }
    }

    @Override
    public ResponseEntity<List<EMGData>> getEmgData() {
        return ResponseEntity.ok(new LinkedList<>(sensorToValue.values()));
    }

    /**
     * Updates the value of the shown progress bars.
     *
     * @param value The given EMG Data gathered from the Myobracelet.
     */
    @Override
    public void callback(Object value) {
        assert value instanceof EmgData;
        EmgData emgData = (EmgData) value;
        if (emgData.getSize() == 8) {
            for (int i = 0; i <= 7; i++) {
                double progress = emgData.getElement(i);
                myoSensorValueMap.get(i).set(myoSensorValueIndex, progress);
                double rmsValue = getRootMeanSquareOfIndex(i);
                sensorToValue.get(i).setEmgValue(rmsValue);
                if (sensorToValue.get(i).getMaxValue() < rmsValue)
                    sensorToValue.get(i).setMaxValue(rmsValue);
            }
            myoSensorValueIndex++;
            if (myoSensorValueIndex == 8) {
                myoSensorValueIndex = 0;
            }
        }
    }

    /**
     * Calculates the Root Mean Square of ten values and normates the values from 0 to 100
     *
     * @param index Which myo sensor should be calculated.
     * @return The rms and normalized value of the sensor.
     */
    private double getRootMeanSquareOfIndex(int index) {
        double sum = 0;
        for (int i = 0; i < myoSensorValueMap.get(index).size(); i++) {
            sum += Math.pow(myoSensorValueMap.get(index).get(i), 2);
        }
        sum = sum / 10.0;
        return (((Math.sqrt(sum)) / 128) * 100);
    }

}
