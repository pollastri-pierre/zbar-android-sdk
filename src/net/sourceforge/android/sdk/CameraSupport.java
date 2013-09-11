package net.sourceforge.android.sdk;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;

/**
 * A Wrapper class around the android {@link Camera} with functionalities
 * extensions.
 * 
 * @author Pierre Pollastri
 * 
 */
class CameraSupport {

	private long CONTINUOUS_AUTO_FOCUS_FREQUENCY = 2000l;

	private Camera mCamera;
	private AutoFocusCallback mAutoFocusCallback;
	private boolean mIsContinousAutoFocusedEnabled = false;
	private Handler mUiPostHandler = new Handler(Looper.getMainLooper());

	public CameraSupport(Camera camera) {
		mCamera = camera;
	}

	public void setContinuousFocus(boolean continuousFocus) {
		mIsContinousAutoFocusedEnabled = continuousFocus;
		if (hasBuiltInContinousFocus()) {
			mIsContinousAutoFocusedEnabled = false;
			getParameters().setFocusMode(
					continuousFocus ? Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
							: Parameters.FOCUS_MODE_AUTO);
		} else {
			getParameters().setFocusMode(Parameters.FOCUS_MODE_AUTO);
			mContinuousAutoFocusRunnable.run();
		}
	}

	public boolean hasBuiltInContinousFocus() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			Camera.Parameters parameters = mCamera.getParameters();
			for (String f : parameters.getSupportedFocusModes()) {
				if (f == Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) {
					return true;
				}
			}
		}
		return false;
	}

	public Camera getCamera() {
		return mCamera;
	}

	public void release() {
		if (mCamera == null) {
			return;
		}
		try {
			mCamera.release();
			mCamera = null;
		} catch (Exception e) {

		}
	}

	public boolean isValid() {
		return mCamera != null;
	}

	public static CameraSupport open() {
		int cameraId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? CameraInfo.CAMERA_FACING_BACK
				: 0;
		return open(cameraId);
	}

	@SuppressLint("NewApi")
	public static CameraSupport open(int cameraId) {
		Camera camera = null;
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				camera = Camera.open(cameraId);
			} else {
				camera = Camera.open();
			}
		} catch (Exception e) {

		}
		return new CameraSupport(camera);
	}

	public Parameters getParameters() {
		return mCamera.getParameters();
	}

	public void setDisplayOrientation(int degrees) {
		mCamera.setDisplayOrientation(degrees);
	}

	public void setPreviewDisplay(SurfaceHolder surfaceHolder)
			throws IOException {
		mCamera.setPreviewDisplay(surfaceHolder);
	}

	public void setPreviewCallback(PreviewCallback previewCallback) {
		mCamera.setPreviewCallback(previewCallback);
	}

	public void startPreview() {
		mCamera.startPreview();
	}

	public void autoFocus(AutoFocusCallback autoFocusCallback) {
		mAutoFocusCallback = autoFocusCallback;
		try {
			mCamera.autoFocus(mContinuousAutoFocusCallback);
		} catch (Exception e) {

		}
	}

	public void stopPreview() {
		try {
			mCamera.stopPreview();
		} catch (Exception e) {

		}
	}

	private AutoFocusCallback mContinuousAutoFocusCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			if (mAutoFocusCallback != null) {
				mAutoFocusCallback.onAutoFocus(success, camera);
			}
			if (mIsContinousAutoFocusedEnabled) {
				mUiPostHandler.postDelayed(mContinuousAutoFocusRunnable,
						CONTINUOUS_AUTO_FOCUS_FREQUENCY);
			}
		}
	};

	private Runnable mContinuousAutoFocusRunnable = new Runnable() {

		@Override
		public void run() {
			autoFocus(mAutoFocusCallback);
		}
	};

}
