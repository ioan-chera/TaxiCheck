package org.i_chera.taxicheck;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Global app data info. Holds stuff that should outlive activity configuration change
 */

final class AppGlobal
{
    private static final String DATA_FILE = "data.txt"; // holds a copy of the taxi file to parse
    private static final String TAG = "AppGlobal";      // for logging

    private static AppGlobal sInstance; // global instance

    private boolean mInitialized;       // true if it was already initialized
    private TaxiData mData;             // taxi license data

    interface TaxiDataChangeListener
    {
        void taxiDataChanged(TaxiData data);
    }

    private TaxiDataChangeListener mListener;

    /**
     * Gets the unique instance, creating it if necessary
     * @return it
     */
    static AppGlobal get()
    {
        if(sInstance == null)
            sInstance = new AppGlobal();
        return sInstance;
    }

    /**
     * Checks if there's any data
     * @return
     */
    TaxiData getData()
    {
        return mData;
    }

    void saveDataSource(String result, Context context) throws IOException
    {
        Log.i(TAG, "PostExecute");
        FileOutputStream stream = null;
        try
        {
            stream = context.openFileOutput(DATA_FILE, Context.MODE_PRIVATE);
            stream.write(result.getBytes());
        }
        finally
        {
            Utility.close(stream);
        }
    }

    /**
     * Loads the taxi data from the internal file
     * @param context a context which allows getting the file
     */
    boolean refreshTaxiData(Context context)
    {
        Log.i(TAG, "Start parse data");
        FileInputStream stream = null;
        try
        {
            stream = context.openFileInput(DATA_FILE);
            mData = new TaxiData(stream);
            if(mListener != null)
                mListener.taxiDataChanged(mData);
            return true;
        }
        catch(IOException e)
        {
            return false;
        }
        finally
        {
            Log.i(TAG, "Stop parse data");
            Utility.close(stream);
        }
    }

    /**
     * Does initializing that should only happen once per process.
     * @param context a context for app stuff
     */
    void processInitialize(Context context)
    {
        if(mInitialized)
            return;
        mInitialized = true;
        refreshTaxiData(context);
    }

    void setListener(TaxiDataChangeListener listener)
    {
        mListener = listener;
    }

    /**
     * Private constructor
     */
    private AppGlobal()
    {
    }


}
