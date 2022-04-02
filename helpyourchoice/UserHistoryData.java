package com.ryu.helpyourchoice;

public class UserHistoryData {
    private float[] rate;       //비율 데이터
    private String[] choice;    //선택지 데이터
    private String dataName;    //데이터 이름

    public UserHistoryData(){
        //데이터 초기화
        rate = new float[]{1,1,1,1,1,1,1,1};
        choice = new String[8];
        dataName = "";
    }

    protected void setRate(float[] newRate){
        rate = newRate;
    }

    protected void setChoice(String[] newChoice){
        choice = newChoice;
    }

    protected void setDataName(String newName){
        dataName = newName;
    }

    protected float[] getRate(){
        return rate;
    }

    protected String[] getChoice(){
        return choice;
    }

    protected String getDataName(){
        return dataName;
    }

}
