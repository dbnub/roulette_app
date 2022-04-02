package com.ryu.helpyourchoice;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SettingActivity extends AppCompatActivity {
    LinearLayout settingLayout;
    Button exitBtn;
    TextView soundText,rouletteText,backgroundText;
    ToggleButton soundBtn,backgroundBtn,rouletteBtn;
    private boolean soundON,rouletteON,backgroundON;
    static SharedPreferences pref;              //공유 프레퍼런스 변수
    static SharedPreferences.Editor editor;     //Editor 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        soundON = false;
        rouletteON = false;
        backgroundON = false;

        setTextView();
        setBtn();
        setLayout();
    }

    //TextView 연결
    protected void setTextView(){
        soundText = (TextView) findViewById(R.id.soundOption);
        rouletteText = (TextView) findViewById(R.id.rouletteOption);
        backgroundText = (TextView) findViewById(R.id.backgroundOption);
    }

    //버튼 연결
    private void setBtn(){
        exitBtn = (Button) findViewById(R.id.exitBtn_setting);
        soundBtn = (ToggleButton) findViewById(R.id.soundTBtn);
        backgroundBtn = (ToggleButton) findViewById(R.id.BGColorTBtn);
        rouletteBtn = (ToggleButton) findViewById(R.id.rouletteColorTBtn);
    }

    //레이아웃 연결
    private void setLayout(){
        settingLayout = (LinearLayout) findViewById(R.id.settingLayout);
    }

    protected void onPause(){
        super.onPause();
        saveState();
    }

    protected void onResume(){
        super.onResume();
        restoreState();
        //버튼 상태 재설정
        soundBtn.setChecked(soundON);
        rouletteBtn.setChecked(rouletteON);
        backgroundBtn.setChecked(backgroundON);
        //상태 재설정
        setBackground(backgroundON);
        setBGM(soundON);
    }
    protected void restoreState(){          //저장된 상태 불러오기
        if((pref!=null)&&(pref.contains("bgm"))&&(pref.contains("roulette"))&&
                (pref.contains("background"))){
            //저장했던 상태 불러오기
            soundON = pref.getBoolean("bgm",false);
            rouletteON = pref.getBoolean("roulette",false);
            backgroundON = pref.getBoolean("background",false);
        }
    }
    protected void saveState(){             //상태 저장하기
        editor.putBoolean("bgm",soundBtn.isChecked());          //배경음악 ON/OFF 상태 입력
        editor.putBoolean("roulette",rouletteBtn.isChecked());  //룰렛 DarkMode ON/OFF 상태 입력
        editor.putBoolean("background",backgroundBtn.isChecked());  //배경 DarkMode ON/OFF 상태 입력
        editor.commit();                //데이터 저장
    }

    public void onClick(View view){
        switch(view.getId()){
            case R.id.exitBtn_setting:
                finish();
                break;
            case R.id.soundTBtn:
                Toast.makeText(getApplicationContext(), "BGM " + soundBtn.getText(), Toast.LENGTH_SHORT).show();
                setBGM(soundBtn.isChecked());
                break;
            case R.id.BGColorTBtn:
                Toast.makeText(getApplicationContext(), "background Dark Mode " + backgroundBtn.getText(), Toast.LENGTH_SHORT).show();
                setBackground(backgroundBtn.isChecked());
                break;
            case R.id.rouletteColorTBtn:
                Toast.makeText(getApplicationContext(), "roulette Dark Mode " + rouletteBtn.getText(), Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    //배경 다크모드 설정
    private void setBackground(boolean isDark){
        if(isDark){
            //배경에 맞는 텍스트 색상 설정
            soundText.setTextColor(getColor(R.color.white));
            rouletteText.setTextColor(getColor(R.color.white));
            backgroundText.setTextColor(getColor(R.color.white));
            //레이아웃 배경색상 설정
            settingLayout.setBackgroundColor(getColor(R.color.Background_Dark));
        }
        else{
            //배경에 맞는 텍스트 색상 설정
            soundText.setTextColor(getColor(R.color.default_text_color));
            rouletteText.setTextColor(getColor(R.color.default_text_color));
            backgroundText.setTextColor(getColor(R.color.default_text_color));
            //레이아웃 배경색상 설정
            settingLayout.setBackgroundColor(getColor(R.color.white));
        }

    }

    //배경음악 설정
    private void setBGM(boolean isON){
        if(isON){           //음악 재생 서비스 실행
            startService(new Intent(this,MusicService.class));
        }
        else{               //음악 재생 서비스 중지
            stopService(new Intent(this,MusicService.class));
        }
    }
}