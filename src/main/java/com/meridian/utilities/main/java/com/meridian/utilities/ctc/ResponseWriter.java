package main.java.com.meridian.utilities.main.java.com.meridian.utilities.ctc;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseWriter {
    public void writeIn(HttpServletResponse response, String xml) throws IOException {
        response.setContentType("application/xml");
        response.getWriter().write(xml);
    }
}