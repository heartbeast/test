package com.yao.memorytrain;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FlipCardGameActivity extends AppCompatActivity {
    public static final String TAG = "MemoryTrain";

    private GridLayout gameGridLayout;
    private TextView tvGameTime;
    private TextView tvClickCount;
    private Button btnRestart;
    private Button btnSettings;

    private int columnCount = 3; // 默认列数
    private int rowCount = 2; // 默认行数
    private int totalCards = columnCount * rowCount;

//    private List<CardView> cardViews; // 所有卡片视图
    private List<Integer> cardImageIds; // 卡片正面图片资源ID列表
    private List<Integer> selectedImageIds; // 每一轮游戏被选择的卡片

    private CardView firstFlippedCard = null;
    private CardView secondFlippedCard = null;

    private int matchedPairsCount = 0;
    private int clickCounter = 0;
    private boolean isAnimating = false;    // 是否正在动画中

    private Handler timerHandler = new Handler();
    private long startTime = 0L;
    private long gameStartTime;
    private Runnable updateTimerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsedMillis = SystemClock.uptimeMillis() - startTime;
            int seconds = (int) (elapsedMillis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            tvGameTime.setText(String.format("时间: %02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 1000); // 每秒更新一次
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flipcard);

        gameGridLayout = findViewById(R.id.game_grid_layout);
        tvGameTime = findViewById(R.id.tv_game_time);
        tvClickCount = findViewById(R.id.tv_click_count);
        btnRestart = findViewById(R.id.btn_restart);
        btnSettings = findViewById(R.id.btn_settings);

        btnRestart.setOnClickListener(v -> startGame());
        btnSettings.setOnClickListener(v -> showSettingsDialog());

        initCardImageResources(); // 初始化卡片正面图片资源
        startGame();
    }


    // 初始化所有可能的卡片正面图片资源
    private void initCardImageResources() {
        cardImageIds = Arrays.asList(
                R.drawable.card_front_ace1, R.drawable.card_front_ace2, R.drawable.card_front_21,R.drawable.card_front_22,
                R.drawable.card_front_31,R.drawable.card_front_32,R.drawable.card_front_41, R.drawable.card_front_42,
                R.drawable.card_front_51, R.drawable.card_front_52,R.drawable.card_front_61,R.drawable.card_front_62,
                R.drawable.card_front_jack1, R.drawable.card_front_jack2,
                R.drawable.card_front_queen1,R.drawable.card_front_queen2,
                R.drawable.card_front_king1,R.drawable.card_front_king2
        );
    }

    private void startGame() {
        timerHandler.removeCallbacks(updateTimerRunnable); // 停止之前的计时器
        startTime = SystemClock.uptimeMillis();
        timerHandler.post(updateTimerRunnable); // 启动新计时器

        matchedPairsCount = 0;
        clickCounter = 0;
        tvClickCount.setText("点击次数: 0");

        gameStartTime = SystemClock.elapsedRealtime();

        initGridLayout();
        initCardList();
        initCardsView();
    }

    private void initGridLayout() {
        gameGridLayout.removeAllViews(); // 清除之前的卡片
        gameGridLayout.setColumnCount(columnCount);
        gameGridLayout.setRowCount(rowCount);
    }
    private void initCardList() {
        selectedImageIds = new ArrayList<>();
        // 随机选择所需的图片ID
        Collections.shuffle(cardImageIds);
        for (int i = 0; i < totalCards / 2; i++) {
            selectedImageIds.add(cardImageIds.get(i));
            selectedImageIds.add(cardImageIds.get(i)); // 每张图片添加两次，形成一对
        }
        Collections.shuffle(selectedImageIds); // 打乱所有卡片的位置

//        for (int i = 0; i < totalCards; i++) {
//            Logd("selectedImageIds=" + selectedImageIds.get(i));
//        }
    }

    private void initCardsView() {
        int cardWidth = (int) (getResources().getDisplayMetrics().widthPixels / columnCount * 0.9);
        int cardHeight = (int) (getResources().getDisplayMetrics().heightPixels / (rowCount + 1) * 0.8);
        Logd("Width="+getResources().getDisplayMetrics().widthPixels+", Height="+getResources().getDisplayMetrics().heightPixels );
        Logd("cardWidth="+cardWidth+", cardHeight="+cardHeight);

        // 创建CardView实例并设置图片ID
        for (int i = 0; i < totalCards; i++) {
            CardView card = new CardView(this);
            card.setCard(selectedImageIds.get(i), selectedImageIds.get(i), R.drawable.card_back);

            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = cardWidth;
            layoutParams.height = cardHeight;
            layoutParams.setMargins(8, 4, 8, 4);
            card.setLayoutParams(layoutParams);
            gameGridLayout.addView(card);

            card.setOnClickListener(v -> {
                Logd("card clicked, id=" + card.getCardId());
                if (isAnimating || card.isFront()) {
                    return; // 正在动画或已翻开的卡片不响应点击。已配对会变成disable状态，不会有点击事件
                }
                isAnimating = true;

                clickCounter++;
                tvClickCount.setText("点击次数: " + clickCounter);

                card.flipCard(); // 翻开卡片

                if (firstFlippedCard == null) {
                    firstFlippedCard = card;
                    card.postDelayed(() -> {
                        isAnimating = false;
                    }, 1000);  // 此处是为了等待翻牌动画结束
                } else {
                    secondFlippedCard = card;
                    Logd("wait animation...");
                    card.postDelayed(() -> {
                        Logd("animation end...");
                        verifyMatch();
                    }, 800);  // 此处是为了等待翻牌动画结束
                }
            });
        }
    }

    private void verifyMatch() {
        Logd("id1="+firstFlippedCard.getCardId()+", id2="+secondFlippedCard.getCardId());
        if (firstFlippedCard.getCardId() == secondFlippedCard.getCardId()) { // 匹配成功
            Logd(" ==matched==");
            firstFlippedCard.vanishCard();
            secondFlippedCard.vanishCard();
            checkGameOver();
        } else { // 匹配失败，等待一段时间后翻回
            Logd(" ==no match,flip back==");
            firstFlippedCard.flipBack();
            secondFlippedCard.flipBack();
        }
        new Handler().postDelayed(() -> {
            firstFlippedCard = null;
            secondFlippedCard = null;
            isAnimating = false;
        }, 1000); // 延迟一会翻回
    }
    public void checkGameOver() {
        matchedPairsCount++;
        // 检查是否所有卡片都已配对
        if (matchedPairsCount * 2 == totalCards) { // 游戏结束
            timerHandler.removeCallbacks(updateTimerRunnable); // 停止计时器
            Toast.makeText(this, "恭喜，所有卡片已配对！点击次数: " + clickCounter + ", 用时: " + tvGameTime.getText().toString().replace("时间: ", ""), Toast.LENGTH_LONG).show();
            // 可以在这里显示游戏结束对话框
        }

        long finalTime = SystemClock.elapsedRealtime() - gameStartTime;
        Logd("Finishing game. Time: " + (finalTime / 1000));
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_settings, null);
        builder.setView(dialogView);

        final RadioGroup radioGroupLevel = dialogView.findViewById(R.id.radio_group_level);
        Button btnConfirm = dialogView.findViewById(R.id.btn_dialog_confirm);

        // 根据当前难度级别设置RadioButton选中状态
        if (columnCount == 3 && rowCount == 2) {
            radioGroupLevel.check(R.id.rb_level_easy);
        } else if (columnCount == 4 && rowCount == 3) {
            radioGroupLevel.check(R.id.rb_level_medium);
        } else if (columnCount == 5 && rowCount == 4) {
            radioGroupLevel.check(R.id.rb_level_hard);
        } else if (columnCount == 6 && rowCount == 5) {
            radioGroupLevel.check(R.id.rb_level_expert);
        }

        final AlertDialog dialog = builder.create();

        btnConfirm.setOnClickListener(v -> {
            int selectedId = radioGroupLevel.getCheckedRadioButtonId();
            if (selectedId == R.id.rb_level_easy) {
                columnCount = 3;
                rowCount = 2;
            } else if (selectedId == R.id.rb_level_medium) {
                columnCount = 4;
                rowCount = 3;
            } else if (selectedId == R.id.rb_level_hard) {
                columnCount = 5;
                rowCount = 4;
            } else if (selectedId == R.id.rb_level_expert) {
                columnCount = 6;
                rowCount = 5;
            }
            totalCards = columnCount * rowCount;

            // 检查总卡片数是否为偶数
            if (totalCards % 2 != 0) {
                Toast.makeText(this, "选择的级别总卡片数必须为偶数！", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();

            Logd("new gridSizeX="+columnCount+",gridSizeY="+rowCount);
            startGame(); // 刷新游戏
        });

        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(updateTimerRunnable); // 页面不可见时停止计时器
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (startTime != 0L && matchedPairsCount * 2 < totalCards) { // 游戏进行中才恢复计时器
            timerHandler.post(updateTimerRunnable);
        }
    }
    void Logd(String msg) {
        Log.d(TAG, msg);
    }
    void Loge(String msg) {
        Log.e(TAG, msg);
    }

}
