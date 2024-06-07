package com.demo.camunda.node.event;

import com.demo.camunda.node.BaseNode;
import com.demo.camunda.node.BpmContext;
import com.demo.camunda.node.NodeType;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.instance.EndEvent;

import java.util.Collection;

public class EndEventNode extends BaseNode {
    @Override
    public AbstractFlowNodeBuilder<?, ?> createFlowNode(AbstractFlowNodeBuilder<?, ?> preFlow, BaseNode node, BpmContext context) {
        Collection<EndEvent> modelElementsByType = preFlow.getElement().getModelInstance().getModelElementsByType(EndEvent.class);
        if (modelElementsByType.isEmpty()) {
            preFlow.endEvent();
        } else {//如果已经存在end节点,则讲该节点连接到 end节点，避免出现多个end节点
            EndEvent endEvent = (EndEvent) modelElementsByType.toArray()[0];
            preFlow.connectTo(endEvent.getId());
        }
        return null;
    }

    @Override
    public String getNodeType() {
        return NodeType.END_EVENT.getName();
    }
}