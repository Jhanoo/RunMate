pipeline {
  agent any
  environment {
    REMOTE       = 'ubuntu@k12d107.p.ssafy.io'
    APPDIR       = '/home/ubuntu/runmate-backend'
    JAR_NAME     = 'app.jar'
    CONTAINER    = 'runmate-backend'
    COMPOSE_FILE = "${APPDIR}/docker-compose.yml"
  }

  stages {
    stage('Checkout') {
      steps {
        git(
          url: 'https://lab.ssafy.com/s12-final/S12P31D107.git',
          branch: 'develop/back',
          credentialsId: 'gitlab-https'
        )
      }
    }

    stage('Build JAR') {
      steps {
        sh './gradlew clean bootJar -x test'
      }
    }

    stage('Deploy to EC2') {
      steps {
        sshagent(['ec2-ssh']) {
          // 1) 빌드된 JAR 전송
          sh """
            scp -o StrictHostKeyChecking=no build/libs/*.jar \
                ${REMOTE}:${APPDIR}/${JAR_NAME}
          """

          // 2) EC2에서 docker-compose 재시작
          sh """
            ssh -o StrictHostKeyChecking=no ${REMOTE} \\
              'cd ${APPDIR} &&
               docker-compose down &&
               docker-compose up -d
              '
          """
        }
      }
    }
  }

  post {
    success { echo '✅ Deploy succeeded' }
    failure { echo '❌ Deploy failed' }
  }
}
