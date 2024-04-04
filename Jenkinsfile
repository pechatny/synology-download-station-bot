pipeline {
    environment {
        imageName = "nexus.pechatny.synology.me:4445/repository/my-local-docker-repo/dsget-bot-app:$BUILD_NUMBER"
        registryCredentials = "nexus-jenkins-docker"
        registry = "https://nexus.pechatny.synology.me:4445"
        dockerImage = ''
    }
    agent any
    stages {
        stage('Git checkout'){
            steps{
                git credentialsId: '4851be6a-eea6-40b6-9004-53706128ca31', url: 'http://pechatny.synology.me:10080/d.pechatnikov/synology-download-station-bot.git'
            }
        }
        stage('Build image'){
            steps{
                script{
                    dockerImage = docker.build imageName
                }
            }
        }
        stage('Docker Run') {
            steps {
                script {
                    sh 'docker run -d --rm --name ds-get-telegram-bot-ci -e ENVIRONMENT_PROFILE_NAME=\'synology\' ' + imageName
                }
            }
        }
    }
}
