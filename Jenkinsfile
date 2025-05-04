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
          sh """
            ssh -o StrictHostKeyChecking=no ${REMOTE} \\
            'set -e
             # 1) 원격 디렉토리 git 상태 확인
             if [ ! -d "${APPDIR}/.git" ]; then
               echo "→ 최초 배포: repo clone"
               git clone -b develop/back git@lab.ssafy.com:s12-final/S12P31D107.git ${APPDIR}
             else
               echo "→ 기존 repo pull"
               cd ${APPDIR}
               git fetch origin develop/back
               git reset --hard origin/develop/back
             fi

             # 2) Docker Compose 로 다시 빌드 & 재시작
             cd ${APPDIR}
             docker-compose down
             docker-compose up -d --build
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