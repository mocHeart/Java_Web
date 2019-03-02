package moc.servlet;

import moc.domain.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  用户注册的初始化的Servlet
 */
public class InitServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        // 创建一个List集合用于保存用户注册的信息
        List<User> list = new ArrayList<User>();
        // 将list保存到ServletContext作用域中
        this.getServletContext().setAttribute("list", list);
    }
}
