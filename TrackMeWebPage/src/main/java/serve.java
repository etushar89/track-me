import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Servlet implementation class serve
 */
@WebServlet("/serve")
public class serve extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private locInfo loc;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public serve() {
        super();
        loc = new locInfo();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        synchronized (loc) {
            out.write(loc.getLati() + "/" + loc.getLongi() + "/" + loc.getDate());
        }
        out.flush();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        InputStream i = request.getInputStream();
        InputStreamReader sr = new InputStreamReader(i);

        int data;
        String locStr = "";
        while ((data = sr.read()) != -1) {
            locStr = locStr + (char) data;
        }

        String[] splits = locStr.split("/");

        synchronized (loc) {
            loc.setLati(Float.parseFloat(splits[0]));
            loc.setLongi(Float.parseFloat(splits[1]));
            loc.setDate(splits[2]);
        }
    }

    private class locInfo {
        private float lati;
        private float longi;
        private String dt;

        public locInfo() {
            lati = 41.8394295f;
            longi = -87.6165938f;
        }

        float getLati() {
            return lati;
        }

        void setLati(float lati) {
            this.lati = lati;
        }

        float getLongi() {
            return longi;
        }

        void setLongi(float longi) {
            this.longi = longi;
        }

        String getDate() {
            return dt;
        }

        void setDate(String dt) {
            this.dt = dt;
        }
    }
}
