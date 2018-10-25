### 在sping中搭建activi工作流


#### 用到的工具
 - Maven
    - 配置文件：pom.xml文件
 - 基础框架 spring boot
    - 配置文件：application.yml
 - spring data jpa
    - 数据源配置：DataSourceConfig.java
 - flowable(activiti)
    - 自动部署配置文件：FlowableConfig.java
    
#### 说明

- 所有的spring 配置都是基于java注解配置
- spring boot 中自己集成的工具没有列出，类似于测试工具，日志工具等。
- 为了测试方便，我将所有的方法都放在测试类中

