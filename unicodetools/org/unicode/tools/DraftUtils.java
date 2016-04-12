package org.unicode.tools;

import org.unicode.cldr.util.CLDRPaths;
import org.unicode.cldr.util.CldrUtility;
import org.unicode.text.utility.Settings;

/**
 * Stuff used by the 'draft' class that doesn't belong in CLDR core.
 * @author srl
 *
 */
public class DraftUtils {

    /**
     * This actually refers into the unicodetools project.
     */
    public static final String UCD_DIRECTORY = Settings.UCD_DIR + "/" + Settings.latestVersion + "-Update";

}
