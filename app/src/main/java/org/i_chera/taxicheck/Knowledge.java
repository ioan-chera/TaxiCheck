package org.i_chera.taxicheck;

import java.util.HashMap;

/**
 * Holds some static knowledge
 */

final class Knowledge
{
    /**
     * These must be sorted to avoid duplicates
     */
    private static final String sKnownCars[] = {
            "AUDI",
            "BMW",
            "CHEVROLET",
            "CITROEN",
            "DACIA",
            "DCAIA",
            "DAEWOO",
            "FIAT",
            "FORD",
            "HYUNDAI",
            "KIA",
            "KYA",
            "OPEL",
            "MAZDA",
            "MERCEDES",
            "MERCEDEZ",
            "MERCESES",
            "NISSAN",
            "PEUGEOT",
            "PROTON",
            "RENAULT",
            "RENULT",
            "SEAT",
            "SKODA",
            "VOLKSWAGEN",
    };

    /**
     * Spell-check names of cars and other strings
     */
    private static final HashMap<String, String> sSpellCheck = new HashMap<>();

    static
    {
        sSpellCheck.put("DCAIA", "DACIA");
        sSpellCheck.put("KYA", "KIA");
        sSpellCheck.put("MERCEDEZ", "MERCEDES");
        sSpellCheck.put("RENULT", "RENAULT");
    }

    /**
     * Applies spellchecking
     * @param name The potentially wrongly written name
     * @return The possibly correct variant
     */
    private static String spellCheck(String name)
    {
        String result = sSpellCheck.get(name);
        if(result != null)
            return result;
        return name;
    }

    /**
     * Returns a known car from a string
     * @param text The full text to search in. Case insensitive.
     * @return The car found inside, in all-caps. Empty string if not found.
     */
    static String findKnownCar(String text)
    {
        text = text.toUpperCase();
        for(String car: sKnownCars)
        {
            if(text.matches(".*\\b" + car + "\\b.*"))
                return spellCheck(car);
        }
        return "";
    }
}
