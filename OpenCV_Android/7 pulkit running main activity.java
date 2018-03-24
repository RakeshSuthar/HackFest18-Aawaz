package com.example.pulkit.opencvtest;

import android.gesture.OrientedBoundingBox;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
    private Mat mDilatedMat;
    private Mat hierarchy;
    private Mat temp;
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
                    temp = new Mat();
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


        mDilatedMat.release();
        // TODO Auto-generated method stub
        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
        Core.flip(mRgbaF, mRgba, 1);

        // Pulkit Singhal

        Scalar lowerThreshold = new Scalar(0, 48, 80); // skin color – lower hsv values
        Scalar upperThreshold = new Scalar(20, 255, 255); // skin color – higher hsv values

        Imgproc.cvtColor(mRgba,mHsvMat,Imgproc.COLOR_RGB2HSV);
        Core.inRange(mHsvMat,lowerThreshold,upperThreshold,mMaskMat);

        org.opencv.core.Size s1 = new org.opencv.core.Size(5,5);
        org.opencv.core.Size s2 = new org.opencv.core.Size(3,3);
        org.opencv.core.Point p = new org.opencv.core.Point();
        Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,s1, p);

        Imgproc.erode(mMaskMat,mMaskMat,new Mat());
        Imgproc.dilate(mMaskMat,mMaskMat,new Mat());
        Imgproc.GaussianBlur(mMaskMat,mMaskMat,s2,1,1);

        Core.bitwise_and(mRgba,mRgba,mDilatedMat,mMaskMat);

        // Black and White
        Imgproc.threshold(mDilatedMat,mDilatedMat,20,250,Imgproc.THRESH_BINARY);

        Imgproc.cvtColor(mDilatedMat,mDilatedMat,Imgproc.COLOR_RGB2GRAY);

        final List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mDilatedMat,contours,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);

        Imgproc.cvtColor(mDilatedMat,mDilatedMat,Imgproc.COLOR_GRAY2RGB);



        double maxArea = 0;
        MatOfPoint max_contour = new MatOfPoint();
        Iterator<MatOfPoint> iterator = contours.iterator();

        while (iterator.hasNext()){
            MatOfPoint contour = iterator.next();

            double area = Imgproc.contourArea(contour);
            if(area > maxArea){
                maxArea = area;
                max_contour = contour;

            }
        }


        MatOfInt hull = new MatOfInt();
        //vector< vector <org.opencv.core.Point> >hull( contours.size() );
        Imgproc.convexHull(max_contour,hull,false);
        MatOfPoint mopOut = new MatOfPoint();
        mopOut.create((int)hull.size().height,1,CvType.CV_32SC2);

        for(int i = 0; i < hull.size().height ; i++)
        {
            int index = (int)hull.get(i, 0)[0];
            double[] point = new double[] {
                    max_contour.get(index, 0)[0], max_contour.get(index, 0)[1]
            };
            mopOut.put(i, 0, point);
        }

        Imgproc.drawContours(mDilatedMat,contours,-1,colorGreen,iLineThickness);




        org.opencv.core.Rect rect = Imgproc.boundingRect(max_contour);

        // mDilatedMat = mDilatedMat.submat(rect);

        //Imgproc.cvtColor(getHandContour,getHandContour,Imgproc.COLOR_GRAY2RGB);
        Imgproc.rectangle(mDilatedMat, rect.tl(), rect.br(), new Scalar(0, 255, 0), 2);


        //Imgproc.drawContours(getHandContour,contours,-1,colorGreen,iLineThickness);



        return mDilatedMat; // This function must return
    }
}
