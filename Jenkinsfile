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
                    junit(
                        testResults: '**/target/surefire-reports/TEST-*.xml',
                        testDataPublishers: [
                            jiraTestResultReporter(
                                configs: [
                                    jiraStringField(fieldKey: 'summary', value: '${DEFAULT_SUMMARY}'),
                                    jiraStringField(fieldKey: 'description', value: '${DEFAULT_DESCRIPTION}'),
                                    jiraStringArrayField(fieldKey: 'labels', values: [jiraArrayEntry(value: 'Jenkins'), jiraArrayEntry(value: 'Integration')])
                                ],
                                projectKey: 'SCRUM',
                                issueType: '10001',
                                autoRaiseIssue: true,
                                autoResolveIssue: false,
                                autoUnlinkIssue: false
                            )
                        ]
                    )
                    archiveArtifacts 'target/*.jar'
                }
            }
        }
    }
}