package aero.ivao.ua.online;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    int length = 0;
    boolean doneParsing = false;
    ArrayList<atc> atcs = new ArrayList<atc>();
    ListAdapter listAdapter;
    String[] positions = new String[15];
    String[] names = new String[15];
    String[] vids = new String[15];
    ListView lvMain;
    ProgressBar pb;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listAdapter = new ListAdapter(this, atcs);
        lvMain = (ListView) findViewById(R.id.lvMain);
        pb = (ProgressBar) findViewById(R.id.pb);
        new ParseTask().execute();
        startService(new Intent(this, BasicService.class));
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void onClick(View v){
        long[] vibrationPattern = {0, 300, 200, 300};
        Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notiflogo)
                        .setContentTitle("Новые диспетчеры онлайн!")
                        .setContentText("Самое время полетать!")
                        .setAutoCancel(true)
                        .setSound(ringUri)
                        .setVibrate(vibrationPattern);
        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(221, mBuilder.build());
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://aero.ivao.ua.online/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://aero.ivao.ua.online/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
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

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            // Вывод JSON
            Log.d("JSON", strJson);
            try {
                Log.d("Start", "Start");
                JSONObject jsonObject = new JSONObject(strJson);
                Log.d("Start", "Obj");
                JSONArray data = jsonObject.getJSONArray("data");
                Log.d("Start", "Get");
                length = data.length();
                Log.d("L2", String.valueOf(length));
                for (int i = length - 1; i >= 0; i--) {
                    //Переносим позиции из JSON в массив
                    JSONArray tmp = data.getJSONArray(i);
                    positions[i] = tmp.getString(0);
                    names[i] = tmp.getString(2);
                    vids[i] = tmp.getString(1);
                    Log.d("POS", positions[i]);
                    Log.d("NAME", names[i]);
                    Log.d("VID", vids[i]);
                    atcs.add(new atc(names[i], positions[i], vids[i]));
                }
                lvMain.setAdapter(listAdapter);
                lvMain.setVisibility(View.VISIBLE);
                pb.setVisibility(View.GONE);
            } catch (Exception e) {
                doneParsing = true;
            }

        }

    }

}


