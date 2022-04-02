package com.ryu.helpyourchoice;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

//음악재생 서비스 클래스
public class MusicService extends Service {
    MediaPlayer player;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate(){
        player = MediaPlayer.create(this,R.raw.romance);    //재생기 생성
        player.setLooping(true);        //반복재생설정
    }

    public void onDestroy() {
        player.stop();
    }               //음악 중지

    public int onStartCommand(Intent intent, int flags, int startId){      //음악 재생
        player.start();
        return super.onStartCommand(intent,flags,startId);
    }
}
