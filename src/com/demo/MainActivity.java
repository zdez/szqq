package com.demo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	private static final boolean DEBUG = true;
	
	private static final String CACHE_FILE_DIR = "demo";
	
	private View mMoreLayout;
	private ListView mListView;
	private MyAdapter mAdapter;
	//
	private View mBtnMore;
	private View mBtnCamara;
	private View mBtnRecord;
	private View mBtnFile;
	private View mBtnSend;
	private View mBtnCancel;
	//
	//
	private ImageView mCameraPreview;
	private TextView mFilePreview;
	private View mRecordLayout;
	private EditText mInputText;
	//
	private String mCurrentPhotoPath;
	//
	private MediaPlayer mMediaPlayer;
	private MediaRecorder mMediaRecorder;
	private boolean mIsRecording;
	
	//
	private Content mContent;
	//
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	
	
	private static final int ACTION_CODE_TAKE_PICTURE = 1000;
	private static final int ACTION_CODE_BROWSE_FILE = 1001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_main);
		
		mContent = new Content();
		
		mMoreLayout = findViewById(R.id.more_layout);
		mBtnMore = findViewById(R.id.more);
		mBtnMore.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mMoreLayout.getVisibility() == View.VISIBLE){
					mMoreLayout.setVisibility(View.GONE);
				}else{
					mMoreLayout.setVisibility(View.VISIBLE);
				}
			}
		});
		mRecordLayout = findViewById(R.id.record_layout);
		mRecordLayout.setVisibility(View.GONE);
		mInputText = (EditText) findViewById(R.id.input);
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
						File f = createTmpFile(JPEG_FILE_SUFFIX);
						mCurrentPhotoPath = f.getAbsolutePath();
						if(DEBUG)Log.i(TAG ,"mCurrentPhotoPath is "+mCurrentPhotoPath);
						i.putExtra(MediaStore.EXTRA_OUTPUT ,Uri.fromFile(f));
						startActivityForResult(i ,ACTION_CODE_TAKE_PICTURE);
						mBtnMore.setEnabled(false);
                        mMoreLayout.setVisibility(View.GONE);
					} catch (IOException e) {
						if(DEBUG)e.printStackTrace();
						Toast.makeText(MainActivity.this ,"can not take picture" ,Toast.LENGTH_SHORT).show();
					}
					
				}
			});
		}
		//
		mBtnRecord = findViewById(R.id.record);
		mBtnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	showRecordingLayout();
            }
        });
		findViewById(R.id.start_recording).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if (mMediaRecorder == null) {
					if(mContent.audio != null){
		        		//删除旧文件
		        		mContent.audio.delete();
		        		mContent.audio = null;
		        	}
					mContent.audio = startRecording();
					if(mContent.audio != null){
						((Button)v).setText(R.string.stop_recording);
					}
				} else {
					stopRecording();
					((Button)v).setText(R.string.start_recording);
				}
			}
		});
		findViewById(R.id.start_playing).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mMediaPlayer != null){
					((Button)v).setText(R.string.start_playing);
					stopPlaying();
				}else{
					((Button)v).setText(R.string.stop_playing);
					startPlaying();
				}
			}
		});
		//发送文件的按钮
		mBtnFile = findViewById(R.id.file);
		mBtnFile.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			    intent.setType("file/*");
			    startActivityForResult(intent, ACTION_CODE_BROWSE_FILE);
			}
		});
		mFilePreview = (TextView) findViewById(R.id.file_preview);
		//
		mCameraPreview = (ImageView) findViewById(R.id.image_preview);
		//发送与取消按钮
		mBtnSend = findViewById(R.id.send);
		mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendContent();
            }
        });
		mBtnCancel = findViewById(R.id.cancel);
		mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetContent();
            }
        });
		
		mAdapter = new MyAdapter(this);
		mListView = (ListView) findViewById(R.id.listview);
		mListView.setAdapter(mAdapter);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == ACTION_CODE_TAKE_PICTURE){
			showCamaraPreview(data);
		}else if(requestCode == ACTION_CODE_BROWSE_FILE){
			showFilePreview(data);
		}
	}
	
	private File createTmpFile(String fileSuffix) throws IOException {
		File storageDir = new File(
				Environment.getExternalStorageDirectory(), 
			    CACHE_FILE_DIR);
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
	        fileSuffix, 
	        storageDir
	    );
	    return image;
	}
	
	private void showRecordingLayout(){
		//
		mBtnMore.setEnabled(false);
        mMoreLayout.setVisibility(View.GONE);
        mInputText.setVisibility(View.INVISIBLE);
        mBtnCancel.setVisibility(View.VISIBLE);
        mRecordLayout.setVisibility(View.VISIBLE);
        //
        
	}
	
	/**
	 * 显示将要发送的图片
	 * @param data
	 */
	private void showCamaraPreview(Intent data){
	    //隐藏"更多"
	    mBtnMore.setEnabled(false);
        mMoreLayout.setVisibility(View.GONE);
        mCameraPreview.setVisibility(View.VISIBLE);
        mInputText.setVisibility(View.INVISIBLE);
        mBtnCancel.setVisibility(View.VISIBLE);
        //
        if(data != null){
            Bundle b = data.getExtras();
            Bitmap bm = (Bitmap) b.get("data");
            mCameraPreview.setImageBitmap(bm);  
        }else{//从文件里面读
        	File image = new File(mCurrentPhotoPath);
        	if(image.exists()){
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
                mContent.image = image;
        	}
        }
       
	}
	
	private void showFilePreview(Intent data){
		if(data != null){
			//隐藏"更多"
		    mBtnMore.setEnabled(false);
	        mMoreLayout.setVisibility(View.GONE);
	        mFilePreview.setVisibility(View.VISIBLE);
	        mInputText.setVisibility(View.INVISIBLE);
	        mBtnCancel.setVisibility(View.VISIBLE);
	        //
        	String path = data.getDataString();
        	if(DEBUG){
        		Log.i(TAG ,"showFilePreview. path is "+path);
        	}
        	mFilePreview.setText(path);
        	mContent.file = new File(path);
		}
	}
	
	private void sendContent(){
	    if(mInputText.getVisibility() == View.VISIBLE){
	        mContent.message = mInputText.getText().toString();    
	    }
	    new SendTask().execute(mContent);
	}
	
	private void resetContent(){
	    if(mContent.image != null){
	        mCameraPreview.setVisibility(View.GONE);
	        mBtnCancel.setVisibility(View.GONE);
	    }else if(mContent.audio != null){
	        mRecordLayout.setVisibility(View.GONE);
	        mBtnCancel.setVisibility(View.GONE);
	    }else if(mContent.file != null){
	        mFilePreview.setVisibility(View.GONE);
	        mBtnCancel.setVisibility(View.GONE);
	    }
	    mBtnMore.setEnabled(true);
	    mInputText.setVisibility(View.VISIBLE);
	    mInputText.setText("");
	    mContent.clear();
	}
	
	class SendTask extends AsyncTask<Content ,Void ,ArrayList<Content>>{

        @Override
        protected void onPostExecute(ArrayList<Content> result) {
            super.onPostExecute(result);
            if(DEBUG){
                Log.i(TAG ,"SendTask.onPostExecute.");
            }
            resetContent();
            if(result.size() != 0){
            	mAdapter.setData(result);
            	mListView.setSelection(mAdapter.getCount() - 1);
            }
        }

        @Override
        protected ArrayList<Content> doInBackground(Content... params) {
            boolean send = false;
            try {
                if(params[0].message != null){
                    Server.send(params[0].message);
                }else if(params[0].image != null){
                    Server.send(params[0].image);
                }else if(params[0].audio != null){
                    Server.send(params[0].audio);
                }else if(params[0].file != null){
                    Server.send(params[0].file);
                }
                send = true;
            } catch (IOException e) {
                if(DEBUG)
                    e.printStackTrace();
                send = false;
            }
            ArrayList<Content> result = null;
            //保存发送记录
            if(Database.saveContent(MainActivity.this ,params[0], send)){
            	result = Database.getContent(MainActivity.this);
            }
            return result;
        }
	    
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
		private ArrayList<Content> mData;
		
		MyAdapter(Context context){
			mContext = context;
		}
		
		public void setData(ArrayList<Content> list){
			mData = list;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mData != null ? mData.size() : 0;
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
			Content content = mData.get(mData.size() - 1 - position);//需要倒过来显示
			if(content.message != null){
				holder.text.setText(content.message);
			}else if(content.image != null){
				holder.text.setText(mContext.getString(R.string.image) + " : "+ content.image.getAbsolutePath());
			}else if(content.audio != null){
				holder.text.setText(mContext.getString(R.string.audio) + " : " + content.audio.getAbsolutePath());
			}else if(content.file != null){
				holder.text.setText(mContext.getString(R.string.file) + " : " + content.file.getAbsolutePath());
			}
			return convertView;
		}
		
	}
	
	private void startPlaying() {
		try {
			if(mMediaRecorder != null){
				//如果正在录音，先将其停止
				stopRecording();
			}
			if(mContent.audio != null){
				 mMediaPlayer = new MediaPlayer();
				 mMediaPlayer.setDataSource(mContent.audio.getAbsolutePath());
				 mMediaPlayer.prepare();
		         mMediaPlayer.start();
			}
        } catch (IOException e) {
            if(DEBUG){
                Log.e(TAG, "prepare() failed");    
            }
            stopPlaying();
        }
    }

    private void stopPlaying() {
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    private File startRecording() {
    	File file = null;
        try {
        	if(mMediaPlayer != null){
        		//如果正在播放录音，先停止播放
        		stopPlaying();
        	}
			file = createTmpFile(".mp4");
			mMediaRecorder = new MediaRecorder();
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mMediaRecorder.setOutputFile(file.getAbsolutePath());
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			mMediaRecorder.prepare();
			
			 mMediaRecorder.start();
        } catch (IOException e) {
            if(DEBUG){
                e.printStackTrace();
            }
            file = null;
            stopRecording();
        }
        return file;
    }

    private void stopRecording() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

}
