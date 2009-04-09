<html>
<head>
<%@ include file="header.jsp" %>
<title>Unicode Regex Utility Demo</title>
</head>

<body>
<%
        request.setCharacterEncoding("UTF-8");
        //response.setContentType("text/html;charset=UTF-8"); //this is redundant
        String queryString = request.getQueryString();
        
        UtfParameters utfParameters = new UtfParameters(queryString);
        
        String regex = utfParameters.getParameter("a");

        if (regex == null) {
            regex = "\\p{Nd}+([[:WB=MB:][:WB=MN:]]\\p{Nd}+)?";
        }
        String fixedRegex = UnicodeRegex.fix(regex);
                
        String test = utfParameters.getParameter("b");
        if (test == null) {
          test = "The 35 quick brown fox jumped over 1.234 lazy dogs: 1:234.";
        }
        String testPattern = UnicodeUtilities.showRegexFind(fixedRegex, test);
%>
<h1>Unicode Regex Utility Demo</h1>
<%@ include file="others.jsp" %>
<form name="myform">
  <table border="1" cellpadding="0" cellspacing="0" style="border-collapse: collapse; width:100%">
    <tr>
      <th style="width: 50%">Input</th>
    </tr>
    <tr>
      <td><textarea name="a" rows="8" cols="10" style="width: 100%"><%=regex%></textarea></td>
    </tr>
    <tr>
      <th style="width: 50%">TestText</th>
    </tr>
    <tr>
      <td><textarea name="b" rows="8" cols="10" style="width: 100%"><%=test%></textarea></td>
    </tr>
    <tr>
      <td><input id='main' type="submit" value="Show Modified Regex Pattern" onClick="window.location.href='regex.jsp?a='+document.getElementById('main').value"/>&nbsp;</td>
    </tr>
</table>
</form>
  <hr>
  <h2>Modified Regex Pattern</h2>
  <p><%=fixedRegex%></p>
  <hr>
  <h2>Underlined Find Values</h2>
  <p><%=testPattern%></p>
<%@ include file="footer.jsp" %>
</body>
</html>
