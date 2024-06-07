package com.demo.camunda.node.task;

import com.demo.camunda.node.BaseNode;
import com.demo.camunda.node.BpmContext;
import com.demo.camunda.node.BpmnBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.instance.CompletionCondition;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.UserTask;

import java.util.Objects;

import static com.demo.camunda.node.NodeType.USER_TASK;

/**
 * 审批节点
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTaskNode extends BaseNode {
    public static final String CREATOR_0 = "creator";
    public static final String CREATOR = "${creator}";
    public static final String ASSIGN_0 = "assign_%s_user";
    public static final String ASSIGN = "${assign_%s_user}";
    public static final String ASSIGNS = "${assign_%s_users}";
    public static final String ASSIGNS_0 = "assign_%s_users";
    public static final String AND_MOD = "${nrOfActiveInstances == nrOfInstances}";//会签
    public static final String OR_MOD = "${nrOfCompletedInstances >= 1 }";//或签
    /**
     * 0 : 并行会签 1: 并行或签  2:顺序签
     */
    private Integer mod;

    @Override
    public AbstractFlowNodeBuilder<?, ?> createFlowNode(AbstractFlowNodeBuilder<?, ?> preFlow, BaseNode node, BpmContext context) {
        String id = BpmnBuilder.id(getNodeType(), node, context);
        UserTaskNode userTaskNode = (UserTaskNode) node;
        UserTask userTask = (UserTask) BpmnBuilder.createInstance(preFlow, USER_TASK.getName());
        userTask.setId(id);
        userTask.setName(node.getName());
        if (Objects.isNull(mod)) {//简单签
            userTask.setCamundaAssignee(String.format(ASSIGN, userTask.getId()));
        } else {//会签
            userTask.setCamundaAssignee(String.format(ASSIGN, userTask.getId()));
            MultiInstanceLoopCharacteristics multi = BpmnBuilder.createElement(userTask, MultiInstanceLoopCharacteristics.class);
            multi.setCamundaCollection(String.format(ASSIGNS, userTask.getId()));
            multi.setCamundaElementVariable(String.format(ASSIGN_0, userTask.getId()));
            CompletionCondition completionCondition = BpmnBuilder.createElement(multi, CompletionCondition.class);
            multi.setSequential(false);//默认并行签
            if (mod == 1) {// 或签
                completionCondition.setTextContent(OR_MOD);
            } else if (mod == 0) {// 会签
                completionCondition.setTextContent(AND_MOD);
            } else { // 并行，为true时就是顺序签
                multi.setSequential(true);
            }
            multi.setCompletionCondition(completionCondition);
            userTask.setLoopCharacteristics(multi);// 站内信和提醒
        }
        return userTask.builder();
    }

    @Override
    public String getNodeType() {
        return USER_TASK.getName();
    }

}
