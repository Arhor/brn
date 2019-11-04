package com.epam.brn.controller

import com.epam.brn.constant.BrnParams
import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.job.csv.task.UploadFromCsvJob
import com.epam.brn.service.TaskService
import io.swagger.annotations.ApiOperation
import org.apache.logging.log4j.kotlin.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(BrnPath.TASKS)
class TaskController(private val taskService: TaskService, private val uploadTaskFromCsvJob: UploadFromCsvJob) {

    private val log = logger()

    @GetMapping
    @ApiOperation("Get all tasks")
    fun getTasks(
        @RequestParam(value = "exerciseId", required = false) exerciseId: Long?
    ): ResponseEntity<BaseResponseDto> {
        exerciseId?.let {
            log.debug("Getting tasks for exercisedId $exerciseId")
            return ResponseEntity
                .ok()
                .body(BaseResponseDto(data = taskService.getAllTasksByExerciseId(it)))
        }
        log.debug("Getting all tasks")
        return ResponseEntity
            .ok()
            .body(BaseResponseDto(data = taskService.getAllTasks()))
    }

    @GetMapping(value = ["/{${BrnParams.TASK_ID}}"])
    @ApiOperation("Get task by id")
    fun getTaskById(@PathVariable(BrnParams.TASK_ID) taskId: Long): ResponseEntity<BaseResponseDto> {
        return ResponseEntity.ok()
            .body(BaseResponseDto(data = taskService.getTaskById(taskId)))
    }

    @PostMapping
    fun addTasks(@RequestParam(value = "taskFile") taskFile: MultipartFile): ResponseEntity<String> {
        uploadTaskFromCsvJob.uploadTasks(taskFile)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}