// Windows version (uses "bat" instead of "sh")
pipeline {
    agent any

    environment {
        DOCKERHUB_USER = 'sagarikasagar'
        IMAGE          = "${DOCKERHUB_USER}/temp-converter"
        WEB_IMAGE      = "${DOCKERHUB_USER}/temp-converter-web"
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
                bat 'docker build -t %IMAGE%:%BUILD_NUMBER% -t %IMAGE%:latest .'
            }
        }

        stage('Security Scan (Trivy)') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    bat 'docker save -o image.tar %IMAGE%:%BUILD_NUMBER%'
                    bat 'docker run --rm -v trivy-cache:/root/.cache/ -v "%WORKSPACE%":/work aquasec/trivy:latest image --timeout 20m --scanners vuln --skip-db-update --java-db-repository ghcr.io/aquasecurity/trivy-java-db:1 --input /work/image.tar --severity HIGH,CRITICAL --exit-code 0'
                }
            }
        }

        stage('Run Container') {
            steps {
                bat 'docker run --rm %IMAGE%:%BUILD_NUMBER%'
                bat 'docker run --rm %IMAGE%:%BUILD_NUMBER% c2f 100'
            }
        }

        stage('Build Frontend Image') {
            steps {
                bat 'docker build -f Dockerfile.web -t %WEB_IMAGE%:%BUILD_NUMBER% -t %WEB_IMAGE%:latest .'
            }
        }

        stage('Run Frontend') {
            steps {
                bat(returnStatus: true, script: 'docker rm -f temp-web')
                bat 'docker run -d -p 8081:80 --name temp-web %WEB_IMAGE%:%BUILD_NUMBER%'
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
