        pipeline {
            /* insert Declarative Pipeline here */
            agent {
              label 'linux1'
            }

            tools {
              git 'Default'
              maven 'Maven-3.9.10'
              dockerTool 'docker'
            }

            stages {

                stage("clean gateway service"){
                    steps{
                     dir("${env.workspace}/Backend/FintrackerGateway"){
                        sh "mvn clean"
                     }
                     }
                }
                stage(" Package gateway service"){
                steps{
                    dir("${env.workspace}/Backend/FintrackerGateway"){
                        sh "mvn package -DskipTests"
                    }
                    }
                }
                stage("Build Docker Image"){
                    steps{
                    dir("${env.workspace}/Backend/FintrackerGateway"){
                        sh "docker build -t fintrackergateway:latest ."
                    }
                    }
                }
                stage("Push Docker Image to Google Cloud Registry"){
                    steps{dir("${env.workspace}/Backend/FintrackerGateway"){
                         withCredentials([file(credentialsId: 'GCP_SERVICE_ACCOUNT_KEY', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                                   sh """
                                                  gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS
                                                  gcloud config set project fintracker-466022
                                                  gcloud auth configure-docker us-central1-docker.pkg.dev --quiet

                                                  docker tag fintrackergateway:latest us-central1-docker.pkg.dev/fintracker-466022/fintracker/gateway/fintrackergateway:latest
                                                  docker push us-central1-docker.pkg.dev/fintracker-466022/fintracker/gateway/fintrackergateway:latest
                                              """
                                }
                    }
                    }
                }


            }


        }