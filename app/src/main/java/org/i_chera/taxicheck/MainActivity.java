package org.i_chera.taxicheck;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    private static final String PMB_HOME = "http://www.pmb.ro";
    private static final String PMB_WEBSITE =
            PMB_HOME + "/adrese_utile/transport_urban/autorizatii_taxi/autorizatii_TAXI.php";
    private static final String PDF_LINK_BASE =
            "/adrese_utile/transport_urban/autorizatii_taxi/doc/situatia_autorizatiilor_taxi_";
    private static final int DATE_SIZE = 8;
    private static final int PDF_LINK_SIZE = PDF_LINK_BASE.length() + DATE_SIZE + ".pdf".length();

    private PdfParseTask mPdfTask;

    private static boolean sAppStuffInit;

    /**
     * Initializes per-app settings
     */
    private static void initAppStuff(Context context)
    {
        if(sAppStuffInit)
            return;
        sAppStuffInit = true;
    }

    /**
     * On creation, setup the interface
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initAppStuff(this);

        setContentView(R.layout.activity_main);

        Button updateDataButton = findViewById(R.id.button_update_data);
        updateDataButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                updateData();
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(mPdfTask != null)
            mPdfTask.cancel(true);
    }

    /**
     * Displays a text message, usually error.
     * @param message
     */
    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(int resource)
    {
        showToast(getString(resource));
    }

    /**
     * Reads a PDF from URL
     * @param url
     */
    private void loadLink(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }



    /**
     * Handles HTML cityhall data.
     * @param response
     */
    private void handleHtmlData(String response)
    {
        Document doc = Jsoup.parse(response);
        Elements links = doc.getElementsByTag("a");
        String correctLink = null;
        int latest = 0;
        for(Element link: links)
        {
            String href = link.attr("href");
            try
            {
                if(href.startsWith(PDF_LINK_BASE) && href.length() == PDF_LINK_SIZE)
                {
                    String substring = href.substring(PDF_LINK_BASE.length(),
                            PDF_LINK_BASE.length() + DATE_SIZE);
                    int date = Integer.parseInt(substring);
                    if(date > latest)
                    {
                        latest = date;
                        correctLink = href;
                    }
                }
            }
            catch(NumberFormatException e)
            {
                Log.w(TAG, "Error in URL " + link);
            }
        }
        if(correctLink == null)
        {
            showToast(R.string.could_not_find_links);
            return;
        }

        String link = PMB_HOME + correctLink;
        if(mPdfTask == null)
            mPdfTask = new PdfParseTask();
        mPdfTask.execute(link);
    }

    /**
     * Update the data from the internet.
     */
    private void updateData()
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, PMB_WEBSITE, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                handleHtmlData(response);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                showToast(R.string.error_getting_taxi_data);
            }
        });
        queue.add(request);
    }

    /**
     * Parses the remote PDF
     */
    private class PdfParseTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                String result = Utility.parsePdfFromUrl(params[0]);
                return result;
            }
            catch(IOException e)
            {
                Log.w(TAG, "Error getting PDF: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            if(result != null)
                Log.i(TAG, result);
            else
                showToast(R.string.could_not_parse_pdf);
        }
    }
}
