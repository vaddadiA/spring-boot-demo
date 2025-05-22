pipeline {
    agent any

    tools {
        maven 'Maven 3.8.6' // <-- Use the Maven tool you configured in Jenkins
    }

    environment {
        DOCKER_IMAGE = 'spring-app'
        IMAGE_TAG = "${BUILD_NUMBER}"
        NEXUS_REGISTRY = '99.79.71.6:8083'
        FULL_IMAGE_NAME = "${NEXUS_REGISTRY}/${DOCKER_IMAGE}:${IMAGE_TAG}"
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
                sh 'docker build -t $FULL_IMAGE_NAME .'
            }
        }

        stage('Trivy Scan') {
            steps {
                sh 'trivy image --severity HIGH,CRITICAL $FULL_IMAGE_NAME || true'
            }
        }

        stage('Docker Push to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-docker-creds', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                    sh '''
                        echo "$NEXUS_PASS" | docker login $NEXUS_REGISTRY -u "$NEXUS_USER" --password-stdin
                        docker push $FULL_IMAGE_NAME
                        docker logout $NEXUS_REGISTRY
                       ... 
                }
            }
        }

        stage('Configure EKS') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds-id']]) {
                    sh '''
                        export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
                        export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
                        aws eks update-kubeconfig --region ca-central-1 --name nk

                        # Run kubectl commands in same shell with env
                        if ! kubectl get deployment spring-app --namespace=default; then
                            echo "spring-app deployment not found. Applying YAMLs..."
                            kubectl apply -f k8s/deployment.yaml
                            kubectl apply -f k8s/service.yaml
                        else
                            echo "spring-app deployment already exists. Skipping apply."
                        fi

                        kubectl set image deployment/spring-app spring-app=$FULL_IMAGE_NAME --namespace=default
                    '''
                }
            }
        }
    }

    post {
        failure {
            echo '❌ Pipeline failed. Check logs for errors in build, scan, push, or deploy.'
        }
        success {
            echo "✅ Deployment successful: $FULL_IMAGE_NAME"
        }
    }
}
