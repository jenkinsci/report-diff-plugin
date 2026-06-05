package io.jenkins.plugins.report.genericdiff;

import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for pipeline support in RpmsReportPublisher
 */
@WithJenkins
class RpmsReportPublisherPipelineTest {

    @Test
    void testPipelineWithEchoCommand(JenkinsRule jenkins) throws Exception {
        // Create a pipeline job
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "test-pipeline");
        
        // Define a simple pipeline that uses the plugin
        String pipelineScript = 
            "node {\n" +
            "    writeFile file: 'test.txt', text: 'line1\\nline2\\nline3'\n" +
            "    step([\n" +
            "        $class: 'RpmsReportPublisher',\n" +
            "        buildstopast: 10,\n" +
            "        moveunstable: false,\n" +
            "        movefailed: false,\n" +
            "        moveempty: false,\n" +
            "        configurations: [[\n" +
            "            id: 'test-id',\n" +
            "            command: 'cat test.txt',\n" +
            "            maintitle: 'Test Report',\n" +
            "            addedlines: 'Added',\n" +
            "            removedlines: 'Removed',\n" +
            "            updatedlines: 'Changed',\n" +
            "            nochanges: 'No changes'\n" +
            "        ]]\n" +
            "    ])\n" +
            "}";
        
        project.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        
        // Run the pipeline
        WorkflowRun run1 = jenkins.assertBuildStatusSuccess(project.scheduleBuild2(0));
        
        // Verify the action was added
        RpmsReportAction action = run1.getAction(RpmsReportAction.class);
        assertNotNull(action, "RpmsReportAction should be present");
        assertEquals(DefaultStrings.MAIN_TITLE_REPORT, action.getDisplayName());
        
        // Run again to test diff
        WorkflowRun run2 = jenkins.assertBuildStatusSuccess(project.scheduleBuild2(0));
        RpmsReportAction action2 = run2.getAction(RpmsReportAction.class);
        assertNotNull(action2, "RpmsReportAction should be present in second run");
    }

    @Test
    void testPipelineWithMultipleConfigurations(JenkinsRule jenkins) throws Exception {
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "test-multi-config");
        
        String pipelineScript = 
            "node {\n" +
            "    writeFile file: 'file1.txt', text: 'content1'\n" +
            "    writeFile file: 'file2.txt', text: 'content2'\n" +
            "    step([\n" +
            "        $class: 'RpmsReportPublisher',\n" +
            "        buildstopast: 5,\n" +
            "        moveunstable: false,\n" +
            "        movefailed: false,\n" +
            "        moveempty: false,\n" +
            "        configurations: [\n" +
            "            [\n" +
            "                id: 'config1',\n" +
            "                command: 'cat file1.txt',\n" +
            "                maintitle: 'Config 1',\n" +
            "                addedlines: 'Added',\n" +
            "                removedlines: 'Removed',\n" +
            "                updatedlines: 'Changed',\n" +
            "                nochanges: 'No changes'\n" +
            "            ],\n" +
            "            [\n" +
            "                id: 'config2',\n" +
            "                command: 'cat file2.txt',\n" +
            "                maintitle: 'Config 2',\n" +
            "                addedlines: 'Added',\n" +
            "                removedlines: 'Removed',\n" +
            "                updatedlines: 'Changed',\n" +
            "                nochanges: 'No changes'\n" +
            "            ]\n" +
            "        ]\n" +
            "    ])\n" +
            "}";
        
        project.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        
        WorkflowRun run = jenkins.assertBuildStatusSuccess(project.scheduleBuild2(0));
        
        RpmsReportAction action = run.getAction(RpmsReportAction.class);
        assertNotNull(action, "RpmsReportAction should be present");
        
        RpmsReport report = action.getTarget();
        assertNotNull(report, "RpmsReport should be available");
        
        List<RpmsReportSingle> index = report.getIndex();
        assertEquals(2, index.size(), "Should have 2 configurations");
    }

    @Test
    void testPipelineWithMoveUnstable(JenkinsRule jenkins) throws Exception {
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "test-unstable");
        
        // First run - establish baseline
        String pipelineScript1 = 
            "node {\n" +
            "    writeFile file: 'data.txt', text: 'initial'\n" +
            "    step([\n" +
            "        $class: 'RpmsReportPublisher',\n" +
            "        buildstopast: 10,\n" +
            "        moveunstable: true,\n" +
            "        movefailed: false,\n" +
            "        moveempty: false,\n" +
            "        configurations: [[\n" +
            "            id: 'data',\n" +
            "            command: 'cat data.txt',\n" +
            "            maintitle: 'Data',\n" +
            "            addedlines: 'Added',\n" +
            "            removedlines: 'Removed',\n" +
            "            updatedlines: 'Changed',\n" +
            "            nochanges: 'No changes'\n" +
            "        ]]\n" +
            "    ])\n" +
            "}";
        
        project.setDefinition(new CpsFlowDefinition(pipelineScript1, true));
        WorkflowRun run1 = jenkins.assertBuildStatusSuccess(project.scheduleBuild2(0));
        
        // Second run - with changes, should become unstable
        String pipelineScript2 = 
            "node {\n" +
            "    writeFile file: 'data.txt', text: 'changed'\n" +
            "    step([\n" +
            "        $class: 'RpmsReportPublisher',\n" +
            "        buildstopast: 10,\n" +
            "        moveunstable: true,\n" +
            "        movefailed: false,\n" +
            "        moveempty: false,\n" +
            "        configurations: [[\n" +
            "            id: 'data',\n" +
            "            command: 'cat data.txt',\n" +
            "            maintitle: 'Data',\n" +
            "            addedlines: 'Added',\n" +
            "            removedlines: 'Removed',\n" +
            "            updatedlines: 'Changed',\n" +
            "            nochanges: 'No changes'\n" +
            "        ]]\n" +
            "    ])\n" +
            "}";
        
        project.setDefinition(new CpsFlowDefinition(pipelineScript2, true));
        WorkflowRun run2 = jenkins.assertBuildStatus(Result.UNSTABLE, project.scheduleBuild2(0));
        
        assertNotNull(run2.getAction(RpmsReportAction.class), "RpmsReportAction should be present");
    }

    @Test
    void testPipelineWithDiffReportAction(JenkinsRule jenkins) throws Exception {
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "test-diff-action");
        
        String pipelineScript = 
            "node {\n" +
            "    writeFile file: 'test.txt', text: 'test content'\n" +
            "    step([\n" +
            "        $class: 'RpmsReportPublisher',\n" +
            "        buildstopast: 10,\n" +
            "        moveunstable: false,\n" +
            "        movefailed: false,\n" +
            "        moveempty: false,\n" +
            "        configurations: [[\n" +
            "            id: 'testid',\n" +
            "            command: 'cat test.txt',\n" +
            "            maintitle: 'Test',\n" +
            "            addedlines: 'Added',\n" +
            "            removedlines: 'Removed',\n" +
            "            updatedlines: 'Changed',\n" +
            "            nochanges: 'No changes'\n" +
            "        ]]\n" +
            "    ])\n" +
            "}";
        
        project.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun run = jenkins.assertBuildStatusSuccess(project.scheduleBuild2(0));
        
        // Verify both actions are present
        RpmsReportAction rpmsAction = run.getAction(RpmsReportAction.class);
        DiffReportAction diffAction = run.getAction(DiffReportAction.class);
        
        assertNotNull(rpmsAction, "RpmsReportAction should be present");
        assertNotNull(diffAction, "DiffReportAction should be present");
        assertEquals(DefaultStrings.DIFF_TITLE_REPORT, diffAction.getDisplayName());
    }
}

