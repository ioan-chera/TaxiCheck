package org.i_chera.taxicheck;

import android.util.Log;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Created by ioan on 04.03.2018.
 */

final class Utility
{
    private static String TAG = "Utility";

    /**
     * Gets a PDF from URL and parses it. Should not be called from the UI thread.
     * @param urlString
     * @return
     */
    static String parsePdfFromUrl(String urlString) throws IOException
    {
        URL url = new URL(urlString);

        BufferedInputStream stream = new BufferedInputStream(url.openStream());
        PdfReader reader = new PdfReader(stream);
        int numPages = reader.getNumberOfPages();
        Log.i(TAG, "Number of pages: " + numPages);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < numPages; ++i)
        {
            builder.append(PdfTextExtractor.getTextFromPage(reader, i + 1).trim())
                    .append('\n');
            if(i == 0)
                Log.i(TAG, builder.toString());
        }
        return builder.toString();
    }
}
