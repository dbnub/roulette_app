package com.ryu.helpyourchoice;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class StartActivity extends AppCompatActivity {
    LinearLayout rouletteLayout, startLayout;    //룰렛을 추가할 레이아웃, 전체 레이아웃
    private EditText[] userInputRate, userInputChoice;   //비율설정, 선택지 EditText 배열
    private View[] rouletteColorView;     //룰렛 색상을 표시할 뷰 배열
    private LinearLayout[] userInputLayout;   //scrollView 내부의 레이아웃 배열
    private TextView resultText;      //결과를 표시할 Text
    Button exitBtn,saveBtn,spinBtn,appendBtn,deleteBtn;  //각 버튼들
    private Roulette mRoulette;                     //룰렛 커스텀 뷰
    private Dialog setDataNameDialog;               //저장 데이터의 이름을 설정하는 커스텀 대화상자 객체
    private String[] userChoiceArray;               //user가 입력한 선택지 문자열 저장
    private float[] userRateArray;                  //user가 입력한 선택지별 비율 저장
    private int[] ratingID, choiceID,inputLayoutID, colorViewID;     //userInputRate[], userInputChoice[], userInputLayout[], rouletteColorView[]와 연결할 ID 배열
    final int MAX_CHOICE_NUM = 8;       //선택지 최대 개수
    private int choiceNum = 2;                  //현재 VISIBLE한 선택지 개수
    static boolean isRating = false;       //동일 비율 룰렛과 사용자 설정 비율 룰렛 판별
    private boolean isStart = false;            //spinBtn이 start 버튼인지 OK 버튼인지 판별
    private boolean inputError = false;         //user's input에 문제가 있는지 판별
    private boolean isFileDuplicated = false;   //파일 명이 겹치면 true
    private int saveDataNum = 0;                    //현재 저장되어있는 user 데이터 개수
    private UserHistoryData saveData;           //user가 save할 데이터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        //각 뷰들 id로 연결
        startLayout = (LinearLayout) findViewById(R.id.startLayout);
        rouletteLayout = (LinearLayout) findViewById(R.id.rouletteLayout_start);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        spinBtn = (Button) findViewById(R.id.spinBtn);
        appendBtn = (Button) findViewById(R.id.appendBtn);
        deleteBtn = (Button) findViewById(R.id.deleteBtn);
        resultText = (TextView) findViewById(R.id.resultText);

        setDataNameDialog = new Dialog(StartActivity.this);     //Dialog 초기화
        setDataNameDialog.setContentView(R.layout.dialog_data_name);      //dialog_data_name.xml과 연결

        //저장데이터 초기화
        saveData = new UserHistoryData();
        
        //배열 초기화
        userInputRate = new EditText[8];
        userInputChoice = new EditText[8];
        rouletteColorView = new View[8];
        userInputLayout = new LinearLayout[8];

        //ID 배열 설정
        setInputID();
        //유저가 입력한 값을 저장하는 배열 설정
        setUserInput();

        //선택지, 비율 배열 초기화
        userChoiceArray = new String[8];
        userRateArray = new float[]{1,1,1,1,1,1,1,1};

        //룰렛 커스텀 뷰 초기화
        mRoulette = new Roulette(this,rouletteLayout,userRateArray,8);

        //exit버튼 연결 및 클릭이벤트 설정
        exitBtn = (Button) findViewById(R.id.exitBtn_start);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //동일 비율 룰렛/사용자 설정 비율 룰렛 판별
        setVisibilityRating();
        //커스텀 뷰를 레이아웃에 추가
        rouletteLayout.addView(mRoulette);
    }

    protected void onPause(){
        super.onPause();
    }

    protected void onResume(){
        super.onResume();
        setVisibilityRating();
        setRouletteColor();
        setBackgroundColor();
        restoreState();
    }

    //공유 데이터 불러오기
    protected void restoreState(){
        if(HistoryActivity.pref!=null && HistoryActivity.pref.contains("saveDataNum")){
            this.saveDataNum = HistoryActivity.pref.getInt("saveDataNum",0);
            if(isSelectedRoulette()){
                setSelectedRoulette();
            }
        }
    }
    //유저가 History에서 누른 상태인지 판별
    protected boolean isSelectedRoulette(){
        if(HistoryActivity.pref.contains("isSelectedRoulette")&&HistoryActivity.pref.contains("fileName")){
            if(HistoryActivity.pref.getBoolean("isSelectedRoulette",false)){
                return true;
            }
        }
        return false;
    }

    //유저가 저장한 데이터를 토대로 룰렛 설정
    protected void setSelectedRoulette(){
        FileInputStream fis;
        String[] rates, choices;
        byte[] buffer;
        String fileName = HistoryActivity.pref.getString("fileName","");  //유저가 누른 file name 저장
        //파일 입출력이므로 try-catch문으로 작성
        try{
            fis = openFileInput(fileName+".txt");
            buffer = new byte[fis.available()];             //파일 전체 읽어오기
            fis.read(buffer);
            String[] subStr = new String(buffer).split("\r\n");      //구분자로 문자열 자르기
            rates = subStr[0].split(" ");               //첫 줄(비율 데이터) 저장
            choices = subStr[1].split(" ");             //둘째 줄(선택지 데이터) 저장
            //유저가 저장한 선택지 개수만큼 반복하면서 저장된 데이터대로 레이아웃 세팅
            for(int i=0;i<rates.length;i++){
                userInputRate[i].setText(rates[i]);
                userInputChoice[i].setText(choices[i]);
                userInputRate[i].setVisibility(View.VISIBLE);
                userInputChoice[i].setVisibility(View.VISIBLE);
                //세번째 부터는 가려져있던 Linear Layout 하나씩 VISIBLE로 설정
                if(i>1) {
                    userInputLayout[i].setVisibility(View.VISIBLE);
                    choiceNum++;
                }
            }
            //공유데이터 초기화 및 저장
            HistoryActivity.editor.putBoolean("isSelectedRoulette",false);
            HistoryActivity.editor.commit();
            fis.close();
        }catch(IOException e){
            e.getStackTrace();
        }
    }

    //저장할 비율 데이터 문자열로 반환
    protected String writeRateData(UserHistoryData data){
        String str = "";
        float[] rate = data.getRate();
        for(int i=0;i<choiceNum;i++){
            str+=String.valueOf(rate[i])+" ";
        }
        //공백 제거
        str.trim();
        //구분자 삽입
        str+="\r\n";
        return str;
    }

    //저장할 선택지 데이터 문자열로 반환
    protected String writeChoiceData(UserHistoryData data){
        String str = "";
        String[] choice = data.getChoice();
        for(int i=0;i<choiceNum;i++){
            str+=choice[i]+" ";
        }
        //공백 제거
        str.trim();
        //구분자 삽입
        str+="\r\n";
        return str;
    }

    //fileName과 같은 파일이 존재하는지 여부를 boolean 값으로 반환
    protected boolean isFileExist(String fileName){
        FileInputStream fis;
        byte[] buffer;
        String[] fileNameArray;
        try{
            fis = openFileInput("file_names.txt");
            buffer = new byte[fis.available()];
            fis.read(buffer);
            fileNameArray = new String(buffer).split("\r\n");       //구분자로 문자열 자르기
            for(int i=0;i<fileNameArray.length;i++){
                if((fileName+".txt").equals(fileNameArray[i])) {        //파라미터 fileName과 같은 파일명이 저장 되어있는 경우
                    fis.close();
                    return true;
                }
            }
            fis.close();
        }catch(IOException e){
            e.getStackTrace();
        }
        return false;
    }

    //동일한 이름의 파일이 있을 경우 나오는 dialog
    protected void duplicatedFileName(EditText editText){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("동일한 이름의 파일이 있습니다\n파일을 덮어쓰시겠습니까?");
        //yes를 누르면 isFileDuplicated에 파일이 겹친다는 정보를 주고 setSaveData 메소드 호출
        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isFileDuplicated = true;
                setSaveData(editText.getText().toString());
            }
        });
        //no를 누르면 토스트 메시지 출력
        alertDialogBuilder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(StartActivity.this, "취소되었습니다", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //View에 순서대로 룰렛 색상 표시
    protected void setRouletteColorView(final int[] COLORS){
        for(int i=0;i<MAX_CHOICE_NUM;i++){
            rouletteColorView[i].setBackgroundColor(COLORS[i]);
        }
    }
    //선택지 문자열 배열 설정
    protected void setUserChoiceArray(){
        for(int i=0; i<choiceNum; i++){
            if(userInputChoice[i].getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), (i+1) + "번째 선택지 입력이 올바르지 않습니다", Toast.LENGTH_SHORT).show();
                inputError = true;
                break;
            }
            userChoiceArray[i] = userInputChoice[i].getText().toString();
        }
    }

    //저장할 파일 이름을 입력받는 custom dialog
    protected void showDialog(){
        setDataNameDialog.show();
        EditText userInputName = setDataNameDialog.findViewById(R.id.userInputName);
        Button okBtn = setDataNameDialog.findViewById(R.id.okBtn);
        Button cancelBtn = setDataNameDialog.findViewById(R.id.cancelBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //예외처리
                try {
                    if(isFileExist(userInputName.getText().toString())){     //같은 이름의 파일이 있는지 확인
                        duplicatedFileName(userInputName);          //있다면 duplicatedFileName dialog 호출
                    }
                    else                                            //없으면 데이터 설정
                        setSaveData(userInputName.getText().toString());
                    setDataNameDialog.dismiss();                            //dialog 닫기
                } catch(Exception e){                                       //에러 출력 catch문
                    e.printStackTrace();
                    Toast.makeText(StartActivity.this, "[ERROR] 저장에 실패했습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDataNameDialog.dismiss();        //dialog가 닫힘
            }
        });
    }

    //저장할 데이터의 정보를 설정
    protected void setSaveData(String dataName){
        saveData.setDataName(dataName);                     //데이터의 이름(파일명) 설정
        saveData.setChoice(userChoiceArray);                //선택지 배열 설정
        if(isRating)                                        //사용자 비율 설정 룰렛이면 비율 배열도 설정
            saveData.setRate(userRateArray);
        try{
            FileOutputStream fos, fos_fileName;     //유저 입력 정보를 저장할 파일(fos), 저장된 파일 이름들을 갖고있는 파일(fos_fileName)
            //유저가 입력한 정보를 파일에 저장
            fos = openFileOutput(saveData.getDataName()+".txt",Context.MODE_PRIVATE);
            fos.write(writeRateData(saveData).getBytes());
            fos.write(writeChoiceData(saveData).getBytes());
            fos.close();
            //동일한 파일명이 없을 경우에만 fos_fileName에 파일명 추가
            if(!isFileDuplicated) {
                fos_fileName = openFileOutput("file_names.txt", Context.MODE_APPEND);
                fos_fileName.write((saveData.getDataName() + ".txt\r\n").getBytes());
                fos_fileName.close();
                saveDataNum+=1;                     //저장된 데이터(파일) 개수 증가
            }
            isFileDuplicated = false;               //boolean값 초기화
            Toast.makeText(StartActivity.this, "저장이 완료되었습니다", Toast.LENGTH_SHORT).show();
            //공유 데이터 설정
            HistoryActivity.editor.putInt("saveDataNum",saveDataNum);
            HistoryActivity.editor.commit();
        }catch(IOException e){
            e.getStackTrace();
        }
    }

    //비율 배열 설정
    protected void setUserRateArray(){
        float data = 0f;
        for(int i=0; i<choiceNum; i++){
            if(userInputRate[i].getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), (i+1) + "번째 비율 입력이 올바르지 않습니다", Toast.LENGTH_SHORT).show();
                inputError = true;
                break;
            }
            data = Float.parseFloat(userInputRate[i].getText().toString());
            if(data<=0){
                Toast.makeText(getApplicationContext(), (i+1) + "번째 비율이 올바르지 않습니다\n비율은 양수만 입력 가능합니다", Toast.LENGTH_SHORT).show();
                inputError = true;
                break;
            }
            userRateArray[i] = data;
        }
    }

    //onClick 메소드
    public void onClick(View view){
        switch (view.getId()){
            case R.id.saveBtn:                  //저장 버튼
                if(isStart) {
                    if(saveDataNum>=HistoryActivity.MAX_DATA_NUM){          //최대치 제한
                        Toast.makeText(StartActivity.this,"최대 10개의 데이터만 저장할 수 있습니다",Toast.LENGTH_SHORT).show();
                    }
                    else
                        showDialog();          //파일명 입력하는 dialog 호출
                }
                else
                    Toast.makeText(StartActivity.this, "OK 버튼을 누른 후에 저장해주세요", Toast.LENGTH_SHORT).show();
                break;
            case R.id.appendBtn:                //선택지 추가 버튼(+)
                if(choiceNum<8){                //최대치 제한
                    userInputLayout[choiceNum].setVisibility(View.VISIBLE);
                    choiceNum++;
                    if(isStart){                //룰렛 동작 버튼 초기화
                        spinBtn.setText("OK");
                        isStart=false;
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "선택지는 최대 8개 입니다.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.deleteBtn:            //선택지 삭제 버튼(-)
                if(choiceNum>2){
                    userInputLayout[choiceNum-1].setVisibility(View.GONE);
                    choiceNum--;
                    if(isStart){            //룰렛 동작 버튼 초기화
                        spinBtn.setText("OK");
                        isStart=false;
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "선택지는 최소 2개 입니다.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.spinBtn:              //룰렛 동작 버튼
                if(isStart) {               //버튼 text가 Start일 때 클릭된 경우
                    //룰렛 돌리기
                    mRoulette.startSpin();
                    mRoulette.invalidate();
                    mRoulette.getResult(resultText, spinBtn);
                    spinBtn.setText("OK");          //text를 OK로 변경
                    isStart = false;                //boolean값 변경
                }
                else{                       //버튼 text가 OK일 때 클릭된 경우
                    if(isRating)            //사용자 설정 비율일 경우
                        setUserRateArray(); //비율 배열 재설정
                    if(!inputError)         //유저가 올바른 값을 입력했을 경우
                        setUserChoiceArray(); //선택지 배열 재설정

                    // 비율/선택지 배열 둘 다 문제 없을 경우에 룰렛 동작
                    if(!inputError){
                        mRoulette.setChoiceNum(this.choiceNum); //룰렛의 선택지 개수 설정
                        mRoulette.setRate(userRateArray);       //룰렛의 비율 배열 설정
                        mRoulette.setString(userChoiceArray);   //룰렛의 문자열(선택지) 배열 설정
                        mRoulette.invalidate();
                        spinBtn.setText("start");               //버튼 text를 start로 변경
                        isStart = true;                         //boolean값 변경
                    }
                    inputError = false;    //값을 다시 초기화 시킨다
                }
                break;
            default:
                break;
        }
    }

    //ID배열에 레이아웃 뷰들 id로 연결
    protected void setInputID(){
        ratingID = new int[]{R.id.userInputRating1,R.id.userInputRating2,R.id.userInputRating3,
                R.id.userInputRating4,R.id.userInputRating5,R.id.userInputRating6,R.id.userInputRating7,
                R.id.userInputRating8};
        choiceID = new int[]{R.id.userInputChoice1,R.id.userInputChoice2,R.id.userInputChoice3,
                R.id.userInputChoice4,R.id.userInputChoice5,R.id.userInputChoice6,R.id.userInputChoice7,
                R.id.userInputChoice8};
        inputLayoutID = new int[]{R.id.userInputLayout1,R.id.userInputLayout2,R.id.userInputLayout3,
                R.id.userInputLayout4,R.id.userInputLayout5,R.id.userInputLayout6,R.id.userInputLayout7,
                R.id.userInputLayout8};
        colorViewID = new int[]{R.id.rouletteColor1,R.id.rouletteColor2,R.id.rouletteColor3,
                R.id.rouletteColor4,R.id.rouletteColor5,R.id.rouletteColor6,R.id.rouletteColor7,
                R.id.rouletteColor8};
    }

    //ID를 연결하여 각 배열에 저장
    protected void setUserInput(){
        for(int i=0;i<MAX_CHOICE_NUM;i++){
            userInputRate[i] = (EditText) findViewById(ratingID[i]);
            userInputChoice[i] = (EditText) findViewById(choiceID[i]);
            userInputLayout[i] = (LinearLayout) findViewById(inputLayoutID[i]);
            rouletteColorView[i] = (View) findViewById(colorViewID[i]);
        }
    }

    //fontColor 어둡게 설정
    protected void setUserInputFontToDark(){
        for(int i=0;i<MAX_CHOICE_NUM;i++){
            userInputChoice[i].setHintTextColor(getColor(R.color.default_text_color));
            userInputChoice[i].setTextColor(getColor(R.color.default_text_color));
            userInputRate[i].setHintTextColor(getColor(R.color.default_text_color));
            userInputRate[i].setTextColor(getColor(R.color.default_text_color));
        }
    }

    //fontColor 밝게 설정
    protected void setUserInputFontToLight(){
        for(int i=0;i<MAX_CHOICE_NUM;i++){
            userInputChoice[i].setTextColor(getColor(R.color.white));
            userInputRate[i].setTextColor(getColor(R.color.white));
            userInputChoice[i].setHintTextColor(getColor(R.color.white));
            userInputRate[i].setHintTextColor(getColor(R.color.white));
        }
    }

    //배경 다크모드 설정
    protected void setBackgroundColor(){
        if((SettingActivity.pref!=null)&&(SettingActivity.pref.contains("background"))){
            if(SettingActivity.pref.getBoolean("background",false)){        //DarkMode일 경우
                startLayout.setBackgroundColor(getColor(R.color.Background_Dark));
                resultText.setTextColor(getColor(R.color.white));
                setUserInputFontToLight();
            }
            else {                                                      //DarkMode가 아닐 경우
                startLayout.setBackgroundColor(getColor(R.color.white));
                resultText.setTextColor(getColor(R.color.default_text_color));
                setUserInputFontToDark();
            }
        }
    }

    //룰렛 다크모드 설정
    protected void setRouletteColor(){
        if((SettingActivity.pref!=null)&&(SettingActivity.pref.contains("roulette"))){
            if(SettingActivity.pref.getBoolean("roulette",false)){      //DarkMode일 경우
                mRoulette.setColors(MainActivity.color_Dark);
                setRouletteColorView(MainActivity.color_Dark);
            }
            else {                                                     //DarkMode가 아닐 경우
                mRoulette.setColors(MainActivity.color_Light);
                setRouletteColorView(MainActivity.color_Light);
            }
        }
        else {                                                     //초기값은 LightMode
            mRoulette.setColors(MainActivity.color_Light);
            setRouletteColorView(MainActivity.color_Light);
        }
    }

    //동일 비율 룰렛인지 사용자 설정 비율 룰렛인지에 따라 '비율 입력' EditText 설정
    protected void setVisibilityRating(){
        if(isRating){
            for(int i=0;i<MAX_CHOICE_NUM;i++){              //사용자 설정 비율 룰렛이면 VISIBLE
                userInputRate[i].setVisibility(View.VISIBLE);
            }
        }
        else{                                               //동일 비율 룰렛이면 INVISIBLE
            for(int i=0;i<MAX_CHOICE_NUM;i++) {
                userInputRate[i].setVisibility(View.INVISIBLE);
            }
        }
    }
}