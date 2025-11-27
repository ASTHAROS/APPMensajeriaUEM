package com.appmsg.appmensajeriauem;

import com.appmsg.appmensajeriauem.model.User;
import com.appmsg.appmensajeriauem.repository.UserRepository;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import static com.appmsg.appmensajeriauem.utils.PassUtils.hashPassword;

@WebServlet("/user")
public class UserServlet extends HttpServlet {

    private UserRepository repo;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        repo = new UserRepository();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (email == null || email.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Falta el parámetro email\"}");
            return;
        }

        User user = repo.getUserByEmail(email);

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"Usuario no encontrado\"}");
            return;
        }

        String json = gson.toJson(user);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(json);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        try {
            User user = gson.fromJson(sb.toString(), User.class);
            String plainPass = user.getPassword();
            user.setPassword(hashPassword(plainPass));

            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"El usuario debe tener ID\"}");
                return;
            }

            repo.createUser(user);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"message\":\"Usuario creado correctamente\"}");

        } catch (JsonSyntaxException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"JSON inválido\"}");
        }
    }
}