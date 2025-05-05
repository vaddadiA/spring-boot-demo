pipeline {
    agent any
    environment {
        DOCKER_IMAGE = 'your-spring-app'
        DEPLOY_SERVER = 'root@3.99.157.27'
    }
    stages {
        stage('Clone') {
            steps {
                git branch: 'main', url: 'https://github.com/Mouni7777/spring-boot-demo.git'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Docker Build') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE .'
            }
        }
        stage('Push & Deploy') {
            steps {
                sshagent(['ssh-deploy-key']) {
                    sh '''
                    docker save $DOCKER_IMAGE | bzip2 | ssh $DEPLOY_SERVER 'bunzip2 | docker load'
                    ssh $DEPLOY_SERVER 'docker stop $DOCKER_IMAGE || true && docker rm $DOCKER_IMAGE || true'
                    ssh $DEPLOY_SERVER 'docker run -d --name $DOCKER_IMAGE -p 8080:8080 $DOCKER_IMAGE'
                    '''
                }
            }
        }
    }
}
