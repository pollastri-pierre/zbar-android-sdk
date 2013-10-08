package net.sourceforge.zbar.android;

import java.io.IOException;

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;

public class CameraSupport {

	private static final long AUTO_FOCUS_DELAY = 1000;

	private Camera mCamera;
	private PreviewCallback mPreviewCallback;
	private AutoFocusCallback mAutoFocusCallback;
	private Handler mAutoFocusHandler = new Handler(Looper.getMainLooper());
	private boolean mContinuousAutoFocus = false;
	private boolean mPreviewing = false;

	public CameraSupport() {

	}

	public boolean open(PreviewCallback previewCallback,
			AutoFocusCallback autoFocusCallback) {
		if (!isOpen()) {
			try {
				mCamera = Camera.open();
				mCamera.getParameters().setFocusMode(
						Camera.Parameters.FOCUS_MODE_AUTO);
			} catch (Exception e) {

			}
			setAutoFocusCallback(autoFocusCallback);
			setPreviewCallback(previewCallback);
		}
		return isOpen();
	}

	public Camera getCamera() {
		return mCamera;
	}

	public boolean isOpen() {
		return mCamera != null;
	}

	public void setDisplayOrientation(int degrees) {
		if (isOpen()) {
			getCamera().setDisplayOrientation(degrees);
		}
	}

	public void release() {
		if (isOpen()) {
			stopPreview();
			Camera camera = mCamera;
			mCamera = null;
			camera.setPreviewCallback(null);
			camera.release();
		}
	}

	public void setContinuousAutoFocus(boolean enable) {
		mContinuousAutoFocus = enable;
		if (!isOpen()) {
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			Camera.Parameters parameters = getCamera().getParameters();
			for (String f : parameters.getSupportedFocusModes()) {
				if (f == Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
					getCamera().getParameters().setFocusMode(
							Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
					return;
				}
			}
		}
		if (mContinuousAutoFocus) {
			autoFocus();
		}
	}

	public void setPreviewCallback(PreviewCallback previewCallback) {
		mPreviewCallback = previewCallback;
		if (isOpen()) {
			getCamera().setPreviewCallback(mPrivatePreviewCallback);
		}
	}

	public void setAutoFocusCallback(AutoFocusCallback callback) {
		mAutoFocusCallback = callback;
	}

	public void autoFocus() {
		if (isPreviewing()) {
			getCamera().autoFocus(mPrivateAutoFocusCallback);
		} else if (mContinuousAutoFocus && isOpen()) {
			mPrivateAutoFocusCallback.onAutoFocus(false, null);
		}
	}

	public void autofocus(AutoFocusCallback callback) {
		if (isPreviewing()) {
			getCamera().autoFocus(callback);
		}
	}

	public void setPreviewDisplay(SurfaceHolder holder) throws IOException {
		if (isOpen()) {
			getCamera().setPreviewDisplay(holder);
		}
	}

	public void startPreview() {
		if (!mPreviewing && isOpen()) {
			getCamera().startPreview();
			getCamera().setPreviewCallback(mPrivatePreviewCallback);
			mPreviewing = true;
		}
	}

	public void stopPreview() {
		if (isPreviewing()) {
			mPreviewing = false;
			try {
				getCamera().cancelAutoFocus();
				getCamera().setPreviewCallback(null);
				getCamera().stopPreview();
			} catch (Exception e) {

			}
		}
	}

	public boolean isPreviewing() {
		return isOpen() && mPreviewing;
	}

	private final PreviewCallback mPrivatePreviewCallback = new PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (mPreviewCallback != null) {
				mPreviewCallback.onPreviewFrame(data, camera);
			}
		}
	};

	private final AutoFocusCallback mPrivateAutoFocusCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean succeed, Camera camera) {
			if (mAutoFocusCallback != null && camera != null) {
				mAutoFocusCallback.onAutoFocus(succeed, camera);
			}
			if (mContinuousAutoFocus) {
				mAutoFocusHandler.postDelayed(mAutoFocusRunnable,
						AUTO_FOCUS_DELAY);
			}
		}
	};

	private Runnable mAutoFocusRunnable = new Runnable() {

		@Override
		public void run() {
			autoFocus();
		}
	};

}
