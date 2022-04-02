package com.ryu.helpyourchoice;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

public class Roulette extends View {
    private float[] rates;              //룰렛 비율 배열
    private String[] choice;            //선택지 문자열 배열
    private int[] COLORS;               //룰렛 색상 배열
    private Animation anim;             //애니메이션 변수
    private Runnable runnable;          //Runnable 변수
    private Handler handler;            //Handler 변수
    private LinearLayout layout;        //표시될 레이아웃 변수
    private Paint paint;                //Paint 변수
    boolean isSpin=false;               //룰렛 돌릴지 말지 판별하는 boolean 변수
    int degree, choiceNum;              //회전 각도, 룰렛 선택지 개수 변수

    public Roulette(Context context, LinearLayout layout, float[] rates, int num){      //생성자
        super(context);
        //룰렛 초기화
        this.layout = layout;
        degree=0;
        choiceNum = num;
        setRate(rates);
    }

    //선택지 개수 설정
    protected void setChoiceNum(int num){
        choiceNum = num;
    }

    //룰렛 비율 설정
    protected void setRate(float[] userRates){
        rates = new float[choiceNum];
        float total=0;
        for(int i=0;i<rates.length;i++){
            total+=userRates[i];
        }
        for(int i=0;i<rates.length;i++){
            rates[i]=360f*(userRates[i]/total);
        }
    }
    //룰렛 색상 설정
    protected void setColors(int[] newColors){
        COLORS = newColors.clone();
    }
    //선택지 문자열 설정
    protected void setString(String[] userStr){
        this.choice = userStr.clone();
    }
    //랜덤 회전값 설정
    private int setRandomDegree(){
        Random random = new Random();
        this.degree = 7200+random.nextInt(360)+1;
        return this.degree;
    }
    //룰렛 돌리기 시작
    protected void startSpin(){
        this.isSpin = true;
    }

    //결과를 표시해주는 메소드
    private void showResult(TextView resultText, float[] rates, String[] txt){
        //순수 random을 이용해 얻은 값
        int moveDegree = this.degree-7200;
        //안드로이드에서 원을 그리면 270도가 12시 방향
        //12시 방향을 가리키고 있는 부분의 각도 구하기
        float resultAngle = (moveDegree>270)?360-(float)moveDegree+270:270-(float)moveDegree;
        float eachAngle = 0f;
        //resultAngle과 비교하며 결과값 구하기
        for(int i=0; i<rates.length; i++){
            eachAngle+=rates[i];
            if(eachAngle>=resultAngle){
                resultText.setText("[결과] : "+txt[i]);
                return;
            }
        }
    }

    //스레드를 이용하여 버튼 활성화 및 결과 텍스트 설정
    protected void getResult(TextView txt, Button startBtn){
        runnable = new Runnable() {
            @Override
            public void run() {
                showResult(txt,rates,choice);       //결과 표시
                startBtn.setEnabled(true);          //버튼을 다시 클릭 가능하게 바꿈
            }
        };
        handler = new Handler();                    //핸들러 객체 할당
        txt.setText("[결과] : ");                    //텍스트 설정
        startBtn.setEnabled(false);                 //spin하는 동안 버튼 비활성화
        handler.postDelayed(runnable,3000);     //룰렛 멈추기 전까지 delay후에 runnable
    }

    //애니메이션 생성 및 동작 코드
    protected void createAnimation(){
        //뷰를 회전시켜주는 animation 객체 할당
        anim=new RotateAnimation(0,setRandomDegree(),Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        anim.setDuration(3000);     //회전하는 시간
        anim.setFillEnabled(true);  // animation 종료 후, 뷰의 위치를 유지하는 것을 컨트롤 할것인지 입력
        anim.setFillAfter(true);    //animation 종료 후, 뷰의 위치를 유지할 것인지 입력
        startAnimation(anim);       //animation start
    }
    //룰렛 그리기
    protected void onDraw(Canvas canvas) {
        final int width = layout.getWidth();        //레이아웃 너비
        final int height = layout.getHeight();      //레이아웃 높이
        final int radius = width/5*2;               //룰렛의 반지름
        float temp = 0;

        //룰렛을 그릴 사각형 설정
        RectF rectf = new RectF(width/2f-radius,height/2f-radius,
                width/2f+radius,height/2f+radius);

        //paint 설정
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);

        //부채꼴 이어 그리기
        for (int i = 0; i < rates.length; i++) {
            if (i > 0)
                temp += rates[i - 1];
            paint.setColor(COLORS[i]);      //paint color 설정
            canvas.drawArc(rectf,temp,rates[i],true,paint);     //부채꼴 그리기

            //애니메이션 실행
            if(isSpin){
                createAnimation();      //애니메이션 생성 및 동작 메소드 호출
                isSpin=false;       //isSpin값 초기화
            }
        }
    }
}

