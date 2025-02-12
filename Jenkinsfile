pipeline {
    agent any
    environment {
        AWS_REGION = 'us-east-1'
        ECR_REPO_URI = '762233752349.dkr.ecr.us-east-1.amazonaws.com/vote'
        ECS_CLUSTER = 'vote-cluster'
        ECS_SERVICE = 'vote-service'
        TASK_DEFINITION_FAMILY = 'vote-task'
        TASK_DEFINITION_FILE = 'task-definition.json'
        AWS_DEFAULT_REGION = 'us-east-1'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'feature/branch',
                    url: 'https://github.com/shugilbert/vote.git',
                    credentialsId: 'github-credentials'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    env.IMAGE_TAG = "${ECR_REPO_URI}:${env.BUILD_ID}"  // Assign dynamically
                    sh "docker build -t ${IMAGE_TAG} ."
                }
            }
        }

        stage('Login to AWS ECR') {
            steps {
                script {
                    sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO_URI}"
                }
            }
        }

        stage('Push to ECR') {
            steps {
                script {
                    sh "docker push ${IMAGE_TAG}"
                }
            }
        }

        stage('Update ECS Task Definition') {
            steps {
                script {
                    // Retrieve and modify task definition
                    sh """
                        aws ecs describe-task-definition --task-definition ${TASK_DEFINITION_FAMILY} --query taskDefinition --output json > ${TASK_DEFINITION_FILE}
                        jq '. | {containerDefinitions, family, executionRoleArn, networkMode, requiresCompatibilities, cpu, memory}' ${TASK_DEFINITION_FILE} > new-task-def.json
                        jq --arg IMAGE "${IMAGE_TAG}" '.containerDefinitions[0].image = $IMAGE' new-task-def.json > updated-task-def.json
                    """

                    // Register the new task definition and save response
                    sh "aws ecs register-task-definition --cli-input-json file://updated-task-def.json --output json > task-def-response.json"
                }
            }
        }

        stage('Update ECS Service') {
            steps {
                script {
                    // Extract task definition ARN from the response file
                    def taskDefArn = sh(script: "jq -r '.taskDefinition.taskDefinitionArn' task-def-response.json", returnStdout: true).trim()

                    // Update ECS service with new task definition
                    sh "aws ecs update-service --cluster ${ECS_CLUSTER} --service ${ECS_SERVICE} --task-definition ${taskDefArn}"
                }
            }
        }
    }

    post {
        success {
            echo '✅ CI/CD pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed. Check logs for details.'
        }
    }
}
