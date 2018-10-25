package com.senver.activiti.config;

import org.flowable.engine.*;
import org.flowable.rest.application.ContentTypeResolver;
import org.flowable.rest.application.DefaultContentTypeResolver;
import org.flowable.rest.service.api.RestResponseFactory;
import org.flowable.spring.ProcessEngineFactoryBean;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@ComponentScan(value = {"org.flowable.rest.service"})
public class FlowableConfig {

    @Autowired
    DataSource dataSource;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    private ResourcePatternResolver resourceLoader;
    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration() throws IOException{
        SpringProcessEngineConfiguration springProcessEngineConfiguration = new SpringProcessEngineConfiguration();
        springProcessEngineConfiguration.setDataSource(dataSource); //数据源的配置
        springProcessEngineConfiguration.setTransactionManager(transactionManager); // 事物定义
        springProcessEngineConfiguration.setDatabaseSchemaUpdate("true"); //表的自动更新
        //自动部署已经存在的
        Resource[] resources = resourceLoader.getResources("classpath:/config/processes/*");
        springProcessEngineConfiguration.setDeploymentResources(resources);
        springProcessEngineConfiguration.setDeploymentName("dome-test");
        springProcessEngineConfiguration.setAsyncExecutorActivate(true);
        return springProcessEngineConfiguration;
    }

    //定义引擎工厂
    @Bean
    public ProcessEngineFactoryBean processEngine() throws IOException{
        ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
        processEngineFactoryBean.setProcessEngineConfiguration(processEngineConfiguration());
        return processEngineFactoryBean;
    }

    //集成rest服务
    @Bean
    public RestResponseFactory restResponseFactory() {
        return new RestResponseFactory();
    }

    //运行时Service，可以处理所有正在运行状态的流程实例、任务等
    @Bean
    public RuntimeService runtimeService() throws Exception{
        return processEngine().getObject().getRuntimeService();
    }

    //任务Service，用于管理、查询任务。例如：签收、办理、指派等
    @Bean
    public TaskService taskService() throws Exception{
        return processEngine().getObject().getTaskService();
    }

    //流程仓库Service，用于管理流程仓库。例如：部署、删除、读取流程资源
    @Bean
    public RepositoryService repositoryService() throws Exception {
        return processEngine().getObject().getRepositoryService();
    }


    //历史Service，用于查询所有历史数据。例如：流程实例、任务、活动、变量、附件等
    @Bean
    public HistoryService historyService() throws Exception{
        return processEngine().getObject().getHistoryService();
    }

    //引擎管理Service，和具体业务无关，主要可以查询引擎配置、数据库、作业等
    @Bean
    public ManagementService managementService() throws Exception{
        return processEngine().getObject().getManagementService();
    }

    @Bean
    public ContentTypeResolver contentTypeResolver (){
        return new DefaultContentTypeResolver();
    }

    //表单Service，用于读取和流程、任务相关的表单数据
    @Bean
    public FormService formService() throws Exception{
        return processEngine().getObject().getFormService();
    }

    //身份Service，可以管理和查询用户、组之间的关系
    @Bean
    public IdentityService identityService() throws Exception{
        return processEngine().getObject().getIdentityService();
    }

}
