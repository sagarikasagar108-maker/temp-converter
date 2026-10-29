// Windows version (uses "bat" instead of "sh")
pipeline {
    agent any

    environment {
        // CHANGE THIS to your own Docker Hub username
        DOCKERHUB_USER = 'sagarikasagar'
        IMAGE          = "${DOCKERHUB_USER}/temp-converter"
        WEB_IMAGE       = "${DOCKERHUB_USER}/temp-converter-web"
    }

    // SCM trigger: Jenkins checks GitHub every 2 minutes and builds if there is a new commit
    triggers {
        pollSCM('H/2 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test (inside Docker)') {
            steps {
                // Compiles, runs JUnit tests and builds the image. Fails if a test fails.
                bat 'docker build -t %IMAGE%:%BUILD_NUMBER% -t %IMAGE%:latest .'
            }
        }

        stage('Security Scan (Trivy)') {
            steps {
                // If the scan itself has a problem, the stage turns yellow but the pipeline continues.
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    bat 'docker save -o image.tar %IMAGE%:%BUILD_NUMBER%'
                    bat 'docker run --rm -v trivy-cache:/root/.cache/ -v "%WORKSPACE%":/work aquasec/trivy:latest image --timeout 20m --scanners vuln --input /work/image.tar --severity HIGH,CRITICAL --exit-code 0'
                }
            }
        }

        stage('Run Container') {
            steps {
                bat 'docker run --rm %IMAGE%:%BUILD_NUMBER%'
                bat 'docker run --rm %IMAGE%:%BUILD_NUMBER% c2f 100'
            }
        }

                stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_PASS')]) {
                    bat 'echo %DH_PASS%| docker login -u %DH_USER% --password-stdin'
                    bat 'docker push %IMAGE%:%BUILD_NUMBER%'
                    bat 'docker push %IMAGE%:latest'
                    bat 'docker push %WEB_IMAGE%:%BUILD_NUMBER%'
                    bat 'docker push %WEB_IMAGE%:latest'
                }
            }
        }
    }

    post {
        success { echo 'Pipeline succeeded: image built, scanned and pushed.' }
        failure { echo 'Pipeline FAILED: check the console output.' }
        always  { bat(returnStatus: true, script: 'docker logout') }
    }
}
