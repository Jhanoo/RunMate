pipeline {
  agent any

  environment {
    REMOTE  = 'ubuntu@k12d107.p.ssafy.io'
    APPDIR  = '/home/ubuntu/runmate-backend'
  }

  stages {
    stage('Checkout') {
      steps {
        git url: 'https://lab.ssafy.com/s12-final/S12P31D107.git',
            branch: 'develop/back',
            credentialsId: 'gitlab-https'
      }
    }
    stage('Deploy to EC2 (build & run)') {
      steps {
        sshagent(['ec2-ssh']) {
          // here-doc 대신 단일 SSH 명령으로 변경
          sh """
            ssh -o StrictHostKeyChecking=no ${REMOTE} \\
              "cd ${APPDIR} && \\
               docker-compose down && \\
               docker-compose up -d --build"
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
