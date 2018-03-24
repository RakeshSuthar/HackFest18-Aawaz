package com.example.pulkit.opencvtest;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {

    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";
    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;

    // Rakesh kumar suthar
    private Mat mHsvMat;
    private Mat mMaskMat;
    private Mat imask;
    private Mat mDilatedMat;
    private Mat hierarchy;
    private Scalar CONTOUR_COLOR;

    private int channelCount = 3;
    private int iLineThickness = 5;

    private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
    private List<MatOfPoint> mContours1 = new ArrayList<MatOfPoint>();
    private List<MatOfPoint> mMaxContours = new ArrayList<MatOfPoint>();

    private Scalar colorGreen = new Scalar(0, 255, 0);
    private static double mMinContourArea = 0.3;

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;
    // Used in Camera selection from menu (when implemented)
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                    // Rakesh kumar suthar
                    mHsvMat = new Mat();
                    mMaskMat = new Mat();

                    mDilatedMat = new Mat();
                    hierarchy = new Mat();
                    CONTOUR_COLOR = new Scalar(255,0,0,255);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        // TODO Auto-generated method stub
        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
        Core.flip(mRgbaF, mRgba, 1);

        //Pulkit Singhal


        Scalar lowerThreshold = new Scalar(110, 50, 50); // skin color – lower hsv values
        Scalar upperThreshold = new Scalar(130, 255, 255); // skin color – higher hsv values
        Mat converted = new Mat();
        Imgproc.cvtColor(mRgba,converted,Imgproc.COLOR_RGB2HSV);
        Mat skinmask = new Mat();
        Core.inRange(converted,lowerThreshold,upperThreshold,skinmask);

        org.opencv.core.Size s1 = new org.opencv.core.Size(11,11);
        org.opencv.core.Size s2 = new org.opencv.core.Size(3,3);

        org.opencv.core.Point p = new org.opencv.core.Point();
        Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,s1, p);
        Imgproc.erode(skinmask,mDilatedMat,new Mat());
        Imgproc.dilate(skinmask,mDilatedMat,new Mat());
        Imgproc.GaussianBlur(skinmask,skinmask,s2,0);
        Mat skin = new Mat();
        Core.bitwise_and(mRgba,mRgba,skin,skinmask);

        /*
        Mat im_ycrcb = new Mat();
        Imgproc.cvtColor(mRgba,im_ycrcb,Imgproc.COLOR_RGB2YCrCb);

        Mat skin_ycrcb = new Mat();
        Core.inRange(im_ycrcb,new Scalar(0,133,77),new Scalar(255,173,127), skin_ycrcb);
        Imgproc.findContours(skin_ycrcb,contours,hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }
        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                Core.multiply(contour, new Scalar(0,0), contour);
                mContours.add(contour);
            } else {
                mContours1.add(contour);
            }
        }

        Imgproc.drawContours(mRgba, mContours, -1, CONTOUR_COLOR);
        Imgproc.cvtColor(mRgba,mRgba, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(mRgba,mRgba,100,255,Imgproc.THRESH_BINARY);
        //Imgproc.GaussianBlur(mRgba,mRgba,mRgba.size(),2);
*/
                /*Mat raw = new Mat();
        Mat hsv = new Mat();
        Imgproc.cvtColor(mRgba,hsv,Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(mRgba,mRgba,Imgproc.COLOR_RGB2GRAY);
        double maxH = 0;
        Core.inRange(hsv,new Scalar(0, 0.23 * 255, 50),new Scalar(50, 0.68 * 255, 255),mRgba);
*/



/*
        Imgproc.cvtColor(mRgba, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Scalar lowerThreshold = new Scalar(0, 10, 60); // skin color – lower hsv values
        Scalar upperThreshold = new Scalar(20, 150, 255); // skin color – higher hsv values
        Core.inRange(mHsvMat, lowerThreshold, upperThreshold, mMaskMat);
        //Core.inRange(mRgba, new Scalar(230,0,0), new Scalar(255,0,0), mMaskMat);
       // imask = new Mat();
       // imask=





        //Imgproc.dilate(mMaskMat, mDilatedMat, new Mat());
        Imgproc.findContours(mMaskMat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                Core.multiply(contour, new Scalar(0,0), contour);
                mContours.add(contour);
            } else {
                mContours1.add(contour);
            }
        }

        Imgproc.drawContours(mRgba, mContours, -1, CONTOUR_COLOR);
        Imgproc.drawContours(mRgba, mContours1, -1, CONTOUR_COLOR);

        Imgproc.cvtColor(mHsvMat, mRgba, Imgproc.COLOR_HSV2RGB, 4);
        Imgproc.cvtColor(mRgba,mRgba, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(mRgba,mRgba,100,255,Imgproc.THRESH_BINARY_INV);
*/
        return skin; // This function must return
    }
}
