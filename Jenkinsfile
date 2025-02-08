pipeline {
    agent any
    environment {
        // Set your ECR repository URL
        ECR_REPO_URI = '762233752349.dkr.ecr.us-east-1.amazonaws.com/vote'
        IMAGE_TAG = "vote:${env.BUILD_ID}"
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
                    docker tag ${IMAGE_TAG} ${ECR_REPO_URI}:${IMAGE_TAG}
                    docker push ${ECR_REPO_URI}:${IMAGE_TAG}
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Docker image successfully built, tested, and pushed to ECR!'
        }
        failure {
            echo 'Something went wrong. Check the logs for more details.'
        }
    }
}
