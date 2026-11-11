package com.vehiclerental.exception;

import com.vehiclerental.config.GlobalModelAttributes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** One place that turns exceptions into friendly error pages. */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final System.Logger LOG = System.getLogger(GlobalExceptionHandler.class.getName());

    private final GlobalModelAttributes common;

    public GlobalExceptionHandler(GlobalModelAttributes common) {
        this.common = common;
    }

    @ExceptionHandler({ResourceNotFoundException.class, NoHandlerFoundException.class, NoResourceFoundException.class})
    public ModelAndView notFound(Exception ex, HttpServletRequest request) {
        String message = ex instanceof ResourceNotFoundException ? ex.getMessage()
                : "The page you asked for does not exist or has moved.";
        return page(HttpStatus.NOT_FOUND, "We could not find that", message, request);
    }

    @ExceptionHandler({BusinessRuleException.class, MethodArgumentTypeMismatchException.class,
            HttpRequestMethodNotSupportedException.class})
    public ModelAndView badRequest(Exception ex, HttpServletRequest request) {
        String message = ex instanceof BusinessRuleException ? ex.getMessage()
                : "That request is not valid. Go back and try again.";
        return page(HttpStatus.BAD_REQUEST, "That did not work", message, request);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView unexpected(Exception ex, HttpServletRequest request) {
        LOG.log(System.Logger.Level.ERROR, "Unexpected error", ex);
        return page(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong",
                "An unexpected error occurred. Please try again.", request);
    }

    private ModelAndView page(HttpStatus status, String title, String message, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addAllObjects(common.values(request));
        mav.addObject("status", status.value());
        mav.addObject("title", title);
        mav.addObject("message", message);
        return mav;
    }
}
