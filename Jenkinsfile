pipeline {
  agent any
  environment {
    // EC2 배포 대상 정보
    REMOTE_HOST = 'ubuntu@k12d107.p.ssafy.io'
    APP_DIR     = '/home/ubuntu/runmate-backend'
  }
  stages {
    stage('Checkout') {
      steps {
        // develop/back 브랜치에서 체크아웃
        git url: 'https://lab.ssafy.com/s12-final/S12P31D107.git',
            branch: 'develop/back',
            credentialsId: 'gitlab-https'
      }
    }
    stage('Build Docker Image') {
      steps {
        sh 'docker build -t runmate-backend:latest .'
      }
    }
    stage('Deploy to EC2') {
      steps {
        sshagent(['ec2-ssh']) {
          sh """
            ssh -o StrictHostKeyChecking=no ${REMOTE_HOST} << 'EOF'
              cd ${APP_DIR}
              docker-compose down
              docker-compose up -d --build
            EOF
          """
        }
      }
    }
  }
  post {
    success {
      echo '✅ Deploy succeeded'
    }
    failure {
      echo '❌ Deploy failed'
    }
  }
}
