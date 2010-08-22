package org.unicode.draft;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.unicode.cldr.unittest.TestAll.TestInfo;
import org.unicode.cldr.util.CLDRFile;
import org.unicode.cldr.util.Counter;
import org.unicode.cldr.util.SupplementalDataInfo.PopulationData;
import org.unicode.jsp.FileUtilities;
import org.unicode.jsp.FileUtilities.SemiFileReader;

import com.ibm.icu.impl.Row;
import com.ibm.icu.impl.Row.R2;
import com.ibm.icu.impl.Row.R3;
import com.ibm.icu.impl.Row.R4;
import com.ibm.icu.impl.Row.R5;
import com.ibm.icu.util.ULocale;

public class CountryPopulationByCode {
    private static final boolean SHOW_INTERNET = true;
    private static final boolean SHOW_WEIGHTS = false;
    private static final boolean SHOW_SOURCE = false;
    static TestInfo testInfo = TestInfo.getInstance();

    public static void main(String[] args) {

        //countryPopulation();
        countryLanguagePopulation();
    }

    private static void countryLanguagePopulation() {
        Map<String, Row.R2<Integer, Integer>> country2internetUsers = new TreeMap();

        SemiFileReader handler = new SequenceHandler(country2internetUsers).process(CountryPopulationByCode.class, "internetUsers.txt");

        Counter<String> gdp = new Counter();
        Counter<String> language2InternetLatest = new Counter();
        TreeSet<R5<String, Double, Double, String, Boolean>> rowSet = new TreeSet();

        for (String territoryCode : testInfo.getStandardCodes().getGoodAvailableCodes("territory")) {
            Set<String> languages;
            try {
                languages = testInfo.getSupplementalDataInfo().getLanguagesForTerritoryWithPopulationData(territoryCode);
                if (languages == null) continue;
            } catch (Exception e) {
                continue;
            }
            R2<Integer, Integer> internetData = country2internetUsers.get(territoryCode);
            if (internetData == null) continue;
            double totalWeighted = 0;
            double total = 0;

            PopulationData territoryData = testInfo.getSupplementalDataInfo().getPopulationDataForTerritory(territoryCode);
            double territoryGdp = territoryData.getGdp();

            double maxLiteratePopulation = 0;
            if (SHOW_SOURCE || territoryCode.equals("CN")) {
                System.out.println(
                        territoryCode + "\t" 
                        + testInfo.getEnglish().getName(CLDRFile.TERRITORY_NAME,territoryCode)
                        + "\t"
                        + "\t"
                        + "\t" + territoryData.getPopulation() 
                        + "\t" + territoryData.getLiteratePopulation() // getWeightedLiteratePopulation(data)
                        + "\t" + territoryData.getOfficialStatus()
                        + "\t" + territoryData.getGdp()
                );

            }
            for (String languageCode : languages) {
                PopulationData data = testInfo.getSupplementalDataInfo().getLanguageAndTerritoryPopulationData(languageCode, territoryCode);
                if (SHOW_SOURCE || territoryCode.equals("CN")) {
                    System.out.println(
                            territoryCode + "\t" 
                            + testInfo.getEnglish().getName(CLDRFile.TERRITORY_NAME,territoryCode)
                            + "\t" + languageCode 
                            + "\t" + getBaseName(languageCode)
                            + "\t" + data.getPopulation() 
                            + "\t" + data.getLiteratePopulation() // getWeightedLiteratePopulation(data)
                            + "\t" + data.getOfficialStatus());
                }
                totalWeighted += getWeightedLiteratePopulation(data);
                double literatePopulation = data.getLiteratePopulation();
                if (maxLiteratePopulation < literatePopulation) maxLiteratePopulation = literatePopulation;
                total += literatePopulation;
            }
            double literatePopulationInTerritory = territoryData.getLiteratePopulation();
            addRow(rowSet, territoryCode, "und", 1-total/literatePopulationInTerritory, 1-total/literatePopulationInTerritory);
            addRow(rowSet, territoryCode, "mul", 1-maxLiteratePopulation/literatePopulationInTerritory, 1-maxLiteratePopulation/literatePopulationInTerritory);

            for (String languageCode : languages) {
                PopulationData data = testInfo.getSupplementalDataInfo().getLanguageAndTerritoryPopulationData(languageCode, territoryCode);

                String languageName = getBaseName(languageCode);
                if (territoryCode.equals("IL")) {
                    System.out.println("$$\t" + data.getLiteratePopulation() + "\t" + testInfo.getEnglish().getName(languageCode));
                }
                double ratioWeighted = getWeightedLiteratePopulation(data)/totalWeighted;
                double ratio = data.getLiteratePopulation()/literatePopulationInTerritory;

                addRow(rowSet, territoryCode, languageCode, ratioWeighted, ratio);

                gdp.add(languageName, (int)(ratioWeighted * territoryGdp));
                language2InternetLatest.add(languageName, (int)(ratioWeighted * internetData.get1()));
            }
        }
        if (SHOW_WEIGHTS) {
            System.out.println("*** Factors");

            Object oldRegion = "";
            int counter = 1;
            for (R5<String, Double, Double, String, Boolean> row : rowSet) {
                Object region = row.get0();
                counter = region.equals(oldRegion) ? counter + 1 : 1;
                System.out.println(region + "\t" + counter + "\t" + -row.get1() + "\t" + -row.get2() + "\t" + row.get3() + "\t" + (row.get4() ? "K" : ""));
                oldRegion = region;
            }
        }

        if (SHOW_INTERNET) {
            //        for (String languageName : language2Internet2000.keySet()) {
            //            System.out.println(languageName + "\t2000\t" + language2Internet2000.get(languageName));
            //        }
            System.out.println("*** internet/gdp");
            for (String languageName : gdp.getKeysetSortedByCount(false)) {
                System.out.println(languageName + "\t" + language2InternetLatest.get(languageName) + "\t" + gdp.get(languageName));
            }
        }
    }

    private static void addRow(TreeSet<R5<String, Double, Double, String, Boolean>> rowSet, String territoryCode, String languageCode, double ratioWeighted, double ratio) {
        R5<String, Double, Double, String, Boolean> row = Row.of(
                testInfo.getEnglish().getName("territory", territoryCode) + "\t" + territoryCode, 
                -ratio, 
                -ratioWeighted, 
                testInfo.getEnglish().getName(languageCode) + "\t" + languageCode,
                KEY_LOCALES.contains(languageCode));
        rowSet.add(row);
    }

    static final         Set<String> KEY_LOCALES = new LinkedHashSet(Arrays.asList(
            "en", "es", "de", "fr", "ja", "it", "tr", "pt", "zh", "nl", 
            "pl", "ar", "ru", "zh_Hant", "ko", "th", "sv", "fi", "da", 
            "he", "nb", "el", "hr", "bg", "sk", "lt", "vi", "lv", "sr", 
            "pt_PT", "ro", "hu", "cs", "id", "sl", "fil", "fa", "uk", 
            "ca", "hi", "et", "eu", "is", "sw", "ms", "bn", "am", "ta", 
            "te", "mr", "ur", "ml", "kn", "gu", "or"));

    private static double getWeightedLiteratePopulation(PopulationData data) {
        return data.getLiteratePopulation() * data.getOfficialStatus().getWeight();
    }

    private static String getBaseName(String languageCode) {
        String baseLanguage = languageCode.contains("Hant") ? languageCode : new ULocale(languageCode).getLanguage();

        String languageName = testInfo.getEnglish().getName(baseLanguage);
        return languageName;
    }

    static class SequenceHandler extends FileUtilities.SemiFileReader {
        Map<String, Row.R2<Integer, Integer>> country2internetUsers = new TreeMap();
        public final static Pattern TABS = Pattern.compile("\\t+");
        public static final Map<String,String> name2code = new HashMap();
        public static final Map<String,String> remapName = new HashMap();
        static {
            for (String territory : testInfo.getStandardCodes().getGoodAvailableCodes("territory")) {
                name2code.put(testInfo.getEnglish().getName(CLDRFile.TERRITORY_NAME, territory), territory);
            }
            remapName.put("Korea, South", "South Korea");
            remapName.put("Hong Kong�*", "Hong Kong SAR China");
            remapName.put("Bosnia-Herzegovina", "Bosnia and Herzegovina");
            remapName.put("Kyrgystan", "Kyrgyzstan");
            remapName.put("Cote d'Ivoire", "Côte d’Ivoire");
            remapName.put("Afganistan", "Afghanistan");
            remapName.put("Trinidad & Tobago", "Trinidad and Tobago");
            remapName.put("Kosovo", "Serbia");
            remapName.put("Palestine(West Bk.)", "Palestinian Territories");
            remapName.put("Congo, Dem. Rep.", "Congo - Kinshasa");
            remapName.put("Reunion (FR)", "Réunion");
            remapName.put("Macao�*", "Macau SAR China");
            remapName.put("Brunei Darussalem", "Brunei");
            remapName.put("Congo", "Congo - Brazzaville");
            remapName.put("Papau New Guinea", "Papua New Guinea");
            remapName.put("Myanmar", "Myanmar [Burma]");
            remapName.put("St. Vincent & Grenadines", "Saint Vincent and the Grenadines");
            remapName.put("Antigua & Barbuda", "Antigua and Barbuda");
            remapName.put("Guernsey & Alderney", "Guernsey");
            remapName.put("US Virgin Islands", "U.S. Virgin Islands");
            remapName.put("Sao Tome & Principe", "São Tomé and Príncipe");
            remapName.put("Central African Rep.", "Central African Republic");
            remapName.put("St. Kitts & Nevis", "Saint Kitts and Nevis");
            remapName.put("Northern Marianas", "Northern Mariana Islands");
            remapName.put("Monserrat", "Montserrat");
            remapName.put("Wallis & Futuna", "Wallis and Futuna");
            remapName.put("Saint Helena (UK)", "Saint Helena");
            remapName.put("Vatican City State", "Vatican City");
        }

        protected String[] splitLine(String line) {
            return TABS.split(line);
        }

        public SequenceHandler(Map<String, R2<Integer, Integer>> rawLanguageToSequencesCounter2) {
            country2internetUsers = rawLanguageToSequencesCounter2;
        }

        @Override
        protected boolean isCodePoint() {
            return false;
        }
        @Override
        protected boolean handleLine(int start, int end, String[] items) {
            String code = name2code.get(items[0]);
            String rename = remapName.get(items[0]);
            if (rename != null) {
                code = name2code.get(rename);
            }
            if (code == null) {
                //System.out.println("remapName.put(\"" + items[0] + "\", \"XX\");");
                code = items[0];
            }
            country2internetUsers.put(code, 
                    Row.of(Integer.parseInt(items[1].replace(",","")), Integer.parseInt(items[2].replace(",",""))));
            return true;
        }
        @Override
        protected void handleEnd() {
            Set<String> missing = new TreeSet(testInfo.getStandardCodes().getGoodAvailableCodes("territory"));
            missing.removeAll(country2internetUsers.keySet());
            for (String s : missing) {
                System.out.println("//missing\t" + s + "\t" + testInfo.getEnglish().getName(CLDRFile.TERRITORY_NAME, s));
            }
        }
    }

    private static void countryPopulation() {
        Set<R4<Double, Double, String, Integer>> byPopulation = new TreeSet<R4<Double, Double, String, Integer>>();

        for (String code : testInfo.getStandardCodes().getGoodAvailableCodes("territory")) {
            Set<Integer> numbers = testInfo.getSupplementalDataInfo().numericTerritoryMapping
            .getAll(code);
            if (numbers == null) {
                //System.out.println("Skipping " + code);
                continue;
            }
            for (Integer regionNumber : numbers) {
                PopulationData population = testInfo.getSupplementalDataInfo()
                .getPopulationDataForTerritory(code);
                if (population == null) {
                    System.out.println("Skipping " + code + ", " + regionNumber);
                    continue;
                }
                R4<Double, Double, String, Integer> items = Row
                .of(population.getPopulation(), population.getGdp(), code, regionNumber);
                byPopulation.add(items);
            }
        }
        for (R4<Double, Double, String, Integer> row : byPopulation) {
            final String name = row.get2();
            System.out.println(testInfo.getEnglish().getName(CLDRFile.TERRITORY_NAME, name)
                    + "\t" + row.get0()
                    + "\t" + row.get1()
                    + "\t" + name
                    + "\t" + row.get3());
        }
    }
}