pipeline {
    agent any
    environment {
        // Set your ECR repository URL
        ECR_REPO_URI = '762233752349.dkr.ecr.us-east-1.amazonaws.com/vote'
        IMAGE_TAG = "vote:${env.BUILD_ID}"
        AWS_DEFAULT_REGION = 'us-east-1'
    }

    stages {
        // Uncomment and use this stage if you need to perform a checkout from Git
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
                    // Build the Docker image
                    sh "docker build -t ${IMAGE_TAG} ."
                }
            }
        }

        // Uncomment and modify the 'Test Docker Image' stage if needed
        /*
        stage('Test Docker Image') {
            steps {
                script {
                    // Run tests inside the Docker container (if applicable)
                    sh "docker run --rm ${IMAGE_TAG} python -m unittest discover tests/"
                }
            }
        }
        */

        stage('Login to AWS ECR') {
            steps {
                script {
                    // Use withCredentials to inject AWS credentials and log in to AWS ECR
                    withCredentials([aws(credentialsId: 'aws-creds')]) {
                        // Log in to AWS ECR
                        sh """
                        aws ecr get-login-password --region ${AWS_DEFAULT_REGION} | docker login --username AWS --password-stdin ${ECR_REPO_URI}
                        """
                    }
                }
            }
        }

        stage('Push to ECR') {
            steps {
                script {
                    // Tag the Docker image and push to ECR
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
