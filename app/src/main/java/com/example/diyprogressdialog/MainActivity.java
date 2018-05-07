package com.example.diyprogressdialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    AlertDialog progressDialog;
    private ProgressBar progressBar;
    private TextView progress_number;
    private TextView progress_percent;
    private String mProgressNumberFormat;
    private NumberFormat mProgressPercentFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.create_dialog);
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                showProgressDialog();
            }
        });
    }

    private void initFormats() {
        mProgressNumberFormat = "%1d/%2d";
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("HandlerLeak")
    public void showProgressDialog() {
        final View progressView = initProgressView();

            /*
            TextView title = new TextView(context);
            title.setText("有新版本啦~");
            title.setTextSize(20);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(getColor(R.color.colorAccent));
            */

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("有新版本啦~");
        //            builder.setCustomTitle(title);
        builder.setIcon(R.mipmap.ic_launcher_round);
        builder.setMessage("这是内容");
        builder.setView(progressView);
        builder.setPositiveButton("确定", null);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        progressDialog = builder.create();
        progressDialog.show();
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(progressDialog);
            Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
            mTitle.setAccessible(true);
            TextView mTitleView = (TextView) mTitle.get(mAlertController);
            mTitleView.setTextColor(getColor(R.color.colorAccent));
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setGravity(Gravity.CENTER);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        progressDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        progressDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressView.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    int progress = 0;

                    @Override
                    public void run() {
                        while (progress <= 100) {
                            progressBar.setProgress(progress);
                            handler.sendEmptyMessage(0);
                            if (progress == 100) {
                                //progressDialog.dismiss();
                            }
                            try {
                                Thread.sleep(35);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            progress++;
                        }
                    }
                }).start();
            }
        });

        updateProgressView(progressView);
    }

    @NonNull
    public View initProgressView() {
        initFormats();
        LayoutInflater inflater = LayoutInflater.from(this);
        final View progressView = inflater.inflate(R.layout.progress_layout, null);
        progressBar = progressView.findViewById(R.id.progress_bar);
        progress_number = progressView.findViewById(R.id.progress_number);
        progress_percent = progressView.findViewById(R.id.progress_percent);
        final String format = mProgressNumberFormat;
        progress_number.setText(String.format(format, 0, 100));
        SpannableString tmp = new SpannableString(mProgressPercentFormat.format(0));
        tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        progress_percent.setText(tmp);
        progressView.setVisibility(View.INVISIBLE);
        return progressView;
    }

    @SuppressLint("HandlerLeak")
    private void updateProgressView(View view) {
        final String format = mProgressNumberFormat;
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                    /* Update the number and percent */
                int progress = progressBar.getProgress();
                int max = progressBar.getMax();
                if (mProgressNumberFormat != null) {
                    progress_number.setText(String.format(format, progress, max));
                } else {
                    progress_number.setText("");
                }
                if (mProgressPercentFormat != null) {
                    double percent = (double) progress / (double) max;
                    SpannableString tmp1 = new SpannableString(mProgressPercentFormat.format(percent));
                    tmp1.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                            0, tmp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    progress_percent.setText(tmp1);
                } else {
                    progress_percent.setText("");
                }
                if (progress == progressBar.getMax()) {
                    //progressDialog.dismiss();
                }
            }
        };
    }
}
