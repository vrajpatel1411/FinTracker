pipeline {
    agent { label 'linux1' }

    environment {
        PROJECT_ID = 'fintracker-466022'
        REGION = 'us-central1'
        CLUSTER = 'fintracker-cluster'
        REGISTRY = "us-central1-docker.pkg.dev/${PROJECT_ID}/fintracker"
    }

    tools {
        git 'Default'
        maven 'Maven-3.9.10'
        dockerTool 'docker'
    }

    stages {

        // ==== Fintracker Gateway Service ====
        stage("Clean Gateway Service") {
            steps {
                dir("${env.workspace}/Backend/FintrackerGateway") {
                    sh 'mvn clean'
                }
            }
        }

        stage("Package Gateway Service") {
            steps {
                dir("${env.workspace}/Backend/FintrackerGateway") {
                    sh 'mvn package -DskipTests'
                }
            }
        }

        stage("Build Gateway Docker Image") {
            steps {
                dir("${env.workspace}/Backend/FintrackerGateway") {
                    sh 'docker build -t fintrackergateway:latest .'
                }
            }
        }

        stage("Push Gateway Docker Image to GCR") {
            steps {
                dir("${env.workspace}/Backend/FintrackerGateway") {
                    withCredentials([file(credentialsId: 'GCP_SERVICE_ACCOUNT_KEY', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                        sh """
                            gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS
                            gcloud config set project $PROJECT_ID
                            gcloud auth configure-docker $REGION-docker.pkg.dev --quiet

                            docker tag fintrackergateway:latest $REGISTRY/gateway/fintrackergateway:latest
                            docker push $REGISTRY/gateway/fintrackergateway:latest
                        """
                    }
                }
            }
        }

        stage("Deploy Gateway to GKE") {
            steps {
                dir("${env.workspace}/Backend/FintrackerGateway") {
                    withCredentials([file(credentialsId: 'GCP_SERVICE_ACCOUNT_KEY', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                        sh """
                            gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS
                            gcloud config set project $PROJECT_ID
                            gcloud container clusters get-credentials $CLUSTER --region $REGION

                            kubectl apply -f k8s/Deployment.yaml
                            kubectl apply -f k8s/service.yaml
                            kubectl apply -f k8s/managed-cert.yaml
                            kubectl apply -f k8s/ingress.yaml
                            kubectl apply -f k8s/backend.yaml
                        """
                    }
                }
            }
        }

        // ==== User Authentication Service ====
        stage("Clean Auth Service") {
            steps {
                dir("${env.workspace}/Backend/UserAuthService") {
                    sh 'mvn clean'
                }
            }
        }

        stage("Package Auth Service") {
            steps {
                dir("${env.workspace}/Backend/UserAuthService") {
                    sh 'mvn package -DskipTests'
                }
            }
        }

        stage("Build Auth Docker Image") {
            steps {
                dir("${env.workspace}/Backend/UserAuthService") {
                    sh 'docker build -t userauth:latest .'
                }
            }
        }

        stage("Push Auth Docker Image to GCR") {
            steps {
                dir("${env.workspace}/Backend/UserAuthService") {
                    withCredentials([file(credentialsId: 'GCP_SERVICE_ACCOUNT_KEY', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                        sh """
                            gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS
                            gcloud config set project $PROJECT_ID
                            gcloud auth configure-docker $REGION-docker.pkg.dev --quiet

                            docker tag userauth:latest $REGISTRY/userauthentication/userauthentication:latest
                            docker push $REGISTRY/userauthentication/userauthentication:latest
                        """
                    }
                }
            }
        }

        stage("Deploy Auth to GKE") {
            steps {
                dir("${env.workspace}/Backend/UserAuthService") {
                    withCredentials([file(credentialsId: 'GCP_SERVICE_ACCOUNT_KEY', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                        sh """
                            gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS
                            gcloud config set project $PROJECT_ID
                            gcloud container clusters get-credentials $CLUSTER --region $REGION

                            kubectl apply -f k8s/secret.yaml
                            kubectl apply -f k8s/Deployment.yaml
                            kubectl apply -f k8s/service.yaml
                        """
                    }
                }
            }
        }
        stage("Build Frontend Docker Image") {
                    steps {
                        dir("${env.workspace}/Frontend/fintracker-frontend") {
                            sh 'docker build -t fintrackerfrontend:latest .'
                        }
                    }
        }
        stage("Push frontend Docker Image to GCR") {
                    steps {
                        dir("${env.workspace}/Frontend/fintracker-frontend") {
                            withCredentials([file(credentialsId: 'GCP_SERVICE_ACCOUNT_KEY', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                                sh """
                                    gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS
                                    gcloud config set project $PROJECT_ID
                                    gcloud auth configure-docker $REGION-docker.pkg.dev --quiet

                                    docker tag fintrackerfrontend:latest $REGISTRY/frontend/fintrackerfrontend:latest
                                    docker push $REGISTRY/frontend/fintrackerfrontend:latest
                                """
                            }
                        }
                    }
         }
         stage("Deploy frontend to GKE") {
                     steps {
                         dir("${env.workspace}/Frontend/fintracker-frontend") {
                             withCredentials([file(credentialsId: 'GCP_SERVICE_ACCOUNT_KEY', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                                 sh """
                                     gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS
                                     gcloud config set project $PROJECT_ID
                                     gcloud container clusters get-credentials $CLUSTER --region $REGION

                                     kubectl apply -f k8s/managed-cert.yaml
                                     kubectl apply -f k8s/Deployment.yaml
                                     kubectl apply -f k8s/service.yaml
                                     kubectl apply -f k8s/ingress.yaml
                                 """
                             }
                         }
                     }
                 }
    }
}
