package com.demo.camunda.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BpmContext {
    private BpmnModelInstance instance;
    private Map<String, Integer> nodeIds = new HashMap<>();
}