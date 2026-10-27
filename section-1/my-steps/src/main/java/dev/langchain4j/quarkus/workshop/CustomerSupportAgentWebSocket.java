package dev.langchain4j.quarkus.workshop;
import java.time.Duration;

import dev.langchain4j.guardrail.InputGuardrailException;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Multi;

@WebSocket(path = "/customer-support-agent")
public class CustomerSupportAgentWebSocket {

    private final CustomerSupportAgent customerSupportAgent;

    public CustomerSupportAgentWebSocket(CustomerSupportAgent customerSupportAgent) {
        this.customerSupportAgent = customerSupportAgent;
    }

    @OnOpen
    public String onOpen() {
        return "Welcome to Miles of Smiles! How can I help you today?";
    }

    @OnTextMessage
    public Multi<String> onTextMessage(String message) {
        try {
            // return customerSupportAgent.chat(message);
            return customerSupportAgent.chat(message)
                .ifNoItem().after(Duration.ofSeconds(10)).fail()    // timeout: no first token within 10s
                .onFailure().retry().atMost(3)                              // retry: resubscribe on failure
                .onFailure().recoverWithItem(throwable -> {                 // fallback
                    Log.errorf(throwable, "Error calling the LLM: %s", throwable.getMessage());
                    return "Failed to get a response from the AI Model. Are you sure it's up and running, and configured correctly?";
                });
        } catch (InputGuardrailException e) {
            Log.errorf(e, "Error calling the LLM: %s", e.getMessage());
            return Multi.createFrom().item("Sorry, I am unable to process your request at the moment. It's not something I'm allowed to do.");
        } catch (Exception e) {
            Log.errorf(e, "Error calling the LLM: %s", e.getMessage());
            return Multi.createFrom().item("I ran into some problems. Please try again.");
        }
    }
}
