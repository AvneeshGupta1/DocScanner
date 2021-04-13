package com.vlk.multimager.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.legacy.app.FragmentCompat;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.vlk.multimager.R;
import com.vlk.multimager.utils.Constants;
import com.vlk.multimager.utils.Image;
import com.vlk.multimager.utils.Params;
import com.vlk.multimager.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vansikrishna on 08/06/2016.
 */
public class CameraFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final int ORIENTATION_PORTRAIT_NORMAL = 1;
    private static final int ORIENTATION_PORTRAIT_INVERTED = 2;
    private static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
    private static final int ORIENTATION_LANDSCAPE_INVERTED = 4;
    RelativeLayout coordinatorLayout;
    RelativeLayout parentLayout;
    RelativeLayout cameraLayout;
    SurfaceView surfaceView;
    ImageButton captureButton;
    ImageButton doneAllButton;
    ImageButton flashButton;
    LinearLayout layoutImages;
    HorizontalScrollView layoutBottom;
    RelativeLayout previewLayout;
    ImageView previewImageView;
    Button doneButton;
    Button retakeButton;
    Button nextButton;
    ArrayList<Image> selectedImages = new ArrayList<>();
    int pictureRotation = 0;
    OrientationEventListener mOrientationEventListener;
    Uri fileUri;
    Params params;
    private SurfaceHolder previewHolder = null;
    private ProgressBar progressBar;
    private int mOrientation = -1;
    private Camera camera = null;
    private boolean inPreview = false, cameraConfigured = false, isZoomSupported = false;
    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            if (inPreview) {
                if (camera != null)
                    camera.stopPreview();
            }
            cameraConfigured = false;
            getPictureRotation(ORIENTATION_LANDSCAPE_NORMAL);
            initPreview(width, height);
            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };
    Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            File file = getOutputMediaFile();
            new SavePhotoTask(file).execute(data);
            camera.startPreview();
            inPreview = true;
        }
    };
    private float dist;
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (inPreview && cameraLayout.getVisibility() == View.VISIBLE && isZoomSupported) {
                // Get the pointer ID
                Camera.Parameters params = camera.getParameters();
                int action = event.getAction();

                if (event.getPointerCount() > 1) {
                    // handle multi-touch events
                    if (action == MotionEvent.ACTION_POINTER_DOWN) {
                        dist = getFingerSpacing(event);
                    } else if (action == MotionEvent.ACTION_MOVE) {
                        camera.cancelAutoFocus();
                        handleZoom(event, params);
                    }
                } else {
                    // handle single touch events
                    if (action == MotionEvent.ACTION_UP) {
                        handleFocus(event, params);
                    }
                }
                return true;
            } else
                return false;
        }
    };

    public static CameraFragment newInstance(Params params) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, params);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

        layoutBottom = view.findViewById(R.id.layoutBottom);
        layoutImages = view.findViewById(R.id.layout_images);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
        parentLayout = view.findViewById(R.id.parentLayout);
        cameraLayout = view.findViewById(R.id.cameraLayout);
        surfaceView = view.findViewById(R.id.surfaceView);
        captureButton = view.findViewById(R.id.captureButton);
        Utils.changeDrawableColor(getActivity(), captureButton);
        doneAllButton = view.findViewById(R.id.doneAllButton);
        Utils.changeDrawableColor(getActivity(), doneAllButton);
        doneAllButton.setVisibility(View.GONE);
        flashButton = view.findViewById(R.id.flashButton);
        Utils.changeDrawableColor(getActivity(), flashButton);
        previewLayout = view.findViewById(R.id.previewLayout);
        previewImageView = view.findViewById(R.id.previewImageView);
        doneButton = view.findViewById(R.id.doneButton);
        retakeButton = view.findViewById(R.id.retakeButton);
        nextButton = view.findViewById(R.id.nextButton);
        progressBar = view.findViewById(R.id.progressBar);
        captureButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);
        retakeButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        doneAllButton.setOnClickListener(this);
        flashButton.setOnClickListener(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        checkPermissions();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            params = (Params) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    private void requestMultiplePermissions() {
        FragmentCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                Constants.REQUEST_MULTIPLE_PERMISSIONS);
    }

    private void checkPermissions() {
        if (!((BaseActivity) getActivity()).hasCameraPermission(getActivity())
                || !((BaseActivity) getActivity()).hasStoragePermission(getActivity())) {
            requestMultiplePermissions();
        } else {
            setupCamera();
            showCameraLayout(true);
            initCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_MULTIPLE_PERMISSIONS:
                if (((BaseActivity) getActivity()).validateGrantedPermissions(grantResults)) {
                    setupCamera();
                    showCameraLayout(true);
                    initCamera();
                } else {
                    Toast.makeText(getActivity(), "Permissions not granted.", Toast.LENGTH_LONG).show();
                    setEmptyResult();
                }
                break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void showCameraLayout(boolean flag) {
        if (flag) {
            cameraLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            previewLayout.setVisibility(View.GONE);
            layoutBottom.setVisibility(View.GONE);
        } else {
            cameraLayout.setVisibility(View.GONE);
            previewLayout.setVisibility(View.VISIBLE);
            showPreviewImage();

            layoutImages.removeAllViews();
            for (int i = 0; i < selectedImages.size(); i++) {
                RelativeLayout image = getThumbNail(selectedImages.get(i).imagePath, i);
                layoutImages.addView(image);
            }
            if (!selectedImages.isEmpty()) {
                doneAllButton.setVisibility(View.VISIBLE);
            } else {
                doneAllButton.setVisibility(View.GONE);
            }
            layoutBottom.setVisibility(selectedImages.size() == 0 ? View.GONE : View.VISIBLE);
        }
    }

    private void showPreviewImage() {
        progressBar.setVisibility(View.VISIBLE);
        Picasso.get()
                .load(new File(selectedImages.get(selectedImages.size() - 1).imagePath))
                .error(R.drawable.no_image_full)
                .into(previewImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    private void init() {
        handleInputParams();
        initFlashIcon();
    }

    private void handleInputParams() {
        if (params.getCaptureLimit() == 0) {
            Utils.showLongSnack(parentLayout, "Please mention the capture limit as a parameter.");
            setEmptyResult();
        }
        Utils.setViewBackgroundColor(getActivity(), captureButton, params.getActionButtonColor());
        Utils.setViewBackgroundColor(getActivity(), doneAllButton, params.getActionButtonColor());
        Utils.setViewBackgroundColor(getActivity(), flashButton, params.getActionButtonColor());
        Utils.setButtonTextColor(doneButton, params.getButtonTextColor());
        Utils.setButtonTextColor(retakeButton, params.getButtonTextColor());
        Utils.setButtonTextColor(nextButton, params.getButtonTextColor());
    }

    private void initFlashIcon() {
        boolean isFlashAvailable = Utils.hasCameraFlashHardware(getActivity());
        if (!isFlashAvailable) {
            flashButton.setVisibility(View.GONE);
        }
    }

    private RelativeLayout getThumbNail(String path, int number) {
        RelativeLayout view = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_image, null, false);
        ImageView imageView = view.findViewById(R.id.iv_thumb);
        final TextView textView = view.findViewById(R.id.tv_number);
        textView.setVisibility(View.INVISIBLE);
        textView.setText(String.valueOf((number + 1)));

        Picasso.get().load(new File(path)).resize(200, 200).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                textView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {

            }
        });


        return view;
    }

    private void toggleFlashState() {
        try {
            if (!inPreview)
                return;
            if (camera.getParameters().getFlashMode().equalsIgnoreCase(Camera.Parameters.FLASH_MODE_TORCH)) {
                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
                flashButton.setImageResource(R.drawable.ic_flash_on);
            } else {
                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                flashButton.setImageResource(R.drawable.ic_flash_off);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setEmptyResult() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void setIntentResult(Intent intent) {
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    public void onBackPressed() {
        if (!selectedImages.isEmpty() && cameraLayout.getVisibility() == View.VISIBLE) {
            showCameraLayout(false);
        } else
            setEmptyResult();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setEmptyResult();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.equals(captureButton)) {
            if (params.getCaptureLimit() > selectedImages.size())
                captureImage();
            else {
                Utils.showShortSnack(parentLayout, "You can capture only " + params.getCaptureLimit() + " images at a time.");
            }
        } else if (view.equals(flashButton)) {
            toggleFlashState();
        } else if (view.equals(doneButton)) {
            if (selectedImages.size() > 0)
                collectAllPaths();
            else
                setEmptyResult();
        } else if (view.equals(doneAllButton)) {
            if (selectedImages.size() > 0)
                collectAllPaths();
            else
                setEmptyResult();
        } else if (view.equals(retakeButton)) {
            retakeImage();
        } else if (view.equals(nextButton)) {
            showCameraLayout(true);
        }
    }

    private void retakeImage() {
        if (selectedImages.size() > 0)
            selectedImages.remove(selectedImages.size() - 1);
        showCameraLayout(true);
    }

    private void initCamera() {
        previewHolder = surfaceView.getHolder();
        surfaceView.setOnTouchListener(onTouchListener);
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void setupCamera() {
        try {
            if (camera == null) {
                camera = Camera.open();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Camera Open", e.toString());
        }
        startPreview();
        setupOrientationListener();
    }

    private void setupOrientationListener() {
        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(getActivity(), SensorManager.SENSOR_DELAY_NORMAL) {

                @Override
                public void onOrientationChanged(int orientation) {

                    // determine our orientation based on sensor response
                    int lastOrientation = mOrientation;

                    if (orientation >= 315 || orientation < 45) {
                        if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                            mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                        }
                    } else if (orientation < 315 && orientation >= 225) {
                        if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                            mOrientation = ORIENTATION_PORTRAIT_NORMAL;
                        }
                    } else if (orientation < 225 && orientation >= 135) {
                        if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                            mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                        }
                    } else { // orientation <135 && orientation > 45
                        if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                            mOrientation = ORIENTATION_PORTRAIT_INVERTED;
                        }
                    }

                    if (lastOrientation != mOrientation) {
                        getPictureRotation(mOrientation);
                        changeCameraParameters();
                        changeDoneAllImageOrientation(mOrientation);
                    }
                }
            };
        }
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    private void changeDoneAllImageOrientation(int orientation) {
        int degrees = 0;
        switch (orientation) {
            case ORIENTATION_LANDSCAPE_NORMAL:
                degrees = 0;
                break;
            case ORIENTATION_PORTRAIT_NORMAL:
                degrees = 90;
                break;
            case ORIENTATION_LANDSCAPE_INVERTED:
                degrees = 180;
                break;
            case ORIENTATION_PORTRAIT_INVERTED:
                degrees = 270;
                break;
        }
        doneAllButton.setImageDrawable(getRotatedImage(R.drawable.ic_done_all, degrees));
    }

    private Drawable getRotatedImage(int drawableId, int degrees) {
        Bitmap original = BitmapFactory.decodeResource(getResources(), drawableId);
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        Bitmap rotated = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        return new BitmapDrawable(rotated);
    }

    private void changeCameraParameters() {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setRotation(pictureRotation);
            camera.setParameters(parameters);
        }
    }

    @Override
    public void onPause() {
        if (camera != null) {
            if (inPreview) {
                camera.stopPreview();
            }
            camera.release();
            camera = null;
            inPreview = false;
        }
        if (mOrientationEventListener != null)
            mOrientationEventListener.disable();
        super.onPause();
    }

    private void startPreview() {
        if (cameraConfigured && camera != null) {
            camera.startPreview();
            inPreview = true;
        }
    }

    private void captureImage() {
        if (inPreview) {
            camera.takePicture(null, null, photoCallback);
            inPreview = false;
        }
    }

    private void collectAllPaths() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST, selectedImages);
        setIntentResult(intent);
    }

    private void initPreview(int width, int height) {
        if (camera != null && previewHolder.getSurface() != null) {
            try {
                camera.setPreviewDisplay(previewHolder);
            } catch (Throwable t) {
                Log.e("initPreview",
                        "Exception in setPreviewDisplay() = " + t.toString());
                Toast.makeText(getActivity(), t.toString(),
                        Toast.LENGTH_LONG).show();
            }

            if (!cameraConfigured) {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height, parameters);
                Camera.Size pictureSize = getLargestPictureSize(parameters);
                int maxZoomLevel = parameters.getMaxZoom();
                isZoomSupported = parameters.isZoomSupported();
                if (size != null && pictureSize != null) {
//					parameters.setPreviewSize(size.width, size.height);
                    parameters.setPictureSize(pictureSize.width,
                            pictureSize.height);
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    parameters.setJpegQuality(100);
                    parameters.setRotation(pictureRotation);
                    camera.setParameters(parameters);
                    camera.setDisplayOrientation(90);
                    cameraConfigured = true;
                }
            }
        }
    }

    private int getPictureRotation(int orientation) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        int degrees = 0;
        switch (orientation) {
            case ORIENTATION_LANDSCAPE_NORMAL:
                degrees = 0;
                break;
            case ORIENTATION_PORTRAIT_NORMAL:
                degrees = 90;
                break;
            case ORIENTATION_LANDSCAPE_INVERTED:
                degrees = 180;
                break;
            case ORIENTATION_PORTRAIT_INVERTED:
                degrees = 270;
                break;
        }
        int result = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
            pictureRotation = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
            pictureRotation = result;
        }

        return pictureRotation;
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return (result);
    }

    private Camera.Size getLargestPictureSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }
        return (result);
    }

    private String getImageRealPathFromURI(Uri contentUri) {
        String realPath = "";
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                realPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            } else {
                Log.e("Image Real Path", "Cursor count appearing to be zero");
//                Toast.makeText(getActivity(), "Cursor count appearing to be zero", Toast.LENGTH_LONG).show();
                realPath = "";
            }
        } catch (Exception e) {
            Log.e("Image Real Path", "Exception fetching getImageRealPathFromURI() due to " + e.toString());
//            Toast.makeText(getActivity(),"Exception fetching getImageRealPathFromURI() due to "+e.toString(), Toast.LENGTH_LONG).show();
            realPath = "";
        } finally {
            cursor.close();
        }
        return realPath;
    }

    protected File getOutputMediaFile() {
        String pictureDic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        File file = new File(pictureDic, timeStamp + ".jpg");

        fileUri = getUriFromFile(file);
        String[] pathList = new String[]{file.getPath()};
        MediaScannerConnection.scanFile(getActivity().getApplicationContext(), pathList, null, null);
        return file;
    }


    private Uri getUriFromFile(File file) {
        if (Build.VERSION.SDK_INT < 24) {
            return Uri.fromFile(file);
        } else {
            return FileProvider.getUriForFile(getActivity(), getActivity().getPackageName()+".provider", file);
        }
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > dist) {
            //zoom in
            if (zoom < maxZoom) {
                zoom = zoom + 2;
                if (zoom > maxZoom)
                    zoom = maxZoom;
            }
        } else if (newDist < dist) {
            //zoom out
            if (zoom > 0) {
                zoom = zoom - 2;
                if (zoom < 0)
                    zoom = 0;
            }
        }
        dist = newDist;
        params.setZoom(zoom);
        camera.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /**
     * Determine the space between the first two fingers
     */
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    class SavePhotoTask extends AsyncTask<byte[], String, File> {

        File photoFile;

        public SavePhotoTask(File file) {
            photoFile = file;
        }

        @Override
        protected File doInBackground(byte[]... jpeg) {
            String path = "";
            File photo = photoFile;
            if (photo.exists()) {
                photo.delete();
            }
            try {
                FileOutputStream fos = new FileOutputStream(photo.getPath());
                fos.write(jpeg[0]);
                fos.close();
                path = photo.getPath();

                if (pictureRotation == 90 || pictureRotation == 270) {
                    Bitmap bmp = BitmapFactory.decodeFile(path);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                    FileOutputStream fOut;
                    try {
                        fOut = new FileOutputStream(path);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                        fOut.flush();
                        fOut.close();
                    } catch (Exception ignored) {
                        Log.e("MultiCamera", "Exception in photoCallback");
                    }
                }

                Log.e("Image path", "" + path);
            } catch (java.io.IOException e) {
                Log.e("MultiCamera", "Exception in photoCallback", e);
                return null;
            }
            return photo;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (file != null) {
                Image image = new Image(0, fileUri, file.getPath(),
                        (pictureRotation == 90 || pictureRotation == 270));
                selectedImages.add(image);
                showCameraLayout(false);
            } else {
                Toast.makeText(getActivity(), "Sorry. An error occured while capturing image. Please try again.", Toast.LENGTH_LONG).show();
                showCameraLayout(true);
            }
        }
    }

}
