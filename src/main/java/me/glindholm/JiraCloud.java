package me.glindholm;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import me.glindholm.jira.cloud.api.IssueCommentsApi;
import me.glindholm.jira.cloud.api.IssuesApi;
import me.glindholm.jira.cloud.api.MyselfApi;
import me.glindholm.jira.cloud.api.ProjectsApi;
import me.glindholm.jira.cloud.invoker.ApiClient;
import me.glindholm.jira.cloud.invoker.ApiException;
import me.glindholm.jira.cloud.model.CreatedIssue;
import me.glindholm.jira.cloud.model.IssueTypeDetails;
import me.glindholm.jira.cloud.model.PageBeanProject;
import me.glindholm.jira.cloud.model.Project;
import me.glindholm.jira.cloud.model.User;

public class JiraCloud {
    public static void main(String[] args) throws ApiException {
        ApiClient apiClient = new ApiClient();
        apiClient.setUsername("gnl@ballroomdancemusic.info");
        apiClient.setPassword("xxx");
        apiClient.setDebugging(true);
        apiClient.setBasePath("https://glindholm.atlassian.net");
        // Look up my user
        MyselfApi myselfApi = new MyselfApi(apiClient);
        User user = myselfApi.getCurrentUser(null);
        // Find the project called "JCFC"
        // createIssue(apiClient, user);
        IssuesApi issues = new IssuesApi(apiClient);
        Boolean updateHistory;

        IssueCommentsApi issueComment = new IssueCommentsApi(apiClient);
        String expand = "";
        String key = "JCFC-1";
        String myText = "NO! It is not";
        JSONObject ja = new JSONObject().put("body",
                new JSONObject().put("type", "doc").put("version", 1).put("content",
                        new JSONArray().put(new JSONObject().put("type", "paragraph").put("content",
                                new JSONArray().put(new JSONObject().put("type", "text").put("text", myText))))));
        Comment newComment = issueComment.addComment(key, ja.toMap(), expand);
        System.out.println(newComment);
    }

    private static void createIssue(ApiClient apiClient, User user) throws ApiException {
        ProjectsApi projectsApi = new ProjectsApi(apiClient);
        PageBeanProject projects = projectsApi.searchProjects(null, // startAt
                null, // maxResults
                null, // orderBy
                null, // id
                null, // keys
                "JCFC", // query
                null, // typeKey
                null, // categoryId
                null, // action
                "issueTypes", // expand issue types
                null, // status
                null, // properties
                null // propertyQuery
                );

        if (projects.getValues().size() > 0) {
            // Find the first project
            Project project = projects.getValues().get(0);

            String issueTypeId = "0";
            // Find the bug issue type
            for (IssueTypeDetails issueTypeDetails : project.getIssueTypes()) {
                if (issueTypeDetails.getName().equalsIgnoreCase("task")) {
                    issueTypeId = issueTypeDetails.getId();
                }
            }

            // Create an issue
            IssuesApi issuesApi = new IssuesApi(apiClient);
            Map<String, Object> issueCreateParams = new HashMap<>();

            Map<String, Object> fields = new HashMap<>();
            fields.put("summary", "Create a test issue");
            Map<String, Object> issueTypeField = new HashMap<>();
            issueTypeField.put("id", issueTypeId);
            fields.put("issuetype", issueTypeField);
            Map<String, Object> projectField = new HashMap<>();
            projectField.put("id", project.getId());
            fields.put("project", projectField);
            // Assign to me
            Map<String, Object> assigneeField = new HashMap<>();
            assigneeField.put("accountId", user.getAccountId());
            fields.put("assignee", assigneeField);

            issueCreateParams.put("fields", fields);

            CreatedIssue createdIssue = issuesApi.createIssue(issueCreateParams, true);

            System.out.println("Created an issue, and assigned it to " + user.getDisplayName());
            System.out.println("The issue key is " + createdIssue.getKey());
        }
    }
}
