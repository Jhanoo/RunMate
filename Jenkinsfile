pipeline {
  agent any

  environment {
    REMOTE = 'ubuntu@k12d107.p.ssafy.io'
    APPDIR = '/home/ubuntu/runmate-backend'
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

    stage('Deploy to EC2 (build & run)') {
      steps {
        sshagent(['ec2-ssh']) {
          sh """
            ssh -o StrictHostKeyChecking=no ${REMOTE} << 'EOF'
              cd ${APPDIR}
              docker-compose down
              docker-compose up -d --build
            EOF
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
