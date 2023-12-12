package de.rweisleder.archunit.spring;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static de.rweisleder.archunit.spring.MergedAnnotationPredicates.springAnnotatedWith;

/**
 * Collection of {@link DescribedPredicate predicates} that can be used to match the different Spring stereotypes.
 *
 * @author Roland Weisleder
 */
public class SpringComponentPredicates {

    /**
     * Returns a predicate that matches classes that are Spring components.
     * These are classes that are directly annotated with {@code @Component},
     * meta-annotated with {@code @Component} (like {@code @RestController}, {@code @Controller}, {@code @Service}, {@code @Repository}, {@code @Configuration}),
     * or extend from the Spring Data {@code Repository} interface.
     */
    public static DescribedPredicate<JavaClass> springComponent() {
        DescribedPredicate<JavaClass> hasComponentAnnotation = springAnnotatedWith("org.springframework.stereotype.Component").forSubtype();
        DescribedPredicate<JavaClass> isSpringDataRepository = springDataRepository();
        return hasComponentAnnotation.or(isSpringDataRepository).as("Spring component");
    }

    /**
     * Returns a predicate that matches classes that are Spring controller classes.
     * These are classes that are directly or meta-annotated with {@code @Controller} or {@code @RestController}.
     */
    public static DescribedPredicate<JavaClass> springController() {
        return springAnnotatedWith("org.springframework.stereotype.Controller")
                .as("Spring controller").forSubtype();
    }

    /**
     * Returns a predicate that matches classes that are Spring service classes.
     * These are classes that are directly or meta-annotated with {@code @Service}.
     */
    public static DescribedPredicate<JavaClass> springService() {
        return springAnnotatedWith("org.springframework.stereotype.Service")
                .as("Spring service").forSubtype();
    }

    /**
     * Returns a predicate that matches classes that are Spring repository classes.
     * These are classes that are directly or meta-annotated with {@code @Repository},
     * or extend from the Spring Data {@code Repository} interface.
     */
    public static DescribedPredicate<JavaClass> springRepository() {
        DescribedPredicate<JavaClass> hasRepositoryAnnotation = springAnnotatedWith("org.springframework.stereotype.Repository").forSubtype();
        DescribedPredicate<JavaClass> isSpringDataRepository = springDataRepository();

        return hasRepositoryAnnotation.or(isSpringDataRepository)
                .as("Spring repository");
    }

    /**
     * Returns a predicate that matches classes that are Spring repository classes
     * by extending from the Spring Data {@code Repository} interface.
     */
    private static DescribedPredicate<JavaClass> springDataRepository() {
        return assignableTo("org.springframework.data.repository.Repository")
                .and(not(springAnnotatedWith("org.springframework.data.repository.NoRepositoryBean").forSubtype()));
    }

    /**
     * Returns a predicate that matches classes that are Spring configuration classes.
     * These are classes that are directly or meta-annotated with {@code @Configuration}.
     */
    public static DescribedPredicate<JavaClass> springConfiguration() {
        return springAnnotatedWith("org.springframework.context.annotation.Configuration")
                .as("Spring configuration").forSubtype();
    }
}
