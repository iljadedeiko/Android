package com.example.labdarbas2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView lvRss;
    ArrayList<String> titles;
    ArrayList<String> links;
    ArrayList<String> author;
    Button button15min;
    Button buttonDelfi;
    Button buttonClear;
    TextView textViewName;
    private WebView webView;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    public boolean clear = false;
    String url;
    String prefUrl;
    private Animation webViewAnim;
    private Animation lvAnim;
    private Animation nameAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        lvRss.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Uri uri = Uri.parse(links.get(position));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        webView.setVisibility(View.GONE);

        if (pref.contains("saved_url")) {
            textViewName.setVisibility(View.GONE);
        }

        webViewAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.view_anim);
        lvAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.view2_anim);
        nameAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.name_anim);

        textViewName.startAnimation(nameAnim);
        YoYo.with(Techniques.Pulse)
                .duration(1000)
                .repeat(3)
                .delay(2000)
                .playOn(textViewName);
    }

    private void init() {
        lvRss = (ListView) findViewById(R.id.lvRss);
        webView = (WebView) findViewById(R.id.webView);
        textViewName = (TextView) findViewById(R.id.textViewName);
        url = "https://www.delfi.lt/";
        pref = getSharedPreferences("saved_url", MODE_PRIVATE);
        editor = pref.edit();

        titles = new ArrayList<String>();
        links = new ArrayList<String>();
        author = new ArrayList<String>();

        button15min = (Button) findViewById(R.id.Button15min);
        buttonDelfi = (Button) findViewById(R.id.ButtonDelfi);
        buttonClear = (Button) findViewById(R.id.ButtonClear);
    }

    public String getUrl() {
        prefUrl = pref.getString("saved_url", url);
        return prefUrl;
    }
    
    public void saveUrl(String url) {
        editor.putString("saved_url", url);
        editor.apply();
    }

    public void open15minRss(View view) {
        webView.setVisibility(View.GONE);
        new ProcessInBackground().execute();

        lvRss.startAnimation(webViewAnim);
    }

    public void openDelfiRss(View view) {
        webView.setVisibility(View.VISIBLE);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(getUrl());

        webView.startAnimation(lvAnim);
    }

    public void openGame(View view) {
        Intent intent = new Intent(this, game.class);
        startActivity(intent);
    }

    public void clearButton(View view) {
        editor.remove("saved_url");
        editor.apply();
        clear = true;
        Toast.makeText(this, "Data is cleared!", Toast.LENGTH_SHORT).show();
    }

    public void onStop() {
        super.onStop();
        if (!clear) {
            String webUrl = webView.getUrl();
            saveUrl(webUrl);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public InputStream getInputStream(URL url) {

        try {
            return url.openConnection().getInputStream();
        }
        catch (IOException e) {
            return null;
        }
    }

    public class ProcessInBackground extends AsyncTask<Integer, Void, Exception> {

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Loading rss feed...please wait...");
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... integers) {
            try {
                URL url = new URL("https://www.15min.lt/rss");
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(getInputStream(url), "UTF-8");

                boolean insideItem = false;
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagname = xpp.getName();
                    if (eventType == XmlPullParser.START_TAG) {
                        if (tagname.equalsIgnoreCase("item")) {
                            insideItem = true;
                        }
                        else if (tagname.equalsIgnoreCase("title")) {
                            if (insideItem) {
                                titles.add(xpp.nextText());
                            }
                        }
                        else if (tagname.equalsIgnoreCase("author")) {
                            if (insideItem) {
                                author.add(xpp.nextText());
                            }
                        }
                        else if (tagname.equalsIgnoreCase("link")) {
                            if (insideItem) {
                                links.add(xpp.nextText());
                            }
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = false;
                    }

                    eventType = xpp.next();
                }

            }
            catch (MalformedURLException e) {
                exception = e;
            }
            catch (XmlPullParserException e) {
                exception = e;
            }
            catch (IOException e) {
                exception = e;
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, titles);
            lvRss.setAdapter(adapter);
//            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, author);
//            lvRss.setAdapter(adapter2);

            progressDialog.dismiss();
        }
    }
}