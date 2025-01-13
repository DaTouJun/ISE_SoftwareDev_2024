package org.flitter.backend.repository;

import org.flitter.backend.entity.Task;
import org.flitter.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Task findByTitle(String title);

    List<Task> findByBelongedProjectId(Long id);
    


    // 查找某个项目下的所有任务数量
    @Query("SELECT COUNT(t) FROM Task t WHERE t.belongedProject.id = :projectId")
    Long countAllTasksByProjectId(Long projectId);

    // 查找某个项目下已完成的任务数量
    @Query("SELECT COUNT(t) FROM Task t WHERE t.belongedProject.id = :projectId AND t.isCompleted = true")
    Long countCompletedTasksByProjectId(Long projectId);


    List<Task> findByAssignees(User assignee);
}
