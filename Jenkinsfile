pipeline {
    // agent {
    //     label 'maven'
    // }
    stages {

        stage("Maven build") {
            steps {
                script {
                    node("maven") {
                        def pom = readMavenPom file: "pom.xml"
                        sh "mvn clean package -DskipTests"
                        APP_VERSION = pom.version
                        artifactId = pom.artifactId
                        groupId = pom.groupId.replace(".", "/")
                        packaging = pom.packaging
                        NEXUS_ARTIFACT_PATH = "${groupId}/${artifactId}/${APP_VERSION}/${artifactId}-${APP_VERSION}.${packaging}"
                        echo "Artifact = ${NEXUS_ARTIFACT_PATH}"
                    }
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
            steps {
                script {
                    openshift.withCluster() {
                        openshift.newBuild("--name=prometeo", "--image-stream=jansible:latest", "--binary")
                    }
                }
            }
        }

        stage('Build Application Image') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.selector("bc", "prometeo").startBuild("--from-file=target/${artifactId}-${APP_VERSION}.${packaging}", "--wait")
                    }
                }
            }
        }

        stage('Dev Deployment') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    input 'Do you approve deployment?'
                }
            }
        }

        stage('Promote to DEV') {
            steps {
                script {
                openshift.withCluster() {
                    openshift.tag("prometeo:latest", "prometeo:dev")
                    }
                }
            }
        }
    }
}