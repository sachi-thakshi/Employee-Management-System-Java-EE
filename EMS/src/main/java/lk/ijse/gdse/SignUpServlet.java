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
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@WebServlet("/signup")
public class SignUpServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> user = mapper.readValue(req.getInputStream(), Map.class);

        ServletContext sc = req.getServletContext();
        BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");
        try {
            Connection connection = ds.getConnection();
            PreparedStatement pstm =  connection.prepareStatement("INSERT INTO systemusers (UserID,UserName,UserPassword, UserEmail) VALUES (?,?,?,?)");
            pstm.setString(1, UUID.randomUUID().toString());
            pstm.setString(2,user.get("UserName"));
            pstm.setString(3,user.get("UserPassword"));
            pstm.setString(4,user.get("UserEmail"));
            int executed = pstm.executeUpdate();

            PrintWriter out = resp.getWriter();
            resp.setContentType("application/json");
            if(executed>0){
//                System.out.println("1");
                resp.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(out,Map.of("code","200",
                                             "status","success",
                                             "message","User registered successfully"
                ));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(out,Map.of("code","400",
                                             "status","failed",
                                             "message","User registration failed"
                ));
            }
//            System.out.println("0");
            connection.close();
        } catch (SQLException e) {
            PrintWriter out = resp.getWriter();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(out,Map.of("code","500",
                                         "status","failed",
                                         "message","Internal Server Error"
            ));
            throw new RuntimeException(e);
        }

    }
}