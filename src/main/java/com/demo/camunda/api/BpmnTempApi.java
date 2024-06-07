package com.demo.camunda.api;

import com.demo.camunda.node.BaseNode;
import com.demo.camunda.node.BpmContext;
import com.demo.camunda.node.BpmnBuilder;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bpmn/temp")
@RequiredArgsConstructor
public class BpmnTempApi {
    private final RepositoryService repositoryService;

    @PostMapping("/create")
    public Boolean create(@RequestBody BaseNode baseNode) {
        BpmContext context = BpmnBuilder.build("demo", baseNode);
        DeploymentWithDefinitions deployment = repositoryService.createDeployment()
                .name("demo")
                .source("bpmn")
                .addModelInstance("demo.bpmn", context.getInstance())
                .deployWithResult();
        return true;
    }

}
