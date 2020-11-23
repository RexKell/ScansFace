package com.scansface.mobile.scansface;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * author: rexkell
 * date: 2020/10/26
 * explain:
 * 人脸识别
 */
class ScansFaceView extends View {
    private  Paint paint;

    private RectF circleRect;
    //扫描框动画
    ValueAnimator scanceAnim;
    int circleRadius=350;
    float angle=0f;
    public boolean isLoading=false;
    Path topPath;
    Path bottomPath;
    int width;
    int height;

    public ScansFaceView(Context context) {
        this(context,null);
    }

    public ScansFaceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ScansFaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);

    }
    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(Color.parseColor("#ffffff"));
        paint.setStyle(Paint.Style.FILL);
         width=canvas.getWidth();
         height=canvas.getHeight();
        //上半部分白色区域
        if (topPath==null){
            topPath=new Path();
            topPath.moveTo(0,0);
            topPath.lineTo(width,0);
            topPath.lineTo(width,height/3);
            topPath.lineTo(width/2+circleRadius,height/3);
            topPath.addArc(width/2-circleRadius,height/3-circleRadius,width/2+circleRadius,height/3+circleRadius,0,-180);
            topPath.lineTo(0,height/3);
            topPath.lineTo(0,0);
        }
        canvas.drawPath(topPath,paint);
        if (bottomPath==null){
            bottomPath=new Path();
            bottomPath.moveTo(0,height/3);
            bottomPath.lineTo(0,height);
            bottomPath.lineTo(width,height);
            bottomPath.lineTo(width,height/3);
            bottomPath.lineTo(width/2+circleRadius,height/3);
            bottomPath.addArc(width/2-circleRadius,height/3-circleRadius,width/2+circleRadius,height/3+circleRadius,0,180);
            bottomPath.lineTo(0,height/3);
        }
        canvas.drawPath(bottomPath,paint);

        //下部分白色区域
        if (circleRect==null){
            circleRect=new RectF(width/2-circleRadius,height/3-circleRadius,width/2+circleRadius,height/3+circleRadius);
        }
        paint.setColor(Color.parseColor("#e3e3e3"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(width/2,height/3,circleRadius,paint);
        if (isLoading){
            if (scanceAnim==null||scanceAnim.isPaused()){
                scanceAnim= ValueAnimator.ofInt(0, 360);
                scanceAnim.setRepeatCount(ValueAnimator.INFINITE);
                scanceAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                scanceAnim.setDuration(5000);
                scanceAnim.addUpdateListener(animatorUpdateListener);
                scanceAnim.start();
            }
            //绘制圆弧
            paint.setColor(Color.parseColor("#6581EA"));
            canvas.drawArc(circleRect,-90,angle,false,paint);
        }
    }
    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener=new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            angle=(int)animation.getAnimatedValue();
            invalidate();
        }
    };
    public void startLoading(){
        this.isLoading=true;
        invalidate();
    }
    public void stopLoading(){
        this.isLoading=false;
        invalidate();
    }
    public Point getCenterPoint(){
        return new Point(getWidth()/2,getHeight()/3);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (scanceAnim!=null){
            scanceAnim.removeAllUpdateListeners();
            scanceAnim=null;
        }
    }
}
