package com.demo.camunda.node.task;

import com.demo.camunda.node.BaseNode;
import com.demo.camunda.node.BpmContext;
import com.demo.camunda.node.BpmnBuilder;
import com.demo.camunda.node.NodeType;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.instance.SendTask;

/**
 * 抄送节点
 */
public class SendTaskNode extends BaseNode {
    @Override
    public AbstractFlowNodeBuilder<?, ?> createFlowNode(AbstractFlowNodeBuilder<?, ?> preFlow, BaseNode node, BpmContext context) {
        String id = BpmnBuilder.id(getNodeType(), node, context);
        SendTask sendTask = (SendTask) BpmnBuilder.createInstance(preFlow, NodeType.SEND_TASK.getName());
        sendTask.setId(id);
        sendTask.setName(node.getName());
        sendTask.setCamundaClass("com.comen.sms.bpm.component.MySendTaskDelegate");
        return sendTask.builder();
    }

    @Override
    public String getNodeType() {
        return NodeType.SEND_TASK.getName();
    }
}
