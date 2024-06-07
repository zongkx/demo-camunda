package com.demo.camunda.node;

import cn.hutool.core.util.StrUtil;
import com.demo.camunda.node.event.EndEventNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.lang.reflect.Method;
import java.util.UUID;

@Slf4j
public class BpmnBuilder {
    /**
     * json 转 bpmn xml
     */
    public static BpmContext build(String processId, BaseNode node) {
        BpmContext context = new BpmContext();
        ProcessBuilder executableProcess = Bpmn.createExecutableProcess(processId);
        StartEventBuilder startEventBuilder = executableProcess.startEvent();
        create(startEventBuilder, node, context);//递归新建节点
        BpmnModelInstance modelInstance = startEventBuilder.done();
        Bpmn.validateModel(modelInstance);// 校验
        context.setInstance(modelInstance);
        return context;
    }

    public static AbstractFlowNodeBuilder<?, ?> create(AbstractFlowNodeBuilder<?, ?> preFlow, BaseNode node, BpmContext context) {
        AbstractFlowNodeBuilder<?, ?> newFlow = node.createFlowNode(preFlow, node, context);
        AbstractFlowNodeBuilder<?, ?> result = newFlow;
        if (result == null) {
            result = preFlow;
        }
        if (node.getNext() != null) {
            node.getNext().setMergeGatewayId(node.getMergeGatewayId());
            if (node instanceof EndEventNode) {
                result = node.createFlowNode(newFlow, node, context);//如果是 end节点，则不再递归
            } else {
                result = create(newFlow, node.getNext(), context);
            }
        }
        return result;
    }


    protected static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) {
        Method method;
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                return method;
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    public static String id(String prefix, BaseNode node, BpmContext context) {
        if (node == null || StrUtil.isBlankIfStr(node.getId())) {
            String id = id(prefix, "");
            node.setId(id);
            context.getNodeIds().put(id, 1);
            return id;
        }
        String id = node.getId();
        if (context.getNodeIds().get(id) != null) {
            //ID重复，重新生成
            id = id(prefix, "");
        }
        if (!id.startsWith(prefix)) {
            id = prefix + "_" + id;
        }
        node.setId(id);
        context.getNodeIds().put(id, 1);
        return id;
    }

    public static String id(String prefix, String suffix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").toLowerCase() + "_" + suffix;
    }

    public static Object createInstance(AbstractFlowNodeBuilder<?, ?> flowNodeBuilder, String nodeType) {
        Method createTarget = BpmnBuilder.getDeclaredMethod(flowNodeBuilder, "createTarget", Class.class);
        try {
            if (createTarget != null) {
                createTarget.setAccessible(true);
            }
            Class<? extends ModelElementInstance> clazz = NodeType.getElementTypeClass(nodeType)
                    .orElseThrow(() -> new BpmnModelException("Unsupported BPMN element of type " + nodeType));
            return createTarget.invoke(flowNodeBuilder, clazz);
        } catch (Exception e) {
            log.error("error", e);
            throw new RuntimeException(e);
        }
    }

    public static <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, Class<T> elementClass) {
        return createElement(parentElement, elementClass, "");
    }

    public static <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, Class<T> elementClass, String id) {
        T element = parentElement.getModelInstance().newInstance(elementClass);
        if (StringUtils.isNotBlank(id)) {
            element.setAttributeValue("id", id, true);
        }
        parentElement.addChildElement(element);
        return element;
    }

}
