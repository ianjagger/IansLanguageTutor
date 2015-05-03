package com.psychastria.ianslanguagetutor;


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;

public class UpgradeActivity extends Activity {

    private Spinner langSpinner;
    public static final String PREFS_NAME = "MyOptionsFile";
    final ArrayList<String> languages = new ArrayList<String>();
    Activity dg;

    private String LANGFILE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        LANGFILE = "Languages.xml";
        dg = this;

        ImageButton nextUpgradeButton = (ImageButton) findViewById(R.id.nextUpgradeButton);
        nextUpgradeButton.setVisibility(View.GONE);

        new GetLanguageListAsyncTask().execute();

        Configuration config = getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_upgrade_land);
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_upgrade);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_upgrade_land);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_upgrade);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.upgrade, menu);
        return true;
    }

    public void doUpgradeUpgradeClick(View e){
        if (DetectConnection.checkInternetConnection(getBaseContext()))
        {

            ImageButton nextUpgradeButton = (ImageButton) findViewById(R.id.nextUpgradeButton);
            Button downloadButton = (Button) findViewById(R.id.downloadButton);

            nextUpgradeButton.setVisibility(View.GONE);
            downloadButton.setVisibility(View.GONE);

            new GetFTPAsyncTask().execute();

        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(dg);

            builder.setMessage("Please check internet connectivity and try again")
                    .setTitle("Download");

            AlertDialog dialog = builder.create();
            dialog.show();


        }
    }



    public void doUpgradeNextClick(View e){
        String language = langSpinner.getSelectedItem().toString();

        SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor edit = pref.edit();

        edit.putString("language", language);
        edit.putString("languagesystemname", language);

        edit.commit();

        Intent optionsLang = new Intent (this, LanguageOptionsActivity.class);
        startActivity(optionsLang);


    }

    public class GetFTPAsyncTask extends AsyncTask<String, Void, List<String>>
    {

        protected void onPreExecute()
        {

        }

        protected void onPostExecute(List<String> jobList)
        {
            ImageButton nextUpgradeButton = (ImageButton) findViewById(R.id.nextUpgradeButton);
            Button downloadButton = (Button) findViewById(R.id.downloadButton);

            nextUpgradeButton.setVisibility(View.VISIBLE);
            downloadButton.setVisibility(View.VISIBLE);

            AlertDialog.Builder builder = new AlertDialog.Builder(dg);

            builder.setMessage("Downloaded")
                    .setTitle("Done");

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        @TargetApi(Build.VERSION_CODES.FROYO)
        @Override
        protected List<String> doInBackground(String... params)
        {
            try
            {
                DownloadFile(Constants.DOWNLOADPATH, LANGFILE);

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                FileInputStream is = openFileInput(LANGFILE);

                Document doc = builder.parse(is);

                XPathFactory xpfactory = XPathFactory.newInstance();
                XPath xpath = xpfactory.newXPath();

                XPathExpression expr = xpath.compile("//languages/language/displayname/text()");

                Object result = expr.evaluate(doc, XPathConstants.NODESET);
                NodeList nodes = (NodeList) result;

                for (int i = 0; i < nodes.getLength(); i++) {
                    String languagefilename = nodes.item(i).getNodeValue() + ".xml";

                    DownloadFile("http://www.mittelalter.co.uk/psychastria/german/"  + languagefilename, languagefilename);

                    String [] al = fileList();

                    if (Utilities.containsElement(al, languagefilename))
                    {
                        splitFile (languagefilename);

                        deleteFile(languagefilename);
                    }
                }

            }
            catch (Exception ex)
            {

               Log.d ("Error!", ex.getMessage());
            }

            new GetLanguageListAsyncTask().execute();

            return  languages;

        }

        public void DownloadFile (String urlname, String filename)
        {
            try
            {
                URL url = new URL(urlname);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);

                urlConnection.connect();

                deleteFile(filename);

                InputStream inputStream = urlConnection.getInputStream();

                int totalSize = urlConnection.getContentLength();

                if (totalSize > 0)
                {
                    FileOutputStream fileOutput = openFileOutput(filename, Context.MODE_PRIVATE);

                    int downloadedSize = 0;

                    byte[] buffer = new byte[1024];

                    int bufferLength = 0; //used to store a temporary size of the buffer

                    while ( (bufferLength = inputStream.read(buffer)) > 0 )
                    {
                        fileOutput.write(buffer, 0, bufferLength);

                        downloadedSize += bufferLength;

                        int progress=(int)(downloadedSize*100/totalSize);

        //updateProgress(downloadedSize, totalSize);
                    }

                    fileOutput.flush();
                    fileOutput.close();
                }
            }
            catch (Exception ex)
            {
                Log.d ("exception in download", ex.getMessage());
                Log.d ("exception in download", ex.getStackTrace().toString());
            }
        }

        public  void splitFile (String filename)
        {
            try
            {
                InputStream inputStream = openFileInput(filename);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                int i;
                try {
                    i = inputStream.read();
                    while (i != -1)
                    {
                        byteArrayOutputStream.write(i);
                        i = inputStream.read();
                    }
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String Content = byteArrayOutputStream.toString();


                String ContentSplit [] = Content.split("<lesson");
                int csCount = ContentSplit.length;

                String outputFilename = "Index_" + filename;

                deleteFile(outputFilename);

                XmlSerializer serializer = Xml.newSerializer();
                StringWriter writer = new StringWriter();
                serializer.setOutput(writer);
                serializer.startDocument("UTF-8", true);
                serializer.startTag("", "lessons");

                String header = "";

                for (int j=0; j <csCount; j++ )
                {
                    String line = ContentSplit[j].replace("<lesson", "").replace("</vocab>", "");

                    if (j==0)
                        header = line;

                    if (line.contains(" filename=\""))
                    {
                        String fname = line.substring(line.indexOf(" filename=\""), line.indexOf("\" date=\""));

                        fname = fname.replace(" filename=\"", "").replace("\"", "");

                        deleteFile(fname);

                        serializer.startTag("", "lesson");

                        serializer.text(fname);

                        serializer.endTag("", "lesson");

                        FileOutputStream out = openFileOutput(filename.replace(".xml", "") + "_" + fname + ".xml", Context.MODE_PRIVATE);
                        out.write(header.getBytes());
                        out.write("<lesson".getBytes());
                        out.write(line.getBytes());
                        out.write("</vocab>".getBytes());

                        out.flush();
                        out.close();
                    }

                }
                serializer.endTag("", "lessons");
                serializer.endDocument();

                FileOutputStream out = openFileOutput(outputFilename, Context.MODE_PRIVATE);

                out.write(writer.toString().getBytes());

                out.flush();
                out.close();

            }
            catch (Exception ex)
            {

                Log.d ("Exception", ex.getMessage());
                Log.d ("Exception", ex.getStackTrace().toString());


            }


        }

        @TargetApi(Build.VERSION_CODES.FROYO)
        public  void generateIndexFile (String filename)
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                FileInputStream is = openFileInput(filename);
                Document doc = builder.parse(is);

                XPathFactory xpfactory = XPathFactory.newInstance();
                XPath xpath = xpfactory.newXPath();

                XPathExpression expr = xpath.compile("//vocab/lesson/@filename");

                Object result = expr.evaluate(doc, XPathConstants.NODESET);
                NodeList nodes = (NodeList) result;
                ArrayList<String> lessons = new ArrayList<String>();

                for (int i = 0; i < nodes.getLength(); i++) {
                    String lang = nodes.item(i).getNodeValue();
                    lessons.add(lang);
                }

                String outputFilename = "Index_" + filename;

                deleteFile(outputFilename);

                XmlSerializer serializer = Xml.newSerializer();
                StringWriter writer = new StringWriter();
                try {
                    serializer.setOutput(writer);
                    serializer.startDocument("UTF-8", true);
                    serializer.startTag("", "lessons");
                    for (int i = 0; i < lessons.size(); i++){
                        serializer.startTag("", "lesson");

                        String less = lessons.get(i);
                        serializer.text(less);

                        serializer.endTag("", "lesson");
                    }
                    serializer.endTag("", "lessons");
                    serializer.endDocument();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                FileOutputStream out = openFileOutput(outputFilename, Context.MODE_PRIVATE);

                out.write(writer.toString().getBytes());

                out.flush();
                out.close();

            }
            catch (Exception ex)
            {


            }


        }
    }

    public class GetLanguageListAsyncTask extends AsyncTask<String, Void, List<String>>
    {

        protected void onPreExecute()
        {

        }

        protected void onPostExecute(List<String> jobList)
        {
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(dg,
                    android.R.layout.simple_spinner_item, languages);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            langSpinner = (Spinner) findViewById(R.id.langSpinner) ;

            langSpinner.setAdapter(dataAdapter);


            if (languages.size() > 0)
            {
                ImageButton nextUpgradeButton = (ImageButton) findViewById(R.id.nextUpgradeButton);
                nextUpgradeButton.setVisibility(View.VISIBLE);
            }

        }

        @TargetApi(Build.VERSION_CODES.FROYO)
        @Override
        protected List<String> doInBackground(String... params)
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                FileInputStream is = openFileInput(LANGFILE);

                Document doc = builder.parse(is);

                XPathFactory xpfactory = XPathFactory.newInstance();
                XPath xpath = xpfactory.newXPath();

                XPathExpression expr = xpath.compile("//languages/language/displayname/text()");

                Object result = expr.evaluate(doc, XPathConstants.NODESET);
                NodeList nodes = (NodeList) result;

                languages.clear();

                String [] al = fileList();

                for (int i = 0; i < nodes.getLength(); i++) {
                    String lang = nodes.item(i).getNodeValue();

                    if (Utilities.containsElement(al, "Index_" + lang + ".xml"))
                    {
                        languages.add(lang);
                    }
                }

                is.close();
            }
            catch (Exception ex)
            {
                Log.d ("Error!", ex.getMessage());
                //               language = ex.getStackTrace().toString();
            }
            return  languages;

        }
    }

}

