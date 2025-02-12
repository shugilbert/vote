pipeline {
    agent any
    environment {
        AWS_REGION = 'us-east-1'
        ECR_REPO_URI = '762233752349.dkr.ecr.us-east-1.amazonaws.com/vote'
        IMAGE_TAG = "vote:${env.BUILD_ID}"
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
                    sh "docker build -t ${IMAGE_TAG} ."
                }
            }
        }

        stage('Login to AWS ECR') {
            steps {
                script {
                    sh """
                        aws ecr get-login-password --region ${AWS_DEFAULT_REGION} | docker login --username AWS --password-stdin ${ECR_REPO_URI}
                    """
                }
            }
        }

        stage('Push to ECR') {
            steps {
                script {
                    sh """
                        docker tag ${IMAGE_TAG} ${ECR_REPO_URI}:${env.BUILD_ID}
                        docker push ${ECR_REPO_URI}:${env.BUILD_ID}
                    """
                }
            }
        }

        stage('Update ECS Task Definition') {
            steps {
                script {
                    sh """
                        aws ecs describe-task-definition --task-definition ${TASK_DEFINITION_FAMILY} --query taskDefinition > ${TASK_DEFINITION_FILE}
                        jq '.taskDefinition | {containerDefinitions, family, executionRoleArn, networkMode, requiresCompatibilities, cpu, memory}' ${TASK_DEFINITION_FILE} > new-task-def.json
                        jq --arg IMAGE "${ECR_REPO_URI}:${env.BUILD_ID}" '.containerDefinitions[0].image = $IMAGE' new-task-def.json > updated-task-def.json
                        aws ecs register-task-definition --cli-input-json file://updated-task-def.json
                    """
                }
            }
        }

        stage('Update ECS Service') {
            steps {
                script {
                    // Register the ECS service using the updated task definition ARN
                    def taskDefArn = sh(script: "aws ecs describe-task-definition --task-definition ${TASK_DEFINITION_FAMILY} --query 'taskDefinition.taskDefinitionArn' --output text", returnStdout: true).trim()
                    sh """
                        aws ecs update-service --cluster ${ECS_CLUSTER} --service ${ECS_SERVICE} --task-definition ${taskDefArn}
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'CI and CD pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check logs for more details.'
        }
    }
}
