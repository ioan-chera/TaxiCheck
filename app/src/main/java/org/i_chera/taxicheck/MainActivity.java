package org.i_chera.taxicheck;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity implements TextWatcher,
        AppGlobal.TaxiDataChangeListener
{
    private static final String TAG = "MainActivity";

    private static final String PMB_HOME = "http://www.pmb.ro";
    private static final String PMB_WEBSITE =
            PMB_HOME + "/adrese_utile/transport_urban/autorizatii_taxi/autorizatii_TAXI.php";
    private static final String PDF_LINK_BASE =
            "/adrese_utile/transport_urban/autorizatii_taxi/doc/situatia_autorizatiilor_taxi_";
    private static final int DATE_SIZE = 8;
    private static final int PDF_LINK_SIZE = PDF_LINK_BASE.length() + DATE_SIZE + ".pdf".length();

    private static final String PREF_LATEST_PDF = "latestPdf";

    private PdfParseTask mPdfTask;

    private Button mButton;
    private EditText mEditNumber;
    private TextView mResultView;

    private AppGlobal mApp;

    /**
     * On creation, setup the interface
     * @param savedInstanceState saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mApp = AppGlobal.get();
        mApp.setListener(this);
        mApp.processInitialize(this);

        setContentView(R.layout.activity_main);

        mButton = findViewById(R.id.button_update_data);
        mButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                updateTaxiData();
            }
        });

        mEditNumber = findViewById(R.id.edit_number);
        mResultView = findViewById(R.id.status);

        mEditNumber.addTextChangedListener(this);
    }

    @Override
    public void taxiDataChanged(TaxiData data)
    {
        afterTextChanged(null);
    }

    @Override
    public void afterTextChanged(Editable s)
    {
        if(mEditNumber == null)
            return;

        if(mApp.getData() == null)
        {
            mResultView.setText(R.string.no_data_available);
            return;
        }

        String license = mEditNumber.getText().toString();
        license = TaxiData.normalize(license);
        if(!license.matches(TaxiData.LICENSE_PATTERN))
        {
            mResultView.setText(R.string.fill_in_fields);
            return;
        }
        int status = mApp.getData().getLicenseStatus(license);

        switch(status)
        {
            case TaxiData.STATUS_INVALID:
                mResultView.setText(R.string.license_invalid);
                break;
            case TaxiData.STATUS_VALID:
                mResultView.setText(R.string.license_valid);
                break;
            default:
                mResultView.setText(R.string.license_unregistered);
                break;
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
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
     * @param message what to print
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
     * Handles HTML cityhall data.
     * @param response the obtained text
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

        String savedLink = getPreferences(Context.MODE_PRIVATE).getString(PREF_LATEST_PDF, "");
        if(correctLink.equals(savedLink))
        {
            showToast(R.string.already_loaded);
            if(mApp.refreshTaxiData(this))
                return;
        }

        getPreferences(Context.MODE_PRIVATE).edit().putString(PREF_LATEST_PDF, correctLink).apply();

        String link = PMB_HOME + correctLink;
        if(mPdfTask == null)
            mPdfTask = new PdfParseTask();
        mPdfTask.execute(link);
    }

    /**
     * Update the data from the internet.
     */
    private void updateTaxiData()
    {
        // Look in the shared preferences
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
        protected void onPreExecute()
        {
            mButton.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                return Utility.parsePdfFromUrl(params[0]);
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
            try
            {
                mButton.setEnabled(true);
                mApp.saveDataSource(result, MainActivity.this);
                mApp.refreshTaxiData(MainActivity.this);
            }
            catch(IOException e)
            {
                showToast(R.string.failed_writing);
            }
        }
    }
}
