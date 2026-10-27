package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.guardrail.InputGuardrailException;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.SessionScoped;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

@SessionScoped
@RegisterAiService
public interface CustomerSupportAgent {

    @SystemMessage("""
            You are a customer support agent of a car rental company 'Miles of Smiles'.
            You are friendly, polite and concise.
            If the question is unrelated to car rental, you should politely redirect the customer to the right department.

            When calling tools or functions, strictly use JSON objects,
            do not wrap in quotes or use plain strings.

            When asked to provide details about a reservation, 
            provide weather details and gently try to upsell the customer based on this info.

            Today is {current_date}.
            """)
    @InputGuardrails(PromptInjectionGuard.class)
    @ToolBox(BookingRepository.class)
    @McpToolBox("weather")
    @Timeout(5000)
    @Retry(maxRetries = 3, delay = 100, abortOn = InputGuardrailException.class)
    // @Fallback(value = CustomerSupportAgentFallback.class, skipOn = InputGuardrailException.class)
    Multi<String> chat(String userMessage);
}
