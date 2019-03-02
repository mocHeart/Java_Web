package moc.servlet;

import moc.domain.User;

import javax.jws.soap.SOAPBinding;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 用户登陆的Servlet
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/LoginServlet")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");  // 设置request的解析编码方式
        // 接受数据
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        // 从ServletContext域中获得保存的用户信息的集合
        List<User> list = (List<User>) this.getServletContext().getAttribute("list");
        for(User user : list){
            // 判断用户名是否正确
            if(username.equals(user.getUsername())){
                // 用户名正确
                if(password.equals(user.getPassword())){
                    // 密码正确
                    // 登录成功

                    // 完成记住用户名的功能
                    // 判断记住用户名的复选框是否勾上
                    String remember = request.getParameter("remember");
                    if ("true".equals(remember)){
                        // JSP中不能使用中文Cookie,需进行编码和解码
                        Cookie cookie = new Cookie("username", URLEncoder.encode(user.getUsername(), "UTF-8"));
                        // 解码示意： URLDecoder.decode(username, "UTF-8")
                        // cookie关闭浏览器，默认会丢失，需设置有效的路径和时间
                        //cookie.setPath("/reg_login_MVC");
                        // 设置有效的时间
                        cookie.setMaxAge(24*60*60);  // 保存24小时（单位为秒）
                        // 将cookie回写到浏览器
                        response.addCookie(cookie);
                    }


                    // 将用户的信息保存在session中
                    request.getSession().setAttribute("user", user);
                    // 重定向到登录成功页面
                    response.sendRedirect("success.jsp");
                    return;
                }
            }
        }
        // 登录失败
        request.setAttribute("msg", "用户名或密码错误！");
        // 转发到登录页面
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
}
