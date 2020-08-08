pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'gradle build'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            cleanWs()
        }
    }
}
