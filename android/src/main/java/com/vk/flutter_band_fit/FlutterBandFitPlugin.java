package com.vk.flutter_band_fit;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.vk.flutter_band_fit.handler.BandStreamHandler;
import com.vk.flutter_band_fit.model.BleDevices;
import com.vk.flutter_band_fit.util.GlobalMethods;
import com.vk.flutter_band_fit.util.WatchConstants;
import com.yc.pedometer.dial.OnlineDialUtil;
import com.yc.pedometer.dial.Rgb;
import com.yc.pedometer.info.BPVOneDayInfo;
import com.yc.pedometer.info.CustomTestStatusInfo;
import com.yc.pedometer.info.DeviceParametersInfo;
import com.yc.pedometer.info.HeartRateHeadsetSportModeInfo;
import com.yc.pedometer.info.OxygenInfo;
import com.yc.pedometer.info.Rate24HourDayInfo;
import com.yc.pedometer.info.RateOneDayInfo;
import com.yc.pedometer.info.SevenDayWeatherInfo;
import com.yc.pedometer.info.SleepInfo;
import com.yc.pedometer.info.SleepTimeInfo;
import com.yc.pedometer.info.SportsModesInfo;
import com.yc.pedometer.info.StepOneDayAllInfo;
import com.yc.pedometer.info.StepOneHourInfo;
import com.yc.pedometer.info.TemperatureInfo;
import com.yc.pedometer.listener.OnlineDialListener;
import com.yc.pedometer.listener.OxygenRealListener;
import com.yc.pedometer.listener.TemperatureListener;
import com.yc.pedometer.sdk.BLEServiceOperate;
import com.yc.pedometer.sdk.BloodPressureChangeListener;
import com.yc.pedometer.sdk.BluetoothLeService;
import com.yc.pedometer.sdk.DataProcessing;
import com.yc.pedometer.sdk.ICallback;
import com.yc.pedometer.sdk.ICallbackStatus;
import com.yc.pedometer.sdk.RateChangeListener;
import com.yc.pedometer.sdk.RateOf24HourRealTimeListener;
import com.yc.pedometer.sdk.ServiceStatusCallback;
import com.yc.pedometer.sdk.SleepChangeListener;
import com.yc.pedometer.sdk.StepChangeListener;
import com.yc.pedometer.sdk.UTESQLOperate;
import com.yc.pedometer.sdk.WriteCommandToBLE;
import com.yc.pedometer.utils.BLEVersionUtils;
import com.yc.pedometer.utils.BandLanguageUtil;
import com.yc.pedometer.utils.CalendarUtils;
import com.yc.pedometer.utils.GetFunctionList;
import com.yc.pedometer.utils.GlobalVariable;
import com.yc.pedometer.utils.SPUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * FlutterBandFitPlugin
 */
public class FlutterBandFitPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

    // ServiceStatusCallback, ICallback,
    // RateCalibrationListener, TurnWristCalibrationListener, TemperatureListener, OxygenRealListener, BreatheRealListener
    /// PluginRegistry.ActivityResultListener
    ///FlutterPluginRegistry

    private FlutterPluginBinding flutterPluginBinding;
    private ActivityPluginBinding activityPluginBinding;

    private MethodChannel methodChannel;
    private EventChannel eventChannel, bpEventChannel;

    // Callbacks
    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    // private Map<String, Runnable> callbackById = new HashMap<>();
    // Map<String, Map<String, Object>> mCallbacks = new HashMap<>();

    private Context mContext;
    private Activity activity;
    // private Application mApplication;
    private MobileConnect mobileConnect;

    private final int REQUEST_ENABLE_BT = 1212;
    // private final int REQUEST_BLE_ENABLE = 1213;
    // private Boolean validateDeviceListCallback = false;

    // pedometer integration
    private BluetoothLeService mBluetoothLeService;
    private BLEServiceOperate bleServiceOperate;
    private WriteCommandToBLE mWriteCommand;
    private UTESQLOperate mUTESQLOperate;
    // private Updates mUpdates;
    private DataProcessing mDataProcessing;

    //Return Back Handler Constants
    private final int RETURN_DELAY_MS = 500;
    //private final int SERVER_CALL_BACK_OK_MSG = 31;

    //
    private SharedPreferences.Editor sharedEditor;
    private SharedPreferences sharedPref;

    private final String BAND_FACE_PROGRESS = "band_dial_progress";


    private Context getApplicationContext() {
        return this.mContext.getApplicationContext();
    }

    private boolean checkPermissionEnabled(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.flutterPluginBinding = flutterPluginBinding;
        this.mContext = flutterPluginBinding.getApplicationContext();
        sharedPref = mContext.getSharedPreferences("smart_band", Context.MODE_PRIVATE);
        sharedEditor = sharedPref.edit();
        setUpEngine(this, flutterPluginBinding.getBinaryMessenger(), flutterPluginBinding.getApplicationContext());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
        eventChannel.setStreamHandler(null);
        bpEventChannel.setStreamHandler(null);
        methodChannel = null;
        eventChannel = null;
        bpEventChannel = null;

        GlobalVariable.BLE_UPDATE = false;
//        if (mUpdates != null) {
//            mUpdates.unRegisterBroadcastReceiver();
//        }
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        Log.e("onAttachedToActivity", "inside_attached");
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        Log.e("onAttachedToActivity", "onReattachedToActivityForConfigChanges");
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        Log.e("onDetachedFromActivity", "onDetachedFromActivityForConfigChanges");
    }

    @Override
    public void onDetachedFromActivity() {
        Log.e("onDetachedFromActivity", "onDetachedFromActivity");
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        try {
            handleMethodCall(call, result);
            //this.flutterResultBluConnect = result;
        } catch (Exception exp) {
            Log.e("onMethodCallExp::", exp.getMessage());
        }
    }

    private void setUpEngine(FlutterBandFitPlugin flutterBandFitPlugin, BinaryMessenger binaryMessenger, Context applicationContext) {
        methodChannel = new MethodChannel(binaryMessenger, WatchConstants.SMART_METHOD_CHANNEL); // "mobile_smart_watch"
        methodChannel.setMethodCallHandler(flutterBandFitPlugin);

        eventChannel = new EventChannel(binaryMessenger, WatchConstants.SMART_EVENT_CHANNEL);
        bpEventChannel = new EventChannel(binaryMessenger, WatchConstants.SMART_BP_TEST_CHANNEL);

        //mCallbackChannel = new MethodChannel(binaryMessenger, WatchConstants.SMART_CALLBACK);
        // mCallbackChannel.setMethodCallHandler(callbacksHandler);
        //mCallbackChannel.setMethodCallHandler(mobileSmartWatchPlugin);
        eventChannel.setStreamHandler(new BandStreamHandler(applicationContext));
        bpEventChannel.setStreamHandler(new BandStreamHandler(applicationContext));

        try {
            mUTESQLOperate = UTESQLOperate.getInstance(applicationContext.getApplicationContext());
            mobileConnect = new MobileConnect(applicationContext.getApplicationContext());
            bleServiceOperate = mobileConnect.getBLEServiceOperate();
            bleServiceOperate.setServiceStatusCallback(new ServiceStatusCallback() {
                @Override
                public void OnServiceStatuslt(int status) {
                    if (status == ICallbackStatus.BLE_SERVICE_START_OK) {
                        Log.e("inside_service_result", "" + mBluetoothLeService);
                        if (mBluetoothLeService == null) {
                            startListeningCallback(true);
                        }
                    }
                }
            });

            mBluetoothLeService = bleServiceOperate.getBleService();
            if (mBluetoothLeService != null) {
                startListeningCallback(false);
            }

            mWriteCommand = WriteCommandToBLE.getInstance(applicationContext.getApplicationContext());
            mDataProcessing = DataProcessing.getInstance(applicationContext.getApplicationContext());

//            mUpdates = Updates.getInstance(mContext);
//            mUpdates.setHandler(mUpdateHandler);// Get upgrade operation information
//            mUpdates.registerBroadcastReceiver();

            startListeningDataProcessing();


        } catch (Exception exp) {
            Log.e("setUpEngineExp:", exp.getMessage());
        }
    }

    private void startListeningDataProcessing() {
        mDataProcessing.setOnStepChangeListener(new StepChangeListener() {
            @Override
            public void onStepChange(StepOneDayAllInfo info) {
                if (info != null) {
                    //Log.e("onStepChange1", "calendar: " + info.getCalendar());
                    Log.e("onStepChange2", "mSteps: " + info.getStep() + ", mDistance: " + info.getDistance() + ", mCalories=" + info.getCalories());
                    Log.e("onStepChange3", "mRunSteps: " + info.getRunSteps() + ", mRunDistance: " + info.getRunDistance() + ", mRunCalories=" + info.getRunCalories() + ", mRunDurationTime=" + info.getRunDurationTime());
                    Log.e("onStepChange4", "mWalkSteps: " + info.getWalkSteps() + ", mWalkDistance: " + info.getWalkDistance() + ", mWalkCalories=" + info.getWalkCalories() + ", mWalkDurationTime=" + info.getWalkDurationTime());
                    Log.e("onStepChange5", "getStepOneHourArrayInfo: " + info.getStepOneHourArrayInfo() + ", getStepRunHourArrayInfo: " + info.getStepRunHourArrayInfo() + ", getStepWalkHourArrayInfo=" + info.getStepWalkHourArrayInfo());

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("steps", "" + info.getStep());
                        //   jsonObject.put("distance", ""+info.getDistance());
                        //  jsonObject.put("calories", ""+info.getCalories());
                        jsonObject.put("distance", "" + GlobalMethods.convertDoubleToStringWithDecimal(info.getDistance()));
                        jsonObject.put("calories", "" + GlobalMethods.convertDoubleToStringWithDecimal(info.getCalories()));
                        // runOnUIThread(WatchConstants.STEPS_REAL_TIME, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                        pushJsonEventObjCallBack(WatchConstants.STEPS_REAL_TIME, jsonObject, WatchConstants.SC_SUCCESS);
                    } catch (Exception e) {
                        // e.printStackTrace();
                        Log.e("onStepJSONExp::", e.getMessage());
                    }

                }
            }
        });
        mDataProcessing.setOnRateListener(new RateChangeListener() {
            @Override
            public void onRateChange(int rate, int status) {
                Log.e("onRateListener", "rate: " + rate + ", status: " + status);
                updateContinuousHeartRate(rate);
                /*try {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("hr", "" + rate);
                            } catch (Exception e) {
                                // e.printStackTrace();
                                Log.e("onRateJSONExp: ", e.getMessage());
                            }
                            runOnUIThread(WatchConstants.HR_REAL_TIME, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                        }
                    });
                } catch (Exception exp) {
                    Log.e("onRateExp: ", exp.getMessage());
                }*/
            }
        });
        mDataProcessing.setOnSleepChangeListener(new SleepChangeListener() {
            @Override
            public void onSleepChange() {
                Log.e("onSleepChangeCalender", CalendarUtils.getCalendar(0));
                SleepTimeInfo sleepTimeInfo = UTESQLOperate.getInstance(mContext).querySleepInfo(CalendarUtils.getCalendar(0));
                int deepTime, lightTime, awakeCount, sleepTotalTime;
                if (sleepTimeInfo != null) {
                    deepTime = sleepTimeInfo.getDeepTime();
                    lightTime = sleepTimeInfo.getLightTime();
                    awakeCount = sleepTimeInfo.getAwakeCount();
                    sleepTotalTime = sleepTimeInfo.getSleepTotalTime();
                    Log.e("sleepTimeInfo", "deepTime: " + deepTime + ", lightTime: " + lightTime + ", awakeCount=" + awakeCount + ", sleepTotalTime=" + sleepTotalTime);
                }
            }
        });
        mDataProcessing.setOnBloodPressureListener(new BloodPressureChangeListener() {
            @Override
            public void onBloodPressureChange(int highPressure, int lowPressure, int status) {
                Log.e("onBloodPressureChange", "highPressure: " + highPressure + ", lowPressure: " + lowPressure + ", status=" + status);
                try {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("high", "" + highPressure);
                        jsonObject.put("low", "" + lowPressure);
                        jsonObject.put("status", "" + status);
                        //jsonObject.put("time", );
                        // runOnUIThread(WatchConstants.BP_RESULT, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                        //pushEventCallBack(WatchConstants.BP_RESULT, jsonObject, WatchConstants.SC_SUCCESS);
                        pushOtherEventCallBack(WatchConstants.BP_RESULT, jsonObject, WatchConstants.SC_SUCCESS);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        Log.e("bpChangeJSONExp::", e.getMessage());
                    }

                } catch (Exception exp) {
                    Log.e("bpChangeExp::", exp.getMessage());
                }
            }
        });
        mDataProcessing.setOnRateOf24HourListenerRate(new RateOf24HourRealTimeListener() {
            @Override
            public void onRateOf24HourChange(int maxHeartRateValue, int minHeartRateValue, int averageHeartRateValue, boolean isRealTimeValue) {
                Log.e("onRateOf24Hour", "maxHeartRateValue: " + maxHeartRateValue + ", minHeartRateValue: " + minHeartRateValue + ", averageHeartRateValue=" + averageHeartRateValue + ", isRealTimeValue=" + isRealTimeValue);

                try {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("maxHr", "" + maxHeartRateValue);
                        jsonObject.put("minHr", "" + minHeartRateValue);
                        jsonObject.put("avgHr", "" + averageHeartRateValue);
                        jsonObject.put("rtValue", isRealTimeValue);
                        // runOnUIThread(WatchConstants.BP_RESULT, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                        pushJsonEventObjCallBack(WatchConstants.HR_24_REAL_RESULT, jsonObject, WatchConstants.SC_SUCCESS);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        Log.e("onRateOf24JSONExp::", e.getMessage());
                    }

                } catch (Exception exp) {
                    Log.e("onRate24HourExp::", exp.getMessage());
                }
            }
        });
        /*mUpdates.setOnServerCallbackListener(new OnServerCallbackListener() {
            @Override
            public void OnServerCallback(int status, String description) {

                Log.e("OnServerCallback", "status: " + status + ", description: " + description);

                if (status == GlobalVariable.SERVER_CALL_BACK_SUCCESSFULL) {//access server OK
                    mUpdateHandler.sendEmptyMessage(SERVER_CALL_BACK_OK_MSG);
                } else {//can't access server
                    mUpdateHandler.sendEmptyMessage(GlobalVariable.SERVER_IS_BUSY_MSG);
                }
            }
        });*/
    }

//    private final Handler mUpdateHandler = new Handler(Looper.getMainLooper()) {
//        public void handleMessage(Message msg) {
//            Log.e("mUpdateHandler:", msg.what + " - " + msg.getData());
//        }
//    };

    private void updateContinuousHeartRate(int rate) {
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("hr", "" + rate);
            } catch (Exception e) {
                // e.printStackTrace();
                Log.e("onRateJSONExp: ", e.getMessage());
            }
            // runOnUIThread(WatchConstants.HR_REAL_TIME, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
            //sendEventToDart(jsonObject, WatchConstants.SMART_EVENT_CHANNEL);
            pushJsonEventObjCallBack(WatchConstants.HR_REAL_TIME, jsonObject, WatchConstants.SC_SUCCESS);
        } catch (Exception exp) {
            Log.e("onRateExp: ", exp.getMessage());
        }
    }

    private long hexToLong(String hex) {
        return Long.parseLong(hex, 16);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ICallbackStatus.READ_CHAR_SUCCESS: // 137
                    break;
                case ICallbackStatus.SYNC_TIME_OK: // 6 //sync time ok
                    pushJsonEventObjCallBack(WatchConstants.SYNC_TIME_OK, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;
                case ICallbackStatus.WRITE_COMMAND_TO_BLE_SUCCESS: // 148
                    //Log.e("bandResult", "WRITE_COMMAND_TO_BLE_SUCCESS "+isBandFaceRunning);
                    //Log.e("bandResult", "WRITE_COMMAND_TO_BLE_SUCCESS "+getBandDialProgressStatus());
                    pushJsonEventSuccessFailure(WatchConstants.SYNC_BLE_WRITE_SUCCESS, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;
                case ICallbackStatus.WRITE_COMMAND_TO_BLE_FAIL: // 149
                    //Log.e("bandResult", "WRITE_COMMAND_TO_BLE_FAIL "+isBandFaceRunning);
                    // Log.e("bandResult", "WRITE_COMMAND_TO_BLE_SUCCESS "+getBandDialProgressStatus());
                    // when any ble write command failed
                    pushJsonEventSuccessFailure(WatchConstants.SYNC_BLE_WRITE_FAIL, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;
                case ICallbackStatus.OFFLINE_STEP_SYNC_OK: // 2
                    //steps sync done
                    pushJsonEventArrayCallBack(WatchConstants.SYNC_STEPS_FINISH, new JSONArray(), WatchConstants.SC_SUCCESS);
                    break;
                case ICallbackStatus.OFFLINE_SLEEP_SYNC_OK: // 6
                    //sleep sync done
                    pushJsonEventArrayCallBack(WatchConstants.SYNC_SLEEP_FINISH, new JSONArray(), WatchConstants.SC_SUCCESS);
                    break;
                case ICallbackStatus.OFFLINE_STEP_SYNC_TIMEOUT: // 93
                    //sleep sync done
                    pushJsonEventObjCallBack(WatchConstants.SYNC_STEPS_TIME_OUT, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;

                case ICallbackStatus.OFFLINE_SLEEP_SYNC_TIMEOUT: // 93
                    //sleep sync done
                    pushJsonEventObjCallBack(WatchConstants.SYNC_SLEEP_TIME_OUT, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;

                case ICallbackStatus.SYNC_TEMPERATURE_COMMAND_FINISH_CRC_FAIL:
                    pushJsonEventObjCallBack(WatchConstants.SYNC_TEMPERATURE_TIME_OUT, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;

                case ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNC_OK: // 47
                    //bp sync done
                    pushJsonEventArrayCallBack(WatchConstants.SYNC_BP_FINISH, new JSONArray(), WatchConstants.SC_SUCCESS);
                    break;
                case ICallbackStatus.OFFLINE_24_HOUR_RATE_SYNC_OK: // 82
                    pushJsonEventArrayCallBack(WatchConstants.SYNC_24_HOUR_RATE_FINISH, new JSONArray(), WatchConstants.SC_SUCCESS);
                    break;

                case ICallbackStatus.SYNC_TEMPERATURE_COMMAND_OK: // 110
                    pushJsonEventArrayCallBack(WatchConstants.SYNC_TEMPERATURE_FINISH, new JSONArray(), WatchConstants.SC_SUCCESS);
                    break;
                case ICallbackStatus.ECG_DATA_SYNC_OK: // 165
                    pushJsonEventObjCallBack(WatchConstants.SYNC_ECG_DATA_FINISH, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;

                case ICallbackStatus.SYNC_OXYGEN_COMMAND_OK: // 165
                    pushJsonEventArrayCallBack(WatchConstants.SYNC_OXYGEN_FINISH, new JSONArray(), WatchConstants.SC_SUCCESS);
                    break;

                case ICallbackStatus.SET_STEPLEN_WEIGHT_OK: // 8
                    //runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS, new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                    pushJsonEventObjCallBack(WatchConstants.UPDATE_DEVICE_PARAMS, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;

                case ICallbackStatus.BLOOD_PRESSURE_TEST_START: // 50
                    pushOtherEventCallBack(WatchConstants.BP_TEST_STARTED, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;

                case ICallbackStatus.BLOOD_PRESSURE_TEST_END: // 91
                    pushOtherEventCallBack(WatchConstants.BP_TEST_FINISHED, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;

                case ICallbackStatus.BLOOD_PRESSURE_TEST_TIME_OUT: // 48
                    pushOtherEventCallBack(WatchConstants.BP_TEST_TIME_OUT, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;

                case ICallbackStatus.BLOOD_PRESSURE_TEST_ERROR: // 49
                    pushOtherEventCallBack(WatchConstants.BP_TEST_ERROR, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;

                case ICallbackStatus.QUERY_CURRENT_TEMPERATURE_COMMAND_OK: // 49
                    pushOtherEventCallBack(WatchConstants.TEMP_TEST_OK, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;

                case ICallbackStatus.RATE_TEST_START: // 79
                    // runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS,  new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                    pushJsonEventObjCallBack(WatchConstants.HR_TEST_STARTED, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;
                case ICallbackStatus.RATE_TEST_STOP: // 80
                    // runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS,  new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                    pushJsonEventObjCallBack(WatchConstants.HR_TEST_FINISHED, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;
                case ICallbackStatus.CONNECTED_STATUS: // 20
                    // connected successfully
                    //runOnUIThread(new JSONObject(), WatchConstants.DEVICE_CONNECTED, WatchConstants.SC_SUCCESS);
                    //flutterResultBluConnect.success(connectionStatus);
                    // updateConnectionStatus(true);
                    //updateConnectionStatus2(true);
                    //updateConnectionStatus3(true);
                    // mobileConnect.getBluetoothLeService().setRssiHandler(mHandlerMessage);
                    updateReadRSSIThread();
                    updatePasswordStatus();
                    // runOnUIThread(WatchConstants.DEVICE_CONNECTED, new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                    pushJsonEventObjCallBack(WatchConstants.DEVICE_CONNECTED, new JSONObject(), WatchConstants.SC_SUCCESS);
                    break;

            }
        }
    };

    private void startListeningCallback(boolean initial) {
        if (initial) {
            mBluetoothLeService = mobileConnect.getBLEServiceOperate().getBleService();
        }
        mobileConnect.setBluetoothLeService(mBluetoothLeService);
        mBluetoothLeService.setICallback(new ICallback() {
            @Override
            public void OnResult(boolean status, int result) {
                Log.e("onResult:", "status>> " + status + " resultValue>> " + result);
                try {
                    boolean checkProgress;
                    JSONObject jsonObject = new JSONObject();
                    switch (result) {
                        case ICallbackStatus.GET_BLE_VERSION_OK:
                            String deviceVersion = SPUtil.getInstance(mContext).getImgLocalVersion();
                            Log.e("deviceVersion::", deviceVersion);
                            jsonObject.put("deviceVersion", deviceVersion);
                            // deviceVersionIDResult.success(jsonObject.toString());
                            // runOnUIThread(new JSONObject(), WatchConstants.DEVICE_VERSION, WatchConstants.SC_SUCCESS);
                            //  runOnUIThread(WatchConstants.DEVICE_VERSION, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                            pushJsonEventObjCallBack(WatchConstants.DEVICE_VERSION, jsonObject, WatchConstants.SC_SUCCESS);
                            break;
                        case ICallbackStatus.GET_BLE_BATTERY_OK:
                            //String deviceVer = SPUtil.getInstance(mContext).getImgLocalVersion();
                            String batteryStatus = "" + SPUtil.getInstance(mContext).getBleBatteryValue();
                            Log.e("batteryStatus::", batteryStatus);
                            //jsonObject.put("deviceVersion", deviceVer);
                            jsonObject.put("batteryStatus", batteryStatus);
                            // runOnUIThread(jsonObject, WatchConstants.BATTERY_VERSION, WatchConstants.SC_SUCCESS);
                            //deviceBatteryResult.success(jsonObject.toString());
                            // runOnUIThread(WatchConstants.BATTERY_STATUS, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                            pushJsonEventObjCallBack(WatchConstants.BATTERY_STATUS, jsonObject, WatchConstants.SC_SUCCESS);
                            break;
                        // while connecting a device
                        case ICallbackStatus.READ_CHAR_SUCCESS: // 137
                            mHandler.sendEmptyMessage(ICallbackStatus.READ_CHAR_SUCCESS);
                            break;
                        case ICallbackStatus.SYNC_TIME_OK: // 6
                            mHandler.sendEmptyMessage(ICallbackStatus.SYNC_TIME_OK);
                            break;
                        case ICallbackStatus.WRITE_COMMAND_TO_BLE_SUCCESS: // 148
                            checkProgress = getBandDialProgressStatus();
                            //Log.e("ICallbackStatus", "WRITE_COMMAND_TO_BLE_SUCCESS "+getBandDialProgressStatus());
                            if (!checkProgress) {
                                mHandler.sendEmptyMessage(ICallbackStatus.WRITE_COMMAND_TO_BLE_SUCCESS);
                            }
                            break;
                        case ICallbackStatus.WRITE_COMMAND_TO_BLE_FAIL: // 149
                            checkProgress = getBandDialProgressStatus();
                            //Log.e("ICallbackStatus", "WRITE_COMMAND_TO_BLE_FAIL "+checkProgress);
                            if (!checkProgress) {
                                mHandler.sendEmptyMessage(ICallbackStatus.WRITE_COMMAND_TO_BLE_FAIL);
                            }
                            break;
                        case ICallbackStatus.OFFLINE_STEP_SYNC_OK: // 2
                            mHandler.sendEmptyMessage(ICallbackStatus.OFFLINE_STEP_SYNC_OK);
                            break;
                        case ICallbackStatus.OFFLINE_SLEEP_SYNC_OK: // 6
                            //sleep sync done
                            mHandler.sendEmptyMessage(ICallbackStatus.OFFLINE_SLEEP_SYNC_OK);
                            break;
                        case ICallbackStatus.OFFLINE_STEP_SYNC_TIMEOUT: // 93
                            //sleep sync done
                            mHandler.sendEmptyMessage(ICallbackStatus.OFFLINE_STEP_SYNC_TIMEOUT);
                            break;
                        case ICallbackStatus.OFFLINE_SLEEP_SYNC_TIMEOUT: // 93
                            //sleep sync done
                            mHandler.sendEmptyMessage(ICallbackStatus.OFFLINE_SLEEP_SYNC_TIMEOUT);
                            break;
                        case ICallbackStatus.SYNC_TEMPERATURE_COMMAND_FINISH_CRC_FAIL:
                            mHandler.sendEmptyMessage(ICallbackStatus.SYNC_TEMPERATURE_COMMAND_FINISH_CRC_FAIL);
                            break;

                        case ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNC_OK: // 47
                            //bp sync done
                            mHandler.sendEmptyMessage(ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNC_OK);
                            break;

                        case ICallbackStatus.OFFLINE_24_HOUR_RATE_SYNC_OK: // 82
                            mHandler.sendEmptyMessage(ICallbackStatus.OFFLINE_24_HOUR_RATE_SYNC_OK);
                            break;

                        case ICallbackStatus.SYNC_TEMPERATURE_COMMAND_OK: // 110
                            mHandler.sendEmptyMessage(ICallbackStatus.SYNC_TEMPERATURE_COMMAND_OK);
                            break;

                        case ICallbackStatus.ECG_DATA_SYNC_OK: // 165
                            mHandler.sendEmptyMessage(ICallbackStatus.ECG_DATA_SYNC_OK);
                            break;

                        case ICallbackStatus.SYNC_OXYGEN_COMMAND_OK: // 165
                            mHandler.sendEmptyMessage(ICallbackStatus.SYNC_OXYGEN_COMMAND_OK);
                            break;

                        case ICallbackStatus.SET_STEPLEN_WEIGHT_OK: // 8
                            //runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS, new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                            mHandler.sendEmptyMessage(ICallbackStatus.SET_STEPLEN_WEIGHT_OK);
                            break;

                        case ICallbackStatus.BLOOD_PRESSURE_TEST_START: // 50
                            mHandler.sendEmptyMessage(ICallbackStatus.BLOOD_PRESSURE_TEST_START);
                            break;

                        case ICallbackStatus.BLOOD_PRESSURE_TEST_END: // 91
                            mHandler.sendEmptyMessage(ICallbackStatus.BLOOD_PRESSURE_TEST_END);
                            break;

                        case ICallbackStatus.BLOOD_PRESSURE_TEST_TIME_OUT: // 48
                            mHandler.sendEmptyMessage(ICallbackStatus.BLOOD_PRESSURE_TEST_TIME_OUT);
                            break;

                        case ICallbackStatus.BLOOD_PRESSURE_TEST_ERROR: // 49
                            mHandler.sendEmptyMessage(ICallbackStatus.BLOOD_PRESSURE_TEST_ERROR);
                            break;

                        case ICallbackStatus.QUERY_CURRENT_TEMPERATURE_COMMAND_OK: // 49
                            mHandler.sendEmptyMessage(ICallbackStatus.QUERY_CURRENT_TEMPERATURE_COMMAND_OK);
                            break;

                        case ICallbackStatus.RATE_TEST_START: // 79
                            // runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS,  new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                            mHandler.sendEmptyMessage(ICallbackStatus.RATE_TEST_START);
                            break;
                        case ICallbackStatus.RATE_TEST_STOP: // 80
                            // runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS,  new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                            mHandler.sendEmptyMessage(ICallbackStatus.RATE_TEST_STOP);
                            break;

                        case ICallbackStatus.CONNECTED_STATUS: // 20
                            mHandler.sendEmptyMessage(ICallbackStatus.CONNECTED_STATUS);
                            break;

//                        case ICallbackStatus.OFFLINE_RATE_SYNC_OK: // 23
//                            pushJsonEventObjCallBack(WatchConstants.SYNC_RATE_FINISH, new JSONObject(), WatchConstants.SC_SUCCESS);
//                            break;
                        case ICallbackStatus.TEMPERATURE_DATA_SYNCING: // 111
                            //pushJsonEventArrayCallBack(WatchConstants.SYNC_TEMPERATURE_FINISH, new JSONArray(), WatchConstants.SC_SUCCESS);
                            break;

                        case ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNCING: // 46
                            // runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS,  new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                            break;

                        case ICallbackStatus.START_OXYGEN_COMMAND_OK: // 120
                            jsonObject.put("status", status);
                            pushOtherEventCallBack(WatchConstants.OXYGEN_TEST_STARTED, jsonObject, WatchConstants.SC_SUCCESS);
                            break;

                        case ICallbackStatus.STOP_OXYGEN_COMMAND_OK: // 121
                            jsonObject.put("status", status);
                            pushOtherEventCallBack(WatchConstants.OXYGEN_TEST_FINISHED, jsonObject, WatchConstants.SC_SUCCESS);
                            break;

                        //after connections
                        case ICallbackStatus.SYNC_STATUS_24_HOUR_RATE_OPEN:  //175
                            // sync 24 hrs heart rate status
                            jsonObject.put("status", status);
                            pushJsonEventObjCallBack(WatchConstants.SYNC_STATUS_24_HOUR_RATE_OPEN, jsonObject, WatchConstants.SC_SUCCESS);
                            break;

                        case ICallbackStatus.QUERY_CURRENT_OXYGEN_COMMAND_OK:  //126
                            // sync 24 hrs heart rate status
                            jsonObject.put("status", status);
                            pushJsonEventObjCallBack(WatchConstants.SYNC_STATUS_CURRENT_OXYGEN_CMD, jsonObject, WatchConstants.SC_SUCCESS);
                            break;


                        case ICallbackStatus.SET_OXYGEN_AUTOMATIC_TEST_COMMAND_OK:  //126
                            // sync 24 hrs heart rate status
                            jsonObject.put("status", status);
                            pushJsonEventObjCallBack(WatchConstants.SYNC_STATUS_24_HOUR_OXYGEN_OPEN, jsonObject, WatchConstants.SC_SUCCESS);
                            break;

                        case ICallbackStatus.SYNC_TEMPERATURE_AUTOMATICTEST_INTERVAL_COMMAND_OK: // 107
                            jsonObject.put("status", status);
                            pushJsonEventObjCallBack(WatchConstants.SYNC_TEMPERATURE_24_HOUR_AUTOMATIC, jsonObject, WatchConstants.SC_SUCCESS);
                            break;

                        /*case ICallbackStatus.DO_NOT_DISTURB_OPEN: // 107
                            jsonObject.put("status", status);
                            pushEventCallBack(WatchConstants.DND_OPENED, jsonObject, WatchConstants.SC_SUCCESS);
                            break;

                        case ICallbackStatus.DO_NOT_DISTURB_CLOSE: // 107
                            jsonObject.put("status", status);
                            pushEventCallBack(WatchConstants.DND_CLOSED, jsonObject, WatchConstants.SC_SUCCESS);
                            break;*/

                        case ICallbackStatus.QUERY_BAND_LANGUAGE_OK: // 147
                            jsonObject.put("status", status);
                            jsonObject.put("result", result);
                            int languageType = SPUtil.getInstance(mContext).getBandCurrentLanguageType();
                            jsonObject.put("languageType", "" + languageType);
                            pushJsonEventObjCallBack(WatchConstants.QUERY_BAND_LANGUAGE, jsonObject, WatchConstants.SC_SUCCESS);
                            break;

                        case ICallbackStatus.BAND_LANGUAGE_SYNC_OK: // 78
                            jsonObject.put("status", status);
                            jsonObject.put("result", result);
                            pushJsonEventObjCallBack(WatchConstants.SYNC_BAND_LANGUAGE, jsonObject, WatchConstants.SC_SUCCESS);
                            break;

                        case ICallbackStatus.SEVEN_DAY_WEATHER_SYNC_SUCCESS: // 51
                            jsonObject.put("status", status);
                            pushJsonEventObjCallBack(WatchConstants.SYNC_WEATHER_SUCCESS, jsonObject, WatchConstants.SC_SUCCESS);
                            break;


                        case ICallbackStatus.DISCONNECT_STATUS: // 19
                            String lastConnectAddress = SPUtil.getInstance(mContext).getLastConnectDeviceAddress();
                            //  boolean connectResult = mobileConnect.getBLEServiceOperate().connect(lastConnectAddress);
                            // jsonObject.put("status", connectResult);
                            // disconnected successfully
                            // mobileConnect.disconnectDevice();
                            // updateConnectionStatus(false);
                            // runOnUIThread(WatchConstants.DEVICE_DISCONNECTED, new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                            pushJsonEventObjCallBack(WatchConstants.DEVICE_DISCONNECTED, jsonObject, WatchConstants.SC_SUCCESS);
                            // runOnUIThread(new JSONObject(), WatchConstants.DEVICE_DISCONNECTED, WatchConstants.SC_SUCCESS);
                            break;
                    }
                } catch (Exception exp) {
                    Log.e("ble_service_exp:", exp.getMessage());
                    // runOnUIThread(WatchConstants.CALLBACK_EXCEPTION, new JSONObject(), WatchConstants.SERVICE_LISTENING, WatchConstants.SC_FAILURE);
                    pushJsonEventObjCallBack(WatchConstants.CALLBACK_EXCEPTION, new JSONObject(), WatchConstants.SC_FAILURE);
                }
            }

            @Override
            public void OnDataResult(boolean status, int result, byte[] data) {
                Log.e("OnDataResult:", "data.length>> " + data.length);
                JSONArray hexArray = new JSONArray();
                JSONArray decimalArray = new JSONArray();
                StringBuilder stringBuilder = null;
                try {
                    if (data != null && data.length > 0) {
                        Log.e("OnDataResult:", "result>> " + result + ": status>> " + status + " :data>> " + data.toString());

                        stringBuilder = new StringBuilder(data.length);

                        for (byte byteChar : data) {
                            //Log.e("each_byteChar :", "" + byteChar);
                            String hexCode = String.format("%02X", byteChar);
                            // Log.e("each_data_str :", "" + hexCode);
                            Log.e("each_data_dec :", "" + Integer.parseInt(String.format("%02X", byteChar), 16));
                            //Log.e("each_data_int :", "" + Integer.parseInt(String.format("%02X", byteChar),2));
                            hexArray.put(hexCode);
                            decimalArray.put("" + Integer.parseInt(hexCode, 16));
                            stringBuilder.append(hexCode);
                        }
                        Log.e("dataBuilder :", "" + stringBuilder.toString());
                        //Log.e("dataBuilderLong :", "" + hexToLong(stringBuilder.toString()));
                    }
                } catch (Exception exp) {
                    Log.e("stringBuilderExp:", "" + exp.getMessage());
                }
                switch (result) {
                    case ICallbackStatus.DO_NOT_DISTURB_CLOSE://85 // Do not disturb mode off
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("result", "" + result);
                            jsonObject.put("status", "" + status);
                            jsonObject.put("value", stringBuilder != null ? stringBuilder.toString() : "");
                            jsonObject.put("hex", hexArray);
                            jsonObject.put("decimal", decimalArray);
                            Log.e("DO_NOT_DISTURB_CLOSE :", "" + jsonObject.toString());
                            pushJsonEventObjCallBack(WatchConstants.DND_CLOSED, jsonObject, WatchConstants.SC_SUCCESS);
                        } catch (Exception exp) {
                            Log.e("DND_CLOSE_EXP: ", exp.getMessage());
                        }
                        break;
                    case ICallbackStatus.DO_NOT_DISTURB_OPEN://回调 勿扰模式打开
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("result", "" + result);
                            jsonObject.put("status", "" + status);
                            jsonObject.put("value", stringBuilder != null ? stringBuilder.toString() : "");
                            jsonObject.put("hex", hexArray);
                            jsonObject.put("decimal", decimalArray);
                            Log.e("DO_NOT_DISTURB_OPEN :", "" + jsonObject.toString());
                            pushJsonEventObjCallBack(WatchConstants.DND_OPENED, jsonObject, WatchConstants.SC_SUCCESS);
                        } catch (Exception exp) {
                            Log.e("DND_OPEN_EXP: ", exp.getMessage());
                        }
                        break;
                    case ICallbackStatus.QUICK_SWITCH_SURPPORT_COMMAND_OK://118 ;
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("result", "" + result);
                            jsonObject.put("status", "" + status);
                            jsonObject.put("value", stringBuilder != null ? stringBuilder.toString() : "");
                            jsonObject.put("hex", hexArray);
                            jsonObject.put("decimal", decimalArray);
                            Log.e("QUICK_SUPPORT_data:", jsonObject.toString());
                            pushJsonEventObjCallBack(WatchConstants.QUICK_SWITCH_SUPPORT, jsonObject, WatchConstants.SC_SUCCESS);
                        } catch (Exception exp) {
                            Log.e("QUICK_SUPPORT_EXP: ", exp.getMessage());
                        }
                        break;


                    case ICallbackStatus.QUICK_SWITCH_STATUS_COMMAND_OK://119 ; Callback The APP queries the status of the shortcut switch, returns all the status of the shortcut switch, and automatically reports the status of the shortcut switch when the shortcut switch on the bracelet changes
                        // LogUtils.d("QUICK_SWITCH_STATUS_CMD_OK", "The APP queries the status of the shortcut switch, returns all the status of the shortcut switch, and automatically reports the status of the shortcut switch when the shortcut switch on the bracelet changes");
                        //For data parsing, refer to the document queryQuickSwitchSupListStatus method description
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("result", "" + result);
                            jsonObject.put("status", "" + status);
                            jsonObject.put("value", stringBuilder != null ? stringBuilder.toString() : "");
                            jsonObject.put("hex", hexArray);
                            jsonObject.put("decimal", decimalArray);
                            Log.e("QUICK_STATUS_data:", jsonObject.toString());
                            pushJsonEventObjCallBack(WatchConstants.QUICK_SWITCH_STATUS, jsonObject, WatchConstants.SC_SUCCESS);
                        } catch (Exception exp) {
                            Log.e("QUICK_STATUS_EXP: ", exp.getMessage());
                        }
                        break;
                }
            }

            @Override
            public void onCharacteristicWriteCallback(int i) {
                Log.e("onCharWriteCallback:", "status>> " + i);
            }

            @Override
            public void onIbeaconWriteCallback(boolean b, int i, int i1, String s) {
                Log.e("onIbeaconWriteCallback:", "status>> " + i);
            }

            @Override
            public void onQueryDialModeCallback(boolean b, int i, int i1, int i2) {
                Log.e("onQueryDialModeCbk:", "status>> " + i);
            }

            @Override
            public void onControlDialCallback(boolean b, int i, int i1) {
                Log.e("onControlDialCbk:", "status>> " + i);
            }

            @Override
            public void onSportsTimeCallback(boolean b, String s, int i, int i1) {
                Log.e("onSportsTimeCallback:", "status>> " + i);

            }

            @Override
            public void OnResultSportsModes(boolean b, int i, int i1, int i2, SportsModesInfo sportsModesInfo) {
                Log.e("OnResultSportsModes:", "status>> " + i);
            }

            @Override
            public void OnResultHeartRateHeadset(boolean b, int i, int i1, int i2, HeartRateHeadsetSportModeInfo heartRateHeadsetSportModeInfo) {
                Log.e("OnResultHRateHeadset:", "status>> " + i);
            }

            @Override
            public void OnResultCustomTestStatus(boolean b, int i, CustomTestStatusInfo customTestStatusInfo) {
                Log.e("OnResultCusTestStatus:", "status>> " + i);
            }
        });

        mBluetoothLeService.setTemperatureListener(new TemperatureListener() {
            @Override
            public void onTestResult(TemperatureInfo temperatureInfo) {
                Log.e("temperatureListener", "temperature: " + temperatureInfo.getBodyTemperature() + ", type: " + temperatureInfo.getType());
                try {
                    if (temperatureInfo != null) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("calender", temperatureInfo.getCalendar());
                        //jsonObject.put("type", "" + temperatureInfo.getType());
                        jsonObject.put("inCelsius", "" + GlobalMethods.convertDoubleToCelsiusWithDecimal(temperatureInfo.getBodyTemperature()));
                        jsonObject.put("inFahrenheit", "" + GlobalMethods.getTempIntoFahrenheit(temperatureInfo.getBodyTemperature()));
                        //jsonObject.put("startDate", "" + temperatureInfo.getStartDate()); //yyyyMMddHHmmss
                        jsonObject.put("time", "" + GlobalMethods.convertIntToHHMmSs(temperatureInfo.getSecondTime()));

                        //jsonObject.put("time1", "" + GlobalMethods.convertTimeToHHMm(temperatureInfo.getStartDate()));

                        Log.e("onTestResult", "object: " + jsonObject.toString());
                        //pushEventCallBack(WatchConstants.TEMP_RESULT, jsonObject, WatchConstants.SC_SUCCESS);
                        Log.e("onTestResult", "mUTESQLOperate: " + mUTESQLOperate.toString());
                        if (mUTESQLOperate != null) {
                            mUTESQLOperate.saveTemperature(temperatureInfo);
                            Log.e("onTestResult", "mUTESQLOperate: After save saveTemperature");
                        }
                        pushOtherEventCallBack(WatchConstants.TEMP_RESULT, jsonObject, WatchConstants.SC_SUCCESS);
                    } else {
                        pushOtherEventCallBack(WatchConstants.TEMP_TEST_TIME_OUT, new JSONObject(), WatchConstants.SC_SUCCESS);
                    }
                } catch (Exception exp) {
                    Log.e("onTestResultExp:", exp.getMessage());
                }
            }

            @Override
            public void onSamplingResult(TemperatureInfo temperatureInfo) {

            }
        });//Setting up a temperature test，Sampling data callback

        mBluetoothLeService.setRateCalibrationListener(status -> {
            Log.e("onRateCalibraStatus:", "rateStatus>> " + status);
        });//Set up heart rate calibration monitor

        mBluetoothLeService.setTurnWristCalibrationListener(status -> {
            Log.e("onTurnWristCalStatus:", "status>> " + status);
        });//Set up wrist watch calibration

        mBluetoothLeService.setOxygenListener(new OxygenRealListener() {
            @Override
            public void onTestResult(int status, OxygenInfo oxygenInfo) {
                Log.e("oxygenListener", "status: " + status);
                try {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        if (oxygenInfo != null) {
                            Log.e("oxygenListener", "value: " + oxygenInfo.getOxygenValue() + ", status: " + status);

                            jsonObject.put("calender", oxygenInfo.getCalendar());
                            jsonObject.put("value", "" + oxygenInfo.getOxygenValue());
                            jsonObject.put("startDate", "" + oxygenInfo.getStartDate()); //yyyyMMddHHmmss
                            jsonObject.put("time", "" + GlobalMethods.getTimeByIntegerMin(oxygenInfo.getTime()));

                            pushOtherEventCallBack(WatchConstants.OXYGEN_RESULT, jsonObject, WatchConstants.SC_SUCCESS);
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                        Log.e("oxygenListenerJSONExp::", e.getMessage());
                    }

                } catch (Exception exp) {
                    Log.e("oxygenListenerExp::", exp.getMessage());
                }

            }
        });//Oxygen Listener

        mBluetoothLeService.setBreatheRealListener((status, breatheInfo) -> {
            Log.e("setBreatheRealListener", "value: " + breatheInfo.getBreatheValue() + ", status: " + status);
        });//Breathe Listener

//        mBluetoothLeService.setOnlineDialListener(status -> {
//            Log.e("setOnlineDialListener", "status: " + status);
//            if (OnlineDialUtil.getInstance().getDialStatus() == OnlineDialUtil.DialStatus.RegularDial) {
//                Log.e("setOnlineDialListener", "onlineDialStatus  status =" + status);
//                switch (status) {
//                    case OnlineDialUtil.READ_DEVICE_ONLINE_DIAL_CONFIGURATION_OK:
//
//                        break;
//                    case OnlineDialUtil.PREPARE_SEND_ONLINE_DIAL_DATA:
//                        Log.e("setOnlineDialListener","Prepare to send watch face data");
//                      /* mWriteCommand.setWatchSyncProgressListener(new WatchSyncProgressListener() {
//                           @Override
//                           public void WatchSyncProgress(int i) {
//
//                           }
//                       });*/
//                    /*   if (mUIFile != null) {
//                           OnlineDialDataTask task = new OnlineDialDataTask();
//                           task.execute();
//                       }*/
//
//                        break;
//                    case OnlineDialUtil.SEND_ONLINE_DIAL_SUCCESS:
//                        break;
//                    case OnlineDialUtil.SEND_ONLINE_DIAL_CRC_FAIL:
//                        break;
//                    case OnlineDialUtil.SEND_ONLINE_DIAL_DATA_TOO_LARGE:
//                        break;
//                }
//            }
//        });

        Log.e("inside_service_result", "listeners" + mBluetoothLeService);
    }

    private void updatePasswordStatus() {
        uiThreadHandler.postDelayed(() -> {
            //Log.e("mWriteCommand", mWriteCommand.toString());
            if (mWriteCommand != null) {
                mWriteCommand.sendToQueryPasswardStatus();
            }
        }, RETURN_DELAY_MS);
    }

    private void handleMethodCall(MethodCall call, Result result) {
        String method = call.method;
        switch (method) {
//            case WatchConstants.START_LISTENING:
//                //initDeviceConnection(result);
//                startListening(call.arguments, result);
//                break;
//            case WatchConstants.BLE_RE_INITIALIZE:
//                bleReInitialize(result);
//                break;
            case WatchConstants.ANDROID_DEVICE_SDK_INT:
                result.success(Build.VERSION.SDK_INT);
                break;
            case WatchConstants.DEVICE_RE_INITIATE:
                initReInitialize(result);
                break;
            case WatchConstants.DEVICE_INITIALIZE:
                initDeviceConnection(result);
                break;
            case WatchConstants.START_DEVICE_SEARCH:
                searchForBTDevices(result);
                break;
           /* case WatchConstants.STOP_DEVICE_SEARCH:
                String resultStatus = mobileConnect.stopDevicesScan();
                result.success(resultStatus);
                break;*/
            case WatchConstants.GET_LAST_DEVICE_ADDRESS:
                String lastConnectAddress = SPUtil.getInstance(mContext).getLastConnectDeviceAddress();
                result.success(lastConnectAddress);
                break;

            case WatchConstants.CONNECT_LAST_DEVICE:
                String lastAddress = SPUtil.getInstance(mContext).getLastConnectDeviceAddress();
                boolean connectResult = mobileConnect.getBLEServiceOperate().connect(lastAddress);
                result.success(connectResult);
                break;
            case WatchConstants.CLEAR_GATT_DISCONNECT:
                boolean clearGatt = mobileConnect.clearGattDisconnect();
                result.success(clearGatt);
                break;

            case WatchConstants.CHECK_FIND_BAND:
                boolean findAvailable = GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_BAND_FIND_PHONE_FUNCTION);
                result.success(findAvailable);
                break;

            case WatchConstants.FIND_BAND_DEVICE:
                findBandDevice(result);
                break;

            case WatchConstants.RESET_DEVICE_DATA:
                deleteDevicesAllData(result);
                break;

            case WatchConstants.CHECK_DIAL_SUPPORT:
                boolean dialSupport = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_ONLINE_DIAL);
                result.success(dialSupport);
                break;

//            case WatchConstants.GET_DEVICE_DATA_INFO:
//                fetchDeviceDataInfo(result);
//                break;

            case WatchConstants.READ_ONLINE_DIAL_CONFIG:
                readOnlineDialConfig(result);
                break;

            case WatchConstants.PREPARE_SEND_ONLINE_DIAL:
                prepareSendOnlineDialData(result);
                break;

//            case WatchConstants.LISTEN_WATCH_DIAL_PROGRESS:
//                listenWatchDialProgress(result);
//                break;

            case WatchConstants.SEND_ONLINE_DIAL_DATA:
                sendOnlineDialData(call, result);
                break;

            case WatchConstants.SEND_ONLINE_DIAL_PATH:
                sendOnlineDialPath(call, result);
                break;

            case WatchConstants.STOP_ONLINE_DIAL_DATA:
                stopOnlineDialData(result);
                break;


            case WatchConstants.BIND_DEVICE:
                connectBluDevice(call, result);
                break;
            case WatchConstants.UNBIND_DEVICE:
                disconnectBluDevice(result);
                break;
            case WatchConstants.SET_USER_PARAMS:
                setUserParams(call, result);
                break;


            case WatchConstants.SET_DO_NOT_DISTURB:
                setDoNotDisturb(call, result);
                break;

            case WatchConstants.SET_REJECT_CALL:
                setRejectIncomingCall(call, result);
                break;

            case WatchConstants.SET_24_HEART_RATE:
                set24HeartRate(call, result);
                break;
            case WatchConstants.SET_24_OXYGEN:
                set24BloodOxygen(call, result);
                break;
            case WatchConstants.SET_24_TEMPERATURE_TEST:
                set24HrTemperatureTest(call, result);
                break;
            case WatchConstants.SET_WEATHER_INFO:
                setSevenDaysWeatherInfo(call, result);
                break;

            case WatchConstants.SET_BAND_LANGUAGE:
                setDeviceBandLanguage(call, result);
                break;

            case WatchConstants.GET_DEVICE_VERSION:
                getDeviceVersion(result);
                break;


            case WatchConstants.GET_DEVICE_BATTERY_STATUS:
                getDeviceBatteryStatus(result);
                break;

            case WatchConstants.CHECK_CONNECTION_STATUS:
                getCheckConnectionStatus(result);
                break;

            case WatchConstants.CHECK_QUICK_SWITCH_SETTING:
                callCheckQuickSwitchStatus(result);
                break;

            // sync all the data,from watch to the local (android SDK)
            case WatchConstants.SYNC_ALL_JUDGE:
                fetchAllJudgement(result);
                break;

            case WatchConstants.GET_SYNC_STEPS:
                syncAllStepsData(result);
                break;
            case WatchConstants.GET_SYNC_SLEEP:
                syncAllSleepData(result);
                break;
            case WatchConstants.GET_SYNC_RATE:
                syncRateData(result);
                break;
            case WatchConstants.GET_SYNC_BP:
                syncBloodPressure(result);
                break;
            case WatchConstants.GET_SYNC_OXYGEN:
                syncOxygenSaturation(result);
                break;
            case WatchConstants.GET_SYNC_TEMPERATURE:
                syncBodyTemperature(result);
                break;
            //start doing test here
            case WatchConstants.START_BP_TEST:
                startBloodPressure(result);
                break;
            case WatchConstants.STOP_BP_TEST:
                stopBloodPressure(result);
                break;
//            case WatchConstants.START_HR_TEST:
//                startHeartRate(result);
//                break;
//            case WatchConstants.STOP_HR_TEST:
//                stopHeartRate(result);
//                break;
            case WatchConstants.START_OXYGEN_TEST:
                startOxygenSaturation(result);
                break;
            case WatchConstants.STOP_OXYGEN_TEST:
                stopOxygenSaturation(result);
                break;

            case WatchConstants.START_TEST_TEMP:
                startTempTest(result);
                break;

            //fetch individual data
            case WatchConstants.FETCH_OVERALL_BY_DATE:
                fetchOverAllBySelectedDate(call, result);
                break;

            case WatchConstants.FETCH_STEPS_BY_DATE:
                fetchStepsBySelectedDate(call, result);
                break;
            case WatchConstants.FETCH_SLEEP_BY_DATE:
                fetchSleepByDate(call, result);
                break;
            case WatchConstants.FETCH_BP_BY_DATE:
                fetchBPByDate(call, result);
                break;
            case WatchConstants.FETCH_HR_BY_DATE:
                fetchHRByDate(call, result);
                break;

            case WatchConstants.FETCH_OXYGEN_BY_DATE:
                fetchOxygenByDate(call, result);
                break;

            case WatchConstants.FETCH_24_HOUR_HR_BY_DATE:
                fetch24HourHRDateByDate(call, result);
                break;
            case WatchConstants.FETCH_TEMP_BY_DATE:
                fetchTemperatureByDate(call, result);
                break;

            // fetch all the data
            case WatchConstants.FETCH_OVERALL_DEVICE_DATA:
                fetchOverAllDeviceData(result);
                break;
            case WatchConstants.FETCH_ALL_STEPS_DATA:
                fetchAllStepsData(result);
                break;
            case WatchConstants.FETCH_ALL_SLEEP_DATA:
                fetchAllSleepData(result);
                break;
            case WatchConstants.FETCH_ALL_BP_DATA:
                fetchAllBPData(result);
                break;
            case WatchConstants.FETCH_ALL_TEMP_DATA:
                fetchAllTemperatureData(result);
                break;
            case WatchConstants.FETCH_ALL_HR_24_DATA:
                fetchAllHeartRate24Data(result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    /* private void bleReInitialize(Result result) {
     try {
         new Handler().postDelayed(() -> {
             mobileConnect.getBluetoothLeService().initialize();
             result.success(WatchConstants.SC_BLE_RE_INIT);
         }, 1000);

     } catch (Exception exp) {
         Log.e("bleReInitializeExp::", "" + exp.getMessage());
     }
 }*/
    private void initReInitialize(Result result) {
        try {
            //mUTESQLOperate = UTESQLOperate.getInstance(mContext.getApplicationContext());
            mobileConnect = new MobileConnect(mContext.getApplicationContext());
            bleServiceOperate = mobileConnect.getBLEServiceOperate();
            bleServiceOperate.setServiceStatusCallback(new ServiceStatusCallback() {
                @Override
                public void OnServiceStatuslt(int status) {
                    if (status == ICallbackStatus.BLE_SERVICE_START_OK) {
                        Log.e("inside_service_result", "" + mBluetoothLeService);
                        if (mBluetoothLeService == null) {
                            startListeningCallback(true);
                        }
                    }
                }
            });

            mBluetoothLeService = bleServiceOperate.getBleService();
            if (mBluetoothLeService != null) {
                startListeningCallback(false);
            }

//            mWriteCommand = WriteCommandToBLE.getInstance(mContext.getApplicationContext());
//            mDataProcessing = DataProcessing.getInstance(mContext.getApplicationContext());

            startListeningDataProcessing();
//            new Handler().postDelayed(() -> {
//                result.success(WatchConstants.SC_RE_INIT);
//            }, 1000);

            uiThreadHandler.postDelayed(() -> result.success(WatchConstants.SC_RE_INIT), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("initReInitialize::", "" + exp.getMessage());
        }
    }

    private void initDeviceConnection(Result result) {
        // this.flutterInitResultBlu = result;
        boolean isException = false;
        // String[] multiplePermission = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_PRIVILEGED, Manifest.permission.BLUETOOTH_ADVERTISE};
        // String[] multiplePermission = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        try {
            if (mobileConnect != null) {
                //  boolean initStatus = initializeData();
                //mobileConnect = new MobileConnect(this.mContext.getApplicationContext(), activity);
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!checkPermissionEnabled(Manifest.permission.BLUETOOTH_CONNECT)) {
                        //permissionLauncher.launch(multiplePermission);
                        ActivityCompat.requestPermissions(activity, multiplePermission, REQUEST_BLE_ENABLE);
                    }
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }*/
                boolean blu4Enabled = mobileConnect.checkBlu4();
                Log.e("blu4Enabled:", "" + blu4Enabled);
                if (blu4Enabled) {
                    String resultStatus = mobileConnect.startListeners();
                    boolean enable = mobileConnect.isBleEnabled();
                    Log.e("device_enable:", "" + enable);
                    if (enable) {
                        boolean connectionStatus = SPUtil.getInstance(mContext).getBleConnectStatus();
                        Log.e("connectionStatus:", "" + connectionStatus);
                        //result.success(resultStatus);
                       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (!checkPermissionEnabled(Manifest.permission.BLUETOOTH_SCAN)) {
                                //permissionLauncher.launch(multiplePermission);
                                ActivityCompat.requestPermissions(activity, multiplePermission, REQUEST_BLE_ENABLE);
                            }
                        }*/
                        uiThreadHandler.postDelayed(() -> {
                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (bluetoothAdapter != null) {
                                Log.e("blueAdapter_status11:", "" + bluetoothAdapter.isEnabled());
                                /*if (!bluetoothAdapter.isEnabled()) {
                                    bluetoothAdapter.enable();
                                }*/
                            }
                            result.success(resultStatus);
                        }, RETURN_DELAY_MS);

                    } else {
                        // turn on bluetooth
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (!checkPermissionEnabled(Manifest.permission.BLUETOOTH_SCAN)) {
                                //permissionLauncher.launch(multiplePermission);
                                ActivityCompat.requestPermissions(activity, multiplePermission, REQUEST_BLE_ENABLE);
                            }
                            try {
                                BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
                                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                                if (bluetoothAdapter != null) {
                                    Log.e("blueAdapter_status1:", "" + bluetoothAdapter.isEnabled());
                                    if (!bluetoothAdapter.isEnabled()) {
                                        bluetoothAdapter.enable();
                                    }
                                }
                            } catch (Exception exp) {
                                Log.e("blue_service_exp", exp.getMessage());
                                isException = true;
                            }
                        } else {*/
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                        }
                        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        //}
                        boolean finalIsException = isException;
                        uiThreadHandler.postDelayed(() -> {
                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (bluetoothAdapter != null) {
                                Log.e("blueAdapter_status2:", "" + bluetoothAdapter.isEnabled());
                                /*if (!bluetoothAdapter.isEnabled()) {
                                    bluetoothAdapter.enable();
                                }*/
                            }
                            if (finalIsException) {
                                result.success(WatchConstants.BLE_NOT_ENABLED);
                            } else {
                                result.success(resultStatus);
                            }

                        }, RETURN_DELAY_MS);
                    }
                } else {
                    result.success(WatchConstants.BLE_NOT_SUPPORTED);
                }
                /*boolean enable = mobileConnect.isBleEnabled();
                boolean blu4 = mobileConnect.checkBlu4();
                boolean connectionStatus = SPUtil.getInstance(mContext).getBleConnectStatus();
                String resultStatus = mobileConnect.startListeners();
                Log.e("device_enable:", "" + enable);
                Log.e("device_blu4e:", "" + blu4);
                Log.e("connectionStatus:", "" + connectionStatus);
                if (!connectionStatus){
                    // connection_status == false then BluetoothAdapter not initialized
                }else{

                }
                if (enable) {
                    if (blu4) {
                        //String resultStatus = mobileConnect.startListeners();
                        Log.e("init_res_status", "" + resultStatus);
                        result.success(resultStatus);
                    } else {
                        result.success(WatchConstants.BLE_NOT_SUPPORTED);
                    }
                } else {

                    //String resultStatus = mobileConnect.startListeners();
                    Log.e("else_res_status", "" + resultStatus);
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    result.success(resultStatus);
                }*/
            } else {
                Log.e("device_connect_err:", "device_connect not initiated..");
            }
        } catch (Exception exp) {
            Log.e("initDeviceExp::", "" + exp.getMessage());
        }
    }

    private void searchForBTDevices(Result result) {
        try {
            JSONObject jsonObject = new JSONObject();
            if (mobileConnect != null) {
                /*mBluetoothLeService = mobileConnect.getBluetoothLeService();
                if (mBluetoothLeService != null) {
                    //initBlueServices(status);
                    initBlueServices();
                }*/
                uiThreadHandler.postDelayed(() -> {
                    String resultStat = mobileConnect.stopDevicesScan();
                    Log.e("resultStat", "deviceScanStop::" + resultStat);
                    ArrayList<BleDevices> bleDeviceList = mobileConnect.getDevicesList();
                    JSONArray jsonArray = new JSONArray();
                    for (BleDevices device : bleDeviceList) {
                        Log.e("device_for", "device::" + device.getName());
                        try {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("name", device.getName());
                            jsonObj.put("address", device.getAddress());
                            // jsonObj.put("rssi", device.getRssi());
                            // jsonObj.put("deviceType", device.getDeviceType());
                            // jsonObj.put("bondState", device.getBondState());
                            // jsonObj.put("alias", device.getAlias());
                            jsonArray.put(jsonObj);
                        } catch (Exception e) {
                            //  e.printStackTrace();
                            Log.e("jsonExp::", "jsonParse::" + e.getMessage());
                        }
                    }
                    // mDevices = mLeDevices;
//                    Type listType = new TypeToken<ArrayList<BleDevices>>() {
//                    }.getType();
//                    String jsonString = new Gson().toJson(bleDeviceList, listType);
//                    JsonArray jsonArray2 = new Gson().toJsonTree(bleDeviceList, listType).getAsJsonArray();
                    Log.e("jsonString ", "jsonString::" + jsonArray.toString());
                    try {
                        jsonObject.put("status", WatchConstants.SC_SUCCESS);
                        jsonObject.put("data", jsonArray);
                        Log.e("jsonStringList", jsonObject.toString());
                        result.success(jsonObject.toString());
                    } catch (Exception e) {
                        //e.printStackTrace();
                        Log.e("searchForBTExp2::", e.getMessage());
                    }
                    //       Gson gson = new Gson();
                    //       String jsonOutput = "Your JSON String";
                    //       Type listType = new TypeToken<List<Post>>(){}.getType();
                    //       List<Post> posts = gson.fromJson(jsonOutput, listType);
                    // convert into the json and send back as a response to flutter sdk
                }, 7000);
                String resultStatus = mobileConnect.startDevicesScan();
                Log.e("startStatus", resultStatus);
            } else {
                try {
                    jsonObject.put("status", WatchConstants.SC_CANCELED); // not connected
                    jsonObject.put("data", new JSONArray());
                } catch (Exception e) {
                    //e.printStackTrace();
                    Log.e("jsonExp123::", "jsonParse::" + e.getMessage());
                }
                result.success(jsonObject.toString());
            }
        } catch (Exception exp) {
            Log.e("searchForBTExp1::", exp.getMessage());
        }
    }

    private void connectBluDevice(MethodCall call, Result result) {
        try {
            String address = call.argument("address");
            boolean status = mobileConnect.connectDevice(address);
            result.success(status);
        } catch (Exception exp) {
            Log.e("connectBluDeviceExp:", exp.getMessage());
        }
        //String index = (String) call.argument("index");
        //String name = call.argument("name");
        //String alias = call.argument("alias");

        //String deviceType = call.argument("deviceType");
        // String rssi = call.argument("rssi");
        // String bondState = call.argument("bondState");

           /* if (mBluetoothLeService != null) {
                mBluetoothLeService.readRssi();
            }*/
//                    mBluetoothLeService = mobileConnect.getBluetoothLeService();
//
//                    if (mBluetoothLeService != null) {
//                        initBlueServices(status);
//                    }

    }

    private void updateReadRSSIThread() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (mBluetoothLeService != null) {
                    mBluetoothLeService.readRssi();
                    //mBluetoothLeService.initialize();
                }
            }
        }).start();
    }

    private void disconnectBluDevice(Result result) {
        try {
            boolean status = mobileConnect.disconnectDevice();
            result.success(status);
//            int value = 9;
//            byte var3 = (byte)((value & '\uff00') >> 8);
//            byte var4 = (byte)(value & 255);
//            Log.e("var3:", ""+var3);
//            Log.e("var4:", ""+var4);
        } catch (Exception exp) {
            Log.e("disconnectBluDeviceExp:", exp.getMessage());
        }
    }

    private void setUserParams(MethodCall call, Result result) {
        try {
            String age = call.argument("age");
            String height = call.argument("height");
            String weight = call.argument("weight");
            String gender = call.argument("gender");
            String steps = call.argument("steps");
            String isCel = call.argument("isCelsius");
            String screenOffTime = call.argument("screenOffTime");
            String isChineseLang = call.argument("isChineseLang");
            String raiseHandWakeUp = call.argument("raiseHandWakeUp");


            assert age != null;
            int bodyAge = Integer.parseInt(age);
            assert height != null;
            int bodyHeight = Integer.parseInt(height);
            assert weight != null;
            int bodyWeight = Integer.parseInt(weight);
            assert steps != null;
            int bodySteps = Integer.parseInt(steps);

            assert screenOffTime != null;
            int setScreenOffTime = Integer.parseInt(screenOffTime);

            boolean isMale = false;
            assert gender != null;
            if (gender.trim().equalsIgnoreCase("male")) {
                isMale = true;
            }
            boolean isCelsius = false;
            assert isCel != null;
            if (isCel.equalsIgnoreCase("true")) {
                isCelsius = true;
            }

            boolean isChinese = false;
            assert isChineseLang != null;
            if (isChineseLang.equalsIgnoreCase("true")) {
                isChinese = true;
            }

            boolean isRaiseHandWakeUp = false;
            assert raiseHandWakeUp != null;
            if (raiseHandWakeUp.equalsIgnoreCase("true")) {
                isRaiseHandWakeUp = true;
            }

            boolean isHighestRateOpen = true;  // Maximum heart rate warning on off, true is on, false to close
            boolean isLowestRateOpen = true;
            boolean bandLostOpen = true;
            //int highestHeartRate = 120;
            //int lowestHeartRate = 40;

            // boolean isSupported = GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_NEW_PARAMETER_SETTINGS_FUNCTION);
            // Log.e("isSupported::", "isSupported>>" + isSupported);

            // boolean isSupp = GetFunctionList.isSupportFunction(mContext, GlobalVariable.IS_SUPPORT_NEW_PARAMETER_SETTINGS_FUNCTION);
            Log.e("isCelsius::", "isCelsius>>" + isCelsius);

            //if (isSupported) {
            DeviceParametersInfo info = new DeviceParametersInfo();
            info.setBodyAge(bodyAge);
            info.setBodyHeight(bodyHeight);
            info.setBodyWeight(bodyWeight);
            info.setStepTask(bodySteps);
            info.setBodyGender(isMale ? DeviceParametersInfo.switchStatusYes : DeviceParametersInfo.switchStatusNo);
            info.setCelsiusFahrenheitValue(isCelsius ? GlobalVariable.TMP_UNIT_CELSIUS : GlobalVariable.TMP_UNIT_FAHRENHEIT);
            info.setOffScreenTime(setScreenOffTime);
            info.setOnlySupportEnCn(isChinese ? DeviceParametersInfo.switchStatusYes : DeviceParametersInfo.switchStatusNo);  // no for english, yes for chinese
            info.setRaisHandbrightScreenSwitch(isRaiseHandWakeUp ? DeviceParametersInfo.switchStatusYes : DeviceParametersInfo.switchStatusNo);  // true if bright light turn on

            info.setHighestRateAndSwitch(GlobalVariable.HEART_RATE_FILTER_MAX_VALUE, DeviceParametersInfo.switchStatusYes);
            info.setLowestRateAndSwitch(GlobalVariable.HEART_RATE_FILTER_MIN_VALUE, DeviceParametersInfo.switchStatusYes);

            //info.setRaisHandbrightScreenSwitch(DeviceParametersInfo.switchStatusYes);
            //info.setHighestRateAndSwitch(141, DeviceParametersInfo.switchStatusYes);
            //info.setDeviceLostSwitch(DeviceParametersInfo.switchStatusNo);
            String resultant;
            if (mWriteCommand != null) {
                boolean isSupported = GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_NEW_PARAMETER_SETTINGS_FUNCTION);
                Log.e("isNewParamsSupported::", "isSupported>>" + isSupported);
                if (isSupported) {
                    mWriteCommand.sendDeviceParametersInfoToBLE(info);
                } else {
                    int temperature = isCelsius ? GlobalVariable.TMP_UNIT_CELSIUS : GlobalVariable.TMP_UNIT_FAHRENHEIT;
                    mWriteCommand.sendStepLenAndWeightToBLE(bodyHeight, bodyWeight, setScreenOffTime, bodySteps, isRaiseHandWakeUp,
                            isHighestRateOpen, GlobalVariable.HEART_RATE_FILTER_MAX_VALUE, isMale, bodyAge, bandLostOpen,
                            isLowestRateOpen, GlobalVariable.HEART_RATE_FILTER_MIN_VALUE, temperature, isChinese);
                }
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                // result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
//        } else {
//            result.success(WatchConstants.SC_NOT_SUPPORTED);
//        }

        } catch (Exception exp) {
            Log.e("setUserParamsExp::", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void readOnlineDialConfig(Result result) {
        try {
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {

                    String resultant;
                    boolean dialSupport = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_ONLINE_DIAL);
                    if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
                        //OnlineDialUtil.getInstance().setDialStatus(OnlineDialUtil.DialStatus.RegularDial);
                        if (mWriteCommand != null) {
                            BluetoothLeService mBluetoothLeService = BLEServiceOperate.getInstance(mContext).getBleService();
                            if (mBluetoothLeService != null) {
                                OnlineDialUtil.getInstance().setDialStatus(OnlineDialUtil.DialStatus.RegularDial);
                                JSONObject jsonObject = new JSONObject();
                                mBluetoothLeService.setOnlineDialListener(new OnlineDialListener() {
                                    @Override
                                    public void onlineDialStatus(int status) {
                                        Log.e("onlineDialStatus", "status: " + status);
                                        if (OnlineDialUtil.getInstance().getDialStatus() == OnlineDialUtil.DialStatus.RegularDial) {
                                            Log.e("setOnlineDialListener", "onlineDialStatus  status =" + status);
                                            switch (status) {
                                                case OnlineDialUtil.READ_DEVICE_ONLINE_DIAL_CONFIGURATION_OK:
                                                    Log.e("setOnlineDialListener", "READ_DEVICE_ONLINE_DIAL_CONFIGURATION_OK");

                                                    String bleName = BLEVersionUtils.getInstance(mContext).getBleVersionName();
                                                    String mac = BLEVersionUtils.getInstance(mContext).getBleMac();
                                                    String dpi = SPUtil.getInstance(mContext).getResolutionWidthHeight();
                                                    String maxCapacity = SPUtil.getInstance(mContext).getDialMaxDataSize();
                                                    int shape = SPUtil.getInstance(mContext).getDialScreenType();
                                                    int compatibleLevel = SPUtil.getInstance(mContext).getDialScreenCompatibleLevel();

                                                    try {
                                                        jsonObject.put("dialSupport", dialSupport);
                                                        jsonObject.put("status", WatchConstants.SC_SUCCESS);
                                                        jsonObject.put("bleName", bleName);
                                                        jsonObject.put("mac", mac);
                                                        jsonObject.put("dpi", dpi);
                                                        jsonObject.put("maxCapacity", maxCapacity);
                                                        jsonObject.put("type", 0);
                                                        jsonObject.put("shape", "" + shape);
                                                        jsonObject.put("compatible", "" + compatibleLevel);

                                                        pushOtherEventCallBack(WatchConstants.GET_DEVICE_DATA_INFO, jsonObject, WatchConstants.SC_SUCCESS);
                                                        // do api call here
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                        pushOtherEventCallBack(WatchConstants.GET_DEVICE_DATA_INFO, jsonObject, WatchConstants.SC_FAILURE);
                                                    }
                                                    break;
                                                case OnlineDialUtil.PREPARE_SEND_ONLINE_DIAL_DATA:
                                                    Log.e("setOnlineDialListener", "Prepare to send watch face data");
                                                    Log.e("setOnlineDialListener", "PREPARE_SEND_ONLINE_DIAL_DATA");
                                                    pushOtherEventCallBack(WatchConstants.PREPARE_SEND_ONLINE_DIAL, jsonObject, WatchConstants.SC_SUCCESS);
                                                    // read the file in byte
                                                    // check ble connect status
                                                    // call sendOnlineDialData

                                          /* mWriteCommand.setWatchSyncProgressListener(new WatchSyncProgressListener() {
                                               @Override
                                               public void WatchSyncProgress(int i) {

                                               }
                                           });*/
                                        /*   if (mUIFile != null) {
                                               OnlineDialDataTask task = new OnlineDialDataTask();
                                               task.execute();
                                           }*/
                                                    break;
                                                case OnlineDialUtil.SEND_ONLINE_DIAL_SUCCESS:
                                                    Log.e("setOnlineDialListener", "SEND_ONLINE_DIAL_SUCCESS");
                                                    pushOtherEventCallBack(WatchConstants.SEND_ONLINE_DIAL_SUCCESS, jsonObject, WatchConstants.SC_SUCCESS);
                                                    break;
                                                case OnlineDialUtil.SEND_ONLINE_DIAL_CRC_FAIL:
                                                    Log.e("setOnlineDialListener", "SEND_ONLINE_DIAL_CRC_FAIL");
                                                    pushOtherEventCallBack(WatchConstants.SEND_ONLINE_DIAL_FAIL, jsonObject, WatchConstants.SC_SUCCESS);
                                                    break;
                                                case OnlineDialUtil.SEND_ONLINE_DIAL_DATA_TOO_LARGE:
                                                    Log.e("setOnlineDialListener", "SEND_ONLINE_DIAL_DATA_TOO_LARGE");
                                                    pushOtherEventCallBack(WatchConstants.SEND_ONLINE_DIAL_LARGE, jsonObject, WatchConstants.SC_SUCCESS);
                                                    break;
                                            }
                                        }
                                    }
                                });
                            }
                            mWriteCommand.readDeviceOnlineDialConfiguration();
                            //WriteCommandToBLE.getInstance(mContext).readDeviceOnlineDialConfiguration();
                            //result.success(WatchConstants.SC_INIT);
                            resultant = WatchConstants.SC_INIT;
                        } else {
                            //result.success(WatchConstants.SC_FAILURE);
                            resultant = WatchConstants.SC_FAILURE;
                        }
                        //uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
                    } else {
                        resultant = WatchConstants.SC_DISCONNECTED;
                    }
                    result.success(resultant);
                }
            });
        } catch (Exception exp) {
            Log.e("readOnDialConfigExp::", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void prepareSendOnlineDialData(Result result) {
        try {
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    String resultant;
                    final int[] tempProgress = {0};
                    if (mWriteCommand != null) {
                        mWriteCommand.prepareSendOnlineDialData();
                        //result.success(WatchConstants.SC_INIT);
                        mWriteCommand.setWatchSyncProgressListener(progress -> {
                            Log.e("WatchSyncProgress::", "" + progress);
                            try {
                                if (progress == 0) {
                                    tempProgress[0] = progress;
                                }
                                if (tempProgress[0] != progress) {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("progress", progress);
                                    tempProgress[0] = progress;
                                    pushDialFaceProgressCallBack(jsonObject, progress);
                                }
                            } catch (Exception exp) {
                                Log.e("listenWatchJsonExp::", exp.getMessage());
                                //e.printStackTrace();
                                tempProgress[0] = 0;
                            }
                        });
                        resultant = WatchConstants.SC_INIT;
                    } else {
                        resultant = WatchConstants.SC_FAILURE;
                    }
                    result.success(resultant);
                }
            });
            /*Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                @Override
                public void run() {
                    String resultant;
                    final int[] tempProgress = {0};
                    if (mWriteCommand != null) {
                        mWriteCommand.prepareSendOnlineDialData();
                        //result.success(WatchConstants.SC_INIT);
                        mWriteCommand.setWatchSyncProgressListener(progress -> {
                            Log.e("WatchSyncProgress::", "" + progress);
                            try {
                                if (tempProgress[0] != progress) {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("progress", progress);
                                    tempProgress[0] = progress;
                                    pushDialFaceProgressCallBack(jsonObject, progress);
                                }
                            } catch (Exception exp) {
                                Log.e("listenWatchJsonExp::", exp.getMessage());
                                //e.printStackTrace();
                                tempProgress[0] = 0;
                            }
                        });

                        //isBandFaceRunning = true;
                        resultant = WatchConstants.SC_INIT;
                    } else {
                        //result.success(WatchConstants.SC_FAILURE);
                        resultant = WatchConstants.SC_FAILURE;
                    }
                    result.success(resultant);
                }
            }, 300, TimeUnit.MILLISECONDS);*/
            //           uiThreadHandler.post(() -> {
//                String resultant;
//                final int[] tempProgress = {0};
//                if (mWriteCommand != null) {
//                    mWriteCommand.prepareSendOnlineDialData();
//                    //result.success(WatchConstants.SC_INIT);
//                    mWriteCommand.setWatchSyncProgressListener(progress -> {
//                        Log.e("WatchSyncProgress::", "" + progress);
//                        try {
//                            if (tempProgress[0] != progress) {
//                                JSONObject jsonObject = new JSONObject();
//                                jsonObject.put("progress", progress);
//                                tempProgress[0] = progress;
//                                pushDialFaceProgressCallBack(jsonObject);
//                            }
//                        } catch (Exception exp) {
//                            Log.e("listenWatchJsonExp::", exp.getMessage());
//                            //e.printStackTrace();
//                            tempProgress[0] = 0;
//                        }
//                    });

            //isBandFaceRunning = true;
//                    resultant = WatchConstants.SC_INIT;
//                } else {
//                    //result.success(WatchConstants.SC_FAILURE);
//                    resultant = WatchConstants.SC_FAILURE;
//                }
//                result.success(resultant);
            //           });
//            String resultant;
//            if (mWriteCommand != null) {
//                mWriteCommand.prepareSendOnlineDialData();
//                //result.success(WatchConstants.SC_INIT);
//                resultant = WatchConstants.SC_INIT;
//            } else {
//                //result.success(WatchConstants.SC_FAILURE);
//                resultant = WatchConstants.SC_FAILURE;
//            }
//            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("prepareSendDialExp::", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }
    }

   /* private void listenWatchDialProgress(Result result) {
        try {
            String resultant;
            final int[] tempProgress = {0};
            if (mWriteCommand != null) {
                mWriteCommand.setWatchSyncProgressListener(new WatchSyncProgressListener() {
                    @Override
                    public void WatchSyncProgress(int progress) {
                        Log.e("WatchSyncProgress::", "" + progress);
                        try {
                            if (tempProgress[0] != progress) {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("progress", progress);
                                tempProgress[0] = progress;
                                pushDialFaceProgressCallBack(jsonObject, progress);
                            }
                        } catch (Exception exp) {
                            Log.e("listenWatchJsonExp::", exp.getMessage());
                            //e.printStackTrace();
                            tempProgress[0] = 0;
                        }
                    }
                });
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            Log.e("resultant::", "listenWatchDialProgress>>" + resultant);
            result.success(resultant);
            // uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("listenWatchDialExp::", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }
    }*/

    private void sendOnlineDialPath(MethodCall call, Result result) {
        try {
            String downloadPath = call.argument("path");
            Log.e("sendOnlineDialPath::", "downloadPath>>" + downloadPath);
//            Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
//                @Override
//                public void run() {
//                   byte[] data = Rgb.getInstance().readBinToByte(mContext, downloadPath);
//                    if (data != null && data.length > 0) {
//                        if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
//                            if (mWriteCommand != null) {
//                                mWriteCommand.sendOnlineDialData(data);
//                            }
//                            result.success(WatchConstants.SC_INIT);
//                        }else {
//                            result.success(WatchConstants.SC_DISCONNECTED);
//                        }
//                    }else{
//                        result.success(WatchConstants.SC_FAILURE);
//                    }
//                }
//            }, 300, TimeUnit.MILLISECONDS);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //add your code here
                            byte[] data = Rgb.getInstance().readBinToByte(mContext, downloadPath);
                            if (data != null && data.length > 0) {
                                if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
                                    if (mWriteCommand != null) {
                                        mWriteCommand.sendOnlineDialData(data);
                                    }
                                    result.success(WatchConstants.SC_INIT);
                                } else {
                                    result.success(WatchConstants.SC_DISCONNECTED);
                                }
                            } else {
                                result.success(WatchConstants.SC_FAILURE);
                            }
                        }
                    }, 800);
                }
            });
            //           uiThreadHandler.post(new Runnable() {
            //               @Override
            //               public void run() {
            //                   OnlineDialTask task = new OnlineDialTask();
            //                   task.execute(downloadPath);
//                    byte[] data = Rgb.getInstance().readBinToByte(mContext, downloadPath);
//                    if (data != null && data.length > 0) {
//                        Log.e("onPostExecute","data" + data.length);
//                        if (!SPUtil.getInstance(mContext).getBleConnectStatus()) {
//                            return;
//                        }
//                        Log.e("onPostExecute","sendOnlineDialData executed");
//                        //WriteCommandToBLE.getInstance(mContext).sendOnlineDialData(data);
//                        //syncBandFaceData(data);
//                        if (mWriteCommand != null) {
//                            mWriteCommand.sendOnlineDialData(data);
//                        }
            //                       result.success(WatchConstants.SC_INIT);
//                    }else{
//                        result.success(WatchConstants.SC_FAILURE);
//                    }
            //               }
            //           });
            //String resultant;
            //new OnlineDialTask12().execute(downloadPath);
            //uiThreadHandler.postDelayed(() -> result.success(WatchConstants.SC_INIT), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("sendOnlineDialPathExp::", exp.getMessage());
        }
    }

    /*class OnlineDialTask12 extends AsyncTaskExecutorService<String, Void, Boolean> {
        byte[] data = null;
        long rechargeSpan = 0;
        @Override
        protected Boolean doInBackground(String s) {
            rechargeSpan = System.currentTimeMillis();
            String path = s;
            Log.e("OnlineDialTask::", "path>>" + path);
            data = Rgb.getInstance().readBinToByte(mContext, path);
            Log.e("OnlineDialTask::", "data>> "+data);
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (data != null && data.length > 0) {
                Log.e("onPostExecute","data" + data.length);
                if (!SPUtil.getInstance(mContext).getBleConnectStatus()) {
                    return;
                }
                Log.e("onPostExecute","sendOnlineDialData executed");
                WriteCommandToBLE.getInstance(mContext).sendOnlineDialData(data);
            }
        }
    }*/

    class OnlineDialTask extends AsyncTask<String, Void, Boolean> {
        //        String stringData = null;
        byte[] data = null;
        long rechargeSpan = 0;

        @Override
        protected Boolean doInBackground(String... params) {
            rechargeSpan = System.currentTimeMillis();
            //String path = mContext.getExternalFilesDir(null) + "/" + mUIFile.getTitle() + ".bin";
            String path = params[0];
            Log.e("OnlineDialTask::", "path>>" + path);
            data = Rgb.getInstance().readBinToByte(mContext, path);
            Log.e("OnlineDialTask::", "data>> " + Arrays.toString(data));
            if (data != null && data.length > 0) {
                Log.e("onPostExecute", "data" + data.length);
//                if (!SPUtil.getInstance(mContext).getBleConnectStatus()) {
//                    return;
//                }
                Log.e("onPostExecute", "sendOnlineDialData executed");
                //WriteCommandToBLE.getInstance(mContext).sendOnlineDialData(data);
                //syncBandFaceData(data);
                if (mWriteCommand != null) {
                    mWriteCommand.sendOnlineDialData(data);
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            long elapsedTime = System.currentTimeMillis() - rechargeSpan;
            Log.e("onPostExecute", "time consuming" + elapsedTime + " millisecond");
            //20200306 add
//            if (data != null && data.length > 0) {
//                Log.e("onPostExecute","data" + data.length);
//                if (!SPUtil.getInstance(mContext).getBleConnectStatus()) {
//                    return;
//                }
//                Log.e("onPostExecute","sendOnlineDialData executed");
//                //WriteCommandToBLE.getInstance(mContext).sendOnlineDialData(data);
//                //syncBandFaceData(data);
//                if (mWriteCommand != null) {
//                    mWriteCommand.sendOnlineDialData(data);
//                }
//            }
        }
    }

    private void syncBandFaceData(byte[] resultData) {
        try {
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mWriteCommand != null) {
                        mWriteCommand.sendOnlineDialData(resultData);
                    }
                }
            });
            //uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("sendOnlineDialDataExp::", exp.getMessage());
        }
    }

    private void sendOnlineDialData(MethodCall call, Result result) {
        try {
            String resultant;
            byte[] data = call.argument("data");
            if (mWriteCommand != null) {
                mWriteCommand.sendOnlineDialData(data);
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            Log.e("resultant::", "sendOnlineDialData>>" + resultant);
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("sendOnlineDialDataExp::", exp.getMessage());
        }
    }

    private void stopOnlineDialData(Result result) {
        try {
            uiThreadHandler.post(() -> {
                String resultant;
                if (mWriteCommand != null) {
                    mWriteCommand.stopOnlineDialData();
                    //result.success(WatchConstants.SC_INIT);
                    resultant = WatchConstants.SC_INIT;
                } else {
                    //result.success(WatchConstants.SC_FAILURE);
                    resultant = WatchConstants.SC_FAILURE;
                }
                result.success(resultant);
            });
            //uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("stopOnlineDialDataExp::", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }

        // try1
        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
            @Override
            public void run() {

            }
        }, 500, TimeUnit.MILLISECONDS);

        // try2
        Runnable runnable = new Runnable() {
            public void run() {
                // Do something
            }
        };
        worker.schedule(runnable, 2, TimeUnit.SECONDS);

        //try3
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //add your code here
                    }
                }, 1000);
            }
        });

        //try4
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
            }
        }, 100);
    }

    private void findBandDevice(Result result) {
        try {
            String resultant;
            if (mWriteCommand != null) {
                mWriteCommand.findBand(3);
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            Log.e("resultant::", "findBandDevice>>" + resultant);
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("findBandDeviceExp::", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void deleteDevicesAllData(Result result) {
        try {
            String resultant;
            if (mWriteCommand != null) {
                mWriteCommand.deleteDevicesAllData();
                resultant = WatchConstants.SC_INIT;
            } else {
                resultant = WatchConstants.SC_FAILURE;
            }
            Log.e("deleteDataRes:", "deleteDevicesAllData>>" + resultant);
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("deleteDataExp:", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void set24BloodOxygen(MethodCall call, Result result) {
        try {
            String enable = call.argument("enable");
            boolean isEnable = false;
            assert enable != null;
            if (enable.trim().toLowerCase().equalsIgnoreCase("true")) {
                isEnable = true;
            }
            String resultant;
            if (mWriteCommand != null) {
                // 1hr *60
                mWriteCommand.syncOxygenAutomaticTest(isEnable, 60);
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("set24HeartRateExp::", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void set24HeartRate(MethodCall call, Result result) {
        try {
            String enable = call.argument("enable");
            boolean isEnable = false;
            assert enable != null;
            if (enable.trim().toLowerCase().equalsIgnoreCase("true")) {
                isEnable = true;
            }
            String resultant;
            if (mWriteCommand != null) {
                mWriteCommand.open24HourRate(isEnable);
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("set24HeartRateExp::", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void setRejectIncomingCall(MethodCall call, Result result) {
        try {
            String enable = call.argument("enable");
            boolean isEnable = false;
            assert enable != null;
            if (enable.trim().toLowerCase().equalsIgnoreCase("true")) {
                isEnable = true;
            }
            String resultant;
            if (mWriteCommand != null) {
                mWriteCommand.sendEndCallToBle(isEnable);
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("setRejectInCallExp:", exp.getMessage());
        }
    }

    private void setDoNotDisturb(MethodCall call, Result result) {
        try {
            String isMessageOn = call.argument("isMessageOn");
            String isMotorOn = call.argument("isMotorOn");
            String disturbTimeSwitch = call.argument("disturbTimeSwitch");

            String from_time_hour = call.argument("from_time_hour");
            String from_time_minute = call.argument("from_time_minute");

            String to_time_hour = call.argument("to_time_hour");
            String to_time_minute = call.argument("to_time_minute");

            boolean isEnableMessageOn = false;
            boolean isEnableMotorOn = false;
            boolean isDisturbTimeSwitch = false;

            assert isMessageOn != null;
            if (isMessageOn.trim().toLowerCase().equalsIgnoreCase("true")) {
                isEnableMessageOn = true;
            }

            assert isMotorOn != null;
            if (isMotorOn.trim().toLowerCase().equalsIgnoreCase("true")) {
                isEnableMotorOn = true;
            }

            assert disturbTimeSwitch != null;
            if (disturbTimeSwitch.trim().toLowerCase().equalsIgnoreCase("true")) {
                isDisturbTimeSwitch = true;
            }

            String resultant;
            if (mWriteCommand != null) {
                if (isDisturbTimeSwitch) {
                    assert from_time_hour != null;
                    int fromHour = Integer.parseInt(from_time_hour);

                    assert from_time_minute != null;
                    int fromMinute = Integer.parseInt(from_time_minute);

                    assert to_time_hour != null;
                    int toHour = Integer.parseInt(to_time_hour);

                    assert to_time_minute != null;
                    int toMinute = Integer.parseInt(to_time_minute);

                    mWriteCommand.sendDisturbToBle(isEnableMessageOn, isEnableMotorOn, isDisturbTimeSwitch, fromHour, fromMinute, toHour, toMinute);
                } else {
                    mWriteCommand.sendDisturbToBle(isEnableMessageOn, isEnableMotorOn, isDisturbTimeSwitch, 0, 0, 0, 0);
                }

                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("setDoNotDisturbExp::", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void setSevenDaysWeatherInfo(MethodCall call, Result result) {
        try {
            String infoData = call.argument("data");
            assert infoData != null;
            JSONObject jsonObject = new JSONObject(infoData);

            Log.e("jsonObject::", "" + jsonObject);
            SevenDayWeatherInfo sevenDayWeatherInfo = new SevenDayWeatherInfo();

            String cityName = jsonObject.optString("cityName");//max only 4 characters
            sevenDayWeatherInfo.setCityName(cityName);
            //sevenDayWeatherInfo.setCelsiusFahrenheit();

            String todayWeatherCode = jsonObject.optString("todayWeatherCode");
            int todayTmpCurrent = jsonObject.optInt("todayTmpCurrent");
            int todayTmpMax = jsonObject.optInt("todayTmpMax");
            int todayTmpMin = jsonObject.optInt("todayTmpMin");

            //today
            sevenDayWeatherInfo.setTodayWeatherCode(todayWeatherCode);
            sevenDayWeatherInfo.setTodayTmpCurrent(todayTmpCurrent);
            sevenDayWeatherInfo.setTodayTmpMax(todayTmpMax);
            sevenDayWeatherInfo.setTodayTmpMin(todayTmpMin);
            sevenDayWeatherInfo.setTodayPm25(0);
            sevenDayWeatherInfo.setTodayAqi(0);

            String secondDayWeatherCode = jsonObject.optString("secondDayWeatherCode");
            int secondDayTmpMax = jsonObject.optInt("secondDayTmpMax");
            int secondDayTmpMin = jsonObject.optInt("secondDayTmpMin");
            //second
            sevenDayWeatherInfo.setSecondDayWeatherCode(secondDayWeatherCode);
            sevenDayWeatherInfo.setSecondDayTmpMax(secondDayTmpMax);
            sevenDayWeatherInfo.setSecondDayTmpMin(secondDayTmpMin);

            String thirdDayWeatherCode = jsonObject.optString("thirdDayWeatherCode");
            int thirdDayTmpMax = jsonObject.optInt("thirdDayTmpMax");
            int thirdDayTmpMin = jsonObject.optInt("thirdDayTmpMin");
            //third
            sevenDayWeatherInfo.setThirdDayWeatherCode(thirdDayWeatherCode);
            sevenDayWeatherInfo.setThirdDayTmpMax(thirdDayTmpMax);
            sevenDayWeatherInfo.setThirdDayTmpMin(thirdDayTmpMin);

            String fourthDayWeatherCode = jsonObject.optString("fourthDayWeatherCode");
            int fourthDayTmpMax = jsonObject.optInt("fourthDayTmpMax");
            int fourthDayTmpMin = jsonObject.optInt("fourthDayTmpMin");
            //fourth
            sevenDayWeatherInfo.setFourthDayWeatherCode(fourthDayWeatherCode);
            sevenDayWeatherInfo.setFourthDayTmpMax(fourthDayTmpMax);
            sevenDayWeatherInfo.setFourthDayTmpMin(fourthDayTmpMin);

            String fifthDayWeatherCode = jsonObject.optString("fifthDayWeatherCode");
            int fifthDayTmpMax = jsonObject.optInt("fifthDayTmpMax");
            int fifthDayTmpMin = jsonObject.optInt("fifthDayTmpMin");
            //fifth
            sevenDayWeatherInfo.setFifthDayWeatherCode(fifthDayWeatherCode);
            sevenDayWeatherInfo.setFifthDayTmpMax(fifthDayTmpMax);
            sevenDayWeatherInfo.setFifthDayTmpMin(fifthDayTmpMin);

            String sixthDayWeatherCode = jsonObject.optString("sixthDayWeatherCode");
            int sixthDayTmpMax = jsonObject.optInt("sixthDayTmpMax");
            int sixthDayTmpMin = jsonObject.optInt("sixthDayTmpMin");
            //sixth
            sevenDayWeatherInfo.setSixthDayWeatherCode(sixthDayWeatherCode);
            sevenDayWeatherInfo.setSixthDayTmpMax(sixthDayTmpMax);
            sevenDayWeatherInfo.setSixthDayTmpMin(sixthDayTmpMin);

            String seventhDayWeatherCode = jsonObject.optString("seventhDayWeatherCode");
            int seventhDayTmpMax = jsonObject.optInt("seventhDayTmpMax");
            int seventhDayTmpMin = jsonObject.optInt("seventhDayTmpMin");
            //seventh
            sevenDayWeatherInfo.setSeventhDayWeatherCode(seventhDayWeatherCode);
            sevenDayWeatherInfo.setSeventhDayTmpMax(seventhDayTmpMax);
            sevenDayWeatherInfo.setSeventhDayTmpMin(seventhDayTmpMin);


            Log.e("isCelsiusFahrenheit::", "" + sevenDayWeatherInfo.getCelsiusFahrenheit());
            Log.e("getTodayTmpCurrent::", "" + sevenDayWeatherInfo.getTodayTmpCurrent());
            Log.e("getCityName::", "" + sevenDayWeatherInfo.getCityName());
            Log.e("getNowWeatherCode::", "" + sevenDayWeatherInfo.getNowWeatherCode());
            Log.e("getTodayWeatherTxt::", "" + sevenDayWeatherInfo.getTodayWeatherTxt());
            Log.e("getTodayTmpMin::", "" + sevenDayWeatherInfo.getTodayTmpMin());
            Log.e("getTodayTmpMax::", "" + sevenDayWeatherInfo.getTodayTmpMax());
            Log.e("getTodayAqi::", "" + sevenDayWeatherInfo.getTodayAqi());
            Log.e("getTodayPm25::", "" + sevenDayWeatherInfo.getTodayPm25());
            Log.e("getTodayDate::", "" + sevenDayWeatherInfo.getTodayDate());
            Log.e("getTodaySunrise::", "" + sevenDayWeatherInfo.getTodaySunrise());
            Log.e("getTodaySunset::", "" + sevenDayWeatherInfo.getTodaySunset());
//                    "todayPm25":"",
//                    "todayAqi":"",
            boolean enableSevenDays = GetFunctionList.isSupportFunction(mContext, GlobalVariable.IS_SUPPORT_SEVEN_DAYS_WEATHER);
            Log.e("enableSevenDays::", "" + enableSevenDays);
            boolean enableSevenDays123 = GetFunctionList.isSupportFunction(mContext, GlobalVariable.IS_SUPPORT_WEATHER_FORECAST);
            Log.e("enableSevenDays123::", "" + enableSevenDays123);

            String resultant;
            if (mWriteCommand != null) {
                // mWriteCommand.syncWeatherToBLEForXiaoYang(sevenDayWeatherInfo);
                mWriteCommand.syncSevenDayWeatherToBle(sevenDayWeatherInfo);
                //mWriteCommand.syncWeatherToBle(sevenDayWeatherInfo);
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("set24HeartRateExp::", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void setDeviceBandLanguage(MethodCall call, Result result) {
        try {
            String resultant;
            String langInfo = call.argument("lang");
            assert langInfo != null;
            if (langInfo.equalsIgnoreCase("es")) {
                //if spanish
                if (mWriteCommand != null) {
                    mWriteCommand.syncBandLanguage(BandLanguageUtil.BAND_LANGUAGE_ES);
                    //result.success(WatchConstants.SC_INIT);
                    resultant = WatchConstants.SC_INIT;
                } else {
                    //result.success(WatchConstants.SC_FAILURE);
                    resultant = WatchConstants.SC_FAILURE;
                }
            } else {
                // english
                if (mWriteCommand != null) {
                    mWriteCommand.syncBandLanguage(BandLanguageUtil.BAND_LANGUAGE_EN);
                    //result.success(WatchConstants.SC_INIT);
                    resultant = WatchConstants.SC_INIT;
                } else {
                    //result.success(WatchConstants.SC_FAILURE);
                    resultant = WatchConstants.SC_FAILURE;
                }
            }
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("set24HeartRateExp::", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void set24HrTemperatureTest(MethodCall call, Result result) {
        try {
            String resultant;
            //String inter = call.argument("interval");
            String isOpen = call.argument("enable");
            //assert inter != null;
            //int interval = Integer.parseInt(inter);

            assert isOpen != null;
            boolean openEnabled = isOpen.toLowerCase().equalsIgnoreCase("true");
            if (mWriteCommand != null) {
                mWriteCommand.syncTemperatureAutomaticTestInterval(openEnabled, 30);
                //mWriteCommand.syncTemperatureAutomaticTestInterval(openEnabled, 2 *60);
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("set24HeartRateExp::", exp.getMessage());
            //result.success(WatchConstants.SC_FAILURE);
        }
    }


    /*private void fetchDeviceDataInfo(Result result) {
        try {
            JSONObject jsonObject = new JSONObject();

            boolean dialSupport = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_ONLINE_DIAL);
            String bleName = BLEVersionUtils.getInstance(mContext).getBleVersionName();
            String mac = BLEVersionUtils.getInstance(mContext).getBleMac();
            String dpi = SPUtil.getInstance(mContext).getResolutionWidthHeight();
            String maxCapacity = SPUtil.getInstance(mContext).getDialMaxDataSize();
            int shape = SPUtil.getInstance(mContext).getDialScreenType();
            int compatibleLevel = SPUtil.getInstance(mContext).getDialScreenCompatibleLevel();

            jsonObject.put("dialSupport", dialSupport);
            jsonObject.put("status", WatchConstants.SC_SUCCESS);
            jsonObject.put("bleName", bleName);
            jsonObject.put("mac", mac);
            jsonObject.put("dpi", dpi);
            jsonObject.put("maxCapacity", maxCapacity);
            jsonObject.put("type", 0);
            jsonObject.put("shape", "" + shape);
            jsonObject.put("compatible", "" + compatibleLevel);

            //result.success(jsonObject.toString());
            uiThreadHandler.postDelayed(() -> result.success(jsonObject.toString()), 100);
        } catch (Exception exp) {
            Log.e("fetchDeviceDInfoExp::", exp.getMessage());
            //  result.success(WatchConstants.SC_FAILURE);
        }
    }*/

    private void getDeviceVersion(Result result) {
        try {
            // deviceVersionIDResult = result;
            String resultant;
            //if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
            if (mWriteCommand != null) {
                mWriteCommand.sendToReadBLEVersion();
                // result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                // result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            //} else {
            //device disconnected
            //result.success(WatchConstants.SC_DISCONNECTED);
            // resultant = WatchConstants.SC_DISCONNECTED;
            //}

            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("getDeviceVersionExp:", exp.getMessage());
        }
    }

    private void getDeviceBatteryStatus(Result result) {
        try {
            // deviceBatteryResult = result;
            String resultant;
            //if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
            if (mWriteCommand != null) {
                mWriteCommand.sendToReadBLEBattery();
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            //  } else {
            //device disconnected
            //result.success(WatchConstants.SC_DISCONNECTED);
            //    resultant = WatchConstants.SC_DISCONNECTED;
            // }
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("getBatteryStatusExp:", exp.getMessage());
        }
    }

    private void getCheckConnectionStatus(Result result) {
        try {
            result.success(SPUtil.getInstance(mContext).getBleConnectStatus());
            // uiThreadHandler.postDelayed(() -> result.success(SPUtil.getInstance(mContext).getBleConnectStatus()), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("getConnectionStatusExp:", exp.getMessage());
        }
    }

    private void callCheckQuickSwitchStatus(Result result) {
        try {
            String resultant;
            //if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
            boolean isSupportBandQuickSwitch = GetFunctionList.isSupportFunction_Fourth(mContext, GlobalVariable.IS_SUPPORT_BAND_QUICK_SWITCH_SETTING); // language supported by the query device is supported
            Log.e("isSupportQuickSwitch:", "" + isSupportBandQuickSwitch);
            if (isSupportBandQuickSwitch) {
                if (mWriteCommand != null) {
                    mWriteCommand.queryQuickSwitchSupList();
                }
            }
            if (mWriteCommand != null) {
                mWriteCommand.queryQuickSwitchSupListStatus();
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            //} else {
            //device disconnected
            //result.success(WatchConstants.SC_DISCONNECTED);
            //    resultant = WatchConstants.SC_DISCONNECTED;
            //}
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("quickSwitchStatusExp:", exp.getMessage());
        }
    }

    // sync the activities
    private void fetchAllJudgement(Result result) {
        try {
            boolean bluConnect = SPUtil.getInstance(mContext).getBleConnectStatus();
            Log.e("bluConnect: ", "" + bluConnect);
            JSONObject judgeJson = new JSONObject();
            String resultant;
            if (bluConnect) {
                boolean rkPlatform = SPUtil.getInstance(mContext).getRKPlatform();
                boolean isSupportNewParams = GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_NEW_PARAMETER_SETTINGS_FUNCTION);
                boolean isBandLostFunction = GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_BAND_LOST_FUNCTION);
                boolean isSwitchBraceletLang = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_SWITCH_BRACELET_LANGUAGE);
                boolean isSupportTempUnitSwitch = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_TEMPERATURE_UNIT_SWITCH);
                boolean isMinHRAlarm = GetFunctionList.isSupportFunction_Fourth(mContext, GlobalVariable.IS_SUPPORT_MIN_HEAR_RATE_ALARM);

                boolean isSupportHorVer = GetFunctionList.isSupportFunction(mContext, GlobalVariable.IS_SUPPORT_HOR_VER);

                boolean isSupport24HrRate = GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_24_HOUR_RATE_TEST);
                boolean isTemperatureTest = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_TEMPERATURE_TEST);
                boolean isTemperatureCalibration = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_TEMPERATURE_CALIBRATION);
                boolean isSupportOxygen = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_OXYGEN);

                boolean isSupportBreathe = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_BREATHE);

                boolean isSupportWeather = GetFunctionList.isSupportFunction(mContext, GlobalVariable.IS_SUPPORT_SEVEN_DAYS_WEATHER);

                boolean isWeatherForeCast = GetFunctionList.isSupportFunction(mContext, GlobalVariable.IS_SUPPORT_WEATHER_FORECAST);

                boolean isSupportDrinkWater = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_DRINK_WATER_REMIND);

                boolean isSupportWristCalibra = GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_TURN_WRIST_CALIBRATION);

                boolean isSupportBandFindPhone = GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_BAND_FIND_PHONE_FUNCTION);

                boolean isSupportMusicalControl = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_MUSIC_CONTROL);
                boolean isSupportWristDetection = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_WRIST_DETECTION_SWITCH);
                boolean isSupportMultiSportsHR = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_MULTIPLE_SPORTS_MODES_HEART_RATE);
                boolean isSupportSportControl = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_SPORT_CONTROL);

                boolean isSupportOnlineDial = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_ONLINE_DIAL);
                boolean isSupportBodyComposition = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_BODY_COMPOSITION);
                boolean isSupportECG = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_ECG);

                boolean isSupportQueryBandLang = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_QUERY_BAND_LANGUAGE); // to get current value of the language
                boolean isSupportBandLangDisplay = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_BAND_LANGUAGE_DISPLAY); // language supported by the query device is supported

                boolean isSupportBandQuickSwitch = GetFunctionList.isSupportFunction_Fourth(mContext, GlobalVariable.IS_SUPPORT_BAND_QUICK_SWITCH_SETTING); // language supported by the query device is supported

                String resultJson;
                try {

                    //judgeJson.put("status", WatchConstants.SC_SUCCESS);
                    judgeJson.put("rkPlatform", rkPlatform);
                    judgeJson.put("isSupportNewParams", isSupportNewParams);
                    judgeJson.put("isBandLostFunction", isBandLostFunction);
                    judgeJson.put("isBraceletLangSwitch", isSwitchBraceletLang);
                    judgeJson.put("isTempUnitSwitch", isSupportTempUnitSwitch);
                    judgeJson.put("isMinHRAlarm", isMinHRAlarm);
                    judgeJson.put("isTempTest", isTemperatureTest);
                    judgeJson.put("isTempCalibration", isTemperatureCalibration);
                    judgeJson.put("isSupportHorVer", isSupportHorVer);
                    judgeJson.put("isSupport24HrRate", isSupport24HrRate);
                    judgeJson.put("isSupportOxygen", isSupportOxygen);
                    judgeJson.put("isSupportBreathe", isSupportBreathe);
                    judgeJson.put("isSupportWeather", isSupportWeather);
                    judgeJson.put("isWeatherForeCast", isWeatherForeCast);
                    judgeJson.put("isSupportDrinkWater", isSupportDrinkWater);
                    judgeJson.put("isSupportWristCalibra", isSupportWristCalibra);
                    judgeJson.put("isSupportBandFindPhone", isSupportBandFindPhone);
                    judgeJson.put("isSupportMusicalControl", isSupportMusicalControl);
                    judgeJson.put("isSupportWristDetection", isSupportWristDetection);
                    judgeJson.put("isSupportMultiSportsHR", isSupportMultiSportsHR);
                    judgeJson.put("isSupportSportControl", isSupportSportControl);
                    judgeJson.put("isSupportOnlineDial", isSupportOnlineDial);
                    judgeJson.put("isSupportBodyComposition", isSupportBodyComposition);
                    judgeJson.put("isSupportECG", isSupportECG);
                    judgeJson.put("isSupportQueryBandLang", isSupportQueryBandLang);
                    judgeJson.put("isSupportBandLangDisplay", isSupportBandLangDisplay);
                    judgeJson.put("isSupportBandQuickSwitch", isSupportBandQuickSwitch);

                    //result.success(judgeJson.toString());
                    resultJson = judgeJson.toString();
                } catch (Exception exp) {
                    Log.e("syncAllJSONExp: ", "" + exp.getMessage());
                    // result.success(WatchConstants.SC_FAILURE);
                    resultJson = WatchConstants.SC_FAILURE;
                }
                resultant = resultJson;
            } else {
                //device disconnected
                //result.success(WatchConstants.SC_DISCONNECTED);
                resultant = WatchConstants.SC_DISCONNECTED;
            }
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("fetchAllJudgeExp:", exp.getMessage());
        }
    }

    private void syncAllStepsData(Result result) {
        try {
            String resultant;
            // Log.e("steps_status", "" + SPUtil.getInstance(mContext).getBleConnectStatus());
            // if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
            if (mWriteCommand != null) {
                mWriteCommand.syncAllStepData();
                // result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                // result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            // } else {
            //device disconnected
            //  result.success(WatchConstants.SC_DISCONNECTED);
            //  resultant = WatchConstants.SC_DISCONNECTED;
            //}
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("syncAllStepsExp:", exp.getMessage());
        }
    }

    private void syncAllSleepData(Result result) {
        try {
            String resultant;
            //Log.e("sleep_status", "" + SPUtil.getInstance(mContext).getBleConnectStatus());
            //if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
            if (mWriteCommand != null) {
                mWriteCommand.syncAllSleepData();
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            //} else {
            //device disconnected
            //    result.success(WatchConstants.SC_DISCONNECTED);
            //}
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("syncAllSleepExp:", exp.getMessage());
        }
    }

    private void syncRateData(Result result) {
        try {
            //boolean support = GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_24_HOUR_RATE_TEST);
            //Log.e("support", "" + support);
            String resultant;
            //Log.e("steps_status", "" + SPUtil.getInstance(mContext).getBleConnectStatus());
            // if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
            if (mWriteCommand != null) {
                //boolean supportHr24 = GetFunctionList.isSupportFunction_Second(mContext,GlobalVariable.IS_SUPPORT_24_HOUR_RATE_TEST);
                //                    if (supportHr24){
//                        mWriteCommand.sync24HourRate();
//                    }else{
                mWriteCommand.syncRateData();
                //  }
                //mWriteCommand.syncRateData();
                /*mWriteCommand. syncAllRateData();
                 */
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            //} else {
            //device disconnected
            //    result.success(WatchConstants.SC_DISCONNECTED);
            //}
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("syncRateDataExp:", exp.getMessage());
        }
    }

    private void syncBloodPressure(Result result) {
        try {
            String resultant;
            //if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
            if (mWriteCommand != null) {
                mWriteCommand.syncAllBloodPressureData();
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
            //} else {
            //    result.success(WatchConstants.SC_DISCONNECTED);
            //}
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("syncBPExp:", exp.getMessage());
        }
    }

    private void syncOxygenSaturation(Result result) {
        try {
            String resultant;
            boolean isSupported = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_OXYGEN);
            Log.e("syncOxygenSat", "isSupported: " + isSupported);
            if (isSupported) {
                // if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
                if (mWriteCommand != null) {
                    mWriteCommand.syncOxygenData();
                    //result.success(WatchConstants.SC_INIT);
                    resultant = WatchConstants.SC_INIT;
                } else {
                    //result.success(WatchConstants.SC_FAILURE);
                    resultant = WatchConstants.SC_FAILURE;
                }
                // } else {
                //     result.success(WatchConstants.SC_DISCONNECTED);
                // }
            } else {
                //result.success(WatchConstants.SC_NOT_SUPPORTED);
                resultant = WatchConstants.SC_NOT_SUPPORTED;
            }
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("syncOxygenExp:", exp.getMessage());
        }
        // below methods need to called, while it supports
        // mBluetoothLeService.setOxygenListener(oxygenRealListener);
    }

    private void syncBodyTemperature(Result result) {
        try {
            String resultant;
            boolean isSupported = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_TEMPERATURE_TEST);
            Log.e("syncBodyTemp", "isSupported: " + isSupported);
            if (isSupported) {
                //if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
                if (mWriteCommand != null) {
                    mWriteCommand.syncAllTemperatureData();
                    //result.success(WatchConstants.SC_INIT);
                    resultant = WatchConstants.SC_INIT;
                } else {
                    //result.success(WatchConstants.SC_FAILURE);
                    resultant = WatchConstants.SC_FAILURE;
                }
                //} else {
                //    result.success(WatchConstants.SC_DISCONNECTED);
                //}
            } else {
                // result.success(WatchConstants.SC_NOT_SUPPORTED);
                resultant = WatchConstants.SC_NOT_SUPPORTED;
            }
            uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
        } catch (Exception exp) {
            Log.e("syncBodyTempExp:", exp.getMessage());
        }
        // mBluetoothLeService.setTemperatureListener(temperatureListener);
    }

    // fetch by date time
    private void fetchOverAllBySelectedDate(MethodCall call, Result result) {
        try {
            String dateTime = call.argument("dateTime"); // always in "yyyyMMdd";
            JSONObject overAllJson = new JSONObject();
            JSONObject stepsJsonData = new JSONObject();
            JSONObject sleepJsonData = new JSONObject();
            if (mUTESQLOperate != null) {
                overAllJson.put("status", WatchConstants.SC_SUCCESS);
                // stepByDate
                StepOneDayAllInfo stepOneDayAllInfo = mUTESQLOperate.queryRunWalkInfo(dateTime);
                List<RateOneDayInfo> rateOneDayInfoList = mUTESQLOperate.queryRateOneDayDetailInfo(dateTime);
                List<Rate24HourDayInfo> rate24HourDayInfoList = mUTESQLOperate.query24HourRateDayInfo(dateTime);
                List<BPVOneDayInfo> bpvOneDayInfoList = mUTESQLOperate.queryBloodPressureOneDayInfo(dateTime);

                SleepTimeInfo sleepTimeInfo = mUTESQLOperate.querySleepInfo(dateTime);

                List<TemperatureInfo> temperatureInfoList = mUTESQLOperate.queryTemperatureDate(dateTime);
                if (temperatureInfoList != null) {
                    Log.e("temperatureInfoList", "temperatureInfoList: " + temperatureInfoList.size());
                    for (TemperatureInfo tempInfo : temperatureInfoList) {
                        Log.e("tempInfoItem", "Cal" + tempInfo.getCalendar() + " - " + tempInfo.getBodyTemperature());
                    }
                }
                //steps data
                if (stepOneDayAllInfo != null) {
                    stepsJsonData.put("steps", stepOneDayAllInfo.getStep());
                    stepsJsonData.put("distance", "" + GlobalMethods.convertDoubleToStringWithDecimal(stepOneDayAllInfo.getDistance()));
                    stepsJsonData.put("calories", "" + GlobalMethods.convertDoubleToStringWithDecimal(stepOneDayAllInfo.getCalories()));
                    stepsJsonData.put("calender", stepOneDayAllInfo.getCalendar());
                    ArrayList<StepOneHourInfo> stepOneHourInfoArrayList = stepOneDayAllInfo.getStepOneHourArrayInfo();
                    JSONArray stepsArray = new JSONArray();
                    for (StepOneHourInfo stepOneHourInfo : stepOneHourInfoArrayList) {
                        JSONObject object = new JSONObject();
                        object.put("step", stepOneHourInfo.getStep());
                        object.put("time", GlobalMethods.getIntegerToHHmm(stepOneHourInfo.getTime()));
                        stepsArray.put(object);
                    }
                    stepsJsonData.put("data", stepsArray);
                    overAllJson.put("steps", stepsJsonData);
                }

                //heart rate data
                if (rateOneDayInfoList != null) {
                    JSONArray hrArray = new JSONArray();
                    for (RateOneDayInfo rateOneDayInfo : rateOneDayInfoList) {
                        JSONObject object = new JSONObject();
                        object.put("calender", rateOneDayInfo.getCalendar());
                        object.put("time", GlobalMethods.getTimeByIntegerMin(rateOneDayInfo.getTime()));
                        object.put("rate", rateOneDayInfo.getRate());
                        object.put("calenderTime", rateOneDayInfo.getCalendarTime());
                        object.put("currentRate", rateOneDayInfo.getCurrentRate());
                        object.put("high", rateOneDayInfo.getHighestRate());
                        object.put("low", rateOneDayInfo.getLowestRate());
                        object.put("average", rateOneDayInfo.getVerageRate());
                        hrArray.put(object);
                    }
                    overAllJson.put("hr", hrArray);
                }

                //HR 24 hours
                if (rate24HourDayInfoList != null) {
                    JSONArray hr24Array = new JSONArray();
                    for (Rate24HourDayInfo rate24HourDayInfo : rate24HourDayInfoList) {
                        JSONObject object = new JSONObject();
                        object.put("calender", rate24HourDayInfo.getCalendar());
                        object.put("time", GlobalMethods.getTimeByIntegerMin(rate24HourDayInfo.getTime()));
                        object.put("rate", rate24HourDayInfo.getRate());
                        //Log.e("jsonObject", "object: " + object.toString());
                        hr24Array.put(object);
                    }
                    overAllJson.put("hr24", hr24Array);
                }

                //BP
                if (bpvOneDayInfoList != null) {
                    JSONArray bpArray = new JSONArray();
                    for (BPVOneDayInfo bpvOneDayInfo : bpvOneDayInfoList) {
                        JSONObject object = new JSONObject();
                        object.put("calender", bpvOneDayInfo.getCalendar());
                        object.put("time", GlobalMethods.getTimeByIntegerMin(bpvOneDayInfo.getBloodPressureTime()));
                        object.put("high", bpvOneDayInfo.getHightBloodPressure());
                        object.put("low", bpvOneDayInfo.getLowBloodPressure());
                        //Log.e("bpObject", "object: " + object.toString());
                        bpArray.put(object);
                    }
                    overAllJson.put("bp", bpArray);
                }
                //sleep data
                if (sleepTimeInfo != null) {
                    sleepJsonData.put("calender", sleepTimeInfo.getCalendar());
                    sleepJsonData.put("total", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getSleepTotalTime()));
                    sleepJsonData.put("light", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getLightTime()));
                    sleepJsonData.put("deep", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getDeepTime()));
                    sleepJsonData.put("awake", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getAwakeTime()));
                    sleepJsonData.put("beginTime", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getBeginTime()));
                    sleepJsonData.put("endTime", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getEndTime()));

                    sleepJsonData.put("totalNum", "" + sleepTimeInfo.getSleepTotalTime());
                    sleepJsonData.put("lightNum", "" + sleepTimeInfo.getLightTime());
                    sleepJsonData.put("deepNum", "" + sleepTimeInfo.getDeepTime());
                    sleepJsonData.put("awakeNum", "" + sleepTimeInfo.getAwakeTime());
                    sleepJsonData.put("beginTimeNum", "" + sleepTimeInfo.getBeginTime());
                    sleepJsonData.put("endTimeNum", "" + sleepTimeInfo.getEndTime());

                    List<SleepInfo> sleepInfoList = sleepTimeInfo.getSleepInfoList();
                    JSONArray sleepArray = new JSONArray();
                    for (SleepInfo sleepInfo : sleepInfoList) {
                        JSONObject object = new JSONObject();
                        object.put("state", sleepInfo.getColorIndex()); // deep sleep: 0, Light sleep: 1,  awake: 2
                        object.put("startTime", GlobalMethods.getTimeByIntegerMin(sleepInfo.getStartTime()));
                        object.put("endTime", GlobalMethods.getTimeByIntegerMin(sleepInfo.getEndTime()));
                        // object.put("diffTime", GlobalMethods.getTimeByIntegerMin(sleepInfo.getDiffTime()));

                        object.put("startTimeNum", "" + sleepInfo.getStartTime());
                        object.put("endTimeNum", "" + sleepInfo.getEndTime());
                        // object.put("diffTimeNum", "" + sleepInfo.getDiffTime());
                        sleepArray.put(object);
                    }
                    sleepJsonData.put("data", sleepArray);

                    overAllJson.put("sleep", sleepJsonData);
                }


                //Temperature
                if (temperatureInfoList != null) {
                    JSONArray temperatureArray = new JSONArray();
                    for (TemperatureInfo temperatureInfo : temperatureInfoList) {
                        JSONObject tempObj = new JSONObject();
                        tempObj.put("calender", temperatureInfo.getCalendar());
                        //tempObj.put("type", "" + temperatureInfo.getType());
                        tempObj.put("inCelsius", "" + GlobalMethods.convertDoubleToCelsiusWithDecimal(temperatureInfo.getBodyTemperature()));
                        tempObj.put("inFahrenheit", "" + GlobalMethods.getTempIntoFahrenheit(temperatureInfo.getBodyTemperature()));
                        //tempObj.put("startDate", "" + temperatureInfo.getStartDate()); //yyyyMMddHHmmss
                        tempObj.put("time", "" + GlobalMethods.convertIntToHHMmSs(temperatureInfo.getSecondTime()));

                        // tempObj.put("time1", "" + GlobalMethods.convertTimeToHHMm(temperatureInfo.getStartDate()));
                        Log.e("jsonObject", "object: " + tempObj.toString());
                        temperatureArray.put(tempObj);
                    }
                    overAllJson.put("temperature", temperatureArray);
                }

                result.success(overAllJson.toString());
            } else {
                result.success(overAllJson.toString());
            }

        } catch (Exception exp) {
            Log.e("fetchOverAllExp::", exp.getMessage());
            result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void fetchStepsBySelectedDate(MethodCall call, Result result) {
        // providing proper list of the data on basis of the result.
        try {
            //  new SimpleDateFormat("yyyyMMdd", Locale.US)).format(var1) //20220212
            String dateTime = call.argument("dateTime"); // always in "yyyyMMdd";
            JSONObject jsonObject = new JSONObject();
            if (mUTESQLOperate != null) {

                Log.e("dateTime::", dateTime);
                StepOneDayAllInfo stepOneDayAllInfo = mUTESQLOperate.queryRunWalkInfo(dateTime);
                Log.e("stepOneDayAllInfo::", "" + stepOneDayAllInfo);
                jsonObject.put("status", WatchConstants.SC_SUCCESS);
                if (stepOneDayAllInfo != null) {
                    jsonObject.put("calender", stepOneDayAllInfo.getCalendar());
                    jsonObject.put("steps", "" + stepOneDayAllInfo.getStep());
                    jsonObject.put("distance", GlobalMethods.convertDoubleToStringWithDecimal(stepOneDayAllInfo.getDistance()));
                    jsonObject.put("calories", GlobalMethods.convertDoubleToStringWithDecimal(stepOneDayAllInfo.getCalories()));
                    ArrayList<StepOneHourInfo> stepOneHourInfoArrayList = stepOneDayAllInfo.getStepOneHourArrayInfo();
                    JSONArray jsonArray = new JSONArray();
                    for (StepOneHourInfo stepOneHourInfo : stepOneHourInfoArrayList) {
                        JSONObject object = new JSONObject();
                        object.put("step", stepOneHourInfo.getStep());
                        object.put("time", GlobalMethods.getIntegerToHHmm(stepOneHourInfo.getTime()));
                        jsonArray.put(object);
                    }
                    jsonObject.put("data", jsonArray);
                } else {
                    jsonObject.put("calender", dateTime);
                    jsonObject.put("steps", "0");
                    jsonObject.put("distance", "0.00");
                    jsonObject.put("calories", "0.00");
                    JSONArray jsonArray = new JSONArray();
                    jsonObject.put("data", jsonArray);
                }
                result.success(jsonObject.toString());
            } else {
                jsonObject.put("status", WatchConstants.SC_FAILURE);
                result.success(jsonObject.toString());
            }
        } catch (Exception exp) {
            Log.e("fetchStepExp::", exp.getMessage());
            //  result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void fetchSleepByDate(MethodCall call, Result result) {
        // providing proper list of the data on basis of the result.
        try {
            //  new SimpleDateFormat("yyyyMMdd", Locale.US)).format(var1) //20220212
            String dateTime = call.argument("dateTime"); // always in "yyyyMMdd";
            JSONObject resultJson = new JSONObject();
            if (mUTESQLOperate != null) {

                SleepTimeInfo sleepTimeInfo = mUTESQLOperate.querySleepInfo(dateTime);

                resultJson.put("status", WatchConstants.SC_SUCCESS);
                resultJson.put("calender", sleepTimeInfo.getCalendar());
                resultJson.put("total", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getSleepTotalTime()));
                resultJson.put("light", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getLightTime()));
                resultJson.put("deep", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getDeepTime()));
                resultJson.put("awake", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getAwakeTime()));
                resultJson.put("beginTime", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getBeginTime()));
                resultJson.put("endTime", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getEndTime()));

                resultJson.put("totalNum", "" + sleepTimeInfo.getSleepTotalTime());
                resultJson.put("lightNum", "" + sleepTimeInfo.getLightTime());
                resultJson.put("deepNum", "" + sleepTimeInfo.getDeepTime());
                resultJson.put("awakeNum", "" + sleepTimeInfo.getAwakeTime());
                resultJson.put("beginTimeNum", "" + sleepTimeInfo.getBeginTime());
                resultJson.put("endTimeNum", "" + sleepTimeInfo.getEndTime());
//                Log.e("sleepTimeInfo111", "getBeginTime: " +  GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getBeginTime()));
//                Log.e("sleepTimeInfo111", "getEndTime: " + GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getEndTime()));
//                Log.e("sleepTimeInfo111", "getDeepTime: " + GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getDeepTime()));
//                Log.e("sleepTimeInfo111", "getAwakeTime: " +GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getAwakeTime()));
//                Log.e("sleepTimeInfo111", "getLightTime: " + GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getLightTime()));
//                Log.e("sleepTimeInfo111", "getSleepTotalTime: " + GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getSleepTotalTime()));

                // fetch the particular day sleep along state records
                List<SleepInfo> sleepInfoList = sleepTimeInfo.getSleepInfoList();
                Log.e("sleepInfoList", "sleepInfoList: " + sleepInfoList.size());
                JSONArray jsonArray = new JSONArray();
                for (SleepInfo sleepInfo : sleepInfoList) {
                    JSONObject object = new JSONObject();
                    object.put("state", sleepInfo.getColorIndex()); // deep sleep: 0, Light sleep: 1,  awake: 2
                    object.put("startTime", GlobalMethods.getTimeByIntegerMin(sleepInfo.getStartTime()));
                    object.put("endTime", GlobalMethods.getTimeByIntegerMin(sleepInfo.getEndTime()));
                    // object.put("diffTime", GlobalMethods.getTimeByIntegerMin(sleepInfo.getDiffTime()));

                    object.put("startTimeNum", "" + sleepInfo.getStartTime());
                    object.put("endTimeNum", "" + sleepInfo.getEndTime());
                    //object.put("diffTimeNum", "" + sleepInfo.getDiffTime());
//                    Log.e("sleepInfoList", "getColorIndex: " + sleepInfo.getColorIndex());
//                    Log.e("sleepInfoList", "getDiffTime: " + GlobalMethods.getTimeByIntegerMin(sleepInfo.getDiffTime()));
//                    Log.e("sleepInfoList", "getStartTime: " + GlobalMethods.getTimeByIntegerMin(sleepInfo.getStartTime()) + " -- " + GlobalMethods.getTimeByIntegerMin(sleepInfo.getEndTime()));
                    // Log.e("sleepInfoList", );
                    jsonArray.put(object);
                }

                resultJson.put("data", jsonArray);

                result.success(resultJson.toString());
            } else {
                result.success(resultJson.toString());
            }
        } catch (Exception exp) {
            Log.e("fetchStepExp::", exp.getMessage());
            // result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void fetchBPByDate(MethodCall call, Result result) {
        // providing proper list of the data on basis of the result.
        try {
            //  new SimpleDateFormat("yyyyMMdd", Locale.US)).format(var1) //20220212
            String dateTime = call.argument("dateTime"); // always in "yyyyMMdd";
            JSONObject resultJson = new JSONObject();
            if (mUTESQLOperate != null) {

                List<BPVOneDayInfo> bpvOneDayInfoList = mUTESQLOperate.queryBloodPressureOneDayInfo(dateTime);
                Log.e("bpvOneDayInfoList", "bpvOneDayInfoList: " + bpvOneDayInfoList.size());

                resultJson.put("status", WatchConstants.SC_SUCCESS);
                JSONArray jsonArray = new JSONArray();
                for (BPVOneDayInfo bpvOneDayInfo : bpvOneDayInfoList) {
                    JSONObject object = new JSONObject();
                    object.put("calender", bpvOneDayInfo.getCalendar());
                    object.put("time", GlobalMethods.getTimeByIntegerMin(bpvOneDayInfo.getBloodPressureTime()));
                    object.put("high", bpvOneDayInfo.getHightBloodPressure());
                    object.put("low", bpvOneDayInfo.getLowBloodPressure());
                    Log.e("bpObject", "object: " + object.toString());
                    jsonArray.put(object);
                }

                resultJson.put("data", jsonArray);
                result.success(resultJson.toString());
            } else {
                result.success(resultJson.toString());
            }
        } catch (Exception exp) {
            Log.e("fetchStepExp::", exp.getMessage());
            // result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void fetchHRByDate(MethodCall call, Result result) {
        // providing proper list of the data on basis of the result.
        try {
            //  new SimpleDateFormat("yyyyMMdd", Locale.US)).format(var1) //20220212
            String dateTime = call.argument("dateTime"); // always in "yyyyMMdd";
            JSONObject resultJson = new JSONObject();
            if (mUTESQLOperate != null) {
                List<RateOneDayInfo> rateOneDayInfoList = mUTESQLOperate.queryRateOneDayDetailInfo(dateTime);
                // List<RateOneDayInfo> rateOneDayInfoList = mUTESQLOperate.queryRateAllInfo(); // list for the current date //size 5
//                RateOneDayInfo rateOneDay = mUTESQLOperate.queryRateOneDayMainInfo(dateTime);
//                Log.e("rateOneDay", "getRate: " +  rateOneDay.getRate());
//                Log.e("rateOneDay", "getTime: " +  GlobalMethods.getTimeByIntegerMin(rateOneDay.getTime()));
                Log.e("rateOneDayInfoList", "rateOneDayInfoList: " + rateOneDayInfoList.size());
                resultJson.put("status", WatchConstants.SC_SUCCESS);
                JSONArray jsonArray = new JSONArray();
                for (RateOneDayInfo rateOneDayInfo : rateOneDayInfoList) {
                    JSONObject object = new JSONObject();
                    object.put("calender", rateOneDayInfo.getCalendar());
                    object.put("time", GlobalMethods.getTimeByIntegerMin(rateOneDayInfo.getTime()));
                    object.put("rate", rateOneDayInfo.getRate());
                    object.put("calenderTime", rateOneDayInfo.getCalendarTime());
                    object.put("currentRate", rateOneDayInfo.getCurrentRate());
                    object.put("high", rateOneDayInfo.getHighestRate());
                    object.put("low", rateOneDayInfo.getLowestRate());
                    object.put("average", rateOneDayInfo.getVerageRate());
                    Log.e("jsonObject", "object: " + object.toString());
                    jsonArray.put(object);
                }

                resultJson.put("data", jsonArray);
                result.success(resultJson.toString());
            } else {
                result.success(resultJson.toString());
            }
        } catch (Exception exp) {
            Log.e("fetchHRExp::", exp.getMessage());
            // result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void fetch24HourHRDateByDate(MethodCall call, Result result) {
        try {
            String dateTime = call.argument("dateTime"); // always in "yyyyMMdd";
            JSONObject resultJson = new JSONObject();
            if (mUTESQLOperate != null) {
                //  List<Rate24HourDayInfo> rate24HourDayInfoList =  mUTESQLOperate.query24HourRateAllInfo(); // provides overall available 24 hrs data from storage
                List<Rate24HourDayInfo> rate24HourDayInfoList = mUTESQLOperate.query24HourRateDayInfo(dateTime);
                Log.e("rateOneDayInfoList", "rateOneDayInfoList: " + rate24HourDayInfoList.size());

                resultJson.put("status", WatchConstants.SC_SUCCESS);
                JSONArray jsonArray = new JSONArray();
                for (Rate24HourDayInfo rate24HourDayInfo : rate24HourDayInfoList) {
                    JSONObject object = new JSONObject();
                    object.put("calender", rate24HourDayInfo.getCalendar());
                    object.put("time", GlobalMethods.getTimeByIntegerMin(rate24HourDayInfo.getTime()));
                    object.put("rate", rate24HourDayInfo.getRate());
                    Log.e("jsonObject", "object: " + object.toString());
                    jsonArray.put(object);
                }

                resultJson.put("data", jsonArray);

                result.success(resultJson.toString());
            } else {
                result.success(resultJson.toString());
            }
        } catch (Exception exp) {
            Log.e("fetch24HourHRExp::", exp.getMessage());
            // result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void fetchTemperatureByDate(MethodCall call, Result result) {
        try {
            String dateTime = call.argument("dateTime"); // always in "yyyyMMdd";
            JSONObject resultJson = new JSONObject();
            if (mUTESQLOperate != null) {

                List<TemperatureInfo> temperatureInfoList = mUTESQLOperate.queryTemperatureDate(dateTime);
                resultJson.put("status", WatchConstants.SC_SUCCESS);
                JSONArray jsonArray = new JSONArray();
                if (temperatureInfoList != null) {
                    Log.e("temperatureInfoList", "temperatureInfoList: " + temperatureInfoList.size());
                    for (TemperatureInfo tempInfo : temperatureInfoList) {
                        Log.e("tempInfoItem", "Cal" + tempInfo.getCalendar() + " - " + tempInfo.getBodyTemperature());
                    }

                    for (TemperatureInfo temperatureInfo : temperatureInfoList) {
                        JSONObject object = new JSONObject();
                        object.put("calender", temperatureInfo.getCalendar());

                        // object.put("type", "" + temperatureInfo.getType());
                        object.put("inCelsius", "" + GlobalMethods.convertDoubleToCelsiusWithDecimal(temperatureInfo.getBodyTemperature()));
                        object.put("inFahrenheit", "" + GlobalMethods.getTempIntoFahrenheit(temperatureInfo.getBodyTemperature()));
//                    object.put("ambientTemp", "" + temperatureInfo.getAmbientTemperature());
//                    object.put("surfaceTemp", "" + temperatureInfo.getBodySurfaceTemperature());
                        object.put("startDate", "" + temperatureInfo.getStartDate()); //yyyyMMddHHmmss
                        object.put("time", "" + GlobalMethods.convertIntToHHMmSs(temperatureInfo.getSecondTime()));
                        //object.put("time1", "" + GlobalMethods.convertTimeToHHMm(temperatureInfo.getStartDate()));

                        Log.e("jsonObject", "object: " + object.toString());
                        jsonArray.put(object);
                    }
                }

                resultJson.put("data", jsonArray);
                result.success(resultJson.toString());
            } else {
                result.success(resultJson.toString());
            }
        } catch (Exception exp) {
            Log.e("fetchTempByDate::", exp.getMessage());
            // result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void fetchOxygenByDate(MethodCall call, Result result) {
        // providing proper list of the data on basis of the result.
        try {
            //  new SimpleDateFormat("yyyyMMdd", Locale.US)).format(var1) //20220212
            String dateTime = call.argument("dateTime"); // always in "yyyyMMdd";
            JSONObject resultJson = new JSONObject();
            if (mUTESQLOperate != null) {
                List<OxygenInfo> oxygenInfoList = mUTESQLOperate.queryOxygenDate(dateTime);
                Log.e("oxygenInfoList", "oxygenInfoList: " + oxygenInfoList.size());
                resultJson.put("status", WatchConstants.SC_SUCCESS);
                JSONArray jsonArray = new JSONArray();
                for (OxygenInfo oxygenInfo : oxygenInfoList) {
                    JSONObject object = new JSONObject();
                    object.put("calender", oxygenInfo.getCalendar());
                    object.put("value", "" + oxygenInfo.getOxygenValue());
                    // object.put("startDate", "" + oxygenInfo.getStartDate()); //yyyyMMddHHmmss
                    object.put("time", "" + GlobalMethods.getTimeByIntegerMin(oxygenInfo.getTime()));
                    Log.e("oxyObject", "object: " + object.toString());
                    jsonArray.put(object);
                }
                resultJson.put("data", jsonArray);
                result.success(resultJson.toString());
            } else {
                result.success(resultJson.toString());
            }
        } catch (Exception exp) {
            Log.e("fetchOxygenExp::", exp.getMessage());
            // result.success(WatchConstants.SC_FAILURE);
        }
    }

    //gathering individual data
    private void fetchAllStepsData(Result result) {
        // providing proper list of the data on basis of the result.
        try {
            JSONObject resultObject = new JSONObject();
            if (mUTESQLOperate != null) {
                List<StepOneDayAllInfo> list = mUTESQLOperate.queryRunWalkAllDay();
                if (list != null) {
                    Log.e("list", "list: " + list.size());
                    resultObject.put("status", WatchConstants.SC_SUCCESS);
                    JSONArray jsonArray = new JSONArray();
                    for (StepOneDayAllInfo info : list) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("calender", info.getCalendar());
                        jsonObject.put("steps", info.getStep());
                        jsonObject.put("calories", GlobalMethods.convertDoubleToStringWithDecimal(info.getCalories()));
                        jsonObject.put("distance", GlobalMethods.convertDoubleToStringWithDecimal(info.getDistance()));
//                        Log.e("list_info:", "calender: " + info.getCalendar());
//                        Log.e("list_info:", "step: " + info.getStep());
//                        Log.e("list_info:", "cal: " + info.getCalories());
//                        Log.e("list_info:", "dis: " + info.getDistance());

                        ArrayList<StepOneHourInfo> stepOneHourInfoArrayList = info.getStepOneHourArrayInfo();
                        JSONArray stepsArray = new JSONArray();
                        for (StepOneHourInfo stepOneHourInfo : stepOneHourInfoArrayList) {
                            JSONObject object = new JSONObject();
                            object.put("step", stepOneHourInfo.getStep());
                            object.put("time", GlobalMethods.getIntegerToHHmm(stepOneHourInfo.getTime()));
                            stepsArray.put(object);
                        }
                        jsonObject.put("data", stepsArray);

                        jsonArray.put(jsonObject);
                    }
                    resultObject.put("data", jsonArray);
                } else {
                    resultObject.put("status", WatchConstants.SC_FAILURE);
                }
            }
            result.success(resultObject.toString());
        } catch (Exception exp) {
            Log.e("fetchAllStepExp::", exp.getMessage());
            // result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void fetchAllSleepData(Result result) {
        // providing proper list of the data on basis of the result.
        try {
            JSONObject resultObject = new JSONObject();
            if (mUTESQLOperate != null) {
                List<SleepTimeInfo> sleepTimeInfoList = mUTESQLOperate.queryAllSleepInfo();
                if (sleepTimeInfoList != null) {
                    Log.e("list", "list: " + sleepTimeInfoList.size());
                    resultObject.put("status", WatchConstants.SC_SUCCESS);
                    JSONArray jsonArray = new JSONArray();
                    for (SleepTimeInfo sleepTimeInfo : sleepTimeInfoList) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("calender", sleepTimeInfo.getCalendar());
                        jsonObject.put("total", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getSleepTotalTime()));
                        jsonObject.put("light", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getLightTime()));
                        jsonObject.put("deep", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getDeepTime()));
                        jsonObject.put("awake", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getAwakeTime()));
                        jsonObject.put("beginTime", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getBeginTime()));
                        jsonObject.put("endTime", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getEndTime()));

                        jsonObject.put("totalNum", "" + sleepTimeInfo.getSleepTotalTime());
                        jsonObject.put("lightNum", "" + sleepTimeInfo.getLightTime());
                        jsonObject.put("deepNum", "" + sleepTimeInfo.getDeepTime());
                        jsonObject.put("awakeNum", "" + sleepTimeInfo.getAwakeTime());
                        jsonObject.put("beginTimeNum", "" + sleepTimeInfo.getBeginTime());
                        jsonObject.put("endTimeNum", "" + sleepTimeInfo.getEndTime());


                        List<SleepInfo> sleepInfoList = sleepTimeInfo.getSleepInfoList();
                        Log.e("sleepInfoList", "sleepInfoList: " + sleepInfoList.size());

                        JSONArray sleepDataArray = new JSONArray();
                        for (SleepInfo sleepInfo : sleepInfoList) {
                            JSONObject object = new JSONObject();
                            object.put("state", sleepInfo.getColorIndex()); // deep sleep: 0, Light sleep: 1,  awake: 2
                            object.put("startTime", GlobalMethods.getTimeByIntegerMin(sleepInfo.getStartTime()));
                            object.put("endTime", GlobalMethods.getTimeByIntegerMin(sleepInfo.getEndTime()));
                            // object.put("diffTime", GlobalMethods.getTimeByIntegerMin(sleepInfo.getDiffTime()));

                            object.put("startTimeNum", "" + sleepInfo.getStartTime());
                            object.put("endTimeNum", "" + sleepInfo.getEndTime());
                            // object.put("diffTimeNum", "" + sleepInfo.getDiffTime());
                            sleepDataArray.put(object);
                        }

                        jsonObject.put("data", sleepDataArray);

                        jsonArray.put(jsonObject);
                    }
                    resultObject.put("data", jsonArray);
                } else {
                    resultObject.put("status", WatchConstants.SC_FAILURE);
                }
            }
            result.success(resultObject.toString());
        } catch (Exception exp) {
            Log.e("fetchAllSleepExp::", exp.getMessage());
            // result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void fetchAllBPData(Result result) {
        // providing proper list of the data on basis of the result.
        try {
            JSONObject resultObject = new JSONObject();
            if (mUTESQLOperate != null) {
                List<BPVOneDayInfo> bpvOneDayInfoList = mUTESQLOperate.queryAllBloodPressureInfo();
                if (bpvOneDayInfoList != null) {
                    // Log.e("bplist", "list: " + bpvOneDayInfoList.size());
                    resultObject.put("status", WatchConstants.SC_SUCCESS);
                    JSONArray jsonArray = new JSONArray();
                    for (BPVOneDayInfo bpvOneDayInfo : bpvOneDayInfoList) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("calender", bpvOneDayInfo.getCalendar());
                        jsonObject.put("time", GlobalMethods.getTimeByIntegerMin(bpvOneDayInfo.getBloodPressureTime()));
                        jsonObject.put("high", bpvOneDayInfo.getHightBloodPressure());
                        jsonObject.put("low", bpvOneDayInfo.getLowBloodPressure());
                        jsonArray.put(jsonObject);
                    }
                    resultObject.put("data", jsonArray);
                } else {
                    resultObject.put("status", WatchConstants.SC_FAILURE);
                }
            }
            result.success(resultObject.toString());
        } catch (Exception exp) {
            Log.e("fetchAllBPExp::", exp.getMessage());
            // result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void fetchAllTemperatureData(Result result) {
        // providing proper list of the data on basis of the result.
        try {
            JSONObject resultObject = new JSONObject();
            if (mUTESQLOperate != null) {
                List<TemperatureInfo> temperatureInfoList = mUTESQLOperate.queryTemperatureAll();
                if (temperatureInfoList != null) {
                    Log.e("temperatureInfoList", "list: " + temperatureInfoList.size());
                    if (temperatureInfoList != null) {
                        for (TemperatureInfo tempInfo : temperatureInfoList) {
                            Log.e("tempInfoItem2925", "Cal" + tempInfo.getCalendar() + " - " + tempInfo.getBodyTemperature());
                        }
                    }
                    resultObject.put("status", WatchConstants.SC_SUCCESS);
                    JSONArray jsonArray = new JSONArray();
                    for (TemperatureInfo temperatureInfo : temperatureInfoList) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("calender", temperatureInfo.getCalendar());
                        //jsonObject.put("type", "" + temperatureInfo.getType());
                        jsonObject.put("inCelsius", "" + GlobalMethods.convertDoubleToCelsiusWithDecimal(temperatureInfo.getBodyTemperature()));
                        jsonObject.put("inFahrenheit", "" + GlobalMethods.getTempIntoFahrenheit(temperatureInfo.getBodyTemperature()));
                        //jsonObject.put("startDate", "" + temperatureInfo.getStartDate()); //yyyyMMddHHmmss
                        jsonObject.put("time", "" + GlobalMethods.convertIntToHHMmSs(temperatureInfo.getSecondTime()));

                        //jsonObject.put("time1", "" + GlobalMethods.convertTimeToHHMm(temperatureInfo.getStartDate()));

                        jsonArray.put(jsonObject);
                    }
                    resultObject.put("data", jsonArray);
                } else {
                    resultObject.put("status", WatchConstants.SC_FAILURE);
                }
            }
            result.success(resultObject.toString());
        } catch (Exception exp) {
            Log.e("fetchAllTempExp::", exp.getMessage());
            // result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void fetchAllHeartRate24Data(Result result) {
        // providing proper list of the data on basis of the result.
        try {
            JSONObject resultObject = new JSONObject();
            if (mUTESQLOperate != null) {
                List<Rate24HourDayInfo> rate24HourDayInfoList = mUTESQLOperate.query24HourRateAllInfo();
                if (rate24HourDayInfoList != null) {
                    Log.e("hr24list", "list: " + rate24HourDayInfoList.size());
                    resultObject.put("status", WatchConstants.SC_SUCCESS);
                    JSONArray jsonArray = new JSONArray();
                    for (Rate24HourDayInfo rate24HourDayInfo : rate24HourDayInfoList) {
                        JSONObject object = new JSONObject();
                        object.put("calender", rate24HourDayInfo.getCalendar());
                        object.put("time", GlobalMethods.getTimeByIntegerMin(rate24HourDayInfo.getTime()));
                        object.put("rate", rate24HourDayInfo.getRate());
                        //Log.e("jsonObject", "object: " + object.toString());
                        jsonArray.put(object);
                    }
                    resultObject.put("data", jsonArray);
                } else {
                    resultObject.put("status", WatchConstants.SC_FAILURE);
                }
            }
            result.success(resultObject.toString());
        } catch (Exception exp) {
            Log.e("fetchAllHr24Exp::", exp.getMessage());
            // result.success(WatchConstants.SC_FAILURE);
        }
    }

    private void fetchOverAllDeviceData(Result result) {
        try {
            JSONObject overAllJson = new JSONObject();
            if (mUTESQLOperate != null) {
                overAllJson.put("status", WatchConstants.SC_SUCCESS);
                //steps data
                List<StepOneDayAllInfo> stepsInfoList = mUTESQLOperate.queryRunWalkAllDay();
                if (stepsInfoList != null) {
                    JSONArray stepsJsonArray = new JSONArray();
                    for (StepOneDayAllInfo info : stepsInfoList) {

                        JSONObject stepsObject = new JSONObject();
                        stepsObject.put("calender", info.getCalendar());
                        stepsObject.put("steps", info.getStep());
                        stepsObject.put("calories", GlobalMethods.convertDoubleToStringWithDecimal(info.getCalories()));
                        stepsObject.put("distance", GlobalMethods.convertDoubleToStringWithDecimal(info.getDistance()));
                        ArrayList<StepOneHourInfo> stepOneHourInfoArrayList = info.getStepOneHourArrayInfo();
                        JSONArray stepsArray = new JSONArray();
                        for (StepOneHourInfo stepOneHourInfo : stepOneHourInfoArrayList) {
                            JSONObject objStep = new JSONObject();
                            objStep.put("step", stepOneHourInfo.getStep());
                            objStep.put("time", GlobalMethods.getIntegerToHHmm(stepOneHourInfo.getTime()));
                            stepsArray.put(objStep);
                        }
                        stepsObject.put("data", stepsArray);

                       /* Log.e("calender:", " " + info.getCalendar());
                        Log.e("steps:", " " + info.getStep());
                        Log.e("calories:", " " + info.getCalories());
                        Log.e("distance:", " " + info.getDistance());

                        Log.e("getRunSteps:", " " + info.getRunSteps());
                        Log.e("getRunCalories:", " " + info.getRunCalories());
                        Log.e("getRunDistance:", " " + info.getRunDistance());
                        Log.e("getRunHourDetails:", " " + info.getRunHourDetails());
                        Log.e("getRunDurationTime:", " " + info.getRunDurationTime());

                        Log.e("getWalkSteps:", " " + info.getWalkSteps());
                        Log.e("getWalkCalories:", " " + info.getWalkCalories());
                        Log.e("getWalkDistance:", " " + info.getWalkDistance());
                        Log.e("getWalkHourDetails:", " " + info.getWalkHourDetails());
                        Log.e("getWalkDurationTime:", " " + info.getWalkDurationTime());*/

//                        ArrayList<StepRunHourInfo>  stepRunHourInfoList = info.getStepRunHourArrayInfo();
//                        ArrayList<StepWalkHourInfo>  stepWalkHourInfoList = info.getStepWalkHourArrayInfo();
//                        ArrayList<StepWalkHourInfo>  stepWalkHourInfoList = info.get();

                        stepsJsonArray.put(stepsObject);
                    }
                    overAllJson.put("steps", stepsJsonArray);
                }

                //sleep data
                List<SleepTimeInfo> sleepInfoList = mUTESQLOperate.queryAllSleepInfo();
                if (sleepInfoList != null) {
                    JSONArray sleepJsonArray = new JSONArray();
                    for (SleepTimeInfo sleepTimeInfo : sleepInfoList) {
                        JSONObject sleepObject = new JSONObject();
                        sleepObject.put("calender", sleepTimeInfo.getCalendar());
                        sleepObject.put("total", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getSleepTotalTime()));
                        sleepObject.put("light", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getLightTime()));
                        sleepObject.put("deep", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getDeepTime()));
                        sleepObject.put("awake", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getAwakeTime()));
                        sleepObject.put("beginTime", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getBeginTime()));
                        sleepObject.put("endTime", GlobalMethods.getTimeByIntegerMin(sleepTimeInfo.getEndTime()));

                        sleepObject.put("totalNum", "" + sleepTimeInfo.getSleepTotalTime());
                        sleepObject.put("lightNum", "" + sleepTimeInfo.getLightTime());
                        sleepObject.put("deepNum", "" + sleepTimeInfo.getDeepTime());
                        sleepObject.put("awakeNum", "" + sleepTimeInfo.getAwakeTime());
                        sleepObject.put("beginTimeNum", "" + sleepTimeInfo.getBeginTime());
                        sleepObject.put("endTimeNum", "" + sleepTimeInfo.getEndTime());
                        List<SleepInfo> sleepDataList = sleepTimeInfo.getSleepInfoList();
                        JSONArray sleepDataArray = new JSONArray();
                        for (SleepInfo sleepInfo : sleepDataList) {
                            JSONObject obj = new JSONObject();
                            obj.put("state", sleepInfo.getColorIndex()); // deep sleep: 0, Light sleep: 1,  awake: 2
                            obj.put("startTime", GlobalMethods.getTimeByIntegerMin(sleepInfo.getStartTime()));
                            obj.put("endTime", GlobalMethods.getTimeByIntegerMin(sleepInfo.getEndTime()));
                            // obj.put("diffTime", GlobalMethods.getTimeByIntegerMin(sleepInfo.getDiffTime()));

                            obj.put("startTimeNum", "" + sleepInfo.getStartTime());
                            obj.put("endTimeNum", "" + sleepInfo.getEndTime());
                            // obj.put("diffTimeNum", "" + sleepInfo.getDiffTime());
                            sleepDataArray.put(obj);
                        }
                        sleepObject.put("data", sleepDataArray);
                        sleepJsonArray.put(sleepObject);
                    }
                    overAllJson.put("sleep", sleepJsonArray);
                }

                //heart rate data
                List<Rate24HourDayInfo> rate24InfoList = mUTESQLOperate.query24HourRateAllInfo();
                if (rate24InfoList != null) {
                    JSONArray rate24Array = new JSONArray();
                    for (Rate24HourDayInfo rate24HourDayInfo : rate24InfoList) {
                        JSONObject objectRate = new JSONObject();
                        objectRate.put("calender", rate24HourDayInfo.getCalendar());
                        objectRate.put("time", GlobalMethods.getTimeByIntegerMin(rate24HourDayInfo.getTime()));
                        objectRate.put("rate", rate24HourDayInfo.getRate());
                        //Log.e("jsonObject", "object: " + object.toString());
                        rate24Array.put(objectRate);
                    }
                    overAllJson.put("hr24", rate24Array);
                }

                // bp data
                List<BPVOneDayInfo> bpInfoList = mUTESQLOperate.queryAllBloodPressureInfo();
                if (bpInfoList != null) {
                    JSONArray bpArray = new JSONArray();
                    for (BPVOneDayInfo bpvOneDayInfo : bpInfoList) {
                        JSONObject bpObject = new JSONObject();
                        bpObject.put("calender", bpvOneDayInfo.getCalendar());
                        bpObject.put("time", GlobalMethods.getTimeByIntegerMin(bpvOneDayInfo.getBloodPressureTime()));
                        bpObject.put("high", bpvOneDayInfo.getHightBloodPressure());
                        bpObject.put("low", bpvOneDayInfo.getLowBloodPressure());
                        bpArray.put(bpObject);
                    }
                    overAllJson.put("bp", bpArray);
                }

                //temperature data
                List<TemperatureInfo> temperatureInfoList = mUTESQLOperate.queryTemperatureAll();
                if (temperatureInfoList != null) {
                    Log.e("temperatureInfoList::", "size>>" + temperatureInfoList.size());
                    if (temperatureInfoList != null) {
                        for (TemperatureInfo tempInfo : temperatureInfoList) {
                            Log.e("tempInfoItem", "Cal" + tempInfo.getCalendar() + " - " + tempInfo.getBodyTemperature());
                        }
                    }
                    JSONArray temperatureArray = new JSONArray();
                    for (TemperatureInfo temperatureInfo : temperatureInfoList) {
                        JSONObject temperatureObject = new JSONObject();
                        temperatureObject.put("calender", temperatureInfo.getCalendar());
                        // temperatureObject.put("type", "" + temperatureInfo.getType());
                        temperatureObject.put("inCelsius", "" + GlobalMethods.convertDoubleToCelsiusWithDecimal(temperatureInfo.getBodyTemperature()));
                        temperatureObject.put("inFahrenheit", "" + GlobalMethods.getTempIntoFahrenheit(temperatureInfo.getBodyTemperature()));
                        //temperatureObject.put("startDate", "" + temperatureInfo.getStartDate()); //yyyyMMddHHmmss
                        temperatureObject.put("time", "" + GlobalMethods.convertIntToHHMmSs(temperatureInfo.getSecondTime()));
                        //temperatureObject.put("time1", "" + GlobalMethods.convertTimeToHHMm(temperatureInfo.getStartDate()));
                        temperatureArray.put(temperatureObject);
                    }
                    overAllJson.put("temperature", temperatureArray);
                }

                List<OxygenInfo> oxygenInfoList = mUTESQLOperate.queryOxygenAll();
                if (oxygenInfoList != null) {
                    JSONArray oxygenArray = new JSONArray();
                    for (OxygenInfo oxygenInfo : oxygenInfoList) {
                        JSONObject oxygenObject = new JSONObject();
                        oxygenObject.put("calender", oxygenInfo.getCalendar());
                        oxygenObject.put("value", "" + oxygenInfo.getOxygenValue());
                        // oxygenObject.put("startDate", "" + oxygenInfo.getStartDate()); //yyyyMMddHHmmss
                        oxygenObject.put("time", "" + GlobalMethods.getTimeByIntegerMin(oxygenInfo.getTime()));
                        oxygenArray.put(oxygenObject);
                    }
                    overAllJson.put("oxygen", oxygenArray);
                }

                result.success(overAllJson.toString());
            } else {
                result.success(overAllJson.toString());
            }

        } catch (Exception exp) {
            Log.e("fetchOverAllExp::", exp.getMessage());
            result.success(WatchConstants.SC_FAILURE);
        }
    }


    // start -stop test
    private void startOxygenSaturation(Result result) {
        String resultant;
        boolean isSupported = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_OXYGEN);
        if (isSupported) {
            if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
                if (mWriteCommand != null) {
                    mWriteCommand.startOxygenTest();
                    //result.success(WatchConstants.SC_INIT);
                    resultant = WatchConstants.SC_INIT;
                } else {
                    // result.success(WatchConstants.SC_FAILURE);
                    resultant = WatchConstants.SC_FAILURE;
                }
            } else {
                //result.success(WatchConstants.SC_DISCONNECTED);
                resultant = WatchConstants.SC_DISCONNECTED;
            }
        } else {
            //result.success(WatchConstants.SC_NOT_SUPPORTED);
            resultant = WatchConstants.SC_NOT_SUPPORTED;
        }

        uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
    }

    private void stopOxygenSaturation(Result result) {
        String resultant;
        boolean isSupported = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_OXYGEN);
        if (isSupported) {
            if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
                if (mWriteCommand != null) {
                    mWriteCommand.stopOxygenTest();
                    //result.success(WatchConstants.SC_INIT);
                    resultant = WatchConstants.SC_INIT;
                } else {
                    //result.success(WatchConstants.SC_FAILURE);
                    resultant = WatchConstants.SC_FAILURE;
                }
            } else {
                //result.success(WatchConstants.SC_DISCONNECTED);
                resultant = WatchConstants.SC_DISCONNECTED;
            }
        } else {
            //result.success(WatchConstants.SC_NOT_SUPPORTED);
            resultant = WatchConstants.SC_NOT_SUPPORTED;
        }
        uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
    }

    private void startBloodPressure(Result result) {
        String resultant;
        if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
            if (mWriteCommand != null) {
                mWriteCommand.sendBloodPressureTestCommand(GlobalVariable.BLOOD_PRESSURE_TEST_START);
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
        } else {
            //result.success(WatchConstants.SC_DISCONNECTED);
            resultant = WatchConstants.SC_DISCONNECTED;
        }
        uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
    }

    private void stopBloodPressure(Result result) {
        String resultant;
        if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
            if (mWriteCommand != null) {
                mWriteCommand.sendBloodPressureTestCommand(GlobalVariable.BLOOD_PRESSURE_TEST_STOP);
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
        } else {
            //result.success(WatchConstants.SC_DISCONNECTED);
            resultant = WatchConstants.SC_DISCONNECTED;
        }
        uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
    }

    /*private void startHeartRate(Result result) {
        String resultant;
        if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
            if (mWriteCommand != null) {
                mWriteCommand.sendRateTestCommand(GlobalVariable.RATE_TEST_START);
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
               // result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
        } else {
            //result.success(WatchConstants.SC_DISCONNECTED);
            resultant = WatchConstants.SC_DISCONNECTED;
        }
        uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
    }

    private void stopHeartRate(Result result) {
        String resultant;
        if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
            if (mWriteCommand != null) {
                mWriteCommand.sendRateTestCommand(GlobalVariable.RATE_TEST_STOP);
                //result.success(WatchConstants.SC_INIT);
                resultant = WatchConstants.SC_INIT;
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
        } else {
            //result.success(WatchConstants.SC_DISCONNECTED);
            resultant = WatchConstants.SC_DISCONNECTED;
        }
        uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
    }*/

    private void startTempTest(Result result) {
        String resultant;
        if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
            if (mWriteCommand != null) {
                if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_TEMPERATURE_TEST)) {
                    mWriteCommand.queryCurrentTemperatureData();
                    //result.success(WatchConstants.SC_INIT);
                    resultant = WatchConstants.SC_INIT;
                } else {
                    // result.success(WatchConstants.SC_NOT_SUPPORTED);
                    resultant = WatchConstants.SC_NOT_SUPPORTED;
                }
            } else {
                //result.success(WatchConstants.SC_FAILURE);
                resultant = WatchConstants.SC_FAILURE;
            }
        } else {
            //result.success(WatchConstants.SC_DISCONNECTED);
            resultant = WatchConstants.SC_DISCONNECTED;
        }
        uiThreadHandler.postDelayed(() -> result.success(resultant), RETURN_DELAY_MS);
    }


    private void setBandDialProgressStatus(boolean status) {
        if (sharedEditor != null) {
            Log.e("getProgressStatus:", "Progress: " + status);
            sharedEditor.putBoolean(BAND_FACE_PROGRESS, status);
            sharedEditor.apply();
        }
    }

    private boolean getBandDialProgressStatus() {
        if (sharedPref != null) {
            //Log.e("getProgressStatus:", "Progress: "+isBandFaceRunning);
            return sharedPref.getBoolean(BAND_FACE_PROGRESS, false);
        } else {
            return false;
        }
    }


    private void pushDialFaceProgressCallBack(final JSONObject data, int progress) {
        boolean isBandFaceRunning = false;
        if (progress == 100 || progress == 0) {
            isBandFaceRunning = false;
        } else if (progress > 0) {
            isBandFaceRunning = true;
        }
        setBandDialProgressStatus(isBandFaceRunning);
        Log.e("isBandFaceRunning:", "Progress: " + isBandFaceRunning);
//        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    JSONObject args = new JSONObject();
//                    args.put("status", WatchConstants.SC_SUCCESS);
//                    args.put("result", WatchConstants.WATCH_DIAL_PROGRESS_STATUS);
//                    args.put("data", data);
//                    sendEventToDart(args, WatchConstants.SMART_BP_TEST_CHANNEL);
//                } catch (Exception e) {
//                    // e.printStackTrace();
//                    Log.e("sendEventExp:", e.getMessage());
//                }
//            }
//        }, 1, TimeUnit.SECONDS);
        uiThreadHandler.postDelayed(() -> {
            try {
                JSONObject args = new JSONObject();
                args.put("status", WatchConstants.SC_SUCCESS);
                args.put("result", WatchConstants.WATCH_DIAL_PROGRESS_STATUS);
                args.put("data", data);
                sendEventToDart(args, WatchConstants.SMART_BP_TEST_CHANNEL);
            } catch (Exception e) {
                // e.printStackTrace();
                Log.e("sendEventExp:", e.getMessage());
            }
        }, 700);
    }

    private void pushJsonEventSuccessFailure(final String result, final JSONObject data, final String status) {
        //Log.e("isBandFaceRunning:", "SuccessFailure: "+isBandFaceRunning);
        //if (!getBandDialProgressStatus()){
        uiThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject args = new JSONObject();
                    args.put("status", status);
                    args.put("result", result);
                    args.put("data", data);
                    sendEventToDart(args, WatchConstants.SMART_EVENT_CHANNEL);
                } catch (Exception e) {
                    // e.printStackTrace();
                    Log.e("sendEventExp:", e.getMessage());
                }
            }
        }, 300);
        // }

//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            try {
//                JSONObject args = new JSONObject();
//                args.put("status", status);
//                args.put("result", result);
//                args.put("data", data);
//                sendEventToDart(args, WatchConstants.SMART_EVENT_CHANNEL);
//            } catch (Exception e) {
//                // e.printStackTrace();
//                Log.e("sendEventExp:", e.getMessage());
//            }
//        }, 400);
    }

    private void pushJsonEventArrayCallBack(final String result, final JSONArray data, final String status) {
        uiThreadHandler.postDelayed(() -> {
            try {
                JSONObject args = new JSONObject();
                args.put("status", status);
                args.put("result", result);
                args.put("data", data);
                sendEventToDart(args, WatchConstants.SMART_EVENT_CHANNEL);
            } catch (Exception e) {
                // e.printStackTrace();
                Log.e("sendEventExp:", e.getMessage());
            }
        }, 50);
    }

    private void pushJsonEventObjCallBack(final String result, final JSONObject data, final String status) {
        uiThreadHandler.postDelayed(() -> {
            try {
                JSONObject args = new JSONObject();
                args.put("status", status);
                args.put("result", result);
                args.put("data", data);
                sendEventToDart(args, WatchConstants.SMART_EVENT_CHANNEL);
            } catch (Exception e) {
                // e.printStackTrace();
                Log.e("sendEventExp:", e.getMessage());
            }
        }, 50);
    }

    private void pushOtherEventCallBack(final String result, final JSONObject data, final String status) {
        uiThreadHandler.postDelayed(() -> {
            try {
                JSONObject args = new JSONObject();
                args.put("status", status);
                args.put("result", result);
                args.put("data", data);
                sendEventToDart(args, WatchConstants.SMART_BP_TEST_CHANNEL);
            } catch (Exception e) {
                // e.printStackTrace();
                Log.e("sendEventExp:", e.getMessage());
            }
        }, 100);
    }

    private void sendEventToDart(final JSONObject params, String channel) {
        Intent intent = new Intent();
        //intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction(WatchConstants.BROADCAST_ACTION_NAME);
        intent.putExtra("params", params.toString());
        intent.putExtra("channel", channel);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        //mContext.sendBroadcast(intent);
    }

    /*private boolean initializeData() {
        mobileConnect = new MobileConnect(this.mContext.getApplicationContext(), activity);
        bleServiceOperate = mobileConnect.getBLEServiceOperate();
        *//*bleServiceOperate.setServiceStatusCallback(new ServiceStatusCallback() {
            @Override
            public void OnServiceStatuslt(int status) {
                if (status == ICallbackStatus.BLE_SERVICE_START_OK) {
                    Log.e("inside_service_result", "" + mBluetoothLeService);
                    if (mBluetoothLeService == null) {
                        startListeningCallback(true);
                    }
                }
            }
        });*//*
        mBluetoothLeService = bleServiceOperate.getBleService();
        if (mBluetoothLeService != null) {
            startListeningCallback(false);
        }
        return true;
    }*/

   /* private void setUpDataEngine( BinaryMessenger binaryMessenger){
        try{
            Log.e("inside_set_up_engine", "binaryMessenger");
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                Log.e("inside_set_up_engine", "");
//                mCallbackChannel = new MethodChannel(binaryMessenger, WatchConstants.SMART_CALLBACK);
//                mCallbackChannel.setMethodCallHandler(callbacksHandler);
                }
            });
        }catch (Exception exp){
            Log.e("set_up_engine_exp", exp.getMessage());
        }
    }*/

    /*private Handler mHandlerMessage = new Handler() {
        public void handleMessage(Message msg) {
            Log.e("Msg_What_Handler: ", "" + msg.what);
            switch (msg.what) {
                case GlobalVariable.GET_RSSI_MSG:
                    Bundle bundle = msg.getData();
                    Log.e("GET_RSSI_MSG: ", bundle.getInt(GlobalVariable.EXTRA_RSSI) + "");
                    break;
            }
        }
    };*/

        /*MethodCallHandler callbacksHandler = new MethodCallHandler() {
        @Override
        public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
            try{
                updateCallBackHandler(call,result);
            }catch (Exception exp){
                Log.e("callbacksHandlerExp:",""+exp.getMessage());
            }
        }
    };*/

    /*private void updateCallBackHandler(MethodCall call, Result result) {
        final String method = call.method;
        Log.e("calling_method", "callbacksHandler++ " + method); // startListening
        //WatchConstants.START_LISTENING.equalsIgnoreCase(method)
        //if ("startListening".equals(method)) {
        if (WatchConstants.START_LISTENING.equalsIgnoreCase(method)) {
            startListening(call.arguments, result);
        } else {
            result.notImplemented();
        }
    }*/


    /*private void startListening(Object arguments, Result rawResult) {
        try {
            // Get callback id
            String callbackName = (String) arguments;

            Log.e("callbackName", "start_listener " + callbackName);// smartCallbacks

            if (callbackName.equals(WatchConstants.SMART_CALLBACK)) {
                validateDeviceListCallback = true;
            }

            Map<String, Object> args = new HashMap<>();
            args.put("id", callbackName);
            mCallbacks.put(callbackName, args);

            rawResult.success(null);

        } catch (Exception exp) {
            Log.e("startListeningExp:", exp.getMessage());
        }
    }

    private void cancelListening(Object args, MethodChannel.Result result) {
        // Get callback id
        //  int currentListenerId = (int) args;
        String callbackName = (String) args;
        Log.e("callbackName", "cancel_listener " + callbackName);
        // Remove callback
        mCallbacks.remove(callbackName);
        // Do additional stuff if required to cancel the listener
        result.success(null);
    }*/

   /* private void pushOxygenEventCallBack(final String result, final JSONObject data, final String status) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject args = new JSONObject();
                    args.put("status", status);
                    args.put("result", result);
                    args.put("data", data);
                    sendEventToDart(args, WatchConstants.SMART_OXYGEN_TEST_CHANNEL);
                } catch (Exception e) {
                    // e.printStackTrace();
                    Log.e("sendEventExp:", e.getMessage());
                }
            }
        }, 500);
    }*/

    /* private void pushTemperatureEventCallBack(final String result, final JSONObject data, final String status) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject args = new JSONObject();
                    args.put("status", status);
                    args.put("result", result);
                    args.put("data", data);
                    sendEventToDart(args, WatchConstants.SMART_TEMP_TEST_CHANNEL);
                } catch (Exception e) {
                    // e.printStackTrace();
                    Log.e("sendEventExp:", e.getMessage());
                }
            }
        }, 500);
    }*/


    /*private void runOnUIThread(final String result, final JSONObject data, final String callbackName, final String status) {
        try {
            *//*activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("runOnUIThread", "Calling runOnUIThread with activity: " + data);

                    try {
                        JSONObject args = new JSONObject();
                        args.put("id", callbackName);
                        args.put("status", status);
                        args.put("result", result);
                        args.put("data", data);
                        Log.e("mCallbackChannel:: ", ""+mCallbackChannel);
                        mCallbackChannel.invokeMethod(WatchConstants.CALL_LISTENER, args.toString());

                    } catch (Exception e) {
                        // e.printStackTrace();
                        Log.e("data_run_exp:", e.getMessage());
                    }
                }
            });*//*
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject args = new JSONObject();
                        args.put("id", callbackName);
                        args.put("status", status);
                        args.put("result", result);
                        args.put("data", data);
                        Log.e("mCallbackChannel2:: ", "" + mCallbackChannel);
                        mCallbackChannel.invokeMethod(WatchConstants.CALL_LISTENER, args.toString());

                    } catch (Exception e) {
                        // e.printStackTrace();
                        Log.e("data_run_exp2:", e.getMessage());
                    }
                }
            });
            //  final String result
            // uiThreadHandler
            *//*new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {

                }
            });*//*
     *//* new Handler(Looper.getMainLooper()).post(new Runnable() {
                 @Override
                 public void run() {
                 Log.e("runOnUIThread", "Calling runOnUIThread with: " + data);

                  try {
                       JSONObject args = new JSONObject();
                       args.put("id", callbackName);
                       args.put("status", status);
                       args.put("result", result);
                       args.put("data", data);
                       Log.e("mCallbackChannel:: ", ""+mCallbackChannel);
                       mCallbackChannel.invokeMethod(WatchConstants.CALL_LISTENER, args.toString());

                   } catch (Exception e) {
                       // e.printStackTrace();
                       Log.e("data_run_exp:", e.getMessage());
                   }
                }
              }
            );*//*
        } catch (Exception exp) {
            Log.e("onUIThreadPushExp: ", "" + exp.getMessage());
        }
    }*/


  /* private void updateConnectionStatus(boolean status) {
        uiThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                flutterResultBluConnect.success(status);
            }
        },200);
        //getMainExecutor().
    }
    private void updateConnectionStatus2(boolean status) {
        try {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    flutterResultBluConnect.success(status);
                    Log.e("updateConnectionStatus2", "return success");
                }
            });
        }catch (Exception exp) {
            Log.e("updateConnectionStatus2", exp.getMessage());
        }
        *//*activity.getMainExecutor().post(new Runnable() {
            @Override
            public void run() {
                flutterResultBluConnect.success(status);
            }
        });*//*
     *//*  activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                flutterResultBluConnect.success(status);
            }.
        });*//*
    }
    private void updateConnectionStatus3(boolean status) {
        try {
            this.flutterResultBluConnect.success(status);
            Log.e("updateConnectionStatus3", "flutterResultBluConnectSuccess");
        }catch (Exception exp) {
            Log.e("updateConnectionStatus3", exp.getMessage());
        }
    }*/

   /* @Override
    public void OnServiceStatuslt(int status) {
        if (status == ICallbackStatus.BLE_SERVICE_START_OK) {
            Log.e("inside_service_result", ""+mBluetoothLeService);
           // LogUtils.d(TAG, "OnServiceStatuslt mBluetoothLeService11 ="+mBluetoothLeService);
            if (mBluetoothLeService == null) {
                mBluetoothLeService = mobileConnect.getBLEServiceOperate().getBleService();
                mobileConnect.setBluetoothLeService(mBluetoothLeService);
                mBluetoothLeService.setICallback(this);
                mBluetoothLeService.setRateCalibrationListener(this);
                mBluetoothLeService.setTurnWristCalibrationListener(this);
                mBluetoothLeService.setTemperatureListener(this);
                mBluetoothLeService.setOxygenListener(this);
                *//*if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_TEMPERATURE_TEST)) {
                    mBluetoothLeService.setTemperatureListener(this);
                }
                if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_OXYGEN)) {
                    mBluetoothLeService.setOxygenListener(this);
                }*//*
                mBluetoothLeService.setBreatheRealListener(this);
                Log.e("inside_service_result", "listeners"+mBluetoothLeService);
            }
        }
    }*/

   /* private final OxygenRealListener oxygenRealListener = new OxygenRealListener() {
        @Override
        public void onTestResult(int status, OxygenInfo oxygenInfo) {
            Log.e("oxygenRealListener", "value: " + oxygenInfo.getOxygenValue() + ", status: " + status);
        }
    };*/

   /* private final TemperatureListener temperatureListener = new TemperatureListener() {
        @Override
        public void onTestResult(TemperatureInfo temperatureInfo) {

            Log.e("temperatureListener", "temperature: " + temperatureInfo.getBodyTemperature() + ", type: " + temperatureInfo.getType());
            try {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = new JSONObject();
//                jsonObject.put("calender", "" + temperatureInfo.getCalendar());
//                jsonObject.put("type", "" + temperatureInfo.getType());
//                jsonObject.put("bodyTemp", "" + temperatureInfo.getBodyTemperature());
//                jsonObject.put("ambientTemp", "" + temperatureInfo.getAmbientTemperature());
//                jsonObject.put("surfaceTemp", "" + temperatureInfo.getBodySurfaceTemperature());
//                jsonObject.put("startDate", "" + temperatureInfo.getStartDate());
//                jsonObject.put("time", "" + temperatureInfo.getSecondTime());

                        try {
                            jsonObject.put("calender", temperatureInfo.getCalendar());
                            jsonObject.put("type", "" + temperatureInfo.getType());
                            jsonObject.put("inCelsius", "" + GlobalMethods.convertDoubleToStringWithDecimal(temperatureInfo.getBodyTemperature()));
                            jsonObject.put("inFahrenheit", "" + GlobalMethods.getTempIntoFahrenheit(temperatureInfo.getBodyTemperature()));
                            jsonObject.put("startDate", "" + temperatureInfo.getStartDate()); //yyyyMMddHHmmss
                            jsonObject.put("time", "" + GlobalMethods.convertIntToHHMmSs(temperatureInfo.getSecondTime()));

                            Log.e("onTestResult", "object: " + jsonObject.toString());

                        } catch (Exception e) {
                           // e.printStackTrace();
                            Log.e("onTestResultJSONExp:", e.getMessage());
                        }

                        runOnUIThread(WatchConstants.TEMP_RESULT, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                    }
                });

            } catch (Exception exp) {
                Log.e("onTestResultExp:", exp.getMessage());
            }

        }

        @Override
        public void onSamplingResult(TemperatureInfo temperatureInfo) {

        }
    };*/

   /* private final RateChangeListener mOnRateListener = new RateChangeListener() {
        @Override
        public void onRateChange(int rate, int status) {
            Log.e("onRateListener", "rate: " + rate + ", status: " + status);
            try {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("hr", "" + rate);
                        } catch (Exception e) {
                           // e.printStackTrace();
                            Log.e("onRateJSONExp: ", e.getMessage());
                        }
                        runOnUIThread(WatchConstants.HR_REAL_TIME, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                    }
                });
            } catch (Exception exp) {
                Log.e("onRateExp: ", exp.getMessage());
            }
        }
    };*/

    /*private final RateOf24HourRealTimeListener mOnRateOf24HourListener = new RateOf24HourRealTimeListener() {
        @Override
        public void onRateOf24HourChange(int maxHeartRateValue, int minHeartRateValue, int averageHeartRateValue, boolean isRealTimeValue) {
            //Monitor the maximum, minimum, and average values of the 24-hour heart rate bracelet.
            // Need to enter the heart rate test interface on the wristband (or call the synchronization method) to get the value
            Log.e("onRateOf24Hour", "maxHeartRateValue: " + maxHeartRateValue + ", minHeartRateValue: " + minHeartRateValue + ", averageHeartRateValue=" + averageHeartRateValue + ", isRealTimeValue=" + isRealTimeValue);
        }
    };*/

   /* private final SleepChangeListener mOnSleepChangeListener = new SleepChangeListener() {
        @Override
        public void onSleepChange() {
            try{
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("onSleepChangeCalender", CalendarUtils.getCalendar(0));
                        SleepTimeInfo sleepTimeInfo = UTESQLOperate.getInstance(mContext).querySleepInfo(CalendarUtils.getCalendar(0));
                        int deepTime, lightTime, awakeCount, sleepTotalTime;
                        if (sleepTimeInfo != null) {
                            deepTime = sleepTimeInfo.getDeepTime();
                            lightTime = sleepTimeInfo.getLightTime();
                            awakeCount = sleepTimeInfo.getAwakeCount();
                            sleepTotalTime = sleepTimeInfo.getSleepTotalTime();
                            Log.e("sleepTimeInfo", "deepTime: " + deepTime + ", lightTime: " + lightTime + ", awakeCount=" + awakeCount + ", sleepTotalTime=" + sleepTotalTime);
                        }
                    }
                });
            }catch (Exception exp){
                Log.e("onSleepChangeExp::", exp.getMessage());
            }

        }
    };

    private final BloodPressureChangeListener mOnBloodPressureListener = new BloodPressureChangeListener() {

        @Override
        public void onBloodPressureChange(int highPressure, int lowPressure, int status) {
            Log.e("onBloodPressureChange", "highPressure: " + highPressure + ", lowPressure: " + lowPressure + ", status=" + status);
            try {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("high", "" + highPressure);
                            jsonObject.put("low", "" + lowPressure);
                            jsonObject.put("status", "" + status);
                            runOnUIThread(WatchConstants.BP_RESULT, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                        } catch (Exception e) {
                            //e.printStackTrace();
                            Log.e("bpChangeJSONExp::", e.getMessage());
                        }
                    }
                });

            } catch (Exception exp) {
                Log.e("bpChangeExp::", exp.getMessage());
            }
        }
    };

    private final StepChangeListener mOnStepChangeListener = new StepChangeListener() {
        @Override
        public void onStepChange(StepOneDayAllInfo info) {
            try {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (info != null) {
                            //Log.e("onStepChange1", "calendar: " + info.getCalendar());
                            Log.e("onStepChange2", "mSteps: " + info.getStep() + ", mDistance: " + info.getDistance() + ", mCalories=" + info.getCalories());
                            Log.e("onStepChange3", "mRunSteps: " + info.getRunSteps() + ", mRunDistance: " + info.getRunDistance() + ", mRunCalories=" + info.getRunCalories() + ", mRunDurationTime=" + info.getRunDurationTime());
                            Log.e("onStepChange4", "mWalkSteps: " + info.getWalkSteps() + ", mWalkDistance: " + info.getWalkDistance() + ", mWalkCalories=" + info.getWalkCalories() + ", mWalkDurationTime=" + info.getWalkDurationTime());
                            Log.e("onStepChange5", "getStepOneHourArrayInfo: " + info.getStepOneHourArrayInfo() + ", getStepRunHourArrayInfo: " + info.getStepRunHourArrayInfo() + ", getStepWalkHourArrayInfo=" + info.getStepWalkHourArrayInfo());

                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("steps", "" + info.getStep());
                                //   jsonObject.put("distance", ""+info.getDistance());
                                //  jsonObject.put("calories", ""+info.getCalories());
                                jsonObject.put("distance", "" + GlobalMethods.convertDoubleToStringWithDecimal(info.getDistance()));
                                jsonObject.put("calories", "" + GlobalMethods.convertDoubleToStringWithDecimal(info.getCalories()));
                                runOnUIThread(WatchConstants.STEPS_REAL_TIME, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);

                            } catch (Exception e) {
                               // e.printStackTrace();
                                Log.e("onStepJSONExp::", e.getMessage());
                            }

                        }
                    }
                });

            } catch (Exception exp) {
                Log.e("onStepChangeExp::", exp.getMessage());
                // runOnUIThread(WatchConstants.STEPS_REAL_TIME, new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
            }
        }
    };*/

   /* private class AsyncExecuteUpdate extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            p = new ProgressDialog(MainActivity.this);
//            p.setMessage("Please wait...It is downloading");
//            p.setIndeterminate(false);
//            p.setCancelable(false);
//            p.show();
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                if (mWriteCommand != null) {
                    mWriteCommand.syncAllStepData();
                    mWriteCommand.syncAllSleepData();
                    mWriteCommand.syncRateData();
                   *//*mWriteCommand.syncAllRateData();
                    if (isSupport24HrRate) {
                        mWriteCommand.sync24HourRate();
                    }*//*
                    mWriteCommand.syncAllBloodPressureData();
                    mWriteCommand.syncAllTemperatureData();
                    if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_OXYGEN)){
                        mWriteCommand.syncOxygenData();
                    }
                    return WatchConstants.SC_SUCCESS;
                }else{
                    return WatchConstants.SC_FAILURE;
                }
               // return WatchConstants.SC_SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
                return WatchConstants.SC_FAILURE;
            }
        }
        @Override
        protected void onPostExecute(String bitmap) {
            super.onPostExecute(bitmap);

        }
    }*/
/*    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("act_resultCode", "" + resultCode);
        Log.e("act_requestCode", "" + requestCode);
        try {
            if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
                this.flutterInitResultBlu.success(WatchConstants.SC_CANCELED);
            } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
                // do the statement
                String resultStatus = this.mobileConnect.startListeners();
                Log.e("act_res_status", "" + resultStatus);

                // String result = deviceConnect.startDevicesScan();
                this.flutterInitResultBlu.success(resultStatus);
            } else {
                Log.e("inside_else", "" + "nothing to do here");
                // do nothing
            }
           // return false;
        } catch (Exception exp) {
            Log.e("act_result_exp:", "" + exp.getMessage());
            //return false;
        }
        return false;
    }*/

    /*private void initBlueServices() {
        //boolean connectionStatus
        Log.e("mBluetoothLeService::", "initBlueServices");
        try{
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeService.setICallback(new ICallback() {
                        @Override
                        public void OnResult(boolean status, int result) {
                            Log.e("onResult:", "status>> " + status + " resultValue>> " + result);
                            try {
                                JSONObject jsonObject = new JSONObject();
                                switch (result) {
                                    case ICallbackStatus.GET_BLE_VERSION_OK:
                                        String deviceVersion = SPUtil.getInstance(mContext).getImgLocalVersion();
                                        Log.e("deviceVersion::", deviceVersion);
                                        jsonObject.put("deviceVersion", deviceVersion);
                                        // deviceVersionIDResult.success(jsonObject.toString());
                                        // runOnUIThread(new JSONObject(), WatchConstants.DEVICE_VERSION, WatchConstants.SC_SUCCESS);
                                        runOnUIThread(WatchConstants.DEVICE_VERSION, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                                        break;
                                    case ICallbackStatus.GET_BLE_BATTERY_OK:
                                        //String deviceVer = SPUtil.getInstance(mContext).getImgLocalVersion();
                                        String batteryStatus = "" + SPUtil.getInstance(mContext).getBleBatteryValue();
                                        Log.e("batteryStatus::", batteryStatus);
                                        //jsonObject.put("deviceVersion", deviceVer);
                                        jsonObject.put("batteryStatus", batteryStatus);
                                        // runOnUIThread(jsonObject, WatchConstants.BATTERY_VERSION, WatchConstants.SC_SUCCESS);
                                        //deviceBatteryResult.success(jsonObject.toString());
                                        runOnUIThread(WatchConstants.BATTERY_STATUS, jsonObject, WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                                        break;
                                    // while connecting a device
                                    case ICallbackStatus.READ_CHAR_SUCCESS: // 137
                                        break;
                                    case ICallbackStatus.WRITE_COMMAND_TO_BLE_SUCCESS: // 148
                                        break;
                                    case ICallbackStatus.SYNC_TIME_OK: // 6
                                        //sync time ok
                                        break;

                                    case ICallbackStatus.SET_STEPLEN_WEIGHT_OK: // 8
                                        runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS, new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                                        break;
                                    case ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNCING: // 46
                                        // runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS,  new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                                        break;
                                    case ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNC_OK: // 47
                                        //runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS,  new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                                        break;

                                    case ICallbackStatus.BLOOD_PRESSURE_TEST_START: // 50
                                        //runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS,  new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                                        break;


                                    case ICallbackStatus.RATE_TEST_START: // 79
                                        // runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS,  new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                                        break;
                                    case ICallbackStatus.RATE_TEST_STOP: // 80
                                        // runOnUIThread(WatchConstants.UPDATE_DEVICE_PARAMS,  new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                                        break;

                                    case ICallbackStatus.CONNECTED_STATUS: // 20
                                        // connected successfully
                                        //runOnUIThread(new JSONObject(), WatchConstants.DEVICE_CONNECTED, WatchConstants.SC_SUCCESS);
                                        //flutterResultBluConnect.success(connectionStatus);
                                        runOnUIThread(WatchConstants.DEVICE_CONNECTED, new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                                        break;
                                    case ICallbackStatus.DISCONNECT_STATUS: // 19
                                        // disconnected successfully
                                        // mobileConnect.disconnectDevice();

                                        runOnUIThread(WatchConstants.DEVICE_DISCONNECTED, new JSONObject(), WatchConstants.SMART_CALLBACK, WatchConstants.SC_SUCCESS);
                                        // runOnUIThread(new JSONObject(), WatchConstants.DEVICE_DISCONNECTED, WatchConstants.SC_SUCCESS);
                                        break;
                                }
                            } catch (Exception exp) {
                                Log.e("ble_service_exp:", exp.getMessage());
                                runOnUIThread(WatchConstants.CALLBACK_EXCEPTION, new JSONObject(), WatchConstants.SERVICE_LISTENING, WatchConstants.SC_FAILURE);
                            }
                        }

                        @Override
                        public void OnDataResult(boolean status, int i, byte[] bytes) {
                            Log.e("OnDataResult:", "status>> " + status + "resultValue>> " + i);
                        }

                        @Override
                        public void onCharacteristicWriteCallback(int i) {
                            Log.e("onCharWriteCallback:", "status>> " + i);
                        }

                        @Override
                        public void onIbeaconWriteCallback(boolean b, int i, int i1, String s) {

                        }

                        @Override
                        public void onQueryDialModeCallback(boolean b, int i, int i1, int i2) {

                        }

                        @Override
                        public void onControlDialCallback(boolean b, int i, int i1) {

                        }

                        @Override
                        public void onSportsTimeCallback(boolean b, String s, int i, int i1) {

                        }

                        @Override
                        public void OnResultSportsModes(boolean b, int i, int i1, int i2, SportsModesInfo sportsModesInfo) {

                        }

                        @Override
                        public void OnResultHeartRateHeadset(boolean b, int i, int i1, int i2, HeartRateHeadsetSportModeInfo heartRateHeadsetSportModeInfo) {

                        }

                        @Override
                        public void OnResultCustomTestStatus(boolean b, int i, CustomTestStatusInfo customTestStatusInfo) {

                        }
                    });

                    if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_TEMPERATURE_TEST)) {
                        mBluetoothLeService.setTemperatureListener(temperatureListener);
                    }

                    if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_OXYGEN)) {
                        mBluetoothLeService.setOxygenListener(oxygenRealListener);
                    }
                }
            });


        }catch (Exception exp) {
            Log.e("mBluetoothLeExp::", exp.getMessage());
        }


    }*/
}
