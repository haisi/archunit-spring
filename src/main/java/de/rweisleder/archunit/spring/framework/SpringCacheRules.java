/*
 * #%L
 * ArchUnit Spring Integration
 * %%
 * Copyright (C) 2023 - 2024 Roland Weisleder
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.rweisleder.archunit.spring.framework;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;

import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static de.rweisleder.archunit.spring.MergedAnnotationPredicates.springAnnotatedWith;

/**
 * Collection of {@link ArchRule rules} that can be used to check the usage of Spring's generic cache abstraction.
 *
 * @author Roland Weisleder
 */
public class SpringCacheRules {

    /**
     * A rule that checks that methods annotated with {@code @Cacheable} are not called from within the same class.
     * Such internal calls bypass Spring's proxy mechanism, causing the intended caching behavior to be ignored.
     * <p>
     * Example of a violating method:
     * <pre>{@code
     * public class BookService {
     *
     *     @Cacheable("books")
     *     public Book findBook(String isbn) {
     *         return database.findBook(isbn);
     *     }
     *
     *     public String findBookTitle(String isbn) {
     *         Book book = findBook(isbn); // Violation, as this internal call bypasses the proxy functionality
     *         return book.getTitle();
     *     }
     * }
     * }</pre>
     * <p>
     * This rule should only be used if caching is used in proxy mode, see the {@code @EnableCaching} annotation.
     */
    public static final ArchRule CacheableMethodNotCalledFromSameClass = methods()
            .that(are(springAnnotatedWith("org.springframework.cache.annotation.Cacheable")))
            .should(notBeCalledFromWithinTheSameClass());

    private static ArchCondition<JavaMethod> notBeCalledFromWithinTheSameClass() {
        return new ArchCondition<JavaMethod>("not be called from within the same class") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                for (JavaMethodCall methodCall : method.getCallsOfSelf()) {
                    boolean calledFromWithinSameClass = methodCall.getOriginOwner().equals(methodCall.getTargetOwner());
                    if (calledFromWithinSameClass) {
                        events.add(violated(method, methodCall.getDescription()));
                    }
                }
            }
        };
    }
}