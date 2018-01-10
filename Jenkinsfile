pipeline {
    agent none

    stages {
        stage("Init"){
            agent any
            steps{
                script {
                    sh "oc version"
                }
            }
        }

        stage('Create Image Builder Prometeo') {
            when {
                expression {
                    openshift.withCluster() {
                        return !openshift.selector("bc", "prometeo").exists();
                    }
                }
            }
            agent any
            steps { 
                script {
                    openshift.withCluster() {
                        openshift.newBuild("--name=prometeo", "--image-stream=jansible:latest", "--binary")
                    }
                }
            }
        }

        stage("Maven build") {
            agent { label 'maven' }
            steps {
                script {
                        def pom = readMavenPom file: "pom.xml"
                        sh "mvn clean package -DskipTests"
                        APP_VERSION = pom.version
                        artifactId = pom.artifactId
                        groupId = pom.groupId.replace(".", "/")
                        packaging = pom.packaging
                        NEXUS_ARTIFACT_PATH = "${groupId}/${artifactId}/${APP_VERSION}/${artifactId}-${APP_VERSION}.${packaging}"
                        echo "Building container image with artifact = ${NEXUS_ARTIFACT_PATH}"

                        // This is here until we get the Nexus repo setup
                        openshift.withCluster() {
                            openshift.selector("bc", "prometeo").startBuild("--from-file=target/${artifactId}-${APP_VERSION}.${packaging}", "--wait")
                        }
                    }
                }
        }
        

        // stage('Build Application Image') {
        //     agent { label 'maven' }
        //     steps {
        //         script {
        //             openshift.withCluster() {
        //                 openshift.selector("bc", "prometeo").startBuild("--from-file=target/${artifactId}-${APP_VERSION}.${packaging}", "--wait")
        //             }
        //         }
        //     }
        // }

        stage('Dev Deployment') {
            agent any
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    input 'Do you approve deployment?'
                }
            }
        }

        stage('Promote to DEV') {
            agent any
            steps {
                script {
                openshift.withCluster() {
                    openshift.tag("prometeo:latest", "prometeo:dev")
                    }
                }
            }
        }

        stage('Create app if not already there') {
            agent any
            when {
                expression {
                    openshift.withCluster() {
                        return !openshift.selector("dc", "prometeo-dev").exists();
                    }
                }
            }
            steps {
                 script {
                    openshift.withCluster() {
                        openshift.newApp("prometeo:dev", "--name=prometeo-dev").narrow('svc').expose()
                    }
                    sleep 2
                    sh "oc set triggers dc/prometeo-dev --manual"
                    sh "oc volume dc/prometeo-dev --add -t secret -m /tmp/secrets --secret-name=mongodb --name=mongodb-secret"
                    sh "oc volume dc/prometeo-dev --add -t secret -m /app/.ssh/keys --secret-name='sshkey' --default-mode='0600'"
                    sh "oc set triggers dc/prometeo-dev --auto"
                }
            }
        }
    }
}
