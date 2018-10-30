package com.senver.activiti;

import com.senver.activiti.entity.Person;
import org.apache.commons.io.FileUtils;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    HistoryService historyService;

    @Test
    public void contextLoads() {
    }


    //启动流程实例
    @Test
    public void startProcessInstance() {

        String processKey = "DocConsulst"; //流程图的ID
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey);

        System.out.println("流程ID: " + processInstance.getId());
        System.out.println("流程ID: " + processInstance.getProcessInstanceId());
        System.out.println("流程ID: " + processInstance.getProcessDefinitionId());
    }

    //查看当前办理人的任务
    @Test
    public void findPersonnelTaskList() {
        String assignee = "张三";
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(assignee).list();
        if (tasks != null && tasks.size() > 0) {
            for (Task task : tasks) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务办理人：" + task.getAssignee());
                System.out.println("任务名称：" + task.getName());
                System.out.println("任务创建时间：" + task.getCreateTime());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());

            }
        } else {
            System.out.println("没有查到");
        }
    }

    //完成任务
    @Test
    public void completeTask(){
        //任务ID
        String taskId = "12509";
        //完成任务的同时，设置流程变量，让流程变量判断连线该如何执行
        Map<String, Object> variables = new HashMap<String, Object>();
        //其中message对应sequenceFlow.bpmn中的${message=='不重要'}，不重要对应流程变量的值
        variables.put("message", "不重要");
        processEngine.getTaskService()//
                .complete(taskId,variables);
        System.out.println("完成任务："+taskId);
    }

    //查询流程定义
    @Test
    public void findProcessDifinitisonList() {
        List<ProcessDefinition> list = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey("DocConsulst")
                .orderByProcessDefinitionVersion().desc()
                .list();
        if (list != null && list.size() > 0) {
            for (ProcessDefinition pd : list) {
                System.out.println("流程定义的ID：" + pd.getId());
                System.out.println("流程定义的名称：" + pd.getName());
                System.out.println("流程定义的Key：" + pd.getKey());
                System.out.println("流程定义的部署ID：" + pd.getDeploymentId());
                System.out.println("流程定义的资源名称：" + pd.getResourceName());
                System.out.println("流程定义的版本：" + pd.getVersion());
            }
        }
    }

    //删除流程定义
    @Test
    public void deleteProcessDifinition() {
        //部署对象ID
        String deploymentId = "601";
        //使用部署ID删除流程定义,true表示级联删除
        repositoryService.deleteDeployment(deploymentId, true);
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

    /**
     * 通过基础变量设置流程变量
     * 设置流程变量对应数据库表：act_ru_variable（储存数据类型为基础数据类型）
     */
    //基本类型设置
    @Test
    public void setProcessVariables() {
        String processInstanceId = "5";//流程实例ID
        String assignee = "张三";//任务办理人

        //查询当前办理人的任务ID
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)//使用流程实例ID
                .taskAssignee(assignee)//任务办理人
                .singleResult();
        //设置流程变量【基本类型】
        taskService.setVariable(task.getId(), "请假人", assignee);
        taskService.setVariableLocal(task.getId(), "请假天数", 3);
        taskService.setVariable(task.getId(), "请假日期", new Date());
    }

    /**
     * 通过javaBean类设置流程变量
     * 数据库对应表：act_ru_variable(储存的数据类型为serializable）
     * 真实的数据存储表为：act_ge_bytearray
     */
    @Test
    public void setProcessVariablesJava() {
        String processInstanceId = "5";//流程实例ID
        String assignee = "张三";//任务办理人
        TaskService taskService = processEngine.getTaskService();//获取任务的Service，设置和获取流程变量

        //查询当前办理人的任务ID
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)//使用流程实例ID
                .taskAssignee(assignee)//任务办理人
                .singleResult();

        //设置流程变量【javabean类型】
        Person p = new Person();
        p.setId(1);
        p.setName("王五");
        taskService.setVariable(task.getId(), "人员信息", p);
        System.out.println("流程变量设置成功~~");
    }

    /**
     * 通过基本类型获取流程变量
     */
    @Test
    public void getProcessVariables() {
        String processInstanceId = "5";//流程实例ID
        String assignee = "张三";//任务办理人
        //获取当前办理人的任务ID
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee(assignee)
                .singleResult();

        //获取流程变量【基本类型】
        String person = (String) taskService.getVariable(task.getId(), "请假人");
        Integer day = (Integer) taskService.getVariableLocal(task.getId(), "请假天数");
        Date date = (Date) taskService.getVariable(task.getId(), "请假日期");
        System.out.println(person + "  " + day + "   " + date);

    }

    /**
     * 通过JavaBean获取
     */
    @Test
    public void getProcessVariablesJava() {
        String processInstanceId = "5";//流程实例ID
        String assignee = "张三";//任务办理人
        //获取当前办理人的任务ID
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee(assignee)
                .singleResult();

        //获取流程变量【javaBean类型】
        Person p = (Person) taskService.getVariable(task.getId(), "人员信息");
        System.out.println(p.getId() + "  " + p.getName());
        System.out.println("获取成功~~");
    }

    /**
     * 查询历史流程变量
     * 数据库对应表：act_ru_execution
     */
    @Test
    public void getHistoryProcessVariables() {
        List<HistoricVariableInstance> list = historyService
                .createHistoricVariableInstanceQuery()//创建一个历史的流程变量查询
                .variableName("请假天数")
                .list();

        if (list != null && list.size() > 0) {
            for (HistoricVariableInstance hiv : list) {
                System.out.println(hiv.getTaskId() + "  " + hiv.getVariableName() + "		" + hiv.getValue() + "		" + hiv.getVariableTypeName());
            }
        }
    }

    /**
     * 查询历史流程实例
     * 数据库中的表：act_hi_procinst
     */
    @Test
    public void findHisProcessInstance() {
        List<HistoricProcessInstance> list = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionId("DocConsulst:1:4")//流程定义ID
                .list();

        if (list != null && list.size() > 0) {
            for (HistoricProcessInstance hi : list) {
                System.out.println(hi.getId() + "	  " + hi.getStartTime() + "   " + hi.getEndTime());
            }
        }
    }

    /**
     * 查询历史活动
     * 数据库表：act_hi_actinst
     */
    @Test
    public void findHisActivitiList(){
        String processInstanceId = "5";
        List<HistoricActivityInstance> list = processEngine.getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        if(list != null && list.size()>0){
            for(HistoricActivityInstance hai : list){
                System.out.println(hai.getId()+"  "+hai.getActivityName());
            }
        }
    }


    /**
     * 查询历史任务
     * 数据表：act_hi_taskinst
     */
    @Test
    public void findHisTaskList() {
        String processInstanceId = "5";
        List<HistoricTaskInstance> list = processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        if (list != null && list.size() > 0) {
            for (HistoricTaskInstance hti : list) {
                System.out.println(hti.getId() + "    " + hti.getName() + "   " + hti.getClaimTime());
            }
        }
    }
    /**
     * 查询历史流程变量
     * 数据表：act_hi_varinst
     */
    @Test
    public void findHisVariablesList(){
        String processInstanceId = "5";
        List<HistoricVariableInstance> list = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        if(list != null && list.size()>0){
            for(HistoricVariableInstance hvi:list){
                System.out.println(hvi.getId()+"    "+hvi.getVariableName()+"	"+hvi.getValue());
            }
        }
    }





}
