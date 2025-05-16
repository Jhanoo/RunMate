pipeline {
    agent any

    environment {
        REMOTE    = 'ubuntu@k12d107.p.ssafy.io'
        APPDIR    = '/home/ubuntu/runmate-backend'
        JAR_NAME  = 'app.jar'
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

        stage('Grant Permissions') {
            steps {
                // gradlew 실행 권한 추가
                sh 'chmod +x backend/spring-boot/gradlew'
            }
        }

        stage('Build JAR') {
            steps {
                // spring-boot 디렉토리로 이동해서 빌드
                sh 'cd backend/spring-boot && ./gradlew clean bootJar -x test'
            }
        }

        stage('Build Node.js') {
            steps {
                // Node.js 애플리케이션 의존성 설치 및 빌드
                sh '''
                    cd backend/nodejs
                    npm ci
                    npm run build
                '''
            }
        }

        stage('Prepare Marathon Crawler') {
            steps {
                // 마라톤 크롤러 디렉토리 생성 및 필요한 파일 준비
                sh '''
                    mkdir -p backend/marathon-crawler/logs
                    touch backend/marathon-crawler/.env
                '''
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent(['ec2-ssh']) {
                    // 빌드된 JAR 파일을 EC2로 전송
                    sh """
                        scp -o StrictHostKeyChecking=no backend/spring-boot/build/libs/*.jar \
                            ${REMOTE}:${APPDIR}/${JAR_NAME}
                    """
                    
                    // Node.js 애플리케이션 디렉토리 복사
                    sh """
                        ssh -o StrictHostKeyChecking=no ${REMOTE} 'mkdir -p ${APPDIR}/backend/nodejs'
                        
                        scp -o StrictHostKeyChecking=no -r backend/nodejs/package*.json \
                            backend/nodejs/tsconfig.json \
                            backend/nodejs/src \
                            backend/nodejs/Dockerfile \
                            ${REMOTE}:${APPDIR}/backend/nodejs/
                    """
                    
                    // 마라톤 크롤러 디렉토리 복사
                    sh """
                        ssh -o StrictHostKeyChecking=no ${REMOTE} 'mkdir -p ${APPDIR}/backend/marathon-crawler/logs'
                        
                        scp -o StrictHostKeyChecking=no -r backend/marathon-crawler/Dockerfile \
                            backend/marathon-crawler/requirements.txt \
                            backend/marathon-crawler/app.py \
                            backend/marathon-crawler/src \
                            backend/marathon-crawler/config \
                            backend/marathon-crawler/scripts \
                            backend/marathon-crawler/.env \
                            ${REMOTE}:${APPDIR}/backend/marathon-crawler/
                    """
                    
                    // docker-compose 재시작
                    sh """
                        ssh -o StrictHostKeyChecking=no ${REMOTE} \\
                            'cd ${APPDIR} &&
                             echo "Docker 컨테이너 중지 및 제거" &&
                             docker-compose down --remove-orphans &&
                             echo "새 컨테이너 시작" &&
                             docker-compose up -d
                            '
                    """
                }
            }
        }

        stage('Notification') {
            steps {
                echo 'jenkins notification!'
            }
            post {
                success {
                    script {
                        def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                        def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
                        mattermostSend(color: 'good',
                            message: "빌드 성공: ${env.JOB_NAME} #${env.BUILD_NUMBER} by ${Author_ID}(${Author_Name})\n(<${env.BUILD_URL}|Details>)",
                            endpoint: 'https://meeting.ssafy.com/hooks/zcfm5oym4pdybkp9fbjia331rw',
                            channel: 'D107git'
                        )
                    }
                }
                failure {
                    script {
                        def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                        def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
                        mattermostSend(color: 'danger',
                            message: "빌드 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER} by ${Author_ID}(${Author_Name})\n(<${env.BUILD_URL}|Details>)",
                            endpoint: 'https://meeting.ssafy.com/hooks/zcfm5oym4pdybkp9fbjia331rw',
                            channel: 'D107git'
                        )
                    }
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
