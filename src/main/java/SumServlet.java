import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class SumServlet extends HttpServlet {

    // Code based on https://plumbr.io/blog/java/how-to-use-asynchronous-servlets-to-improve-performance
    private static final BlockingQueue<AsyncContext> queue = new ArrayBlockingQueue<>(20);
    private static final AtomicLong sum = new AtomicLong(0);

    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    private static synchronized void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (BufferedReader br = request.getReader()) {
            String requestContent = br.readLine();

            if (requestContent.equals("end")) {
                addToWaitingList(request.startAsync(request, response));
                endEvent();
            }
            else if (requestContent.matches("^\\d{1,11}$")) {
                sum.updateAndGet(v -> v + Long.parseLong(requestContent));
                AsyncContext ac = request.startAsync(request, response);
                // Request arbitrarily set to time out after 60 seconds
                ac.setTimeout(60000);
                addToWaitingList(ac);
            }
            else {
                try (PrintWriter pw = response.getWriter()) {
                    pw.println("Invalid request");
                }
            }
        }
    }

    private synchronized static void addToWaitingList(AsyncContext ac) {
        queue.add(ac);
    }

    private static synchronized void endEvent() {
        List<AsyncContext> clients = new ArrayList<>(queue.size());
        queue.drainTo(clients);
        clients.parallelStream().forEach((AsyncContext ac) -> {
            ServletResponse response = ac.getResponse();
            try (PrintWriter pw = response.getWriter()) {
                pw.print(sum.longValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
            ac.complete();
        });
        sum.set(0);
    }
}