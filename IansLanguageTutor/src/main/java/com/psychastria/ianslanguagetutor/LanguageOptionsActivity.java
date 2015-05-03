package com.psychastria.ianslanguagetutor;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
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

public class LanguageOptionsActivity extends Activity {

    private String language = "";
    private String languageSystemName = "";
    boolean displayEnglish = true;
    public static final String PREFS_NAME = "MyOptionsFile";
    private Spinner lessonSpinner;
    private TextView hiddenLanguage;
    private TextView displayLanguage;

    final ArrayList<String> lessons =  new ArrayList<String>();
    private Activity dg = null;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_options_land);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_options);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        dg = this;

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        language = settings.getString("language", "");
        languageSystemName = settings.getString("languagesystemname", "");

        try
        {
            String [] al = fileList();

            boolean f1 = Utilities.containsElement(al, "Index_" + language + ".xml");

            if (language.equals("") || languageSystemName.equals("") || !f1)
            {
                Intent upgradeOptions = new Intent (this, UpgradeActivity.class);
                startActivity(upgradeOptions);
            }

            displayEnglish = settings.getBoolean("displayEnglish", false);

            updateDisplayLanguageVisible();

            ImageButton nextOptionsButton = (ImageButton) findViewById(R.id.nextOptionsButton);
            nextOptionsButton.setVisibility(View.GONE);

            new MyAsyncTask().execute();
        }
        catch (Exception ex)
        {
            Utilities.processException(this, ex);
        }

        Configuration config = getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_options_land);
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_options);
        }

    }

    public void doOptionsNextClick(View e){

        lessonSpinner = (Spinner) findViewById(R.id.lessonSpinner) ;

        SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor edit = pref.edit();

        edit.putBoolean("displayEnglish", displayEnglish);

        if (lessonSpinner.getVisibility() == View.VISIBLE)
        {
            edit.putString("lesson", lessonSpinner.getSelectedItem().toString());
            edit.putInt("lessonrowno", 0);
        }
        else
        {
            if (pref.getString("lesson", "").equals(""))
            {
                edit.putString("lesson", lessonSpinner.getItemAtPosition(0).toString());
            }

        }

        edit.commit();

        Intent mainLang = new Intent (this, MainLanguageActivity.class);
        startActivity(mainLang);

    }

    public void doOptionsPreviousClick(View e){
        Intent upgradeOptions = new Intent (this, UpgradeActivity.class);
        startActivity(upgradeOptions);
    }


    public void doOptionsSwapClick(View e){
        displayEnglish = ! displayEnglish;

        updateDisplayLanguageVisible();
    }

    private void updateDisplayLanguageVisible() {
        hiddenLanguage = (TextView) findViewById(R.id.hiddenLangView) ;
        displayLanguage = (TextView) findViewById(R.id.visibleLangView);

        if (displayEnglish)
        {
            displayLanguage.setText("English");
            hiddenLanguage.setText(language);
        }
        else
        {
            displayLanguage.setText(language);
            hiddenLanguage.setText("English");
        }
    }

    public void continueRadioButtonClick(View e){
        lessonSpinner = (Spinner) findViewById(R.id.lessonSpinner) ;
        lessonSpinner.setVisibility(View.GONE);

    }

    public void lessonRadioButtonClick(View e){
        lessonSpinner = (Spinner) findViewById(R.id.lessonSpinner) ;
        lessonSpinner.setVisibility(View.VISIBLE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.language_options, menu);
        return true;
    }

    public void doLangOptionsPrevClick(View e){
            Intent upgradeOptions = new Intent (this, UpgradeActivity.class);
            startActivity(upgradeOptions);

    }

    public class MyAsyncTask extends AsyncTask <String, Void, List<String>>
    {

        protected void onPreExecute()
        {

        }

        protected void onPostExecute(List<String> jobList)
        {
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(dg,
                    android.R.layout.simple_spinner_item, lessons);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            lessonSpinner = (Spinner) findViewById(R.id.lessonSpinner) ;

            lessonSpinner.setAdapter(dataAdapter);

            SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
            String selectedLesson = pref.getString("lesson", "");

            if (!selectedLesson.equals(""))
            {
                int found = lessons.indexOf(selectedLesson);
                lessonSpinner.setSelection(found);

            }

            ImageButton nextOptionsButton = (ImageButton) findViewById(R.id.nextOptionsButton);
            nextOptionsButton.setVisibility(View.VISIBLE);
        }

        @TargetApi(Build.VERSION_CODES.FROYO)
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

}