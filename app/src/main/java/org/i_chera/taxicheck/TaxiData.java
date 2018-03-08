package org.i_chera.taxicheck;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds collected taxi data.
 */

class TaxiData
{
    private static final String TAG = "TaxiData";

    static class Entry
    {
        final int index;
        final boolean legal;
        final String car;

        Entry(int inIndex, boolean inLegal, String inCar)
        {
            index = inIndex;
            legal = inLegal;
            car = inCar;
        }
    }

    private final HashMap<String, Entry> mMap;

    static final int STATUS_VALID = 0;
    static final int STATUS_INVALID = 1;
    static final int STATUS_UNREGISTERED = 2;

    static final String LICENSE_PATTERN = "\\b[A-Z]{1,2}-?[0-9]{2,3}-?[A-Z]{3}\\b";
    private static final String INDEX_PATTERN = "^[0-9]+";

    private static final Pattern sIndexPattern = Pattern.compile(INDEX_PATTERN);
    private static final Pattern sLicensePattern = Pattern.compile(LICENSE_PATTERN);
    private static final Pattern sValidity = Pattern.compile("\\bVALIDA\\b");

    /**
     * Normalizes a license string
     * @param license the non-formatted string
     * @return the normalized string
     */
    static String normalize(String license)
    {
        return license.toUpperCase().replaceAll("[\\s\\-]+", "");
    }

    /**
     * Gets info about a license plate.
     * @param license The license plate, normalized.
     * @return one of three values
     */
    int getLicenseStatus(String license)
    {
        Entry value = mMap.get(license);
        if(value == null)
            return STATUS_UNREGISTERED;
        return value.legal ? STATUS_VALID : STATUS_INVALID;
    }

    /**
     * Returns the index of a license entry.
     * @param license
     * @return Integer.MIN_VALUE if not in list.
     */
    int getIndex(String license)
    {
        Entry value = mMap.get(license);
        if(value == null)
            return Integer.MIN_VALUE;
        return value.index;
    }

    String getCar(String license)
    {
        Entry value = mMap.get(license);
        if(value == null)
            return "";
        return value.car;
    }

    boolean hasData()
    {
        return mMap != null && !mMap.isEmpty();
    }

    /**
     * Constructs from an input stream file.
     * @param stream The file to read and parse
     * @throws IOException if there's error reading file.
     */
    TaxiData(InputStream stream) throws IOException
    {
        mMap = new HashMap<>();
        String[] lines = Utility.readLinesFromStream(stream);

        // Check that it starts with number
        for(String line: lines)
        {
            try
            {
               if(!line.matches(INDEX_PATTERN + "\\s.*" + LICENSE_PATTERN + ".*$"))
                    continue;
                Matcher licenseMatch = sLicensePattern.matcher(line);
                Matcher indexMatch = sIndexPattern.matcher(line);
                if(indexMatch.find() && licenseMatch.find())
                {
                    String key = normalize(licenseMatch.group());

                    int index = Integer.parseInt(indexMatch.group());
                    boolean legal = sValidity.matcher(line).find();
                    String car = Utility.toTitleCase(Knowledge.findKnownCar(line));

                    mMap.put(key, new Entry(index, legal, car));
                }
            }
            catch(NumberFormatException e)
            {
                Log.w(TAG, "Error parsing number in line: " + line);
            }
        }
    }
}
