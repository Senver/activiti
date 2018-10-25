package com.senver.activiti;

import org.apache.commons.io.FileUtils;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivitiApplicationTests {


    @Autowired
    ProcessEngine processEngine;
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;
    @Autowired
    RepositoryService repositoryService;

    @Test
    public void contextLoads() {
    }


    //启动流程实例
    @Test
    public void startProcessInstance(){

        String processKey = "DocConsulst"; //流程图的ID
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey);

        System.out.println("流程ID: "+ processInstance.getId());
        System.out.println("流程ID: "+ processInstance.getProcessInstanceId());
        System.out.println("流程ID: "+ processInstance.getProcessDefinitionId());
    }

    //查看当前办理人的任务
    @Test
    public void findPersonnelTaskList(){
        String assignee = "张三";
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(assignee).list();
        if (tasks != null && tasks.size()>0){
            for (Task task:tasks){
                System.out.println("任务ID：" +task.getId());
                System.out.println("任务办理人："+task.getAssignee());
                System.out.println("任务名称："+task.getName());
                System.out.println("任务创建时间："+task.getCreateTime());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());

            }
        }else{
            System.out.println("没有查到");
        }
    }

    //完成任务
    @Test
    public void completeTask(){
        String taskID = "17509";
        taskService.complete(taskID);
        System.out.println("完成任务"+ taskID);
    }
    //查询流程定义
    @Test
    public void findProcessDifinitisonList(){
        List<ProcessDefinition> list = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey("DocConsulst")
                .orderByProcessDefinitionVersion().desc()
                .list();
        if (list != null && list.size()>0){
            for (ProcessDefinition pd:list){
                System.out.println("流程定义的ID："+pd.getId());
                System.out.println("流程定义的名称："+pd.getName());
                System.out.println("流程定义的Key："+pd.getKey());
                System.out.println("流程定义的部署ID："+pd.getDeploymentId());
                System.out.println("流程定义的资源名称："+pd.getResourceName());
                System.out.println("流程定义的版本："+pd.getVersion());
            }
        }
    }

    //删除流程定义
    @Test
    public void deleteProcessDifinition(){
        //部署对象ID
        String deploymentId = "601";
        //使用部署ID删除流程定义,true表示级联删除
        repositoryService.deleteDeployment(deploymentId,true);
        System.out.println("删除成功~~~");
    }

    //查看流程定义的资源文件，目的是处理流程图片
    @Test
    public void viewPng() throws IOException, IOException {
        //部署ID
        String deploymentId = "1";
        //获取的资源名称
        List<String> list = repositoryService.getDeploymentResourceNames(deploymentId);
        //获得资源名称后缀.png
        String resourceName = "";
        if (list != null && list.size() > 0) {
            for (String name : list) {
                if (name.indexOf(".png") >= 0) {//返回包含该字符串的第一个字母的索引位置
                    resourceName = name;
                }
            }
        }

        //获取输入流，输入流中存放.PNG的文件
        InputStream in = repositoryService.getResourceAsStream(deploymentId, resourceName);

        //将获取到的文件保存到本地
        FileUtils.copyInputStreamToFile(in, new File("D:/" + resourceName));

        System.out.println("文件保存成功！");
    }

    /**设置流程变量对应数据库表：act_ru_variable*/
    @Test
    public void setProcessVariables(){
        String processInstanceId = "5";//流程实例ID
        String assignee = "张三";//任务办理人

        //查询当前办理人的任务ID
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)//使用流程实例ID
                .taskAssignee(assignee)//任务办理人
                .singleResult();

        //设置流程变量【基本类型】
        taskService.setVariable(task.getId(), "请假人", assignee);
        taskService.setVariableLocal(task.getId(), "请假天数",3);
        taskService.setVariable(task.getId(), "time", new Date());

        //测试github


    }


}
