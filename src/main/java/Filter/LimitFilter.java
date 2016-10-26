package Filter;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Administrator on 2016/10/25.
 */

public class LimitFilter implements javax.servlet.Filter {
    //private FilterConfig config;
    public void destroy() {
    }

    public void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, javax.servlet.FilterChain chain) throws javax.servlet.ServletException, IOException {
        HttpServletResponse httpServletResponse = (HttpServletResponse)response;
       /* String charset = config.getInitParameter("charset");
        if(charset==null){
            charset = "UTF-8";
        }
        request.setCharacterEncoding(charset);*/

        //httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");

        //httpServletResponse.setHeader("Access-Control-Allow-Headers", "Content-Type,Accept");

        //httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");

        //httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

        //httpServletResponse.setHeader("Access-Control-Max-Age", "1209600");

        //httpServletResponse.setHeader("Access-Control-Expose-Headers","accesstoken");

        //httpServletResponse.setHeader("Access-Control-Request-Headers","accesstoken");

        //httpServletResponse.setHeader("Expires","-1");

        //httpServletResponse.setHeader("Cache-Control","no-cache");

        // httpServletResponse.setHeader("pragma","no-cache");

//        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
//        httpServletResponse.setHeader("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept");
//        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");


        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");

        httpServletResponse.setHeader("Access-Control-Allow-Headers", "User-Agent,X-Requested-With,Accept,Origin,Cache-Control,Content-type,Date,Server,withCredentials,AccessToken");

        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");

        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

        httpServletResponse.setHeader("Access-Control-Max-Age", "1209600");

        httpServletResponse.setHeader("Access-Control-Expose-Headers","accesstoken");

        httpServletResponse.setHeader("Access-Control-Request-Headers","accesstoken");

        httpServletResponse.setHeader("Expires","-1");

        httpServletResponse.setHeader("Cache-Control","no-cache");

        httpServletResponse.setHeader("pragma","no-cache");



        HttpServletRequest httpServletRequest = (HttpServletRequest)request;


        if("OPTIONS".equals(httpServletRequest.getMethod())){//OPTIONS方法不要拦截，不然跨域设置不成功

            return;
        }

        chain.doFilter(request, response);

    }

    public void init(javax.servlet.FilterConfig config) throws javax.servlet.ServletException {
        //config=config;
    }

}
