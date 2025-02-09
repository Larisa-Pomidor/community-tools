package com.community.tools.dto;

import com.community.tools.model.TaskNameAndStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class UserForTaskStatusDto {

  private final String gitName;
  private final LocalDate dateLastActivity;
  private final Integer completedTasks;
  private final List<TaskNameAndStatus> taskStatuses;

  /**
   * Constructor for DTO.
   * @param gitName - gitName
   * @param dateLastActivity - dateLastActivity
   * @param completedTasks - completedTasks
   * @param taskStatuses - list of task and status
   */
  public UserForTaskStatusDto(String gitName, LocalDate dateLastActivity, Integer completedTasks,
      List<TaskNameAndStatus> taskStatuses) {
    this.gitName = gitName;
    this.dateLastActivity = dateLastActivity;
    this.completedTasks = completedTasks;
    this.taskStatuses = taskStatuses;
  }

}
