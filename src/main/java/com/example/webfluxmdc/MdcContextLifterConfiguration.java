package com.example.webfluxmdc;

import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class MdcContextLifterConfiguration {
    private String MDC_CONTEXT_REACTOR_KEY = MdcContextLifterConfiguration.class.getName();

    @PostConstruct
    private void contextOperatorHook() {
        Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY,
                Operators.lift((scannable, coreSubscriber) -> new MdcContextLifter<>(coreSubscriber))
        );
    }

    @PreDestroy
    private void cleanupHook() {
        Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY);
    }
}

