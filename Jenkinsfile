pipeline {
    agent any
    environment {
        DOCKER_IMAGE = 'spring-app'
        IMAGE_TAG = "${BUILD_NUMBER}"
        NEXUS_REGISTRY = '52.60.137.39:8083'
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
                    '''
                }
            }
        }

        stage('Configure EKS') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds-id']]) {
                    sh 'aws eks update-kubeconfig --region ca-central-1 --name nk'
                }
            }
        }

        stage('Apply K8s YAML (if needed)') {
            steps {
                script {
                    def deploymentExists = sh (
                        script: "kubectl get deployment spring-app --namespace=default || true",
                        returnStdout: true
                    ).trim()

                    if (deploymentExists.contains('NotFound')) {
                        echo "spring-app deployment not found. Applying deployment and service YAML..."
                        sh 'kubectl apply -f k8s/deployment.yaml'
                        sh 'kubectl apply -f k8s/service.yaml'
                    } else {
                        echo "spring-app deployment already exists. Skipping YAML apply."
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                kubectl set image deployment/spring-app spring-app=$FULL_IMAGE_NAME --namespace=default
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
