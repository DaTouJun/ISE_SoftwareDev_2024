package org.flitter.backend.service;

import org.flitter.backend.config.SecurityConfig;
import org.flitter.backend.dto.TaskForGanttDTO;
import org.flitter.backend.entity.Project;
import org.flitter.backend.entity.Task;
import org.flitter.backend.entity.User;
import org.flitter.backend.repository.PreMessageRepository;
import org.flitter.backend.repository.ProjectRepository;
import org.flitter.backend.repository.TaskRepository;
import org.flitter.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.flitter.backend.entity.PreSendingMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.flitter.backend.dto.TaskAssigneeDTO;

import java.util.HashSet;

import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskAllocationService {
    private final SecurityConfig securityConfig;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PreMessageRepository preMessageRepository;
    private final ProcessService processService;

    @Autowired
    public TaskAllocationService(SecurityConfig securityConfig,
                                 TaskRepository taskRepository,
                                 ProjectRepository projectRepository,
                                 UserRepository userRepository,
                                 PreMessageRepository preMessageRepository,
                                 ProcessService processService) {
        this.securityConfig = securityConfig;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.preMessageRepository = preMessageRepository;
        this.processService = processService;
    }

    @Transactional
    public Task allocateTask(TaskAssigneeDTO taskAss) throws Exception {
        //判空都在controller中实现了
        User user = securityConfig.getCurrentUser();
        Task task = new Task();
        Task existingTask = taskRepository.findByTitle(taskAss.getTitle());
        if (existingTask != null) {
            throw new IllegalArgumentException("任务名已经被使用过");
        }

        //判断权限

        //校验任务的分配人信息是否有效
        Set<User> validAssignees = new HashSet<>();
        for (User assignee : taskAss.getAssignees()) {
            if (assignee == null) {
                throw new Exception("任务的分配人信息不能为空");
            }

            // 校验assignee是否有有效的id
            if (assignee.getId() == null) {
                throw new Exception("A false user id");
            } else {
                // 校验assignee是否已存在于数据库
                User existingUser = userRepository.findById(assignee.getId()).orElse(null);
                if (existingUser == null) {
                    throw new Exception("任务的分配人 " + assignee.getId() + " 不存在");
                } else {
                    validAssignees.add(existingUser);
                }
            }
        }

        Project existingProject = projectRepository.findById(taskAss.getBelongedProject().getId()).orElse(null);
        if (existingProject == null) {
            throw new Exception("没有找到对应的项目");
        }
        task.setBelongedProject(existingProject);
        //如果前端没有输入发布者信息，那就把当前操作人当成发布
        if (task.getPublisher() == null || task.getPublisher().getId() == null) {
            User nowUser = securityConfig.getCurrentUser();
            task.setPublisher(nowUser);
        } else {
            User existingUser = userRepository.findById(task.getPublisher().getId()).orElse(null);
            if (existingUser != null) {
                task.setPublisher(existingUser);
            } else {
                throw new Exception("not a right user");
            }

        }
        if (taskAss.getStartDate().isAfter(taskAss.getEndDate())) {
            throw new Exception("time error");
        }
        task.setTitle(taskAss.getTitle());
        task.setDescription(taskAss.getDescription());
        task.setStartDate(taskAss.getStartDate());
        task.setEndDate(taskAss.getEndDate());
        task.setAssignees(validAssignees);
        task.setIsCompleted(false);      //任务还未完成
        task.setPercentCompleted(0.0);   //任务完成程度百分之0;
        // 创建任务
        taskRepository.save(task);
        processService.computeProgress(task.getBelongedProject().getId());
        InitPreMessage(task);
        return task;
    }
    @Transactional
    public Task getTaskByID(Long id) { //通过任务id来具体获取一个具体任务
        User user = securityConfig.getCurrentUser();

        Task existingTask = taskRepository.findById(id).orElse(null);
        if (existingTask == null) {
            throw new IllegalArgumentException("没有找到对应的任务");
        }
        return existingTask;
    }

    //通过项目编号来获取属于该项目所有任务,所以传入的id为项目编号
    public List<Task> getAllTasks(Long id) {
        User user = securityConfig.getCurrentUser();
        //权限判断

        //查看输入的项目id是否有效
        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) {
            throw new IllegalArgumentException("没有找到对应的项目,您输入的项目id不正确");
        }

        return taskRepository.findByBelongedProjectId(id);        // 这里有问题
    }

    //通过员工编号，来查看他所参与的任务
    public List<Task> getAllTasksByWorkerid(Long Workerid) {
        User user = securityConfig.getCurrentUser();

        User existingWorker = userRepository.findById(Workerid).orElse(null);
        if (existingWorker == null) {
            throw new IllegalArgumentException("没有该id对应的员工");
        }
        //User tuser = new User();
        //tuser.setId(existingWorker.g
        return taskRepository.findByAssignees(existingWorker);
    }

    @Transactional
    public boolean ModifyTask(TaskAssigneeDTO taskAss) {
        User currentUser = securityConfig.getCurrentUser();
        //获取任务
        Task existingTask = (Task) taskRepository.findById(taskAss.getId())
                .orElseThrow(() -> new NoSuchElementException("Task not found"));

        //权限验证

        //修改任务内容
        existingTask.setTitle(taskAss.getTitle() != null ? taskAss.getTitle() : existingTask.getTitle());
        //existingTask.setAssignees(taskAss.getAssignees() != null ? taskAss.getAssignees() : existingTask.getAssignees());//这样是有问题的，这样会将原来的数据库替换掉，
        existingTask.setEndDate(taskAss.getEndDate() != null ? taskAss.getEndDate() : existingTask.getEndDate());
        existingTask.setStartDate(taskAss.getStartDate() != null ? taskAss.getStartDate() : existingTask.getStartDate());
        existingTask.setDescription(taskAss.getDescription() != null ? taskAss.getDescription() : existingTask.getDescription());
        existingTask.setIsCompleted(taskAss.getIsCompleted() != null ? taskAss.getIsCompleted() : existingTask.getIsCompleted());
        existingTask.setPercentCompleted(taskAss.getPercentCompleted() != null ? taskAss.getPercentCompleted() : existingTask.getPercentCompleted());

        //更新assignees
        Set<User> currentAssignees = existingTask.getAssignees();
        Set<User> newAssignees = taskAss.getAssignees();

        if(newAssignees!=null){
            //找出要添加的的用户
            Set<User> toAdd = new HashSet<>(newAssignees);
            toAdd.removeAll(currentAssignees);

            currentAssignees.addAll(toAdd);

            existingTask.setAssignees(currentAssignees);
        }


        //保存修改
        taskRepository.save(existingTask);
        processService.computeProgress(existingTask.getBelongedProject().getId());
        return true;    // TODO : ?
    }

    public void InitPreMessage(Task task) {
        PreSendingMessage preSendingMessage = new PreSendingMessage();
        preSendingMessage.setTaskId(task.getId());
        Set<User> assignees = task.getAssignees();
        String taskUser = assignees.stream()
                .map(user -> String.valueOf(user.getId())) // 提取id并转换为字符串
                .collect(Collectors.joining(","));
        preSendingMessage.setUserIds(taskUser);
        preSendingMessage.setTime(task.getStartDate());
        preSendingMessage.setCreatedAt(task.getEndDate());
        preMessageRepository.save(preSendingMessage);
    }

    public List<TaskForGanttDTO> getGanttElement(Long id) {
        User user = securityConfig.getCurrentUser();
        //权限判断

        //查看输入的项目id是否有效
        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) {
            throw new IllegalArgumentException("没有找到对应的项目,您输入的项目id不正确");
        }

        List<Task> tasks = taskRepository.findByBelongedProjectId(id);

        List<TaskForGanttDTO> taskGantt = new ArrayList<>();
        for (Task task : tasks) {
            TaskForGanttDTO gantt = new TaskForGanttDTO(task.getId(), task.getTitle(), task.getStartDate(), task.getEndDate(), task.getIsCompleted());
            taskGantt.add(gantt);
        }

        return taskGantt;

    }
}
