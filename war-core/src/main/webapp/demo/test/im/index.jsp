<%@page import="java.lang.String"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="org.silverpeas.image.imagemagick.Im4javaManager"%>
<%@page import="org.im4java.process.ProcessStarter"%>
<%@page import="org.im4java.core.IMOperation"%>
<%@page import="org.im4java.core.ConvertCmd"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>

  <%
    // Im4java settings
    for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
      if ("path".equals(entry.getKey().toLowerCase())) {
        StringBuilder sb = new StringBuilder();
        for (String path : entry.getValue().split(";")) {
          String newPath = path.toLowerCase().replaceAll("windows", "");
          if (path.length() == newPath.length()) {
            if (sb.length() > 0) {
              sb.append(";");
            }
            sb.append(path);
          }
        }
        try {
          final ConvertCmd cmd = new ConvertCmd();
          cmd.setSearchPath(sb.toString());
          cmd.run(new IMOperation().version());
          ProcessStarter.setGlobalSearchPath(sb.toString());
        } catch (final Exception e) {
          ProcessStarter.setGlobalSearchPath(null);
          out.println("<b>Variable PATH : </b><br/>");
          out.println(sb.toString());
          out.println("<br/>");
          out.println("<b>Exception message : </b><br/>");
          out.println(e.getMessage());
          e.printStackTrace();
        }
      }
    }

    if (Im4javaManager.isActivated()) {

      out.println("<br/>");
      out.println("<br/>");
      out.println("<br/>");
      out.println("Image Magick est détecté ... (" + ProcessStarter.getGlobalSearchPath() + ")");

    }
  %>

</body>
</html>