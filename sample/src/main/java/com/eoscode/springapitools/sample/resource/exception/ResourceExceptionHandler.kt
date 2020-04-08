package com.eoscode.springapitools.sample.resource.exception

import com.eoscode.springapitools.resource.exception.BaseResourceExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ResourceExceptionHandler : BaseResourceExceptionHandler() {}
