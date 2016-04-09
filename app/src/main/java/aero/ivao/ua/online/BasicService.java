package aero.ivao.ua.online;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class BasicService extends Service {
    final String LOG_TAG = "myLogs";
    ArrayList<atc> atcs = new ArrayList<atc>();
    int length;
    int length1;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;
    final String FILENAME = "servData";
    NotificationManager nm;
    int SDK_VERSION = android.os.Build.VERSION.SDK_INT;

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        beginObservation();
        return Service.START_STICKY;

    }

            public void onDestroy() {
                super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    void beginObservation() {
        new Thread(new Runnable() {
            public void run() {
                mTimer = new Timer();
                mMyTimerTask = new MyTimerTask();
                mTimer.schedule(mMyTimerTask, 0, 300000);
            }
        }).start();
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String resultJson = "";
            try {
                URL url = new URL("http://wildfly-ivao.rhcloud.com/StatView/JsonAtcOnline");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();
                Log.d("Start", "Start");
                JSONObject jsonObject = new JSONObject(resultJson);
                Log.d("Start", "Obj");
                JSONArray data = jsonObject.getJSONArray("data");
                Log.d("Start", "Get");
                length = data.length();
                String[] positions = new String[length];
                String[] names = new String[length];
                String[] vids = new String[length];
                Log.d("L2", String.valueOf(length));
                for (int i = length-1; i >= 0; i--) {
                    //Переносим позиции из JSON в массив
                    JSONArray tmp = data.getJSONArray(i);
                    positions[i] = tmp.getString(0);
                    names[i] = tmp.getString(2);
                    vids[i] = tmp.getString(1);
                    Log.d("POS", positions[i]);
                    Log.d("NAME", names[i]);
                    Log.d("VID", vids[i]);
                }

                //2 JSON
                Log.d("Start", "Start");
                String oldJson = readFile();
                JSONObject jsonObject1 = new JSONObject(oldJson);
                Log.d("Start", "Obj");
                JSONArray data1 = jsonObject1.getJSONArray("data");
                Log.d("Start", "Get");
                length1 = data1.length();
                Log.d("L2", String.valueOf(length1));
                Log.d("JSON", oldJson);
                boolean updated = false;
                String[] prevPositions = new String[length1];
                for (int i = 0; i < length1; i++) {
                    //Переносим позиции из JSON в массив
                    JSONArray tmp1 = data1.getJSONArray(i);
                    if(tmp1.length() != 0){
                        prevPositions[i] = tmp1.getString(0);
                        Log.d("REP", "REP");
                    }
                }
                Log.d("LEN", String.valueOf(length1));
                for (int i = 0; i < length; i++){
                    Log.d("I", String.valueOf(i));
                    Log.d("1", positions[i]);
                    updated = true;
                    for (int j = 0; j < length1; j++){
                        Log.d("J", String.valueOf(j));
                        Log.d("2", prevPositions[j]);
                        if(positions[i].equals(prevPositions[j])){
                            updated = false;
                        }
                    }
                    if(updated){
                        break;
                    }
                }
                if(updated){
                    if(SDK_VERSION >= 16) {
                        long[] vibrationPattern = {0, 300, 200, 300};
                        Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(aero.ivao.ua.online.BasicService.this)
                                        .setSmallIcon(R.drawable.logonotif_white)
                                        .setContentTitle("Новые диспетчеры онлайн!")
                                        .setContentText("Самое время полетать!")
                                        .setAutoCancel(true)
                                        .setSound(ringUri)
                                        .setVibrate(vibrationPattern);
                        Intent resultIntent = new Intent(aero.ivao.ua.online.BasicService.this, MainActivity.class);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(aero.ivao.ua.online.BasicService.this);
                        stackBuilder.addParentStack(MainActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        mBuilder.setContentIntent(resultPendingIntent);
                        NotificationManager mNotificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(221, mBuilder.build());
                    }else{
                        long[] vibrationPattern = {0, 300, 200, 300};
                        Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(aero.ivao.ua.online.BasicService.this)
                                .setSmallIcon(R.drawable.logonotif_black)
                                .setContentTitle("Новые диспетчеры онлайн!")
                                .setContentText("Самое время полетать!")
                                .setAutoCancel(true)
                                .setSound(ringUri)
                                .setVibrate(vibrationPattern);
                        NotificationManagerCompat nm = NotificationManagerCompat.from(aero.ivao.ua.online.BasicService.this);
                        nm.notify(221, mBuilder.build());
                    }
                    writeFile(resultJson);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void writeFile(String json) {
        try {
            // отрываем поток для записи
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput(FILENAME, MODE_PRIVATE)));
            // пишем данные
            bw.write(json);
            // закрываем поток
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    String readFile() {
        String str = "";
        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput(FILENAME)));
            // читаем содержимое
            str = br.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
