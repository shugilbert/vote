pipeline {
    agent any
    environment {
        AWS_REGION = 'us-east-1'
        ECR_REPO_URI = '762233752349.dkr.ecr.us-east-1.amazonaws.com/vote'
        IMAGE_TAG = "${ECR_REPO_URI}:${env.BUILD_ID}"
        ECS_CLUSTER = 'vote-cluster'
        ECS_SERVICE = 'vote-service'
        TASK_DEFINITION_FAMILY = 'vote-task'
        TASK_DEFINITION_FILE = 'task-definition.json'
    }

    stages {
        stage('Pull Latest Image') {
            steps {
                script {
                    sh "aws ecr describe-images --repository-name vote --region ${AWS_REGION}"
                }
            }
        }

        stage('Update ECS Task Definition') {
            steps {
                script {
                    sh """
                        aws ecs describe-task-definition --task-definition ${TASK_DEFINITION_FAMILY} --query taskDefinition > ${TASK_DEFINITION_FILE}
                        jq '.taskDefinition | {containerDefinitions, family, executionRoleArn, networkMode, requiresCompatibilities, cpu, memory}' ${TASK_DEFINITION_FILE} > new-task-def.json
                        jq --arg IMAGE "${IMAGE_TAG}" '.containerDefinitions[0].image = $IMAGE' new-task-def.json > updated-task-def.json
                        aws ecs register-task-definition --cli-input-json file://updated-task-def.json
                    """
                }
            }
        }

        stage('Update ECS Service') {
            steps {
                script {
                    sh """
                        aws ecs update-service --cluster ${ECS_CLUSTER} --service ${ECS_SERVICE} --task-definition ${TASK_DEFINITION_FAMILY}
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'ECS Deployment Successful!'
        }
        failure {
            echo 'Deployment failed. Check logs for details.'
        }
    }
}

