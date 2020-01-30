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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class SumServlet extends HttpServlet {


    // Code heavily based on https://plumbr.io/blog/java/how-to-use-asynchronous-servlets-to-improve-performance
    private static final BlockingQueue<AsyncContext> queue = new ArrayBlockingQueue<>(20);
    private static ExecutorService executorService = Executors.newScheduledThreadPool(20);
    private static final AtomicLong sum = new AtomicLong(0);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    private static void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (BufferedReader br = request.getReader()) {
            String requestContent = br.readLine();

            if (requestContent.equals("end")) {
                addToWaitingList(request.startAsync());
                executorService.execute(SumServlet::endEvent);
            }

            else if (requestContent.matches("^\\d{1,11}$")) {
                sum.updateAndGet(v -> v + Long.parseLong(requestContent));
                AsyncContext ac = request.startAsync();
                // Request set to time out after 1000 seconds
                ac.setTimeout(1000000);
                addToWaitingList(ac);
            }

            else {
                try (PrintWriter pw = response.getWriter()) {
                    pw.println("Invalid request");
                }
            }
        }
    }

    private static void addToWaitingList(AsyncContext c) {
        queue.add(c);
    }

    private static void endEvent() {
        List<AsyncContext> clients = new ArrayList<>(queue.size());
        queue.drainTo(clients);
        clients.parallelStream().forEach((AsyncContext ac) -> {
            ServletResponse response = ac.getResponse();
            try (PrintWriter pw = response.getWriter()) {
                pw.println(sum);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ac.complete();
        });
        sum.set(0);
    }
}
