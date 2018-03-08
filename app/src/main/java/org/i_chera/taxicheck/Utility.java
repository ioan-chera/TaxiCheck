package org.i_chera.taxicheck;

import android.app.Activity;
import android.util.Log;
import android.view.WindowManager;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * Various utility calls
 */

final class Utility
{
    private static String TAG = "Utility";

    /**
     * Gets a PDF from URL and parses it. Should not be called from the UI thread.
     * @param urlString URL where to read PDF
     * @return The resulting plain text
     */
    static String parsePdfFromUrl(String urlString) throws IOException
    {
        URL url = new URL(urlString);

        InputStream urlStream = null;
        BufferedInputStream stream = null;
        PdfReader reader = null;
        try
        {
            urlStream = url.openStream();
            stream = new BufferedInputStream(urlStream);
            reader = new PdfReader(stream);
            int numPages = reader.getNumberOfPages();
            Log.i(TAG, "Number of pages: " + numPages);
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < numPages; ++i)
            {
                builder.append(PdfTextExtractor.getTextFromPage(reader, i + 1).trim())
                        .append('\n');
            }
            return builder.toString();
        }
        finally
        {
            if(reader != null)
                reader.close();
            close(stream);
            close(urlStream);
        }
    }

    static void close(Closeable closeable)
    {
        if(closeable == null)
            return;
        try
        {
            closeable.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Adapted from https://stackoverflow.com/a/13357785
     */
    static String convertStreamToString(InputStream stream) throws IOException
    {
        InputStreamReader streamReader = null;
        BufferedReader reader = null;
        try
        {
            streamReader = new InputStreamReader(stream);
            reader = new BufferedReader(streamReader);
            StringBuilder builder = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
                builder.append(line).append('\n');
            return builder.toString();
        }
        finally
        {
            close(reader);
            close(streamReader);
        }
    }

    /**
     * Reads lines from an InputStream
     * @param stream The input stream
     * @return an array of strings, each one being a line
     * @throws IOException on reading error
     */
    static String[] readLinesFromStream(InputStream stream) throws IOException
    {
        InputStreamReader streamReader = null;
        BufferedReader reader = null;
        try
        {
            streamReader = new InputStreamReader(stream);
            reader = new BufferedReader(streamReader);
            ArrayList<String> lines = new ArrayList<>();
            String line;
            while((line = reader.readLine()) != null)
                lines.add(line);

            String[] result = new String[lines.size()];
            result = lines.toArray(result);
            return result;
        }
        finally
        {
            close(reader);
            close(streamReader);
        }
    }

    /**
     * Makes the case of a character to title case.
     * @param text The text to convert
     * @return Title Case
     */
    static String toTitleCase(String text)
    {
        if(text.isEmpty())
            return "";
        StringBuilder builder = new StringBuilder(text.length());
        builder.append(Character.toUpperCase(text.charAt(0)));
        boolean makeUpper = false;
        for(int i = 1; i < text.length(); ++i)
        {
            char character = text.charAt(i);
            if(Character.isSpaceChar(character))
                makeUpper = true;
            else if(makeUpper)
            {
                builder.append(Character.toUpperCase(text.charAt(i)));
                makeUpper = false;
            }
            else
                builder.append(Character.toLowerCase(text.charAt(i)));
        }
        return builder.toString();
    }

    /**
     * Convenient method to just show keyboard
     * @param activity necessary context
     */
    static void showKeyboard(Activity activity)
    {
        activity.getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

}
