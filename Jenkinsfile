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
        CURRENT_IMAGE = 'latest' // ✅ Explicitly defining CURRENT_IMAGE
        dkr = '762233752349.dkr.ecr.us-east-1.amazonaws.com/vote'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'feature/branch',
                    url: 'https://github.com/shugilbert/vote.git',
                    credentialsId: 'github-credentials'
            }
        }

        stage('Extract Current Image Tag') {
            steps {
                script {
                    CURRENT_IMAGE = sh(
                        script: "aws ecs describe-task-definition --task-definition ${TASK_DEFINITION_FAMILY} --query 'taskDefinition.containerDefinitions[0].image' --output text",
                        returnStdout: true
                    ).trim()
                    echo "Current Image in Task Definition: ${CURRENT_IMAGE}"
                    env.CURRENT_IMAGE = CURRENT_IMAGE // ✅ Ensures global access
                }
            }
        }

        stage('Build and Tag New Docker Image') {
            steps {
                script {
                    def IMAGE_TAG = "${762233752349.dkr.ecr.us-east-1.amazonaws.com/result}:${BUILD_ID}" // ✅ Use Jenkins build ID to create a dynamic image tag
                    echo "Generated IMAGE_TAG: ${IMAGE_TAG}"
                    sh "docker build -t ${IMAGE_TAG} ."
                }
            }
        }

       stage('Login to AWS ECR') {
    steps {
        script {
            sh 'aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 762233752349.dkr.ecr.us-east-1.amazonaws.com'
        }
    }
}

stage('Push to ECR') {
    steps {
        script {
            sh 'docker push 762233752349.dkr.ecr.us-east-1.amazonaws.com/vote:latest'
        }
    }
}

stage('Update ECS Task Definition') {
    steps {
        script {
            sh 'aws ecs register-task-definition --family vote-task --container-definitions file://task-definition.json'
        }
    }
}

stage('Update ECS Service') {
    steps {
        script {
            sh 'aws ecs update-service --cluster vote-cluster --service vote-service --task-definition vote-task'
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
