package com.yao.memorytrain;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

// 自定义CardView，继承自AppCompatImageView，封装了翻牌动画
public class CardView extends AppCompatImageView {
    private static final String TAG = FlipCardGameActivity.TAG;

    private int cardId; // 用于判断是否匹配的唯一ID
    private int frontImageResId;
    private int backImageResId;
    public boolean isFrontShowing = false; // 当前是否显示正面
//    public boolean isMatched = false;      // 是否已配对
    public boolean isAnimating = false;    // 是否正在动画中

    private ObjectAnimator flipOutAnimator;
    private ObjectAnimator flipInAnimator;
    private ObjectAnimator vanishAnimator; // 消失动画

    // 监听器接口，用于通知外部动画完成
    public interface CardAnimationListener {
        void onFlipAnimationEnd(CardView cardView);
        void onVanishAnimationEnd(CardView cardView);
    }

    private CardAnimationListener animationListener;

    public void setCardAnimationListener(CardAnimationListener listener) {
        this.animationListener = listener;
    }

    public CardView(Context context) {
        super(context);
        init();
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化翻牌动画
        setupFlipAnimations();
        // 初始化消失动画
        setupVanishAnimation();
        // 默认显示背面
        setImageResource(R.drawable.card_back); // 确保你有这个资源
    }

    public void setCard(int cardId, int frontImageResId, int backImageResId) {
        Logd("CardView.setCard: cardId=" + cardId + ", frontResId="+frontImageResId+", backResId="+backImageResId);
        this.cardId = cardId;
        this.frontImageResId = frontImageResId;
        this.backImageResId = backImageResId;
        this.isFrontShowing = false; // 初始化为背面
//        this.isMatched = false;      // 初始化为未配对
        this.isAnimating = false;
        // 确保视图在设置新卡片时重置状态
        this.setScaleX(1f);
        this.setScaleY(1f);
        this.setAlpha(1f);
        this.setRotationY(0f);
        setImageResource(backImageResId);
        setVisibility(VISIBLE);
    }

    private void setupFlipAnimations() {
        flipOutAnimator = ObjectAnimator.ofFloat(this, "rotationY", 0f, 90f);
        flipOutAnimator.setDuration(200);
        flipOutAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        flipOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isFrontShowing = !isFrontShowing;
                setImageResource(isFrontShowing ? frontImageResId : backImageResId);
                flipInAnimator.start();
            }
        });

        flipInAnimator = ObjectAnimator.ofFloat(this, "rotationY", -90f, 0f);
        flipInAnimator.setDuration(200);
        flipInAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        flipInAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                if (animationListener != null) {
                    animationListener.onFlipAnimationEnd(CardView.this);
                }
            }
        });
    }

    private void setupVanishAnimation() {
        vanishAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this,
                PropertyValuesHolder.ofFloat( View.ROTATION, 0f, 360f),
                PropertyValuesHolder.ofFloat("scaleX", 1f, 0f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 0f),
                PropertyValuesHolder.ofFloat("alpha", 1f, 0f)
        );
        vanishAnimator.setDuration(1000);
        vanishAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                setVisibility(INVISIBLE); // 动画结束后隐藏
                if (animationListener != null) {
                    animationListener.onVanishAnimationEnd(CardView.this);
                }
            }
        });
    }


    public void flipCard() {
        if (isAnimating) {
            return;
        }

        isAnimating = true; // 开始动画时设为true
        if (isFrontShowing) {
            flipOutAnimator.setFloatValues(0f, 90f);
            flipInAnimator.setFloatValues(-90f, 0f);
        } else {
            flipOutAnimator.setFloatValues(0f, -90f);
            flipInAnimator.setFloatValues(90f, 0f);
        }
        flipOutAnimator.start();
    }

    // 翻回背面（不触发监听器，用于不匹配时自动翻回）
    public void flipBack() {
        if (isAnimating || !isFrontShowing) {
            return;
        }
        isAnimating = true; // 开始动画时设为true
        flipOutAnimator.setFloatValues(0f, 90f);
        flipInAnimator.setFloatValues(-90f, 0f);
        flipOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isFrontShowing = !isFrontShowing;
                setImageResource(isFrontShowing ? frontImageResId : backImageResId);
                flipInAnimator.start();
            }
        });
        flipInAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false; // 动画结束时设为false
                setEnabled(true); // 不再响应点击
                // 这里不调用animationListener.onFlipAnimationEnd，因为是自动翻回
                // 移除监听器以防止对下一次正常的翻牌动画造成影响
//                flipOutAnimator.removeAllListeners();
//                flipInAnimator.removeAllListeners();
//                setupFlipAnimations(); // 重新设置默认监听器
            }
        });
        flipOutAnimator.start();
    }

    public void vanishCard() {
        if (isAnimating) {
            return;
        }
        setEnabled(false); // 不再响应点击
        vanishAnimator.start();
    }

    // Getters
    public int getCardId() {
        return cardId;
    }

    public boolean isFrontShowing() {
        return isFrontShowing;
    }

//    public boolean isMatched() {
//        return isMatched;
//    }

    public boolean isAnimating() {
        return isAnimating;
    }

    void Logd(String msg) {
        Log.d(TAG, msg);
    }
}
