package com.mmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

/**
 * Created by changlei on 2018/12/5.
 */
@ControllerAdvice
@Slf4j
public class MallExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handlerException(Exception e) {
        log.error("{} Exception", MallExceptionHandler.class.getName(), e);
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());
        // 当使用是Jackson2.x的时候使用MappingJackson2JsonView，课程中使用的是1.9
        modelAndView.addObject("status", ResponseCode.ERROR.getCode());
        modelAndView.addObject("status", ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg", "接口异常，详细请查看服务端后端日志");
        modelAndView.addObject("data", e.toString());
        return modelAndView;
    }
}
