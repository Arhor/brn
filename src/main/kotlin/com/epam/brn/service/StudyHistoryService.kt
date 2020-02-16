package com.epam.brn.service

import com.epam.brn.converter.StudyHistoryConverter
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.StudyHistoryRepository
import java.security.InvalidParameterException
import javax.persistence.EntityManager
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StudyHistoryService(
    @Autowired val studyHistoryRepository: StudyHistoryRepository,
    @Autowired val entityManager: EntityManager,
    @Autowired val studyHistoryConverter: StudyHistoryConverter
) {
    private val log = logger()

    fun saveOrUpdateStudyHistory(studyHistoryDto: StudyHistoryDto): StudyHistoryDto {
        val existingStudyHistory = studyHistoryRepository.findByUserAccountIdAndExerciseId(
            studyHistoryDto.userId,
            studyHistoryDto.exerciseId
        )
        val studyHistory = existingStudyHistory.map { studyHistoryEntity ->
            log.debug("Replacing $studyHistoryDto")
            studyHistoryConverter.updateStudyHistory(studyHistoryDto, studyHistoryEntity)
            studyHistoryDto.responseCode = HttpStatus.OK
            studyHistoryEntity
        }
            .orElseGet {
                log.debug("Saving $studyHistoryDto")
                val userReference = entityManager.getReference(UserAccount::class.java, studyHistoryDto.userId)
                val exerciseReference = entityManager.getReference(Exercise::class.java, studyHistoryDto.exerciseId)
                val studyHistoryEntity = StudyHistory(userAccount = userReference, exercise = exerciseReference)
                studyHistoryConverter.updateStudyHistory(studyHistoryDto, studyHistoryEntity)
                studyHistoryDto.responseCode = HttpStatus.CREATED
                studyHistoryEntity
            }
        studyHistoryRepository.save(studyHistory)
        return studyHistoryDto
    }

    @Throws(InvalidParameterException::class)
    fun patchStudyHistory(studyHistoryDto: StudyHistoryDto): StudyHistoryDto {
        log.debug("Patching $studyHistoryDto")
        return studyHistoryRepository.findByUserAccountIdAndExerciseId(
            studyHistoryDto.userId,
            studyHistoryDto.exerciseId
        ).map { studyHistoryEntity ->
            studyHistoryConverter.updateStudyHistoryWhereNotNull(studyHistoryDto, studyHistoryEntity)
            studyHistoryRepository.save(studyHistoryEntity).toDto()
        }
            .orElseThrow { NoDataFoundException("Could not find requested study history with id=${studyHistoryDto.id}") }
    }

    fun getStartTime(): LocalDateTime {
        return studyHistoryRepository.getStartTime()
    }

    fun getEndTime(): LocalDateTime {
        return studyHistoryRepository.getEndTime()
    }
}
