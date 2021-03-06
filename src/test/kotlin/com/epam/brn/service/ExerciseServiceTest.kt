package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.NoiseDto
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.SubGroup
import com.epam.brn.model.UserAccount
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
internal class ExerciseServiceTest {
    @InjectMocks
    lateinit var exerciseService: ExerciseService

    @Mock
    lateinit var exerciseRepository: ExerciseRepository

    @Mock
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Mock
    lateinit var userAccountService: UserAccountService

    @Mock
    lateinit var urlConversionService: UrlConversionService

    private val series = Series(
        id = 1L,
        name = "Распознавание простых слов",
        type = "type",
        level = 1,
        description = "Распознавание простых слов",
        exerciseGroup = ExerciseGroup(
            id = 2L,
            name = "Речевые упражнения",
            description = "Речевые упражнения"
        )
    )

    @Test
    fun `should get exercises by user`() {
        // GIVEN
        val exerciseMock: Exercise = mock(Exercise::class.java)
        val exerciseDtoMock = ExerciseDto(2, 1, "name", 1, NoiseDto(0, ""))
        val exerciseId = 1L
        `when`(exerciseMock.toDto(true)).thenReturn(exerciseDtoMock)
        `when`(exerciseMock.id).thenReturn(exerciseId)
        `when`(studyHistoryRepository.getDoneExercisesIdList(anyLong())).thenReturn(listOf(exerciseId))
        `when`(exerciseRepository.findAll()).thenReturn(listOf(exerciseMock))
        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserId(22L)
        // THEN
        assertEquals(actualResult, listOf(exerciseDtoMock))
        verify(exerciseRepository).findAll()
        verify(studyHistoryRepository).getDoneExercisesIdList(anyLong())
    }

    @Test
    fun `should get exercises by user and series`() {
        // GIVEN
        val subGroupId = 2L
        val userId = 2L
        val exercise1 = Exercise(id = 1, name = "pets")
        val exercise2 = Exercise(id = 2, name = "pets")
        `when`(studyHistoryRepository.getDoneExercises(subGroupId, userId)).thenReturn(listOf(exercise1))
        `when`(exerciseRepository.findExercisesBySubGroupId(subGroupId)).thenReturn(listOf(exercise1, exercise2))
        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserIdAndSubGroupId(userId, subGroupId)
        // THEN
        assertEquals(actualResult.size, 2)
        verify(exerciseRepository).findExercisesBySubGroupId(subGroupId)
        verify(studyHistoryRepository).getDoneExercises(anyLong(), anyLong())
    }

    @Test
    fun `should get exercise by id`() {
        // GIVEN
        val exerciseMock: Exercise = mock(Exercise::class.java)
        val exerciseDtoMock = ExerciseDto(2, 1, "name", 1, NoiseDto(0, ""))
        `when`(exerciseMock.toDto()).thenReturn(exerciseDtoMock)
        `when`(exerciseRepository.findById(anyLong())).thenReturn(Optional.of(exerciseMock))
        // WHEN
        val actualResult: ExerciseDto = exerciseService.findExerciseById(1L)
        // THEN
        assertEquals(actualResult, exerciseDtoMock)
        verify(exerciseRepository).findById(anyLong())
    }

    @Test
    fun `should get exercise by name and level`() {
        // GIVEN
        val exerciseMock: Exercise = mock(Exercise::class.java)
        `when`(exerciseRepository.findExerciseByNameAndLevel("name", 1)).thenReturn(Optional.of(exerciseMock))
        // WHEN
        val actualResult: Exercise = exerciseService.findExerciseByNameAndLevel("name", 1)
        // THEN
        assertEquals(actualResult, exerciseMock)
        verify(exerciseRepository).findExerciseByNameAndLevel(anyString(), anyInt())
    }

    @Test
    fun `should return 2 availableExercises for one subgroup with last done success`() {
        // GIVEN
        val subGroup = SubGroup(
            series = series,
            level = 1,
            code = "code",
            name = "subGroup name"
        )
        val ex1 = Exercise(id = 1, name = "pets", subGroup = subGroup)
        val ex2 = Exercise(id = 2, name = "pets", subGroup = subGroup)
        val ex3 = Exercise(id = 3, name = "pets ddd", subGroup = subGroup)
        val ex4 = Exercise(id = 4, name = "pets ddd", subGroup = subGroup)
        val listAll = listOf(ex1, ex2, ex3, ex4)
        val listDone = listOf(ex1)
        val studyHistory1 = StudyHistory(
            exercise = ex1,
            userAccount = mock(UserAccount::class.java),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 5,
            replaysCount = 0
        )
        `when`(studyHistoryRepository.findLastByUserAccountId(1))
            .thenReturn(listOf(studyHistory1))
        ReflectionTestUtils.setField(exerciseService, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseService, "minRightAnswersIndex", 0.8)

        // WHEN
        val actualResult = exerciseService.getAvailableExercises(listDone, listAll, 1)
        // THEN
        assertEquals(2, actualResult.size)
        assertTrue(actualResult.containsAll(listOf(ex1, ex3)))
    }

    @Test
    fun `should return availableExercises for one subgroup with last done success`() {
        // GIVEN
        val subGroup = SubGroup(
            series = series,
            level = 1,
            code = "code",
            name = "subGroup name"
        )
        val ex1 = Exercise(id = 1, name = "pets", subGroup = subGroup)
        val ex2 = Exercise(id = 2, name = "pets", subGroup = subGroup)
        val ex3 = Exercise(id = 3, name = "pets ddd", subGroup = subGroup)
        val ex4 = Exercise(id = 4, name = "pets ddd", subGroup = subGroup)
        val listAll = listOf(ex1, ex2, ex3, ex4)
        val listDone = listOf(ex1, ex3)
        val studyHistory1 = StudyHistory(
            exercise = ex1,
            userAccount = mock(UserAccount::class.java),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 0,
            replaysCount = 0
        )
        val studyHistory3 = StudyHistory(
            exercise = ex3,
            userAccount = mock(UserAccount::class.java),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 1,
            replaysCount = 1
        )
        `when`(studyHistoryRepository.findLastByUserAccountId(1))
            .thenReturn(listOf(studyHistory1, studyHistory3))
        ReflectionTestUtils.setField(exerciseService, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseService, "minRightAnswersIndex", 0.8)

        // WHEN
        val actualResult = exerciseService.getAvailableExercises(listDone, listAll, 1)
        // THEN
        assertEquals(4, actualResult.size)
        assertTrue(actualResult.containsAll(listOf(ex1, ex2, ex3, ex4)))
    }

    @Test
    fun `should return availableExercises for one subgroup with last done UNSUCCESS`() {
        // GIVEN
        val subGroup = SubGroup(
            series = series,
            level = 1,
            code = "code",
            name = "subGroup name"
        )
        val ex1 = Exercise(id = 1, name = "pets", subGroup = subGroup)
        val ex2 = Exercise(id = 2, name = "pets", subGroup = subGroup)
        val ex3 = Exercise(id = 3, name = "pets", subGroup = subGroup)
        val ex4 = Exercise(id = 4, name = "pets ddd", subGroup = subGroup)
        val ex5 = Exercise(id = 5, name = "pets ddd", subGroup = subGroup)
        val listAll = listOf(ex1, ex2, ex3, ex4, ex5)
        val listDone = listOf(ex1, ex2)
        val studyHistory1 = StudyHistory(
            exercise = ex1,
            userAccount = mock(UserAccount::class.java),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 0,
            replaysCount = 0
        )
        val studyHistory2 = StudyHistory(
            exercise = ex2,
            userAccount = mock(UserAccount::class.java),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 5,
            replaysCount = 1
        )
        `when`(studyHistoryRepository.findLastByUserAccountId(1))
            .thenReturn(listOf(studyHistory1, studyHistory2))
        ReflectionTestUtils.setField(exerciseService, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseService, "minRightAnswersIndex", 0.8)

        // WHEN
        val actualResult = exerciseService.getAvailableExercises(listDone, listAll, 1)
        // THEN
        assertEquals(3, actualResult.size)
        assertTrue(actualResult.containsAll(listOf(ex1, ex2, ex4)))
    }

    @Test
    fun `should return availableExercises for several subgroups`() {
        // GIVEN
        val subGroup1 = SubGroup(
            series = series,
            level = 1,
            code = "code1",
            name = "subGroup name1"
        )
        val subGroup2 = SubGroup(
            series = series,
            level = 2,
            code = "code2",
            name = "subGroup name2"
        )
        val ex1 = Exercise(id = 1, name = "pets", subGroup = subGroup1)
        val ex2 = Exercise(id = 2, name = "pets", subGroup = subGroup1)
        val ex3 = Exercise(id = 3, name = "pets ddd", subGroup = subGroup1)
        val ex4 = Exercise(id = 4, name = "pets ddd", subGroup = subGroup1)
        val ex11 = Exercise(id = 11, name = "food", subGroup = subGroup2)
        val ex12 = Exercise(id = 12, name = "food", subGroup = subGroup2)
        val ex13 = Exercise(id = 13, name = "food eee", subGroup = subGroup2)
        val ex14 = Exercise(id = 14, name = "food eee", subGroup = subGroup2)
        val listAll = listOf(ex1, ex2, ex3, ex4, ex11, ex12, ex13, ex14)
        val listDone = listOf(ex1, ex2, ex11)
        val studyHistory1 = StudyHistory(
            exercise = ex1,
            userAccount = mock(UserAccount::class.java),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 0,
            replaysCount = 0
        )
        val studyHistory2 = StudyHistory(
            exercise = ex2,
            userAccount = mock(UserAccount::class.java),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 2,
            replaysCount = 2
        )
        val studyHistory11 = StudyHistory(
            exercise = ex11,
            userAccount = mock(UserAccount::class.java),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 6,
            replaysCount = 4
        )
        `when`(studyHistoryRepository.findLastByUserAccountId(1))
            .thenReturn(listOf(studyHistory1, studyHistory2, studyHistory11))
        ReflectionTestUtils.setField(exerciseService, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseService, "minRightAnswersIndex", 0.8)

        // WHEN
        val actualResult = exerciseService.getAvailableExercises(listDone, listAll, 1)
        // THEN
        assertEquals(5, actualResult.size)
        assertTrue(actualResult.containsAll(listOf(ex1, ex2, ex3, ex11, ex13)))
    }
}
