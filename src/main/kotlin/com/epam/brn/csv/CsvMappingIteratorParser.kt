package com.epam.brn.csv

import com.epam.brn.csv.converter.Converter
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class CsvMappingIteratorParser {

    val log = logger()

    final inline fun <reified Source, reified Target> parseCsvFile(
        file: InputStream,
        converter: Converter<Source, Target>,
        csvParser: CsvParser<Source>
    ): Map<String, Pair<Target?, String?>> {

        ByteArrayInputStream(IOUtils.toByteArray(file)).use {

            val parsedValues = hashMapOf<String, Source>()
            val sourceToTarget = hashMapOf<String, Pair<Target?, String?>>()
            val csvLineNumbersToValues = getCsvLineNumbersToValues(it)
            val mappingIterator = csvParser.parseCsvFile(it)
            while (mappingIterator.hasNextValue()) {
                val lineNumber = mappingIterator.currentLocation.lineNr
                try {
                    val line = mappingIterator.nextValue()
                    csvLineNumbersToValues[lineNumber]?.let {
                        parsedValues[it] = line
                    }

                    log.debug("Successfully parsed line with number $lineNumber")
                } catch (e: Exception) {
                    csvLineNumbersToValues[lineNumber]?.let {
                        sourceToTarget[it] = Pair(null, "Parse Exception - wrong format: ${e.localizedMessage}")
                    }

                    log.error("Failed to parse line with number $lineNumber ", e)
                }
            }
            sourceToTarget.putAll(parsedValues
                .map { parsedValue -> parsedValue.key to Pair(converter.convert(parsedValue.value), null) }
                .toMap())

            return sourceToTarget
        }
    }

    fun getCsvLineNumbersToValues(file: InputStream): Map<Int, String> {

        val listOfLinesWithoutHeader = BufferedReader(InputStreamReader(file))
            .lines()
            .skip(NumberUtils.LONG_ONE)
            .collect(Collectors.toList())

        val result = mutableMapOf<Int, String>()
        listOfLinesWithoutHeader.forEachIndexed { index, s -> result[index + 2] = s }

        file.reset()
        return result
    }
}
