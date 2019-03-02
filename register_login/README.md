这是一个在Intellij 2018下编写的简单动态web工程，采用MVC的设计模式实现用户的注册和登录功能。
运行环境：JAVA JDK1.8 + TomCat 7.0
关键点：
  1. mvc的设计方式，JSP用于视图层，即界面的显示；Servlet用于用户的请求、处理、响应；JavaBean用于数据层，当然本例中并不是一个真正的JavaBean，确少序列化等操作。
  2. 采用Servlet初始化全局的一些变量。
  3. 采用commons-fileupload-1.2.1.jar处理带有文件上传功能的表单。
  4. Cookie的使用。
  5. session保存登录信息以及未登录直接访问等异常的处理。
