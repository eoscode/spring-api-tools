package com.eoscode.springapitools.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HeaderExposureFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse res = (HttpServletResponse) response;
        res.addHeader("access-control-expose-headers", "Location");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
