package com.ryu.helpyourchoice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

public class MainActivity extends AppCompatActivity {
    LinearLayout rouletteLayout,mainLayout;
    static int[] color_Light;       //LightMode 룰렛 색상
    static int[] color_Dark;        //DarkMode 룰렛 색상
    Button startBtn,historyBtn,settingBtn,howToUseBtn;
    private Roulette mRoulette;     //룰렛 커스텀 뷰 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rouletteLayout = (LinearLayout) findViewById(R.id.li_layout);
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        startBtn = (Button) findViewById(R.id.startBtn);
        historyBtn = (Button) findViewById(R.id.HistoryBtn);
        settingBtn = (Button) findViewById(R.id.SettingBtn);
        howToUseBtn = (Button) findViewById(R.id.HowToUseBtn);

        //SettingActivity 클래스의 정보를 저장하는 공유프레퍼런스,에디터 설정
        SettingActivity.pref = getSharedPreferences("setting", Activity.MODE_PRIVATE);
        SettingActivity.editor = SettingActivity.pref.edit();

        //HistoryActivity 클래스의 정보를 저장하는 공유프레퍼런스,에디터 설정
        HistoryActivity.pref = getSharedPreferences("history",Activity.MODE_PRIVATE);
        HistoryActivity.editor = HistoryActivity.pref.edit();


        setColor_Light();           //LightMode 룰렛 색상 설정
        setColor_Dark();            //DarkMode 룰렛 색상 설정
        setBGM();                   //배경음악 설정

        //커스텀 뷰 객체 생성
        mRoulette = new Roulette(MainActivity.this,rouletteLayout,new float[]{1,1,1,1,1,1,1,1},8);
        setRouletteColor();         //룰렛 색상 설정
        rouletteLayout.addView(mRoulette);      //레이아웃에 커스텀 뷰 추가
    }
    protected void onPause(){
        super.onPause();
    }
    protected void onResume(){
        super.onResume();
        setBackgroundColor();
        setRouletteColor();
    }

    //배경음악 설정
    protected void setBGM(){
        if((SettingActivity.pref!=null)&&(SettingActivity.pref.contains("bgm"))){
            if(SettingActivity.pref.getBoolean("bgm",false)){
                startService(new Intent(this,MusicService.class));
            }
            else
                stopService(new Intent(this,MusicService.class));
        }
    }
    //배경 다크모드 설정
    protected void setBackgroundColor(){
        if((SettingActivity.pref!=null)&&(SettingActivity.pref.contains("background"))){
            if(SettingActivity.pref.getBoolean("background",false)){
                mainLayout.setBackgroundColor(getColor(R.color.Background_Dark));
            }
            else
                mainLayout.setBackgroundColor(getColor(R.color.white));
        }
    }
    //룰렛 다크모드 설정
    protected void setRouletteColor(){
        if((SettingActivity.pref!=null)&&(SettingActivity.pref.contains("roulette"))){
            if(SettingActivity.pref.getBoolean("roulette",false)){
                mRoulette.setColors(color_Dark);
            }
            else
                mRoulette.setColors(color_Light);
        }
        else                 //초기값은 LightMode로
            mRoulette.setColors(color_Light);

    }

    //start버튼 클릭 이벤트(팝업 메뉴)
    public void popUpClick(View view){
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.start_menu,popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(MainActivity.this,StartActivity.class);
                switch(menuItem.getItemId()){
                    case R.id.normalMenu:       //동일 비율 룰렛 클릭
                        StartActivity.isRating = false;     //사용자 설정 비율이 아님을 저장
                        break;
                    case R.id.ratingMenu:       //사용자 설정 비율 클릭
                        StartActivity.isRating = true;      //사용자 설정 비율임을 저장
                        break;
                }
                startActivity(intent);      //화면 전환
                return true;
            }
        });
        popup.show();
    }

    //클릭 이벤트
    public void onClick(View button){
        Intent intent;
        switch(button.getId()){
            case R.id.HistoryBtn:               //HistoryActivity로 화면 이동
                intent = new Intent(this,HistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.SettingBtn:               //SettingActivity로 화면 이동
                intent = new Intent(this,SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.HowToUseBtn:              //HowToUseActivity로 화면 이동
                intent = new Intent(this,HowToUseActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    //LightMode 룰렛 색상
    protected void setColor_Light(){
        color_Light=new int[]{getColor(R.color.Light_1),getColor(R.color.Light_2),getColor(R.color.Light_3),getColor(R.color.Light_4),
                getColor(R.color.Light_5),getColor(R.color.Light_6),getColor(R.color.Light_7),getColor(R.color.Light_8)};
    }

    //DarkMode 룰렛 색상
    protected void setColor_Dark(){
        color_Dark=new int[]{getColor(R.color.Dark_1),getColor(R.color.Dark_2),getColor(R.color.Dark_3),getColor(R.color.Dark_4),
                getColor(R.color.Dark_5),getColor(R.color.Dark_6),getColor(R.color.Dark_7),getColor(R.color.Dark_8)};
    }
}