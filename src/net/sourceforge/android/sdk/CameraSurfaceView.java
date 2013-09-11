package net.sourceforge.android.sdk;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Based on ZBar Android SDK Camera preview
 * https://github.com/ZBar/ZBar/tree/master/android
 * 
 * @author Pierre Pollastri
 * 
 */
public class CameraSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {

	private CameraSupport mCamera;
	private SurfaceHolder mHolder;
	private PreviewCallback mPreviewCallback;
	private AutoFocusCallback mAutoFocusCallback;

	@SuppressWarnings("deprecation")
	@SuppressLint("InlinedApi")
	public CameraSurfaceView(Context context, Camera camera,
			PreviewCallback previewCallback, AutoFocusCallback autoFocusCallback) {
		super(context);
		mCamera = new CameraSupport(camera);
		mPreviewCallback = previewCallback;
		mAutoFocusCallback = autoFocusCallback;
		mCamera.setContinuousFocus(true);
		mHolder = getHolder();

		mHolder = getHolder();
		mHolder.addCallback(this);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

	}

	public void init() {

	}

	public void open() {

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		Size size = getBestPreviewSize(height, width, mCamera.getParameters());
		float aspectRatio = (float) size.height / (float) size.width;
		if (width < height * aspectRatio) {
			width = (int) (height * aspectRatio + .5);
		} else {
			height = (int) (width / aspectRatio + .5);
		}
		setMeasuredDimension(width, height);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (mHolder.getSurface() == null) {
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		try {
			// Hard code camera surface rotation 90 degs to match Activity view
			// in portrait
			mCamera.setDisplayOrientation(90);
			Size bestFit = getBestPreviewSize(width, height,
					mCamera.getParameters());
			mCamera.getParameters().setPreviewSize(bestFit.width,
					bestFit.height);
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(mPreviewCallback);
			mCamera.startPreview();
			mCamera.autoFocus(mAutoFocusCallback);
		} catch (Exception e) {

		}

	}

	/**
	 * Based on http://stackoverflow.com/a/15303282/1209254
	 */
	Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;
		float dr = Float.MAX_VALUE;
		float ratio = (float) width / (float) height;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			float r = (float) size.width / (float) size.height;
			if (Math.abs(r - ratio) < dr && size.width <= width
					&& size.height <= height) {
				dr = Math.abs(r - ratio);
				result = size;
			}
		}

		return result;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {

		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

}
