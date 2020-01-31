import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import static org.junit.Assert.assertEquals;

public class SumServletTest extends Mockito {

    @Mock
    HttpServletRequest requestEnd;

    @Mock
    HttpServletRequest requestFive;

    @Mock
    HttpServletRequest requestFloat;

    @Mock
    HttpServletResponse response;

    @Mock
    BufferedReader brEnd;

    @Mock
    BufferedReader brFive;

    @Mock
    BufferedReader brFloat;

    @Mock
    AsyncContext ac;

    SumServlet sumServlet = new SumServlet();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(brEnd.readLine()).thenReturn("end");
        when(requestEnd.getReader()).thenReturn(brEnd);

        when(brFive.readLine()).thenReturn("5");
        when(requestFive.getReader()).thenReturn(brFive);

        when(brFloat.readLine()).thenReturn("1.1");
        when(requestFloat.getReader()).thenReturn(brFloat);

        when(requestFive.startAsync(requestFive, response)).thenReturn(ac);
        when(requestEnd.startAsync(requestEnd, response)).thenReturn(ac);
        when(ac.getResponse()).thenReturn(response);
    }

    @Test
    public void testServletSum10() throws IOException, ServletException {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        sumServlet.doPost(requestFive, response);
        sumServlet.doPost(requestFive, response);
        sumServlet.doPost(requestEnd, response);

        try {
            Thread.sleep(200);
            assertEquals("10", sw.toString().trim());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testServletOnFloatRequest() throws IOException, ServletException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        sumServlet.doPost(requestFloat, response);

        try {
            Thread.sleep(200);
            assertEquals("Invalid request", sw.toString().trim());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testServlet20Threads() throws IOException, ServletException {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        for (int i = 0; i < 19; i++) {
            sumServlet.doPost(requestFive, response);
        }
        sumServlet.doPost(requestEnd, response);

        try {
            Thread.sleep(200);
            assertEquals("95", sw.toString().trim());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test(expected = IllegalStateException.class)
    public void testServlet25Threads() throws IOException, ServletException {


        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        for (int i = 0; i < 24; i++) {
            sumServlet.doPost(requestFive, response);
        }
        sumServlet.doPost(requestEnd, response);
    }

    @Test
    public void testServletMultipleEndRequests() throws IOException, ServletException {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        sumServlet.doPost(requestFive, response);
        sumServlet.doPost(requestEnd, response);

        sw = new StringWriter();
        PrintWriter pw2 = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw2);

        for (int i = 0; i < 5; i++) {
            sumServlet.doPost(requestFive, response);
        }
        sumServlet.doPost(requestEnd, response);

        try {
            Thread.sleep(200);
            assertEquals("25", sw.toString().trim());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
