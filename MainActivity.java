package com.example.helloapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "HelloWorld";

    // 翻转动画
    private ImageView cardImageView;
    private boolean isFrontShowing = false; // 标记当前显示的是否是正面
    // 用于正面和背面动画的ObjectAnimator
    private ObjectAnimator flipOutAnimator;
    private ObjectAnimator flipInAnimator;

    // 旋转消失动画
    private ImageView rotatingImageView;
    ObjectAnimator rotateAnimator;
    private boolean isImageVisible = true; // 初始状态：图片是可见的
    private boolean isAnimating = false;   // 用于避免动画冲突

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardImageView = findViewById(R.id.card_image_view);

        // 设置透视距离。这个值越大，透视效果越不明显。
        // 一般设置为屏幕宽度或高度的几倍，这里使用设备DPI来计算，使之独立于屏幕密度。
        float scale = getResources().getDisplayMetrics().density;
        cardImageView.setCameraDistance(8000 * scale); // 8000 是一个经验值，可以调整

        // 初始化翻转动画
        loadAnimations();

        rotatingImageView = findViewById(R.id.fadeout_view);
        setupVanishAnimation();
        Logd("====onCreate======");
    }

    private void loadAnimations() {
        // 创建第一个动画：从当前角度旋转到90度 (牌面消失)
        flipOutAnimator = ObjectAnimator.ofFloat(cardImageView, "rotationY", 0f, 90f);
        flipOutAnimator.setDuration(200); // 动画时长
        flipOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 动画结束时，切换图片并开始第二个动画
                isFrontShowing = !isFrontShowing;
                cardImageView.setImageResource(isFrontShowing ? R.drawable.card_front : R.drawable.card_back);

                // 开始第二个动画：从-90度旋转到0度 (牌面出现)
                flipInAnimator.start();
            }
        });

        // 创建第二个动画：从-90度旋转到0度 (牌面出现)
        flipInAnimator = ObjectAnimator.ofFloat(cardImageView, "rotationY", -90f, 0f);
        flipInAnimator.setDuration(200); // 动画时长
    }

    public void flipCard(View view) {
        if (flipOutAnimator.isRunning() || flipInAnimator.isRunning()) {
            // 如果动画正在运行，则不响应点击，避免重复启动
            return;
        }

        // 根据当前显示状态调整第一个动画的起始和结束角度
        if (isFrontShowing) {
            // 当前是正面，翻转到背面：从0度旋转到90度
            flipOutAnimator.setFloatValues(0f, 90f);
            flipInAnimator.setFloatValues(-90f, 0f);
        } else {
            // 当前是背面，翻转到正面：从0度旋转到-90度
            flipOutAnimator.setFloatValues(0f, -90f);
            flipInAnimator.setFloatValues(90f, 0f); // 第二个动画从90度旋转到0度
        }

        flipOutAnimator.start(); // 启动第一个动画
    }

    private void setupVanishAnimation() {
        rotateAnimator = ObjectAnimator.ofPropertyValuesHolder(
                rotatingImageView,
                PropertyValuesHolder.ofFloat( View.ROTATION, 0f, 360f),
                PropertyValuesHolder.ofFloat("scaleX", 1f, 0f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 0f),
                PropertyValuesHolder.ofFloat("alpha", 1f, 0f)
        );

        // 设置动画持续时间
        rotateAnimator.setDuration(1000);
        Logd("rotateAnimator setup ok");
    }

    public void startRotationFadeOutAnimation(View view) {
        Logd("startRotationFadeOutAnimation enter");
        rotateAnimator.start();
//        // 如果当前有动画正在进行，则不响应点击，避免冲突
//        if (isAnimating) {
//            return;
//        }
//
//        isAnimating = true; // 动画即将开始
//
//        if (isImageVisible) {
//            // 当前是可见状态，执行“旋转消失”动画
//            rotatingImageView.animate()
//                    .rotation(360f)      // 旋转360度
//                    .scaleX(0f)          // 水平缩放到0
//                    .scaleY(0f)          // 垂直缩放到0
//                    .alpha(0f)           // 透明度缩放到0 (完全透明)
//                    .setDuration(1000)   // 动画持续1秒
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            super.onAnimationEnd(animation);
//                            rotatingImageView.setVisibility(View.INVISIBLE); // 消失但保留空间
//                            isImageVisible = false; // 更新状态为已消失
//                            isAnimating = false;    // 动画结束，重置标志
//                        }
//                    });
//        } else {
//            // 当前是消失状态，执行“旋转出现”动画
//
//            // 1. 确保 View 是可见的 (尽管仍然是透明且缩小的)
//            rotatingImageView.setVisibility(View.VISIBLE);
//
//            // 2. 关键步骤：瞬间将 View 的属性设置为它消失时的状态，作为“出现”动画的起点
//            rotatingImageView.setRotation(360f); // 从 360 度开始旋转（或 -360 度，只要是完全转过一圈即可）
//            rotatingImageView.setScaleX(0f);     // 从 0 缩放开始
//            rotatingImageView.setScaleY(0f);     // 从 0 缩放开始
//            rotatingImageView.setAlpha(0f);      // 从 0 透明度开始
//
//            Log.v(TAG, "before animate");
//            // 3. 开始动画，将其从消失状态逐渐恢复到正常显示状态
//            rotatingImageView.animate()
//                    .rotation(0f)        // 旋转回 0 度
//                    .scaleX(1f)          // 缩放回 1 (原始大小)
//                    .scaleY(1f)          // 缩放回 1 (原始大小)
//                    .alpha(1f)           // 透明度到 1 (完全不透明)
//                    .setDuration(1000)   // 动画持续1秒
//                    .setListener(new AnimatorListenerAdapter() { // 添加监听器以重置 isAnimating
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            Log.v(TAG, "onAnimationEnd");
//                            super.onAnimationEnd(animation);
//                            isImageVisible = true; // 更新状态为可见
//                            isAnimating = false;    // 动画结束，重置标志
//                        }
//                    });
//            Log.v(TAG, "after animate");
//        }


    }
    void Logd(String msg) {
        Log.d(TAG, msg);
    }
    void Loge(String msg) {
        Log.e(TAG, msg);
    }
}
