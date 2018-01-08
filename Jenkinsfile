pipeline {
  agent {
      label 'maven'
  }
  stages {
    // stage('Build App') {

    // // download and configure all common cicd stuff		 
    // dir('cicd') {
    //     def payloadString = build.buildVariableResolver.resolve("payload")

    //     payloadObject = new groovy.json.JsonSlurper().parseText(payloadString)

    //     targetCommit = payloadObject.pull_request.head.sha

    //     echo "Artifact = ${targetCommit}"

    //     // download all cicd required files		
    //     // git "${params.CICD_GIT_URL}"
    //     // load openshift-utils functions (using this path as convention.. define a env var if desired...)		
    //     // openshiftUtils = load 'pipeline/functions/openshift-utils.groovy'
    //     // load groovy functions		
    //     // newman = load 'pipeline/functions/newman.groovy'

    // }
    // }

    stage("Maven build") {
        steps {
            // Get source code from repository
            // git "${params.APP_GIT_URL}"

            // extract info from pom.xml
            def pom = readMavenPom file: "pom.xml"

            sh "mvn clean package -DskipTests"   
 
            // global variable
            APP_VERSION = pom.version
            artifactId = pom.artifactId
            groupId = pom.groupId.replace(".", "/")
            packaging = pom.packaging
            NEXUS_ARTIFACT_PATH = "${groupId}/${artifactId}/${APP_VERSION}/${artifactId}-${APP_VERSION}.${packaging}"  
            echo "Artifact = ${NEXUS_ARTIFACT_PATH}"  
        }     
    }

    stage("Openshift Image build"){
        steps {
            script{
                openshift.withCluster() {
                def bc = created.narrow('bc')
                echo "Starting binary build in project ${openshift.project()} for application ${NEXUS_ARTIFACT_PATH}"
                openshift.withProject() {
                    def buildartifact="${artifactId}-${APP_VERSION}.${packaging}"
                    echo "Using file ${buildartifact} in build"

                    def build = openshift.startBuild("prometeo", "--from-file=./target/${buildartifact}")
                    build.describe()
                    build.watch {
                        return it.object().status.phase == "Complete"
                    }
                    def images = openshift.selector("imagestream")
                    images.withEach { // The closure body will be executed once for each selected object.
        // The 'it' variable will be bound to a Selector which selects a single
        // object which is the focus of the iteration.
                        echo "Images: ${it.name()} is defined in ${openshift.project()}"
                    }
                }
            }}
        }
    }
}
}