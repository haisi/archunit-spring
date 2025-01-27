/*
 * #%L
 * ArchUnit Spring Integration
 * %%
 * Copyright (C) 2024 Hasan Kara
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

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static de.rweisleder.archunit.spring.SpringAnnotationPredicates.springAnnotatedWith;
import static de.rweisleder.archunit.spring.framework.SpringProxyRules.beProxyable;
import static de.rweisleder.archunit.spring.framework.SpringProxyRules.notBeCalledFromWithinTheSameClass;
import static de.rweisleder.archunit.spring.internal.InternalUtils.availableMethods;

/**
 * Collection of {@link ArchRule rules} that can be used to check the usage of Spring's declarative retry support.
 *
 * @author Hasan Kara
 */
public final class SpringRetryRules {

    private SpringRetryRules() {
    }

    /**
     * A rule that checks that Spring can create proxies for methods that are annotated with {@code @Retryable}.
     * It is most convenient for Spring if such methods are public and not final,
     * and the bean classes containing these methods are also not final.
     * <p>
     * If Spring can not create a proxy for a method, this can lead to an exception when starting the context
     * or to unexpected behavior when calling the method.
     *
     * @see SpringProxyRules#beProxyable()
     */
    public static final ArchRule RetryableMethodsAreProxyable = all(availableMethods())
            .that(are(springAnnotatedWith("org.springframework.retry.annotation.Retryable")))
            .should(beProxyable());

    /**
     * A rule that checks that methods annotated with {@code @Retryable} are not called from within the same class.
     * Such internal calls bypass Spring's proxy mechanism, causing the intended retry behavior to be ignored.
     * <p>
     * Example of a violating method:
     * <pre>{@code
     * public class BookService {
     *
     *     @Retryable(retryFor = SQLException.class)
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
     * This rule should only be used if retrying is used in proxy mode, see the {@code @EnableRetry} annotation.
     *
     * @see SpringProxyRules#notBeCalledFromWithinTheSameClass()
     * @see <a href="https://github.com/spring-projects/spring-retry#javaConfigForRetryProxies">Java Configuration for Retry Proxies</a>
     */
    public static final ArchRule RetryableMethodsNotCalledFromSameClass = all(availableMethods())
            .that(are(springAnnotatedWith("org.springframework.retry.annotation.Retryable")))
            .should(notBeCalledFromWithinTheSameClass());
}
