package com.demo.camunda;
/**
 * @author zongkx
 */

import com.demo.camunda.node.BpmContext;
import com.demo.camunda.node.BpmnBuilder;
import com.demo.camunda.node.NodeType;
import com.demo.camunda.node.event.EndEventNode;
import com.demo.camunda.node.task.SendTaskNode;
import com.demo.camunda.node.task.UserTaskNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;


/**
 * @author zongkx
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BpmTests {

    @BeforeAll
    public void before() {

    }

    @Test
    @SneakyThrows
    public void a1() {
        SendTaskNode startNode = new SendTaskNode();
        startNode.setNodeType(NodeType.NodeName.USER_TASK);
        UserTaskNode approveNode = new UserTaskNode();
        approveNode.setNodeType(NodeType.NodeName.USER_TASK);
        approveNode.setMod(1);
        startNode.setNext(approveNode);

        EndEventNode endNode = new EndEventNode();
        approveNode.setNext(endNode);
        String s = new ObjectMapper().writeValueAsString(startNode);
        System.out.println(s);
        BpmContext context = BpmnBuilder.build("demo", startNode);
        String xml = Bpmn.convertToString(context.getInstance());
        System.out.println(xml);
    }
}