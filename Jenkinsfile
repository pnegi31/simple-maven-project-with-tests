pipeline {
    agent any

    stages {
        stage("Clone") {
            steps {
                // Get some code from a GitHub repository
                git 'https://github.com/pnegi31/simple-maven-project-with-tests.git'
            }
        }
        stage("Run Tests") {
            steps {
                // Run Maven tests
                sh "mvn -Dmaven.test.failure.ignore=true clean package"
            }

            post {
                success {
                    // Publish test results and report to JIRA
                    junit (
                        testResults: '**/target/surefire-reports/TEST-*.xml',
                        testDataPublishers: [
                            jiraTestResultReporter(
                                configs: [
                                    jiraStringField(fieldKey: 'summary', value: '${DEFAULT_SUMMARY}'),
                                    jiraStringField(fieldKey: 'description', value: '${DEFAULT_DESCRIPTION}'),
                                    jiraStringArrayField(fieldKey: 'labels', values: [jiraArrayEntry(value: 'Jenkins'), jiraArrayEntry(value:'Integration')])
                                ],
                                projectKey: 'JIRA',
                                issueType: '10001', // task
                                autoRaiseIssue: true,
                                autoResolveIssue: false,
                                autoUnlinkIssue: false,
                            )
                        ]
                    )
                    archiveArtifacts 'target/*.jar'
                }
            }
        }
    }
}