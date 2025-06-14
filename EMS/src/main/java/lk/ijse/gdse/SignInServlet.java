package lk.ijse.gdse;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@WebServlet("/signin")
public class SignInServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> user = mapper.readValue(req.getInputStream(), Map.class);
        ServletContext sc = req.getServletContext();
        BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");

        //user login
        String email = user.get("UserEmail");
        String password = user.get("UserPassword");
        System.out.println("Email: "+email+" Password: "+password);

        try {
            Connection connection = ds.getConnection();
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM systemusers WHERE UserEmail=? AND UserPassword=?");
            pstm.setString(1,email);
            pstm.setString(2,password);
            ResultSet rs = pstm.executeQuery();
            PrintWriter out = resp.getWriter();
            resp.setContentType("application/json");
            if(rs.next()){
                resp.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(out,Map.of("code","200",
                                             "status","success",
                                             "message","User logged in successfully"
                ));
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                mapper.writeValue(out,Map.of("code","401",
                                             "status","failed",
                                             "message","User login failed"
                ));
            }

        } catch (SQLException e) {
            PrintWriter out = resp.getWriter();
            mapper.writeValue(out,Map.of("code","500",
                                         "status","failed",
                                         "message","Internal Server Error"
            ));
            throw new RuntimeException(e);
        }
    }
}
