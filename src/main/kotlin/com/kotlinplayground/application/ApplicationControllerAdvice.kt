package com.kotlinplayground.application

import com.kotlinplayground.application.exceptions.SchoolNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApplicationControllerAdvice {

    @ExceptionHandler
    fun handleSchoolNotFound(schoolNotFoundException: SchoolNotFoundException): ApplicationError {
        return ApplicationError(
            HttpStatus.NOT_FOUND,
            "School ${schoolNotFoundException.message} could not found"
        )
    }
}


class ApplicationError(httpStatus: HttpStatus, message: String)