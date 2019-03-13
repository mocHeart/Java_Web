package sso.filter;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

public class UserFilter implements Filter {
    private String server;
    private String app;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        server = filterConfig.getInitParameter("server");
        app = filterConfig.getInitParameter("app");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (Objects.equals("/ssoLogout", ((HttpServletRequest) request).getServletPath())) {
            ((HttpServletResponse) response).sendRedirect(server + "/ssoLogout?source=" + app);
            return;
        }


        String ticket = null;
        if (null != ((HttpServletRequest) request).getCookies()) {
            for (Cookie cookie : ((HttpServletRequest) request).getCookies()) {
                if (Objects.equals(cookie.getName(), "Ticket_Granting_Ticket")) {
                    ticket = cookie.getValue();
                    break;
                }
            }
        }

        // 如果从Cookie中拿到ticket
        if (!Objects.equals(null, ticket)) {
            // 判断cookie超时时间
            String[] values = ticket.split(":");
            // 在由URL拿ticket,进行判断
            ticket = request.getParameter("ticket");

            if (Long.valueOf(values[1]) < System.currentTimeMillis()) {  // 超时
                // 不是单点登录来的
                if (Objects.equals(null, ticket)) {
                    ((HttpServletResponse) response).sendRedirect(server + "/ssoLogin?source=" + app);
                    return;
                } else {  // 单点登录回来的
                    //ticket带上cookie的有限期限
                    ticket = ticket + ":" + (System.currentTimeMillis() + 10000);  // 10秒
                    ((HttpServletResponse) response).addCookie(new Cookie("Ticket_Granting_Ticket", ticket));
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            // 应该继续用户校验，如果不是用户或非法用户，需要跳转到登录页面或者不需要登录的页面
            filterChain.doFilter(request, response);
            return;
        }

        // 如果没有从Cookie中拿到ticket
        // 从request域(SSO Sever的url传递过来的)中取出ticket的值，判断是否为空
        ticket = request.getParameter("ticket");
        if (!Objects.equals(null, ticket) && !Objects.equals("", ticket.trim())) {
            //ticket带上cookie的有限期限
            ticket = ticket + ":" + (System.currentTimeMillis() + 10000);  // 10秒

            ((HttpServletResponse) response).addCookie(new Cookie("Ticket_Granting_Ticket", ticket));
            filterChain.doFilter(request, response);
        } else {
            // 如果ticket为空（说明即没有从SSO Server拿到ticket，也没有同类WebApp为自己在浏览器放ticket的cookie），
            // 就转到SSO Server的登录页面，并带上源
            ((HttpServletResponse) response).sendRedirect(server + "/ssoLogin?source=" + app);
        }
    }

    @Override
    public void destroy() {

    }
}
