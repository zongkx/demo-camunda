package com.demo.camunda.node;

import lombok.AllArgsConstructor;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.Arrays;
import java.util.Optional;

/**
 * BPMN所有节点类型枚举
 */
@AllArgsConstructor
public enum NodeType {

    END_EVENT(NodeName.END_EVENT, EndEvent.class),
    START_EVENT(NodeName.START_EVENT, StartEvent.class),
    USER_TASK(NodeName.USER_TASK, UserTask.class),
    CREATE_USER_TASK(NodeName.CREATE_TASK, UserTask.class),
    EXCLUSIVE_GATEWAY(NodeName.EXCLUSIVE_GATEWAY, ExclusiveGateway.class),
    PARALLEL_GATEWAY(NodeName.PARALLEL_GATEWAY, ParallelGateway.class),
    INCLUSIVE_GATEWAY(NodeName.INCLUSIVE_GATEWAY, InclusiveGateway.class),
    COMPLEX_GATEWAY(NodeName.COMPLEX_GATEWAY, ComplexGateway.class),
    EVENT_BASED_GATEWAY(NodeName.EVENT_BASED_GATEWAY, EventBasedGateway.class),
    SEND_TASK(NodeName.SEND_TASK, SendTask.class),


    ;

    private final String elementTypeName;
    private final Class<? extends ModelElementInstance> elementTypeClass;

    public static Optional<Class<? extends ModelElementInstance>> getElementTypeClass(final String elementTypeName) {
        NodeType nodeType = Arrays.stream(values())
                .filter(a -> a.elementTypeName != null && a.elementTypeName.equals(elementTypeName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unsupported BPMN element of type " + elementTypeName));
        return Optional.ofNullable(nodeType.elementTypeClass);
    }

    public String getName() {
        return this.elementTypeName;
    }

    public static class NodeName {
        public static final String CREATE_TASK = "createTask";
        public static final String EXCLUSIVE_GATEWAY = "exclusiveGateway";
        public static final String PARALLEL_GATEWAY = "parallelGateway";
        public static final String INCLUSIVE_GATEWAY = "inclusiveGateway";
        public static final String COMPLEX_GATEWAY = "complexGateway";
        public static final String EVENT_BASED_GATEWAY = "eventBasedGateway";
        public static final String USER_TASK = "userTask";
        public static final String SEND_TASK = "sendTask";
        public static final String END_EVENT = "endEvent";
        public static final String START_EVENT = "startEvent";
    }

 
}
