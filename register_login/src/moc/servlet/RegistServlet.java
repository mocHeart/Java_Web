package moc.servlet;

import moc.domain.User;
import moc.utils.UploadUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户注册的Servlet
 */

@WebServlet(name = "RegistServlet", urlPatterns = "/RegistServlet")
public class RegistServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 数据的接受
        // 文件上传基本操作：
        try {
            // 1. 创建一个磁盘文件项工厂对象
            // commons-fileupload-1.2.1.jar
            DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
            // 2. 创建一个核心解析类
            ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
            // 3. 解析request请求，返回List集合，List集合存放的是FileItem对象
            List<FileItem> list = servletFileUpload.parseRequest(request);

            // 定义一个List集合, 用于保存兴趣爱好数据：
            List<String> hobbyList = new ArrayList<String>();
            // 定义一个Map集合用于保存接收到的数据：
            Map<String, String> map = new HashMap<String, String>();
            // 定义一个字符串用于保存上传文件的路径：
            String url = null;

            // 4. 遍历集合，获得每个FileItem，判断是表单项还是文件上传项
            for (FileItem fileItem : list) {
                // 判断是表单项还是文件上传项
                if (fileItem.isFormField()){
                    // 普通表单项
                    // 接受表单项参数的值
                    String name = fileItem.getFieldName(); // 获得表单项name属性的值
                    String value = fileItem.getString("UTF-8"); // 获得表单项的值
                    System.out.println(name + ":  " + value);
                    // 接受复选框的数据
                    if("hobby".equals(name)){
                        String hobbyValue = fileItem.getString("UTF-8");
                        // 接受到一个值，将这个值存入hobbyList集合中
                        hobbyList.add(hobbyValue);
                        // 复选框的值，每个值以", "隔开
                        hobbyValue = hobbyList.toString().substring(1, hobbyList.toString().length()-1);
                        System.out.println(name + " ->  " + hobbyList);
                        // 将爱好的值存入到Map集合
                        map.put(name, hobbyValue);
                    }
                    else{
                        // 将普通数据存放到map集合
                        map.put(name, value);
                    }
                }
                else {
                    // 文件上传项
                    // 文件上传功能
                    // 获得文件上传的名称：
                    String fileName = fileItem.getName();
                    // 上传的文件名进行校验
                    if(fileName != null && !"".equals(fileName)){  // 注意这里的校验
                        // 通过工具类获得唯一文件名
                        String uuidFileName = UploadUtils.getUUIDFileName(fileName);
                        // 获得文件上传的数据
                        InputStream is = fileItem.getInputStream();
                        // 获得文件上传的路径
                        String path = this.getServletContext().getRealPath("/upload");
                        // 将输入流对接到输出流：
                        url = path + "\\" + uuidFileName;
                        System.out.println(url);
                        OutputStream os = new FileOutputStream(url);
                        int len = 0;
                        byte[] b = new byte[1024];
                        while ( (len = is.read(b)) != -1 ){
                            os.write(b, 0, len);
                        }
                        System.out.println("文件上传成功。");
                        is.close();
                        os.close();
                    }
                }
            }
            System.out.println(map);
            // 获得ServletContext对象：
            List<User> userList = (List<User>) this.getServletContext().getAttribute("list");
            // 校验用户名
            for(User user : userList){
                if(user.getUsername().equals(map.get("username"))){
                    request.setAttribute("msg", "用户名已存在！");
                    request.getRequestDispatcher("/regist.jsp").forward(request, response);
                    return;
                }
            }
            // 封装数据到User里面去：
            User user = new User();
            user.setUsername(map.get("username"));
            user.setPassword(map.get("password"));
            user.setSex(map.get("sex"));
            user.setNickname(map.get("nickname"));
            user.setHobby(map.get("hobby"));
            user.setPath(url);
            // 将注册用户的信息存入到List集合中去：

            userList.add(user);
            for (User user1 : userList){
                System.out.println(user1);
            }
            this.getServletContext().setAttribute("list", userList);
            // 注册成功，跳转（重定向）到登录页面
            // 将用户名存起来，用于在登录页面使用
            request.getSession().setAttribute("username", user.getUsername());
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
        catch (FileUploadException e) {
            e.printStackTrace();
        }
    }
}
