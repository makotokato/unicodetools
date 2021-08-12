package org.unicode.jsp;

import com.ibm.icu.dev.util.UnicodeMap;
import com.ibm.icu.text.Normalizer;
import com.ibm.icu.text.StringPrepParseException;
import com.ibm.icu.text.StringTransform;
import com.ibm.icu.text.UnicodeSet;

public class Idna implements StringTransform {

  public enum IdnaType {
    valid, ignored, mapped, deviation, disallowed;
  }

  public UnicodeMap<IdnaType> types = new UnicodeMap<IdnaType>();
  public UnicodeMap<String> mappings = new UnicodeMap<String>();
  protected UnicodeMap<String> mappings_display = new UnicodeMap<String>();
  protected UnicodeSet validSet = new UnicodeSet();
  protected UnicodeSet validSet_transitional = new UnicodeSet();
  protected boolean checkPunycodeValidity = false;
  private final String name;
  
  protected Idna() {
    String[] names = this.getClass().getName().split("[.]");
    name = names[names.length-1];
  }

  public IdnaType getType(int i) {
    return types.get(i);
  }

  public String getName() {
    return name;
  }

  public String transform(String source) {
    return transform(source, false);
  }

  public String transform(String source, boolean display) {
    String remapped = (display ? mappings_display : mappings).transform(source);
    return Normalizer.normalize(remapped, Normalizer.NFC);
  }

  public String toUnicode(String source, boolean[] error, boolean display) {
    error[0] = false;
    String remapped = transform(source, display);
    StringBuilder result = new StringBuilder();
    for (String label : remapped.split("[.]")) {
      if (result.length() != 0) {
        result.append('.');
      }
      String fixedLabel = fromPunycodeOrUnicode(label, error);
      result.append(fixedLabel);
      if (!error[0] && (checkPunycodeValidity || label.equals(fixedLabel))) {
        if (isValidLabel(fixedLabel, true) != 0) {
          error[0] = true;
        }
      }
    }
    String resultString = result.toString();
    return resultString;
  }

  protected String fromPunycodeOrUnicode(String label, boolean[] error) {
    if (!label.startsWith("xn--")) {
      return label;
    }
    try {
      StringBuffer temp = new StringBuffer();
      temp.append(label.substring(4));
      StringBuffer depuny = Punycode.decode(temp, null);
      return depuny.toString();
    } catch (StringPrepParseException e) {
      error[0] = true;
      return label;
    }
  }

  public String toPunyCode(String source, boolean[] error) {
    String clean = toUnicode(source, error, false);
    StringBuilder result = new StringBuilder();
    for (String label : clean.split("[.]")) {
      if (result.length() != 0) {
        result.append('.');
      }
      if (IdnaTypes.LABEL_ASCII.containsAll(label)) {
        result.append(label);
      } else {
        try {
          StringBuffer temp = new StringBuffer();
          temp.append(label);
          StringBuffer depuny = Punycode.encode(temp, null);
          result.append("xn--").append(depuny);
        } catch (StringPrepParseException e) {
          error[0] = true;
          result.append(label);
        }
      }
    }
    return result.toString();
  }
  
  public int isValidLabel(String string, boolean display) {
    /*
The label must contain at least one code point.
The label must not contain a U+002D HYPHEN-MINUS character in both the third position and fourth positions.
The label must neither begin nor end with a U+002D HYPHEN-MINUS character.
The label must be in Unicode Normalization Form NFC.
The label must not contain a U+002E ( . ) FULL STOP.
Each code point in the label must only have certain status values according to Section 5, IDNA Mapping Table:
For Transitional Processing, each value must be valid.
For Nontransitional Processing, each value must be either valid or deviation.
The label must not begin with a combining mark, that is: General_Category=Mark.
     */
    if (string.length() == 0) return 1;
    if (string.length() > 3 && string.charAt(2) == '-' && string.charAt(3) == '-') return 2; // fix to use code points
    if (string.startsWith("-") || string.endsWith("-")) return 3;
    if (!Normalizer.isNormalized(string, Normalizer.NFC, 0)) return 4;
    if (string.contains(".")) return 5;
    if (!(display ? validSet_transitional : validSet).containsAll(string)) return 6;
    if (IdnaTypes.COMBINING_MARK.contains(string.codePointAt(0))) return 7;
    return 0;
  }

  public boolean isValid(String string) {
    String trans = transform(string);
    return Normalizer.isNormalized(trans, Normalizer.NFC, 0) && validSet.containsAll(trans);
  }
}
