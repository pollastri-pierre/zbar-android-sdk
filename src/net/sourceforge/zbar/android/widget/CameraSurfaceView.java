package net.sourceforge.zbar.android.widget;

import net.sourceforge.zbar.android.CameraSupport;
import android.content.Context;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class CameraSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {

	private CameraSupport mCameraSupport;
	private PreviewCallback mPreviewCallback;
	private AutoFocusCallback mAutoFocusCallback;
	private SurfaceHolder mHolder;

	public CameraSurfaceView(Context context) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setCameraSupport(new CameraSupport());
		setVisibility(View.GONE);
	}

	public void setCameraSupport(CameraSupport camera) {
		if (camera.isOpen()) {
			throw new UnsupportedOperationException(
					"CameraSurfaceView cannot handle opened camera");
		}
		mCameraSupport = camera;
	}

	public void open(PreviewCallback previewCallback,
			AutoFocusCallback autoFocusCallback) {
		setCameraSupport(new CameraSupport());
		setVisibility(View.GONE);
		mPreviewCallback = previewCallback;
		mCameraSupport.open(previewCallback, autoFocusCallback);
		setVisibility(View.VISIBLE);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		/*
		 * If your preview can change or rotate, take care of those events here.
		 * Make sure to stop the preview before resizing or reformatting it.
		 */
		if (mHolder.getSurface() == null || !mCameraSupport.isOpen()) {
			// preview surface does not exist
			return;
		}

		mCameraSupport.stopPreview();

		try {
			// Hard code camera surface rotation 90 degs to match Activity view
			// in portrait
			mCameraSupport.setDisplayOrientation(90);
			mCameraSupport.setPreviewDisplay(mHolder);
			mCameraSupport.setPreviewCallback(mPreviewCallback);
			mCameraSupport.startPreview();
			mCameraSupport.setContinuousAutoFocus(true);
		} catch (Exception e) {
			Log.d("DBG", "Error starting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	public void release() {
		mCameraSupport.release();
	}

	public CameraSupport getCameraSupport() {
		return mCameraSupport;
	}

}
