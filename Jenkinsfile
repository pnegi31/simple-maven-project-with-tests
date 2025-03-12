pipeline {
    agent any

    stages {
        stage("Run Tests") {
            steps {
                script {
                    // Determine the branch behavior based on the branch name
                    String branchBehavior = "fail" // Default to "fail"
                    if (env.BRANCH_NAME == "Feature_2") {
                        branchBehavior = "pass"
                    } else if (env.BRANCH_NAME == "Feature_3") {
                        branchBehavior = "random"
                    }

                    // Run Maven with the branch behavior as a system property
                    sh "mvn -Dmaven.test.failure.ignore=true -Dbranch.behavior=${branchBehavior} clean package"
                }
            }
            post {
                success {
                    script {
                        // Find similar issues for test failures
                        def testResults = readJSON file: 'target/surefire-reports/TEST-*.xml'
                        testResults.each { testResult ->
                            if (testResult.failures > 0) {
                            def issueKey = findSimilarIssue(
                                    testResult.testSuite,
                                    testResult.path,
                                    env.GIT_COMMIT,
                                    env.CORE_VERSION,
                                    env.CORE_LINE,
                                    "JIRA"
                                )

                                // Check if the issue is resolved
                                boolean isResolved = issueKey ? isTicketResolved(issueKey) : false

                                // Set autoUnlinkIssue based on the resolved status
                                boolean autoUnlinkIssue = isResolved

                                // Publish test results and report to JIRA
                                junit(
                                    testResults: '**/target/surefire-reports/TEST-*.xml',
                                    testDataPublishers: [
                                        jiraTestResultReporter(
                                            configs: [
                                                jiraStringField(fieldKey: 'summary', value: '${DEFAULT_SUMMARY}'),
                                                jiraStringField(fieldKey: 'description', value: '${DEFAULT_DESCRIPTION}'),
                                                jiraStringArrayField(fieldKey: 'labels', values: [jiraArrayEntry(value: 'Jenkins'), jiraArrayEntry(value: 'Integration')])
                                            ],
                                            projectKey: 'JIRA',
                                            issueType: '10001',
                                            autoRaiseIssue: true,
                                            autoResolveIssue: false,
                                            autoUnlinkIssue: autoUnlinkIssue
                                        )
                                    ]
                                )
                            }
                        }
                    }
                    archiveArtifacts 'target/*.jar'
                }
            }
        }
    }
}

// Helper function to handle JIRA credentials
def withJiraCredentials(Closure body) {
    withCredentials([usernameColonPassword(credentialsId: "release-notes-api-jira-token", variable: 'jira_cred')]) {
        body()
    }
}

// Helper function to check if a JIRA ticket is resolved
def isTicketResolved(issueKey) {
    withJiraCredentials {
        withEnv(["JIRA_URL=http://localhost:8080/api/latest/issue", "ISSUE_KEY=$issueKey"]) {
            def ticketStatus = sh(returnStdout: true, script: '''
                curl -s -u $jira_cred -X GET -H "Content-Type: application/json" $JIRA_URL/$ISSUE_KEY | jq -r '.fields.status.name'
            ''').trim()
            return ticketStatus == "Resolved"
        }
    }
}

// Helper function to find similar issues in JIRA
def findSimilarIssue(testSuite, path, commitSha, coreVersion, coreLine, jiraProject) {
    withJiraCredentials {
        withEnv(["JIRA_URL=http://localhost:8080/rest/api/latest/search"]) {
            def searchQuery = """
                project = "$jiraProject" AND
                summary ~ "$testClassName.$testMethodName" AND
                description ~ "$path" AND
                status NOT IN (Resolved, Closed)
            """.stripIndent().trim()
            def encodedQuery = URLEncoder.encode(searchQuery, 'UTF-8')
            def searchResults = sh(returnStdout: true, script: '''
                curl -s -u $jira_cred -X GET -H "Content-Type: application/json" \
                "$JIRA_URL?jql=${encodedQuery}&maxResults=1" | jq -r '.issues[0].key'
            ''').trim()
            return searchResults ?: null
        }
    }
}