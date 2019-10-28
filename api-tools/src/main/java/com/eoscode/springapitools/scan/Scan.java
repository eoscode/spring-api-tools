package com.eoscode.springapitools.scan;

import com.eoscode.springapitools.data.domain.Find;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Set;


@Service
public class Scan {

    @PostConstruct
    public static void execute() {
        /*Stack<Package> stack = new Stack<>();
        stack.push(Package.getPackage("com.delfos"));
        while(!stack.isEmpty()) {
            Package temp = stack.pop();
            annotatedClasses.addAll(getAnnotatedClasses(temp));
            for(Package p : temp.getPackages()) {
                stack.push(p);
            }
        }*/

        Reflections reflections = new Reflections(
                new ConfigurationBuilder().setUrls(
                        ClasspathHelper.forPackage( "com.delfos" ) ).setScanners(
                        new SubTypesScanner(),
                        new TypeAnnotationsScanner() ) );
        //Set<Method> methods = reflections.getMethodsAnnotatedWith(NoDeleteEntity.class);
        Set<Class<?>> methods = reflections.getTypesAnnotatedWith(Find.class);
        System.out.println("ok");

    }

}
