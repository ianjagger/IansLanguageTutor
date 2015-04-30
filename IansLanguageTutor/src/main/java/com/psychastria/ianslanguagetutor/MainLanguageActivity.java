package com.psychastria.ianslanguagetutor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class MainLanguageActivity extends Activity {

    private String language = "";
    private String languageSystemName = "";
    private String lesson = "";
    private int lessonrowno = 0;
    boolean displayEnglish = true;
    public static final String PREFS_NAME = "MyOptionsFile";
    private TextView hiddenLanguage;
    private TextView displayLanguage;
    private TextView lessonView;
    private ImageView visibleImage;
    boolean nextlesson = false;

    List<String> lessons =  new ArrayList<String>();
    List<List<String>> vocab = new ArrayList<List<String>>();
    private Activity dg = null;

    @Override
    public void onStop() {
        super.onStop();  // Always call the superclass method first

        SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor edit = pref.edit();

        edit.putString("lesson", lesson);
        edit.putInt("lessonrowno", lessonrowno);

        edit.commit();

    }

    public void doMainBackClick(View e){
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor edit = pref.edit();

        edit.putString("lesson", lesson);
        edit.putInt("lessonrowno", lessonrowno);

        edit.commit();

        Intent optionsLang = new Intent (this, LanguageOptionsActivity.class);
        startActivity(optionsLang);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_land);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_main);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dg = this;

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        language = settings.getString("language", "");
        languageSystemName = settings.getString("languagesystemname", "");
        displayEnglish = settings.getBoolean("displayEnglish", false);
        lesson = settings.getString("lesson", "");
        lessonrowno = settings.getInt("lessonrowno", 0);

        ImageButton nextPhraseButton = (ImageButton) findViewById(R.id.nextPhraseButton);
        nextPhraseButton.setVisibility(View.GONE);

        ImageButton previousPhraseButton = (ImageButton) findViewById(R.id.previousPhraseButton);
        previousPhraseButton.setVisibility(View.GONE);

        new LessonsAsyncTask().execute();

        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_land);
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_main);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_language, menu);
        return true;
    }

    public void loadVocabItem()
    {
        List<String> vocabItem = vocab.get(lessonrowno);
        hiddenLanguage = (TextView)findViewById(R.id.mainHiddenTextView);
        displayLanguage = (TextView)findViewById(R.id.mainVisibleTextView);

        Log.d ("vocab item", Integer.toString(vocabItem.size()));

        if (displayEnglish)
        {
            displayLanguage.setText(vocabItem.get(0));
            hiddenLanguage.setText(vocabItem.get(1));
        }
        else
        {
            displayLanguage.setText(vocabItem.get(1));
            hiddenLanguage.setText(vocabItem.get(0));
        }

        hiddenLanguage.setVisibility(View.GONE);
    }

    public void doPreviousPhraseClick(View e){
        if (lessonrowno > 0)
            lessonrowno--;
        else
        {
            getPreviousLesson();
            lessonrowno = 0;
        }
        loadVocabItem();

    }

    public void doNextPhraseClick(View e){
        if (lessonrowno < vocab.size() - 1)
            lessonrowno++;
        else
        {
            getNextLesson();
            lessonrowno = 0;
        }
        loadVocabItem();

    }

    public int getIndex (List<String> list, String item)
    {
        int retval = -1;

        int count = list.size();
        for (int i=0; i < count; i++)
        {
            if (list.get(i).equals(item))
                retval = i;

        }
        return retval;
    }

    public void getNextLesson()
    {
        int found = getIndex(lessons, lesson);
        if (found < lessons.size() - 1)
        {
            lesson = lessons.get(found + 1);
        }
        else
        {
            lesson = lessons.get(0);
        }

        nextlesson = true;
        postUpdatedLesson();
    }

    public void getPreviousLesson()
    {
        int found = getIndex(lessons, lesson);
        if (found > 0)
        {
            lesson = lessons.get(found - 1);
        }
        else
        {
            lesson = lessons.get(lessons.size() - 1);
        }

        nextlesson = false;
        postUpdatedLesson();
    }

    private void postUpdatedLesson() {
        hiddenLanguage = (TextView)findViewById(R.id.mainHiddenTextView);
        displayLanguage = (TextView)findViewById(R.id.mainVisibleTextView);

        hiddenLanguage.setVisibility(View.GONE);
        displayLanguage.setText("Loading...");

        lessonView = (TextView)findViewById(R.id.lessonView);
        lessonView.setText(lesson);

        ImageButton nextPhraseButton = (ImageButton) findViewById(R.id.nextPhraseButton);
        nextPhraseButton.setVisibility(View.GONE);

        ImageButton previousPhraseButton = (ImageButton) findViewById(R.id.previousPhraseButton);
        previousPhraseButton.setVisibility(View.GONE);

        new VocabAsyncTask().execute();
    }


    public void doVisibleClick(View e){
        hiddenLanguage.setVisibility(View.VISIBLE);
    }

    public int getVocabCount()
    {
        return vocab.size();
    }

    public class LessonsAsyncTask extends AsyncTask<String, Void, List<String>>
    {

        protected void onPreExecute()
        {

        }

        protected void onPostExecute(List<String> jobList)
        {
            lessonView = (TextView)findViewById(R.id.lessonView);

            if (lesson.equals(""))
                lesson = lessons.get(0);

            lessonView.setText(lesson);

            new VocabAsyncTask().execute();
        }


        @Override
        protected List<String> doInBackground(String... params)
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                FileInputStream is = openFileInput("Index_" + languageSystemName + ".xml");
                Document doc = builder.parse(is);

                XPathFactory xpfactory = XPathFactory.newInstance();
                XPath xpath = xpfactory.newXPath();

                XPathExpression expr = xpath.compile("//lessons/lesson");

                Object result = expr.evaluate(doc, XPathConstants.NODESET);
                NodeList nodes = (NodeList) result;
                for (int i = 0; i < nodes.getLength(); i++) {

                    Node n = nodes.item(i);
                    String lang = n.getFirstChild().getNodeValue();
                    lessons.add(lang);
                }

            }
            catch (Exception ex)
            {

                language = ex.getStackTrace().toString();
            }
            return  lessons;

        }
    }

    public class VocabAsyncTask extends AsyncTask<String, Void, List<String>>
    {

        protected void onPreExecute()
        {

        }

        protected void onPostExecute(List<String> jobList)
        {
            if (nextlesson == false)
            {
                lessonrowno = vocab.size() - 1;
            }

            loadVocabItem();

            ImageButton nextPhraseButton = (ImageButton) findViewById(R.id.nextPhraseButton);
            nextPhraseButton.setVisibility(View.VISIBLE);

            ImageButton previousPhraseButton = (ImageButton) findViewById(R.id.previousPhraseButton);
            previousPhraseButton.setVisibility(View.VISIBLE);

        }

        @Override
        protected List<String> doInBackground(String... params)
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                FileInputStream is = openFileInput(languageSystemName + "_" + lesson + ".xml");

                Document doc = builder.parse(is);

                XPathFactory xpfactory = XPathFactory.newInstance();
                XPath xpath = xpfactory.newXPath();

                XPathExpression expr = xpath.compile("//vocab/lesson[@filename = '" + lesson + "']/line");

                Object result = expr.evaluate(doc, XPathConstants.NODESET);
                NodeList nodes = (NodeList) result;

                vocab.clear();

                for (int i = 0; i < nodes.getLength(); i++) {
                    Node n =  nodes.item(i);

                    String english = n.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
                    String language = n.getChildNodes().item(1).getChildNodes().item(0).getNodeValue();

                    List<String> line = new ArrayList<String>();
                    line.add(english);
                    line.add(language);

                    vocab.add(line);
                }
                lessonrowno = 0;
            }
            catch (Exception ex)
            {

                language = ex.getStackTrace().toString();
            }
            return  lessons;

        }
    }

}