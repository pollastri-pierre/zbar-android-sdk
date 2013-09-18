package net.sourceforge.android.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

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

	public CameraSurfaceView(Context context) {
		this(context, null);
		setVisibility(View.GONE);
	}

	public CameraSurfaceView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		setVisibility(View.GONE);
	}

	public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mHolder = getHolder();
		mHolder.addCallback(this);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		setVisibility(View.GONE);
	}

	public void open(PreviewCallback previewCallback,
			AutoFocusCallback autoFocusCallback) {
		mCamera = CameraSupport.open();
		mPreviewCallback = previewCallback;
		mAutoFocusCallback = autoFocusCallback;
		mCamera.setContinuousFocus(true);
		setVisibility(View.INVISIBLE);
		setVisibility(View.VISIBLE);
	}

	public void release() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mCamera == null || !mCamera.isValid()) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		Size size = getBestPreviewSize(height, width, mCamera.getParameters());
		float aspectRatio = (float) size.height / (float) size.width;
		if (width < height * aspectRatio) {
			width = (int) (height * aspectRatio + .5);
		} else {
			height = (int) (width / aspectRatio + .5);
		}

		mHolder.setFixedSize(width, height);

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (mHolder.getSurface() == null || mCamera == null
				|| !mCamera.isValid()) {
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		try {
			// TODO Change this
			// Hard code camera surface rotation 90 degrees to match Activity
			// view
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
		if (mCamera == null) {
			return;
		}
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (Exception e) {

		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	public CameraSupport getCameraSupport() {
		return mCamera;
	}

}
