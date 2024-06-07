package com.demo.camunda.node.gateway;

import cn.hutool.core.util.StrUtil;
import com.demo.camunda.node.BaseNode;
import com.demo.camunda.node.BpmContext;
import com.demo.camunda.node.BpmnBuilder;
import com.demo.camunda.node.NodeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.demo.camunda.node.NodeType.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class GatewayNode extends BaseNode {
    public static final String MERGE_GATEWAY_ID_SUFFIX = "_mergeGatewayIdSuffix";
    public static final String GATEWAY_DEFAULT_CONDITION_EXPRESSION = "${bpmnExpression.gatewayDefaultConditionExpression()}";
    private final boolean defaultBranches = false;
    private String expression;
    private String expressionJson;
    private List<GatewayNode> branches;


    @Override
    public AbstractFlowNodeBuilder<?, ?> createFlowNode(AbstractFlowNodeBuilder<?, ?> preFlow, BaseNode node, BpmContext context) {
        GatewayNode gatewayNode = (GatewayNode) node;
        String id = BpmnBuilder.id(getNodeType(), node, context);
        Gateway gateway = getGateway(preFlow);
        gateway.setId(id);
        gateway.setName(node.getName());
        if (gatewayNode.getBranches() == null || gatewayNode.getBranches().isEmpty()) {
            return gateway.builder();
        }
        String mergeGatewayId = BpmnBuilder.id(getNodeType(), node, context) + MERGE_GATEWAY_ID_SUFFIX;
        List<AbstractFlowNodeBuilder<?, ?>> branches = new ArrayList<>();
        //循环所有的分支节点
        for (GatewayNode branchNode : gatewayNode.getBranches()) {
            BaseNode next = branchNode.getNext();
            if (next == null && branchNode.isDefaultBranches()) {
                branches.add(gateway.builder());
                //设置网关条件
                continue;
            }
            if (next != null) {
                next.setMergeGatewayId(mergeGatewayId);
                branches.add(BpmnBuilder.create(gateway.builder(), next, context));
            }
        }
        // 创建一个新的网关作为合并网关
        Gateway mergeGateway = null;
        // 将所有需要合并的分支连接到这个合并网关
        for (AbstractFlowNodeBuilder<?, ?> nodeBuilder : branches) {
            if (mergeGateway != null) {
                nodeBuilder.connectTo(mergeGateway.getId());
                continue;
            }
            mergeGateway = getGateway(branches.get(0));
            mergeGateway.setId(mergeGatewayId);
            mergeGateway.setName("合并" + node.getName());
        }
        //设置网关条件
        setConditionExpression(gateway, preFlow, gatewayNode);
        return mergeGateway.builder();

    }

    private Gateway getGateway(AbstractFlowNodeBuilder<?, ?> s) {
        return switch (this.getNodeType()) {
            case NodeName.EXCLUSIVE_GATEWAY ->
                    (ExclusiveGateway) BpmnBuilder.createInstance(s, EXCLUSIVE_GATEWAY.getName());
            case NodeName.PARALLEL_GATEWAY ->
                    (ParallelGateway) BpmnBuilder.createInstance(s, PARALLEL_GATEWAY.getName());
            case NodeName.INCLUSIVE_GATEWAY ->
                    (InclusiveGateway) BpmnBuilder.createInstance(s, INCLUSIVE_GATEWAY.getName());
            case NodeName.COMPLEX_GATEWAY -> (ComplexGateway) BpmnBuilder.createInstance(s, COMPLEX_GATEWAY.getName());
            case NodeName.EVENT_BASED_GATEWAY ->
                    (EventBasedGateway) BpmnBuilder.createInstance(s, EVENT_BASED_GATEWAY.getName());
            default -> null;
        };
    }

    private void setConditionExpression(Gateway gateway, AbstractFlowNodeBuilder<?, ?> s, GatewayNode gatewayNode) {
        Collection<SequenceFlow> sequenceFlows = switch (this.getNodeType()) {
            case NodeType.NodeName.EXCLUSIVE_GATEWAY ->
                    ((ExclusiveGateway) gateway).builder().getElement().getOutgoing();
            case NodeType.NodeName.INCLUSIVE_GATEWAY ->
                    ((InclusiveGateway) gateway).builder().getElement().getOutgoing();
            default -> null;
        };
        if (sequenceFlows != null) {//设置网关条件，不做类型判断，有都配置
            int i = 0;
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                ConditionExpression conditionExpression = s.getElement().getModelInstance().newInstance(ConditionExpression.class);
                GatewayNode branches = gatewayNode.getBranches().get(i);
                String expression = branches.getExpression();
                if (StrUtil.isBlankIfStr(expression) && branches.isDefaultBranches()) {
                    //未设置条件的给个默认的1==1,防止出错
                    expression = GATEWAY_DEFAULT_CONDITION_EXPRESSION;
                }
                if (StrUtil.isBlankIfStr(expression)) {
                    //未设置条件的给个默认的1==1,防止出错
                    expression = "${1==1}";
                }
                conditionExpression.setTextContent(expression);
                sequenceFlow.setConditionExpression(conditionExpression);
                i++;
            }
        }
    }

}
