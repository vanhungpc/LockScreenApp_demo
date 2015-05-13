package com.lockscreen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

@SuppressLint("InflateParams")
public class LockScreenActivity extends Activity implements AnimationListener {

	private static final String TAG = "LockScreenActivity";
	boolean inDragMode;

	int windowwidth;
	int windowheight;
	ImageView home;
	Dialog dialog;
	// int phone_x,phone_y;
	int home_x, home_y;
	int[] droidpos;
	DrawingView dv;
	LinearLayout show, draw;
	private Paint mPaint;

	ImageView imgLook, imgFinish, imgCancel, imgChooseFile;

	private LayoutParams layoutParams;
	ArrayList<String> arrData;
	TextView txtCurrentDay;
	ArrayList<Coordinates> arr;
	private static final int REQUEST_PICK_FILE = 1;
	String filePath = "";
	private Path mPath;
	MediaPlayer mp;
	Animation animationFadeIn, animationFadeOut;
	View wd;
	// @Override
	// public void onAttachedToWindow() {
	// this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);//
	// Android4.0Ò»ÏÂÆÁ±ÎHome¼ü
	// super.onAttachedToWindow();
	// }

	/*
	 * @Override protected void onNewIntent(Intent intent) { // TODO
	 * Auto-generated method stub super.onNewIntent(intent);
	 * getWindow().addFlags
	 * (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager
	 * .LayoutParams
	 * .FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_FULLSCREEN);
	 * 
	 * }
	 */
	List<Point> points;
	private File sdcardObj = new File(Environment.getExternalStorageDirectory()
			.getAbsolutePath());
	private ArrayList<String> filelist = new ArrayList<String>();

	public static boolean isStarted = false;// ÅÐ¶ÏËøÆÁÒ³ÃæÊÇ·ñ´ò¿ª×´Ì¬

	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);// Android4.0Ò»ÏÂÆÁ±ÎHome¼ü
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onAttachedToWindow();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		arrData = new ArrayList<String>();
		arr = new ArrayList<Coordinates>();
		points = new ArrayList<Point>();
		super.onCreate(savedInstanceState);
		// animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
		// animationFadeOut = AnimationUtils.loadAnimation(this,
		// R.anim.fadeout);
		// this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED
		// ,FLAG_HOMEKEY_DISPATCHED);// ÆÁ±Î4.0+ home¼ü
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		setContentView(R.layout.main);
		txtCurrentDay = (TextView) findViewById(R.id.txtCurrentDay);

		Date curDate = new Date();
		SimpleDateFormat format = new SimpleDateFormat("E, MMMM dd");
		String DateToStr = format.format(curDate);
		txtCurrentDay.setText(DateToStr);

		if (getIntent() != null && getIntent().hasExtra("kill")
				&& getIntent().getExtras().getInt("kill") == 1) {
			finish();
		}
		isStarted = true;
		try {
			// initialize receiver
			startService(new Intent(this, LockScreenService.class));
			StateListener phoneStateListener = new StateListener();
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE);

			windowwidth = getWindowManager().getDefaultDisplay().getWidth();
			System.out.println("windowwidth" + windowwidth);
			windowheight = getWindowManager().getDefaultDisplay().getHeight();
			System.out.println("windowheight" + windowheight);

			home = (ImageView) findViewById(R.id.home);

			MarginLayoutParams marginParams1 = new MarginLayoutParams(
					home.getLayoutParams());

			marginParams1.setMargins((windowwidth / 24) * 10, 0,
					(windowheight / 32) * 8, 0);
			// marginParams1.setMargins(((windowwidth-home.getWidth())/2),0,(windowheight/32)*10,0);
			LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
					marginParams1);

			home.setLayoutParams(layout);

		} catch (Exception e) {
			// TODO: handle exception
		}
		show = (LinearLayout) findViewById(R.id.show);
		imgLook = (ImageView) findViewById(R.id.imgLook);
		imgLook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mp = MediaPlayer.create(LockScreenActivity.this, R.raw.emerge);

				if (mp.isPlaying()) {
					mp.pause();
				} else {
					mp.start();
				}
				mp.setOnCompletionListener(new OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
						mp.release();
					};
				});

				DisplayMetrics dm = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(dm);
				int width = dm.widthPixels;
				int height = dm.heightPixels;
				if (width > height) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				} else {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
				// return false;

				// SimpleDialogDown();
				// ScaleAnimation animation = new ScaleAnimation((float) 0.0,
				// (float) 1.0, (float) 0.0, (float) 1.0, (float) 0.5,
				// (float) 0.85);
				// animation.setDuration(500);
				// animation.setStartOffset(500);
				// // dialog.getLayoutInflater().inflate(resource, root).
				// wd = dialog.findViewById(R.id.layout);
				// wd.setAnimation(animation);

				dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationScale;
				dialog.show();

			}

		});
		dv = new DrawingView(this);
		dialog = new Dialog(this);

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.canvas_dialog);

		draw = (LinearLayout) dialog.findViewById(R.id.draw);

		draw.addView(dv);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.GREEN);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(12);

		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
		DisplayMetrics dimension = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dimension);
		int w = dimension.widthPixels;
		int h = dimension.heightPixels;
		lp.x = 5; // The new position of the X coordinates
		lp.y = 5; // The new position of the Y coordinates
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		if (width > height) {
			lp.width = w - 10; // Width
			lp.height = h; // Height
		} else {
			lp.width = w - 10; // Width
			lp.height = 6 * h / 7; // Height
		}

		// lp.alpha = 0.7f; // Transparency
		dialogWindow.setAttributes(lp);

		imgFinish = (ImageView) dialog.findViewById(R.id.imgFinish);
		imgCancel = (ImageView) dialog.findViewById(R.id.imgCancel);
		imgFinish.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MediaPlayer mp1 = MediaPlayer.create(LockScreenActivity.this,
						R.raw.gulp);
				//
				// dialog.dismiss();
				mp1.start();
				mp1.setOnCompletionListener(new OnCompletionListener() {
					public void onCompletion(MediaPlayer mpb) {
						mpb.release();

					};
				});
				if (arrData.size() > 0) {
					// dialog.getWindow().getAttributes().windowAnimations =
					// R.style.DialogAnimationScalef;
					// overridePendingTransition(R.anim.scaleanimationf,
					// R.anim.scaleanimationf);

					// fadeOutHUD(findViewById());
					saveFile();
					Intent intent = new Intent(LockScreenActivity.this,
							MainFinish.class);
					startActivity(intent);
					finish();
					dialog.dismiss();
					overridePendingTransition(R.anim.scaleanimationf,
							R.anim.scaleanimationf);

					// dialog.getWindow().getAttributes().windowAnimations =
					// R.style.DialogAnimationScalef;
					// dialog.show();
					// if (dialog != null) {
					// if (dialog.isShowing()) {
					// startTimer(500);
					// // dialog.dismiss();
					//
					// }
					//
					// }
					// finish();
					// dialog.getWindow().getAttributes().windowAnimations =
					// R.style.DialogAnimationScalef;
					// overridePendingTransition(R.anim.out_down,
					// R.anim.out_up);

					// saveFile();
					//
				}

			}
		});
		imgCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				arrData = new ArrayList<String>();

				draw.removeAllViews();
				dv.invalidate();
				dv = new DrawingView(LockScreenActivity.this);
				draw.addView(dv);

			}
		});

		dialog.setOnKeyListener(new Dialog.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface arg0, int keyCode,
					KeyEvent event) {
				// DisplayMetrics dm = new DisplayMetrics();
				// getWindowManager().getDefaultDisplay().getMetrics(dm);
				// int width = dm.widthPixels;
				// int height = dm.heightPixels;
				// if(width > height){
				// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				// }else{
				// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				// }
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					// ActivityInfo.SCREEN_ORIENTATION_USER
					dialog.dismiss();
				}

				if (keyCode == KeyEvent.KEYCODE_HOME) {
					Toast.makeText(LockScreenActivity.this, "123",
							Toast.LENGTH_LONG).show();
					return false;
				}
				return true;
			}
		});
		imgChooseFile = (ImageView) dialog.findViewById(R.id.imgChooseFile);
		imgChooseFile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LockScreenActivity.this,
						FilePickerActivity.class);
				Bundle data = new Bundle();
				DisplayMetrics dm = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(dm);
				int width = dm.widthPixels;
				int height = dm.heightPixels;
				if (width > height) {
					data.putString("keyrotate", "land");
				} else {
					data.putString("keyrotate", "port");
				}
				intent.putExtras(data);
				startActivityForResult(intent, REQUEST_PICK_FILE);

			}
		});

	}

	private void startTimer(long time) {
		CountDownTimer counter = new CountDownTimer(300, 100) {
			public void onTick(long millisUntilDone) {

				// Log.d("counter_label", "Counter text should be changed");
				// tv.setText("You have " + millisUntilDone + "ms");
			}

			public void onFinish() {
				finish();

			}
		}.start();
	}

	public void fadeOutHUD(View view) {
		AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setDuration(800);
		view.setAnimation(fadeOut);
		view.startAnimation(fadeOut);
		fadeOut.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationScale;
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationScalef;
				dialog.dismiss();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
	}

	public void onSlideTouch(View view, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			int x_cord = (int) event.getRawX();
			int y_cord = (int) event.getRawY();

			if (x_cord > windowwidth) {
				x_cord = windowwidth;
			}
			if (y_cord > windowheight) {
				y_cord = windowheight;
			}

			layoutParams.leftMargin = x_cord - 25;
			layoutParams.topMargin = y_cord - 75;

			view.setLayoutParams(layoutParams);
			break;
		default:
			break;

		}

		// When the user pushes down on an ImageView
		/*
		 * if ( event.getAction() == MotionEvent.ACTION_DOWN ) { inDragMode =
		 * true; //Set a variable so we know we started draggin the imageView
		 * //Set the selected ImageView X and Y exact position
		 * selectedImageViewX =
		 * Math.abs((int)event.getRawX()-((ImageView)view).getLeft());
		 * selectedImageViewY =
		 * Math.abs((int)event.getRawY()-((ImageView)view).getTop()); //Bring
		 * the imageView in front ((ImageView)view).bringToFront(); }
		 * 
		 * //When the user let's the ImageView go (raises finger) if (
		 * event.getAction() == MotionEvent.ACTION_UP ) { inDragMode = false;
		 * //Reset the variable which let's us know we're not in drag mode
		 * anymore }
		 * 
		 * //When the user keeps his finger on te screen and drags it (slides
		 * it) if ( event.getAction() == MotionEvent.ACTION_MOVE ) { //If we've
		 * started draggin the imageView if ( inDragMode ) { //Get the imageView
		 * object // ImageView slide = (ImageView)findViewById(R.id.slide);
		 * //Get a parameters object (THIS EXAMPLE IS FOR A RELATIVE LAYOUT)
		 * RelativeLayout.LayoutParams params =
		 * (RelativeLayout.LayoutParams)view.getLayoutParams(); //Change the
		 * position of the imageview accordingly
		 * params.setMargins((int)event.getRawX()-selectedImageViewX,
		 * (int)event.getRawY()-selectedImageViewY, 0, 0); //Set the new params
		 * view.setLayoutParams(params);
		 * 
		 * //If we hit a limit with our imageView position
		 * /*if((int)event.getRawX()) { //Open another activity Intent it = new
		 * Intent(Slide.this,NextActivity.class); startActivity(it); } } }
		 */

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (Intent.ACTION_MAIN.equals(intent.getAction())) {
			Log.i("MyLauncher", "onNewIntent: HOME Key");

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

		if ((keyCode == KeyEvent.KEYCODE_HOME)) {
			// Key code constant: Home key. This key is handled by the framework
			// and is never delivered to applications.
			Log.e(TAG, "ÏìÓ¦Home¼ü");
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		Log.e(TAG, "ÏìÓ¦Back¼ü");
		return;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Õð¶¯
	 */
	private void virbate() {
		Vibrator vibrator = (Vibrator) this
				.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(200);
	}

	public class DrawingView extends View {

		// public int width;
		// public int height;
		private Bitmap mBitmap;
		private Canvas mCanvas;

		private Paint mBitmapPaint;
		Context context;
		private Paint circlePaint;
		private Path circlePath;
		String coordinate = "";
		String coordinate1 = "";

		public DrawingView(Context c) {
			super(c);
			context = c;
			mPath = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
			circlePaint = new Paint();
			circlePath = new Path();
			circlePaint.setAntiAlias(true);
			circlePaint.setColor(Color.BLUE);
			circlePaint.setStyle(Paint.Style.STROKE);
			circlePaint.setStrokeJoin(Paint.Join.MITER);
			circlePaint.setStrokeWidth(4f);

		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);

			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);

		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

			canvas.drawPath(mPath, mPaint);

			canvas.drawPath(circlePath, circlePaint);
		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 4;

		private void touch_start(float x, float y) {
			// mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			// Log.e("toa do ", ("(" + x + "," + y + ")"));
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;

				circlePath.reset();
				circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			circlePath.reset();
			// commit the path to our offscreen
			mCanvas.drawPath(mPath, mPaint);
			// kill this so we don't double draw
			mPath.reset();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			// System.out.println("toa do ( " + x + "," + y + ")");
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				coordinate = "\n" + x + "," + y + "\n";

				arrData.add(coordinate);
				touch_start(x, y);

				invalidate();

				break;
			case MotionEvent.ACTION_MOVE:
				coordinate1 = x + "," + y + "\n";
				arrData.add(coordinate1);
				touch_move(x, y);
				invalidate();

				break;
			case MotionEvent.ACTION_UP:
				coordinate = "";
				coordinate1 = "";
				touch_up();
				invalidate();
				break;
			}
			return true;
		}
	}

	public String getUdid() {
		final TelephonyManager tm = (TelephonyManager) getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = ""
				+ android.provider.Settings.Secure.getString(
						getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
		UUID deviceUuid = new UUID(androidId.hashCode(),
				((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
		String deviceId = deviceUuid.toString();
		return deviceId;
	}

	public void saveFile() {

		// File dir = getFilesDir();yyyyMMdd_HH_mm_ss
		SimpleDateFormat sdf = new SimpleDateFormat("ddmmyy-hhmm");
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		String nameFile = "";
		// String currentDateandTime = sdf.format(new Date()) + "-" + getUdid()
		// + ".txt";
		File dir;
		if (width > height) {
			nameFile = "Land_" + sdf.format(new Date()) + "-" + getUdid()
					+ ".txt";
			File sdCard = Environment.getExternalStorageDirectory();
			dir = new File(sdCard.getAbsolutePath() + "/baby_insight/Land");
			dir.mkdirs();
		} else {
			nameFile = "Port_" + sdf.format(new Date()) + "-" + getUdid()
					+ ".txt";
			File sdCard = Environment.getExternalStorageDirectory();
			dir = new File(sdCard.getAbsolutePath() + "/baby_insight/Port");
			dir.mkdirs();
		}

		File file = new File(dir, nameFile);
		// file.delete();
		try {

			OutputStream os = new FileOutputStream(file);
			BufferedWriter write = new BufferedWriter(
					new OutputStreamWriter(os));

			for (String coor : arrData) {
				write.write(coor);
			}
			write.flush();
			write.close();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readFile() {
		try {
			File sdCard = Environment.getExternalStorageDirectory();
			File myFile = new File(filePath);
			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fIn));
			String str = "";
			StringBuffer buffer = new StringBuffer();
			str = reader.readLine();
			boolean flag = false;
			while (str != null) {
				Coordinates coor = new Coordinates();
				if (str.equalsIgnoreCase("")) {
					flag = true;
				} else {
					if (flag == true) {
						String strNew[] = str.split(",");
						coor.setStartx(Float.parseFloat(strNew[0]));
						coor.setStarty(Float.parseFloat(strNew[1]));
						flag = false;
					} else {
						String strNew[] = str.split(",");
						coor.setX(Float.parseFloat(strNew[0]));
						coor.setY(Float.parseFloat(strNew[1]));
					}
				}

				buffer.append(str + "\n");
				if (!str.equalsIgnoreCase("")) {
					arr.add(coor);
				}

				str = reader.readLine();
			}
			reader.close();
			fIn.close();

			// userName = arr.get(0);
			// passWork = arr.get(1);

			// String b = question1 + "\n" + question2 + "\n" + question3 + "\n"
			// + question4 + "\n" + "Android version: " + deviceVersion;
			//
			// new SendMailTask1(Survey.this).execute(userName, passWork,
			// toEmail,
			// subJect, b);
			// txtInput.setText(buffer.toStrbing());

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_PICK_FILE:
				if (data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
					// Get the file path
					File f = new File(
							data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));

					// Set the file path text view
					filePath = f.getPath();
					// if(filePath.contains("Land_")){
					// Toast.makeText(LockScreenAppActivity.this, "Land",
					// Toast.LENGTH_LONG).show();
					// }else if(filePath.contains("Port_")){
					// Toast.makeText(LockScreenAppActivity.this, "Port",
					// Toast.LENGTH_LONG).show();
					// }

					String coordinate = "";
					String coordinate1 = "";
					if (!filePath.equalsIgnoreCase("")) {
						arrData = new ArrayList<String>();
						arr = new ArrayList<Coordinates>();
						draw.removeAllViews();
						dv.invalidate();
						dv = new DrawingView(LockScreenActivity.this);
						draw.addView(dv);
						mPath.reset();
						readFile();
						if (arr.size() > 0) {
							for (Coordinates coor : arr) {
								if (coor.getStartx() != 0.0) {
									dv.touch_start(coor.getStartx(),
											coor.getStarty());
									coordinate = "\n" + coor.getStartx() + ","
											+ coor.getStarty() + "\n";

									arrData.add(coordinate);
								} else {
									dv.touch_move(coor.getX(), coor.getY());
									coordinate1 = coor.getX() + ","
											+ coor.getY() + "\n";
									arrData.add(coordinate1);
								}
							}
						}

					}

					// mFilePathTextView.setText(f.getPath());
				}
			}
		}
	}

	class StateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				System.out.println("call Activity off hook");
				finish();

				break;
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			}
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		dialog.dismiss();

	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	};

}