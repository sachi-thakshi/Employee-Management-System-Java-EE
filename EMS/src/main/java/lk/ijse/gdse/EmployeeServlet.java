package lk.ijse.gdse;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/employee/*")
@MultipartConfig
public class EmployeeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getParameter("_method");
        if ("PUT".equalsIgnoreCase(method)) {
            doPut(req, resp);
            return;
        }

        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter out = resp.getWriter();

        String ename = req.getParameter("ename");
        String enumber = req.getParameter("enumber");
        String eaddress = req.getParameter("eaddress");
        String edepartment = req.getParameter("edepartment");
        String estatus = req.getParameter("estatus");

        Part filePart = req.getPart("eimage");
        String originalFileName = filePart.getSubmittedFileName();
        String fileName = UUID.randomUUID() + "_" + originalFileName;

        String uploadPath = "D:\\AAD B72\\JAVA EE\\PROJECTS\\WORK\\EMS-FN\\assets\\images";
        java.io.File uploadDir = new java.io.File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String fileAbsolutePath = uploadPath + java.io.File.separator + fileName;
        filePart.write(fileAbsolutePath);

        ServletContext sc = req.getServletContext();
        BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");

        try (Connection connection = ds.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement(
                    "INSERT INTO employee (eid, ename, enumber, eaddress, edepartment, estatus, eimage) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)"
            );

            String eid = UUID.randomUUID().toString();

            pstm.setString(1, eid);
            pstm.setString(2, ename);
            pstm.setString(3, enumber);
            pstm.setString(4, eaddress);
            pstm.setString(5, edepartment);
            pstm.setString(6, estatus);
            pstm.setString(7, fileName); // relative path for web access

            int executed = pstm.executeUpdate();

            if (executed > 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(out, Map.of(
                        "code", "200",
                        "status", "success",
                        "message", "Employee successfully created!"
                ));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(out, Map.of(
                        "code", "400",
                        "status", "error",
                        "message", "Failed to create employee record."
                ));
            }

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(out, Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Internal server error."
            ));
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // This will be "/{id}" or null
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();

        ServletContext sc = req.getServletContext();
        BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");

        try (Connection connection = ds.getConnection()) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // No ID specified, return all employees
                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM employee");
                ResultSet rs = pstm.executeQuery();

                List<Map<String, String>> employees = new ArrayList<>();
                while (rs.next()) {
                    Map<String, String> emp = new HashMap<>();
                    emp.put("eid", rs.getString("eid"));
                    emp.put("ename", rs.getString("ename"));
                    emp.put("enumber", rs.getString("enumber"));
                    emp.put("eaddress", rs.getString("eaddress"));
                    emp.put("edepartment", rs.getString("edepartment"));
                    emp.put("estatus", rs.getString("estatus"));
                    emp.put("eimage", rs.getString("eimage"));
                    employees.add(emp);
                }

                resp.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(resp.getWriter(), Map.of(
                        "code", "200",
                        "status", "success",
                        "data", employees
                ));
            } else if (pathInfo.startsWith("/")) {
                // ID specified, return the single employee
                String eid = pathInfo.substring(1); // Remove leading "/"

                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM employee WHERE eid = ?");
                pstm.setString(1, eid);
                ResultSet rs = pstm.executeQuery();

                if (rs.next()) {
                    Map<String, String> emp = new HashMap<>();
                    emp.put("eid", rs.getString("eid"));
                    emp.put("ename", rs.getString("ename"));
                    emp.put("enumber", rs.getString("enumber"));
                    emp.put("eaddress", rs.getString("eaddress"));
                    emp.put("edepartment", rs.getString("edepartment"));
                    emp.put("estatus", rs.getString("estatus"));
                    emp.put("eimage", rs.getString("eimage"));

                    resp.setStatus(HttpServletResponse.SC_OK);
                    mapper.writeValue(resp.getWriter(), Map.of(
                            "code", "200",
                            "status", "success",
                            "data", emp
                    ));
                } else {
                    // Employee not found
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), Map.of(
                            "code", "404",
                            "status", "failed",
                            "message", "Employee not found"
                    ));
                }
            } else {
                // Invalid pathInfo format
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(), Map.of(
                        "code", "400",
                        "status", "failed",
                        "message", "Invalid URL"
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Internal Server Error"
            ));
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();

        ServletContext sc = req.getServletContext();
        BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");

        try (Connection connection = ds.getConnection()) {
            String eid = null;
            String ename = null;
            String enumber = null;
            String eaddress = null;
            String edepartment = null;
            String estatus = null;
            String eimageFileName = null;

            // Read form data and file parts
            for (Part part : req.getParts()) {
                String fieldName = part.getName();
                if (part.getContentType() == null) {
                    // regular form field
                    String value = new BufferedReader(new InputStreamReader(part.getInputStream()))
                            .lines().collect(Collectors.joining("\n"));
                    switch (fieldName) {
                        case "eid": eid = value; break;
                        case "ename": ename = value; break;
                        case "enumber": enumber = value; break;
                        case "eaddress": eaddress = value; break;
                        case "edepartment": edepartment = value; break;
                        case "estatus": estatus = value; break;
                    }
                } else if (fieldName.equals("eimage")) {
                    // file upload (new image)
                    String originalFileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                    if (originalFileName != null && !originalFileName.isEmpty()) {
                        // Save uploaded file
                        File uploads = new File("D:\\AAD B72\\JAVA EE\\PROJECTS\\WORK\\EMS-FN\\assets\\images");
                        if (!uploads.exists()) uploads.mkdirs();

                        // Use UUID prefix to avoid name clashes
                        eimageFileName = UUID.randomUUID() + "_" + originalFileName;

                        File file = new File(uploads, eimageFileName);
                        try (InputStream input = part.getInputStream()) {
                            Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }

            // Get existing image filename from DB if no new image uploaded
            if (eid == null || eid.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(), Map.of(
                        "code", "400",
                        "status", "failed",
                        "message", "Employee ID is missing"
                ));
                return;
            }

            if (eimageFileName == null) {
                PreparedStatement checkStmt = connection.prepareStatement("SELECT eimage FROM employee WHERE eid = ?");
                checkStmt.setString(1, eid);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    eimageFileName = rs.getString("eimage");
                }
                rs.close();
                checkStmt.close();
            }

            // Update employee with all fields including image filename
            String sql = "UPDATE employee SET ename=?, enumber=?, eaddress=?, edepartment=?, estatus=?, eimage=? WHERE eid=?";
            PreparedStatement pstm = connection.prepareStatement(sql);
            pstm.setString(1, ename);
            pstm.setString(2, enumber);
            pstm.setString(3, eaddress);
            pstm.setString(4, edepartment);
            pstm.setString(5, estatus);
            pstm.setString(6, eimageFileName);
            pstm.setString(7, eid);

            int updated = pstm.executeUpdate();

            if (updated > 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(resp.getWriter(), Map.of(
                        "code", "200",
                        "status", "success",
                        "message", "Employee updated successfully"
                ));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), Map.of(
                        "code", "404",
                        "status", "failed",
                        "message", "Employee not found"
                ));
            }

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Internal Server Error"
            ));
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");

        String pathInfo = req.getPathInfo(); // e.g. /<eid>
        if (pathInfo == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), Map.of(
                    "code", "400",
                    "status", "failed",
                    "message", "Employee ID is missing"
            ));
            return;
        }

        String eid = pathInfo.substring(1); // remove leading "/"

        ServletContext sc = req.getServletContext();
        BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");

        try (Connection connection = ds.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM employee WHERE eid = ?");
            pstm.setString(1, eid);

            int deleted = pstm.executeUpdate();

            PrintWriter out = resp.getWriter();
            if (deleted > 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(out, Map.of(
                        "code", "200",
                        "status", "success",
                        "message", "Employee deleted successfully"
                ));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(out, Map.of(
                        "code", "404",
                        "status", "failed",
                        "message", "Employee not found"
                ));
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Internal Server Error"
            ));
            throw new RuntimeException(e);
        }
    }
}
