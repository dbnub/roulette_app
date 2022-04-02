package com.ryu.helpyourchoice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HowToUseActivity extends AppCompatActivity {
    LinearLayout howToUseLayout;
    TextView txt1,txt2,txt3,txt4;
    Button exitBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_use);
        howToUseLayout = (LinearLayout) findViewById(R.id.howToUseLayout);
        txt1 = (TextView) findViewById(R.id.use_text1);
        txt2 = (TextView) findViewById(R.id.use_text2);
        txt3 = (TextView) findViewById(R.id.use_text3);
        txt4 = (TextView) findViewById(R.id.use_text4);
        exitBtn = (Button) findViewById(R.id.exitBtn_HowToUse);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    protected void onResume(){
        super.onResume();
        setBackgroundColor();
    }

    //배경 다크모드 설정
    protected void setBackgroundColor(){
        if((SettingActivity.pref!=null)&&(SettingActivity.pref.contains("background"))) {
            if (SettingActivity.pref.getBoolean("background", false)) {
                howToUseLayout.setBackgroundColor(getColor(R.color.Background_Dark));
                txt1.setTextColor(getColor(R.color.white));
                txt2.setTextColor(getColor(R.color.white));
                txt3.setTextColor(getColor(R.color.white));
                txt4.setTextColor(getColor(R.color.white));
            } else {
                howToUseLayout.setBackgroundColor(getColor(R.color.white));
                txt1.setTextColor(getColor(R.color.default_text_color));
                txt2.setTextColor(getColor(R.color.default_text_color));
                txt3.setTextColor(getColor(R.color.default_text_color));
                txt4.setTextColor(getColor(R.color.default_text_color));
            }
        }
    }
}