package sso.servlet;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServlet extends HttpServlet {

    // 记录同组的应用
    private String servers;

    // 十个线程的线程池
    private ExecutorService service = Executors.newFixedThreadPool(10);
    /**
     *  为server参数表示的应用设置cookie，也就是一个应用登录后，会调用setCookie方法为另一个应用设置cookie
     * @param server
     * @param ticket
     */
    private void syncCookie(String server, String ticket, String method) {
        // submit()方法的作用是向线程池提交一个Runnable任务用于执行
        service.submit(new Runnable() {
            @Override
            public void run() {
                // 创建请求方法的实例，并指明请求的URL
                // 如果需要发送POST请求，就创建HttpPost对象
                HttpPost httpPost = new HttpPost(server + "/" + method + "?ticket=" + ticket);
                CloseableHttpClient httpClient = null;
                CloseableHttpResponse response = null;
                try {
                    // 将请求发出去，就会触发另一个应用的Servlet中的
                    // servletPath为/setCookie的代码的执行，也就是为另一个应用添加了cookie
                    httpClient = HttpClients.createDefault();
                    // 请求httpPost并获得返回结果
                    response = httpClient.execute(httpPost);
                    // entity是添加cookie的同时存放到response中的ok, 得到该值就说明cookie添加成功了
                    HttpEntity entity = response.getEntity();
                    String responseContent = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("================== :" + responseContent);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (null != response) {
                            response.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (null != httpClient) {
                            httpClient.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // /main说明是服务端登录后转发过来的地址，从中取出domains的值，
        // 也就是另一个web应用的的地址，然后为该应用调用setCookie方法
        if (Objects.equals("/main", request.getServletPath())) {
            String domains = request.getParameter("domains");
            if (null != domains) {
                this.servers = domains;
            }
            String ticket = request.getParameter("ticket");

            if (null != domains && null != ticket) {   // 避免直接在地址栏上输入/main，导致domain和ticket为空
                for (String server : domains.split(",")) {
                    if (!Objects.equals(null, server) && !Objects.equals("", server.trim())) {
                        syncCookie(server, ticket, "setCookie");  // 新线程去执行
                    }
                }
            }

            request.getRequestDispatcher("/WEB-INF/views/main.jsp").forward(request, response);
        }
        // 当一个应用程序登录成功后调用setCookie()方法，
        // 会触发一个请求的执行，就会执行另一个应用的这段else if后的代码
        else if (Objects.equals("/setCookie", request.getServletPath())) {
            String ticket = request.getParameter("ticket");
            response.addCookie(new Cookie("Ticket_Granting_Ticket", ticket));
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/text; character=utf-8");
            PrintWriter out = null;
            try {
                out = response.getWriter();
                /* ok的值会在另一个应用的setCookie方法中获得，得到ok代表cookie添加成功啦 */
                out.write("ok");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != out) {
                    out.close();
                }
            }
        } else if (Objects.equals("/logout", request.getServletPath())) {
            Cookie cookie = new Cookie("Ticket_Granting_Ticket", null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            if (null != servers) {
                for (String server : servers.split(",")) {
                    if (!Objects.equals(null, server) && !Objects.equals("", server.trim())) {
                        syncCookie(server, "", "removeCookie");
                    }
                }
            }
            request.getRequestDispatcher("/WEB-INF/views/logout.jsp").forward(request, response);
        } else if (Objects.equals("/removeCookie", request.getServletPath())) {
            Cookie cookie = new Cookie("Ticket_Granting_Ticket", null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/text; charset=utf-8");
            PrintWriter out = null;
            try {
                out = response.getWriter();
                out.write("ok");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (null != out) {
                    out.close();
                }
            }
        }
    }
}















