package sso.servlet;

import javafx.beans.binding.ObjectExpression;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class LoginServlet extends HttpServlet {
    private String domains;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        domains = config.getInitParameter("domains");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 由SSO Server登录页面（由注册页面重定向才能到该页面）提交过来的请求，source中带有重定向的源
        if (Objects.equals("/login", request.getServletPath())) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String source = request.getParameter("source");

            // ??
            if (null == source || Objects.equals("", source)) {
                // request.getHeader("referer") 获取来访者地址
                // 来访不是链接过来的，而是浏览器直接输地址过来的，此时该值为null
                String referer = request.getHeader("referer");
                // refer: http://127.0.0.1:800/ssoLogin?source=http://127.0.0.1:8082
                // 取出子串 http://127.0.0.1:8081，然后赋值给source
                source = referer.substring(referer.indexOf("source=") + 7);
            }

            String referer = request.getHeader("referer");
            System.out.println("====== SSO Server referer: " + referer);


            // 用户信息的校验
            // 如果正确，生成唯一标识UUID（授权令牌） 的ticket，和需要通知免登陆的web应用的地址
            if (Objects.equals(username, password)) {
                // 通过UUID.randomUUID().toString()会得到一个由字母和数字组成，用-连接的随机字符串，用replace()方法去掉-
                String ticket = UUID.randomUUID().toString().replace("-", "");
                System.out.println("*****************SSO Server ticket: " + ticket);
                response.sendRedirect(source + "/main?ticket=" + ticket + "&domains=" +
                        domains.replace(source + ",", "").replace("," + source, "").replace(source, ""));
            }

            // **sso-server拦截未登录请求
            // 失败，带上之前传过来的源，返回登录界面重新登录
            else {
                request.setAttribute("source", source);
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            }
        }
        // web应用重定向过来的登录请求，request中拥有请求web应用的源
        else if (Objects.equals("/ssoLogin", request.getServletPath())) {
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }

        // 退出登录
        else if (Objects.equals("/ssoLogout", request.getServletPath())) {
            String source = request.getParameter("source");

            //由于用户信息没有存储在内存或者类似memcache这样的缓存中，所以这里没有相关的助理
            //在ssoLogout请求时，传过来当前的用户名，根据用户名查找内存或者缓存，删除相应信息，以完成退出
            //用户从哪来？在实行ssoLogin时返回的ticket中，要包含用户的信息（能标识用户唯一性即可，uuid也可以，只是需要在sso的server中记录一下这个uuid和用户的对应关系）
            //webapp1或者webapp2在调用ssoLogout时把ticket传回来即可

            if (null == source || Objects.equals("", source)) {
                String referer = request.getHeader("referer");
                source = referer.substring(referer.indexOf("source=") + 7);
            }

            response.sendRedirect(source + "/logout?domains=" +
                    domains.replace(source + ",", "").replace("," + source, "").replace(",", ""));
        }
    }
}
