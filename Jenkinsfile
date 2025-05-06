pipeline {
    agent any
    environment {
        DEPLOY_SERVER = 'ec2-user@15.223.121.66'
        DOCKER_IMAGE = 'your-spring-app'
    }
    stages {
        stage('Clone') {
            steps {
                git branch: 'main', url: 'https://github.com/Mouni7777/spring-boot-demo.git'
            }
        }
        stage('Build') {
            steps {
                sh '/opt/maven/bin/mvn clean package -DskipTests'
            }
        }
        stage('Test') {
            steps {
                sh '/opt/maven/bin/mvn test'
            }
        }
        stage('Docker Build') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE .'
            }
        }
        stage('Trivy Scan') {
            steps {
                script {
                    // Scan the local image and output in table format
                    sh 'trivy image --severity HIGH,CRITICAL $DOCKER_IMAGE'
                }
            }
        }
        stage('Push & Deploy') {
            environment {
                DEPLOY_SERVER = 'ec2-user@15.223.121.66'
                DOCKER_IMAGE = 'your-spring-app' // replace with actual image
            }
            steps {
                sshagent(['ssh-deploy-key']) {
                    sh """
                        docker save ${DOCKER_IMAGE} | bzip2 | ssh -o StrictHostKeyChecking=no ${DEPLOY_SERVER} 'bunzip2 | docker load'
                        ssh -o StrictHostKeyChecking=no ${DEPLOY_SERVER} 'docker stop ${DOCKER_IMAGE} || true && docker rm ${DOCKER_IMAGE} || true'
                        ssh -o StrictHostKeyChecking=no ${DEPLOY_SERVER} 'docker run -d --name ${DOCKER_IMAGE} -p 8081:8080 ${DOCKER_IMAGE}'
                    """
                }
            }
        }
    }
    post {
        failure {
            echo 'Pipeline failed. Check logs for errors (Trivy scan, build, or deploy issues).'
        }
    }
}
