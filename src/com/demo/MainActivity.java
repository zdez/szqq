package com.demo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	private static final boolean DEBUG = true;
	
	private View mMoreLayout;
	private ListView mListView;
	
	//
	private View mBtnCamara;
	private View mBtnRecord;
	private View mBtnFile;
	//
	private ImageView mCameraPreview;
	//
	private String mCurrentPhotoPath;
	
	private static final int ACTION_CODE_TAKE_PICTURE = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_main);
		
		mMoreLayout = findViewById(R.id.more_layout);
		findViewById(R.id.more).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mMoreLayout.getVisibility() == View.VISIBLE){
					mMoreLayout.setVisibility(View.GONE);
				}else{
					mMoreLayout.setVisibility(View.VISIBLE);
				}
			}
		});
		//设置照相机
		mBtnCamara = findViewById(R.id.camara);
		if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			mBtnCamara.setVisibility(View.GONE);
		}else{
			mBtnCamara.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					try {
						Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						File f = createImageFile();
						mCurrentPhotoPath = f.getAbsolutePath();
						if(DEBUG)Log.i(TAG ,"mCurrentPhotoPath is "+mCurrentPhotoPath);
						i.putExtra(MediaStore.EXTRA_OUTPUT ,Uri.fromFile(f));
						startActivityForResult(i ,ACTION_CODE_TAKE_PICTURE);
					} catch (IOException e) {
						if(DEBUG)e.printStackTrace();
						Toast.makeText(MainActivity.this ,"can not take picture" ,Toast.LENGTH_SHORT).show();
					}
					
				}
			});
		}
		
		mBtnRecord = findViewById(R.id.record);
		mBtnFile = findViewById(R.id.file);
		
//		mCameraPreview = (ImageView) findViewById(R.id.camera_preview);
		
		mListView = (ListView) findViewById(R.id.listview);
		mListView.setAdapter(new MyAdapter(this));
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == ACTION_CODE_TAKE_PICTURE){
			if(data != null){
				Bundle b = data.getExtras();
				Bitmap bm = (Bitmap) b.get("data");
				mCameraPreview.setImageBitmap(bm);	
			}else{
				setPic();
			}
			
		}
	}
	
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	private File createImageFile() throws IOException {
		File storageDir = new File(
				Environment.getExternalStorageDirectory(), 
			    "demo");
		if(!storageDir.exists()){
			if(!storageDir.mkdir()){
				Log.i(TAG ,"create dir failed . dir is "+storageDir.getAbsolutePath());
			}
		}
	    // Create an image file name
	    String timeStamp = 
	        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName =  timeStamp + "_";
	    File image = File.createTempFile(
	        imageFileName, 
	        JPEG_FILE_SUFFIX, 
	        storageDir
	    );
	    return image;
	}
	
	private void setPic() {
	    // Get the dimensions of the View
	    int targetW = mCameraPreview.getWidth();
	    int targetH = mCameraPreview.getHeight();
	  
	    // Get the dimensions of the bitmap
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
	    int photoW = bmOptions.outWidth;
	    int photoH = bmOptions.outHeight;
	  
	    // Determine how much to scale down the image
	    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
	  
	    // Decode the image file into a Bitmap sized to fill the View
	    bmOptions.inJustDecodeBounds = false;
	    bmOptions.inSampleSize = scaleFactor;
	    bmOptions.inPurgeable = true;
	  
	    Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
	    mCameraPreview.setImageBitmap(bitmap);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private static class MyAdapter extends BaseAdapter {
		
		class Holder{
			public TextView text;
			public ImageView image;
		}
		
		private Context mContext;
		
		MyAdapter(Context context){
			mContext = context;
		}

		@Override
		public int getCount() {
			return 15;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if(convertView == null){
				holder = new Holder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item_history ,parent ,false);
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.image = (ImageView) convertView.findViewById(R.id.image);
				convertView.setTag(holder);
			}else{
				holder = (Holder) convertView.getTag();
			}
			holder.text.setText("text positon" + position);
			return convertView;
		}
		
	}

}
