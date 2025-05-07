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
                // gradlew 파일에 실행 권한 추가
                sh 'chmod +x gradlew'
            }
        }

        stage('Build JAR') {
            steps {
                // 이제 실행 권한 있으니 빌드 가능
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
                            endpoint: 'https://meeting.ssafy.com/hooks/zgysdij1afygmbfxq1w478owmc',
                            channel: 'd107 - gitlab'
                        )
                    }
                }
                failure {
                    script {
                        def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                        def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
                        mattermostSend(color: 'danger',
                            message: "빌드 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER} by ${Author_ID}(${Author_Name})\n(<${env.BUILD_URL}|Details>)",
                            endpoint: 'https://meeting.ssafy.com/hooks/zgysdij1afygmbfxq1w478owmc',
                            channel: 'd107 - gitlab'
                        )
                    }
                }
            }
        }
    }

    post {
        success { echo '✅ Deploy succeeded' }
        failure { echo '❌ Deploy failed' }
    }
}

