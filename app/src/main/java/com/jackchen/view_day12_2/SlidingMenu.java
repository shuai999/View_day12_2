package com.jackchen.view_day12_2;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.HorizontalScrollView;

/**
 * Email: 2185134304@qq.com
 * Created by JackChen 2018/3/20 14:52
 * Version 1.0
 * Params:
 * Description:  酷狗侧滑菜单效果
*/
public class SlidingMenu extends HorizontalScrollView {

    // 菜单的宽度
    private int mMenuWidth ;
    // 内容页、侧滑页面
    private View mContentView , mMenuView ;

    public SlidingMenu(Context context) {
        this(context,null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 获取自定义属性
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu);

        float rightMargin = array.getDimension(R.styleable.SlidingMenu_menuRightMargin, ScreenUtils.dip2px(context, 50));
        // 菜单的宽度 = 屏幕的宽度 - 右边的一小部分距离（自定义属性）
        mMenuWidth = (int) (getScreenWidth(context) - rightMargin);

        array.recycle();
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 这个方法是布局解析完毕后，也就是xml文件解析完毕后，调用这个方法

        // 获取LinearLayout 因为这两个 布局文件是用LinearLayout包裹的，所以先获取LinearLayout
        ViewGroup container = (ViewGroup) getChildAt(0);

        // 获取这个LinearLayout中包裹的子View的个数
        int childCount = container.getChildCount();
        if (childCount != 2){
            throw new RuntimeException("只能放置两个子View !") ;
        }

        // 1.  从container中 获取左边菜单部分
        mMenuView = container.getChildAt(0) ;
        // 设置布局参数
        ViewGroup.LayoutParams menuParams = mMenuView.getLayoutParams() ;
        menuParams.width = mMenuWidth ;
        // 7.0以下手机必须采用下面的方式设置布局参数
        mMenuView.setLayoutParams(menuParams);


        // 2.  从container中获取内容页
        mContentView = container.getChildAt(1) ;
        // 设置布局参数
        ViewGroup.LayoutParams contentParams = mContentView.getLayoutParams() ;
        contentParams.width = getScreenWidth(getContext()) ;
        mContentView.setLayoutParams(contentParams);


        // 初始化进来的时候需要菜单关闭，发现这个方法放到这里没用
//        scrollTo(mMenuWidth,0);
    }



    // onLayout() 方法是在 onFinishInflate()方法之后执行的，也在View的绘制流程之后执行的
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 初始化进来的时候需要菜单关闭，这个方法放到这里可以
        scrollTo(mMenuWidth,0);
    }



    // 手指抬起是二选一，要么打开、要么关闭
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        // 1. 获取手指滑动的速率，当大于一定值就认为是快速滑动，可以使用GestureDetecotr处理（系统提供好的类）
        // 2. 处理事件拦截 + ViewGroup 事件分发的源码实践
        // 当菜单打开的时候，手指触摸右边内容部分需要关闭菜单

        // 当手指抬起
        if (ev.getAction() == MotionEvent.ACTION_UP){
            int currentScrollX = getScrollX();
            if (currentScrollX > mMenuWidth/2){
                // 关闭菜单
                closeMenu();
            }else{
                // 打开菜单
                openMenu();
            }

            // 这里return true：确保super.onTouchEvent(ev)不会执行
            return true ;
        }


        return super.onTouchEvent(ev);

    }

    // 处理右边的缩放、左边的缩放和透明度，需要不断的获取当前滚动的位置
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.e("TAG", "l -> " + l);// 变化是 mMenuWidth - 0
        // 算一个梯度值
        float scale = 1f * l / mMenuWidth;// scale 变化是 1 - 0
        // 右边的缩放: 最小是 0.7f, 最大是 1f
        float rightScale = 0.7f + 0.3f * scale;
        // 设置右边的缩放,默认是以中心点缩放
        // 设置缩放的中心点位置
        ViewCompat.setPivotX(mContentView,0);
        ViewCompat.setPivotY(mContentView, mContentView.getMeasuredHeight() / 2);
        ViewCompat.setScaleX(mContentView,rightScale);
        ViewCompat.setScaleY(mContentView, rightScale);

        // 菜单的缩放和透明度
        // 透明度是 半透明到完全透明  0.5f - 1.0f
        float leftAlpha = 0.5f + (1-scale)*0.5f;
        ViewCompat.setAlpha(mMenuView,leftAlpha);
        // 缩放 0.7f - 1.0f
        float leftScale = 0.7f + (1-scale)*0.3f;
        ViewCompat.setScaleX(mMenuView,leftScale);
        ViewCompat.setScaleY(mMenuView, leftScale);

        // 最后一个效果 退出这个按钮刚开始是在右边，安装我们目前的方式永远都是在左边
        // 设置平移，先看一个抽屉效果
        // ViewCompat.setTranslationX(mMenuView,l);
        // 平移 l*0.7f
        ViewCompat.setTranslationX(mMenuView, 0.25f*l);

    }


    /**
     * 打开菜单：滚动到 0 的位置
     */
    private void openMenu(){
        smoothScrollTo(0,0);
    }


    /**
     * 关闭菜单：滚动到mMenuWidth 的位置
     */
    private void closeMenu(){
        smoothScrollTo(mMenuWidth ,0);
    }


    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    private int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * Dip into pixels
     */
    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
