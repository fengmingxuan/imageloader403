package com.open.imagedemo;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private final static String TAG = "MainActivity";
	ImageView mImageView;
	String filePath = "https://img.pximg.com/2017/07/d264b5b3ac0169a.jpg!pximg/both/0x0";
	String mFileName;
	private final static String ALBUM_PATH = Environment.getExternalStorageDirectory() + "/download_test/";
	private Bitmap mBitmap;
	private String mSaveMessage;
	private ProgressDialog mSaveDialog = null;
	private Button mBtnSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mImageView = (ImageView) findViewById(R.id.imageview);
		mBtnSave = (Button) findViewById(R.id.btnsave);
		mBtnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mSaveDialog = ProgressDialog.show(MainActivity.this, "保存图片", "图片正在保存中，请稍等...", true);
				new Thread(saveFileRunnable).start();
			}
		});

		new Thread(connectNet).start();
	}

	private Handler connectHanlder = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "display image");
			// 更新UI，显示图片
			if (mBitmap != null) {
				mImageView.setImageBitmap(mBitmap);// display image
			}
		}
	};

	/*
	 * 连接网络 由于在4.0中不允许在主线程中访问网络，所以需要在子线程中访问
	 */
	private Runnable connectNet = new Runnable() {
		@Override
		public void run() {
			try {
				mFileName = "test.jpg";
				// 以下是取得图片的两种方法
				// ////////////// 方法1：取得的是byte数组, 从byte数组生成bitmap
//				byte[] data = getImage(filePath);
//				if (data != null) {
//					mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);// bitmap
//				} else {
//					Toast.makeText(MainActivity.this, "Image error!", 1).show();
//				}
				// //////////////////////////////////////////////////////
				//
				// //******** 方法2：取得的是InputStream，直接从InputStream生成bitmap
				// ***********/
				 mBitmap =
				 BitmapFactory.decodeStream(getImageStream(filePath));
				// //********************************************************************/

				// 发送消息，通知handler在主线程中更新UI
				connectHanlder.sendEmptyMessage(0);
				Log.d(TAG, "set image ...");
			} catch (Exception e) {
				Toast.makeText(MainActivity.this, "无法链接网络！", 1).show();
				e.printStackTrace();
			}

		}

	};

	/**
	 * Get image from newwork
	 * 
	 * @param path
	 *            The path of image
	 * @return byte[]
	 * @throws Exception
	 */
	@SuppressLint("NewApi") public byte[] getImage(String path) throws Exception {
		try {
			URL url = null;
	        try {
	            url = new URL(path);
	        } catch (MalformedURLException e) {
	            Log.e("getStreamFromNetwork", e.getMessage(), e);
	        }
	        HttpURLConnection conn = null;

	        if (path.startsWith("https")) {
	            trustAllHosts();
	            HttpsURLConnection https;
	            
	            https = (HttpsURLConnection) url
	                    .openConnection();
	            https.setHostnameVerifier(DO_NOT_VERIFY);
	            
	            https.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");  
	            https.setRequestProperty("Upgrade-Insecure-Requests", "1");  
	            https.setRequestProperty("Host", "img.pximg.com");
	            https.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.76 Mobile Safari/537.36");  
//	            https.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");  
	            https.setRequestProperty("Accept-Encoding", ":gzip, deflate, sdch");
	            https.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
	            https.setRequestProperty("Cache-Control", "max-age=0");
	            https.setRequestProperty("Connection", "keep-alive");
	            https.setRequestProperty("referer", "img.pximg.com");
	            conn = https;
	            conn.connect();
	        } else {
	        	conn = (HttpURLConnection) url.openConnection();
	        }
	        
	        conn.setReadTimeout(5 * 1000);
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			
			conn.getResponseCode();
		    InputStream inStream;
//		    if (inStream == null) {
		    	inStream = conn.getInputStream();
//		    }
		 // This is a try with resources, Java 7+ only
		    // If you use Java 6 or less, use a finally block instead
//		    try (Scanner scanner = new Scanner(inStream)) {
//		        scanner.useDelimiter("\\Z");
//		         System.out.print(scanner.next());
//		    }
		    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
		    	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = inStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, len);
				}
				outStream.close();
				inStream.close();
				return outStream.toByteArray();
 			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	

	/**
	 * Get image from newwork
	 * 
	 * @param path
	 *            The path of image
	 * @return InputStream
	 * @throws Exception
	 */
	@SuppressLint("NewApi") public InputStream getImageStream(String path) throws Exception {
		URL url = null;
        try {
            url = new URL(path);
        } catch (MalformedURLException e) {
            Log.e("getStreamFromNetwork", e.getMessage(), e);
        }
        HttpURLConnection conn = null;

        if (path.startsWith("https")) {
            trustAllHosts();
            HttpsURLConnection https;
            
            https = (HttpsURLConnection) url
                    .openConnection();
            https.setHostnameVerifier(DO_NOT_VERIFY);
            
            https.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");  
            https.setRequestProperty("Upgrade-Insecure-Requests", "1");  
            https.setRequestProperty("Host", "img.pximg.com");
            https.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.76 Mobile Safari/537.36");  
//            https.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");  
            https.setRequestProperty("Accept-Encoding", ":gzip, deflate, sdch");
            https.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
            https.setRequestProperty("Cache-Control", "max-age=0");
            https.setRequestProperty("Connection", "keep-alive");
            https.setRequestProperty("referer", "img.pximg.com");
            conn = https;
            conn.connect();
        } else {
        	conn = (HttpURLConnection) url.openConnection();
        }
        
        conn.setReadTimeout(5 * 1000);
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		
		conn.getResponseCode();
	    InputStream inStream;
//	    if (inStream == null) {
	    	inStream = conn.getInputStream();
//	    }
	 // This is a try with resources, Java 7+ only
	    // If you use Java 6 or less, use a finally block instead
//	    try (Scanner scanner = new Scanner(inStream)) {
//	        scanner.useDelimiter("\\Z");
//	         System.out.print(scanner.next());
//	    }
		return inStream;
	}

	   // always verify the host - dont check for certificate
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
    
	/**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] x509Certificates,
                    String s) throws java.security.cert.CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] x509Certificates,
                    String s) throws java.security.cert.CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	/**
	 * 保存文件
	 * 
	 * @param bm
	 * @param fileName
	 * @throws IOException
	 */
	public void saveFile(Bitmap bm, String fileName) throws IOException {
		File dirFile = new File(ALBUM_PATH);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File myCaptureFile = new File(ALBUM_PATH + fileName);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
		bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
		bos.flush();
		bos.close();
	}

	private Runnable saveFileRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				saveFile(mBitmap, mFileName);
				mSaveMessage = "图片保存成功！";
			} catch (IOException e) {
				mSaveMessage = "图片保存失败！";
				e.printStackTrace();
			}
			messageHandler.sendMessage(messageHandler.obtainMessage());
		}

	};

	private Handler messageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mSaveDialog.dismiss();
			Log.d(TAG, mSaveMessage);
			Toast.makeText(MainActivity.this, mSaveMessage, Toast.LENGTH_SHORT).show();
		}
	};
}
