pipeline {
    agent { label 'jenkins-agent' }  // Ensure this label matches your autoscaling group nodes

    environment {
        REPO_URL = 'https://github.com/PaulBarbat/Card_Game_66.git'
        BUILD_DIR = 'build'
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Cloning C++ repository..."
                git branch: 'main', url: "${REPO_URL}"
            }
        }

        stage('Build') {
            steps {
                echo "Building the C++ project..."
                sh '''
                mkdir -p ${BUILD_DIR}
                cd ${BUILD_DIR}
                cmake ..
                make -j$(nproc)
                ls -ll
                '''
            }
        }

        stage('Test') {
            steps {
                echo "Running tests..."
                sh '''
                cd ${BUILD_DIR}
                ctest --output-on-failure
                '''
            }
        }

        stage('Upload to S3'){
            steps{
                script{
                    sh '''
                    cd ${BUILD_DIR}
                    pwd
                    ls -ll
                    '''
                    def executable_path = sh(script: "ls Card_Game_66-*", returnStdout: true).trim()
                    def executable = executable_path.split('/').last()
                    def executable_with_build_number = "${executable}-${env.BUILD_NUMBER}"
                    withAWS(region: 'eu-central-1', credentials: 'bd77ee04-1eed-4e13-ad2b-b4ad37857124') {
                    sh '''
                    ls -ll
                    pwd
                    aws s3 cp ${executable_with_build_number} s3://card-game-66-personal-bucket/builds/${executable_with_build_number}
                    '''
                    }
                }
                
            }
        }
    }

    post {
        success {
            echo "Build and tests completed successfully!"
        }
        failure {
            echo "Build failed! Check logs for details."
        }
    }
}
