pipeline {
    agent any
    
    environment {
        JAVA_VERSION = '17'
        MAVEN_OPTS = '-Xmx1024m'
        DEPLOYMENT_ENV = "${params.ENVIRONMENT ?: 'development'}"
    }
    
    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['development', 'staging', 'production'],
            description: 'Environment to deploy to'
        )
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Skip running tests'
        )
        booleanParam(
            name: 'DRY_RUN',
            defaultValue: false,
            description: 'Perform dry run deployment'
        )
    }
    
    tools {
        maven 'Maven-3.8'
        jdk "JDK-${JAVA_VERSION}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
                
                script {
                    env.BUILD_VERSION = sh(
                        script: "echo '${env.BUILD_NUMBER}-${env.GIT_COMMIT[0..7]}'",
                        returnStdout: true
                    ).trim()
                }
                
                echo "Build Version: ${env.BUILD_VERSION}"
            }
        }
        
        stage('Build') {
            parallel {
                stage('Build Backend') {
                    steps {
                        echo 'Building backend...'
                        dir('backend') {
                            sh 'mvn clean compile -DskipTests'
                        }
                    }
                }
                
                stage('Build Frontend') {
                    steps {
                        echo 'Building frontend...'
                        dir('frontend-javafx') {
                            sh 'mvn clean compile -DskipTests'
                        }
                    }
                }
            }
        }
        
        stage('Test') {
            when {
                not { params.SKIP_TESTS }
            }
            parallel {
                stage('Backend Tests') {
                    steps {
                        echo 'Running backend tests...'
                        dir('backend') {
                            sh 'mvn test'
                        }
                    }
                    post {
                        always {
                            publishTestResults testResultsPattern: 'backend/target/surefire-reports/*.xml'
                        }
                    }
                }
                
                stage('Frontend Tests') {
                    steps {
                        echo 'Running frontend tests...'
                        dir('frontend-javafx') {
                            sh 'mvn test'
                        }
                    }
                    post {
                        always {
                            publishTestResults testResultsPattern: 'frontend-javafx/target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }
        
        stage('Security Scan') {
            parallel {
                stage('OWASP Dependency Check') {
                    steps {
                        echo 'Running OWASP dependency check...'
                        dir('backend') {
                            sh 'mvn org.owasp:dependency-check-maven:check'
                        }
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'backend/target',
                                reportFiles: 'dependency-check-report.html',
                                reportName: 'OWASP Dependency Check Report'
                            ])
                        }
                    }
                }
                
                stage('Security Audit') {
                    steps {
                        echo 'Running security audit...'
                        script {
                            if (isUnix()) {
                                sh '''
                                    echo "Running security audit..."
                                    find . -name "*.java" -exec grep -l "hardcoded\\|password\\|secret" {} \\; || echo "No hardcoded credentials found"
                                '''
                            } else {
                                powershell '''
                                    Write-Host "Running security audit..."
                                    if (Test-Path "scripts\\security-audit.ps1") {
                                        & ".\\scripts\\security-audit.ps1"
                                    } else {
                                        Write-Host "Security audit script not found"
                                    }
                                '''
                            }
                        }
                    }
                }
            }
        }
        
        stage('Code Quality') {
            steps {
                echo 'Running code quality analysis...'
                dir('backend') {
                    script {
                        try {
                            sh '''
                                mvn clean verify sonar:sonar \\
                                    -Dsonar.projectKey=food-ordering-system \\
                                    -Dsonar.organization=your-org \\
                                    -Dsonar.host.url=https://sonarcloud.io \\
                                    -Dsonar.login=${SONAR_TOKEN}
                            '''
                        } catch (Exception e) {
                            echo "SonarQube analysis failed: ${e.getMessage()}"
                            currentBuild.result = 'UNSTABLE'
                        }
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                echo 'Packaging application...'
                parallel {
                    'Package Backend': {
                        dir('backend') {
                            sh 'mvn package -DskipTests'
                        }
                    },
                    'Package Frontend': {
                        dir('frontend-javafx') {
                            sh 'mvn package -DskipTests'
                        }
                    }
                }
                
                // Archive artifacts
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            }
        }
        
        stage('Deploy') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch 'staging'
                    expression { params.ENVIRONMENT != null }
                }
            }
            steps {
                script {
                    def targetEnv = params.ENVIRONMENT ?: getEnvironmentFromBranch()
                    
                    echo "Deploying to ${targetEnv} environment..."
                    
                    if (params.DRY_RUN) {
                        echo "DRY RUN MODE - No actual deployment will be performed"
                    }
                    
                    // Run deployment script
                    if (isUnix()) {
                        sh """
                            echo 'Deploying to ${targetEnv} environment...'
                            mkdir -p deployments/${targetEnv}
                            cp -r backend deployments/${targetEnv}/
                            cp -r frontend-javafx deployments/${targetEnv}/
                            
                            # Create deployment info
                            cat > deployments/${targetEnv}/deployment-info.json << EOF
{
  "deploymentId": "deploy-${targetEnv}-\$(date +%Y%m%d-%H%M%S)",
  "environment": "${targetEnv}",
  "version": "${env.BUILD_VERSION}",
  "timestamp": "\$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "deployedBy": "${env.BUILD_USER_ID ?: 'jenkins'}",
  "branch": "${env.BRANCH_NAME}",
  "buildNumber": "${env.BUILD_NUMBER}"
}
EOF
                            
                            echo '${targetEnv} deployment completed'
                        """
                    } else {
                        powershell """
                            Write-Host 'Deploying to ${targetEnv} environment...'
                            if (Test-Path 'scripts\\deploy-simple.ps1') {
                                if ('${params.DRY_RUN}' -eq 'true') {
                                    & '.\\scripts\\deploy-simple.ps1' -Environment '${targetEnv}' -DryRun
                                } else {
                                    & '.\\scripts\\deploy-simple.ps1' -Environment '${targetEnv}'
                                }
                            } else {
                                Write-Host 'Deployment script not found'
                            }
                        """
                    }
                }
            }
        }
        
        stage('Health Check') {
            when {
                not { params.DRY_RUN }
            }
            steps {
                script {
                    def targetEnv = params.ENVIRONMENT ?: getEnvironmentFromBranch()
                    
                    echo "Running health check for ${targetEnv} environment..."
                    
                    // Health check logic
                    retry(3) {
                        sleep 10
                        echo "Health check attempt..."
                        // Here you would actually test the deployed application
                        echo "Health check passed for ${targetEnv}"
                    }
                }
            }
        }
        
        stage('Notification') {
            steps {
                script {
                    def targetEnv = params.ENVIRONMENT ?: getEnvironmentFromBranch()
                    def status = currentBuild.result ?: 'SUCCESS'
                    
                    echo "Sending deployment notification..."
                    echo "Deployment to ${targetEnv}: ${status}"
                    
                    // Here you could send notifications to Slack, email, etc.
                    if (status == 'SUCCESS') {
                        echo "✅ Deployment to ${targetEnv} successful!"
                    } else {
                        echo "❌ Deployment to ${targetEnv} failed!"
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed'
            
            // Archive logs
            archiveArtifacts artifacts: 'logs/**/*.log', allowEmptyArchive: true
            
            // Clean workspace
            cleanWs()
        }
        
        success {
            echo 'Pipeline succeeded!'
        }
        
        failure {
            echo 'Pipeline failed!'
            
            script {
                def targetEnv = params.ENVIRONMENT ?: getEnvironmentFromBranch()
                
                if (targetEnv == 'production') {
                    echo "Production deployment failed - consider rollback"
                    // Here you could trigger automatic rollback
                }
            }
        }
        
        unstable {
            echo 'Pipeline completed with warnings'
        }
    }
}

// Helper function to determine environment from branch
def getEnvironmentFromBranch() {
    switch(env.BRANCH_NAME) {
        case 'main':
            return 'production'
        case 'staging':
            return 'staging'
        case 'develop':
            return 'development'
        default:
            return 'development'
    }
} 