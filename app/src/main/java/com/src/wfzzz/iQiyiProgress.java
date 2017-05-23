package com.src.wfzzz;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


/**
 * Created by WF on 2017/5/17.
 */

public class iQiyiProgress extends View {

    /**
     * 进度条画笔
     */
    private Paint mColorProgressPoint;
    /**
     * 进度条背景画笔
     */
    private Paint mBackProgressPoint;

    /**
     * 三角形画笔
     */
    private Paint mTrianglePoint;





    private int width;

    private int height;

    int colorProgress=0;

    int transProgress=0;

    int startAngle=-90;

    /**半径*/
    int mTriangRadius=10;

    /**
     * 画三角形坐标原点坐标
     */
    int mTriangX=0;
    int mTriangY=0;

    /**
     * 动画旋转
     */
    Matrix matrix;

    Bitmap mTriangBitmap;

    float triangDiameter;

    public iQiyiProgress(Context context) {
        this(context,null);
    }

    public iQiyiProgress(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public iQiyiProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.iQiyiProgress);
        int progressBackColor=typedArray.getColor(R.styleable.iQiyiProgress_progressBackground, Color.WHITE);
        int progressColor=typedArray.getColor(R.styleable.iQiyiProgress_progressColor, Color.GREEN);
        int triangColor=typedArray.getColor(R.styleable.iQiyiProgress_triangleColor, Color.GREEN);
        float progressWidth=typedArray.getDimensionPixelOffset(R.styleable.iQiyiProgress_progressWidth,(int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
        triangDiameter=typedArray.getDimensionPixelOffset(R.styleable.iQiyiProgress_triangDiameter,(int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()));

        matrix=new Matrix();


        mColorProgressPoint=new Paint();
        mColorProgressPoint.setColor(progressColor);
        mColorProgressPoint.setStrokeWidth(progressWidth);
        mColorProgressPoint.setStyle(Paint.Style.STROKE);
        mColorProgressPoint.setAntiAlias(true);

        mBackProgressPoint=new Paint();
        mBackProgressPoint.setColor(progressBackColor);
        mBackProgressPoint.setStrokeWidth(progressWidth+0.5f);
        mBackProgressPoint.setStyle(Paint.Style.STROKE);
        mBackProgressPoint.setAntiAlias(true);

        mTrianglePoint=new Paint();
        mTrianglePoint.setColor(triangColor);
        mTrianglePoint.setStyle(Paint.Style.FILL);
        thread.start();
    }


    /**
     * 初始化画笔
     */
    public void initView(){



    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetParams();
    }

    private void resetParams() {
        width = getWidth();
        height = getHeight();
        int min= Math.min(width,height);
        if(triangDiameter==0){
            mTriangBitmap= Bitmap.createBitmap(min-min/2,min-min/2, Bitmap.Config.ARGB_4444);//用于实现图片伸缩而建立的另一个图片对象
        }else{
            mTriangBitmap= Bitmap.createBitmap((int)triangDiameter,(int)triangDiameter, Bitmap.Config.ARGB_4444);//用于实现图片伸缩而建立的另一个图片对象

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    private int measure(int origin) {
        int result = 100;
        int specMode = MeasureSpec.getMode(origin);
        int specSize = MeasureSpec.getSize(origin);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float doughnutWidth = Math.min(width, height) / 2 * 0.15f;
        final RectF rectF = new RectF((width > height ? Math.abs(width - height) / 2 : 0) + doughnutWidth / 2, (height > width ? Math.abs(height - width) / 2 : 0) + doughnutWidth / 2, width - (width > height ? Math.abs(width - height) / 2 : 0) - doughnutWidth / 2, height - (height > width ? Math.abs(height - width) / 2 : 0) - doughnutWidth / 2);


        if(isback){
            canvas.drawArc(rectF, startAngle, colorProgress, false, mColorProgressPoint);
        }else{
            //final RectF rectFs = new RectF((width > height ? Math.abs(width - height) / 3 : 50) + doughnutWidth / 2, (height > width ? Math.abs(height - width) / 3 : 50) + doughnutWidth / 2, width - (width > height ? Math.abs(width - height) / 3 : 50) - doughnutWidth / 2, height - (height > width ? Math.abs(height - width) / 3 : 50) - doughnutWidth / 2);
            canvas.drawArc(rectF, startAngle, 360, false, mColorProgressPoint);
            canvas.drawArc(rectF, startAngle, transProgress, false, mBackProgressPoint);
        }
        Matrix matrix=new Matrix();//Android的矩阵变换类
        Canvas c=new Canvas(mTriangBitmap);
        /**
         * 画实心三角形
         */
        drawFillTriang(c);
        matrix.setRotate(transProgress,mTriangBitmap.getWidth()/2,mTriangBitmap.getHeight()/2);//实现旋转，其中degree为旋转的角度，width/2,height/2分别是旋转中心点的x，y坐标，可以自己设定，这里是博主设置的位置，之所以是图片自身的中点是因为这时的图片还在屏幕的左上角，自转的中心坐标就是自身的中心
        matrix.postTranslate(width/2-mTriangBitmap.getWidth()/2,height/2-mTriangBitmap.getHeight()/2);//实现平移，x，y后为平移的坐标，将自转后的图片平移至想要的位置
        canvas.drawBitmap(mTriangBitmap,matrix,new Paint());

    }


    /**
     * 画实心三角形
     */
    public void drawFillTriang(Canvas canvas){
        mTriangX=mTriangBitmap.getWidth()/2;
        mTriangY=mTriangBitmap.getHeight()/2;
        int origin=mTriangBitmap.getWidth() / 2;
        mTriangRadius=origin/4;
        Path path=new Path();

        path.moveTo(mTriangX/2,0);// 此点为多边形的起点
        path.lineTo(mTriangBitmap.getWidth(), mTriangY);
        path.lineTo(mTriangX/2,mTriangBitmap.getHeight());

        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, mTrianglePoint);
    }

    boolean isback=true;

    private Thread thread = new Thread(){
        @Override
        public void run() {
            while(true){
                try {
                    if(isback){
                        if(colorProgress<360){
                            colorProgress+=1;
                        }else{
                            isback=false;
                            colorProgress=0;
                        }
                    }else{
                        if(transProgress<360){
                            transProgress+=1;
                        }else {
                            isback=true;
                            transProgress=0;
                        }
                    }

                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                postInvalidate();
            }
        }
    };


}
