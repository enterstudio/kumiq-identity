package com.kumiq.identity.scim.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.kumiq.identity.scim.endpoint.support.BulkOpResponse;
import com.kumiq.identity.scim.exception.ApiException;
import com.kumiq.identity.scim.utils.JsonDateToUnixTimestampSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
@ControllerAdvice
public class ExceptionResolver {

    @Autowired
    private MessageSource messageSource;

    private static final Logger log = LoggerFactory.getLogger(ExceptionResolver.class);

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleAllExceptions(Exception ex, HttpServletResponse response) {
        if (ex instanceof ApiException)
            return handleApiException((ApiException) ex, response);
        return handleGenericException(ex, response);
    }

    public ErrorResponse handleApiException(ApiException ex, HttpServletResponse response) {
        log.error(ex.defaultMessage());
        response.setStatus(ex.httpStatus().value());
        ErrorResponse errorResponse = ErrorResponse.fromApiException(ex);
        errorResponse.setMessage(messageSource.getMessage(ex.messageCode(), ex.messageArgs(), LocaleContextHolder.getLocale()));
        return errorResponse;
    }

    public ErrorResponse handleGenericException(Exception ex, HttpServletResponse response) {
        log.error(ex.getLocalizedMessage());
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ErrorResponse.fromGenericException(ex);
    }

    public static class ErrorResponse {

        @JsonProperty("error")
        private String errorName;

        @JsonProperty("time")
        @JsonSerialize(using = JsonDateToUnixTimestampSerializer.class)
        private Date errorTime;

        @JsonProperty("message")
        public String message;

        @JsonProperty("statusCode")
        @JsonSerialize(using = BulkOpResponse.HttpStatusJsonSerializer.class)
        public HttpStatus statusCode;

        @JsonProperty("details")
        private Map<String, Object> details;

        public static ErrorResponse fromApiException(ApiException ex) {
            ErrorResponse response = new ErrorResponse();
            response.errorName = ex.getClass().getSimpleName();
            response.errorTime = new Date();
            response.statusCode = ex.httpStatus();
            response.details = CollectionUtils.isEmpty(ex.userInfo()) ? null : ex.userInfo();
            return response;
        }

        public static ErrorResponse fromGenericException(Exception ex) {
            ErrorResponse response = new ErrorResponse();
            response.errorName = "GenericException";
            response.errorTime = new Date();
            response.message = ex.getLocalizedMessage();
            response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            return response;
        }

        public String getErrorName() {
            return errorName;
        }

        public void setErrorName(String errorName) {
            this.errorName = errorName;
        }

        public Date getErrorTime() {
            return errorTime;
        }

        public void setErrorTime(Date errorTime) {
            this.errorTime = errorTime;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public HttpStatus getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(HttpStatus statusCode) {
            this.statusCode = statusCode;
        }
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}
