package com.ryu.helpyourchoice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private LinearLayout historyLayout;
    Button exitBtn;
    private ListView list;
    static SharedPreferences pref;                  //공유프레퍼런스 생성
    static SharedPreferences.Editor editor;         //editor 생성
    String[] fileNameArray;                         //저장된 파일명을 담아둘 문자열 배열
    private ArrayAdapter<String> adapter;           //문자열 어댑터
    private Dialog setDataNameDialog;               //저장 데이터의 이름(파일명)을 설정하는 커스텀 대화상자 객체
    static ArrayList<UserHistoryData> data;         //유저 정보를 담아둘 ArrayList
    final static int MAX_DATA_NUM = 10;             //데이터 저장 최대 용량
    private String fileRename = "";                 //rename할 새로운 파일명
    private int clickedPosition;                    //리스트뷰에서 클릭된 위치값
    private boolean isDark = false;                 //DarkMode인지 판별

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        historyLayout = (LinearLayout) findViewById(R.id.historyLayout);
        list = (ListView) findViewById(R.id.userHistory);
        exitBtn = (Button) findViewById(R.id.exitBtn_history);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        data = new ArrayList<>();                   //ArrayList 객체 할당
        setDataNameDialog = new Dialog(HistoryActivity.this);     //Dialog 초기화
        setDataNameDialog.setContentView(R.layout.dialog_data_name);      //dialog_data_name.xml과 연결
    }

    protected void onPause(){
        super.onPause();
        saveState();
    }

    protected void onResume(){
        super.onResume();
        restoreState();
        setBackgroundColor();
        setListView();
    }

    //현재 저장 상태를 내부 저장소에 저장
    protected void saveState(){
        FileOutputStream fos;
        try {
            fos = openFileOutput("file_names.txt",MODE_PRIVATE);        //데이터 파일들의 이름이 저장된 파일
            for(int i=0;i<data.size();i++){
                String str = data.get(i).getDataName()+".txt\r\n";
                fos.write(str.getBytes());
            }
            fos.close();
            editor.putInt("saveDataNum", data.size());          //공유 데이터에 데이터 개수 최신화
            editor.commit();                                       //공유 데이터 저장
        }catch(IOException e){
            e.getStackTrace();
        }
    }


    //내부 저장소에서 데이터 파일 읽어오기
    protected void restoreState(){
        if((pref!=null)&&(pref.contains("saveDataNum"))) {      //공유 데이터가 없으면 저장 데이터도 없음
            FileInputStream fis, fis_fileName;
            byte[] buffer;
            UserHistoryData userData;
            int saveDataNum = pref.getInt("saveDataNum",0);     //공유 데이터
            data.clear();
            try {
                fis_fileName = openFileInput("file_names.txt");
                buffer = new byte[fis_fileName.available()];
                fis_fileName.read(buffer);
                fileNameArray = new String(buffer).split("\r\n");  //구분자를 기준으로 파일명들을 배열에 저장
                //fileNameArray 토대로 UserHistoryData 채우기
                for (int i = 0; i < saveDataNum; i++) {
                    userData = new UserHistoryData();
                    fis = openFileInput(fileNameArray[i]);
                    buffer = new byte[fis.available()];
                    fis.read(buffer);
                    String[] subStr = new String(buffer).split("\r\n");     //한 줄씩 읽어오기(첫 줄은 rate, 두번째 줄은 선택지)
                    userData.setDataName(fileNameArray[i].substring(0,fileNameArray[i].length()-4));  //".txt" 자르고 저장
                    userData.setRate(setUserRateData(subStr[0]));
                    userData.setChoice(setUserChoiceData(subStr[1]));
                    data.add(userData);             //ArrayList에 저장
                    fis.close();
                }
                fis_fileName.close();

            } catch (IOException e) {
                e.getStackTrace();
            }
        }
    }

    //비율값을 배열(float형)로 반환
    protected float[] setUserRateData(String rateStr){
        String[] temp = rateStr.split(" ");     //구분자를 기준으로 문자열 나누기
        float[] result = new float[8];
        for (int i = 0; i < temp.length; i++) {
            result[i] = Float.parseFloat(temp[i]);      //반환할 배열에 삽입
        }
        return result;
    }

    //선택지를 문자열 배열로 반환
    protected String[] setUserChoiceData(String choiceStr){
        String[] result = new String[8];
        String[] temp = choiceStr.split(" ");       //구분자를 기준으로 문자열 나누기
        for(int i=0;i<temp.length;i++){
            result[i] = temp[i];                //반환할 배열에 삽입
        }
        return result;
    }

    //ListView에 뿌려질 텍스트 색상 설정
    protected void setAdapterTextColor(ArrayList<String> listDataName){
        //어댑터 객체 설정
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listDataName){
            //어댑터가 갖고있는 데이터를 어떻게 보여줄지 정의하는 getView 메소드
            public View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position,convertView,parent);
                TextView txt = (TextView)view.findViewById(android.R.id.text1);
                if(isDark)                  //DarkMode인 경우
                    txt.setTextColor(getColor(R.color.white));
                else                        //DarkMode가 아닌 경우우
                   txt.setTextColor(getColor(R.color.default_text_color));
                return view;
            }
        };
    }

    //리스트뷰 설정
    protected void setListView(){
        ArrayList<String> listDataName = new ArrayList<>();
        for(int i=0;i<data.size();i++){
            listDataName.add(data.get(i).getDataName());
        }
        setAdapterTextColor(listDataName);      //어댑터 설정
        list.setAdapter(adapter);
        registerForContextMenu(list);  //Context 메뉴 등록
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {     //리스트뷰 클릭 이벤트
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showSelectedRoulette(position);
            }
        });
    }

    //클릭된 데이터가 가진 정보로 룰렛 설정
    protected void showSelectedRoulette(int position){
        Intent intent;
        editor.putBoolean("isSelectedRoulette",true);           //선택 여부 입력
        editor.putString("fileName",data.get(position).getDataName());      //파일명 입력
        editor.commit();                                    //공유 데이터 저장
        intent = new Intent(HistoryActivity.this,StartActivity.class);      //인텐트 객체 설정
        StartActivity.isRating = true;                          //사용자 설정 비율 룰렛으로 설정
        startActivity(intent);                                  //화면 전환
    }

    //Context 메뉴 생성할 때 호출되는 메소드
    public void onCreateContextMenu(ContextMenu menu, View view,
                                       ContextMenu.ContextMenuInfo menuInfo){
        getMenuInflater().inflate(R.menu.list_context_menu,menu);
        super.onCreateContextMenu(menu, view, menuInfo);
    }

    //컨텍스트 메뉴 클릭 이벤트
    public boolean onContextItemSelected(MenuItem item){
        //AdapterContextMenuInfo
        //ListView가 onCreateContextMenu 할 때 선택된 항목에 대한 정보를 관리
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        clickedPosition = info.position;       //listView에서 ContextMenu가 보여지는 항목의 위치

        switch(item.getItemId()){
            case R.id.delete_button:
                deleteData(clickedPosition);
                break;
            case R.id.rename_button:
                showDialog();
                break;
        }
        return true;
    }

    //rename할 때 보여지는 dialog
    protected void showDialog(){
        setDataNameDialog.show();
        EditText userInputName = setDataNameDialog.findViewById(R.id.userInputName);
        Button okBtn = setDataNameDialog.findViewById(R.id.okBtn);
        Button cancelBtn = setDataNameDialog.findViewById(R.id.cancelBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {       //ok버튼 클릭
            @Override
            public void onClick(View view) {
                try {
                    if(isDuplicatedFile(userInputName.getText().toString())){           //동일한 파일명이 존재하는 경우
                        Toast.makeText(HistoryActivity.this,"동일한 파일명이 이미 존재합니다\n다른 파일을 삭제 후 저장할 수 있습니다",
                                Toast.LENGTH_SHORT).show();
                    }
                    else {      //동일한 파일명이 존재하지 않는 경우
                        fileRename = userInputName.getText().toString();        //바꿀 파일명 설정
                        renameData(clickedPosition);                //파일명 재설정
                    }
                    setDataNameDialog.dismiss();                    //dialog 닫기
                } catch(Exception e){       //예외처리
                    e.printStackTrace();
                    Toast.makeText(HistoryActivity.this, "[ERROR] 저장에 실패했습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {           //cancel버튼 클릭
            @Override
            public void onClick(View view) {
                Toast.makeText(HistoryActivity.this,"취소 되었습니다",Toast.LENGTH_SHORT).show();
                setDataNameDialog.dismiss();        //dialog가 닫힘
            }
        });
    }

    //데이터(파일) 삭제
    protected void deleteData(int index){
        //선택된 데이터에 해당하는 파일 불러오기
        File file = new File(getFilesDir(),data.get(index).getDataName()+".txt");
        //파일 삭제(삭제되면 true, 안되면 false)
        boolean deleteResult = file.delete();
        if(deleteResult) {          //정상적으로 파일이 삭제된 경우
            data.remove(index);     //ArrayList에서도 삭제
            setListView();          //리스트뷰 재설정
            editor.putInt("saveDataNum",data.size());       //데이터 개수 공유 데이터에 입력
            editor.commit();                                    //데이터 저장
            Toast.makeText(HistoryActivity.this, "파일이 삭제되었습니다", Toast.LENGTH_SHORT).show();
        }
        else            //예외처리
            Toast.makeText(HistoryActivity.this, "[ERROR] 파일삭제에 실패했습니다", Toast.LENGTH_SHORT).show();
    }

    //데이터명(파일명)변경
    protected void renameData(int index){
        //선택된 기존 파일 불러오기
        File beforeFile = new File(getFilesDir(),data.get(index).getDataName()+".txt");
        //새로운 파일명으로된 파일
        File afterFile = new File(getFilesDir(),fileRename+".txt");
        //기존 파일명을 새로운 파일명으로 rename(변경되면 true, 안되면 false)
        boolean result = beforeFile.renameTo(afterFile);
        if(result) {            //정상적으로 이름이 변경된 경우
            data.get(index).setDataName(fileRename);        //ArrayList에 저장된 데이터도 변경
            setListView();              //리스트뷰 재설정
            Toast.makeText(HistoryActivity.this, "파일이 저장되었습니다", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(HistoryActivity.this, "[ERROR] 파일저장에 실패했습니다", Toast.LENGTH_SHORT).show();
    }

    //동일한 데이터명(파일명)이 있는지 판별
    protected boolean isDuplicatedFile(String fileName){
        for(int i=0;i<data.size();i++){
            if(data.get(i).getDataName().equals(fileName)){
                return true;
            }
        }
        return false;
    }

    //배경 다크모드 설정
    protected void setBackgroundColor(){
        if((SettingActivity.pref!=null)&&(SettingActivity.pref.contains("background"))){
            if(SettingActivity.pref.getBoolean("background",false)){
                historyLayout.setBackgroundColor(getColor(R.color.Background_Dark));
                isDark = true;
            }
            else {
                historyLayout.setBackgroundColor(getColor(R.color.white));
                isDark = false;
            }
        }
    }
}