/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package trash.trashbot;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import trash.trashbot.env.Logger;


public class CameraFragment extends Fragment {
    private static final Logger LOGGER = new Logger();

    private static final int MINIMUM_PREVIEW_SIZE = 320;

    private AutoFitTextureView textureView;

    private final Semaphore cameraOpenCloseLock = new Semaphore(1);

    private final int layoutId = R.layout.camera_layout;
    private String cameraId;
    private Size previewSize;
    private final Size inputSize = new Size(640, 480);
    private Integer sensorOrientation;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder previewRequestBuilder;
    private ImageReader previewReader;
    private CaptureRequest previewRequest;
    private CameraCaptureSession captureSession;
    private boolean isProcessingFrame = false;

    private Handler backgroundHandler;
    private HandlerThread backgroundThread;


    private final ImageReader.OnImageAvailableListener imageListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(final ImageReader reader) {
                    //We need wait until we have some size from onPreviewSizeChosen
                    if (previewSize.getWidth() == 0 || previewSize.getHeight() == 0) {
                        return;
                    }
//                    if (rgbBytes == null) {
//                        rgbBytes = new int[previewWidth * previewHeight];
//                    }
//                    try {
                        final Image image = reader.acquireLatestImage();
//
                        if (image == null) {
                            return;
                        }
                        image.close();

//                        if (isProcessingFrame) {
//                            image.close();
//                            return;
//                        }
//                        isProcessingFrame = true;
//                        Trace.beginSection("imageAvailable");
//                        final Image.Plane[] planes = image.getPlanes();
//                        fillBytes(planes, yuvBytes);
//                        yRowStride = planes[0].getRowStride();
//                        final int uvRowStride = planes[1].getRowStride();
//                        final int uvPixelStride = planes[1].getPixelStride();
//
//                        imageConverter =
//                                new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        ImageUtils.convertYUV420ToARGB8888(
//                                                yuvBytes[0],
//                                                yuvBytes[1],
//                                                yuvBytes[2],
//                                                previewWidth,
//                                                previewHeight,
//                                                yRowStride,
//                                                uvRowStride,
//                                                uvPixelStride,
//                                                rgbBytes);
//                                    }
//                                };
//
//                        postInferenceCallback =
//                                new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        image.close();
//                                        isProcessingFrame = false;
//                                    }
//                                };
//
//                        processImage();
//                    } catch (final Exception e) {
//                        LOGGER.e(e, "Exception!");
//                        Trace.endSection();
//                        return;
//                    }
//                    Trace.endSection();
                }
            };

    private final CameraDevice.StateCallback stateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(final CameraDevice cd) {
                    // This method is called when the camera is opened.  We start camera preview here.
                    cameraOpenCloseLock.release();
                    cameraDevice = cd;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(final CameraDevice cd) {
                    cameraOpenCloseLock.release();
                    cd.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(final CameraDevice cd, final int error) {
                    cameraOpenCloseLock.release();
                    cd.close();
                    cameraDevice = null;
                    final Activity activity = getActivity();
                    if (null != activity) {
                        activity.finish();
                    }
                }
            };

    private final CameraCaptureSession.CaptureCallback captureCallback =
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureProgressed(
                        final CameraCaptureSession session,
                        final CaptureRequest request,
                        final CaptureResult partialResult) {
                }

                @Override
                public void onCaptureCompleted(
                        final CameraCaptureSession session,
                        final CaptureRequest request,
                        final TotalCaptureResult result) {
                }
            };


    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(
                        final SurfaceTexture texture, final int width, final int height) {
                    LOGGER.i("onSurfaceTextureAvailable");
                    openCamera(width, height);
                    LOGGER.i("opencamera finished");
                }

                @Override
                public void onSurfaceTextureSizeChanged(
                        final SurfaceTexture texture, final int width, final int height) {
                    LOGGER.i("onSurfaceTextureSizeChanged");
                    configureTransform(width, height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(final SurfaceTexture texture) {
                }
            };


    private void createCameraPreviewSession() {
        try {
            final SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            // This is the output Surface we need to start preview.
            final Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            LOGGER.i("Opening camera preview: " + previewSize.getWidth() + "x" + previewSize.getHeight());

            // Create the reader for the preview frames.
            previewReader = ImageReader.newInstance(
                    previewSize.getWidth(), previewSize.getHeight(),
                    ImageFormat.YUV_420_888, 2);
//
            previewReader.setOnImageAvailableListener(imageListener, backgroundHandler);
            previewRequestBuilder.addTarget(previewReader.getSurface());

            // Auto focus should be continuous for camera preview.
            previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // Flash is automatically enabled when necessary.
            previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            LOGGER.i("addTargetFinished");
            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice.createCaptureSession(
                    Arrays.asList(surface, previewReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(final CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                LOGGER.i("null == cameraService");
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            captureSession = cameraCaptureSession;
                            try {

                                // Finally, we start displaying the camera preview.
                                previewRequest = previewRequestBuilder.build();
                                captureSession.setRepeatingRequest(
                                        previewRequest, captureCallback, backgroundHandler);
                            } catch (final CameraAccessException e) {
                                LOGGER.e(e, "Exception!");
                            }
                        }

                        @Override
                        public void onConfigureFailed(final CameraCaptureSession cameraCaptureSession) {
                            LOGGER.e("onConfigureFailed");
                        }
                    },
                    null);
            LOGGER.i("create session finished");
        } catch (final CameraAccessException e) {
            LOGGER.e(e, "Exception!");
        }
    }

    private void openCamera(final int width, final int height) {
        setUpCameraOutputs();
        configureTransform(width, height);
        final Activity activity = getActivity();
        final CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (final CameraAccessException e) {
            LOGGER.e(e, "Exception!");
        } catch (final InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != captureSession) {
                captureSession.close();
                captureSession = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != previewReader) {
                previewReader.close();
                previewReader = null;
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("ImageListener");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (final InterruptedException e) {
            LOGGER.e(e, "Exception!");
        }
    }

    private void configureTransform(final int viewWidth, final int viewHeight) {
        final Activity activity = getActivity();
        if (null == textureView || null == previewSize || null == activity) {
            return;
        }
        final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        final Matrix matrix = new Matrix();
        final RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        final RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        final float centerX = viewRect.centerX();
        final float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            final float scale =
                    Math.max(
                            (float) viewHeight / previewSize.getHeight(),
                            (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    private void setUpCameraOutputs() {
        final Activity activity = getActivity();
        final CameraManager manager =
                (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {
            final CameraCharacteristics characteristics =
                    manager.getCameraCharacteristics(cameraId);

            final StreamConfigurationMap map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // For still image captures, we use the largest available size.
            final Size largest =
                    Collections.max(
                            Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888)),
                            new CompareSizesByArea());

            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            // Danger, W.R.! Attempting to use too large a preview size could exceed the camera
            // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
            // garbage capture data.
            previewSize =
                    chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                            inputSize.getWidth(),
                            inputSize.getHeight());

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            final int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            } else {
                textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
            }
        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
        }

        onPreviewSizeChosen(previewSize, sensorOrientation);
    }

    protected static Size chooseOptimalSize(final Size[] choices, final int width, final int height) {
        final int minSize = Math.max(Math.min(width, height), MINIMUM_PREVIEW_SIZE);
        final Size desiredSize = new Size(width, height);

        // Collect the supported resolutions that are at least as big as the preview Surface
        boolean exactSizeFound = false;
        final List<Size> bigEnough = new ArrayList<Size>();
        final List<Size> tooSmall = new ArrayList<Size>();
        for (final Size option : choices) {
            if (option.equals(desiredSize)) {
                // Set the size but don't return yet so that remaining sizes will still be logged.
                exactSizeFound = true;
            }

            if (option.getHeight() >= minSize && option.getWidth() >= minSize) {
                bigEnough.add(option);
            } else {
                tooSmall.add(option);
            }
        }

        LOGGER.i("Desired size: " + desiredSize + ", min size: " + minSize + "x" + minSize);
        LOGGER.i("Valid preview sizes: [" + TextUtils.join(", ", bigEnough) + "]");
        LOGGER.i("Rejected preview sizes: [" + TextUtils.join(", ", tooSmall) + "]");

        if (exactSizeFound) {
            LOGGER.i("Exact size match found.");
            return desiredSize;
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            final Size chosenSize = Collections.min(bigEnough, new CompareSizesByArea());
            LOGGER.i("Chosen size: " + chosenSize.getWidth() + "x" + chosenSize.getHeight());
            return chosenSize;
        } else {
            LOGGER.e("Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(final Size lhs, final Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private String chooseCamera() {
        final Activity activity = getActivity();
        final CameraManager manager =
                (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics =
                        manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera.
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) continue;

                boolean useCamera2API = (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                        || isHardwareLevelSupported(characteristics,
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
                assert (useCamera2API);
                return cameraId;
            }
        } catch (Exception e) {
            LOGGER.e(e, "No access or no camera2");
        }
        return null;
    }

    private boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        // deviceLevel is not LEGACY, can use numerical sort
        return requiredLevel <= deviceLevel;
    }

    public void onPreviewSizeChosen(final Size size, final int rotation) {
//        final float textSizePx = TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
//        borderedText = new BorderedText(textSizePx);
//        borderedText.setTypeface(Typeface.MONOSPACE);
//
//        classifier =
//                TensorFlowImageClassifier.create(
//                        getAssets(),
//                        MODEL_FILE,
//                        LABEL_FILE,
//                        INPUT_SIZE,
//                        IMAGE_MEAN,
//                        IMAGE_STD,
//                        INPUT_NAME,
//                        OUTPUT_NAME);
//
//        previewWidth = size.getWidth();
//        previewHeight = size.getHeight();
//
//        sensorOrientation = rotation - getScreenOrientation();
//        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);
//
//        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
//        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
//        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);
//
//        frameToCropTransform = ImageUtils.getTransformationMatrix(
//                previewWidth, previewHeight,
//                INPUT_SIZE, INPUT_SIZE,
//                sensorOrientation, MAINTAIN_ASPECT);
//
//        cropToFrameTransform = new Matrix();
//        frameToCropTransform.invert(cropToFrameTransform);
    }


    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    public CameraFragment() {
    }

    @Override
    public void onCreate(Bundle savedBundleState) {
        super.onCreate(savedBundleState);
        cameraId = chooseCamera();
        LOGGER.i("cameraId = " + cameraId);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
    }


    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
            LOGGER.i("opencamera finished");
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
            LOGGER.i("setsurfacelistener");
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layoutId, container, false);
    }
}
