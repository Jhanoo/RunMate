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
    stage('Deploy to EC2 (pull & run)') {
      steps {
        sshagent(['ec2-ssh']) {
          sh """
            ssh -o StrictHostKeyChecking=no ${REMOTE} \\
            'set -e
            cd ${APPDIR}

            # --- origin URL 을 SSH 로 변경 ---
            git remote set-url origin git@lab.ssafy.com:s12-final/S12P31D107.git

            # 1) repo 없으면 clone, 있으면 pull
            if [ ! -d ".git" ]; then
              echo "→ 최초 배포: repo clone"
              git clone -b develop/back git@lab.ssafy.com:s12-final/S12P31D107.git .
            else
              echo "→ 기존 repo pull"
              git fetch origin develop/back
              git reset --hard origin/develop/back
            fi

            # 2) Docker-compose 재시작
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