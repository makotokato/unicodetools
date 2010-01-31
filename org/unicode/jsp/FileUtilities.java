package org.unicode.jsp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Pattern;

public final class FileUtilities {

  public static abstract class SemiFileReader {
    public final static Pattern SPLIT = Pattern.compile("\\s*;\\s*");

    protected void handleStart() {}
    protected abstract void handleLine(int start, int end, String[] items);
    protected void handleEnd() {}
    
    protected boolean isCodePoint() {
      return true;
    }

    protected String[] splitLine(String line) {
      return SPLIT.split(line);
    }
    
    public SemiFileReader process(Class classLocation, String fileName) {
      handleStart();
      BufferedReader in;
      String line = null;
      int lineCount = 1;
      try {
        in = FileUtilities.openFile(classLocation, fileName);
        for (; ; ++lineCount) {
          line = in.readLine();
          if (line == null) {
            break;
          }
          int comment = line.indexOf("#");
          if (comment >= 0) {
            line = line.substring(0,comment);
          }
          if (line.startsWith("\uFEFF")) {
            line = line.substring(1);
          }
          line = line.trim();
          if (line.length() == 0) {
            continue;
          }
          String[] parts = splitLine(line);
          int start, end;
          if (isCodePoint()) {
            String source = parts[0];
            int range = source.indexOf("..");
            if (range >= 0) {
              start = Integer.parseInt(source.substring(0,range),16);
              end = Integer.parseInt(source.substring(range+2),16);
            } else {
              start = end = Integer.parseInt(source, 16);
            }
          } else {
            start = end = -1;
          }
          handleLine(start, end, parts);
        }
        in.close();
        handleEnd();
      } catch (Exception e) {
        throw (RuntimeException) new IllegalArgumentException(lineCount + ":\t" + line).initCause(e);
      }
      return this;
    }

  }
  //
  //  public static SemiFileReader fillMapFromSemi(Class classLocation, String fileName, SemiFileReader handler) {
  //    return handler.process(classLocation, fileName);
  //  }

  public static BufferedReader openFile(Class class1, String file) throws IOException {
    //URL path = null;
    //String externalForm = null;
    try {
      //      //System.out.println("Reading:\t" + file1.getCanonicalPath());
      //      path = class1.getResource(file);
      //      externalForm = path.toExternalForm();
      //      if (externalForm.startsWith("file:")) {
      //        externalForm = externalForm.substring(5);
      //      }
      //      File file1 = new File(externalForm);
      //      boolean x = file1.canRead();
      //      final InputStream resourceAsStream = new FileInputStream(file1);
      final InputStream resourceAsStream = class1.getResourceAsStream(file);
      InputStreamReader reader = new InputStreamReader(resourceAsStream, FileUtilities.UTF8);
      BufferedReader bufferedReader = new BufferedReader(reader,1024*64);
      return bufferedReader;
    } catch (Exception e) {
      File file1 = new File(file);
      throw (RuntimeException) new IllegalArgumentException("Bad file name: "
              //              + path + "\t" + externalForm + "\t" + 
              + file1.getCanonicalPath()
              + "\r\n" + new File(".").getCanonicalFile() + " => " + Arrays.asList(new File(".").getCanonicalFile().list())).initCause(e);
    }
  }

  public static final Charset UTF8 = Charset.forName("utf-8");

}