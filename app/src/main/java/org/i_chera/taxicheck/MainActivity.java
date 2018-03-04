package org.i_chera.taxicheck;

import android.content.Intent;
import android.net.Uri;
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

    /**
     * On creation, setup the interface
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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
        loadLink(PMB_HOME + correctLink);
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
}
