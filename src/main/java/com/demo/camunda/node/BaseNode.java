package com.demo.camunda.node;


import com.demo.camunda.node.event.EndEventNode;
import com.demo.camunda.node.gateway.GatewayNode;
import com.demo.camunda.node.task.SendTaskNode;
import com.demo.camunda.node.task.UserTaskNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "nodeType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SendTaskNode.class, name = NodeType.NodeName.SEND_TASK),
        @JsonSubTypes.Type(value = UserTaskNode.class, name = NodeType.NodeName.USER_TASK),
        @JsonSubTypes.Type(value = GatewayNode.class, name = NodeType.NodeName.INCLUSIVE_GATEWAY),
        @JsonSubTypes.Type(value = EndEventNode.class, name = NodeType.NodeName.END_EVENT),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseNode {
    /**
     * 节点ID，在一个流程中不能重复
     */
    private String id;
    /**
     * 节点名称
     */
    private String name;
    /**
     * 节点类型，userTask-审批节点，sendTask-抄送节点，endEvent-结束节点，inclusiveGateway-包容网关
     */
    private String nodeType;
    /**
     * next
     */
    private BaseNode next;

    private String mergeGatewayId;

    public abstract AbstractFlowNodeBuilder<?, ?> createFlowNode(AbstractFlowNodeBuilder<?, ?> preFlow, BaseNode node, BpmContext context);


}
