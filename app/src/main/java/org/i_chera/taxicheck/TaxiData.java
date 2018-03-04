package org.i_chera.taxicheck;

import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ioan on 04.03.2018.
 */

class TaxiData
{
    private static final String TAG = "TaxiData";
    private final HashMap<String, Boolean> mMap;

    static final int STATUS_VALID = 0;
    static final int STATUS_INVALID = 1;
    static final int STATUS_UNREGISTERED = 2;

    static final String LICENSE_PATTERN = "\\b[A-Z]{1,2}-?[0-9]{2,3}-?[A-Z]{3}\\b";

    int getLicenseStatus(String license)
    {
        Boolean value = mMap.get(license);
        if(value == null)
            return STATUS_UNREGISTERED;
        return value ? STATUS_VALID : STATUS_INVALID;
    }

    TaxiData(InputStream stream) throws IOException
    {
        mMap = new HashMap<>();
        String[] lines = Utility.readLinesFromStream(stream);

        Pattern pattern = Pattern.compile(LICENSE_PATTERN);
        Pattern validity = Pattern.compile("\\bVALIDA\\b");

        // Check that it starts with number
        for(String line: lines)
        {
            if(!line.matches("^[0-9]+\\s.*" + LICENSE_PATTERN + ".*$"))
                continue;
            Matcher matcher = pattern.matcher(line);
            if(matcher.find())
            {
                String key = matcher.group().replaceAll("-", "");
                boolean value = validity.matcher(line).find();
                mMap.put(key, value);
            }
        }
    }
}
