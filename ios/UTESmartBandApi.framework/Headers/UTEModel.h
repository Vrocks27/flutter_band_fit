//
//  UTEModel.h
//  UTESmartBandApi
//
//  Created by VV on 2017/8/9.
//  Copyright © 2017年 vv. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UTEDefine.h"

@interface UTEModel : NSObject
@end

/*!
 *  UTEModelDevices
 *  This model shows all the attributes of the device and which functions are supported.
 *  Note:Many attributes have values only when the device is connected,Expressed by ①.
 */
@interface UTEModelDevices : NSObject
/**
 *  ①Full firmware version e.g.@"CVD07V000888"
 */
@property (nonatomic,copy  ) NSString *version;
/** ①Firmware name @"CVD07" */
@property (nonatomic,copy  ) NSString *versionName;
/** ①Firmware version number e.g.@"000888" */
@property (nonatomic,copy  ) NSString *versionNum;
/**
 *  ①Device patch version, default is 0
 */
@property (nonatomic,assign) NSInteger patch;
/**
 *  ①Device UI version @"V000234_U0001". See UTEModelDevices. isHasFirmwareUI
 */
@property (nonatomic,copy  ) NSString *versionUI;
/**
 *  Device name
 */
@property (nonatomic,copy  ) NSString *name;
/**
 *  Unique identification of device
 *  Note:If the same phone is connected to the same device A, the identifier will not change; if different phone are connected to the same device A, the identifier will be different.
 */
@property (nonatomic,copy  ) NSString *identifier;
/**
 *  ①Device battery
 *  It can be updated in real time through the setUTEOption:UTEOptionReadDevicesBattery.
 */
@property (nonatomic,assign) NSInteger battery;
/**
 *  Device connection status
 */
@property (nonatomic,assign) BOOL     isConnected;
/**
 *  ①Note:if isRKDevices=YES, Please ignore this parameter
 */
@property (nonatomic,assign) BOOL     isHasExtra;
/**
 *  ①Heart rate
 */
@property (nonatomic,assign) BOOL     isHasHRM;
/**
 *  ①Blood pressure
 */
@property (nonatomic,assign) BOOL     isHasBlood;
/**
 *  ①Blood pressure calibration
 */
@property (nonatomic,assign) BOOL     isHasBloodCalibrate;
/**
 *  ①Blood pressure timing automatic test
 */
@property (nonatomic,assign) BOOL     isHasBloodAutoTest;
/**
 *  ①Blood Pressure Custom
 */
@property (nonatomic,assign) BOOL     isHasBloodPressureCustom;
/**
 *  ①Weather function (support weather in 2 days)
 */
@property (nonatomic,assign) BOOL     isHasWeather;
/**
 *  ①Weather function (support weather in 7 days)
 */
@property (nonatomic,assign) BOOL     isHasWeatherSeven;
/**
 *  ①Screen vertical and horizontal switching
 */
@property (nonatomic,assign) BOOL     isHasOrientation;
/**
 *  ①Do Not Disturb
 */
@property (nonatomic,assign) BOOL     isHasSilence;
/**
 *  ①Raising the wrist, the screen lights up
 */
@property (nonatomic,assign) BOOL     isHasHandLight;
/**
 *  ①Maximum heart rate alert
 */
@property (nonatomic,assign) BOOL     isHasMaxHeartAlert;
/**
 *  ①Minimum heart rate alarm
 */
@property (nonatomic,assign) BOOL     isHasMinHeartAlert;
/**
 *  ①Support heart rate dynamic and static switching
 */
@property (nonatomic,assign) BOOL     isSwitchHeart;
/**
 *  ①Support dial replacement and left-hand and right-hand replacement
 */
@property (nonatomic,assign) BOOL     isHasSwitchHand;
/**
 *  ①Ibeacon
 */
@property (nonatomic,assign) BOOL     isHasIbeacon;
/**
 *  ①Support for walking and running
 */
@property (nonatomic,assign) BOOL     isHasWalkRun;
/**
 *  ①Rope Skipping
 */
@property (nonatomic,assign) BOOL     isHasSkip;
/**
 *  ①Swimming
 */
@property (nonatomic,assign) BOOL     isHasSwim;
/**
 *  ①Bicycle
 */
@property (nonatomic,assign) BOOL     isHasBicycle;
/**
 *  ①TableTennis
 */
@property (nonatomic,assign) BOOL     isHasTableTennis;
/**
 *  ①Badminton
 */
@property (nonatomic,assign) BOOL     isHasBadminton;
/**
 *  ①Tennis
 */
@property (nonatomic,assign) BOOL     isHasTennis;
/**
 *  ①Mountaineering
 */
@property (nonatomic,assign) BOOL     isHasMountaineering;
/**
 *  ①Tramp
 */
@property (nonatomic,assign) BOOL     isHasTramp;
/**
 *  ①Basketball
 */
@property (nonatomic,assign) BOOL     isHasBasketball;
/**
 *  ①Soccer
 */
@property (nonatomic,assign) BOOL     isHasSoccer_USA;
/**
 *  ①America Football
 */
@property (nonatomic,assign) BOOL     isHasFootball_USA;
/**
 *  ①Baseball
 */
@property (nonatomic,assign) BOOL     isHasBaseball;
/**
 *  ①Volleyball
 */
@property (nonatomic,assign) BOOL     isHasVolleyball;
/**
 *  ①Treadmill
 */
@property (nonatomic,assign) BOOL     isHasTreadmill;
/**
 *  ①Sit_Ups
 */
@property (nonatomic,assign) BOOL     isHasSit_Ups;
/**
 *  ①Cricket
 */
@property (nonatomic,assign) BOOL     isHasCricket;
/**
 *  ①Hockey
 */
@property (nonatomic,assign) BOOL     isHasHockey;
/**
 *  ①Dance
 */
@property (nonatomic,assign) BOOL     isHasDance;
/**
 *  ①SpinningCycling
 */
@property (nonatomic,assign) BOOL     isHasSpinningCycling;
/**
 *  ①Yoga
 */
@property (nonatomic,assign) BOOL     isHasYoga;
/**
 *  ①Gymnastics
 */
@property (nonatomic,assign) BOOL     isHasGymnastics;
/**
 *  ①Boating
 */
@property (nonatomic,assign) BOOL     isHasBoating;
/**
 *  ①Jumping_Jacks
 */
@property (nonatomic,assign) BOOL     isHasJumping_Jacks;
/**
 *  ①Custom training
 */
@property (nonatomic,assign) BOOL     isHasFreeSport;
/**
 *  Support more sports types (hundreds of types ,See UTEDeviceSportMode)
 */
@property (nonatomic,assign) BOOL     isHasMoreSportType;
/**
 *  ①The sports items (UTEDeviceSportModeWalking, UTEDeviceSportModeRunning, UTEDeviceSportModeMountaineering, UTEDeviceSportModeTreadmill.), accumulate the number of steps and calories generated, and then add them to the main interface of the device to display the number of steps and calories.
 *  When isHasMoreSportType=YES, also need to sports items(UTEDeviceSportModeIndoorWalking, UTEDeviceSportModeIndoorRunning, UTEDeviceSportModeStepping, UTEDeviceSportModeOutdoorWalking, UTEDeviceSportModeTrailRunning, UTEDeviceSportModeParkour, UTEDeviceSportModeMarathon)
 *
 *  However, the data (steps, calories) obtained by the application synchronously, rather than accumulating each other, so developers need to accumulate the data by themselves.
 *  In this way, the device can be consistent with the application's step count and calorie data.
 */
@property (nonatomic,assign) BOOL     isHasAddAllDataDisplay;

/**
 *  ①New distance and calorie calculation strategies
 */
@property (nonatomic,assign) BOOL     isHasNewCalculate;
/**
 *  ①Support for multiple alarm clock (up to three)
 */
@property (nonatomic,assign) BOOL     isHasMulClock;
/**
 *  ①Support Set an alarm on the device
 */
@property (nonatomic,assign) BOOL     isHasLocalAlarmClock;
/**
 *  ①After sitting for a long time, the device will remind
 */
@property (nonatomic,assign) BOOL     isHasRemindSit;
/**
 *  ①Task reminder
 */
@property (nonatomic,assign) BOOL     isHasRemindTask;
/**
 *  ①Whether the successfully connected device has an upgrade function (if there is, there is no need for an additional operation: invoke method changeDeviveFeature:)
 */
@property (nonatomic,assign) BOOL     isHasUpdateFunction;
/**
 *  ①UV
 */
@property (nonatomic,assign) BOOL     isHasUV;
/**
 *  ①Support WeChat ranking
 */
@property (nonatomic,assign) BOOL     isHasWechat;
/**
 *  ①WeChat protocol 5 mode.(WeChat Streamlined Agreement)
 */
@property (nonatomic,assign) BOOL     isWechatModel5;
/**
 *  ①Can set age and gender, and more
 */
@property (nonatomic,assign) BOOL     isHasAgeSex;
/**
 *  ①Support rejected call function
 */
@property (nonatomic,assign) BOOL     isHasHangup;
/**
 *  ①Heart rate assisted sleep monitoring
 */
@property (nonatomic,assign) BOOL     isHasSleepAux;
/**
 *  ①GPS
 */
@property (nonatomic,assign) BOOL     isHasGPS;
/**
 *  ①Can set time period（After sitting for a long time, the device will remind）
 */
@property (nonatomic,assign) BOOL     isHasSitRemindDuration;
/**
 *  ①Support multiple languages
 */
@property (nonatomic,assign) BOOL     isHasMultiLanguage;
/**
 *  ①Support interface custom language, see UTEModelDeviceInterface
 *  Note:Support numbers from 1 to 50
 */
@property (nonatomic,assign) BOOL     isHasMoreLanguage;
/**
 *  ①Support interface custom language, see UTEModelDeviceInterface
 *  Note:Supports all numbers
 */
@property (nonatomic,assign) BOOL     isHasMoreLanguage51;
/**
 *  ①Read the current language of the device
 */
@property (nonatomic,assign) BOOL     isHasReadLanguage;
/**
 *  ①Which languages the current device supports
 */
@property (nonatomic,assign) BOOL     isHasReadSupportLanguage;
/**
 *  ①Support multiple languages to switch directly, no additional custom strings required
 */
@property (nonatomic,assign) BOOL     isHasLanguageSwitchDirectly;
/**
 *  ①The device will alert when the device is disconnected
 */
@property (nonatomic,assign) BOOL     isHasSmartLost;
/**
 *  ①Support device to find the phone.
 *  Note:After clicking the button, the device sends the value to the app. After receiving the value, the developer handles it by itself.
 *  See delegate uteManageTouchDeviceReceiveData: .
 */
@property (nonatomic,assign) BOOL     isHasFindiPhone;
/**
 *  ①Support Control the push of specified App software information If yes, please use setUTERemindApp:, if no, please use setUTEOption: .
 *  Note:Support App Facebook,Facebook Messenger,Twitter,WhatsApp,Line,Skype,Hangouts
 */
@property (nonatomic,assign) BOOL     isHasSocialNotification;
/**
 *  ①Added more specified apps on the basis of 'isHasSocialNotification'
 */
@property (nonatomic,assign) BOOL     isHasSocialNotification2;
/**
 *  ①Can modify sleep time
 */
@property (nonatomic,assign) BOOL     isHasCustomSleep;
/**
 *  ①Pressure detection
 */
@property (nonatomic,assign) BOOL     isHasBarometric;
/**
 *  ①Heart rate automatic detection, once every 10 minutes
 *  Note:When YES, it means that the device only has automatic heart rate test (can be turned on or off), there is no manual click to test heart rate
 */
@property (nonatomic,assign) BOOL     isHas24HourHRM;
/**
 *  ①Customized device interface (hide and show an interface)
 *  See setUTEDeviceCustomUIStatus: AND readUTEDeviceCustomUIStatus:
 */
@property (nonatomic,assign) BOOL     isHasCustomDeviceUI;
/**
 *  ①Support device power saving mode
 */
@property (nonatomic,assign) BOOL     isHasPowerSave;
/**
 *  ①Support device heart rate calibration
 */
@property (nonatomic,assign) BOOL     isHasCalibrateHRM;
/**
 *  ①Support device (raise wrist, device screen lights up) calibration
 */
@property (nonatomic,assign) BOOL     isHasCalibrateRaiseHand;
/**
 *  ①Static heart rate and dynamic heart rate can be switched automatically
 */
@property (nonatomic,assign) BOOL     isHasHRMautoSwitch;
/**
 *  ①Support headset call
 */
@property (nonatomic,assign) BOOL     isHasHeadset;
/**
 *  ①Support headset heart rate
 */
@property (nonatomic,assign) BOOL     isHasHeadsetHRM;
/**
 *  ①Support device voice broadcast steps (click or touch the device, the app will receive a value, and the app (developer) will eventually process the broadcast steps)
 */
@property (nonatomic,assign) BOOL     isHasVoiceBroadcast;
/**
 *  ①Ali QR code
 */
@property (nonatomic,assign) BOOL     isHasAliQR;
/**
 *  ①Content push
 */
@property (nonatomic,assign) BOOL      isHasContentPush;
/**
 *  ①Support exercise heart rate detection
 */
@property (nonatomic,assign) BOOL      isHasSportHRM;
/**
 *  ①SOS function
 */
@property (nonatomic,assign) BOOL      isHasSOS;
/**
 *  ①App can control device shutdown
 */
@property (nonatomic,assign) BOOL      isHasShutdown;
/**
 *  ①BodyFat
 */
@property (nonatomic,assign) BOOL      isHasBodyFat;
/**
 *  ①Device online real-time dial replacement
 */
@property (nonatomic,assign) BOOL      isHasSwitchDialOnline;
/**
 *  ①Device can change background image
 */
@property (nonatomic,assign) BOOL      isHasDeviceBgChange;
/**
 *  ①Support Chinese and English switching (Chinese and English text are already built in the firmware)
 *  See setUTEInfoModel:
 */
@property (nonatomic,assign) BOOL      isHasSwitchCH_EN;
/**
 *  ①Support Body temperature test (Only for a certain device(Name:DH78))
 */
@property (nonatomic,assign) BOOL      isHasBodyTemp;
/**
 *  ①Support device temperature unit switching (Celsius, Fahrenheit)
 */
@property (nonatomic,assign) BOOL      isHasSwitchTempUnit;
/**
 *  ①ECG
 */
@property (nonatomic,assign) BOOL      isHasECG;
/**
 *  ①Blood Oxygen
 */
@property (nonatomic,assign) BOOL      isHasBloodOxygen;
/**
 *  ①Respiration Rate
 */
@property (nonatomic,assign) BOOL      isHasRespiration;
/**
 *  ①Device can modify Bluetooth name
 */
@property (nonatomic,assign) BOOL      isHasModifyBluetoothName;
/**
 *  ①Support device to check data status (what data has not been synchronized)
 *  Note:If you want to synchronize data, please invoke method syncDataCustomTime:type:
 */
@property (nonatomic,assign) BOOL      isHasDataStatus;
/**
 *  ①Support female physiological cycle reminder
 */
@property (nonatomic,assign) BOOL      isHasMenstruationReminder;
/**
 *  ①Support device shortcut buttons
 */
@property (nonatomic,assign) BOOL      isHasShortcutButton;
/**
 *  ①Support AGPS data delivery
 */
@property (nonatomic,assign) BOOL      isHasAGPS;
/**
 *  ①Device-side sleep analysis
 */
@property (nonatomic,assign) BOOL      isHasResolveSleep;
/**
 *  ①Support device heart rate maximum, minimum, average display
 *  See delegate uteManagerReceiveHRMMaxValue
 */
@property (nonatomic,assign) BOOL      isHasHRMValueMaxMin;
/**
 *  ①Support device can pause during sport
 *  See UTEDeviceSportModeStatus
 */
@property (nonatomic,assign) BOOL      isHasSportPause;
/**
 *  ①Body temperature
 */
@property (nonatomic,assign) BOOL      isHasBodyTemperature;
/**
 *  ①Body temperature Function 2 (original data collection)
 *  See method setBodyTemperatureEnableFuntion2:
 */
@property (nonatomic,assign) BOOL      isHasBodyTemperatureFunction2;
/**
 *  ①Support device Body Temperature calibration
 */
@property (nonatomic,assign) BOOL      isHasCalibrateBodyTemperature;
/**
 *  ①Read which app icons are built into the device
 *  See method readDeviceSupportAppListANCS:
 */
@property (nonatomic,assign) BOOL      isHasIconANCS;
/**
 *  ①When calling, display the contact name and number.
 *  See method sendUTEContactInfo:
 */
@property (nonatomic,assign) BOOL      isHasContact;
/**
 *  ①Device supports the control of phone music players.
 */
@property (nonatomic,assign) BOOL      isHasControlMusic;
/**
 *  ①Can control not to check. Check if the device is properly worn on the hand.
 *  See method setUTEDeviceCheckWearOpen:
 */
@property (nonatomic,assign) BOOL      isHasControl_WearCheck;
/**
 *  ①Custom data supports Bluetooth 5.0
 */
@property (nonatomic,assign) BOOL      isHasCustomDataBluetooth5;
/**
 *  ①Maximum length of Bluetooth 5.0 data packet
 *  Note:If the firmware does not add Bluetooth 5.0 related functions, it is 0.
 */
@property (nonatomic,assign) NSInteger maxBluetooth5DataLength;
/**
 *  ①Drink water reminder
 */
@property (nonatomic,assign) BOOL      isHasDrinkWaterReminder;
/**
 *  ①Support Total Valid Sport Time analysis
 */
@property (nonatomic,assign) BOOL      isHasSportValidTime;
/**
 *  ①Support goal reminders (standing, activity, calories, distance,step)
 */
@property (nonatomic,assign) BOOL      isHasActivityGoal;
/**
 *  ①Support firmware UI version upgrade
 */
@property (nonatomic,assign) BOOL      isHasFirmwareUI;
/**
 *  ①Support hand washing reminder
 */
@property (nonatomic,assign) BOOL      isHasHandWashing;
/**
 *  ①Support meeting reminder
 */
@property (nonatomic,assign) BOOL      isHasMeetingReminder;
/**
 *  ①Support Device time zone
 */
@property (nonatomic,assign) BOOL      isHasTimeZone;
/**
 *  ①Support Mood, Pressure and Fatigue
 */
@property (nonatomic,assign) BOOL      isHasMPF;
/**
 *  ①Support Weather City
 */
@property (nonatomic,assign) BOOL      isHasCustomWeatherCity;
/**
 *  ①Support blood pressure more functions
 */
@property (nonatomic,assign) BOOL      isHasBloodMoreFeatures;
/**
 *  ①Support Weather humidity and UV
 */
@property (nonatomic,assign) BOOL      isHasWeatherHumidityUV;
/**
 *  ①Support Reboot
 */
@property (nonatomic,assign) BOOL      isHasReboot;
/**
 *  ①Support Sport icon list
 */
@property (nonatomic,assign) BOOL      isHasCustomSportIconList;
/**
 *  ①Support Menu icon list
 */
@property (nonatomic,assign) BOOL      isHasCustomMenuIconList;
/**
 *  ①Support Sport Target and  sport heart rate alert
 */
@property (nonatomic,assign) BOOL      isHasSportTargetHRM;
/**
 *  ①Support Siesta Time(do not disturb)
 */
@property (nonatomic,assign) BOOL      isHasCustomSiestaTimeNoDisturb;
/**
 *  ①Support Clock Title
 *  The alarm clock has a label and can also read the device alarm clock information.
 */
@property (nonatomic,assign) BOOL      isHasClockTitle;
/**
 *  ①Support Bluetooth 3.0
 */
@property (nonatomic,assign) BOOL      isHasBluetooth3;
/**
 *  ①Support Clock Show or hidden
 */
@property (nonatomic,assign) BOOL      isHasClockShow;

/**
 *  ①Support factory mode
 */
@property (nonatomic,assign) BOOL      isHasFactoryMode;
/**
 *  ①Support factory mode, light leakage test
 */
@property (nonatomic,assign) BOOL      isHasFactoryLightLeakage;
/**
 *  ①Support factory mode, stress test
 */
@property (nonatomic,assign) BOOL      isHasFactoryPressureAutoTest;
/**
 *  ①Support factory mode, power test
 */
@property (nonatomic,assign) BOOL      isHasFactoryPowerAutoTest;
/**
 *  ①Support factory mode,device button test
 */
@property (nonatomic,assign) BOOL      isHasFactoryTestButton;
/**
 *  ①Support factory mode,device TP test
 */
@property (nonatomic,assign) BOOL      isHasFactoryTestTP;
/**
 *  ①Support factory mode,device RGB test
 */
@property (nonatomic,assign) BOOL      isHasFactoryTestRGB;
/**
 *  ①Support factory mode,device G-sensor test
 */
@property (nonatomic,assign) BOOL      isHasFactoryTestGsensor;
/**
 *  ①Support factory mode,device Gyro test
 */
@property (nonatomic,assign) BOOL      isHasFactoryTestGyro;
/**
 *  ①Support factory mode,device Geomagnetism test
 */
@property (nonatomic,assign) BOOL      isHasFactoryTestGeomagnetism;
/**
 *  ①Support factory mode,device AliIC test
 */
@property (nonatomic,assign) BOOL      isHasFactoryTestAliIC;
/**
 *  ①Support factory mode,device GPS test
 */
@property (nonatomic,assign) BOOL      isHasFactoryTestGPS;
/**
 *  ①Support factory mode,device NFC test
 */
@property (nonatomic,assign) BOOL      isHasFactoryTestNFC;
/**
 *  ①Support factory mode,device PSRAM test
 */
@property (nonatomic,assign) BOOL      isHasFactoryTestPSRAM;
/**
 *  ①Support factory mode,device mike and speaker test
 */
@property (nonatomic,assign) BOOL      isHasFactoryTestMike_Speaker;
/**
 *  ①Support factory mode,device can ModifyMac
 */
@property (nonatomic,assign) BOOL      isHasFactoryTestModifyMac;

/**
 *  ①Device tag name
 */
@property (nonatomic,copy  ) NSString  *flagName;

/**
 *  Device signal strength
 *  Note:if isScanRepeat = true, it will change in real time during scanning, and the connected device will not be assigned a value.
 */
@property (nonatomic,assign) NSInteger rssi;
/**
 *  ①The Bluetooth address of the device.
 */
@property (nonatomic,strong) NSData   *address;
/**
 *  ①Device Bluetooth address (string type) e.g. @"152a354b5e66"
 */
@property (nonatomic,copy  ) NSString   *addressStr;
/**
 *  ①Additional characteristics of the device
 *  Note:Different features support different functions.
 */
@property (nonatomic,assign) UTEDeviceFeature   featureAnother;
/**
 *  Device broadcast contains (4 keys):
 *
 *    kCBAdvDataServiceUUIDs
 *    kCBAdvDataLocalName
 *    kCBAdvDataManufacturerData
 *    kCBAdvDataIsConnectable
 *
 *  Note:If the device is connected, this value will become empty.
 */
@property (nonatomic,strong) NSDictionary        *advertisementData;

/**
 *  What broadcast services does the device have (kCBAdvDataServiceUUIDs in advertisementData).
 *  Note:If the device is connected, this value will become empty.
 */
@property (nonatomic,strong) NSMutableArray<NSString *>   *mServicesUUID;
/**
 *  After the device is connected, the UUID service owned.
 *  Note:If the device is not connected, this value is empty.
 */
@property (nonatomic,strong) NSMutableArray<NSString *>   *mServicesConnectedUUID;
/**
 *  During scanning, broadcast the Bluetooth address (The corresponding value of kCBAdvDataManufacturerData in advertisementData)
 *  Note:If the device is connected, this value will become empty.
 *  Some firmware does not support this broadcast data, so it is empty.
 *  e.g @"112233445566" is 11:22:33:44:55:66
 */
@property (nonatomic,copy  ) NSString            *advertisementAddress;

/** ①Device commissioning customID */
@property (nonatomic,assign) BOOL                ishasCustomID;
/** ①Device commissioning G-Sensor */
@property (nonatomic,assign) BOOL                ishasGsensor;
/** ①Device commissioning information */
@property (nonatomic,strong) NSMutableDictionary *debugInfoDict;
/** ①Device commissioning Auxiliary display */
@property (nonatomic,assign) BOOL                ishasPostDisplay;

@end

/*!
 *  UTEModelAlarm
 */
@interface UTEModelAlarm : NSObject
/**
 *  Alarm clock time e.g.: @"09:30" @"21:05"
 */
@property (nonatomic,copy  ) NSString      *time;
/**
 *  See UTEAlarmWeek
 */
@property (nonatomic,assign) UTEAlarmWeek  week;
/**
 *  On or off
 */
@property (nonatomic,assign) BOOL          enable;
/**
 *  See UTEAlarmNum
 */
@property (nonatomic,assign) UTEAlarmNum   num;
/**
 *  Alarm vibration times Range 0 ~ 100 . Defualt 5.
 *  The more times, the more power the device consumes.
 */
@property (nonatomic,assign) NSInteger     countVibrate;
/**
 *  Vibration Intensity. Range 1 ~ 3
 *  Required: isHasClockTitle = YES
 */
@property (nonatomic,assign) NSInteger     vibrationIntensity;
/**
 *  Title. Defualt @"Alarm".  Maximum length 30
 *  Required: isHasClockTitle = YES
 */
@property (nonatomic,copy  ) NSString      *title;
/**
 *  Valid once. If yes, UTEModelAlarm.week is invalid.
 *  Required: isHasClockTitle = YES
 */
@property (nonatomic,assign) BOOL          once;
/**
 *  Record when an alarm is modified.  Format yyyy-MM-dd-HH-mm-ss
 *  Required: isHasClockTitle = YES
 *
 *  Note: If nil, default record current time.
 */
@property (nonatomic,copy  ) NSString      *timeRecord;
/**
 *  If yes, Device will not display this clock
 *  Required: isHasClockShow = YES
 */
@property (nonatomic,assign) BOOL          hidden;

@end

/*!
 *  UTEModelRunData
 */
@interface UTEModelRunData : NSObject
/**
 *  Time e.g. @"2015-08-08-13"
 */
@property (nonatomic,copy  ) NSString     *time;
/**
 *  The total calories a day, unit kcal
 */
@property (nonatomic,assign) CGFloat      calories;
/**
 *  Total calories in an hour, unit kcal
 */
@property (nonatomic,assign) CGFloat      hourCalories;
/**
 *  Total steps in an hour
 */
@property (nonatomic,assign) NSInteger    hourSteps;
/**
 *  Total steps in a day
 */
@property (nonatomic,assign) NSInteger    totalSteps;
/**
 *  Total distance of the day, unit meter
 */
@property (nonatomic,assign) CGFloat      distances;
/**
 *  Total distance in an hour, unit meter
 */
@property (nonatomic,assign) CGFloat      hourDistances;

@end

/*!
 *  UTEModelSleepData
 */
@interface UTEModelSleepData : NSObject
/**
 *  Sleep start time e.g. @"2015-08-08-23-30"
 */
@property (nonatomic,copy  ) NSString     *startTime;
/**
 *  Sleep end time e.g. @"2015-08-09-07-30"
 */
@property (nonatomic,copy  ) NSString     *endTime;
/**
 *  See UTESleepType
 */
@property (nonatomic,assign) UTESleepType sleepType;

@end

/*!
 *  UTEModelHRMData
 */
@interface UTEModelHRMData : NSObject

/**
 *  Time e.g. @"2015-08-08-23-30"
 */
@property (nonatomic,copy  ) NSString  *heartTime;
/**
 *  Count e.g. @"86"
 */
@property (nonatomic,copy  ) NSString  *heartCount;
/**
 *  See UTEHRMType
 */
@property (nonatomic,assign) UTEHRMType heartType;

@end

/*!
 *  UTEModelBloodData
 */
@interface UTEModelBloodData : NSObject

/**
 *  Time e.g. @"2015-08-08-23-30"
 */
@property (nonatomic,copy) NSString  *bloodTime;
/**
 *  e.g. @"127"
 */
@property (nonatomic,copy) NSString  *bloodSystolic;
/**
 *  e.g. @"97"
 */
@property (nonatomic,copy) NSString  *bloodDiastolic;
/**
 *  See UTEBloodType
 */
@property (nonatomic,assign) UTEBloodType  bloodType;
/**
 *  Whether the heart rate status is normal.
 *  Default is NO.When YES, indicating irregular heart rate.
 *  
 *  Required: isHasBloodPressureCustom=YES
 */
@property (nonatomic,assign) BOOL  heartRateIrregular;
/**
 *  Pulse Rate
 *  Required: isHasBloodPressureCustom=YES
 */
@property (nonatomic,copy  ) NSString  *heartCount;


@end


/*!
 *  UTEModelBloodOxygenData
 */
@interface UTEModelBloodOxygenData : NSObject

/**
 *  Time e.g. @"2015-08-08-23-30"
 */
@property (nonatomic,copy  ) NSString  *time;
/**
 *  e.g. 98. Blood Oxygen is 98% .
 */
@property (nonatomic,assign) NSInteger  value;
/**
 *  See UTEBloodOxygenType
 */
@property (nonatomic,assign) UTEBloodOxygenType  type;

@end

/*!
 *  UTEModelRespirationData
 */
@interface UTEModelRespirationData : NSObject

/**
 *  Time e.g. @"2015-08-08-23-30"
 */
@property (nonatomic,copy  ) NSString  *time;
/**
 *  e.g. 15. Breath rate is 15 breaths per minute.
 */
@property (nonatomic,assign) NSInteger  value;
/**
 *  See UTERespirationType
 */
@property (nonatomic,assign) UTERespirationType  type;

@end

/*!
 *  UTEWeather
 */
@interface UTEModelWeather : NSObject
/**
 *  Only supports Chinese (up to 4 characters), please fill in @"00" OR nil for other languages
 *  When isHasCustomWeatherCity=YES, This property will have no effect. please use property [customCity] , 
 */
@property (nonatomic,copy  ) NSString   *city;
/**
 *  Up to 30 characters.
 *  Required isHasCustomWeatherCity=YES
 */
@property (nonatomic,copy  ) NSString   *customCity;
/**
 *  Today PM2.5
 */
@property (nonatomic,assign) NSInteger  pm25;
/**
 *  Today AQI
 */
@property (nonatomic,assign) NSInteger  aqi;
/**
 *  Current temperature  (Unit Celsius)
 */
@property (nonatomic,assign) NSInteger  temperatureCurrent;
/**
 *  Maximum temperature  (Unit Celsius)
 */
@property (nonatomic,assign) NSInteger  temperatureMax;
/**
 *  Minimum temperature  (Unit Celsius)
 */
@property (nonatomic,assign) NSInteger  temperatureMin;
/**
 *  See method getUTEWeatherType:
 */
@property (nonatomic,assign) UTEWeatherType  type;
/**
 *  Today weather humidity
 *  Required isHasWeatherHumidityUV=YES
 */
@property (nonatomic,assign) NSInteger       humidity;
/**
 *  Today weather UV . Range 1~15
 *  Required isHasWeatherHumidityUV=YES
 */
@property (nonatomic,assign) NSInteger       UV;

@end

/*!
 *  UTEModelSportWalk
 *  Including walking and running, if isHasWalkRun=YES,this model will be used.
 */
@interface UTEModelSportWalkRun : NSObject
/**
 *  Time e.g. @"2015-08-08-13"
 */
@property (nonatomic,copy  ) NSString     *time;
/**
 *  Total steps in the current hour (Including walking and running)
 */
@property (nonatomic,assign) NSInteger    stepsTotal;
/**
 *  Total steps in the current hour (walking)
 */
@property (nonatomic,assign) NSInteger    walkSteps;
/**
 *  Walking start time e.g. @"05" is 2015-08-08-13-05
 */
@property (nonatomic,copy  ) NSString     *walkTimeStart;
/**
 *  Walking end time e.g. @"59" is 2015-08-08-13-59
 */
@property (nonatomic,copy  ) NSString     *walkTimeEnd;
/**
 *  Effective walking duration, Unit minute
 */
@property (nonatomic,assign) NSInteger    walkDuration;
/**
 *  Total distance in the current hour (walking), unit meter
 */
@property (nonatomic,assign) CGFloat      walkDistances;
/**
 *  Total calories in the current hour (walking), unit kcal
 */
@property (nonatomic,assign) CGFloat      walkCalories;


/**
 *  Total steps in the current hour (running)
 */
@property (nonatomic,assign) NSInteger    runSteps;
/**
 *  Running start time e.g. @"05" is 2015-08-08-13-05
 */
@property (nonatomic,copy  ) NSString     *runTimeStart;
/**
 *  Running end time e.g. @"59" is 2015-08-08-13-59
 */
@property (nonatomic,copy  ) NSString     *runTimeEnd;
/**
 *  Effective running duration, Unit minute
 */
@property (nonatomic,assign) NSInteger    runDuration;
/**
 *  Total distance in the current hour (running), unit meter.
 *  Required isHasNewCalculate=Yes, otherwise 0
 */
@property (nonatomic,assign) CGFloat      runDistances;
/**
 *  Total calories in the current hour (running), unit kcal.
 *  Required isHasNewCalculate=Yes, otherwise 0
 */
@property (nonatomic,assign) CGFloat      runCalories;



@end


/*!
 *  UTEModelSportSwim
 */
@interface UTEModelSportSwim : NSObject

/**
 *  Time e.g. @"2015-08-08"
 */
@property (nonatomic,copy  ) NSString     *time;
/**
 *  Start time e.g.  @"08-15" hour-minute
 */
@property (nonatomic,copy  ) NSString     *timeStart;
/**
 *  End time e.g. @"20-15" hour-minute
 */
@property (nonatomic,copy  ) NSString     *timeEnd;
/**
 *  Total length of swimming in a day, unit second
 */
@property (nonatomic,assign) NSInteger    duration;
/**
 *  Total number of sliding arms in a day
 */
@property (nonatomic,assign) NSInteger    count;
/**
 *  The total calories a day, unit kcal
 */
@property (nonatomic,assign) CGFloat      calories;

@end

/*!
 *  UTEModelSportSkip
 */
@interface UTEModelSportSkip : NSObject

/**
 *  Time e.g. @"2015-08-08"
 */
@property (nonatomic,copy  ) NSString     *time;
/**
 *  Start time e.g.  @"08-15" hour-minute
 */
@property (nonatomic,copy  ) NSString     *timeStart;
/**
 *  End time e.g. @"20-15" hour-minute
 */
@property (nonatomic,copy  ) NSString     *timeEnd;
/**
 *  Total length of rope skipping in a day, unit second
 */
@property (nonatomic,assign) NSInteger    duration;
/**
 *  Total number of rope skipping in a day
 */
@property (nonatomic,assign) NSInteger    count;
/**
 *  The total calories a day, unit kcal
 */
@property (nonatomic,assign) CGFloat      calories;

@end


/*!
 *  UTEModelDeviceBicycle
 */
@interface UTEModelDeviceBicycle : NSObject
/**
 *  Time e.g. @"2015-08-08"
 */
@property (nonatomic,copy  ) NSString      *time;
/**
 *  Start time e.g.  @"08-15" hour-minute
 */
@property (nonatomic,copy  ) NSString      *timeStart;
/**
 *  End time e.g. @"20-15" hour-minute
 */
@property (nonatomic,copy  ) NSString      *timeEnd;
/**
 *  Effective running duration, Unit second
 */
@property (nonatomic,assign) NSInteger     durationValid;

/**
 *  Unit kcal
 */
@property (nonatomic,assign) CGFloat       calories;

@end

/*!
 *  UTEModelDeviceTableTennis
 */
@interface UTEModelDeviceTableTennis : NSObject
/**
 *  Time e.g. @"2015-08-08"
 */
@property (nonatomic,copy  ) NSString      *time;
/**
 *  Start time e.g.  @"08-15" hour-minute
 */
@property (nonatomic,copy  ) NSString      *timeStart;
/**
 *  End time e.g. @"20-15" hour-minute
 */
@property (nonatomic,copy  ) NSString      *timeEnd;
/**
 *  Effective duration, unit second
 */
@property (nonatomic,assign) NSInteger     durationValid;

/**
 *  Unit kcal
 */
@property (nonatomic,assign) CGFloat       calories;

@end

/*!
 *  UTEModelDeviceTennis
 */
@interface UTEModelDeviceTennis : NSObject
/**
 *  Time e.g. @"2015-08-08"
 */
@property (nonatomic,copy  ) NSString      *time;
/**
 *  Start time e.g.  @"08-15" hour-minute
 */
@property (nonatomic,copy  ) NSString      *timeStart;
/**
 *  End time e.g. @"20-15" hour-minute
 */
@property (nonatomic,copy  ) NSString      *timeEnd;
/**
 *  Effective duration, unit second
 */
@property (nonatomic,assign) NSInteger     durationValid;

/**
 *  Unit kcal
 */
@property (nonatomic,assign) CGFloat       calories;

@end

/*!
 *  UTEModelDeviceTennis
 */
@interface UTEModelDeviceBadminton : NSObject
/**
 *  Time e.g. @"2015-08-08"
 */
@property (nonatomic,copy  ) NSString      *time;
/**
 *  Start time e.g.  @"08-15" hour-minute
 */
@property (nonatomic,copy  ) NSString      *timeStart;
/**
 *  End time e.g. @"20-15" hour-minute
 */
@property (nonatomic,copy  ) NSString      *timeEnd;
/**
 *  Effective duration, unit second
 */
@property (nonatomic,assign) NSInteger     durationValid;

/**
 *  Unit kcal
 */
@property (nonatomic,assign) CGFloat       calories;

@end


/*!
 *  UTEModelDeviceInfo
 */
@interface UTEModelDeviceInfo : NSObject
/**  Height UTEOptionUnitMeter  unit: cm   range <67,240>
 *
 *   Height UTEOptionUnitInch   range < 2.00 , 7.11 > 2‘00“ ~ 7’11“  ,   means xxx feet xxx inches.
 *   The value of 2 decimal places can only be 00,  01,  02,  03 , 04 , 05 , 06 , 07 , 08 , 09 ,10 , 11.
 *
 *   For example   5.10  ， 5.11   ,   6.00  ， 6.01   ， no 5.12   (because 1 foot equals 12 inches),  5.12  should be written as 6.00
 *   e.g. 5.10  is 5 feet 10 inches.
 *
 *   If not modified, no assignment is required. However, the device that is successfully connected for the first time must be assigned a value, and SDK will save this as a temporary variable. If it is not set, the distance and calories may all be 0.
 */
@property (nonatomic,assign) CGFloat      heigh;
/**
 *   Weight UTEOptionUnitMeter unit:kg range <10,300>
 *   Weight UTEOptionUnitInch  unit:lb range <22,661>
 *
 *   The device that is successfully connected for the first time must be assigned a value, and SDK will save this as a temporary variable. If it is not set, the distance and calories may all be 0.
 *   If the device supports bodyfat, the weight in kg units can be accurate to 2 decimal places, and the weight in lb units can be accurate to 1 decimal place.
 */
@property (nonatomic,assign) CGFloat       weight;

/**  Unit: cm   range <67,240>
 *   Only one of heigh and heighCM can be set. If they are set at the same time, heighCM will be used first.
 */
@property (nonatomic,assign) CGFloat       heighCM;
/**  Unit:kg   range <10,300>
 *   Only one of weight and weightKG can be set. If they are set at the same time, weightKG will be used first.
 */
@property (nonatomic,assign) CGFloat       weightKG;
/**
 *  Bright screen time (unit second), range<5,60>
 */
@property (nonatomic,assign) NSInteger     lightTime;
/**
 *  Goal of the day (Range is <100,100000>)
 */
@property (nonatomic,assign) NSInteger     sportTarget;
/**
 *  See isHasHandLight
 *   1 is open, -1 is close, 0 is default
 */
@property (nonatomic,assign) NSInteger     handlight;
/**
 *  See isHasMaxHeartAlert
 *  Range<100,200>, close is -1
 */
@property (nonatomic,assign) NSInteger     maxHeart;
/**
 *  See isHasMinHeartAlert
 *  Range<40,100>, close is -1
 */
@property (nonatomic,assign) NSInteger     minHeart;
/**
 *  See isHasSleepAux
 */
@property (nonatomic,assign) UTEDeviceSleepAuxType    sleepAux;
/**
 *  Age range <3 ,100>
 */
@property (nonatomic,assign) NSInteger    age;
/**
 *  See UTEDeviceInfoSex
 */
@property (nonatomic,assign) UTEDeviceInfoSex    sex;
/**
 *  See isHasSmartLost
 */
@property (nonatomic,assign) BOOL    isSmartLost;

/**
 *  See isHasSwitchCH_EN
 */
@property (nonatomic,assign) BOOL     languageIsChinese;

/**
 *  See isHasSwitchTempUnit
 */
@property (nonatomic,assign) BOOL     isFahrenheit;

@end

/*!
 *  UTEModelDeviceGPS
 *
 *  Symbol ① Indicates that the value is only available when GPS running.
 *  Symbol ② Indicates that the value is only available when synchronizing data.
 */
@interface UTEModelDeviceGPS : NSObject
/**
 *  ①② e.g. @"23.3333" wgs84 coordinate
 */
@property (nonatomic,copy) NSString     *latitude;
/**
 *  ①② e.g. @"23.3333" wgs84 coordinate
 */
@property (nonatomic,copy) NSString     *longitude;
/**
 *  ①② e.g. @"30.5" (km/h)
 */
@property (nonatomic,copy) NSString     *speed;
/**
 *  ①② e.g. @"88.8" (meter)
 */
@property (nonatomic,copy) NSString     *altitude;
/**
 *  ①② e.g. @"4" , range 0 ~ 4.  0 is the worst, 4 is the best.
 *   If it is negative, the data is invalid this time.
 */
@property (nonatomic,copy) NSString     *signal;

/**
 *  ② Start time e.g. @"2008-08-25-16-21"
 */
@property (nonatomic,copy) NSString     *startTime;
/**
 *  ② End time e.g. @"2008-08-25-17-30"
 */
@property (nonatomic,copy) NSString     *endTime;
/**
 *  ② see UTEDeviceSportMode
 */
@property (nonatomic,assign) UTEDeviceSportMode   sportModel;
/**
 *  ② The interval of each GPS data.
 *   Unit: seconds
 */
@property (nonatomic,assign) NSInteger   gpsInterval;
/**
 *  ② The order of the data, starting from 1.
 */
@property (nonatomic,assign) NSInteger   dataIndex;



@end

/*!
 *  UTEModelDeviceSitRemind
 */
@interface UTEModelDeviceSitRemind : NSObject

/**
 *   Open or close
 */
@property (nonatomic,assign) BOOL       enable;
/**
 *   e.g. @"08:21"
 */
@property (nonatomic,copy) NSString     *startTime;
/**
 *   e.g. @"17:30"
 */
@property (nonatomic,copy) NSString     *endTime;
/**
 *   How long to sit down
 *   Unit minute, range <30,254>
 */
@property (nonatomic,assign) NSInteger   duration;
/**
 *   Do not disturb during Siesta  (fixed time 12:00 to 14:00 pm)
 */
@property (nonatomic,assign) BOOL        enableSiesta;
/**
 *   e.g. @"13:21"
 *
 *   Required:  isHasCustomSiestaTimeNoDisturb = YES
 */
@property (nonatomic,copy) NSString     *siestaStartTime;
/**
 *   e.g. @"14:30"
 *   Required:  isHasCustomSiestaTimeNoDisturb = YES
 */
@property (nonatomic,copy) NSString     *siestaEndTime;

@end

/*!
 *  UTEModelDeviceInterface
 *  NSString maximum length is 24.
 *
 *  There are three ranges:
 *  1. The default is  num1 to 20.
 *  2. isHasMoreLanguage = YES, num1 to 50.
 *  3. isHasMoreLanguage51 = YES, All.
 */
@interface UTEModelDeviceInterface : NSObject

/**
 *  See UTEDeviceLanguage
 */
@property (nonatomic,assign) UTEDeviceLanguage     language;

/**
 *  e.g. @"Step"
 */
@property (nonatomic,copy) NSString     *num1Step;
/**
 *  e.g. @"km"
 */
@property (nonatomic,copy) NSString     *num2KM;
/**
 *  e.g. @"kcal"
 */
@property (nonatomic,copy) NSString     *num3KCAL;
/**
 *  e.g. @"Reading"
 */
@property (nonatomic,copy) NSString     *num4Reading;
/**
 *  e.g. @"Training"
 */
@property (nonatomic,copy) NSString     *num5Training;
/**
 *  e.g. @"Skipping"
 */
@property (nonatomic,copy) NSString     *num6Skipping;
/**
 *  e.g. @"Swimming"
 */
@property (nonatomic,copy) NSString     *num7Swiming;
/**
 *  e.g. @"Message"
 */
@property (nonatomic,copy) NSString     *num8Message;
/**
 *  e.g. @"Function"
 */
@property (nonatomic,copy) NSString     *num9Function;
/**
 *  e.g. @"Dial"
 */
@property (nonatomic,copy) NSString     *num10Dial;
/**
 *  e.g. @"Information"
 */
@property (nonatomic,copy) NSString     *num11Information;
/**
 *  e.g. @"Restore"
 */
@property (nonatomic,copy) NSString     *num12Restore;
/**
 *  e.g. @"PowerOff"
 */
@property (nonatomic,copy) NSString     *num13Poweroff;
/**
 *  e.g. @"Refused"
 */
@property (nonatomic,copy) NSString     *num14Refused;
/**
 *  e.g. @"Version"
 */
@property (nonatomic,copy) NSString     *num15Version;
/**
 *  e.g. @"Find iPhone"
 */
@property (nonatomic,copy) NSString     *num16FindiPhone;
/**
 *  e.g. @"Bicycle"
 */
@property (nonatomic,copy) NSString     *num17Bicycle;
/**
 *  e.g. @"Table Tennis"
 */
@property (nonatomic,copy) NSString     *num18TableTennis;
/**
 *  e.g. @"Badminton"
 */
@property (nonatomic,copy) NSString     *num19Badminton;
/**
 *  e.g. @"Tennis"
 */
@property (nonatomic,copy) NSString     *num20Tennis;


//Required isHasMoreLanguage=YES to support the following:
/**
 *  e.g. @"Status"
 */
@property (nonatomic,copy) NSString     *num21Status;
/**
 *  e.g. @"HRM"
 */
@property (nonatomic,copy) NSString     *num22HeartTitle;
/**
 *  e.g. @"Bp"
 */
@property (nonatomic,copy) NSString     *num23BloodTitle;
/**
 *  e.g. @"Weather"
 */
@property (nonatomic,copy) NSString     *num24Weather;
/**
 *  e.g. @"Running"
 */
@property (nonatomic,copy) NSString     *num25Running;
/**
 *  e.g. @"Watch"
 */
@property (nonatomic,copy) NSString     *num26Watch;
/**
 *  e.g. @"Bright"
 */
@property (nonatomic,copy) NSString     *num27Bright;
/**
 *  e.g. @"More"
 */
@property (nonatomic,copy) NSString     *num28FunctionMore;
/**
 *  e.g. @"Find"
 */
@property (nonatomic,copy) NSString     *num29FindTitle;
/**
 *  e.g. @"Finish"
 */
@property (nonatomic,copy) NSString     *num30FinishSport;
/**
 *  e.g. @"Yes"
 */
@property (nonatomic,copy) NSString     *num31Yes;
/**
 *  e.g. @"No"
 */
@property (nonatomic,copy) NSString     *num32No;
/**
 *  e.g. @"Blood Oxygen"
 */
@property (nonatomic,copy) NSString     *num33BloodOxygen;
/**
 *  e.g. @"Mountaineering"
 */
@property (nonatomic,copy) NSString     *num34Mountaineering;
/**
 *  e.g. @"Tramp"
 */
@property (nonatomic,copy) NSString     *num35Tramp;
/**
 *  e.g. @"Basketball"
 */
@property (nonatomic,copy) NSString     *num36Basketball;
/**
 *  e.g. @"Soccer"
 */
@property (nonatomic,copy) NSString     *num37Soccer_USA;
/**
 *  e.g. @"Baseball"
 */
@property (nonatomic,copy) NSString     *num38Baseball;
/**
 *  e.g. @"Volleyball"
 */
@property (nonatomic,copy) NSString     *num39Volleyball;
/**
 *  e.g. @"Cricket"
 */
@property (nonatomic,copy) NSString     *num40Cricket;
/**
 *  e.g. @"Football"
 */
@property (nonatomic,copy) NSString     *num41Football_USA;
/**
 *  e.g. @"Hockey"
 */
@property (nonatomic,copy) NSString     *num42Hockey;
/**
 *  e.g. @"Dance"
 */
@property (nonatomic,copy) NSString     *num43Dance;
/**
 *  e.g. @"Spinning Cycling"
 */
@property (nonatomic,copy) NSString     *num44SpinningCycling;
/**
 *  e.g. @"Yoga"
 */
@property (nonatomic,copy) NSString     *num45Yoga;
/**
 *  e.g. @"Sit Ups"
 */
@property (nonatomic,copy) NSString     *num46Sit_Ups;
/**
 *  e.g. @"Treadmill"
 */
@property (nonatomic,copy) NSString     *num47Treadmill;
/**
 *  e.g. @"Gymnastics"
 */
@property (nonatomic,copy) NSString     *num48Gymnastics;
/**
 *  e.g. @"Boating"
 */
@property (nonatomic,copy) NSString     *num49Boating;
/**
 *  e.g. @"Jumping Jacks"
 */
@property (nonatomic,copy) NSString     *num50Jumping_Jacks;


//Required isHasMoreLanguage51=YES to support the following:
/**
 *  e.g. @"Sun"
 */
@property (nonatomic,copy) NSString     *num51Sun;
/**
 *  e.g. @"Mon"
 */
@property (nonatomic,copy) NSString     *num52Mon;
/**
 *  e.g. @"Tue"
 */
@property (nonatomic,copy) NSString     *num53Tue;
/**
 *  e.g. @"Wed"
 */
@property (nonatomic,copy) NSString     *num54Wed;
/**
 *  e.g. @"Thu"
 */
@property (nonatomic,copy) NSString     *num55Thu;
/**
 *  e.g. @"Fri"
 */
@property (nonatomic,copy) NSString     *num56Fri;
/**
 *  e.g. @"Sat"
 */
@property (nonatomic,copy) NSString     *num57Sat;
/**
 *  e.g. @"Setting"
 */
@property (nonatomic,copy) NSString     *num58Setting;
/**
 *  e.g. @"mi"
 */
@property (nonatomic,copy) NSString     *num59MI;
/**
 *  e.g. @"Outdoor Running"
 */
@property (nonatomic,copy) NSString     *num60OutdoorRunning;
/**
 *  e.g. @"Walking"
 */
@property (nonatomic,copy) NSString     *num61Walk;
/**
 *  e.g. @"Indoor Running"
 */
@property (nonatomic,copy) NSString     *num62IndoorRunning;
/**
 *  e.g. @"Pause"
 */
@property (nonatomic,copy) NSString     *num63Pause;
/**
 *  e.g. @"Shutdown"
 */
@property (nonatomic,copy) NSString     *num64Shutdown;
/**
 *  e.g. @"Reset"
 */
@property (nonatomic,copy) NSString     *num65Reset;
/**
 *  e.g. @"Device Name"
 */
@property (nonatomic,copy) NSString     *num66DeviceName;
/**
 *  e.g. @"MAC"
 */
@property (nonatomic,copy) NSString     *num67MAC;
/**
 *  e.g. @"App Download"
 */
@property (nonatomic,copy) NSString     *num68AppDownload;
/**
 *  e.g. @"Clear All"
 */
@property (nonatomic,copy) NSString     *num69ClearAll;
/**
 *  e.g. @"Empty"
 */
@property (nonatomic,copy) NSString     *num70Empty;
/**
 *  e.g. @"Times"
 */
@property (nonatomic,copy) NSString     *num71Times;
/**
 *  e.g. @"Ditance"
 */
@property (nonatomic,copy) NSString     *num72Ditance;
/**
 *  e.g. @"Calories"
 */
@property (nonatomic,copy) NSString     *num73Calories;
/**
 *  e.g. @"Testing"
 */
@property (nonatomic,copy) NSString     *num74Testing;
/**
 *  e.g. @"Retry"
 */
@property (nonatomic,copy) NSString     *num75Retry;
/**
 *  e.g. @"To App"
 */
@property (nonatomic,copy) NSString     *num76ToApp;
/**
 *  e.g. @"Sleep"
 */
@property (nonatomic,copy) NSString     *num77Sleep;
/**
 *  e.g. @"Too little data to save"
 */
@property (nonatomic,copy) NSString     *num78TipSaveData;
/**
 *  e.g. @"Music"
 */
@property (nonatomic,copy) NSString     *num79Music;
/**
 *  e.g. @"Body Temp"
 */
@property (nonatomic,copy) NSString     *num80BodyTemp;
/**
 *  e.g. @"Temperature unit"
 */
@property (nonatomic,copy) NSString     *num81UnitTemp;
/**
 *  e.g. @"Female health"
 */
@property (nonatomic,copy) NSString     *num82FemaleH;
/**
 *  e.g. @"Security period"
 */
@property (nonatomic,copy) NSString     *num83FemaleS;
/**
 *  e.g. @"Ovulation"
 */
@property (nonatomic,copy) NSString     *num84FemaleO;
/**
 *  e.g. @"Menstrual period"
 */
@property (nonatomic,copy) NSString     *num85FemaleM;
/**
 *  e.g. @"Breath"
 */
@property (nonatomic,copy) NSString     *num86Breath;
/**
 *  e.g. @"Breathing in"
 */
@property (nonatomic,copy) NSString     *num87BreathIn;
/**
 *  e.g. @"Breathing out"
 */
@property (nonatomic,copy) NSString     *num88BreathOut;

/** e.g. @"Movement distance" */
@property (nonatomic,copy) NSString     *num89SportDistance;
/** e.g. @"Timer" */
@property (nonatomic,copy) NSString     *num90Timer;
/** e.g. @"Time ends" */
@property (nonatomic,copy) NSString     *num91TimerEnd;
/** e.g. @"flashlight" */
@property (nonatomic,copy) NSString     *num92Flashlight;
/** e.g. @"reach the goal!" */
@property (nonatomic,copy) NSString     *num93GoalAchieved;
/** e.g. @"time setting" */
@property (nonatomic,copy) NSString     *num94TimeSetting;
/** e.g. @"Date setting" */
@property (nonatomic,copy) NSString     *num95DateSetting;
/** e.g. @"Phone" */
@property (nonatomic,copy) NSString     *num96iPhone;
/** e.g. @"Dialpad" */
@property (nonatomic,copy) NSString     *num97Dial;
/** e.g. @"Call records" */
@property (nonatomic,copy) NSString     *num98CallRecords;
/** e.g. @"Switch" */
@property (nonatomic,copy) NSString     *num99Switch;
/** e.g. @"No record" */
@property (nonatomic,copy) NSString     *num100NoRecord;
/** e.g. @"Data" */
@property (nonatomic,copy) NSString     *num101Data;
/** e.g. @"Sleep data" */
@property (nonatomic,copy) NSString     *num102SleepData;
/** e.g. @"No records" */
@property (nonatomic,copy) NSString     *num103NoRecords;
/** e.g. @"Close to the wrist detection" */
@property (nonatomic,copy) NSString     *num104CloseWristTest;
/** e.g. @"Alarm clock" */
@property (nonatomic,copy) NSString     *num105AlarmClock;
/** e.g. @"System" */
@property (nonatomic,copy) NSString     *num106System;
/** e.g. @"Brightness" */
@property (nonatomic,copy) NSString     *num107Brightness;
/** e.g. @"Shock" */
@property (nonatomic,copy) NSString     *num108VibrationIntensity;
/** e.g. @"Low power" */
@property (nonatomic,copy) NSString     *num109LowPower;
/** e.g. @"Recharge" */
@property (nonatomic,copy) NSString     *num110Recharge;
/** e.g. @"Full" */
@property (nonatomic,copy) NSString     *num111FullBattery;
/** e.g. @"Not connected" */
@property (nonatomic,copy) NSString     *num112Disconnected;
/** e.g. @"Unknown" */
@property (nonatomic,copy) NSString     *num113Unknown;
/** e.g. @"Sunny" */
@property (nonatomic,copy) NSString     *num114Sunny;
/** e.g. @"Cloudy" */
@property (nonatomic,copy) NSString     *num115Cloudy;
/** e.g. @"Overcast" */
@property (nonatomic,copy) NSString     *num116Overcast;
/** e.g. @"Shower" */
@property (nonatomic,copy) NSString     *num117Shower;
/** e.g. @"T-storm" */
@property (nonatomic,copy) NSString     *num118Thundershower;
/** e.g. @"Rain plus snow" */
@property (nonatomic,copy) NSString     *num119RainSnow;
/** e.g. @"Light rain" */
@property (nonatomic,copy) NSString     *num120LightRain;
/** e.g. @"Heavy rain" */
@property (nonatomic,copy) NSString     *num121HeavyRain;
/** e.g. @"Snow" */
@property (nonatomic,copy) NSString     *num122Snow;
/** e.g. @"Sandstorm" */
@property (nonatomic,copy) NSString     *num123Sandstorm;
/** e.g. @"Fog" */
@property (nonatomic,copy) NSString     *num124Fog;
/** e.g. @"Wind" */
@property (nonatomic,copy) NSString     *num125Wind;
/** e.g. @"Are you sure to exit Do Not Disturb mode?" */
@property (nonatomic,copy) NSString     *num126OpenDoNotDisturbMode;
/** e.g. @"Are you sure to enter Do Not Disturb mode?" */
@property (nonatomic,copy) NSString     *num127CloseDoNotDisturbMode;
/** e.g. @"Start measuring" */
@property (nonatomic,copy) NSString     *num128MeasuringStart;
/** e.g. @"Measuring..." */
@property (nonatomic,copy) NSString     *num129Measuring;
/** e.g. @"Finish" */
@property (nonatomic,copy) NSString     *num130MeasurementCompleted;
/** e.g. @"Measurement failed" */
@property (nonatomic,copy) NSString     *num131MeasurementFailed;
/** e.g. @"Measure again" */
@property (nonatomic,copy) NSString     *num132MeasureAgain;
/** e.g. @"Measuring temperature exit?" */
@property (nonatomic,copy) NSString     *num133MeasurementClose;
/** e.g. @"Language" */
@property (nonatomic,copy) NSString     *num134Language;
/** e.g. @"Sit for too long, move around!" */
@property (nonatomic,copy) NSString     *num135SedentaryReminderTips;
/** e.g. @"Turn wrist wake" */
@property (nonatomic,copy) NSString     *num136TurnWristWake;
/** e.g. @"Volume adjustment" */
@property (nonatomic,copy) NSString     *num137Volume;



@end

/*!
 *  UTEModelDeviceRemindApp
 */
@interface UTEModelDeviceRemindApp : NSObject

//Apps not listed below
@property (nonatomic,assign) UTEDeviceRemindEnableType     Other;

@property (nonatomic,assign) UTEDeviceRemindEnableType     Phone;
@property (nonatomic,assign) UTEDeviceRemindEnableType     SMS;
@property (nonatomic,assign) UTEDeviceRemindEnableType     QQ;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Wechat;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Facebook;
@property (nonatomic,assign) UTEDeviceRemindEnableType     FacebookMessenger;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Twitter;
@property (nonatomic,assign) UTEDeviceRemindEnableType     WhatsApp;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Line;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Skype;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Hangouts;

//When isHasSocialNotification=YES, supports all the above apps.

@property (nonatomic,assign) UTEDeviceRemindEnableType     LinkedIn;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Instagram;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Viber;
@property (nonatomic,assign) UTEDeviceRemindEnableType     KakaoTalk;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Vkontakte;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Snapchat;
@property (nonatomic,assign) UTEDeviceRemindEnableType     GooglePlus;//Google+
@property (nonatomic,assign) UTEDeviceRemindEnableType     Gmail;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Flickr;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Tumblr;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Pinterest;
@property (nonatomic,assign) UTEDeviceRemindEnableType     YouTube;

//When isHasSocialNotification2=YES, supports all the above apps.

@property (nonatomic,assign) UTEDeviceRemindEnableType     Telegram;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Truecaller;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Paytm;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Zalo;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Imo;
@property (nonatomic,assign) UTEDeviceRemindEnableType     MicrosoftTeams;
@property (nonatomic,assign) UTEDeviceRemindEnableType     MicrosoftOutlook;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Swiggy;

@property (nonatomic,assign) UTEDeviceRemindEnableType     Zomato;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Gpay;
@property (nonatomic,assign) UTEDeviceRemindEnableType     PhonePe;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Hotstar;
@property (nonatomic,assign) UTEDeviceRemindEnableType     PrimeVideo;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Flipkart;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Amazon;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Myntra;

@property (nonatomic,assign) UTEDeviceRemindEnableType     NoiseFit;
@property (nonatomic,assign) UTEDeviceRemindEnableType     DailyHunt;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Inshorts;
@property (nonatomic,assign) UTEDeviceRemindEnableType     BookMyShow;
@property (nonatomic,assign) UTEDeviceRemindEnableType     AppleCalendar;
@property (nonatomic,assign) UTEDeviceRemindEnableType     JioTV;
@property (nonatomic,assign) UTEDeviceRemindEnableType     MakeMyTrip;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Netflix;

@property (nonatomic,assign) UTEDeviceRemindEnableType     Ola;
@property (nonatomic,assign) UTEDeviceRemindEnableType     FastrackReflex;
@property (nonatomic,assign) UTEDeviceRemindEnableType     Uber;
@property (nonatomic,assign) UTEDeviceRemindEnableType     YTMusic;

//When isHasIconANCS=YES, supports all the above apps.

@end


/*!
 *  UTEModelDeviceSleepAdjust
 *  Note:Do not overlap "Night Sleep Time Range" with "Nap Sleep Time Range".
 */
@interface UTEModelDeviceSleepAdjust : NSObject

/**
 *  Monitoring sleep start time. e.g. @"23:05"
 *  Night Sleep Time Range:  18:00 ~ 23:59
 *  Nap Sleep Time Range:    12:00 ~ 17:59
 */
@property (nonatomic,copy  ) NSString                   *timeDurationStart;
/**
 *  Monitoring sleep end time. e.g. @"08:20"
 *  Night Sleep Time Range:  00:00 ~ 11:59
 *  Nap Sleep Time Range:    12:00 ~ 17:59
 */
@property (nonatomic,copy  ) NSString                   *timeDurationEnd;
/**
 *  Whether to modify the sleep monitoring time
 */
@property (nonatomic,assign) BOOL                       timeDurationSet;
/**
 *  See UTEDeviceSleepStatus
 */
@property (nonatomic,assign) UTEDeviceSleepStatus       timeDurationStatus;
/**
 *  See UTEDeviceSleepTimeType
 */
@property (nonatomic,assign) UTEDeviceSleepTimeType     timeDurationType;




/**
 *  Modify the night sleep (original) start time e.g. @ "23:05" .
 *  Valid for UTEDeviceSleepTimeTypeNight.
 */
@property (nonatomic,copy  ) NSString                   *timeAdjustStartTimeDefault;
/**
 *  Modify the night sleep (previously modified) start time e.g. @ "23:05" .
 *  Valid for UTEDeviceSleepTimeTypeNight.
 */
@property (nonatomic,copy  ) NSString                   *timeAdjustStartTimeChange;
/**
 *  Whether to correct the start time of sleep
 */
@property (nonatomic,assign) BOOL                       timeAdjustStartTimeSet;

/**
 *  Modify the night sleep (original) start end e.g. @ "08:05" .
 *  Valid for UTEDeviceSleepTimeTypeNight.
 */
@property (nonatomic,copy  ) NSString                   *timeAdjustEndTimeDefault;
/**
 *  Modify the night sleep (previously modified) end time e.g. @ "08:05" .
 *  Valid for UTEDeviceSleepTimeTypeNight.
 */
@property (nonatomic,copy  ) NSString                   *timeAdjustEndTimeChange;
/**
 *  Whether to correct the end time of sleep
 */
@property (nonatomic,assign) BOOL                       timeAdjustEndTimeSet;


@end

/*!
 *  UTEModelDeviceBarometric
 */
@interface UTEModelDeviceBarometric : NSObject

/**
    Time e.g. @"2008-08-08-15-20"
*/
@property (nonatomic,copy  ) NSString           *time;
/**
    Unit hPa
 */
@property (nonatomic,assign) NSInteger          valueBarometric;
/**
    Unit ℃
 */
@property (nonatomic,assign) NSInteger          valueTemperature;

@end

/*!
 *  UTEModelDeviceCustomUI
 */
@interface UTEModelDeviceCustomUI : NSObject

/**
 *  See UTEDeviceCustomUIType
 */
@property (nonatomic,assign) UTEDeviceCustomUIType  ui;
/**
 *  Show or hide
 */
@property (nonatomic,assign) BOOL                   show;

@end

/*!
 *  UTEModelDeviceCustomMsg
 */
@interface UTEModelDeviceCustomMsg : NSObject

/**
 *  See UTEDeviceMsgType
 */
@property (nonatomic,assign) UTEDeviceMsgType  type;
/**
 *  Message content(The length should be less than 160, otherwise it will be truncated to 160)
 *  Format:Application name + message . e.g. @"WhatsApp: Hello ,I'm hungry!"
 */
@property (nonatomic,copy  ) NSString          *msg;

/**
 *  Message sender (not used temporarily)
 */
@property (nonatomic,copy  ) NSString          *sender;

@end

/*!
 *  UTEModelBodyFatConfig
 */
@interface UTEModelDeviceBodyFatConfig : NSObject
/**
 *  See UTEDeviceInfoSex
 */
@property (nonatomic,assign) UTEDeviceInfoSex  gender;
/**
 *  Age
 */
@property (nonatomic,assign) NSInteger          age;
/**
 *  Height Unit cm
 */
@property (nonatomic,assign) NSInteger          height;
/**
 *  Weight Unit kg
 */
@property (nonatomic,assign) CGFloat            weight;
/**
 *  Numbering is used to distinguish different people from testing, the default is 0
 */
@property (nonatomic,assign) NSUInteger         index;

@end


/*!
 *  UTEModelDeviceBodyFat
 */
@interface UTEModelDeviceBodyFat : NSObject
/**
 *  e.g. @"2018-08-08-15-20"
 */
@property (nonatomic,copy  ) NSString          *time;
/**
 *  unit kg
 */
@property (nonatomic,assign) CGFloat            fat;
/**
 *  unit kg
 */
@property (nonatomic,assign) CGFloat            water;
/**
 *  unit kg
 */
@property (nonatomic,assign) CGFloat            muscle;
/**
 *  unit kg
 */
@property (nonatomic,assign) CGFloat            protein;
/**
 *  unit kg
 */
@property (nonatomic,assign) CGFloat            boneSalt;
/**
 *  unit kcal
 */
@property (nonatomic,assign) NSInteger            BMR;
/**
 *  See UTEModelDeviceBodyFatConfig
 */
@property (nonatomic,strong) UTEModelDeviceBodyFatConfig    *config;

@end


/*!
 *  UTEModelDeviceECG
 */
@interface UTEModelDeviceECG : NSObject

/**
 *  Voltage on ordinate
 */
@property (nonatomic,assign) NSInteger          voltage;

/**
 *  Sequence mark 0~9 .
 *  Each set of data starts from 0 to 9. If it is not continuous, it means that one data has not been received, but it will not affect the final result.
 */
@property (nonatomic,assign) NSInteger          index;
/**
 *  Real-time heart rate
 *  During the test,When the device has a heart rate value, and all other parameters are null.
 */
@property (nonatomic,assign) NSInteger          heart;




/**
 *  Whether the final data result is valid.
 */
@property (nonatomic,assign) BOOL               valid;
/**
 *  Average heart rate
 */
@property (nonatomic,assign) NSInteger          heartAverage;
/**
 *  Heart rate variability
 */
@property (nonatomic,assign) NSInteger          HRV;

/**
 *  (invalid) Heart Risk Level
 */
@property (nonatomic,assign) NSInteger          CRL;
/**
 *  (invalid) Fatigue index
 */
@property (nonatomic,assign) NSInteger          fatigueIndex;
/**
 *  (invalid) Heart Strength Rating
 */
@property (nonatomic,assign) NSInteger          HSL;


/**
 *  Format:yyyy-MM-dd-HH-mm
 *  This value is only available for synchronized data.
 */
@property (nonatomic,copy  ) NSString           *time;

@end


/*!
 *  UTEModelDeviceDataStatus
 */
@interface UTEModelDeviceDataStatus : NSObject

@property (nonatomic,assign) BOOL          steps;
@property (nonatomic,assign) BOOL          sleep;
@property (nonatomic,assign) BOOL          hrm;
@property (nonatomic,assign) BOOL          hrm24;
@property (nonatomic,assign) BOOL          blood;
@property (nonatomic,assign) BOOL          skipping;
@property (nonatomic,assign) BOOL          swiming;
@property (nonatomic,assign) BOOL          bicycle;
@property (nonatomic,assign) BOOL          ball;
@property (nonatomic,assign) BOOL          sportHRM;
@property (nonatomic,assign) BOOL          gps;
@property (nonatomic,assign) BOOL          bodyfat;

@end


/*!
 *  UTEModelDeviceMenstruation
 *  Female physiological cycle
 */
@interface UTEModelDeviceMenstruation : NSObject
/**
 *  Date of the first day of the last menstrual period
 *  e.g. @"2018-08-22"
 */
@property (nonatomic,copy  ) NSString         *firstTime;
/**
 *  Duration of menstruation. Range:3 ~ 8 (day)
 */
@property (nonatomic,assign) NSInteger      duration;
/**
 *  Menstrual cycle. Range:24 ~ 40 (day)
 */
@property (nonatomic,assign) NSInteger      cycle;
/**
 *  Turn reminders on or off
 */
@property (nonatomic,assign) BOOL           openReminder;

@end


/*!
 *  UTEModelDeviceDisplayModel
 *  Symbol ① Indicates the value is obtained by calling the method readUTEDisplayInfoFormDevice:
 *  Symbol ② Indicates the value to be filled when invoke method getUTEDisplayInfoFormServer:device:success:failure
 *  Symbol ③ Indicates the value that is returned only when the server is accessed
 */
@interface UTEModelDeviceDisplayModel : NSObject
/**
    ①③ When ID = -1, it means there is no dial in the device to replace
 */
@property (nonatomic,assign) NSInteger               ID;
/**
    ①②③ See UTEDeviceScreenType
 */
@property (nonatomic,assign) UTEDeviceScreenType     screenType;
/**
    ①② Device compatibility flag
 */
@property (nonatomic,assign) NSInteger               compatible;
/**
    ①② Device Resolution: height
 */
@property (nonatomic,assign) NSUInteger               height;
/**
    ①② Device Resolution: Width
 */
@property (nonatomic,assign) NSUInteger               width;
/**
    ①② Device supports the largest dial size (unit bytes)
 */
@property (nonatomic,assign) NSUInteger               maxCapacity;
/**
    ② Please fill in the property of UTEModelDevices.versionName e.g. @"HA12"
 */
@property (nonatomic,copy  ) NSString                *versionName;
/**
    Optional
    This property is temporarily invalid.
    ② Which language show (UTEModelDeviceDisplayModel.title/UTEModelDeviceDisplayModel.des)
*/
@property (nonatomic,assign) UTEDeviceLanguage       language;
/**
    Optional
    ② Download from that Dial index. Default is 0.
 */
@property (nonatomic,assign) NSUInteger              startIndex;
/**
    Optional
    ② Download To that Dial index. Default is 29(startIndex+29).
 */
@property (nonatomic,assign) NSUInteger              endIndex;
/**
    ③ How many Dials are there on the server.
    If 'startIndex' is greater than 'amount', then it is 0.
 */
@property (nonatomic,assign) NSUInteger              amount;
/**
    ③ Which group does this dial belong to.
 
    Optional
    ② From which group (See UTEModelDeviceDisplayGroupModel.ID)
 */
@property (nonatomic,assign) NSUInteger              groupID;

/**
    ③ Preview image link
 */
@property (nonatomic,strong) NSArray                 *previewURLs;
/**
    ③ Firmware(bin file) download link
 */
@property (nonatomic,copy  ) NSString                *firmwareURL;
/**
    ③ Firmware size
 */
@property (nonatomic,assign) NSInteger               firmwareSize;
/**
    ③ See UTEDeviceDisplayMode
 */
@property (nonatomic,assign) UTEDeviceDisplayMode    displayMode;
/**
    ③ Device title
*/
@property (nonatomic,copy  ) NSString                *title;
/**
    ③ Brief description
 */
@property (nonatomic,copy  ) NSString                *des;
/**
    ③ How many times has it been downloaded
 */
@property (nonatomic,assign) NSInteger               dowloadCount;

@end


@interface UTEModelDeviceDisplayGroupModel : NSObject
/**
    Group ID
 */
@property (nonatomic,assign) NSInteger               ID;
/**
    Group name
 */
@property (nonatomic,copy  ) NSString                *name;
/**
    Group sort number
 */
@property (nonatomic,assign) NSInteger               index;
/**
    How many dials are there in this group
 */
@property (nonatomic,assign) NSInteger               count;
/**
    The latest top 3 dials in this group
 */
@property (nonatomic,strong) NSArray<UTEModelDeviceDisplayModel *>  *array;


@end


/*!
 *  UTEModelSportHRMData
 *  Symbol ① Indicates that it has value only when the data is synchronized
 *  Symbol ② Indicates that it is in motion, it has value.
 */
@interface UTEModelSportHRMData : NSObject
/**
    See UTEDeviceSportMode
 */
@property (nonatomic,assign) UTEDeviceSportMode     sportModel;
/**
    ① Start time. Format: yyyy-MM-dd-HH-mm-ss
 */
@property (nonatomic,copy  ) NSString               *timeStart;
/**
    ① End time. Format: yyyy-MM-dd-HH-mm-ss
 */
@property (nonatomic,copy  ) NSString                *timeEnd;
/**
    ①② Only works for these modes ( UTEDeviceSportModeRunning/UTEDeviceSportModeTreadmill/UTEDeviceSportModeWalking/UTEDeviceSportModeMountaineering)
 When isHasMoreSportType=YES, also supports modes(UTEDeviceSportModeIndoorWalking, UTEDeviceSportModeIndoorRunning, UTEDeviceSportModeStepping, UTEDeviceSportModeOutdoorWalking, UTEDeviceSportModeTrailRunning, UTEDeviceSportModeParkour, UTEDeviceSportModeMarathon)
 */
@property (nonatomic,assign) NSInteger               steps;
/**
    ①② Unit:km .Only works for these modes( UTEDeviceSportModeRunning/UTEDeviceSportModeTreadmill/UTEDeviceSportModeWalking/UTEDeviceSportModeMountaineering)
 When isHasMoreSportType=YES, also supports modes(UTEDeviceSportModeIndoorWalking, UTEDeviceSportModeIndoorRunning, UTEDeviceSportModeStepping, UTEDeviceSportModeOutdoorWalking, UTEDeviceSportModeTrailRunning, UTEDeviceSportModeParkour, UTEDeviceSportModeMarathon)
 */
@property (nonatomic,assign) CGFloat                 distance;
/**
    ①② Unit:kcal
 */
@property (nonatomic,assign) CGFloat                 calories;
/**
    ①② Skipping or swiping arms. Only works for these modes(UTEDeviceSportModeSwimming/UTEDeviceSportModeRopeSkipping)
 */
@property (nonatomic,assign) NSInteger               count;
/**
    ① e.g. @[@"68",@"89",@"0",@"77",@"101"...]
    If @"0" appears in the array, sometimes the heart rate cannot be detected
 */
@property (nonatomic,strong) NSArray                 *hrmArray;
/**
    ① See UTEDeviceIntervalTime, Interval between each heart rate.
 */
@property (nonatomic,assign) UTEDeviceIntervalTime   hrmInterval;
/**
    ① Maximum heart rate
 */
@property (nonatomic,assign) NSInteger               hrmMax;
/**
    ① Minimum heart rate
 */
@property (nonatomic,assign) NSInteger               hrmMin;
/**
    ① Average heart rate
 */
@property (nonatomic,assign) NSInteger               hrmAve;
/**
    ② Real-time heart rate. If it cannot be detected, it may be 0.
 */
@property (nonatomic,assign) NSInteger               hrmCurrent;
/**
    ①② Average pace,unit:second.
    How long does it take per kilometer.
    Only works for these modes ( UTEDeviceSportModeRunning/UTEDeviceSportModeTreadmill/UTEDeviceSportModeWalking/UTEDeviceSportModeMountaineering)
 */
@property (nonatomic,assign) NSInteger               speed;
/**
    Required isHasSportValidTime=YES
    ① Total valid exercise time (unit:second)
 */
@property (nonatomic,assign) NSInteger               validTime;


@end

/*!
 *  UTEModelSportHeadsetData
 */
@interface UTEModelSportHeadsetData : NSObject

/**
    See UTEDeviceSportMode
 */
@property (nonatomic,assign) UTEDeviceSportMode     sportModel;
/**
    Real-time heart rate. If it cannot be detected, it may be 0.
 */
@property (nonatomic,assign) NSInteger               hrmCurrent;
/**
    Steps
 */
@property (nonatomic,assign) NSInteger               steps;
/**
    Unit:km .Only work for( UTEDeviceSportModeRunning/UTEDeviceSportModeTreadmill/UTEDeviceSportModeWalking/UTEDeviceSportModeMountaineering)
 */
@property (nonatomic,assign) CGFloat                 distance;
/**
    Unit:kcal
 */
@property (nonatomic,assign) CGFloat                 calories;
/**
    Skipping or swiping arms. Only work for ( UTEDeviceSportModeSwimming/UTEDeviceSportModeRopeSkipping)
 */
@property (nonatomic,assign) NSInteger               count;
/**
    Average pace,unit:second. (Only works for UTEDeviceSportModeRunning)
    How long does it take per kilometer
 */
@property (nonatomic,assign) NSInteger               speed;
/**
    Exercise duration, unit:second
 */
@property (nonatomic,assign) NSInteger               duration;

@end

/*!
 *  UTEModelDialDetail
 *
 */
@interface UTEModelDialDetail  : NSObject

/**
 *  Morning or afternoon e.g. AM.  Whether it exists, or show and hide
 */
@property (nonatomic,assign) BOOL               hasAM_PM;
@property(nonatomic, strong) UIImage            *imgAM_PM;
@property(nonatomic, assign) CGRect             rectAM_PM;
/**
 *  Battery Icon
 */
@property (nonatomic,assign) BOOL               hasIconBattery;
@property(nonatomic, strong) UIImage            *imgBattery;
@property(nonatomic, assign) CGRect             rectBattery;
/**
 *  Date e.g. 08/12   is  August /12
 *  If you want to display 12/08 (August 12), you need to swap the coordinates of 0 and 1, 8 and 2.
 */
@property (nonatomic,assign) BOOL               hasDate;

/** e.g. 08/12  From left to right, first digit ,  @"0"  */
@property(nonatomic, strong) UIImage            *imgDate1;
@property(nonatomic, assign) CGRect             rectDate1;

/** e.g. 08/12  From left to right, the second digit ,  @"8"  */
@property(nonatomic, strong) UIImage            *imgDate2;
@property(nonatomic, assign) CGRect             rectDate2;

/** e.g. 08/12  From left to right, the third digit ,  @"1"  */
@property(nonatomic, strong) UIImage            *imgDate3;
@property(nonatomic, assign) CGRect             rectDate3;

/** e.g. 08/12  From left to right, the fourth digit ,  @"2"  */
@property(nonatomic, strong) UIImage            *imgDate4;
@property(nonatomic, assign) CGRect             rectDate4;

/** e.g. 08/12     @"/"  */
@property(nonatomic, strong) UIImage            *imgDateConnectionSymbol;
@property(nonatomic, assign) CGRect             rectDateConnectionSymbol;

/**
 *  Time e.g. 09:58
 */
@property (nonatomic,assign) BOOL               hasTime;

/** e.g. 09:58  From left to right, first digit  ,  @"0"  */
@property(nonatomic, strong) UIImage            *imgTime1;
@property(nonatomic, assign) CGRect             rectTime1;

/** e.g. 09:58  From left to right, the second digit  ,  @"9"  */
@property(nonatomic, strong) UIImage            *imgTime2;
@property(nonatomic, assign) CGRect             rectTime2;

/** e.g. 09:58  From left to right, the third digit ,   @"5"  */
@property(nonatomic, strong) UIImage            *imgTime3;
@property(nonatomic, assign) CGRect             rectTime3;

/** e.g. 09:58  From left to right, the fourth digit ,   @"8"  */
@property(nonatomic, strong) UIImage            *imgTime4;
@property(nonatomic, assign) CGRect             rectTime4;

/** e.g. 09:58     @":"  */
@property(nonatomic, strong) UIImage            *imgTimeConnectionSymbol;
@property(nonatomic, assign) CGRect             rectTimeConnectionSymbol;

/**
 *  Bluetooth Icon
 */
@property (nonatomic,assign) BOOL               hasIconBluetooth;
@property(nonatomic, strong) UIImage            *imgBluetooth;
@property(nonatomic, assign) CGRect             rectBluetooth;
/**
 *  Step Icon
 */
@property (nonatomic,assign) BOOL               hasIconStep;
@property(nonatomic, strong) UIImage            *imgIconStep;
@property(nonatomic, assign) CGRect             rectIconStep;
/**
 *  Step e.g. 12085  Numbers are right aligned
 */
@property (nonatomic,assign) BOOL               hasStep;

/** From left to right, first digit .   @"1" */
@property(nonatomic, strong) UIImage            *imgStep1;
@property(nonatomic, assign) CGRect             rectStep1;

/** From left to right, the second digit  .   @"2" */
@property(nonatomic, strong) UIImage            *imgStep2;
@property(nonatomic, assign) CGRect             rectStep2;

/** From left to right, the third digit .   @"0" */
@property(nonatomic, strong) UIImage            *imgStep3;
@property(nonatomic, assign) CGRect             rectStep3;

/** From left to right, the fourth digit.   @"8" */
@property(nonatomic, strong) UIImage            *imgStep4;
@property(nonatomic, assign) CGRect             rectStep4;

/** From left to right, the fifth digit .   @"5" */
@property(nonatomic, strong) UIImage            *imgStep5;
@property(nonatomic, assign) CGRect             rectStep5;

/**
 *  The progress bar of the number of steps has a total of 10 equal parts.
 */
@property (nonatomic,assign) BOOL               hasStepProgress;
@property(nonatomic, strong) UIImage            *imgStepProgress;
@property(nonatomic, assign) CGRect             rectStepProgress;
/**
 *  Week e.g. FRI
 */
@property (nonatomic,assign) BOOL               hasWeek;
@property(nonatomic, strong) UIImage            *imgWeek;
@property(nonatomic, assign) CGRect             rectWeek;

/**
 *  Heart Rate Icon
 */
@property (nonatomic,assign) BOOL               hasIconHRM;
@property(nonatomic, strong) UIImage            *imgIconHRM;
@property(nonatomic, assign) CGRect             rectIconHRM;
/**
 *  Heart Rate e.g. 120  Numbers are right aligned
 */
@property (nonatomic,assign) BOOL               hasHRM;

/** From left to right, first digit .   @"1" */
@property(nonatomic, strong) UIImage            *imgHRM1;
@property(nonatomic, assign) CGRect             rectHRM1;

/** From left to right, second digit .   @"2" */
@property(nonatomic, strong) UIImage            *imgHRM2;
@property(nonatomic, assign) CGRect             rectHRM2;

/** From left to right, third digit .   @"0" */
@property(nonatomic, strong) UIImage            *imgHRM3;
@property(nonatomic, assign) CGRect             rectHRM3;


@end


/*!
 *  UTEModelDialInfo
 */
@interface UTEModelDialInfo : NSObject
/**
    Dial index
*/
@property (nonatomic,assign) NSUInteger             index;
/**
    Dial width
*/
@property (nonatomic,assign) CGFloat                width;
/**
    Dial height
*/
@property (nonatomic,assign) CGFloat                height;
/**
    Bin file
*/
@property (nonatomic,strong) NSData                 *dataBin;
/**
    Background image
*/
@property(nonatomic, strong) UIImage                *bg;
/**
    Text Color
*/
@property(nonatomic, strong) UIColor                *textColor;
/**
    Image preview
*/
@property (nonatomic,strong) UIImage                *imagePreview;
/**
    Display Type. If 0, then it is undefined.
*/
@property (nonatomic,assign) UTEDeviceDisplayMode   type;
/**
    The corner radius of the preview image background
*/
@property (nonatomic,assign) CGFloat                radius;
/**
    What elements are there, and their coordinates, image.
*/
@property (nonatomic,strong) UTEModelDialDetail     *status;

@end

/*!
 *  UTEModelDialServer
 */
@interface UTEModelDialServer : NSObject
/**
    Server ID is unique.
*/
@property (nonatomic,assign) NSInteger               ID;
/**
    zip link e.g @"https://aaaaa.zip"
*/
@property (nonatomic,copy  ) NSString                *zipUrl;

@end


/*!
 *  UTEDeviceSportModeInfo
 */
@interface UTEDeviceSportModeInfo : NSObject
/**
    See UTEDeviceSportMode
*/
@property (nonatomic,assign) UTEDeviceSportMode       mode;
/**
    See UTEDeviceSportModeStatus
*/
@property (nonatomic,assign) UTEDeviceSportModeStatus status;
/**
    See UTEDeviceIntervalTime. How often does the device save the heart rate value.
*/
@property (nonatomic,assign) UTEDeviceIntervalTime    hrmTime;
/**
    Required isHasSportPause=YES.
    The activity duration of the device. (Unit second)
*/
@property (nonatomic,assign) NSInteger                duration;
/**
    Required isHasSportPause=YES.
    The activity calories of the device. (Unit kcal)
*/
@property (nonatomic,assign) NSInteger                calories;
/**
    Required isHasSportPause=YES.
    The activity distance of the device. (Unit meter)
*/
@property (nonatomic,assign) NSInteger                distance;
/**
    Required isHasSportPause=YES.
    The activity speed of the device. (Unit second) e.g 430 is 7 minute 10 second per kilometer (7'10")
    If the speed is greater than 6039(99'99"), it will only take the value 6039(99'99").
*/
@property (nonatomic,assign) NSInteger                speed;

@end


/*!
 *  UTEModelBodyTemperature
 */
@interface UTEModelBodyTemperature : NSObject

/**
 *  Format: yyyy-MM-dd-HH-mm-ss
 */
@property (nonatomic,copy  ) NSString      *time;
/**
 *  Body temperature
 *  Unit Celsius e.g. @"36.25"
 *  Device display @"36.2"
 */
@property (nonatomic,copy  ) NSString      *bodyTemperature;
/**
 *  The temperature of the skin, subcutaneous tissues, muscles, etc.
 *  Unit Celsius e.g. @"36.25"
 *  Device display @"36.2"
 *  Required isHasBodyTemperatureFunction2=YES.
 *  See method setBodyTemperatureEnableFuntion2:
 */
@property (nonatomic,copy  ) NSString      *shellTemperature;
/**
 *  Ambient temperature
 *  Unit Celsius e.g. @"36.25"
 *  Device display @"36.2"
 *  Required isHasBodyTemperatureFunction2=YES.
 *  See method setBodyTemperatureEnableFuntion2:
 */
@property (nonatomic,copy  ) NSString      *ambientTemperature;


@end

/*!
 *  UTEModelContactInfo
 */
@interface UTEModelContactInfo : NSObject
/**
 *  Contact Name.
 *  The maximum length is 10. If it exceeds, the first 10 characters will be automatically obtained.
 */
@property (nonatomic,copy  ) NSString      *name;
/**
 *  Phone Number.
 *  The maximum length is 15.
 *  Note: Can only have numbers and symbols +
 */
@property (nonatomic,copy  ) NSString      *number;


@end

/*!
 *  UTEModelFirmwareInfo
 */
@interface UTEModelFirmwareInfo : NSObject

/**
 *  If is YES,Indicates that the device must be upgraded.Because the device has a serious bug.
 */
@property (nonatomic,assign) BOOL          forceUpdate;
/**
 *  e.g. @"MH03BV000133"
 */
@property (nonatomic,copy  ) NSString      *version;
/**
 *  Description
 */
@property (nonatomic,copy  ) NSString      *des;

@end

/*!
 *  UTEModelCustomBloodClock
 */
@interface UTEModelCustomBloodClock  : NSObject

/**
 *  Alarm serial number. Index 0 ~ 11
 */
@property (nonatomic,assign) NSInteger     index;
/**
 *  open
 */
@property (nonatomic,assign) BOOL          open;
/**
 *  HH:mm e.g. @"09:55"
 */
@property (nonatomic,copy  ) NSString      *time;

@end

/*!
 *  UTEModelGoalTypeHistory
 *  Daily historical data
 */
@interface UTEModelGoalTypeHistory  : NSObject

/**
 *  See UTEGoalType
 */
@property (nonatomic,assign) UTEGoalType     type;
/**
 *  yyyy-MM-dd-HH-mm e.g. @"2021-09-15-08-30"
 */
@property (nonatomic,copy  ) NSString        *time;
/**
 *  When type is UTEGoalTypeStanding ,value unit is minutes.
 *  When type is UTEGoalTypeActivity ,value unit is minutes.
 *  When type is UTEGoalTypeCalorie  ,value unit is kcal.
 *  When type is UTEGoalTypeStep     ,value unit is number.
 *  When type is UTEGoalTypeDistance ,value unit is meters.
 */
@property (nonatomic,assign) NSInteger       value;


@end

@interface UTEModelWeatherOneDay  : NSObject
/**
 *  Sunrise   HH:mm  (Some regions may not support)
 */
@property (nonatomic,copy  ) NSString        *sunrise;
/**
 *  Sunset    HH:mm  (Some regions may not support)
 */
@property (nonatomic,copy  ) NSString        *sunset;
/**
 *  Moonrise   HH:mm  (Some regions may not support)
 */
@property (nonatomic,copy  ) NSString        *moonrise;
/**
 *  Moonset    HH:mm  (Some regions may not support)
 */
@property (nonatomic,copy  ) NSString        *moonset;
/**
 *  Maximum temperature. Celsius
 */
@property (nonatomic,assign) NSInteger       maxTemp;
/**
 *  Minimum temperature. Celsius
 */
@property (nonatomic,assign) NSInteger       minTemp;
/**
 *  Weather code
 *  Weather icon  reference URL https://dev.qweather.com/docs/resource/icons/
 */
@property (nonatomic,assign) NSInteger       codeWeather;
/**
 *  Weather type  @"Sunny"   (Depends on phone language)
 */
@property (nonatomic,copy  ) NSString        *text;

@end

/*!
 *  UTEModelWeatherInfo
 */
@interface UTEModelWeatherInfo  : NSObject

/**
 *  The time the server updates the weather   yyyy-MM-dd HH:mm
 */
@property (nonatomic,copy  ) NSString        *updateTime;
/**
 *  Which city  e.g. @"BeiJing"   (Depends on phone language)
 */
@property (nonatomic,copy  ) NSString        *city;
/**
 *  City ID
 */
@property (nonatomic,copy  ) NSString        *cityID;
/**
 *  That district of the city e.g. @"Chaoyang"   (Depends on phone language)
 */
@property (nonatomic,copy  ) NSString        *region;
/**
 *  Today Current Temperature. Celsius
 */
@property (nonatomic,assign) NSInteger       tempNow;
/**
 *  Today weather PM2.5 (Unit μg/m³)
 */
@property (nonatomic,assign) NSInteger       PM25;
/**
 *  Today weather humidity
 */
@property (nonatomic,assign) NSInteger       humidity;
/**
 *  Today weather UV
 */
@property (nonatomic,assign) NSInteger       UV;
/**
 *  Today city aqi (Air Quality Index)
 *  Excellent:                  0 ~ 50
 *  Generally:                  51 ~ 100
 *  Slight pollution:           101 ~ 150
 *  Moderate pollution:     151 ~ 200
 *  Heavy pollution:            201 ~ 300
 *  Serious pollution:          >= 301
 */
@property (nonatomic,assign) NSInteger       aqi;

/**
 *  The highest temperature and lowest temperature today and the next 6 days
 *  index 0: today , index 1: tomorrow ....... and so on
 */
@property(nonatomic, strong) NSArray<UTEModelWeatherOneDay *>        *array;


@end

/*!
 *  UTEModelMPFInfo
 */
@interface UTEModelMPFInfo  : NSObject
/**
 Invalid value :   -1
 Normal mood:       1
 Negative mood:     0
 Positive mood:     2
 */
@property(nonatomic, assign) NSInteger      mood;
/**
    e.g. 80.  Value -1 or 255, Invalid value.
 */
@property(nonatomic, assign) NSInteger      pressure;
/**
    e.g. 80.  Value -1 or 255, Invalid value.
 */
@property(nonatomic, assign) NSInteger      fatigue;
/**
 *  Format: yyyy-MM-dd-HH-mm
 */
@property (nonatomic,copy  ) NSString       *time;

@end

/*!
 *  UTEModelMeeting
 */
@interface UTEModelMeeting  : NSObject

/**
 *  Format: yyyy-MM-dd-HH-mm
 */
@property (nonatomic,copy  ) NSString       *time;
/**
 *  Title cannot exceed 30 characters.   e.g. @"Lunch"
 */
@property (nonatomic,copy  ) NSString       *title;

@end

/*!
 *  UTEModelRegion
 */
@interface UTEModelRegion  : NSObject

/**
 *  The length of the region name cannot exceed 30 characters.
 */
@property (nonatomic,copy  ) NSString       *region;
/**
 *  TimeZone Range:  -12 ~ 14
 */
@property(nonatomic, assign) NSInteger      timeZone;
/**
 *  timeZonePoint Range:  0  or 5
 */
@property(nonatomic, assign) NSInteger      timeZonePoint;

@end

/*!
 *  UTEModelBluetooth3_0
 */
@interface UTEModelBluetooth3_0  : NSObject
/**
 *  Name
 */
@property (nonatomic,copy  ) NSString       *name;
/**
 *  Device has Bluetooth 3.0 turned on
 */
@property(nonatomic, assign) BOOL           open;
/**
 *  Has it been paired with the phone（Not now）
 */
@property(nonatomic, assign) BOOL           pair;
/**
 *  Is it connected
 */
@property(nonatomic, assign) BOOL           connect;
/**
 *  Device address
 */
@property (nonatomic,copy  ) NSString       *address;

@end
