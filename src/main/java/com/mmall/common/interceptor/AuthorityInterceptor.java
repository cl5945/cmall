package com.mmall.common.interceptor;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by changlei on 2018/12/5.
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {
    /**
     * Intercept the execution of a handler. Called after HandlerMapping determined
     * an appropriate handler object, but before HandlerAdapter invokes the handler.
     * <p>DispatcherServlet processes a handler in an execution chain, consisting
     * of any number of interceptors, with the handler itself at the end.
     * With this method, each interceptor can decide to abort the execution chain,
     * typically sending a HTTP error or writing a custom response.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  chosen handler to execute, for type and/or instance evaluation
     * @return {@code true} if the execution chain should proceed with the
     * next interceptor or the handler itself. Else, DispatcherServlet assumes
     * that this interceptor has already dealt with the response itself.
     * @throws Exception in case of errors
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle");
        // 请求中controller中的方法名字
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 解析handlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();

        //解析参数，具体的参数key以及value是什么，我们打印日志
        StringBuffer requsetParamBuffer = new StringBuffer();
        Map paramMap = request.getParameterMap();
        Iterator it = paramMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();

            String mapKey = (String) entry.getKey();
            String mapValue = StringUtils.EMPTY;

            //request 这个参数的map，里面的value返回是一个String[]
            Object obj = entry.getValue();
            if(obj instanceof String[]){
                String[] strs = (String[]) obj;
                mapValue = Arrays.toString(strs);
            }
            requsetParamBuffer.append(mapKey).append(" = ").append(mapValue);
        }

        if("UserManageController".equals(className) && "login".equals(methodName)){
            log.info("权限拦截器拦截到请求，classname:{},methodName:{}",className ,methodName);
            // 如果是拦截到登录请求，不打印参数，因为参数里面有密码，全部打印到日志中，防止日志泄露用户名/密码
            return true;
        }
        log.info(requsetParamBuffer.toString());

        User user = null;
         String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isNotEmpty(loginToken)) {
            String userJson = RedisPoolUtil.get(loginToken);
            user = JsonUtil.string2Obj(userJson, User.class);
        }
        if(user == null || (user.getRole().intValue() != Const.Role.ROLE_ADMIN)){
            // 返回false即不会调用controller 里面的方法 ,但是无法返回错误信息，通过 response 重置的方法将我们的错误信息写到response 中
            response.reset();  // 需要调用reset方法，否则报异常getWriter() has already been called for this response
            response.setCharacterEncoding("UTF-8");  // 设置编码否则会乱码
            response.setContentType("application/json;charset=UTF-8"); //这里要设置返回数据类型

            PrintWriter out = response.getWriter();
            if(user == null){
                out.print(JsonUtil.ojb2String(ServerResponse.createByErrorMessage("拦截器拦截，用户未登录")));
            }else{
                out.print(JsonUtil.ojb2String(ServerResponse.createByErrorMessage("拦截器拦截，用户无权限操作")));
            }
            out.flush();
            out.close();  // 这里需要关闭
            return false;
        }
        return true;
    }

    /**
     * Intercept the execution of a handler. Called after HandlerAdapter actually
     * invoked the handler, but before the DispatcherServlet renders the view.
     * Can expose additional model objects to the view via the given ModelAndView.
     * <p>DispatcherServlet processes a handler in an execution chain, consisting
     * of any number of interceptors, with the handler itself at the end.
     * With this method, each interceptor can post-process an execution,
     * getting applied in inverse order of the execution chain.
     *
     * @param request      current HTTP request
     * @param response     current HTTP response
     * @param handler      handler that started async
     *                     execution, for type and/or instance examination
     * @param modelAndView the {@code ModelAndView} that the handler returned
     *                     (can also be {@code null})
     * @throws Exception in case of errors
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandler");

    }

    /**
     * Callback after completion of request processing, that is, after rendering
     * the view. Will be called on any outcome of handler execution, thus allows
     * for proper resource cleanup.
     * <p>Note: Will only be called if this interceptor's {@code preHandle}
     * method has successfully completed and returned {@code true}!
     * <p>As with the {@code postHandle} method, the method will be invoked on each
     * interceptor in the chain in reverse order, so the first interceptor will be
     * the last to be invoked.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  handler that started async
     *                 execution, for type and/or instance examination
     * @param ex       exception thrown on handler execution, if any
     * @throws Exception in case of errors
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        int a = 1;
    }
}
