pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'chmod 0755 gradlew'
                sh './gradlew publish'
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
