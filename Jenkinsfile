pipeline {
    agent any

    environment {
        PROJECT_ID = 'fintracker-466620'
        REGION = 'us-central1'
        CLUSTER = 'fintracker-cluster'
        REGISTRY = "us-central1-docker.pkg.dev/${PROJECT_ID}/fintracker-docker-registry"
        GOOGLE_APPLICATION_CREDENTIALS = credentials('GCP_SERVICE_ACCOUNT_KEY')
    }

    tools {
        git 'Default'
        maven 'maven-3.9.11'
        dockerTool 'docker'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    COMMIT_SHA = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.COMMIT_SHA = COMMIT_SHA
                }
            }
        }

        stage('GCP Auth') {
            steps {
                sh '''
                    gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS
                    gcloud config set project $PROJECT_ID
                    gcloud auth configure-docker $REGION-docker.pkg.dev --quiet
                    gcloud container clusters get-credentials $CLUSTER --region $REGION
                '''
            }
        }

        stage('Build & Deploy Gateway Service') {
            // when {
            //     expression { sh(script: "git diff --name-only HEAD~1 HEAD | grep ^Backend/FintrackerGateway", returnStatus: true) == 0 }
            // }
            steps {
                dir('Backend/FintrackerGateway') {
                    sh '''
                        mvn clean package -DskipTests
                        docker build -t $REGISTRY/gateway/fintrackergateway:$COMMIT_SHA .
                        docker push $REGISTRY/gateway/fintrackergateway:$COMMIT_SHA

                        sed "s|image:.*|image: $REGISTRY/gateway/fintrackergateway:$COMMIT_SHA|" k8s/Deployment.yaml > k8s/Deployment-patched.yaml

                        kubectl apply -f k8s/service.yaml
                        kubectl apply -f k8s/managed-cert.yaml
                        kubectl apply -f k8s/ingress.yaml
                        kubectl apply -f k8s/backend.yaml
                        kubectl apply -f k8s/Deployment-patched.yaml
                    '''
                }
            }
        }

        stage('Build & Deploy Auth Service') {
            // when {
            //     expression { sh(script: "git diff --name-only HEAD~1 HEAD | grep ^Backend/UserAuthService", returnStatus: true) == 0 }
            // }
            steps {
                dir('Backend/UserAuthService') {
                    sh '''
                        mvn clean package -DskipTests
                        docker build -t $REGISTRY/userauthentication/userauthentication:$COMMIT_SHA .
                        docker push $REGISTRY/userauthentication/userauthentication:$COMMIT_SHA

                        sed "s|image:.*|image: $REGISTRY/userauthentication/userauthentication:$COMMIT_SHA|" k8s/Deployment.yaml > k8s/Deployment-patched.yaml

                       
                        kubectl apply -f k8s/service.yaml
                        kubectl apply -f k8s/Deployment-patched.yaml
                    '''
                }
            }
        }

        stage('Build & Deploy Frontend') {
            // when {
            //     expression { sh(script: "git diff --name-only HEAD~1 HEAD | grep ^Frontend/fintracker-frontend", returnStatus: true) == 0 }
            // }
            steps {
                dir('Frontend/fintracker-frontend') {
                    sh '''
                        docker build -t $REGISTRY/frontend/fintrackerfrontend:$COMMIT_SHA .
                        docker push $REGISTRY/frontend/fintrackerfrontend:$COMMIT_SHA

                        sed "s|image:.*|image: $REGISTRY/frontend/fintrackerfrontend:$COMMIT_SHA|" k8s/Deployment.yaml > k8s/Deployment-patched.yaml

                        kubectl apply -f k8s/service.yaml
                        kubectl apply -f k8s/managed-cert.yaml
                        kubectl apply -f k8s/ingress.yaml
                        kubectl apply -f k8s/Deployment-patched.yaml
                     '''
               }
            }
        
        }
        stage('Build & Deploy Personal Expense Service') {
            // when {
            //     expression { sh(script: "git diff --name-only HEAD~1 HEAD | grep ^Frontend/fintracker-frontend", returnStatus: true) == 0 }
            // }
            steps {
                dir('Backend/personalExpense') {
                    sh '''
                        mvn clean package -DskipTests
                        docker build -t $REGISTRY/personalexpense/personalexpense:$COMMIT_SHA .
                        docker push $REGISTRY/personalexpense/personalexpense:$COMMIT_SHA

                        sed "s|image:.*|image: $REGISTRY/personalexpense/personalexpense:$COMMIT_SHA|" k8s/Deployment.yaml > k8s/Deployment-patched.yaml

                        kubectl apply -f k8s/service.yaml
                        kubectl apply -f k8s/secrets.yaml
                        kubectl apply -f k8s/Deployment-patched.yaml
                    '''
                }
            }
        }
    }

    post {
        always {
            script {
                // Clean up workspace
                cleanWs()
            }
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}

