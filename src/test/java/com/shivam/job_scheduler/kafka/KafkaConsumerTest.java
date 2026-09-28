package com.shivam.job_scheduler.kafka;

import com.shivam.job_scheduler.execution.service.ExecutionClaimService;
import com.shivam.job_scheduler.execution.worker.ExecutionProcessor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {

    @Mock
    private ExecutionClaimService executionClaimService;

    @Mock
    private ExecutionProcessor executionProcessor;

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    @Test
    void duplicateMessageShouldNotProcessExecutionTwice() {
        UUID executionId = UUID.randomUUID();
        ExecutionMessage message = new ExecutionMessage(executionId);

        // First delivery claims the execution.
        // Second delivery cannot claim it again.
        when(executionClaimService.claimExecution(executionId))
                .thenReturn(true, false);

        kafkaConsumer.consume(message);
        kafkaConsumer.consume(message);

        // The processor must run only once.
        verify(executionProcessor, times(1))
                .process(executionId);

        // Both deliveries should attempt to claim.
        verify(executionClaimService, times(2))
                .claimExecution(executionId);
    }
}