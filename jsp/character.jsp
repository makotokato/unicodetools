﻿<html>

<head>
<meta http-equiv="Content-Language" content="en-us">
<meta name="GENERATOR" content="Microsoft FrontPage 6.0">
<meta name="ProgId" content="FrontPage.Editor.Document">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Unicode Property Demo</title>
<link rel="stylesheet" type="text/css" href="index.css">
<style>
<!--
th           { text-align: left }
-->
</style>

<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="com.ibm.icu.text.*" %>
<%@ page import="com.ibm.icu.lang.*" %>
 <%@ page import="com.ibm.icu.util.*" %>
<%@ page import="java.util.regex.*" %>
 <%@ page import="com.ibm.icu.dev.demo.translit.*" %>
<%@ page import="com.ibm.icu.impl.*" %>

<%@ page import="com.ibm.icu.text.*" %> 
<%@ page import="com.ibm.icu.lang.*" %>
<%@ page import="com.ibm.icu.impl.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="jsp.*" %>
<%@ page import="org.unicode.cldr.icu.*" %>

</head>

<body>

<h1>Unicode Property Demo</h1>
<p>
	<a target="character" href="character.jsp">character</a> |
	<a target="properties" href="properties.jsp">properties</a> |
	<a target="list" href="list-unicodeset.jsp">unicode-set</a> |
	<a target="compare" href="unicodeset.jsp">compare-sets</a> |
	<a target="help" href="index.jsp">help</a>
</p>
<form name="myform" action="http://unicode.org/cldr/utility/character.jsp" method="POST">
  <%
		request.setCharacterEncoding("UTF-8");
		String text = request.getParameter("a");
		if (text == null || text.length() == 0) text = "a";
%> <%
String BASE_RULES =
  	"'<' > '&lt;' ;" +
    "'<' < '&'[lL][Tt]';' ;" +
    "'&' > '&amp;' ;" +
    "'&' < '&'[aA][mM][pP]';' ;" +
    "'>' < '&'[gG][tT]';' ;" +
    "'\"' < '&'[qQ][uU][oO][tT]';' ; " +
    "'' < '&'[aA][pP][oO][sS]';' ; ";

String CONTENT_RULES =
    "'>' > '&gt;' ;";

String HTML_RULES = BASE_RULES + CONTENT_RULES + 
"'\"' > '&quot;' ; ";

String HTML_RULES_CONTROLS = HTML_RULES + 
		"([[:C:][:Z:][:whitespace:][:Default_Ignorable_Code_Point:][\\u0080-\\U0010FFFF]-[\\u0020]]) > &hex/xml($1) ; ";


Transliterator toHTML = Transliterator.createFromRules(
        "any-xml", HTML_RULES_CONTROLS, Transliterator.FORWARD);
        
		String HTML_INPUT = "::hex-any/xml10; ::hex-any/unicode; ::hex-any/java;";
		Transliterator fromHTML = Transliterator.createFromRules(
				"any-xml", HTML_INPUT, Transliterator.FORWARD);
		
		text = fromHTML.transliterate(text);

		if (text.length() > 2) {
		    try {
			  text = UTF16.valueOf(Integer.parseInt(text,16));
			} catch (Exception e) {}
		}
		int cp = UTF16.charAt(text, 0);
		String nextHex = "character.jsp?a=" + Utility.hex(cp < 0x110000 ? cp+1 : 0, 4);
		String prevHex = "character.jsp?a=" + Utility.hex(cp > 0 ? cp-1 : 0x10FFFF, 4);
%>
  <p><input type="button" value="Previous" name="B3" onClick="window.location.href='<%=prevHex%>'">
  <input type="text" name="a" size="10" value="<%=text%>">
  <input type="submit" value="Show" name="B1">
  <input type="button" value="Next" name="B2" onClick="window.location.href='<%=nextHex%>'"></p>
</form>
<%
	UnicodeUtilities.showProperties(text, out); 
%>
<p><i>(only includes properties with non-default values)<br>
</i>® = Regex Property (<a href="http://www.unicode.org/reports/tr18/">UTS #18</a>): not formal 
Unicode property<br>
© = ICU-Only Property (not Unicode or Regex)<br>
<i><br>
<p>Version 3<br>
ICU version: <%= com.ibm.icu.util.VersionInfo.ICU_VERSION.toString() %><br>
Unicode version: <%= com.ibm.icu.lang.UCharacter.getUnicodeVersion().toString() %><br>
</body>

</html>
