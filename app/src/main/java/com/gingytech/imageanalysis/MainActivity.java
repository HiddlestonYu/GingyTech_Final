package com.gingytech.imageanalysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.Thread;

import com.gingytech.gingyusb.Device;
import com.gingytech.gingyusb.GingyDev.IUsbEventObserver;
import com.gingytech.gingyusb.RegTable;
import com.gingytech.gingyusb.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity {
	private final static String TAG = "GingyIA";
	@SuppressLint("SdCardPath")

	// region global parameters
	private static FingerprintSensor   mSensor = null;
	private WindowManager.LayoutParams mWindowLayoutParams;
	private Window           		mWindow;
	private String           		mSaveRoot;
	private ImageButton      		mbtnSensingArea;
	private ToggleButton     		mtbtnLive, mtbtnFpEnroll, mtbtnFpAuth;
	private ImageView        		mfpView, mAuthResView;
	private ImageView        		mArrowUpView,mArrowDownView,mArrowLeftView,mArrowRightView;
	private ImageView        		mArrowUpView2,mArrowDownView2,mArrowLeftView2,mArrowRightView2;
	private ImageView				mRotateRight,mRotateRight2,mRotateLeft,mRotateLeft2;
	private GridLayout       		mSensingAreaLayout;
	private TextView         		mtvmsgText, mtvDebug, mtvVerInfo, mtvExtraMsg;
	private Vibrator         		mVibrator;
	private SharedPreferences		mSharedPref;
	private Menu             		mMenu;

	private static Activity		mfa              	= null;
	private Lock          		mSyncLock        	= null;

	private static Thread 		mtid_liveimage   	= null;
	private static Thread 		mtid_enroll      	= null;
	private static Thread 		mtid_auth        	= null;
	private static Thread 		mtid_makebg      	= null;

	private boolean 	mbIsSavefile            	= false;
	// ?????????,??????/???????????????????????????,live?????????
	private boolean 	mbGetImgFromStorage      	= false;
	private int     	mnReadEnrollPersonIdx    	= 1;
	private int     	mnReadEnrollFingerIdx    	= 1;
	private int     	mnReadEnrollCaptureIdx   	= 1;
	private int     	mnReadVerifyPersonIdx    	= 1;
	private int     	mnReadVerifyFingerIdx    	= 1;
	private int     	mnReadVerifyCaptureIdx   	= 1;

	private int     	mnImgWidth              	= 0;
	private int     	mnImgHeight             	= 0;
	private int     	mnImgBitCount           	= 16;
	private int     	mnIspResultW            	= 0;
	private int     	mnIspResultH            	= 0;

	private int     	mnEnrollNums            	= 0;
	private boolean 	mbFingerQualityEnroll   	= false;
	private int     	mnFingerQualityScore    	= 0;
	private boolean 	mbOverlapAllEnroll      	= false;
	private int     	mnOverlapAllScore       	= 0;
	private boolean 	mbOverlapLastEnroll     	= false;
	private int     	mnOverlapLastScore      	= 0;

	private boolean 	mbEnableDebugText       	= false;
	private boolean 	mbGetImgLoop            	= false;
	private boolean 	mbEnrollLoop            	= false;
	private boolean 	mbAuthLoop              	= false;
	private boolean 	mbIsTouched             	= false;
	private boolean 	mbIsLastTouched         	= false;
	private boolean 	mbShowVer               	= false;

	private boolean 	mbSaveEnrollImage       	= false;
	private boolean 	mbSaveAuthImage         	= false;
	// ?????????????????????
	private boolean 	mbSaveOriginalImage     	= false;
	// ?????????Isp1????????????
	private boolean 	mbSaveIsp1Image         	= false;
	// ?????????Bmp???????????????
	private boolean 	mbSaveBmpImage          	= false;

	private boolean 	mbAimingPoing           	= false;
	private boolean 	mbmakebgLoop            	= false;
	private boolean 	mbRectSensingArea       	= false;
	private float   	mfDrawlineWidth         	= 1.0f;

	private int     	mnSensingAreaSize       	= 0;
	private int     	mnSensingColorR         	= 0;
	private int     	mnSensingColorG         	= 0;
	private int     	mnSensingColorB         	= 0;

	private int     	mnIsp2DbCount           	= 0;

	private byte[]  	mBg_Normal              	= null;

	private boolean 	mbHighBrightness        	= false;

	private byte[]  	mIsp2Db                 	= null;
	private boolean 	mbDoIsp1                	= false;
	private boolean 	mbDoIsp2                	= false;

	private int     	mnTemplateNum           	= 0;
	private List<Integer> mTemplateFID          	= new ArrayList<>();
	private List<String> mIDList                	= new ArrayList<>();
	private static final boolean mbIsUsbMode    	= true;

	private static final int 	MAX_ENROLL_TEMPLATE       	= 100;

	private static final int 	ENROLL_STATUS_OK          	= 0;
	private static final int 	ENROLL_STATUS_CANCEL      	= 1;
	private static final int 	ENROLL_STATUS_ERR         	= 2 ;
	private static final int 	ENROLL_STATUS_NOFINGER    	= 3;
	private static final int 	ENROLL_STATUS_LIFTFINGER  	= 4;
	private static final int 	ENROLL_STATUS_SAMEAREA    	= 5;
	private static final int 	ENROLL_STATUS_UPDATECOUNT 	= 6;

	private static final int 	AUTH_STATUS_OK            	= 0;
	private static final int 	AUTH_STATUS_NOFINGER      	= 1;

	private static final int	DEVICE_CONNECT            	= 2;

	private static final int	STATUS_LIVE               	= 1;
	private static final int	STATUS_ENROLL             	= 2;
	private static final int	STATUS_AUTH               	= 3;
	// endregion

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "\nonCreate");
		mfa = this;
		setContentView(R.layout.activity_main);

		// ???preference xml????????????????????????sharedPreference???
		PreferenceManager.setDefaultValues(this, R.xml.pref_image, false);
		// ???????????????
		ObjectOnCreate();

		// Request Permission
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			// Api level >= 30
			if(!Environment.isExternalStorageManager()) {
				try {
					Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
					intent.addCategory("android.intent.category.DEFAULT");
					intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
					startActivityForResult(intent, 101);
				} catch (Exception e) {
					Intent intent = new Intent();
					intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
					startActivityForResult(intent, 101);

				}
			}
		} else {
			// Api level < 30
			if(ActivityCompat.checkSelfPermission(mfa, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(mfa, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102);
			}
		}

		// ?????????????????????
		mSaveRoot = Environment.getExternalStorageDirectory() + "/fingerprint_image";
		MakeDir(mSaveRoot);

		SetSensingAreaPos();
		SetSensingAreaSizeAndColor();

		// ?????????Debug Message
		if(mbEnableDebugText == true){
			mtvDebug.setVisibility(View.VISIBLE);
			mtvDebug.setText("Debug:On");
		} else {
			mtvDebug.setVisibility(View.GONE);
		}

		mSensor = new FingerprintSensor(mbIsUsbMode, this, onUsbEventCallback);

		InitSensorRegister();
		initializeFilePara();

		mSyncLock = new ReentrantLock();

		HideBar();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "\nRESUME");

		SetScreenReverse();

		// ??????isp???????????????????????????.
		if(mbDoIsp1 == false) {
			mtbtnFpEnroll.setEnabled(false);
			mtbtnFpAuth.setEnabled(false);
		} else {
			mtbtnFpEnroll.setEnabled(true);
			mtbtnFpAuth.setEnabled(true);
		}

		// ??????????????????.
		mWindowLayoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
		mWindow.setAttributes(mWindowLayoutParams);

		HideBar();
		Log.d(TAG, "");
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "\nSTOP");
		StopButtonThread();

		// ??????????????????.
		mWindowLayoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
		mWindow.setAttributes(mWindowLayoutParams);

		Log.d(TAG, "");
		super.onStop();
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.d(TAG, "\nDestroy\n");

		// Uninit gingy algorithm
		mSensor.native_algorUninit();
		mSensor = null;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if(requestCode == 102) {
			if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(mfa, "Please open storage permission!", Toast.LENGTH_SHORT).show();
			}else{
				// ?????????????????????,?????????????????????,???????????????????????????.
				initializeFilePara();
			}
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		HideBar();
	}

	private void ObjectOnCreate(){
		mWindow = getWindow();
		mWindowLayoutParams = mWindow.getAttributes();
		mfpView = (ImageView)mfa.findViewById(R.id.ivFP);
		mfpView.setOnLongClickListener(longpress);
		mAuthResView = (ImageView)mfa.findViewById(R.id.ivAuthResult);

		mtbtnLive = (ToggleButton)mfa.findViewById(R.id.tbtnLive2);
		mtbtnLive.setOnClickListener(clickButton);

		mtbtnFpEnroll = (ToggleButton)mfa.findViewById(R.id.tbtnFpEnroll2);
		mtbtnFpEnroll.setOnClickListener(clickButton);
		mtbtnFpAuth = (ToggleButton)mfa.findViewById(R.id.tbtnFpAuth2);
		mtbtnFpAuth.setOnClickListener(clickButton);

		mbtnSensingArea = (ImageButton)mfa.findViewById(R.id.ibtnTouch);
		mbtnSensingArea.setOnTouchListener(touchButton);
		mbtnSensingArea.setScaleType(ImageView.ScaleType.MATRIX);
		mArrowUpView      	= (ImageView)		this.findViewById(R.id.ivArrowUp);
		mArrowDownView    	= (ImageView)		this.findViewById(R.id.ivArrowDown);
		mArrowLeftView    	= (ImageView)		this.findViewById(R.id.ivArrowLeft);
		mArrowRightView   	= (ImageView)		this.findViewById(R.id.ivArrowRight);
		mArrowUpView2     	= (ImageView)		this.findViewById(R.id.ivArrowUp2);
		mArrowDownView2   	= (ImageView)		this.findViewById(R.id.ivArrowDown2);
		mArrowLeftView2   	= (ImageView)		this.findViewById(R.id.ivArrowLeft2);
		mArrowRightView2  	= (ImageView)		this.findViewById(R.id.ivArrowRight2);
		mSensingAreaLayout	= (GridLayout)		this.findViewById(R.id.GL_SensingArea);
		mArrowUpView.setOnClickListener(clickArrow);
		mArrowDownView.setOnClickListener(clickArrow);
		mArrowLeftView.setOnClickListener(clickArrow);
		mArrowRightView.setOnClickListener(clickArrow);
		mArrowUpView2.setOnClickListener(clickArrow);
		mArrowDownView2.setOnClickListener(clickArrow);
		mArrowLeftView2.setOnClickListener(clickArrow);
		mArrowRightView2.setOnClickListener(clickArrow);
		mRotateRight		= (ImageView)		this.findViewById(R.id.ivRotateRight);
		mRotateRight2		= (ImageView)		this.findViewById(R.id.ivRotateRight2);
		mRotateLeft			= (ImageView)		this.findViewById(R.id.ivRotateLeft);
		mRotateLeft2		= (ImageView)		this.findViewById(R.id.ivRotateLeft2);
		mRotateRight.setOnClickListener(clickRotate);
		mRotateRight2.setOnClickListener(clickRotate);
		mRotateLeft.setOnClickListener(clickRotate);
		mRotateLeft2.setOnClickListener(clickRotate);

		mtvmsgText = (TextView)mfa.findViewById(R.id.msgText);
		mtvmsgText.setText("");
		mtvDebug = (TextView)mfa.findViewById(R.id.tvDBG);
		mtvDebug.setLongClickable(true);
		mtvDebug.setOnLongClickListener(longpress);
		mtvExtraMsg = (TextView)mfa.findViewById(R.id.tvMsgExtra);
		mtvExtraMsg.setText("");
		mtvVerInfo = (TextView)mfa.findViewById(R.id.tvVerInfo);
		mtvVerInfo.setText("");

		mSharedPref = PreferenceManager.getDefaultSharedPreferences(mfa);
		mSharedPref.registerOnSharedPreferenceChangeListener(onSettingChangeLister);

		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mbGetImgFromStorage = mSharedPref.getBoolean("setting_read_storage_image", false);
		mnImgWidth = Integer.parseInt(mSharedPref.getString("setting_image_width", "200"));
		mnImgHeight = Integer.parseInt(mSharedPref.getString("setting_image_height", "200"));
		// ??????ISP??????????????????????????????,?????????Resize??????,??????release??????isp library
		mnIspResultW = mnImgWidth;
		mnIspResultH = mnImgHeight;

		mnSensingAreaSize = Integer.parseInt(mSharedPref.getString("setting_image_sensingarea_size", "12"));
		mbRectSensingArea = mSharedPref.getBoolean("setting_image_rectangle", false);

		mnSensingColorR = Integer.parseInt(mSharedPref.getString("setting_image_sensingarea_color_r", "255"))&0xFF;
		mnSensingColorG = Integer.parseInt(mSharedPref.getString("setting_image_sensingarea_color_g", "255"))&0xFF;
		mnSensingColorB = Integer.parseInt(mSharedPref.getString("setting_image_sensingarea_color_b", "255"))&0xFF;
		mbAimingPoing = mSharedPref.getBoolean("setting_image_aimingpoint", false);
		mfDrawlineWidth = Float.parseFloat(mSharedPref.getString("setting_image_drawline_width", "1.0"));

		mnEnrollNums = Integer.parseInt(mSharedPref.getString("setting_image_enrollnum", "20"));
		mbFingerQualityEnroll = mSharedPref.getBoolean("setting_image_enrollqt", true);
		mnFingerQualityScore  = Integer.parseInt(mSharedPref.getString("setting_image_fingerqualityscore","20"));
		mbOverlapAllEnroll    = mSharedPref.getBoolean("setting_enroll_overlap_all", true);
		mnOverlapAllScore     = Integer.parseInt(mSharedPref.getString("setting_enroll_overlapscore_all", "95"));
		mbOverlapLastEnroll   = mSharedPref.getBoolean("setting_enroll_overlap_last", true);
		mnOverlapLastScore    = Integer.parseInt(mSharedPref.getString("setting_enroll_overlapscore_last", "80"));

		mbEnableDebugText = mSharedPref.getBoolean("setting_image_debugtext", false);

		mbSaveEnrollImage = mSharedPref.getBoolean("setting_image_saveenrollimg", false);
		mbSaveAuthImage = mSharedPref.getBoolean("setting_image_saveauthimg", false);
		mbSaveOriginalImage = mSharedPref.getBoolean("setting_original_saving", true);
		mbSaveIsp1Image = mSharedPref.getBoolean("setting_isp1_saving", false);
		mbSaveBmpImage = mSharedPref.getBoolean("setting_bmp_saving", true);

		mnIsp2DbCount = mSharedPref.getInt("isp2_db_update_count", 0);
		Log.d(TAG, "Isp2 Database Update Count:"+ mnIsp2DbCount);

		mbDoIsp1 = mSharedPref.getBoolean("setting_image_do_isp1", false);
		mbDoIsp2 = mSharedPref.getBoolean("setting_image_do_isp2", false);

		mbHighBrightness = mSharedPref.getBoolean("setting_high_brightness", false);

		RelativeLayout rlmain = (RelativeLayout)findViewById(R.id.mainlayout);
		rlmain.setOnClickListener(clickButton);
	}

	private void initializeFilePara() {
		File dbfile = new File(mSaveRoot + "/ISBN.db");
		if(dbfile.exists()) {
			// if exist, don't initial db.
			IspInit(false);
			// if file size isn't correct, remove db and initial it.
			if(dbfile.length() != (28+(4*mnIspResultW*mnIspResultH))) {
				Log.d(TAG, "ISBN.db size: "+ dbfile.length() + " mismatch, reinitialize.");
				dbfile.delete();
				IspInit(true);
			} else {
				mIsp2Db = Utils.LoadFromFile(mSaveRoot + "/ISBN.db");
				if(mIsp2Db == null) {
					Log.d(TAG, "Load Isp2Db fail, reinitialize.");
					dbfile.delete();
					IspInit(true);
				} else {
					Log.d(TAG, "Load Isp2Db from sd.");
				}
			}
		} else {
			// if not exist, initial db.
			IspInit(true);
		}
		// Init algorithm
		if(mSensor.native_algorInit(mSaveRoot, mnIspResultW, mnIspResultH) < 0) {
			ShowMsg("Algorithm initial fail!", true);
		}

		mnTemplateNum = 0;
		mTemplateFID.clear();

		// Load Saved Template Fid
		int []fidList = new int[MAX_ENROLL_TEMPLATE];
		mnTemplateNum = mSensor.native_LoadSavedTemplatesFID(fidList);
		for(int i = 0; i < mnTemplateNum; i++) {
			mTemplateFID.add(fidList[i]);
			Log.d(TAG, "mTemplateFID["+i+"] = "+mTemplateFID.get(i));
		}

		if(mnTemplateNum == 0) {
			// Initialize mIDList
			for (int i = 0; i <= MAX_ENROLL_TEMPLATE; i++) {
				mIDList.add(null);
			}
		} else if(mnTemplateNum > 0) {
			String json_IDList = mSharedPref.getString("IDList", null);
			if (json_IDList != null) {
				Gson gson = new Gson();
				Type type = new TypeToken<List<String>>() {
				}.getType();
				mIDList = gson.fromJson(json_IDList, type);
			} else {
				// Initialize mIDList
				for (int i = 0; i <= MAX_ENROLL_TEMPLATE; i++) {
					mIDList.add(null);
				}
			}
		}

		InitialCSV();
	}

	private OnClickListener clickButton = new OnClickListener(){
		public void onClick(View view){
			switch (view.getId()) {
				case R.id.tbtnLive2:
					if(mbGetImgFromStorage) {
						// ????????????Live?????????
						mtbtnLive.setChecked(false);
						break;
					}
					boolean bIsChecked = mtbtnLive.isChecked();
					ShowMsg("",true);
					if(bIsChecked==true){
						if(mSensor.IsReady() == false) {
							ShowMsg("Device isn't found.", true);
							mtbtnLive.setChecked(false);
							openOtgSetting();
							break;
						}
						mtbtnFpEnroll.setEnabled(false);
						mtbtnFpAuth.setEnabled(false);
						mfpView.setBackgroundColor(Color.WHITE);

						LoadBgToMem();

						String dumppath = mSaveRoot+ "/live_image";
						MakeDir(dumppath);

						mbGetImgLoop = true;
						mtid_liveimage = new Thread(getliveImageThread);
						mtid_liveimage.start();
					}else{
						mbGetImgLoop = false;
						try {
							mtid_liveimage.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						mtid_liveimage = null;

						if(mbDoIsp1 == true) {
							mtbtnFpEnroll.setEnabled(true);
							mtbtnFpAuth.setEnabled(true);
						}
					}
					break;
				case R.id.tbtnFpEnroll2:
					ShowMsg("", true);
					ShowExtraMsg("", false);
					if(mtbtnFpEnroll.isChecked() == true) {
						if(!mbGetImgFromStorage) {
							if (mSensor.IsReady() == false) {
								ShowMsg("Device isn't found.", true);
								mtbtnFpEnroll.setChecked(false);
								openOtgSetting();
								break;
							}
						}

						// BG?????????,????????????enroll??????
						LoadBgToMem();
						if(mBg_Normal == null) {
							ShowMsg("Bg not exist. Make Bg first.", true);
							mtbtnFpEnroll.setChecked(false);
							break;
						}

						if(mnTemplateNum == MAX_ENROLL_TEMPLATE) {
							ShowMsg("Enrolled amount\nreach limit!", true);
							mtbtnFpEnroll.setChecked(false);
							break;
						}

						mtbtnLive.setEnabled(false);
						mtbtnFpAuth.setEnabled(false);
						mMenu.findItem(R.id.action_deltemplate).setEnabled(false);

						if(mbGetImgFromStorage) {
							MakeDir(mSaveRoot + "/Gingy enroll");
						} else {
							String dumppath = mSaveRoot + "/enroll_image";
							MakeDir(dumppath);
						}

						mfpView.setBackgroundColor(Color.WHITE);

						mbEnrollLoop = true;
						mbIsTouched = false;
						mtid_enroll = new Thread(EnrollThread);
						mtid_enroll.setPriority(Thread.MAX_PRIORITY);
						mtid_enroll.start();
					}else {
						mbEnrollLoop = false;
						try {
							mtid_enroll.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						mtid_enroll = null;

						mtbtnLive.setEnabled(true);
						mtbtnFpAuth.setEnabled(true);
					}
					break;
				case R.id.tbtnFpAuth2:
					ShowMsg("", true);
					ShowExtraMsg("", false);
					if(mtbtnFpAuth.isChecked() == true) {
						if(!mbGetImgFromStorage) {
							if (mSensor.IsReady() == false) {
								ShowMsg("Device isn't found.", true);
								mtbtnFpAuth.setChecked(false);
								openOtgSetting();
								break;
							}
						}

						// BG?????????, ????????????????????????
						LoadBgToMem();
						if(mBg_Normal == null) {
							ShowMsg("Bg not exist. Make Bg first.", true);
							mtbtnFpAuth.setChecked(false);
							break;
						}

						if(mnTemplateNum == 0) {
							ShowMsg("Please Enroll First", true);
							mtbtnFpAuth.setChecked(false);
							break;
						}

						mtbtnLive.setEnabled(false);
						mtbtnFpEnroll.setEnabled(false);
						mMenu.findItem(R.id.action_deltemplate).setEnabled(false);

						if(mbGetImgFromStorage) {
							MakeDir(mSaveRoot + "/Gingy verify");
						} else {
							String dumppath = mSaveRoot+ "/auth_image";
							MakeDir(dumppath);
						}

						mfpView.setBackgroundColor(Color.WHITE);

						mbAuthLoop = true;
						mbIsTouched = false;
						mtid_auth = new Thread(AuthThread);
						mtid_auth.setPriority(Thread.MAX_PRIORITY);
						mtid_auth.start();
					}else {
						mbAuthLoop = false;
						try {
							mtid_auth.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						mtid_auth = null;
						mtbtnLive.setEnabled(true);
						mtbtnFpEnroll.setEnabled(true);
					}
					break;
				case R.id.mainlayout:
					HideBar();
					break;
			}
		}
	};

	// region Live_Block
	private Runnable getliveImageThread = new Runnable() {
		@Override
		public void run(){
			//??????????????????
			SetRootBrightness(true);

			boolean bIsTwoByte = mnImgBitCount>8 ? true : false;

			while(mbGetImgLoop){
				long lTimeStart = SystemClock.uptimeMillis();

				byte[] imgbuf = getRaw(bIsTwoByte, STATUS_LIVE);
				if(imgbuf == null) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				long lTimeDiffGetImg = SystemClock.uptimeMillis() - lTimeStart;

				// ?????????BG, ?????????????????????, ??????????????????
				if(mBg_Normal == null) {
					if(mbGetImgLoop == false) {
						ShowMsg("", true);
						UpdateLiveImageStatus(null, 0, 0);
					} else {
						ShowMsg("Bg not exist. Make Bg first.", true);
						if(bIsTwoByte) {
							byte[] tmp = mSensor.ConvertTo8Bits(imgbuf, mnImgWidth, mnImgHeight, mnImgBitCount);
							imgbuf = tmp.clone();
						}
						UpdateLiveImageStatus(imgbuf, mnImgWidth, mnImgHeight);
					}
					mbIsSavefile = false;
					continue;
				}

				// ???????????????????????????.
				byte[] imgorg = imgbuf.clone();
				byte[] imgisp = null;
				byte[] imgisp2 = null;

				int []nResultWidth = new int[1];
				int []nResultHeight = new int[1];
				int []updateResult = new int[] {0};

				long lTimeStartIsp = SystemClock.uptimeMillis();
				Isp1(imgbuf, mBg_Normal, mnImgBitCount, nResultWidth, nResultHeight, updateResult);
				// imgbuf?????????Isp1?????????size
				byte []validbuf = new byte[nResultWidth[0] * nResultHeight[0]];
				System.arraycopy(imgbuf, 0, validbuf, 0, nResultWidth[0] * nResultHeight[0]);
				imgbuf = validbuf;
				imgisp = imgbuf.clone();

				byte[] byIsp2Db_Normal = mIsp2Db.clone();
				imgisp2 = imgbuf.clone();

				mSensor.Isp2(imgbuf, imgisp2, byIsp2Db_Normal, false, mbDoIsp2);

				// ??????Debug??????
				if(mbEnableDebugText) {
					long lTimeDiffIsp = SystemClock.uptimeMillis() - lTimeStartIsp;
					int nQuality = mSensor.native_bioImageQuality(imgisp2);
					float uniformity = mSensor.native_ImageUniformity(imgorg,10,mnImgWidth,mnImgHeight);

					String strDbg = String.format("Quality:%d\nSNR:%d\nDR:%d\nUniformity:%.02f\nIsp T:%d ms", nQuality, 0, 0, uniformity, lTimeDiffIsp);
					UpdateDebugMsg(strDbg);
				}

				if(mbIsSavefile) {
					String strTime = GetDateTimeString();
					String filename = "live_" + strTime;

					SaveImg(imgorg, imgisp, imgisp2, mnImgBitCount, filename, STATUS_LIVE, false);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(mfa, "Save\n("+strTime+")", Toast.LENGTH_SHORT).show();
						}
					});

					mbIsSavefile = false;
					mVibrator.vibrate(50);
				}

				if(mbGetImgLoop) {
					UpdateLiveImageStatus(imgisp2, nResultWidth[0], nResultHeight[0]);
				}
			}

			// thread?????????, ??????UI??????
			UpdateDebugMsg("");
			ShowMsg("", true);
			UpdateLiveImageStatus(null, 0, 0);
			//??????????????????
			SetRootBrightness(false);
		}
	};
	// endregion

	// region Enroll_Block
	private Runnable EnrollThread = new Runnable() {
		@Override
		public void run(){
			//??????????????????
			SetRootBrightness(true);

			boolean bIsTwoByte = mnImgBitCount > 8 ? true : false;
			boolean bEnrollOk = false;
			byte[] imgbuf = null;
			int nRet = 0;
			int nCurCount = 0;
			int[] fid = new int[]{0};

			String strTime = GetDateTimeString();
			ArrayList<byte[]> ImgPreEnrolledList = new ArrayList<byte[]>();
			byte[] byIsp2Db_Normal = mIsp2Db.clone();

			UpdateLiveImageStatus(null, 0, 0);

			mSensor.native_bioEnrollInit(mnEnrollNums);

			while(mbEnrollLoop) {
				// ?????????Touch?????????????????????????????????????????????
				if(mbIsTouched) {
					// ???????????????????????????????????????
					if(mbIsLastTouched == true) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						UpdateEnrollStatus(nCurCount, mnEnrollNums, ENROLL_STATUS_LIFTFINGER, imgbuf);
						continue;
					}
					ShowExtraMsg("", false);

					// ???????????????????????????
					try {
						Thread.sleep(150);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mbIsLastTouched = mbIsTouched;
				} else {
					mbIsLastTouched = mbIsTouched;
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					UpdateEnrollStatus(nCurCount, mnEnrollNums, ENROLL_STATUS_NOFINGER, null);
					continue;
				}

				UpdateEnrollStatus(nCurCount, mnEnrollNums, ENROLL_STATUS_UPDATECOUNT, null);
				imgbuf = getRaw(bIsTwoByte, STATUS_ENROLL);

				if(imgbuf == null) {
					if(mbGetImgFromStorage) {
						// ????????????,????????????
						mbEnrollLoop = false;
						bEnrollOk = false;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mtbtnFpEnroll.setChecked(false);
								mtbtnLive.setEnabled(true);
								mtbtnFpAuth.setEnabled(true);
							}
						});
						break;
					}

					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}

				if(!mbGetImgFromStorage) {
					// ?????????????????????, ????????????????????????????????????, ????????????????????????????????????.
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (mbIsTouched == false) {
						Log.d(TAG, "motion up moment! skip!");
						UpdateEnrollStatus(nCurCount, mnEnrollNums, ENROLL_STATUS_NOFINGER, null);
						continue;
					}
				}

				byte [] imgorg = imgbuf.clone();
				int []nResultWidth  = new int[1];
				int []nResultHeight = new int[1];
				int []updateResult = new int[] {0};

				long lTimeStartIsp = SystemClock.uptimeMillis();
				Isp1(imgbuf, mBg_Normal, mnImgBitCount, nResultWidth, nResultHeight, updateResult);
				// imgbuf?????????Isp1?????????size
				byte []validbuf = new byte[nResultWidth[0] * nResultHeight[0]];
				System.arraycopy(imgbuf, 0, validbuf, 0, nResultWidth[0] * nResultHeight[0]);
				imgbuf = validbuf;
				byte []ispbuf = imgbuf.clone();

				// Isp2Db????????????????????????????????????(Online stage)
				if(mnIsp2DbCount > mnEnrollNums) {
					validbuf = imgbuf.clone();
					// If updateResult is zero, update isp2Db.
					if(updateResult[0] == 0) {
						mSensor.Isp2(imgbuf, validbuf, byIsp2Db_Normal, true, mbDoIsp2);
						imgbuf = validbuf;
						Isp2DbCountIncrease(0);//Auto increasing 1
					} else {
						mSensor.Isp2(imgbuf, validbuf, byIsp2Db_Normal, false, mbDoIsp2);
						imgbuf = validbuf;
					}
				}

				// Isp2Db????????????????????????????????????(Training stage)
				if(mnIsp2DbCount < mnEnrollNums) {
					// In training stage (mnIsp2DbCount < mnEnrollNums), if updateResult is not zero, never enroll it.
					// ??????updateResult?????????, training stage???20??????????????????????????????update Isp2db, ?????????updateResult?????????????????????,????????????????????????Isp2db???????????????.
					if(!mbGetImgFromStorage) {
						if (updateResult[0] != 0) {
							ShowExtraMsg("Too similar...\nTips: Move your finger more!", false);
							continue;
						}
					}

					// Train Isp2Db.
					validbuf = imgbuf.clone();
					mSensor.Isp2(imgbuf, validbuf, byIsp2Db_Normal, true, mbDoIsp2);
					imgbuf = validbuf;
				}
				long lTimeDiffIsp = SystemClock.uptimeMillis() - lTimeStartIsp;

				int []nQuality = new int[] {0};
				int []nOverlap_all = new int[] {0};
				int []nOverlap_last = new int[] {0};
				long lEnrollScreenTime = 0;
				long lEnrollTime = 0;

				long lEnrollScreenTimeStart = SystemClock.uptimeMillis();
				if(mSensor.native_bioEnrollScreen(imgbuf, nQuality, nOverlap_all, nOverlap_last) < 0) {
					Log.e(TAG, "Enroll Screen fail!");
				}
				lEnrollScreenTime = SystemClock.uptimeMillis() - lEnrollScreenTimeStart;

				// ??????Debug??????
				if(mbEnableDebugText) {
					float uniformity = 0;
					uniformity = mSensor.native_ImageUniformity(imgorg,10,mnImgWidth,mnImgHeight);
					String strDbg = String.format("Quality:%d\nSNR:%d\nDR:%d\nUniformity:%.02f\nISP T:%d ms", nQuality[0], 0, 0, uniformity, lTimeDiffIsp);
					UpdateDebugMsg(strDbg);
				}

				if(!mbGetImgFromStorage) {
					// QualityScore?????????
					if (mbFingerQualityEnroll) {
						if (nQuality[0] < mnFingerQualityScore) {
							ShowExtraMsg("Finger Quality is too low...\nTips: Press harder...", false);
							continue;
						}
					}

					// OverlapAllScore??????
					if (mbOverlapAllEnroll) {
						if (nOverlap_all[0] > mnOverlapAllScore) {
							ShowExtraMsg("Finger Move too less...\nTips: Move more...", false);
							continue;
						}
					}

					// OverlapLastScore??????
					if (mbOverlapLastEnroll) {
						if (nOverlap_last[0] > mnOverlapLastScore) {
							ShowExtraMsg("Finger Move too less...\nTips: Move more...", false);
							continue;
						}
					}
				}
				long lEnrollTimeStart = SystemClock.uptimeMillis();
				// ??????Training Stage, ?????????????????????, ?????????Enroll_Screen????????????.
				nRet = mSensor.native_bioEnroll(imgbuf);
				lEnrollTime = SystemClock.uptimeMillis() - lEnrollTimeStart;

				if(nRet >= 0) {
					// enroll ok.
					nCurCount = mnEnrollNums - nRet;
					Log.d(TAG, "Enroll Ret:"+nRet+" CurCount:"+nCurCount+" Max:"+mnEnrollNums);
				} else {
					if(nRet == -2) {
						// input image enroll fail, retry another image.
						Log.e(TAG, "Enroll add fail.");
						continue;
					}
					if(nRet == -3) {
						// input image null.
						Log.e(TAG, "Enroll image is null.");
						continue;
					}
					if(nRet == -1) {
						Log.e(TAG, "Enroll Fail.");
					}
					if(nRet == -4) {
						Log.e(TAG, "Enrolled template reach limit.");
					}
					mbEnrollLoop = false;
					bEnrollOk = false;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mtbtnFpEnroll.setChecked(false);
							mtbtnLive.setEnabled(true);
							mtbtnFpAuth.setEnabled(true);
						}
					});
					break;
				}

				Log.d(TAG, String.format("Enroll Screen Time: %d ms, Enroll Time = %d ms", lEnrollScreenTime, lEnrollTime));

				// ?????????Isp2Db??????????????????Isp2????????????Isp1???????????????.
				ImgPreEnrolledList.add(ispbuf);

				if(mbGetImgFromStorage) {
					if(mbDoIsp1) {
						String path = String.format("%s/Gingy enroll/%04d_%d_%03d_isp1.bmp", mSaveRoot, mnReadEnrollPersonIdx, mnReadEnrollFingerIdx, mnReadEnrollCaptureIdx - 1);
						Utils.SaveImage(ispbuf, (short)mnIspResultW, (short)mnIspResultH, 8, path, Utils.FORMAT_BMP);

						if(mbDoIsp2) {
							byte[] isp2data = imgbuf.clone();
							String image_path = String.format("%s/Gingy enroll/%04d_%d_%03d_isp2.bmp", mSaveRoot, mnReadEnrollPersonIdx, mnReadEnrollFingerIdx, mnReadEnrollCaptureIdx - 1);

							Utils.SaveImage(isp2data, (short)mnIspResultW, (short)mnIspResultH, 8, image_path, Utils.FORMAT_BMP);
						}
					}
				} else {
					if (mbSaveEnrollImage) { // DUMP Enroll Image
						String filename = String.format("enroll_%s_%05d", strTime, nCurCount);

						if (mnIsp2DbCount > mnEnrollNums) {
							SaveImg(imgorg, ispbuf, imgbuf, mnImgBitCount, filename, STATUS_ENROLL, false);
						} else {
							// Training Stage???Isp2???????????????Isp2???????????????, ??????????????????????????????Isp2??????, ????????????.
							SaveImg(imgorg, ispbuf, null, mnImgBitCount, filename, STATUS_ENROLL, false);
						}
					}
				}

				UpdateLiveImageStatus(imgbuf, nResultWidth[0], nResultHeight[0]);

				if(nCurCount == mnEnrollNums) {
					// ????????????
					mbEnrollLoop = false;
					bEnrollOk = true;
					ShowExtraMsg("Processing...", false);

					if(mnIsp2DbCount > mnEnrollNums) {
						if (mSensor.native_bioEnrollFinish(fid) == 0) {
							if (fid[0] > 0) {
								mTemplateFID.add(fid[0]);
								mnTemplateNum++;
							} else if (fid[0] < 0) {
								Log.e(TAG, "Enroll finish fail");
							}
						} else {
							Log.e(TAG, "Enroll finish fail");
						}
					}
					break;
				}else {
					UpdateEnrollStatus(nCurCount, mnEnrollNums, ENROLL_STATUS_OK, imgbuf);

					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Log.d(TAG, "");
			} // while(mbEnrollLoop)

			if(bEnrollOk == true) {
				if(mnIsp2DbCount <= mnEnrollNums) {
					// debug
					if(ImgPreEnrolledList.size() != mnEnrollNums) {
						Log.e(TAG, "something error..");
					}

					// Isp2Db???????????????????????????, ???????????????????????????. ??????????????????????????????????????????Isp2Db???Isp2???, ??????Enroll.
					Log.d(TAG, String.format("Training stage reEnroll, image num: %d", ImgPreEnrolledList.size()));
					mSensor.native_bioEnrollInit(ImgPreEnrolledList.size());

					for(int nIdx=0; nIdx<ImgPreEnrolledList.size(); nIdx++) {
						byte[] ispbuf = ImgPreEnrolledList.get(nIdx);
						byte[] isp2buf = ispbuf.clone();
						mSensor.Isp2(ispbuf, isp2buf, byIsp2Db_Normal, false, mbDoIsp2);

						if(mbGetImgFromStorage) {
							if(mbDoIsp2) {
								byte[] isp2data = isp2buf.clone();
								String image_path = String.format("%s/Gingy enroll/%04d_%d_%03d_isp2.bmp", mSaveRoot, mnReadEnrollPersonIdx, mnReadEnrollFingerIdx, nIdx + 1);

								Utils.SaveImage(isp2data, (short)mnIspResultW, (short)mnIspResultH, 8, image_path, Utils.FORMAT_BMP);
							}
						} else {
							if (mbSaveEnrollImage) {
								String filename = String.format("enroll_%s_%05d", strTime, nIdx + 1);
								// Training Stage???, Isp2?????????ReEnroll????????????????????????, ???????????????.
								SaveImg(null, null, isp2buf, mnImgBitCount, filename, STATUS_ENROLL, false);
							}
						}

						nRet = mSensor.native_bioEnroll(isp2buf);
						Log.d(TAG, String.format("Training Stage->ReEnroll [%02d] PBRet:%d", nIdx, nRet));

						ShowExtraMsg("Processing...", false);
					}

					if(mSensor.native_bioEnrollFinish(fid) == 0) {
						if(fid[0] > 0) {
							mTemplateFID.add(fid[0]);
							mnTemplateNum++;
						} else if(fid[0] < 0) {
							Log.e(TAG, "Enroll finish fail");
						}
					} else {
						Log.e(TAG, "Enroll finish fail");
					}
					Isp2DbCountIncrease(mnEnrollNums + 1);// <- ??????1, ????????????????????????Isp2Db??????????????????????????????(mnIsp2DbCount????????????????????????0->21)
				}

				// ??????Isp2Db
				mIsp2Db = byIsp2Db_Normal.clone();
				UpdateIsp2DB();

				Log.d(TAG, "Enroll Success.");
				UpdateEnrollStatus(nCurCount, mnEnrollNums, ENROLL_STATUS_OK, imgbuf);

				String id = String.format("%04d_%d", mnReadEnrollPersonIdx, mnReadEnrollFingerIdx);
				if(fid[0] > 0) {
					mIDList.set(fid[0], id);

					Gson gson = new Gson();
					String json = gson.toJson(mIDList);

					SharedPreferences.Editor editor = mSharedPref.edit();
					editor.putString("IDList", json);
					editor.commit();
				}
				mnReadEnrollCaptureIdx = 1;
				mnReadEnrollFingerIdx++;
			} else {
				Log.d(TAG, "Enroll Cancel/Fail.");
				UpdateEnrollStatus(nCurCount, mnEnrollNums, ENROLL_STATUS_CANCEL, imgbuf);
				mnReadEnrollCaptureIdx = 1;
			}

			// enroll thread?????????, ??????UI??????
			UpdateDebugMsg("");
			if(!mbGetImgFromStorage||bEnrollOk)
				ShowExtraMsg("", false);
			UpdateLiveImageStatus(null, 0, 0);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mMenu.findItem(R.id.action_deltemplate).setEnabled(true);
				}
			});

			//??????????????????
			SetRootBrightness(false);
		}
	};

	private void UpdateEnrollStatus(final int nCurCount, final int nEnrollMaxCount, final int nStatus, final byte[]imgbuf) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(nStatus == ENROLL_STATUS_OK && nCurCount == nEnrollMaxCount) {
					mtbtnFpEnroll.setChecked(false);
					ShowMsg("Enroll Success!", false);

					mtbtnLive.setEnabled(true);
					mtbtnFpAuth.setEnabled(true);
					mMenu.findItem(R.id.action_deltemplate).setEnabled(true);
				}

				if(nStatus == ENROLL_STATUS_CANCEL) { // user manual cancel.
					mtbtnFpEnroll.setChecked(false);
					ShowMsg("Enroll Cancel!", false);
				}else if(nStatus == ENROLL_STATUS_ERR){
					mtbtnFpEnroll.setChecked(false);
					ShowMsg("Enroll Err stop!", true);

					mtbtnLive.setEnabled(true);
					mtbtnFpAuth.setEnabled(true);
					mMenu.findItem(R.id.action_deltemplate).setEnabled(true);
				}else if(nStatus == ENROLL_STATUS_NOFINGER) {
					ShowMsg(String.format("Enroll : %d/%d", nCurCount, nEnrollMaxCount), false);
				}else if(nStatus == ENROLL_STATUS_UPDATECOUNT) {
					ShowMsg(String.format("Enroll : %d/%d", nCurCount, nEnrollMaxCount), false);
				}else if(nStatus == ENROLL_STATUS_LIFTFINGER) {
					ShowMsg(String.format("Enroll : %d/%d (Lift finger)", nCurCount, nEnrollMaxCount), true);
				}else if(nStatus == ENROLL_STATUS_SAMEAREA) {
					ShowMsg(String.format("Enroll : %d/%d", nCurCount, nEnrollMaxCount), true);
				}else {
					if(nCurCount == nEnrollMaxCount) {
						ShowMsg(String.format("Enroll OK"), false);
					} else {
						ShowMsg(String.format("Enroll Progress: %d/%d", nCurCount, nEnrollMaxCount), false);
					}
					mVibrator.vibrate(50);
				}
			}
		});
	}
	// endregion

	// region Auth_Block
	private Runnable AuthThread = new Runnable() {
		@Override
		public void run(){
			//??????????????????
			SetRootBrightness(true);

			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
			int nRet = AUTH_STATUS_OK;
			byte[] imgbuf = null;
			long lTimeStart = 0;

			boolean bIsTwoByte = mnImgBitCount > 8 ? true : false;

			UpdateLiveImageStatus(null, 0, 0);
			mSensor.native_bioAuthInit();

			while(mbAuthLoop) {
				String strTime = GetDateTimeString();
				// ?????????Touch?????????????????????????????????????????????
				if(mbIsTouched) {
					lTimeStart = SystemClock.uptimeMillis();
					// ???????????????????????????????????????
					if(mbIsLastTouched == true) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						continue;
					}

					// ???????????????????????????
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mbIsLastTouched = mbIsTouched;
				} else {
					mbIsLastTouched = mbIsTouched;
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//??????GUI???????????????
					UpdateAuthStatus(0,AUTH_STATUS_NOFINGER);
					UpdateAuthDebugMsg(0, -1, 0, 0, 0, 0, 0);
					continue;
				}

				imgbuf = getRaw(bIsTwoByte, STATUS_AUTH);

				if(imgbuf == null) {
					if(mbGetImgFromStorage) {
						// ??????????????????or???????????????,????????????.
						mbAuthLoop = false;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mtbtnFpAuth.setChecked(false);
								mtbtnLive.setEnabled(true);
								mtbtnFpEnroll.setEnabled(true);
							}
						});
						break;
					}
					Log.e(TAG, "getimage null!");
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}

				if(!mbGetImgFromStorage) {
					// ?????????????????????, ????????????????????????????????????, ????????????????????????????????????.
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (mbIsTouched == false) {
						UpdateAuthDebugMsg(0, -1, 0, 0, 0, 0, 0);
						continue;
					}
				}

				byte[] imgorg = imgbuf.clone();
				byte[] imgisp = null;
				byte[] imgisp2 = null;

				int []nResultWidth = new int[1];
				int []nResultHeight = new int[1];
				int []updateResult = new int[] {0};

				long lTimeStartIsp = SystemClock.uptimeMillis();
				Isp1(imgbuf, mBg_Normal, mnImgBitCount, nResultWidth, nResultHeight, updateResult);
				// imgbuf?????????Isp1?????????size
				byte []validbuf = new byte[nResultWidth[0] * nResultHeight[0]];
				System.arraycopy(imgbuf, 0, validbuf, 0, nResultWidth[0] * nResultHeight[0]);
				imgbuf = validbuf;
				imgisp = imgbuf.clone();

				byte[] byIsp2Db_Normal = mIsp2Db.clone();
				imgisp2 = imgisp.clone();
				if(updateResult[0] == 0) {
//					Log.d(TAG, "isp2 DB update");
					mSensor.Isp2(imgisp, imgisp2, byIsp2Db_Normal, true, mbDoIsp2);
					Isp2DbCountIncrease(0);//Auto increasing 1
					mIsp2Db = byIsp2Db_Normal.clone();
				} else {
//					Log.d(TAG, "isp2 DB NOT update");
					mSensor.Isp2(imgisp, imgisp2, byIsp2Db_Normal, false, mbDoIsp2);
				}

				long lTimeDiffIsp = SystemClock.uptimeMillis() - lTimeStartIsp;

				int nQuality = 0;
				float uniformity = 0;
				if(mbEnableDebugText) {
					nQuality = mSensor.native_bioImageQuality(imgisp2);
					uniformity = mSensor.native_ImageUniformity(imgorg, 10, mnImgWidth, mnImgHeight);
				}

				int nFingerID = -1;
				boolean bIsPass = false;
				long lAuthTimeBeg = SystemClock.uptimeMillis();

				nRet = mSensor.native_bioAuthenticate(imgisp2);
				if(nRet > 0) {
					// match
					bIsPass = true;
					nFingerID = nRet;
					Log.d(TAG, String.format("Auth match FID: %d", nFingerID));
				} else if(nRet == 0) {
					Log.d(TAG, "Auth mismatch");
				} else {
					Log.e(TAG, "auth fail!");
				}
				WriteCSV(nRet);

				final long lPbAuthTimeDiff = SystemClock.uptimeMillis() - lAuthTimeBeg;

				// ???????????????????????????????????????????????????????????????
				final long lAuthTime = SystemClock.uptimeMillis() -lTimeStart;

				// ???????????????????????????GUI????????????????????????
				UpdateAuthStatus(bIsPass==true?1:0,AUTH_STATUS_OK);
				UpdateAuthDebugMsg(lAuthTime, nFingerID, nQuality, 0, 0, uniformity, lTimeDiffIsp);
				UpdateLiveImageStatus(imgisp2, nResultWidth[0], nResultHeight[0]);

				if(mbGetImgFromStorage) {
					if(mbDoIsp1) {
						String path = String.format("%s/Gingy verify/%04d_%d_%03d_isp1.bmp", mSaveRoot, mnReadVerifyPersonIdx, mnReadVerifyFingerIdx, mnReadVerifyCaptureIdx - 1);
						Utils.SaveImage(imgisp, (short)mnIspResultW, (short)mnIspResultH, 8, path, Utils.FORMAT_BMP);

						if(mbDoIsp2) {
							byte[] isp2data = imgisp2.clone();
							String image_path = String.format("%s/Gingy verify/%04d_%d_%03d_isp2.bmp", mSaveRoot, mnReadVerifyPersonIdx, mnReadVerifyFingerIdx, mnReadVerifyCaptureIdx - 1);

							Utils.SaveImage(isp2data, (short)mnIspResultW, (short)mnIspResultH, 8, image_path, Utils.FORMAT_BMP);
						}
					}
				} else {
					if (mbSaveAuthImage) { // DUMP Auth Image
						String filename = String.format("auth_%d_%s", (bIsPass == true) ? 1 : 0, strTime);
						SaveImg(imgorg, imgisp, imgisp2, mnImgBitCount, filename, STATUS_AUTH, false);
					}
				}

				final long lTimerAuthDiff = SystemClock.uptimeMillis() -lTimeStart;
				Log.d(TAG, String.format("=== Pb Auth Time: %d ms, Foreground Auth Time: %d ms, Total Auth Time: %d ms ===", lPbAuthTimeDiff, lAuthTime, lTimerAuthDiff));

			} // while(mbAuthLoop)

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Bitmap bmp = Bitmap.createBitmap(mnImgWidth, mnImgHeight, Bitmap.Config.ARGB_8888);
					bmp.eraseColor(Color.BLACK);
					mfpView.setImageBitmap(bmp);
					mMenu.findItem(R.id.action_deltemplate).setEnabled(true);
				}
			});
			UpdateIsp2DB();
			UpdateDebugMsg("");
			UpdateAuthStatus(0, AUTH_STATUS_NOFINGER);
			UpdateLiveImageStatus(null, 0, 0);

			//??????????????????
			SetRootBrightness(false);
		}
	};

	private void UpdateAuthStatus(final int nPass, final int nStatus) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(nStatus == AUTH_STATUS_OK) {
					if(nPass==1) {
						mVibrator.vibrate(50);
						Log.d(TAG, "PASS !!!");
						mAuthResView.setImageResource(R.drawable.fppass1);
					} else {
						mVibrator.vibrate(500);
						Log.d(TAG, "NG !!!");
						mAuthResView.setImageResource(R.drawable.fpng1);
					}
				}else if(nStatus == AUTH_STATUS_NOFINGER) {
					Bitmap bmp = Bitmap.createBitmap(mnImgWidth, mnImgHeight, Bitmap.Config.ARGB_8888);
					bmp.eraseColor(Color.BLACK);
					mAuthResView.setImageBitmap(bmp);
				}else ShowMsg("Auth err:"+nStatus, true);
			}
		});
	}

	private void UpdateAuthDebugMsg(final long lAuthTime, final int nFingerID, final int nImgQuality, final int SNR, final int DR, final float Uniformity, final long lTimeDiffIsp) {
		if (mbEnableDebugText==false){
			return;
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String strDbg = String.format("Auth T:%d ms\nFID:%d\nQuality:%d\nSNR:%d\nDR:%d\nUniformity:%.02f\nISP t:%d ms", lAuthTime, nFingerID, nImgQuality, SNR, DR, Uniformity, lTimeDiffIsp);
				UpdateDebugMsg(strDbg);
			}
		});
	}
	// endregion

	// region MakeBg_Block
	private void MakeBG(boolean bStart) {
		if (bStart == true) {
			if (mtid_makebg != null)
				return;

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mtbtnLive.setEnabled(false);
					mtbtnFpEnroll.setEnabled(false);
					mtbtnFpAuth.setEnabled(false);
					mtbtnLive.setVisibility(View.INVISIBLE);
					mtbtnFpEnroll.setVisibility(View.INVISIBLE);
					mtbtnFpAuth.setVisibility(View.INVISIBLE);
					mfpView.setBackgroundColor(Color.WHITE);
				}
			});

			mbmakebgLoop = true;
			mtid_makebg = new Thread(MakeBgThread);
			mtid_makebg.start();
			mtid_makebg.setPriority(Thread.MAX_PRIORITY - 1);
		} else {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mtid_makebg == null)
						return;

					mbmakebgLoop = false;
					try {
						mtid_makebg.join();
						mtid_makebg = null;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					mtbtnLive.setEnabled(true);
					if(mbDoIsp1 == true) {
						mtbtnFpEnroll.setEnabled(true);
						mtbtnFpAuth.setEnabled(true);
					}
					mtbtnLive.setVisibility(View.VISIBLE);
					mtbtnFpEnroll.setVisibility(View.VISIBLE);
					mtbtnFpAuth.setVisibility(View.VISIBLE);
				}
			});
		}

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				MenuItem bedMenuItem;
				bedMenuItem = mMenu.findItem(R.id.action_makebg);

				if (mbmakebgLoop)
					bedMenuItem.setTitle("Stop Make BG");
				else
					bedMenuItem.setTitle("Start Make BG");
			}
		});
	}

	private Runnable MakeBgThread = new Runnable() {
		@Override
		public void run() {
			//??????????????????
			SetRootBrightness(true);
			boolean bIsTwoByte = mnImgBitCount > 8 ? true : false;

			// ?????????200???
			int nMaxImgCount = 200;
			int nRet = 0;

			// Setting??????????????????????????????, ????????????????????????????????????????????????????????????.
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			while (mbmakebgLoop) {
				byte[] imgbuf = getRaw(bIsTwoByte, 0);

				if (imgbuf == null) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}

				// ????????????????????????,??????????????????.
				byte[]tmp = imgbuf.clone();
				if(bIsTwoByte) {
					tmp = mSensor.ConvertTo8Bits(imgbuf, mnImgWidth, mnImgHeight, mnImgBitCount);
				}
				UpdateLiveImageStatus(tmp, mnImgWidth, mnImgHeight);

				// Keep inputting image data until nMaxImgCount, it will return remaining amount.
				nRet = mSensor.native_ImgBgMake(imgbuf, mnImgWidth, mnImgHeight, bIsTwoByte);

				ShowMsg(String.format("MakeBG [%03d/%03d]", nMaxImgCount - nRet, nMaxImgCount), false);

				if(nRet == 0){
					// When return value become 0, get outdata.
					byte[] outdata = new byte[mnImgWidth * mnImgHeight * (bIsTwoByte == true ? 2 : 1)];

					nRet = mSensor.native_ImgBgGet(outdata, mnImgWidth, mnImgHeight, bIsTwoByte);

					if(nRet == 0){
						// Save BG to sdcard.
						ByteBuffer bb = ByteBuffer.wrap(outdata);
						Utils.SaveToFile(bb, mSaveRoot, "BG");
					}else{
						Log.d(TAG, "ImgBgGet() fail");
					}

					mbmakebgLoop = false;
					MakeBG(false);
				}
			}
			// Call after ImgBgMake & ImgBgGet finish.
			mSensor.native_ImgBgFinish();
			//??????????????????
			SetRootBrightness(false);
		}
	};
	// endregion

	/* ??????live/enroll/auth thread????????? */
	private void StopButtonThread(){
		if(mtid_liveimage != null) {
			mbGetImgLoop = false;
			try {
				mtid_liveimage.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mtid_liveimage = null;
		}

		if(mtid_enroll != null) {
			Log.d(TAG, "STOP enroll");
			mbEnrollLoop = false;
			try {
				mtid_enroll.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mtid_enroll = null;
		}

		if(mtid_auth != null) {
			Log.d(TAG, "STOP auth");
			mbAuthLoop = false;
			try {
				mtid_auth.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mtid_auth = null;
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mtbtnLive.setChecked(false);
				mtbtnFpEnroll.setChecked(false);
				mtbtnFpAuth.setChecked(false);
				mtbtnLive.setEnabled(true);
				if(mbDoIsp1 == true) {
					mtbtnFpEnroll.setEnabled(true);
					mtbtnFpAuth.setEnabled(true);
				}
			}
		});
	}

	private OnTouchListener touchButton = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// ?????????????????????
			if((event.getAction() == MotionEvent.ACTION_DOWN) || (event.getAction() == MotionEvent.ACTION_MOVE)) {
				if(mbIsTouched == false) {
					mbIsTouched = true;
					return true;
				}
			}

			// ?????????????????????
			if(event.getAction() == MotionEvent.ACTION_UP||event.getAction() == MotionEvent.ACTION_CANCEL) {
				mbIsTouched = false;
				if(!mbGetImgFromStorage) {
					ShowExtraMsg("", false);
				}
				return true;
			}
			return false;
		}
	};

	private OnLongClickListener longpress = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			switch(v.getId()) {
			case R.id.ivFP: // live?????????, ???????????????, ????????????.
				if(mtid_liveimage != null) {
					mbIsSavefile = true;
				}
				return true;
			}
			return false;
		}
	};

	/**
	 * ???????????????????????????
	 * @param iCenterX ??????????????????X
	 * @param iCenterY ??????????????????Y
	 */
	private void GetRotateCenter(int[] iCenterX,int[] iCenterY)
	{
		int iWidth,iHeight;
		DisplayMetrics dm = this.getResources().getDisplayMetrics();
		iWidth = Integer.parseInt(mSharedPref.getString("setting_image_sensingarea_width", "12"));
		iHeight = Integer.parseInt(mSharedPref.getString("setting_image_sensingarea_height", "12"));
		int nSaW = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, iWidth, dm);
		int nSaH = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, iHeight, dm);
		int nSActiveLen=(int)(Math.sqrt(nSaW*nSaW+nSaH*nSaH)+0.5f);//?????????????????????
		float fHalfActiveLen = nSActiveLen/2.0f;
		iCenterX[0]=(int)fHalfActiveLen;
		iCenterY[0]=(int)fHalfActiveLen;
	}

	/**
	 * ????????????
	 * @param iDegree ???????????????
	 */
	private void SetRotate(int iDegree){
		int[] iCenterX=new int[1];
		int[] iCenterY=new int[1];
		GetRotateCenter(iCenterX,iCenterY);
		Matrix matrix = new Matrix();
		matrix.postRotate(iDegree,iCenterX[0],iCenterY[0]);
		mbtnSensingArea.setImageMatrix(matrix);
	}

	/* ??????????????????Click?????? */
	private  OnClickListener clickRotate = new OnClickListener(){
		@Override
		public void onClick(View view) {
			int iDegree;
			iDegree = Integer.parseInt(mSharedPref.getString("setting_image_sensorposition_rotate", "0"));
			switch(view.getId()){
				case R.id.ivRotateRight:
					//???????????????1???
					iDegree+=1;
					break;
				case R.id.ivRotateRight2:
					//???????????????5???
					iDegree+=5;
					break;
				case R.id.ivRotateLeft:
					//???????????????1???
					iDegree-=1;
					break;
				case R.id.ivRotateLeft2:
					//???????????????5???
					iDegree-=5;
					break;
			}
			if (iDegree>360){
				iDegree = iDegree-360;
			}else if (iDegree<0){
				iDegree = 360+iDegree;
			}
			SetRotate(iDegree);
			mSharedPref.edit()
					.putString("setting_image_sensorposition_rotate", String.format("%d",iDegree))
					.commit();
		}
	};
	/* ???????????????????????????Click?????? */
	private OnClickListener clickArrow = new OnClickListener(){
		public void onClick(View view){
			int iLeft,iTop;
			//???????????????????????????GridLayout???leftMargin,topMargin
			iLeft = Integer.parseInt(mSharedPref.getString("setting_image_sensorposition_left", "300"));
			iTop = Integer.parseInt(mSharedPref.getString("setting_image_sensorposition_top", "1000"));
			//?????????????????????1px, ?????????????????????10px
			switch (view.getId()) {
				case R.id.ivArrowUp:
					iTop = iTop - 1;
					break;
				case R.id.ivArrowDown:
					iTop = iTop + 1;
					break;
				case R.id.ivArrowLeft:
					iLeft = iLeft - 1;
					break;
				case R.id.ivArrowRight:
					iLeft = iLeft + 1;
					break;
				case R.id.ivArrowUp2:
					iTop = iTop - 10;
					break;
				case R.id.ivArrowDown2:
					iTop = iTop + 10;
					break;
				case R.id.ivArrowLeft2:
					iLeft = iLeft - 10;
					break;
				case R.id.ivArrowRight2:
					iLeft = iLeft + 10;
					break;
			}
			//????????????Margin??????GridLayout
			setMargins(mSensingAreaLayout, iLeft, iTop, 0, 0);
			//?????????left,top????????????setting menu
			mSharedPref.edit()
					.putString("setting_image_sensorposition_left", String.format("%d",iLeft))
					.putString("setting_image_sensorposition_top", String.format("%d",iTop))
					.commit();
		}
	};

	/* ?????????????????? */
	private void SetSensingAreaPos() {
		int nMarginLeft = Integer.parseInt(mSharedPref.getString("setting_image_sensorposition_left", "300"));
		int nMarginTop = Integer.parseInt(mSharedPref.getString("setting_image_sensorposition_top", "1000"));
		setMargins(mSensingAreaLayout, nMarginLeft, nMarginTop, 0, 0);
		if (mbRectSensingArea) {
			int iDegree = Integer.parseInt(mSharedPref.getString("setting_image_sensorposition_rotate", "0"));
			SetRotate(iDegree);
		}
	}

	private static void setMargins (View v, int l, int t, int r, int b) {
		if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
			ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
			p.setMargins(l, t, r, b);
			v.requestLayout();
		}
	}

	/* ?????????????????? */
	private void SetSensingAreaSizeAndColor() {
		DisplayMetrics dm = this.getResources().getDisplayMetrics();
		int nSensingAreaSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mnSensingAreaSize, dm);
		int nSaWidth = Integer.parseInt(mSharedPref.getString("setting_image_sensingarea_width", "12"));
		int nSaHeight = Integer.parseInt(mSharedPref.getString("setting_image_sensingarea_height", "12"));
		int nSaW = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, nSaWidth, dm);
		int nSaH = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, nSaHeight, dm);
		float fHalfW = (float)(nSaW/2.0);
		float fHalfH = (float)(nSaH/2.0);
		int nSActiveLen=(int)(Math.sqrt(nSaW*nSaW+nSaH*nSaH)+0.5f);//?????????????????????
		float fHalfActiveLen = nSActiveLen/2.0f;
		float fLeft = 0;
		float fTop = 0;
		float fRight = 0;
		float fDown = 0;
		final Bitmap bmp;
		if(mbRectSensingArea) {
			//?????????????????????????????????bmp???????????????????????????????????????(????????????????????????bmp???)
			fLeft = fHalfActiveLen-fHalfW;
			fTop = fHalfActiveLen-fHalfH;
			fRight = fHalfActiveLen+fHalfW;
			fDown = fHalfActiveLen+fHalfH;
			//?????????????????????????????????bmp
			bmp = Bitmap.createBitmap(nSActiveLen, nSActiveLen, Bitmap.Config.ARGB_8888);
		} else {
			bmp = Bitmap.createBitmap(nSensingAreaSize, nSensingAreaSize, Bitmap.Config.ARGB_8888);
		}

		int bColorR = mnSensingColorR;
		int bColorG = mnSensingColorG;
		int bColorB = mnSensingColorB;
		bmp.eraseColor(Color.BLACK);

		Canvas cv = new Canvas(bmp);
		Paint paint = new Paint();
		paint.setColor(0xFF000000|bColorR<<16|bColorG<<8|bColorB);
		float fCenterX = (float)(nSensingAreaSize/2.0);
		float fCenterY = (float)(nSensingAreaSize/2.0);
		float fRadius  = (float)(nSensingAreaSize/2.0);

		if(mbRectSensingArea) {
			cv.drawRect(fLeft, fTop, fRight, fDown, paint);
		} else {
			cv.drawCircle(fCenterX, fCenterY, fRadius, paint);
		}

		// ??????????????????
		if(mbAimingPoing == true) {
			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(mfDrawlineWidth);
			if(mbRectSensingArea) {
				cv.drawLine(fLeft, fHalfActiveLen, fLeft+nSaW, fHalfActiveLen, paint);
				cv.drawLine(fHalfActiveLen, fTop, fHalfActiveLen, fTop+nSaH, paint);
			} else {
				cv.drawLine(fCenterX, 0, fCenterX, nSensingAreaSize, paint);
				cv.drawLine(0, fCenterY, nSensingAreaSize, fCenterY, paint);
			}
			paint.setStrokeWidth(1.0f);

			if(mSharedPref.getBoolean("setting_image_pressarea_adjust", false)) {
				//????????????????????????
				setAimingArrowVisibility(true);
			} else {
				//????????????????????????
				setAimingArrowVisibility(false);
			}
		} else {
			//????????????????????????
			setAimingArrowVisibility(false);
		}

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//???????????????????????????????????????????????????????????????????????????
				if (mbRectSensingArea){
					mbtnSensingArea.setAdjustViewBounds(false);
					mbtnSensingArea.setScaleType(ImageView.ScaleType.MATRIX);
				}else{
					mbtnSensingArea.setAdjustViewBounds(true);
				}
				mbtnSensingArea.setImageBitmap(bmp);
			}
		});
	}

	/* ????????????, ???Sensor??????????????????????????????????????? */
	private void SetScreenReverse() {
		// Android 6 ??????, ????????????????????????????????????
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean bIsReverse = mSharedPref.getBoolean("setting_image_screenreverse", false);
			if(bIsReverse) {
				if(!Settings.System.canWrite(mfa)) {
					Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
					intent.setData(Uri.parse("package:" + mfa.getPackageName()));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mfa.startActivity(intent);
				} else {
					Settings.System.putInt(getContentResolver(),Settings.System. USER_ROTATION, Surface.ROTATION_180);
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
					Log.d(TAG, "Screen REVERSE PORTRAIT");
				}
			} else {
				if(Settings.System.canWrite(mfa)) {
					Settings.System.putInt(getContentResolver(),Settings.System. USER_ROTATION, Surface.ROTATION_0);
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					Log.d(TAG, "Screen PORTRAIT");
				}
			}
		}
	}

	private void setAimingArrowVisibility(boolean bVisibility) {
		int v;
		if(bVisibility) {
			v = View.VISIBLE;
		} else {
			v = View.INVISIBLE;
		}

		mArrowUpView.setVisibility(v);
		mArrowDownView.setVisibility(v);
		mArrowLeftView.setVisibility(v);
		mArrowRightView.setVisibility(v);
		mArrowUpView2.setVisibility(v);
		mArrowDownView2.setVisibility(v);
		mArrowLeftView2.setVisibility(v);
		mArrowRightView2.setVisibility(v);
		mRotateRight.setVisibility(v);
		mRotateRight2.setVisibility(v);
		mRotateLeft.setVisibility(v);
		mRotateLeft2.setVisibility(v);

	}

	private void SetRootBrightness(boolean bEnable) {
		if(mbHighBrightness) {
			try {
				if(bEnable) {
					String[] temp = {"su", "-c", "echo 0x20000> /sys/class/drm/card0-DSI-1/disp_param"};
					Runtime.getRuntime().exec(temp);
				} else {
					String[] temp = {"su", "-c", "echo 0xE0000> /sys/class/drm/card0-DSI-1/disp_param"};
					Runtime.getRuntime().exec(temp);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void LoadBgToMem() {
		String strFileBG = mSaveRoot+"/BG";

		File bgfile = new File(strFileBG);
		if(bgfile.exists()) {
			mBg_Normal = Utils.LoadFromFile(strFileBG);
			Log.d(TAG, "Load BG from sd.");
		}
	}

	private void MakeDir(String path){
		File folder = new File(path);
		if(!folder.exists()) {
			folder.mkdir();
		}
	}

	private byte[] getRaw(boolean twobyte, int status){
		if(mbGetImgFromStorage) {
			if(status == STATUS_ENROLL) {
				while(true) {
					String image_name = String.format("%04d_%d_%03d.raw", mnReadEnrollPersonIdx, mnReadEnrollFingerIdx, mnReadEnrollCaptureIdx);
					String image_path = mSaveRoot + "/RAD enroll/" + image_name;

					File file = new File(image_path);
					if(!file.exists()) {
//						Log.d(TAG, "File: " + image_name + " not exist!");

						if(mnReadEnrollCaptureIdx == 1) {
							mnReadEnrollFingerIdx++;

							if(mnReadEnrollFingerIdx > 6) {
								mnReadEnrollPersonIdx++;
								mnReadEnrollFingerIdx = 1;
							}
							if(mnReadEnrollPersonIdx > 100) {
								// already read all exist file
								ShowExtraMsg("all file read", true);
								mnReadEnrollPersonIdx = 1;
								mnReadEnrollFingerIdx = 1;
								mnReadEnrollCaptureIdx = 1;
								return null;
							}
							continue;
						} else {
							// file doesn't follow the rule! Enroll cancel.
							ShowExtraMsg(String.format("%04d_%d_XXX doesn't follow the naming rule!", mnReadEnrollPersonIdx, mnReadEnrollFingerIdx), true);
							mnReadEnrollFingerIdx++;
							mnReadEnrollCaptureIdx = 1;
							return null;
						}
					} else {
						Log.d(TAG, "Read: " + image_name);
						mnReadEnrollCaptureIdx++;
						return Utils.LoadFromFile(image_path);
					}
				}
			}

			if(status == STATUS_AUTH) {
				while(true) {
					String image_name = String.format("%04d_%d_%03d.raw", mnReadVerifyPersonIdx, mnReadVerifyFingerIdx, mnReadVerifyCaptureIdx);
					String image_path = mSaveRoot + "/RAD verify/" + image_name;

					File file = new File(image_path);
					if(!file.exists()) {
//						Log.d(TAG, "File: " + image_name + " not exist!");

						if(mnReadVerifyCaptureIdx == 1) {
							mnReadVerifyFingerIdx++;

							if(mnReadVerifyFingerIdx > 6) {
								mnReadVerifyPersonIdx++;
								mnReadVerifyFingerIdx = 1;
							}
							if(mnReadVerifyPersonIdx > 100) {
								// already read all exist file
								ShowExtraMsg("all file read", true);
								mnReadVerifyPersonIdx = 1;
								mnReadVerifyFingerIdx = 1;
								mnReadVerifyCaptureIdx = 1;
								return null;
							}
							continue;
						} else {
							mnReadVerifyFingerIdx++;
							mnReadVerifyCaptureIdx = 1;
							continue;
						}
					} else {
						Log.d(TAG, "Read: " + image_name);
						mnReadVerifyCaptureIdx++;
						return Utils.LoadFromFile(image_path);
					}
				}
			}
		} else {
			int length = mnImgWidth * mnImgHeight * (twobyte == true ? 2 : 1);
			ByteBuffer buffer = ByteBuffer.allocate(length);
			mSyncLock.lock();
//		Log.d(TAG, "getW: "+mSensor.getImgWidth());
//		Log.d(TAG, "getH: "+mSensor.getImgHeight());
//		Log.d(TAG, "getByte: "+mSensor.GetDataBits());
			if (mSensor.IsReady() && mSensor.getImage(buffer, mnImgWidth, mnImgHeight) == true) {
				mSyncLock.unlock();
				return buffer.array();
			} else {
				mSyncLock.unlock();
				return null;
			}
		}
		return null;
	}

	/* ????????????????????? */
	private void UpdateLiveImageStatus(final byte[] image, final int nImgW, final int nImgH) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				int nShowAreaW = ((GridLayout)findViewById(R.id.imgGrid)).getWidth();
				int nShowAreaH = ((GridLayout)findViewById(R.id.imgGrid)).getHeight();

				if(image != null) {
					Bitmap bmp = RawToBmp(image, nImgW, nImgH, nShowAreaW, nShowAreaW * nImgH / nImgW);
					Canvas cv =new Canvas(bmp);
					Paint paint = new Paint();
					if(mbAimingPoing == true) {
						int nW = bmp.getWidth();
						int nH = bmp.getHeight();
						paint.setColor(Color.RED);
						paint.setStrokeWidth(1);
						cv.drawLine(nW/2, 0, nW/2, nH, paint);
						cv.drawLine(0, nH/2, nW, nH/2, paint);
						cv.drawLine(nW/6, 0, nW/6, nH, paint);
						cv.drawLine((float)(nW*(5.0/6.0)), 0, (float)(nW*(5.0/6.0)), nH, paint);
					}

					mfpView.setImageBitmap(bmp);
				}
				else {
					Bitmap bmp = Bitmap.createBitmap(nShowAreaW, nShowAreaH, Bitmap.Config.ARGB_8888);
					bmp.eraseColor(Color.BLACK);
					mfpView.setImageBitmap(bmp);
				}
			}
		});
	}

	private void ShowMsg(final String msgtext, final boolean bIsRed){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(bIsRed)
					mtvmsgText.setTextColor(Color.RED);
				else mtvmsgText.setTextColor(Color.rgb(0x33, 0xB5, 0xE5));
				mtvmsgText.setText(msgtext);
			}
		});
	}

	private void ShowExtraMsg(final String msgtext, final boolean bIsRed){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(bIsRed)
					mtvExtraMsg.setTextColor(Color.RED);
				else mtvExtraMsg.setTextColor(Color.rgb(0xFF, 0xFF, 0xFF));
				mtvExtraMsg.setText(msgtext);
			}
		});
	}

	private void UpdateDebugMsg(final String msgtext){
		if(mbEnableDebugText == false)
			return;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mtvDebug.setText(msgtext);
			}
		});
	}

	private void IspInit(boolean bInitDb){
		// ?????????Isp2Db
		if(bInitDb == true) {
			mnIsp2DbCount = 0;
			mIsp2Db = new byte[28 + (4 * mnIspResultW * mnIspResultH)];
		}

		if (bInitDb == true) {
			mSensor.native_ispInit(mnEnrollNums, mIsp2Db, mnImgWidth, mnImgHeight);
			UpdateIsp2DB();
		} else {
			mSensor.native_ispInit(mnEnrollNums, null, mnImgWidth, mnImgHeight);
		}
	}

	private void Isp1(byte[]imgbuf, byte[]bgbuf, int nDataBits, int []pnResultWidth, int []pnResultHeight, int []pnUpdateResult){
		// ?????????mbDoIsp1, ????????????ResultW/H???IspResultW/H. ?????????, ??????OrgW/H.
		// ?????????mbDoIsp1, ???12bit??????????????????8bit???????????????, 8bit?????????????????????.
		mSensor.native_ISP1(imgbuf, bgbuf, nDataBits, pnResultWidth, pnResultHeight, pnUpdateResult, mbDoIsp1);
	}

	/* ??????ISP2 Database???????????? */
	private void Isp2DbCountIncrease(int nDirectAdd) {
		if(nDirectAdd==0)
			mnIsp2DbCount++;
		else mnIsp2DbCount += nDirectAdd;
		if(mnIsp2DbCount >= Integer.MAX_VALUE)
			mnIsp2DbCount = mnEnrollNums+1;

//		Log.d(TAG, "mnIsp2DbCount:"+ mnIsp2DbCount);
	}

	/* ??????ISP2 Database */
	private void UpdateIsp2DB() {
		SharedPreferences.Editor editor = mSharedPref.edit();
		editor.putInt("isp2_db_update_count", mnIsp2DbCount);
		editor.commit();
//		Log.d(TAG, "Isp2 Database Update Count:"+ mnIsp2DbCount);

		Utils.SaveToFile(ByteBuffer.wrap(mIsp2Db), mSaveRoot, "/ISBN.db");
	}

	private Bitmap RawToBmp(byte[] buffer, int nSrcW, int nSrcH, int nShowAreaW, int nShowAreaH) {
		Bitmap bmp = Utils.getFromByte(buffer, nSrcW, nSrcH, nShowAreaW , nShowAreaH , false);
		return bmp;
	}

	private void SaveImg(byte[] imgorg, byte[] imgisp, byte[] imgisp2, int nDataBits, String filename, int nMode, boolean bIsPass) {
		String OrgFilePath = null;
		String Isp1FilePath = null;
		String Isp2FilePath = null;

		switch(nMode) {
			case STATUS_LIVE:
				OrgFilePath = mSaveRoot + "/live_image/" + filename + "_org";
				Isp1FilePath = mSaveRoot + "/live_image/" + filename + "_isp1";
				Isp2FilePath = mSaveRoot + "/live_image/" + filename + "_isp2";
				break;
			case STATUS_ENROLL:
				OrgFilePath = mSaveRoot + "/enroll_image/" + filename + "_org";
				Isp1FilePath = mSaveRoot + "/enroll_image/" + filename + "_isp1";
				Isp2FilePath = mSaveRoot + "/enroll_image/" + filename + "_isp2";
				break;
			case STATUS_AUTH:
				OrgFilePath = mSaveRoot + "/auth_image/" + filename + "_org";
				Isp1FilePath = mSaveRoot + "/auth_image/" + filename + "_isp1";
				Isp2FilePath = mSaveRoot + "/auth_image/" + filename + "_isp2";
				break;
		}

		// Save Org Image
		if(mbSaveOriginalImage) {
			if(imgorg != null) {
				Utils.SaveImage(imgorg, (short)mnImgWidth, (short)mnImgHeight, nDataBits, OrgFilePath + ".raw", Utils.FORMAT_RAW);
				// ??????12bit??????, ??????????????????bmp???.
				if(mbSaveBmpImage && (nDataBits <= 8)) {
					Utils.SaveImage(imgorg, (short)mnImgWidth, (short)mnImgHeight, nDataBits, OrgFilePath + ".bmp", Utils.FORMAT_BMP);
				}
			}
		}

		// Save Isp1 Image
		if(mbDoIsp1) {
			if(mbSaveIsp1Image) {
				if(imgisp != null) {
					Utils.SaveImage(imgisp, (short)mnIspResultW, (short)mnIspResultH, 8, Isp1FilePath + ".raw", Utils.FORMAT_RAW);
					if(mbSaveBmpImage) {
						Utils.SaveImage(imgisp, (short)mnIspResultW, (short)mnIspResultH, 8, Isp1FilePath + ".bmp", Utils.FORMAT_BMP);
					}
				}
			}

			// Save Isp2 Image
			if(mbDoIsp2) {
				if(imgisp2 != null) {
					byte[] imgdata = imgisp2.clone();

					Utils.SaveImage(imgdata, (short)mnIspResultW, (short)mnIspResultH, 8, Isp2FilePath + ".raw", Utils.FORMAT_RAW);
					if(mbSaveBmpImage) {
						Utils.SaveImage(imgdata, (short)mnIspResultW, (short)mnIspResultH, 8, Isp2FilePath + ".bmp", Utils.FORMAT_BMP);
					}
				}
			}
		}
	}

	private void InitialCSV() {
		String file = mSaveRoot + "/log.csv";
		File f = new File(file);
		if(!f.exists()) {
			try	{
				FileWriter fw = new FileWriter(file, true);
				PrintWriter pw = new PrintWriter(fw, true);
				pw.println("|        Time         |   finger   | result |");
				pw.close();
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}

	private void WriteCSV(int nRet) {
		if(!mbGetImgFromStorage) {
			return;
		}
		String timeString = GetDateTimeString();
//		Log.d(TAG, "Time: " + timeString);
		String id = String.format("%04d_%d_%03d", mnReadVerifyPersonIdx, mnReadVerifyFingerIdx, mnReadVerifyCaptureIdx - 1);
		String file = mSaveRoot + "/log.csv";

		try	{
			FileWriter fw = new FileWriter(file, true);
			PrintWriter pw = new PrintWriter(fw, true);

			pw.print("| " + timeString);
			pw.print(" | " + id);
			if(nRet > 0) {
				pw.println(" | " + mIDList.get(nRet) + " |");
			}
			if(nRet == 0) {
				pw.println(" | unknow |");
			}
			pw.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void CleanFingerTemplate() {
		if(mnTemplateNum == 0) {
			ShowMsg("No Template!!", true);
			return;
		}

		for(int i = 0; i < mnTemplateNum; i++) {
			if(mSensor.native_Remove(mTemplateFID.get(i))!= 0) {
				Log.e(TAG, "Remove fid: " + mTemplateFID.get(i) + " fail");
			}
		}

		ShowMsg("Remove " + mnTemplateNum+ " Template", false);

		mnTemplateNum = 0;
		mTemplateFID.clear();
	}

	/* ??????Sensor module info???register */
	private void InitSensorRegister() {
		SetSensorPixelByteMode();
		int nSelProfile = Integer.parseInt(mSharedPref.getString("setting_image_RegProfile", "1"));

		switch(nSelProfile) {
			case 1:
				if(new File(String.format("%s/register.rcf", Environment.getExternalStorageDirectory().toString())).exists() == true)
					SetRegFromFile(String.format("%s/register.rcf", Environment.getExternalStorageDirectory().toString()),false);
				else if(new File(String.format("%s/register.rcfx", Environment.getExternalStorageDirectory().toString())).exists() == true)
					SetRegFromFile(String.format("%s/register.rcfx", Environment.getExternalStorageDirectory().toString()),false);
				else
				if(mbIsUsbMode) {
//					ShowMsg("No Rcf File!!!", true);
				}
				break;
			case 0:
			default:
				break;
		}
	}

	private void SetRegFromFile(String rcffile, boolean bFromAssets) {
		RegTable.SetRegToSensor(rcffile, bFromAssets, onSensorRegisterCallback, this);
	}

	private RegTable.IRegisterObserver onSensorRegisterCallback = new RegTable.IRegisterObserver() {
		@Override
		public int onRegisterSet(int nRegAddr, int nRegVal) {
			if(mSensor == null)
				return -1;

			mSensor.SetReg(nRegAddr, nRegVal);
			return 0;
		}
	};

	/* ??????Sensor module info */
	private void SetSensorPixelByteMode() {
		Log.d(TAG, "Set Sensor Module Info");
		int nSensorType = Device.GINGYDEV_UDNZ011_SPI;
		int nSensorBits = Device.GINGYDEV_INFO_12BIT;

		mSensor.SetModuleInfo(nSensorType, nSensorBits);
	}


	public void openOtgSetting() {
		Intent intent = new Intent();
		ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$OtgSettingsActivity");
		intent.setComponent(comp);
		try {
			startActivityForResult(intent, 0);
		} catch (Exception e) {

		}
	}

	private IUsbEventObserver onUsbEventCallback = new IUsbEventObserver() {
		@Override
		public int onEvent(int nEvent, int nVar) {
			Log.d(TAG, String.format("Usb Callback Event:%d var:%d", nEvent, nVar));
			if(nEvent == 1) {
				// Usb Attached Event
				if(nVar == DEVICE_CONNECT) {
					// sensor??????????????????????????????rcf???.
					InitSensorRegister();
				}
			} else if(nEvent == 2) {
				// Usb Detached Event
				StopButtonThread();
			}

			return 0;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem bedMenuItem = menu.findItem(R.id.action_version);
		try {
			String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			bedMenuItem.setTitle("Version: "+getString(R.string.app_name)+" ("+versionName+")");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		mMenu = menu;
		menu.findItem(R.id.action_settings).setVisible(true);
		menu.findItem(R.id.action_makebg).setVisible(true);
		mMenu.findItem(R.id.action_deltemplate).setVisible(true);
		menu.findItem(R.id.action_isp2dbclean).setVisible(true);
		menu.findItem(R.id.action_otgenable).setVisible(true);
		menu.findItem(R.id.action_about).setVisible(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.action_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			case R.id.action_deltemplate:
				CleanFingerTemplate();
				return true;
			case R.id.action_makebg:
				StopButtonThread();
				if(mbGetImgFromStorage == false) {
					MakeBG(!mbmakebgLoop);
				}
				return true;
			case R.id.action_isp2dbclean:
			{
				StopButtonThread();
				File f = new File(String.format("%s/ISBN.db", mSaveRoot));
				f.delete();

				IspInit(true);

				ShowMsg("Clean Isp2 DB", false);
			}
				return true;
			case R.id.action_otgenable:
				openOtgSetting();
				return true;
			case R.id.action_about:
				if(mbShowVer == false){
					try {
						final String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mtvVerInfo.setText("ImageAnalysis version: " + versionName + "\n" + mSensor.native_getIspVer() +"\ngingyusb.jar " + mSensor.GetGingyUsbInfo() + "\nSensorFW version: " + mSensor.GetFwVer());
							}
						});
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
					mbShowVer = true;
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mtvVerInfo.setText("");
						}
					});
					mbShowVer = false;
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void HideBar() {
		mfa.getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
						View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
						View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
						View.SYSTEM_UI_FLAG_FULLSCREEN|
						View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
		);
	}

	private String GetDateTimeString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Calendar calendar = Calendar.getInstance();
		return  sdf.format(calendar.getTime());
	}

	/* ???setting?????????????????????, ?????????????????????????????????. */
	private OnSharedPreferenceChangeListener onSettingChangeLister = new OnSharedPreferenceChangeListener(){
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if(key.compareTo("setting_read_storage_image") == 0) {
				mbGetImgFromStorage = mSharedPref.getBoolean("setting_read_storage_image", false);
				return;
			}

			if((key.compareTo("setting_image_width") == 0) || (key.compareTo("setting_image_height") == 0)) {
				mnImgWidth = Integer.parseInt(mSharedPref.getString("setting_image_width", "200"));
				mnImgHeight = Integer.parseInt(mSharedPref.getString("setting_image_height", "200"));
				mnIspResultW = mnImgWidth;
				mnIspResultH = mnImgHeight;

				// ???????????????????????????????????????,
				File f = new File(mSaveRoot + "/ISBN.db");
				f.delete();

				IspInit(true);
				if(mSensor.native_algorInit(mSaveRoot, mnIspResultH, mnIspResultH) < 0) {
					ShowMsg("Algorithm initial fail!", true);
				}

				f = new File(mSaveRoot+"/BG");
				f.delete();
				mBg_Normal = null;

				CleanFingerTemplate();
				mSensor.native_RemoveMobile();
				InitSensorRegister();

				return;
			}

			if((key.compareTo("setting_image_sensorposition_left") == 0) || (key.compareTo("setting_image_sensorposition_top") == 0)
					|| (key.compareTo("setting_image_sensorposition_rotate")==0)){
				SetSensingAreaPos();
				return;
			}

			// ???????????????????????????????????????, ??????????????????
			if(key.compareTo("setting_image_sensingarea_color_r")             == 0 ||
			   key.compareTo("setting_image_sensingarea_color_g")             == 0 ||
			   key.compareTo("setting_image_sensingarea_color_b")             == 0 ||
			   key.compareTo("setting_image_sensingarea_size")                == 0 ||
			   key.compareTo("setting_image_aimingpoint")                     == 0 ||
			   key.compareTo("setting_image_pressarea_adjust")                == 0 ||
			   key.compareTo("setting_image_drawline_width")                  == 0 ||
			   key.compareTo("setting_image_rectangle")                       == 0 ||
			   key.compareTo("setting_image_sensingarea_width")               == 0 ||
			   key.compareTo("setting_image_sensingarea_height")              == 0
			   ) {
				mnSensingAreaSize = Integer.parseInt(mSharedPref.getString("setting_image_sensingarea_size", "12"));
				if(mnSensingAreaSize <= 0) {
					SharedPreferences.Editor editor = mSharedPref.edit();
					editor.putString("setting_image_sensingarea_size", "12")
					      .commit();
					mnSensingAreaSize = 12;
				}

				mnSensingColorR = Integer.parseInt(mSharedPref.getString("setting_image_sensingarea_color_r", "255"))&0xFF;
				mnSensingColorG = Integer.parseInt(mSharedPref.getString("setting_image_sensingarea_color_g", "255"))&0xFF;
				mnSensingColorB = Integer.parseInt(mSharedPref.getString("setting_image_sensingarea_color_b", "255"))&0xFF;
				mbRectSensingArea = mSharedPref.getBoolean("setting_image_rectangle", false);
				mbAimingPoing = mSharedPref.getBoolean("setting_image_aimingpoint", false);
				// ???aimingpoint?????????,??????????????????????????????.
				if(key.compareTo("setting_image_aimingpoint") == 0){
					if(mbAimingPoing) {
						mSharedPref.edit()
							.putBoolean("setting_image_pressarea_adjust", true)
							.commit();
					}
				}

				mfDrawlineWidth = Float.parseFloat(mSharedPref.getString("setting_image_drawline_width", "1.0"));

				SetSensingAreaSizeAndColor();
				//?????????????????????????????????
				if (mbRectSensingArea) {
					SetSensingAreaPos();
				}
				return;
			}

			if(key.compareTo("setting_image_enrollnum") == 0){
				mnEnrollNums = Integer.parseInt(mSharedPref.getString("setting_image_enrollnum", "20"));
				return;
			}

			if(key.compareTo("setting_image_enrollqt") == 0) {
				mbFingerQualityEnroll = mSharedPref.getBoolean("setting_image_enrollqt", true);
				return;
			}
			if(key.compareTo("setting_image_fingerqualityscore") == 0) {
				mnFingerQualityScore = Integer.parseInt(mSharedPref.getString("setting_image_fingerqualityscore","50"));
				return;
			}
			if(key.compareTo("setting_enroll_overlap_all") == 0) {
				mbOverlapAllEnroll = mSharedPref.getBoolean("setting_enroll_overlap_all", true);
				return;
			}
			if(key.compareTo("setting_enroll_overlapscore_all") == 0) {
				mnOverlapAllScore = Integer.parseInt(mSharedPref.getString("setting_enroll_overlapscore_all", "90"));
				return;
			}
			if(key.compareTo("setting_enroll_overlap_last") == 0) {
				mbOverlapLastEnroll = mSharedPref.getBoolean("setting_enroll_overlap_last", true);
				return;
			}
			if(key.compareTo("setting_enroll_overlapscore_last") == 0) {
				mnOverlapLastScore = Integer.parseInt(mSharedPref.getString("setting_enroll_overlapscore_last", "70"));
				return;
			}

			if((key.compareTo("setting_image_debugtext") == 0)){
				mbEnableDebugText = mSharedPref.getBoolean("setting_image_debugtext", false);

				if(mbEnableDebugText == true) {
					mtvDebug.setText("Debug:On");
					mtvDebug.setVisibility(View.VISIBLE);
				}
				else mtvDebug.setVisibility(View.GONE);

				return;
			}

			if((key.compareTo("setting_root_brightness") == 0)){
				mbHighBrightness = mSharedPref.getBoolean("setting_high_brightness", false);
				return;
			}

			if(key.compareTo("setting_image_saveenrollimg") == 0) {
				mbSaveEnrollImage = mSharedPref.getBoolean("setting_image_saveenrollimg", false);
				String dumppath = mSaveRoot+ "/enroll_image" ;
				MakeDir(dumppath);
				return;
			}

			if(key.compareTo("setting_image_saveauthimg") == 0) {
				mbSaveAuthImage = mSharedPref.getBoolean("setting_image_saveauthimg", false);
				String dumppath = mSaveRoot+ "/auth_image" ;
				MakeDir(dumppath);
				return;
			}

			if(key.compareTo("setting_original_saving") == 0) {
				mbSaveOriginalImage = mSharedPref.getBoolean("setting_original_saving", true);
				return;
			}

			if(key.compareTo("setting_isp1_saving") == 0) {
				mbSaveIsp1Image = mSharedPref.getBoolean("setting_isp1_saving", false);
				return;
			}

			if(key.compareTo("setting_bmp_saving") == 0) {
				mbSaveBmpImage = mSharedPref.getBoolean("setting_bmp_saving", true);
				return;
			}

			if(key.compareTo("setting_image_do_isp1") == 0) {
				mbDoIsp1 = mSharedPref.getBoolean("setting_image_do_isp1", false);
				return;
			}

			if(key.compareTo("setting_image_do_isp2") == 0) {
				mbDoIsp2 = mSharedPref.getBoolean("setting_image_do_isp2", false);
				return;
			}
		}
	};
}
