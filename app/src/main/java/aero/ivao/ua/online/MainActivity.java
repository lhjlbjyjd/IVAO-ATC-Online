package aero.ivao.ua.online;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    int length = 0;
    Boolean doneParsing = false, firstLaunch;
    ArrayList<atc> atcs = new ArrayList<>();
    ListAdapter listAdapter;
    String[] positions = new String[15];
    String[] names = new String[15];
    String[] vids = new String[15];
    ListView lvMain;
    ProgressBar pb;
    PackageInfo pInfo;
    String verName;
    TextView noOnline;
    SwipeRefreshLayout mSwipeRefreshLayout;
    SharedPreferences sPref;
    final String FILENAME = "servData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        sPref = getPreferences(MODE_PRIVATE);
        Boolean fileCreated = sPref.getBoolean("FileCreated", false);
        firstLaunch = sPref.getBoolean("FirstLaunch", true);
        Log.d("FirstLaunch", firstLaunch.toString());
        if(!fileCreated){
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        openFileOutput(FILENAME, MODE_PRIVATE)));
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            SharedPreferences.Editor ed = sPref.edit();
            ed.putBoolean("FileCreated", true);
            ed.apply();
        }
        verName = pInfo.versionName;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listAdapter = new ListAdapter(this, atcs);
        lvMain = (ListView) findViewById(R.id.list);
        pb = (ProgressBar) findViewById(R.id.pb);
        noOnline = (TextView) findViewById(R.id.noOnlineText);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(
                Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW);
        new VersionSync().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.bug:
                showBug();
                return true;
            case R.id.about:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onRefresh() {
        reloadData();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private class VersionSync extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        HttpURLConnection urlConnection1 = null;
        BufferedReader reader = null;
        BufferedReader reader1 = null;
        String versionName = "";
        String changeList = "";
        String result = "false";

        @Override
        protected String doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL("http://app.ivao-ua.com/AppVersion.html");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                urlConnection.disconnect();
                versionName = buffer.toString();

                if (versionName.equals(verName)) {
                    result = "true";
                } else {
                    result = "false";
                    URL url1 = new URL("http://app.ivao-ua.com/changelist.html");

                    urlConnection1 = (HttpURLConnection) url1.openConnection();
                    urlConnection1.setRequestMethod("GET");
                    urlConnection1.connect();

                    InputStream is = urlConnection1.getInputStream();
                    StringBuilder buff = new StringBuilder();

                    reader1 = new BufferedReader(new InputStreamReader(is));

                    String line1;
                    while ((line1 = reader1.readLine()) != null) {
                        line1 = "• " + line1 + "\n";
                        buff.append(line1);
                    }

                    changeList = buff.toString();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Вывод JSON
            if (result.equals("true")) {
                continueAppLaunch();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Требуется обновление!")
                        .setMessage(changeList)
                        .setIcon(R.drawable.icon)
                        .setCancelable(false)
                        .setNegativeButton("ОК",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        Uri address = Uri.parse("http://app.ivao-ua.com/GetNewestVersion.html");
                                        Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                                        startActivity(openlinkIntent);
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

        }

    }

    private class ChangeList extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String changeList = "";

        @Override
        protected String doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL("http://app.ivao-ua.com/changelist.html");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    line = "• " + line + "\n";
                    buffer.append(line);
                }
                urlConnection.disconnect();

                changeList = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return changeList;
        }
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Изменения:")
                    .setMessage(changeList)
                    .setIcon(R.drawable.icon)
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
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
                StringBuilder buffer = new StringBuilder();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                urlConnection.disconnect();

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
                if (length == 0) {
                    noOnline.setVisibility(View.VISIBLE);
                } else {
                    noOnline.setVisibility(View.GONE);
                }
                mSwipeRefreshLayout.setRefreshing(false);
            } catch (Exception e) {
                doneParsing = true;
            }

        }

    }

    public void continueAppLaunch() {
        if(firstLaunch){
            Log.d("first","launch");
            SharedPreferences.Editor ed = sPref.edit();
            ed.putBoolean("FirstLaunch", false);
            ed.apply();
            new ChangeList().execute();
        }
        new ParseTask().execute();
        startService(new Intent(this, BasicService.class));
    }

    public void reloadData(){
        atcs.clear();
        new ParseTask().execute();
    }

    public void showAbout(){
        Intent intent = new Intent(this, about.class);
        startActivity(intent);
    }

    public void showBug(){
        String[] temp = new String[1];
        temp[0] = "lhjlbjyjd@gmail.com";
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","lhjlbjyjd@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, temp);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "BUG");
        startActivity(Intent.createChooser(emailIntent, "Сообщить об ошибке..."));
    }

}


