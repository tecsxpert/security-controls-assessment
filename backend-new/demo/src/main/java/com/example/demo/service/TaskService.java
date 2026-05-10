package com.example.demo.service;

import com.example.demo.dto.TaskRequest;
import com.example.demo.dto.TaskResponse;
import com.example.demo.entity.Task;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.TaskRepository;

import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskResponse getTask(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found"
                        )
                );

        return mapToResponse(task);
    }

    public TaskResponse updateTask(
            Long id,
            TaskRequest request
    ) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found"
                        )
                );

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setScore(request.getScore());
        task.setSummary(request.getSummary());
        task.setRecommendation(request.getRecommendation());

        taskRepository.save(task);

        return mapToResponse(task);
    }

    public void deleteTask(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found"
                        )
                );

        taskRepository.delete(task);
    }

    private TaskResponse mapToResponse(Task task) {

        TaskResponse response = new TaskResponse();

        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setScore(task.getScore());
        response.setSummary(task.getSummary());
        response.setRecommendation(task.getRecommendation());

        if(task.getCreatedDate() != null) {
            response.setCreatedDate(
                    task.getCreatedDate().toString()
            );
        }

        return response;
    }
}