package com.example.helloapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private ImageView cardImageView;
    private boolean isFrontShowing = false; // 标记当前显示的是否是正面

    // 用于正面和背面动画的ObjectAnimator
    private ObjectAnimator flipOutAnimator;
    private ObjectAnimator flipInAnimator;

    private ImageView rotatingImageView;
    private Button rotateButton;

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
        rotateButton = findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startRotationFadeOutAnimation(v);
             }
         });
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

    public void startRotationFadeOutAnimation(View view) {
        // 重置 ImageView 的状态，以便每次点击按钮都能看到完整动画
        rotatingImageView.setRotation(0f);
        rotatingImageView.setScaleX(1f);
        rotatingImageView.setScaleY(1f);
        rotatingImageView.setAlpha(1f);
        rotatingImageView.setVisibility(View.VISIBLE);

        // 使用 ViewPropertyAnimator 创建组合动画
        rotatingImageView.animate()
                .rotation(360f)      // 旋转360度
                .scaleX(0f)          // 水平缩放到0
                .scaleY(0f)          // 垂直缩放到0
                .alpha(0f)           // 透明度缩放到0 (完全透明)
                .setDuration(1000)   // 动画持续1秒
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        // 动画结束后，将 ImageView 隐藏起来
                        rotatingImageView.setVisibility(View.GONE);
                    }
                });
    }
}
